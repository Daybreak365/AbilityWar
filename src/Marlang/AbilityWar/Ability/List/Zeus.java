package Marlang.AbilityWar.Ability.List;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.AbilityManifest;
import Marlang.AbilityWar.Ability.AbilityManifest.Rank;
import Marlang.AbilityWar.Ability.Timer.CooldownTimer;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.Game.Games.AbstractGame.Participant;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Math.LocationUtil;
import Marlang.AbilityWar.Utils.Thread.TimerBase;

@AbilityManifest(Name = "제우스", Rank = Rank.GOD)
public class Zeus extends AbilityBase {
	
	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Zeus.class, "Cooldown", 180,
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};
	
	public Zeus(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f번개의 신 제우스."),
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 주변에 번개를 떨어뜨리며 폭발을 일으킵니다. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&f번개 데미지와 폭발 데미지를 받지 않습니다."));
	}
	
	CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());
	
	TimerBase Skill = new TimerBase(3) {

		Location center;
		ArrayList<Location> Circle;
		
		@Override
		public void onStart() {
			center = getPlayer().getLocation();
		}
		
		@Override
		public void TimerProcess(Integer Seconds) {
			if(Seconds.equals(3)) {
				Circle = LocationUtil.getCircle(center, 3, 7, true);
			} else if(Seconds.equals(2)) {
				Circle = LocationUtil.getCircle(center, 5, 7, true);
			} else if(Seconds.equals(1)) {
				Circle = LocationUtil.getCircle(center, 7, 7, true);
			}
			
			for(Location l : Circle) {
				l.getWorld().strikeLightning(l);
				l.getWorld().createExplosion(l, 2);
			}
		}
		
		@Override
		public void onEnd() {}
		
	}.setPeriod(8);
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		if(mt.equals(MaterialType.Iron_Ingot)) {
			if(ct.equals(ClickType.RightClick)) {
				if(!Cool.isCooldown()) {
					Skill.StartTimer();
					
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
				if(e.getCause().equals(DamageCause.LIGHTNING) || e.getCause().equals(DamageCause.BLOCK_EXPLOSION) || e.getCause().equals(DamageCause.ENTITY_EXPLOSION)) {
					e.setCancelled(true);
				}
			}
		}
	}

	@Override
	public void onRestrictClear() {}
	
	@Override
	public void TargetSkill(MaterialType mt, Entity entity) {}
	
}
