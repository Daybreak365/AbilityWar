package daybreak.abilitywar.ability.decorator

import daybreak.abilitywar.ability.AbilityBase.ClickType
import org.bukkit.Material
import org.bukkit.entity.LivingEntity

interface TargetHandler {
	/**
	 * 타겟팅 스킬 발동을 위해 사용됩니다.
	 *
	 * @param material 플레이어가 손에 들고 있는 아이템의 종류
	 * @param entity       타겟팅의 대상
	 */
	fun TargetSkill(material: Material, entity: LivingEntity)
}

interface ActiveHandler {
	/**
	 * 액티브 스킬 발동을 위해 사용됩니다.
	 *
	 * @param material  플레이어가 손에 들고 있는 아이템의 종류
	 * @param clickType 클릭의 종류
	 * @return 능력 발동 여부
	 */
	fun ActiveSkill(material: Material, clickType: ClickType): Boolean
}