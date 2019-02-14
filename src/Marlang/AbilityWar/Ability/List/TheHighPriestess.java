package Marlang.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.Timer.CooldownTimer;
import Marlang.AbilityWar.Ability.Timer.DurationTimer;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.Utils.LocationUtil;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Library.ParticleLib;
import Marlang.AbilityWar.Utils.Library.SoundLib;

public class TheHighPriestess extends AbilityBase {

	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>("교황", "Cooldown", 80,
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};

	public static SettingObject<Integer> DurationConfig = new SettingObject<Integer>("교황", "Duration", 6,
			"# 스킬 지속시간") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1 && value <= 50;
		}
		
	};

	public static SettingObject<Integer> RangeConfig = new SettingObject<Integer>("교황", "Range", 8,
			"# 스킬 사용 시 자신의 영지로 선포할 범위") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1 && value <= 50;
		}
		
	};

	public TheHighPriestess(Player player) {
		super(player, "교황", Rank.A,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 " + DurationConfig.getValue() + "초간 주변 " + RangeConfig.getValue() + "칸을 자신의 영지로 선포합니다. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&f영지 안에서 자신은 재생 효과를, 상대방은 위더 효과를 받습니다."));
	}

	final Integer Duration = DurationConfig.getValue();
	final Integer Range = RangeConfig.getValue();
	
	CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());
	
	DurationTimer Skill = new DurationTimer(this, DurationConfig.getValue() * 20, Cool) {
		
		Location center;
		
		@Override
		public void TimerStart(Data<?>... args) {
			center = getPlayer().getLocation();
			
			for(Player p : LocationUtil.getNearbyPlayers(center, Range, Range)) {
				if(LocationUtil.isInCircle(p.getLocation(), center, Double.valueOf(Range))) {
					SoundLib.ENTITY_EVOCATION_ILLAGER_CAST_SPELL.playSound(p);
				}
			}
			
			super.TimerStart(args);
		}
		
		@Override
		public void DurationSkill(Integer Seconds) {
			for(Location l : LocationUtil.getCircle(center, Range, Range * Range, true)) {
				ParticleLib.SPELL_INSTANT.spawnParticle(l, 1, 0, 0, 0);
			}
			
			for(Player p : LocationUtil.getNearbyPlayers(center, Range, Range)) {
				if(LocationUtil.isInCircle(p.getLocation(), center, Double.valueOf(Range))) {
					if(p.equals(getPlayer())) {
						p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1), true);
					} else {
						p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1), true);
					}
				}
			}
		}
		
	}.setPeriod(1);
	
	@Override
	public boolean ActiveSkill(ActiveMaterialType mt, ActiveClickType ct) {
		if (mt.equals(ActiveMaterialType.Iron_Ingot)) {
			if (ct.equals(ActiveClickType.RightClick)) {
				if(!Skill.isDuration() && !Cool.isCooldown()) {
					
					Skill.StartTimer();
					
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public void PassiveSkill(Event event) {}

	@Override
	public void onRestrictClear() {}

}
