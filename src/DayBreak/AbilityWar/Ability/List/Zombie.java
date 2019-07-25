package DayBreak.AbilityWar.Ability.List;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Ability.AbilityManifest;
import DayBreak.AbilityWar.Ability.AbilityManifest.Rank;
import DayBreak.AbilityWar.Ability.AbilityManifest.Species;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame.Participant;
import DayBreak.AbilityWar.Utils.Thread.TimerBase;

@AbilityManifest(Name = "좀비", Rank = Rank.B, Species = Species.OTHERS)
public class Zombie extends AbilityBase {

	public Zombie(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f받는 데미지가 50% 감소합니다. 근육 경련으로 인해"),
				ChatColor.translateAlternateColorCodes('&', "&f에임이 종종 튑니다."));
	}

	TimerBase Aim = new TimerBase() {
		
		@Override
		public void onStart() {}
		
		@Override
		public void TimerProcess(Integer Seconds) {
			Random r = new Random();
			Integer random = r.nextInt(100) + 1;
			
			if(random <= 10) {
				Location l = getPlayer().getLocation();
				l.setPitch(r.nextInt(360) - 179);
				l.setYaw(r.nextInt(180) - 89);
				getPlayer().teleport(l);
			}
		}
		
		@Override
		public void onEnd() {}
		
	}.setPeriod(5);
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
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
	public void onRestrictClear() {
		Aim.StartTimer();
	}

	@Override
	public void TargetSkill(MaterialType mt, Entity entity) {}
	
}
