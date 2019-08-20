package DayBreak.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Ability.AbilityManifest;
import DayBreak.AbilityWar.Ability.AbilityManifest.Rank;
import DayBreak.AbilityWar.Ability.AbilityManifest.Species;
import DayBreak.AbilityWar.Ability.Timer.CooldownTimer;
import DayBreak.AbilityWar.Ability.Timer.DurationTimer;
import DayBreak.AbilityWar.Config.AbilitySettings.SettingObject;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame.Participant;
import DayBreak.AbilityWar.Utils.Messager;
import DayBreak.AbilityWar.Utils.Library.ParticleLib;
import DayBreak.AbilityWar.Utils.Math.LocationUtil;

@AbilityManifest(Name = "카오스", Rank = Rank.S, Species = Species.GOD)
public class Chaos extends AbilityBase {

	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Chaos.class, "Cooldown", 80,
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};
	
	public static SettingObject<Integer> DurationConfig = new SettingObject<Integer>(Chaos.class, "Duration", 5,
			"# 능력 지속 시간") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}
		
	};

	public static SettingObject<Integer> DistanceConfig = new SettingObject<Integer>(Chaos.class, "Distance", 5,
			"# 거리 설정") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}
		
	};

	public Chaos(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f시작의 신 카오스."),
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 5초간 짙은 암흑 속으로 주변의 생명체들을"),
				ChatColor.translateAlternateColorCodes('&', "&f모두 끌어당깁니다. " + Messager.formatCooldown(CooldownConfig.getValue())));
	}

	private CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());

	private final int Distance = DistanceConfig.getValue();
	
	private DurationTimer Duration = new DurationTimer(this, DurationConfig.getValue() * 20, Cool) {

		private Location center;
		
		@Override
		public void onDurationStart() {
			center = getPlayer().getLocation();
		}
		
		@Override
		public void DurationProcess(Integer Seconds) {
			ParticleLib.SMOKE_LARGE.spawnParticle(center, 0, 0, 0, 100);
			for(Damageable d : LocationUtil.getNearbyEntities(Damageable.class, center, Distance, Distance, getPlayer())) {
				d.damage(1);
				Vector vector = center.toVector().subtract(d.getLocation().toVector()).multiply(0.7);
				d.setVelocity(vector);
			}
		}

		@Override
		protected void onDurationEnd() {}
		
	}.setPeriod(1);
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		if(mt.equals(MaterialType.Iron_Ingot)) {
			if(ct.equals(ClickType.RightClick)) {
				if(!Duration.isDuration() && !Cool.isCooldown()) {
					Duration.StartTimer();
					
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
