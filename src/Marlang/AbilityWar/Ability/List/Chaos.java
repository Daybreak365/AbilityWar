package Marlang.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Damageable;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.Timer.CooldownTimer;
import Marlang.AbilityWar.Ability.Timer.SkillTimer;
import Marlang.AbilityWar.Ability.Timer.SkillTimer.SkillType;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.Utils.LocationUtil;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.NumberUtil;
import Marlang.AbilityWar.Utils.Library.ParticleLib;

public class Chaos extends AbilityBase {

	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>("카오스", "Cooldown", 80,
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};

	public static SettingObject<Integer> DurationConfig = new SettingObject<Integer>("카오스", "Duration", 5,
			"# 능력 지속 시간") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}
		
	};

	public static SettingObject<Integer> DistanceConfig = new SettingObject<Integer>("카오스", "Distance", 5,
			"# 거리 설정") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}
		
	};

	public Chaos() {
		super("카오스", Rank.God,
				ChatColor.translateAlternateColorCodes('&', "&f시작의 신 카오스."),
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 5초간 짙은 암흑 속으로 주변의 생명체들을"),
				ChatColor.translateAlternateColorCodes('&', "&f모두 끌어당깁니다. " + Messager.formatCooldown(CooldownConfig.getValue())));
		
		registerTimer(Cool);
		
		Skill.setPeriod(1);
		
		registerTimer(Skill);
	}

	CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());
	
	SkillTimer Skill = new SkillTimer(this, DurationConfig.getValue() * 20, SkillType.Active, Cool) {
		
		Integer Distance = DistanceConfig.getValue();
		
		Location center;
		
		@Override
		public void TimerStart() {
			center = getPlayer().getLocation();
		}
		
		@Override
		public void TimerProcess(Integer Seconds) {
			ParticleLib.SMOKE_NORMAL.spawnParticle(center, 100, 2, 2, 2);
			for(Damageable d : LocationUtil.getNearbyDamageableEntities(center, Distance, Distance)) {
				if(!d.equals(getPlayer())) {
					d.damage(0.5);
					Vector vector = center.toVector().subtract(d.getLocation().toVector());
					d.setVelocity(vector);
				}
			}
		}
		
	};
	
	@Override
	public void ActiveSkill(ActiveMaterialType mt, ActiveClickType ct) {
		if(mt.equals(ActiveMaterialType.Iron_Ingot)) {
			if(ct.equals(ActiveClickType.RightClick)) {
				if(!Skill.isTimerRunning()) {
					if(!Cool.isCooldown()) {
						Skill.Execute();
					}
				} else {
					Messager.sendMessage(getPlayer(), ChatColor.translateAlternateColorCodes('&', "&6지속 시간 &f" + NumberUtil.parseTimeString(Skill.getTempCount() / 20)));
				}
			}
		}
	}

	@Override
	public void PassiveSkill(Event event) {}

	@Override
	public void AbilityEvent(EventType type) {}

}
