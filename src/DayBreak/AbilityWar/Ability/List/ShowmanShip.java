package DayBreak.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;

import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Ability.AbilityManifest;
import DayBreak.AbilityWar.Ability.AbilityManifest.Rank;
import DayBreak.AbilityWar.Ability.AbilityManifest.Species;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame.Participant;
import DayBreak.AbilityWar.Utils.Library.EffectLib;
import DayBreak.AbilityWar.Utils.Math.LocationUtil;
import DayBreak.AbilityWar.Utils.Thread.TimerBase;

@AbilityManifest(Name = "쇼맨쉽", Rank = Rank.A, Species = Species.HUMAN)
public class ShowmanShip extends AbilityBase {

	public ShowmanShip(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f주변 10칸 이내에 있는 사람 수에 따라 효과를 받습니다."),
				ChatColor.translateAlternateColorCodes('&', "&a1명 이하 &7: &f나약함  &a2명 이상 &7: &f힘 II  &a3명 이상 &7: &f힘 III"));
	}

	private TimerBase Passive = new TimerBase() {
		
		@Override
		public void onStart() {}
		
		@Override
		public void TimerProcess(Integer Seconds) {
			Integer Count = LocationUtil.getNearbyPlayers(getPlayer(), 10, 10).size();
			
			if(Count <= 1) {
				EffectLib.WEAKNESS.addPotionEffect(getPlayer(), 20, 0, true);
			} else if(Count > 1 && Count <= 2) {
				EffectLib.INCREASE_DAMAGE.addPotionEffect(getPlayer(), 20, 1, true);
			} else {
				EffectLib.INCREASE_DAMAGE.addPotionEffect(getPlayer(), 20, 2, true);
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
	public void onRestrictClear() {
		Passive.StartTimer();
	}

	@Override
	public void TargetSkill(MaterialType mt, Entity entity) {}
	
}
