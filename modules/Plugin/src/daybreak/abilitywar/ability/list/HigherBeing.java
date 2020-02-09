package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

@AbilityManifest(Name = "상위존재", Rank = Rank.B, Species = Species.OTHERS)
public class HigherBeing extends AbilityBase {

	public HigherBeing(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f자신보다 낮은 위치에 있는 생명체를 공격 할 때 더욱 강력하게 공격합니다."),
				ChatColor.translateAlternateColorCodes('&', "&f대상과의 높이 차이가 작으면 작을수록 더욱 강한 대미지로 공격합니다."),
				ChatColor.translateAlternateColorCodes('&', "&f자신보다 높은 위치에 있는 생명체는 공격으로 대미지를 입힐 수 없고,"),
				ChatColor.translateAlternateColorCodes('&', "&f같은 높이에 있는 생명체는 추가 대미지 없이 공격할 수 있습니다."));
	}

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		return false;
	}

	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		Entity damager = e.getDamager();
		if (damager instanceof Projectile) {
			ProjectileSource source = ((Projectile) damager).getShooter();
			if (getPlayer().equals(source)) {
				damager = getPlayer();
			}
		}
		if (damager.equals(getPlayer())) {
			double victimLocationY = e.getEntity().getLocation().getY();
			double damagerLocationY = getPlayer().getLocation().getY();
			if (victimLocationY < damagerLocationY) {
				e.setDamage(Math.floor((e.getDamage() + (e.getDamage() * (1 / (damagerLocationY - victimLocationY + 1) * 0.44))) * 10) / 10);
				SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(getPlayer());
				ParticleLib.LAVA.spawnParticle(e.getEntity().getLocation(), 1, 1, 1, 5);
			} else if (victimLocationY != damagerLocationY) {
				e.setCancelled(true);
				SoundLib.BLOCK_ANVIL_BREAK.playSound(getPlayer());
			}
		}
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
	}

}
