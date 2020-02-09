package daybreak.abilitywar.ability.decorator;

import daybreak.abilitywar.ability.AbilityBase.ClickType;
import org.bukkit.Material;

public interface ActiveHandler {

	/**
	 * 액티브 스킬 발동을 위해 사용됩니다.
	 *
	 * @param materialType 플레이어가 클릭할 때 손에 들고 있었던 아이템
	 * @param clickType    클릭의 종류
	 * @return 능력 발동 여부
	 */
	boolean ActiveSkill(Material materialType, ClickType clickType);

}
