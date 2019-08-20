package DayBreak.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Ability.AbilityManifest;
import DayBreak.AbilityWar.Ability.AbilityManifest.Rank;
import DayBreak.AbilityWar.Ability.AbilityManifest.Species;
import DayBreak.AbilityWar.Ability.SubscribeEvent;
import DayBreak.AbilityWar.Config.AbilitySettings.SettingObject;
import DayBreak.AbilityWar.Game.Events.ParticipantDeathEvent;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame.Participant;
import DayBreak.AbilityWar.Utils.Library.ParticleLib;
import DayBreak.AbilityWar.Utils.Math.LocationUtil;
import DayBreak.AbilityWar.Utils.Thread.Timer;

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
	
	private final int Size = SizeConfig.getValue();
	
	private Timer Explosion = new Timer(Size) {
		
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
				ParticleLib.SPELL.spawnParticle(l, 0, 0, 0, 1);
			}
		}
		
		@Override
		public void onEnd() {}
		
	}.setPeriod(1);
	
	@SubscribeEvent
	public void onPlayerDeath(ParticipantDeathEvent e) {
		if(e.getParticipant().equals(getParticipant())) {
			Explosion.StartTimer();
		}
	}
	
	@Override
	public void onRestrictClear() {}

	@Override
	public void TargetSkill(MaterialType mt, LivingEntity entity) {}
	
}
