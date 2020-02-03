package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@AbilityManifest(Name = "이라", Rank = Rank.A, Species = Species.HUMAN)
public class Ira extends AbilityBase {

	public static final SettingObject<Integer> AttackConfig = new SettingObject<Integer>(Ira.class, "AttackTime", 3,
			"# 몇번 공격을 당하면 폭발을 일으킬지 설정합니다.",
			"# 기본값: 3") {

		@Override
		public boolean Condition(Integer value) {
			return value > 1;
		}

	};

	public Ira(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f다른 엔티티에게 " + AttackConfig.getValue() + "번 공격을 당할 때마다 상대방의 위치에 폭발을 일으킵니다."),
				ChatColor.translateAlternateColorCodes('&', "&f자기 자신도 폭발 대미지를 입습니다."));
	}

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		return false;
	}

	private int ExplodeCount = 0;

	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (!e.isCancelled() && e.getEntity().equals(getPlayer()) && !e.getDamager().equals(getPlayer())) {
			if (ExplodeCount >= AttackConfig.getValue() - 1) {
				ExplodeCount = 0;

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
			} else {
				ExplodeCount++;
			}
		}
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
	}

}
