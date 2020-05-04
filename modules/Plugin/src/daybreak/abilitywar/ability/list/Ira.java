package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@AbilityManifest(name = "이라", rank = Rank.A, species = Species.HUMAN, explain = {
		"다른 생명체에게 $[AttackConfig]번 공격을 당할 때마다 상대방의 위치에 폭발을 일으킵니다.",
		"자기 자신도 폭발 대미지를 입습니다."
})
public class Ira extends AbilityBase {

	public static final SettingObject<Integer> AttackConfig = abilitySettings.new SettingObject<Integer>(Ira.class, "AttackTime", 3,
			"# 몇번 공격을 당하면 폭발을 일으킬지 설정합니다.",
			"# 기본값: 3") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	public Ira(Participant participant) {
		super(participant);
	}

	private final int maxStack = AttackConfig.getValue();
	private int explodeStack = 0;

	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (!e.isCancelled() && e.getEntity().equals(getPlayer()) && !e.getDamager().equals(getPlayer())) {
			if (++explodeStack >= maxStack) {
				explodeStack = 0;

				Entity damager = e.getDamager();

				if (damager instanceof Projectile) {
					if (((Projectile) damager).getShooter() instanceof LivingEntity) {
						LivingEntity entity = (LivingEntity) ((Projectile) damager).getShooter();
						Location location = entity.getLocation();
						getPlayer().getWorld().createExplosion(location.getX(), location.getY(), location.getZ(), 1.3f, false, false);
						if (entity.getVelocity().getY() > 0) {
							entity.setVelocity(entity.getVelocity().setY(0));
						}
					}
				} else {
					Location location = damager.getLocation();
					getPlayer().getWorld().createExplosion(location.getX(), location.getY(), location.getZ(), 1.3f, false, false);
					if (damager.getVelocity().getY() > 0) {
						damager.setVelocity(damager.getVelocity().setY(0));
					}
				}
			}
		}
	}

}
