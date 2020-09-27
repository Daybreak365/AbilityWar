package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.Tips;
import daybreak.abilitywar.ability.Tips.Difficulty;
import daybreak.abilitywar.ability.Tips.Level;
import daybreak.abilitywar.ability.Tips.Stats;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

@AbilityManifest(name = "상위존재", rank = Rank.B, species = Species.OTHERS, explain = {
		"자신보다 낮은 위치에 있는 생명체를 공격 할 때 더욱 강력하게 공격합니다.",
		"대상과의 높이 차이가 작으면 작을수록 더욱 강한 대미지로 공격합니다.",
		"자신보다 높은 위치에 있는 생명체는 공격으로 대미지를 입힐 수 없고,",
		"같은 높이에 있는 생명체는 추가 대미지 없이 공격할 수 있습니다."
})
@Tips(tip = {
		"자신보다 아래에 있는 플레이어에게는 한없이 강해지지만, 자신보다 위에 있는",
		"플레이어에게는 한없이 약해집니다. 진정한 강약약강을 보여주세요!",
		"주변 환경에 영향을 많이 받는 능력입니다. 점프로도 카운팅당할 수 있기 때문에,",
		"조심해야 합니다. 항상 상대보다 위에 있을 방법을 찾으세요."
}, stats = @Stats(offense = Level.FIVE, survival = Level.ZERO, crowdControl = Level.ZERO, mobility = Level.ZERO, utility = Level.ZERO), difficulty = Difficulty.NORMAL)
public class HigherBeing extends AbilityBase {

	public HigherBeing(Participant participant) {
		super(participant);
	}

	@SubscribeEvent
	private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
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
				e.setDamage(Math.floor((e.getDamage() + (e.getDamage() * (1 / (damagerLocationY - victimLocationY + 1) * 0.65))) * 10) / 10);
				SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(getPlayer());
				ParticleLib.LAVA.spawnParticle(e.getEntity().getLocation(), 1, 1, 1, 5);
			} else if (victimLocationY != damagerLocationY) {
				e.setCancelled(true);
				SoundLib.BLOCK_ANVIL_BREAK.playSound(getPlayer());
			}
		}
	}

}
