package DayBreak.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;

import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Ability.AbilityManifest;
import DayBreak.AbilityWar.Ability.AbilityManifest.Rank;
import DayBreak.AbilityWar.Config.AbilitySettings.SettingObject;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame.Participant;
import DayBreak.AbilityWar.Utils.VersionCompat.VersionUtil;

@AbilityManifest(Name = "홀수강박증", Rank = Rank.S)
public class OnlyOddNumber extends AbilityBase {

	public static SettingObject<Integer> PercentageConfig = new SettingObject<Integer>(OnlyOddNumber.class, "Percentage", 79, 
			"# 체력이 몇 퍼센트 이하일 때 능력이 발동될지 설정합니다.",
			"# 1 이상, 100 이하의 수 중 홀수로만 설정할 수 있습니다.") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1 && value <= 100 && value % 2 != 0;
		}
		
	};

	public static SettingObject<Integer> OddNumberConfig = new SettingObject<Integer>(OnlyOddNumber.class, "OddNumber", 39, 
			"# 체력이 홀수일 때 데미지를 몇 퍼센트 줄여 받을지 설정합니다.",
			"# 60으로 설정하면 원래 데미지의 40%를 받습니다.") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1 && value <= 100 && value % 2 != 0;
		}
		
	};

	public static SettingObject<Integer> EvenNumberConfig = new SettingObject<Integer>(OnlyOddNumber.class, "EvenNumber", 29, 
			"# 체력이 짝수일 때 데미지를 몇 퍼센트 늘려 받을지 설정합니다.",
			"# 30으로 설정하면 원래 데미지의 130%를 받습니다.") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1 && value <= 100 && value % 2 != 0;
		}
		
	};
	
	public OnlyOddNumber(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f체력이 최대 체력의 " + PercentageConfig.getValue() + "% 이하일 때"),
				ChatColor.translateAlternateColorCodes('&', "&f공격을 받으면 체력에 따라 다른 효과를 받습니다. &f체력이 홀수일 경우 데미지를"),
				ChatColor.translateAlternateColorCodes('&', OddNumberConfig.getValue() + "% 줄여 받고, 체력이 짝수일 경우 데미지를 " + EvenNumberConfig.getValue() + "% 늘려 받습니다."));
	}
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		return false;
	}
	
	Integer Percentage = PercentageConfig.getValue();
	Integer Odd = OddNumberConfig.getValue();
	Integer Even = EvenNumberConfig.getValue();
	
	@Override
	public void PassiveSkill(Event event) {
		if(event instanceof EntityDamageEvent) {
			EntityDamageEvent e = (EntityDamageEvent) event;
			if(e.getEntity().equals(getPlayer())) {
				Double doubleMaxHealth = VersionUtil.getMaxHealth(getPlayer());
				Double doubleHealth = getPlayer().getHealth();
				
				Integer Health = (int) getPlayer().getHealth();
				
				if(doubleHealth <= (doubleMaxHealth / 100) * Percentage) {
					if(Health % 2 == 0) { //짝수
						e.setDamage(e.getDamage() + ((e.getDamage() / 100) * Even));
					} else { //홀수
						e.setDamage(e.getDamage() - ((e.getDamage() / 100) * Odd));
					}
				}
			}
		}
	}

	@Override
	public void onRestrictClear() {}

	@Override
	public void TargetSkill(MaterialType mt, Entity entity) {}
	
}
