package daybreak.abilitywar.ability.list;

import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.config.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.versioncompat.VersionUtil;

@AbilityManifest(Name = "홀수강박증", Rank = Rank.S, Species = Species.HUMAN)
public class OnlyOddNumber extends AbilityBase {

	public static final SettingObject<Integer> PercentageConfig = new SettingObject<Integer>(OnlyOddNumber.class, "Percentage", 79,
			"# 체력이 몇 퍼센트 이하일 때 능력이 발동될지 설정합니다.",
			"# 1 이상, 100 이하의 수 중 홀수로만 설정할 수 있습니다.") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1 && value <= 100 && value % 2 != 0;
		}
		
	};

	public static final SettingObject<Integer> OddNumberConfig = new SettingObject<Integer>(OnlyOddNumber.class, "OddNumber", 39,
			"# 체력이 홀수일 때 데미지를 몇 퍼센트 줄여 받을지 설정합니다.",
			"# 60으로 설정하면 원래 데미지의 40%를 받습니다.") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1 && value <= 100 && value % 2 != 0;
		}
		
	};

	public static final SettingObject<Integer> EvenNumberConfig = new SettingObject<Integer>(OnlyOddNumber.class, "EvenNumber", 29,
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
	
	private final int Percentage = PercentageConfig.getValue();
	private final int Odd = OddNumberConfig.getValue();
	private final int Even = EvenNumberConfig.getValue();
	
	@SubscribeEvent
	public void onEntityDamage(EntityDamageEvent e) {
		if(e.getEntity().equals(getPlayer())) {
			double doubleMaxHealth = VersionUtil.getMaxHealth(getPlayer());
			double doubleHealth = getPlayer().getHealth();
			
			int Health = (int) getPlayer().getHealth();
			
			if(doubleHealth <= (doubleMaxHealth / 100) * Percentage) {
				if(Health % 2 == 0) { //짝수
					e.setDamage(e.getDamage() + ((e.getDamage() / 100) * Even));
				} else { //홀수
					e.setDamage(e.getDamage() - ((e.getDamage() / 100) * Odd));
				}
			}
		}
	}
	
	@Override
	public void onRestrictClear() {}

	@Override
	public void TargetSkill(MaterialType mt, LivingEntity entity) {}
	
}
