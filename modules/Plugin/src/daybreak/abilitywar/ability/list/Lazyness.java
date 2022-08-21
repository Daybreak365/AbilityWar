package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.SubscribeEvent.Priority;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Arrow;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@AbilityManifest(name = "지금의 일은 나중의 나에게", rank = AbilityManifest.Rank.A, species = AbilityManifest.Species.HUMAN, explain = {
		"지금 받을 대미지와 회복을 3초 뒤의 나에게 미루고, 넉백을 무시합니다.",
		"철괴를 우클릭하면 미뤄진 모든 대미지를 지금 바로 0.75배로 줄여 받습니다.",
		"$[COOLDOWN_CONFIG]"
}, summarize = {
		"§c피해§f와 §a회복§f을 3초 뒤로 미룹니다.",
		"미뤄진 모든 §c피해§f를 §7철괴 우클릭§f으로 즉시 §30.75§f배로 줄여 받을 수 있습니다."
})
public class Lazyness extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Lazyness.class, "cooldown", 30,
			"# 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};

	public static final SettingObject<Integer> INVINCIBLE_TICKS = abilitySettings.new SettingObject<Integer>(Lazyness.class, "invincible-ticks", 10,
			"# 무적 틱") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

	};

	public Lazyness(AbstractGame.Participant participant) {
		super(participant);
	}

	private DamageTimer lastDamage = null;
	private final Set<DamageTimer> timers = new CopyOnWriteArraySet<>();
	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());

	private long lastDamageMillis = System.currentTimeMillis();
	private final int invincibleTicks = INVINCIBLE_TICKS.getValue();

	@SubscribeEvent(ignoreCancelled = true, priority = Priority.HIGHEST)
	private void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity().equals(getPlayer())) {
			final long current = System.currentTimeMillis();
			if (current - lastDamageMillis >= invincibleTicks * 50) {
				final DamageTimer damageTimer = new DamageTimer(e, e.getFinalDamage() - e.getDamage(DamageModifier.ABSORPTION));
				this.lastDamage = damageTimer;
				timers.add(damageTimer);
				this.lastDamageMillis = current;
			} else if (lastDamage != null) {
				final double damage = e.getFinalDamage() - e.getDamage(DamageModifier.ABSORPTION);
				if (lastDamage.damage < damage) {
					lastDamage.event = e;
					lastDamage.damage = damage;
					lastDamage.setCount(3);
				}
			}
			e.setCancelled(true);
		}
	}

	@SubscribeEvent(ignoreCancelled = true, priority = Priority.HIGHEST)
	private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.getDamager() instanceof Arrow) {
			e.getDamager().remove();
		}
		onEntityDamage(e);
	}

	@SubscribeEvent(ignoreCancelled = true, onlyRelevant = true, priority = Priority.HIGHEST)
	private void onEntityRegainHealth(final EntityRegainHealthEvent e) {
		e.setCancelled(true);
		new RegainTimer(e.getAmount());
	}

	@SubscribeEvent(ignoreCancelled = true, priority = Priority.HIGHEST)
	private void onEntityDamageByBlock(EntityDamageByBlockEvent e) {
		onEntityDamage(e);
	}

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !cooldownTimer.isCooldown()) {
			if (!timers.isEmpty()) {
				for (DamageTimer timer : timers) {
					timer.damage *= 0.75;
					timer.stop(false);
				}
				cooldownTimer.start();
				return true;
			} else {
				getPlayer().sendMessage("§3받을 대미지§f가 없습니다.");
			}
		}
		return false;
	}

	private class DamageTimer extends AbilityTimer {

		private final ActionbarChannel channel;
		private EntityDamageEvent event;
		private double damage;

		private DamageTimer(EntityDamageEvent event, double damage) {
			super(3);
			this.channel = newActionbarChannel();
			this.event = event;
			this.damage = damage;
			start();
		}

		@Override
		protected void run(int count) {
			channel.update(ChatColor.YELLOW.toString() + count + ChatColor.WHITE + "초: " + ChatColor.RED.toString() + "-" + (Math.round(damage * 100.0) / 100.0));
		}

		@Override
		protected void onEnd() {
			timers.remove(this);
			if (this.equals(lastDamage)) {
				lastDamage = null;
			}
			double toDamage = damage;
			final float absorptionHearts = NMS.getAbsorptionHearts(getPlayer());
			if (!getPlayer().isDead()) {
				if (absorptionHearts > 0) {
					if (toDamage <= absorptionHearts) {
						NMS.setAbsorptionHearts(getPlayer(), (float) (absorptionHearts - toDamage));
					} else {
						NMS.setAbsorptionHearts(getPlayer(), 0);
						toDamage -= absorptionHearts;
						getPlayer().setHealth(Math.max(getPlayer().getHealth() - toDamage, 0.0));
					}
				} else {
					getPlayer().setHealth(Math.max(getPlayer().getHealth() - toDamage, 0.0));
				}
				NMS.broadcastEntityEffect(getPlayer(), (byte) 2);
				getPlayer().setLastDamageCause(event);
			}
			channel.unregister();
		}

		@Override
		protected void onSilentEnd() {
			timers.remove(this);
			if (this.equals(lastDamage)) {
				lastDamage = null;
			}
			channel.unregister();
		}

	}

	private class RegainTimer extends AbilityTimer {

		private final ActionbarChannel channel;
		private final double health;

		private RegainTimer(double health) {
			super(3);
			this.channel = newActionbarChannel();
			this.health = health;
			start();
		}

		@Override
		protected void run(int count) {
			channel.update(ChatColor.YELLOW.toString() + count + ChatColor.WHITE + "초: " + ChatColor.GREEN.toString() + "+" + (Math.round(health * 100.0) / 100.0));
		}

		@Override
		protected void onEnd() {
			if (!getPlayer().isDead()) {
				getPlayer().setHealth(Math.max(0, Math.min(getPlayer().getHealth() + health, getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue())));
			}
			channel.unregister();
		}

		@Override
		protected void onSilentEnd() {
			channel.unregister();
		}

	}

}
