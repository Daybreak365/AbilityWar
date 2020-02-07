package daybreak.abilitywar.ability.decorator;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;

public interface TargetHandler {

	/**
	 * 타겟팅 스킬 발동을 위해 사용됩니다.
	 *
	 * @param materialType 플레이어가 클릭할 때 손에 들고 있었던 아이템
	 * @param entity       타겟팅의 대상, 타겟팅의 대상이 없을 경우 null이 될 수 있습니다. null 체크가 필요합니다.
	 */
	void TargetSkill(Material materialType, LivingEntity entity);

}
