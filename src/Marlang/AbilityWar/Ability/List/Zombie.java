package Marlang.AbilityWar.Ability.List;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Utils.TimerBase;

public class Zombie extends AbilityBase {

	public Zombie() {
		super("좀비", Rank.C,
				ChatColor.translateAlternateColorCodes('&', "&f받는 데미지가 50% 감소합니다. 지능이 떨어져서"),
				ChatColor.translateAlternateColorCodes('&', "&f가끔 에임이 튑니다."));
		
		Aim.setPeriod(5);
		registerTimer(Aim);
	}

	TimerBase Aim = new TimerBase() {
		
		@Override
		public void TimerStart() {}
		
		@Override
		public void TimerProcess(Integer Seconds) {
			Random r = new Random();
			Integer random = r.nextInt(100) + 1;
			
			if(random <= 3) {
				Location l = getPlayer().getLocation();
				l.setPitch(r.nextInt(360) - 179);
				l.setYaw(r.nextInt(180) - 89);
				getPlayer().teleport(l);
			}
		}
		
		@Override
		public void TimerEnd() {}
		
	};
	
	@Override
	public boolean ActiveSkill(ActiveMaterialType mt, ActiveClickType ct) {
		return false;
	}

	@Override
	public void PassiveSkill(Event event) {
		if(event instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
			if(e.getEntity().equals(getPlayer())) {
				e.setDamage(e.getDamage() / 2);
			}
		}
	}

	@Override
	public void AbilityEvent(EventType type) {
		if(type.equals(EventType.RestrictClear)) {
			Aim.StartTimer();
		}
	}

}
