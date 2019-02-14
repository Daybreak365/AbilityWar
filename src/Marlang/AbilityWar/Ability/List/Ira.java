package Marlang.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;

public class Ira extends AbilityBase {

	public static SettingObject<Integer> AttackConfig = new SettingObject<Integer>("이라", "AttackTime", 2,
			"# 몇번 공격을 당하면 폭발을 일으킬지 설정합니다.",
			"# 기본값: 2") {
		
		@Override
		public boolean Condition(Integer value) {
			return value > 1;
		}
		
	};
	
	public Ira(Player player) {
		super(player, "이라", Rank.S,
				ChatColor.translateAlternateColorCodes('&', "&f" + AttackConfig.getValue() + "번 공격을 당할 때마다 상대방의 위치에 폭발을 일으킵니다."),
				ChatColor.translateAlternateColorCodes('&', "&f자기 자신도 폭발 데미지를 입습니다."));
	}

	@Override
	public boolean ActiveSkill(ActiveMaterialType mt, ActiveClickType ct) {
		return false;
	}

	Integer ExplodeCount = 0;
	
	@Override
	public void PassiveSkill(Event event) {
		if(event instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
			if(e.getEntity().equals(getPlayer())) {
				if(ExplodeCount >= AttackConfig.getValue() - 1) {
					ExplodeCount = 0;
					
					Entity Damager = e.getDamager();
					
					if(Damager instanceof Projectile) {
						if(((Projectile) Damager).getShooter() instanceof LivingEntity) {
							LivingEntity entity = (LivingEntity) ((Projectile) Damager).getShooter();
							getPlayer().getWorld().createExplosion(entity.getLocation(), 1, false);
							if(entity.getVelocity().getY() > 0) {
								entity.setVelocity(entity.getVelocity().setY(0));
							}
						}
					} else {
						getPlayer().getWorld().createExplosion(Damager.getLocation(), 1, false);
						if(Damager.getVelocity().getY() > 0) {
							Damager.setVelocity(Damager.getVelocity().setY(0));
						}
					}
				} else {
					ExplodeCount++;
				}
			}
		}
	}

	@Override
	public void onRestrictClear() {}

}
