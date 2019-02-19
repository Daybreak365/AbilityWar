package Marlang.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.Utils.Math.LocationUtil;
import Marlang.AbilityWar.Utils.Thread.TimerBase;
import Marlang.AbilityWar.Utils.VersionCompat.PlayerCompat;
import Marlang.AbilityWar.Utils.VersionCompat.PotionEffectType;

public class DarkVision extends AbilityBase {
	
	public static SettingObject<Integer> DistanceConfig = new SettingObject<Integer>("심안", "Distance", 30,
			"# 거리 설정") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}
		
	};
	
	public DarkVision(Player player) {
		super(player, "심안", Rank.C,
				ChatColor.translateAlternateColorCodes('&', "&f앞이 보이지 않는 대신, 플레이어의 " + DistanceConfig.getValue() + "칸 안에 있는 플레이어들은"),
				ChatColor.translateAlternateColorCodes('&', "&f발광 효과가 적용됩니다."));
	}

	TimerBase Dark = new TimerBase() {
		
		@Override
		public void onStart() {}
		
		@Override
		public void TimerProcess(Integer Seconds) {
			PlayerCompat.addPotionEffect(getPlayer(), PotionEffectType.BLINDNESS, 40, 0, true);
			PlayerCompat.addPotionEffect(getPlayer(), PotionEffectType.SPEED, 40, 3, true);
		}
		
		@Override
		public void onEnd() {}
		
	}.setPeriod(2);
	
	TimerBase Vision = new TimerBase() {
		
		Integer Distance = DistanceConfig.getValue();
		
		@Override
		public void onStart() {}
		
		@Override
		public void TimerProcess(Integer Seconds) {
			for(Player p : LocationUtil.getNearbyPlayers(getPlayer(), Distance, Distance)) {
				PlayerCompat.addPotionEffect(p, PotionEffectType.GLOWING, 10, 0, true);
			}
		}
		
		@Override
		public void onEnd() {}
		
	}.setPeriod(2);
	
	@Override
	public boolean ActiveSkill(ActiveMaterialType mt, ActiveClickType ct) {
		return false;
	}

	@Override
	public void PassiveSkill(Event event) {}

	@Override
	public void onRestrictClear() {
		Dark.StartTimer();
		Vision.StartTimer();
	}

}
