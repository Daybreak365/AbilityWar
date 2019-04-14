package DayBreak.AbilityWar.Ability.List;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Ability.AbilityManifest;
import DayBreak.AbilityWar.Ability.AbilityManifest.Rank;
import DayBreak.AbilityWar.Ability.Timer.CooldownTimer;
import DayBreak.AbilityWar.Config.AbilitySettings.SettingObject;
import DayBreak.AbilityWar.Game.Games.AbstractGame.Participant;
import DayBreak.AbilityWar.Utils.Messager;
import DayBreak.AbilityWar.Utils.Math.LocationUtil;
import DayBreak.AbilityWar.Utils.Thread.TimerBase;

@AbilityManifest(Name = "?†ú?ö∞?ä§", Rank = Rank.GOD)
public class Zeus extends AbilityBase {
	
	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Zeus.class, "Cooldown", 180,
			"# Ïø®Ì??ûÑ") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};
	
	public Zeus(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&fÎ≤àÍ∞ú?ùò ?ã† ?†ú?ö∞?ä§."),
				ChatColor.translateAlternateColorCodes('&', "&fÏ≤†Í¥¥Î•? ?ö∞?Å¥Î¶??ïòÎ©? Ï£ºÎ??óê Î≤àÍ∞úÎ•? ?ñ®?ñ¥?ú®Î¶¨Î©∞ ?è≠Î∞úÏùÑ ?ùº?úº?Çµ?ãà?ã§. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&fÎ≤àÍ∞ú ?ç∞ÎØ∏Ï??? ?è≠Î∞? ?ç∞ÎØ∏Ï?Î•? Î∞õÏ? ?ïä?äµ?ãà?ã§."));
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
