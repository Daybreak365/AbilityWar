package daybreak.abilitywar.utils.base.minecraft.damage;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IDamages {

	boolean damageArrow(@NotNull Entity entity, @NotNull LivingEntity shooter, float damage);
	boolean damageFixed(@NotNull Entity entity, @NotNull LivingEntity damager, float damage);
	boolean damageMagic(@NotNull Entity entity, @Nullable Player damager, boolean ignoreArmor, float damage);

}
