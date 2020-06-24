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
import daybreak.abilitywar.utils.base.minecraft.compat.nms.NMSHandler;
import daybreak.abilitywar.utils.library.SoundLib;
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
		"$[CooldownConfig]"
})
public class Lazyness extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> CooldownConfig = abilitySettings.new SettingObject<Integer>(Lazyness.class, "Cooldown", 30,
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

	private final Set<DamageTimer> timers = new CopyOnWriteArraySet<>();
	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());

	@SubscribeEvent(ignoreCancelled = true, priority = Priority.HIGHEST)
	private void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity().equals(getPlayer())) {
			timers.add(new DamageTimer(e.getFinalDamage() - e.getDamage(DamageModifier.ABSORPTION)));
			getPlayer().setNoDamageTicks(getPlayer().getMaximumNoDamageTicks());
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
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !cooldownTimer.isCooldown()) {
			for (DamageTimer timer : timers) {
				timer.damage *= 0.65;
				timer.stop(false);
			}
			cooldownTimer.start();
			return true;
		}
		return false;
	}

	private class DamageTimer extends Timer {

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
			SoundLib.ENTITY_PLAYER_HURT.playSound(getPlayer());
			double toDamage = damage;
			final float absorptionHearts = NMSHandler.getNMS().getAbsorptionHearts(getPlayer());
			if (!getPlayer().isDead()) {
				if (absorptionHearts > 0) {
					if (toDamage <= absorptionHearts) {
						NMSHandler.getNMS().setAbsorptionHearts(getPlayer(), (float) (absorptionHearts - toDamage));
					} else {
						NMSHandler.getNMS().setAbsorptionHearts(getPlayer(), 0);
						toDamage -= absorptionHearts;
						getPlayer().setHealth(Math.max(getPlayer().getHealth() - toDamage, 0.0));
					}
				} else {
					getPlayer().setHealth(Math.max(getPlayer().getHealth() - toDamage, 0.0));
				}
			}
			channel.unregister();
		}

		@Override
		protected void onSilentEnd() {
			timers.remove(this);
			channel.unregister();
		}

	}

}
