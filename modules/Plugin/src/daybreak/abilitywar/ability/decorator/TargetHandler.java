package daybreak.abilitywar.ability.decorator;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;

public interface TargetHandler {
	/**
	 * 타게팅 스킬 발동을 위해 사용됩니다.
	 *
	 * @param material 플레이어가 손에 들고 있는 아이템의 종류
	 * @param entity       타게팅의 대상
	 */
	void TargetSkill(Material material, LivingEntity entity);
}
