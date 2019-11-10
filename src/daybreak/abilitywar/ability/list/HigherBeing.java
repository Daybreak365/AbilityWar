package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@AbilityManifest(Name = "상위존재", Rank = Rank.B, Species = Species.OTHERS)
public class HigherBeing extends AbilityBase {

	public static final SettingObject<Double> DamageConfig = new SettingObject<Double>(HigherBeing.class, "DamageMultiple", 2.0,
			"# 공격 배수") {

		@Override
		public boolean Condition(Double value) {
			return value > 1;
		}

	};

	public HigherBeing(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f자신보다 낮은 위치에 있는 생명체를 공격 할 때"),
				ChatColor.translateAlternateColorCodes('&', "&f" + DamageConfig.getValue() + "배 강력하게 공격합니다."),
				ChatColor.translateAlternateColorCodes('&', "&f자신보다 높은 위치에 있는 생명체는 공격으로 데미지를 입힐 수 없습니다."));
	}

	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		return false;
	}

	private final double multiple = DamageConfig.getValue();

	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		Entity damager = e.getDamager();
		if (damager.equals(getPlayer()) || (damager instanceof Projectile && getPlayer().equals(((Projectile) damager).getShooter()))) {
			double damagerLocationY = damager.getLocation().getY();
			double playerLocationY = getPlayer().getLocation().getY();
			if (damagerLocationY < playerLocationY) {
				e.setDamage(e.getDamage() * multiple);
				SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(getPlayer());
				ParticleLib.LAVA.spawnParticle(e.getEntity().getLocation(), 1, 1, 1, 5);
			} else if (damagerLocationY != playerLocationY) {
				e.setCancelled(true);
				SoundLib.BLOCK_ANVIL_BREAK.playSound(getPlayer());
			}
		}
	}

	@Override
	public void onRestrictClear() {
	}

	@Override
	public void TargetSkill(MaterialType mt, LivingEntity entity) {
	}

}
