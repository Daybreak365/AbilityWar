package Marlang.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.Timer.CooldownTimer;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.Utils.LocationUtil;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Library.ParticleLib;

public class Terrorist extends AbilityBase {

	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>("테러리스트", "Cooldown", 100,
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};
	
	public Terrorist(Player player) {
		super(player, "테러리스트", Rank.A,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 자신의 주위에 TNT 10개를 떨어뜨립니다. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&f폭발 데미지를 입지 않습니다."));
	}

	CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());
	
	@Override
	public boolean ActiveSkill(ActiveMaterialType mt, ActiveClickType ct) {
		if(mt.equals(ActiveMaterialType.Iron_Ingot)) {
			if(ct.equals(ActiveClickType.RightClick)) {
				if(!Cool.isCooldown()) {
					for(int i = 0; i < 10; i++) {
						for(Location l : LocationUtil.getCircle(getPlayer().getLocation(), i, 20, true)) {
							ParticleLib.LAVA.spawnParticle(l, 1, 0, 0, 0);
						}
					}
					
					for(Location l : LocationUtil.getRandomLocations(getPlayer().getLocation(), 10, 10)) {
						l.getWorld().spawnEntity(l, EntityType.PRIMED_TNT);
					}
					
					Cool.StartTimer();
					
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public void PassiveSkill(Event event) {
		if(event instanceof EntityDamageEvent) {
			EntityDamageEvent e = (EntityDamageEvent) event;
			if(e.getEntity().equals(getPlayer())) {
				if(e.getCause().equals(DamageCause.BLOCK_EXPLOSION) || e.getCause().equals(DamageCause.ENTITY_EXPLOSION)) {
					e.setCancelled(true);
				}
			}
		}
	}

	@Override
	public void onRestrictClear() {}

}
