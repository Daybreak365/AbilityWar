package Marlang.AbilityWar.Ability.List;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.CooldownTimer;
import Marlang.AbilityWar.Ability.Skill.SkillTimer;
import Marlang.AbilityWar.Ability.Skill.SkillTimer.SkillType;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.Utils.LocationUtil;

public class Zeus extends AbilityBase {
	
	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>("제우스", "Cooldown", 180,
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};
	
	public Zeus() {
		super("제우스", Rank.God,
				ChatColor.translateAlternateColorCodes('&', "&f번개의 신 제우스."),
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 주변에 번개를 떨어뜨며 폭발을 일으킵니다."),
				ChatColor.translateAlternateColorCodes('&', "&f번개 데미지와 폭발 데미지를 받지 않습니다.⚡"));
		
		
		registerTimer(Cool);
		
		Skill.setPeriod(8);
		
		registerTimer(Skill);
	}
	
	CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());
	
	SkillTimer Skill = new SkillTimer(this, 3, SkillType.Active, Cool) {
		
		Location center;
		ArrayList<Location> Circle;
		
		@Override
		public void TimerStart() {
			center = getPlayer().getLocation();
		}
		
		@Override
		public void TimerProcess(Integer Seconds) {
			if(Seconds.equals(3)) {
				Circle = LocationUtil.getCircle(center, 3, 10);
			} else if(Seconds.equals(2)) {
				Circle = LocationUtil.getCircle(center, 5, 10);
			} else if(Seconds.equals(1)) {
				Circle = LocationUtil.getCircle(center, 7, 10);
			}
			
			for(Location l : Circle) {
				l.getWorld().strikeLightning(l);
				l.getWorld().createExplosion(l, 2);
			}
		}
		
	};
	
	@Override
	public void ActiveSkill(ActiveMaterialType mt, ActiveClickType ct) {
		if(mt.equals(ActiveMaterialType.Iron_Ingot)) {
			if(ct.equals(ActiveClickType.RightClick)) {
				if(!Cool.isCooldown()) {
					Skill.Execute();
				}
			}
		}
	}
	
	@Override
	public void PassiveSkill(Event event) {
		if(event instanceof EntityDamageEvent) {
			EntityDamageEvent e = (EntityDamageEvent) event;
			if(e.getEntity().equals(getPlayer())) {
				if(e.getCause().equals(DamageCause.LIGHTNING) || e.getCause().equals(DamageCause.BLOCK_EXPLOSION) || e.getCause().equals(DamageCause.ENTITY_EXPLOSION)) {
					e.setCancelled(true);
				}
			}
		}
	}
	
}
