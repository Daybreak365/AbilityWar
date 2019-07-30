package DayBreak.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Ability.AbilityManifest;
import DayBreak.AbilityWar.Ability.AbilityManifest.Rank;
import DayBreak.AbilityWar.Ability.AbilityManifest.Species;
import DayBreak.AbilityWar.Ability.Timer.CooldownTimer;
import DayBreak.AbilityWar.Ability.Timer.DurationTimer;
import DayBreak.AbilityWar.Config.AbilitySettings.SettingObject;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame.Participant;
import DayBreak.AbilityWar.Utils.Messager;
import DayBreak.AbilityWar.Utils.Library.EffectLib;
import DayBreak.AbilityWar.Utils.Library.SoundLib;
import DayBreak.AbilityWar.Utils.Math.LocationUtil;

@AbilityManifest(Name = "광대", Rank = Rank.B, Species = Species.HUMAN)
public class Clown extends AbilityBase {

	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Clown.class, "Cooldown", 60, 
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};

	public static SettingObject<Integer> RangeConfig = new SettingObject<Integer>(Clown.class, "Range", 10, 
			"# 스킬 범위") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};

	public Clown(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 스폰으로 이동합니다. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&f스폰으로 이동한 후 10초 안에 철괴를 다시 우클릭하면 원래 위치로 돌아가"),
				ChatColor.translateAlternateColorCodes('&', "&f주변 " + RangeConfig.getValue() + "칸 이내의 플레이어들을 실명시킵니다."));
	}

	private Location OriginalPoint = null;
	
	private CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());

	private DurationTimer Duration = new  DurationTimer(this, 10, Cool) {

		@Override
		protected void onDurationStart() {
			OriginalPoint = getPlayer().getLocation();
			Location Spawn = getPlayer().getWorld().getSpawnLocation();
			
			getPlayer().teleport(Spawn);
		}

		@Override
		protected void DurationProcess(Integer Seconds) {}

		@Override
		protected void onDurationEnd() {}
		
	};
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		if(mt.equals(MaterialType.Iron_Ingot)) {
			if(ct.equals(ClickType.RightClick)) {
				if(!Duration.isDuration()) {
					if(!Cool.isCooldown()) {
						Duration.StartTimer();
						
						return true;
					}
				} else {
					if(OriginalPoint != null) getPlayer().teleport(OriginalPoint);
					SoundLib.ENTITY_BAT_TAKEOFF.playSound(getPlayer());
					Duration.StopTimer(false);
					
					for(Player p : LocationUtil.getNearbyPlayers(getPlayer(), RangeConfig.getValue(), 250)) {
						SoundLib.ENTITY_WITHER_SPAWN.playSound(p);
						EffectLib.BLINDNESS.addPotionEffect(p, 200, 2, true);
					}
				}
			}
		}
		
		return false;
	}

	@Override
	public void onRestrictClear() {}

	@Override
	public void TargetSkill(MaterialType mt, Entity entity) {}
	
}
