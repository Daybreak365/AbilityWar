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
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;

@AbilityManifest(name = "지금의 일은 나중의 나에게", rank = AbilityManifest.Rank.A, species = AbilityManifest.Species.HUMAN, explain = {
		"지금 받을 대미지를 3초 뒤의 나에게 미루고, 넉백을 무시합니다.",
		"철괴를 우클릭하면 미뤄진 모든 대미지를 지금 바로 0.65배로 줄여 받습니다.",
		"$[COOLDOWN_CONFIG]"
})
public class Lazyness extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Lazyness.class, "Cooldown", 30,
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

	public Lazyness(AbstractGame.Participant participant) {
		super(participant);
	}

	private DamageTimer lastDamage = null;
	private final Set<DamageTimer> timers = new CopyOnWriteArraySet<>();
	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());

	private long lastDamageMillis = System.currentTimeMillis();

	@SubscribeEvent(ignoreCancelled = true, priority = Priority.HIGHEST)
	private void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity().equals(getPlayer())) {
			final long current = System.currentTimeMillis();
			if (current - lastDamageMillis >= getPlayer().getMaximumNoDamageTicks() * 50) {
				final DamageTimer damageTimer = new DamageTimer(e.getFinalDamage() - e.getDamage(DamageModifier.ABSORPTION));
				this.lastDamage = damageTimer;
				timers.add(damageTimer);
				this.lastDamageMillis = current;
			} else if (lastDamage != null) {
				final double damage = e.getFinalDamage() - e.getDamage(DamageModifier.ABSORPTION);
				if (lastDamage.damage < damage) {
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

	@SubscribeEvent(ignoreCancelled = true, priority = Priority.HIGHEST)
	private void onEntityDamageByBlock(EntityDamageByBlockEvent e) {
		onEntityDamage(e);
	}

	@Override
	public boolean ActiveSkill(Material material, ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !cooldownTimer.isCooldown()) {
			if (!timers.isEmpty()) {
				for (DamageTimer timer : timers) {
					timer.damage *= 0.65;
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
		private double damage;

		private DamageTimer(double damage) {
			super(3);
			this.channel = newActionbarChannel();
			this.damage = damage;
			start();
		}

		@Override
		protected void run(int count) {
			channel.update(ChatColor.YELLOW.toString() + count + ChatColor.WHITE + "초 뒤 " + ChatColor.AQUA.toString() + (Math.round(damage * 100.0) / 100.0) + ChatColor.WHITE + " 대미지");
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

}
