package DayBreak.AbilityWar.Ability.List;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;

import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Ability.AbilityManifest;
import DayBreak.AbilityWar.Ability.AbilityManifest.Rank;
import DayBreak.AbilityWar.Ability.Timer.CooldownTimer;
import DayBreak.AbilityWar.Config.AbilitySettings.SettingObject;
import DayBreak.AbilityWar.Game.Games.AbstractGame.Participant;
import DayBreak.AbilityWar.Utils.Messager;
import DayBreak.AbilityWar.Utils.Math.LocationUtil;
import DayBreak.AbilityWar.Utils.Thread.TimerBase;

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
				ChatColor.translateAlternateColorCodes('&', "&f번개를 맞은 플레이어는 3초간 구속됩니다."),
				ChatColor.translateAlternateColorCodes('&', "&f번개 데미지와 폭발 데미지를 받지 않습니다."));
	}
	
	List<Player> MoveRestrict = new ArrayList<Player>();
	
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
				l.getWorld().strikeLightningEffect(l);
				for(Damageable d : LocationUtil.getNearbyDamageableEntities(l, 2, 2)) {
					if(!d.equals(getPlayer())) {
						d.damage(d.getHealth() / 10, getPlayer());
						if(d instanceof Player) {
							new TimerBase(5) {
								@Override
								protected void onStart() {
									MoveRestrict.add((Player) d);
								}
								@Override
								protected void onEnd() {
									MoveRestrict.remove((Player) d);
								}
								@Override
								protected void TimerProcess(Integer Seconds) {}
							}.StartTimer();
						}
					}
				}
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
		} else if(event instanceof PlayerMoveEvent) {
			PlayerMoveEvent e = (PlayerMoveEvent) event;
			if(MoveRestrict.contains(e.getPlayer())) {
				e.setTo(e.getFrom());
			}
		}
	}

	@Override
	public void onRestrictClear() {}
	
	@Override
	public void TargetSkill(MaterialType mt, Entity entity) {}
	
}
