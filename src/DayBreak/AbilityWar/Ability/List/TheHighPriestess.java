package DayBreak.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
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
import DayBreak.AbilityWar.Utils.Library.ParticleLib;
import DayBreak.AbilityWar.Utils.Library.SoundLib;
import DayBreak.AbilityWar.Utils.Math.LocationUtil;
import DayBreak.AbilityWar.Utils.Math.Geometry.Circle;

@AbilityManifest(Name = "교황", Rank = Rank.A, Species = Species.HUMAN)
public class TheHighPriestess extends AbilityBase {

	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(TheHighPriestess.class, "Cooldown", 80,
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};

	public static SettingObject<Integer> DurationConfig = new SettingObject<Integer>(TheHighPriestess.class, "Duration", 6,
			"# 스킬 지속시간") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1 && value <= 50;
		}
		
	};

	public static SettingObject<Integer> RangeConfig = new SettingObject<Integer>(TheHighPriestess.class, "Range", 8,
			"# 스킬 사용 시 자신의 영지로 선포할 범위") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1 && value <= 50;
		}
		
	};

	public TheHighPriestess(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 " + DurationConfig.getValue() + "초간 주변 " + RangeConfig.getValue() + "칸을 자신의 영지로 선포합니다. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&f영지 안에서 자신은 재생 효과를, 상대방은 위더 효과를 받습니다."));
	}

	private final Integer Duration = DurationConfig.getValue();
	private final Integer Range = RangeConfig.getValue();
	
	private CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());
	
	private DurationTimer Skill = new DurationTimer(this, Duration * 20, Cool) {
		
		private Location center;
		
		@Override
		protected void onDurationStart() {
			center = getPlayer().getLocation();
			
			for(Player p : LocationUtil.getNearbyPlayers(center, Range, Range)) {
				if(LocationUtil.isInCircle(center, p.getLocation(), Double.valueOf(Range), true)) {
					SoundLib.ENTITY_EVOKER_CAST_SPELL.playSound(p);
				}
			}
		}

		@Override
		public void DurationProcess(Integer Seconds) {
			for(Location l : new Circle(center, Range).setAmount(Range * 8).setHighestLocation(true).getLocations()) {
				ParticleLib.SPELL_INSTANT.spawnParticle(l, 0, 0, 0, 1);
			}

			for(Player p : LocationUtil.getNearbyPlayers(center, Range, Range)) {
				if(LocationUtil.isInCircle(center, p.getLocation(), Double.valueOf(Range), true)) {
					if(p.equals(getPlayer())) {
						EffectLib.REGENERATION.addPotionEffect(p, 100, 2, false);
					} else {
						EffectLib.WITHER.addPotionEffect(p, 200, 1, true);
					}
				}
			}
		}
		
		@Override
		protected void onDurationEnd() {}
		
	}.setPeriod(1);
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		if (mt.equals(MaterialType.Iron_Ingot)) {
			if (ct.equals(ClickType.RightClick)) {
				if(!Skill.isDuration() && !Cool.isCooldown()) {
					
					Skill.StartTimer();
					
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public void onRestrictClear() {}

	@Override
	public void TargetSkill(MaterialType mt, LivingEntity entity) {}
	
}
