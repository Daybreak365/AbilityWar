package DayBreak.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.PlayerDeathEvent;

import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Ability.AbilityManifest;
import DayBreak.AbilityWar.Ability.AbilityManifest.Rank;
import DayBreak.AbilityWar.Ability.AbilityManifest.Species;
import DayBreak.AbilityWar.Config.AbilitySettings.SettingObject;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame.Participant;
import DayBreak.AbilityWar.Utils.Library.ParticleLib;
import DayBreak.AbilityWar.Utils.Math.LocationUtil;
import DayBreak.AbilityWar.Utils.Thread.TimerBase;

@AbilityManifest(Name = "초신성", Rank = Rank.B, Species = Species.OTHERS)
public class SuperNova extends AbilityBase {

	public static SettingObject<Integer> SizeConfig = new SettingObject<Integer>(SuperNova.class, "Size", 10,
			"# 초신성이 사망할 때 일어날 폭발의 크기") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}
		
	};

	public SuperNova(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f마지막 순간에 큰 폭발을 일으키고 사망합니다."));
	}
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		return false;
	}
	
	Integer Size = SizeConfig.getValue();
	
	TimerBase Explosion = new TimerBase(Size) {
		
		Location center;
		
		@Override
		public void onStart() {
			center = getPlayer().getLocation();
		}
		
		@Override
		public void TimerProcess(Integer Seconds) {
			double Count = ((Size + 1) - Seconds) / 1.2;
			for(Location l : LocationUtil.getSphere(center, Count, 5)) {
				l.getWorld().createExplosion(l, 2);
				ParticleLib.SPELL.spawnParticle(l, 1, 0, 0, 0);
			}
		}
		
		@Override
		public void onEnd() {}
		
	}.setPeriod(1);
	
	@Override
	public void PassiveSkill(Event event) {
		if(event instanceof PlayerDeathEvent) {
			PlayerDeathEvent e = (PlayerDeathEvent) event;
			if(e.getEntity().equals(getPlayer())) {
				Explosion.StartTimer();
			}
		}
	}
	
	@Override
	public void onRestrictClear() {}

	@Override
	public void TargetSkill(MaterialType mt, Entity entity) {}
	
}
