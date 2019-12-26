package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.games.mode.AbstractGame;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

@AbilityManifest(Name = "지금의 일은 나중의 나에게", Rank = AbilityManifest.Rank.A, Species = AbilityManifest.Species.HUMAN)
public class Lazyness extends AbilityBase {

	public Lazyness(AbstractGame.Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f지금 받을 데미지를 3초 뒤의 나에게 미룹니다."));
	}

	@SubscribeEvent
	private void onPlayerDamage(EntityDamageEvent e) {
		if (e.getEntity().equals(getPlayer())) {
			new DamageTimer(e.getFinalDamage());
			getPlayer().setNoDamageTicks(getPlayer().getMaximumNoDamageTicks());
			e.setCancelled(true);
		}
	}

	@SubscribeEvent
	private void onPlayerDamage(EntityDamageByEntityEvent e) {
		if (e.getEntity().equals(getPlayer())) {
			new DamageTimer(e.getFinalDamage());
			getPlayer().setNoDamageTicks(getPlayer().getMaximumNoDamageTicks());
			e.setCancelled(true);
		}
	}

	@SubscribeEvent
	private void onPlayerDamage(EntityDamageByBlockEvent e) {
		if (e.getEntity().equals(getPlayer())) {
			new DamageTimer(e.getFinalDamage());
			getPlayer().setNoDamageTicks(getPlayer().getMaximumNoDamageTicks());
			e.setCancelled(true);
		}
	}

	@Override
	public boolean ActiveSkill(Material materialType, ClickType ct) {
		return false;
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
	}

	private class DamageTimer extends Timer {

		private final double damage;

		private DamageTimer(double damage) {
			super(3);
			this.damage = damage;
			startTimer();
		}

		@Override
		protected void onProcess(int count) {
		}

		@Override
		protected void onEnd() {
			SoundLib.ENTITY_PLAYER_HURT.playSound(getPlayer());
			if (!getPlayer().isDead()) {
				getPlayer().setHealth(Math.max(getPlayer().getHealth() - damage, 0.0));
			}
		}

	}

}
