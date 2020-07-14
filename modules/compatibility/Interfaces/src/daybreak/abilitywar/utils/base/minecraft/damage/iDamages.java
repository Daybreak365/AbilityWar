package daybreak.abilitywar.utils.base.minecraft.damage;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public interface iDamages {

	boolean damageArrow(Entity entity, LivingEntity shooter, float damage);

	boolean damageFixed(Entity entity, Player damager, float damage);

}
