package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.minecraft.version.VersionUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;

@AbilityManifest(Name = "홀수강박증", Rank = Rank.C, Species = Species.HUMAN)
public class OnlyOddNumber extends AbilityBase {

	public static final SettingObject<Integer> OddNumberConfig = new SettingObject<Integer>(OnlyOddNumber.class, "OddNumber", 59,
			"# 체력이 홀수일 때 대미지를 몇 퍼센트 줄여 받을지 설정합니다.",
			"# 60으로 설정하면 원래 대미지의 40%를 받습니다.") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 1 && value <= 100 && value % 2 != 0;
		}

	};

	public static final SettingObject<Integer> EvenNumberConfig = new SettingObject<Integer>(OnlyOddNumber.class, "EvenNumber", 29,
			"# 체력이 짝수일 때 대미지를 몇 퍼센트 늘려 받을지 설정합니다.",
			"# 30으로 설정하면 원래 대미지의 130%를 받습니다.") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 1 && value <= 100 && value % 2 != 0;
		}

	};

	public OnlyOddNumber(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f공격을 받으면 체력에 따라 다른 효과를 받습니다. &f체력이 홀수일 경우 대미지를"),
				ChatColor.translateAlternateColorCodes('&', OddNumberConfig.getValue() + "% 줄여 받고, 체력이 짝수일 경우 대미지를 " + EvenNumberConfig.getValue() + "% 늘려 받습니다."));
	}

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		return false;
	}

	private final int odd = OddNumberConfig.getValue();
	private final int even = EvenNumberConfig.getValue();

	@SubscribeEvent
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity().equals(getPlayer())) {
			double doubleMaxHealth = VersionUtil.getMaxHealth(getPlayer());
			double doubleHealth = getPlayer().getHealth();

			int health = (int) getPlayer().getHealth();

			if (health % 2 == 0) { //짝수
				e.setDamage(e.getDamage() + ((e.getDamage() / 100) * even));
			} else { //홀수
				e.setDamage(e.getDamage() - ((e.getDamage() / 100) * odd));
			}
		}
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
	}

}
