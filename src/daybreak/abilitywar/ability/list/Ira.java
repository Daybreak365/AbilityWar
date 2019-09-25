package daybreak.abilitywar.ability.list;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.config.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;

@AbilityManifest(Name = "이라", Rank = Rank.S, Species = Species.HUMAN)
public class Ira extends AbilityBase {

	public static SettingObject<Integer> AttackConfig = new SettingObject<Integer>(Ira.class, "AttackTime", 4,
			"# 몇번 공격을 당하면 폭발을 일으킬지 설정합니다.",
			"# 기본값: 4") {
		
		@Override
		public boolean Condition(Integer value) {
			return value > 1;
		}
		
	};
	
	public Ira(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f" + AttackConfig.getValue() + "번 공격을 당할 때마다 상대방의 위치에 폭발을 일으킵니다."),
				ChatColor.translateAlternateColorCodes('&', "&f자기 자신도 폭발 데미지를 입습니다."));
	}

	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		return false;
	}

	private int ExplodeCount = 0;
	
	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if(!e.isCancelled() && e.getEntity().equals(getPlayer())) {
			if(ExplodeCount >= AttackConfig.getValue() - 1) {
				ExplodeCount = 0;
				
				Entity Damager = e.getDamager();
				
				if(Damager instanceof Projectile) {
					if(((Projectile) Damager).getShooter() instanceof LivingEntity) {
						LivingEntity entity = (LivingEntity) ((Projectile) Damager).getShooter();
						getPlayer().getWorld().createExplosion(entity.getLocation(), 2, false);
						if(entity.getVelocity().getY() > 0) {
							entity.setVelocity(entity.getVelocity().setY(0));
						}
					}
				} else {
					getPlayer().getWorld().createExplosion(Damager.getLocation(), 2, false);
					if(Damager.getVelocity().getY() > 0) {
						Damager.setVelocity(Damager.getVelocity().setY(0));
					}
				}
			} else {
				ExplodeCount++;
			}
		}
	}
	
	@Override
	public void onRestrictClear() {}

	@Override
	public void TargetSkill(MaterialType mt, LivingEntity entity) {}
	
}
