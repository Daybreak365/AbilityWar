package DayBreak.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Ability.AbilityManifest;
import DayBreak.AbilityWar.Ability.AbilityManifest.Rank;
import DayBreak.AbilityWar.Ability.AbilityManifest.Species;
import DayBreak.AbilityWar.Ability.SubscribeEvent;
import DayBreak.AbilityWar.Config.AbilitySettings.SettingObject;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame.Participant;
import DayBreak.AbilityWar.Utils.Library.EffectLib;
import DayBreak.AbilityWar.Utils.Thread.TimerBase;

@AbilityManifest(Name = "황제", Rank = Rank.A, Species = Species.HUMAN)
public class TheEmperor extends AbilityBase {

	public static SettingObject<Integer> DamageDecreaseConfig = new SettingObject<Integer>(TheEmperor.class, "DamageDecrease", 20, 
			"# 공격 피해 감소량",
			"# 10으로 설정하면 공격을 받았을 때 전체 대미지의 90%를 받습니다.") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1 && value <= 100;
		}
		
	};
	
	public TheEmperor(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f느리고 품위있게 걸어가며 공격 피해가 일정량 감소합니다."),
				ChatColor.translateAlternateColorCodes('&', "&f체력이 한칸 반 이하일 때 공격 피해를 받지 않습니다."));
	}
	
	private TimerBase Passive = new TimerBase() {
		
		@Override
		public void onStart() {}
		
		@Override
		public void TimerProcess(Integer Seconds) {
			EffectLib.SLOW.addPotionEffect(getPlayer(), 30, 1, true);
		}
		
		@Override
		public void onEnd() {}
		
	};
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		return false;
	}
	
	private int DamageDecrease = DamageDecreaseConfig.getValue();
	
	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if(e.getEntity().equals(getPlayer())) {
			Double damage = (e.getDamage() / 100) * (100 - DamageDecrease);
			e.setDamage(damage);
			
			Integer Health = (int) getPlayer().getHealth();
			if(Health <= 2) {
				e.setCancelled(true);
			}
		}
	}
	
	@Override
	public void onRestrictClear() {
		Passive.StartTimer();
	}

	@Override
	public void TargetSkill(MaterialType mt, Entity entity) {}
	
}
