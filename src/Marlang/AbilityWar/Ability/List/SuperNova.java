package Marlang.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.PlayerDeathEvent;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.GameManager.Object.Participant;
import Marlang.AbilityWar.Utils.Library.ParticleLib;
import Marlang.AbilityWar.Utils.Math.LocationUtil;
import Marlang.AbilityWar.Utils.Thread.TimerBase;

public class SuperNova extends AbilityBase {

	public static SettingObject<Integer> SizeConfig = new SettingObject<Integer>("초신성", "Size", 10,
			"# 초신성이 사망할 때 일어날 폭발의 크기") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}
		
	};

	public SuperNova(Participant participant) {
		super(participant, "초신성", Rank.B,
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
				l.getWorld().createExplosion(l, 1);
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
