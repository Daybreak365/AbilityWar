package daybreak.abilitywar.utils.base.minecraft.damage.v1_15_R1;

import daybreak.abilitywar.utils.base.minecraft.damage.IDamages;
import net.minecraft.server.v1_15_R1.DamageSource;
import net.minecraft.server.v1_15_R1.EntityArrow;
import net.minecraft.server.v1_15_R1.EntityDamageSource;
import net.minecraft.server.v1_15_R1.EntityDamageSourceIndirect;
import net.minecraft.server.v1_15_R1.EntityLiving;
import net.minecraft.server.v1_15_R1.EntityPotion;
import net.minecraft.server.v1_15_R1.EntityTypes;
import net.minecraft.server.v1_15_R1.ItemStack;
import net.minecraft.server.v1_15_R1.Items;
import net.minecraft.server.v1_15_R1.MovingObjectPosition;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DamageImpl implements IDamages {
	@Override
	public boolean damageArrow(@NotNull Entity entity, @NotNull LivingEntity shooter, float damage) {
		final net.minecraft.server.v1_15_R1.Entity nmsEntity = ((CraftEntity) entity).getHandle();
		final EntityLiving nmsShooter = ((CraftLivingEntity) shooter).getHandle();
		return nmsEntity.damageEntity(DamageSource.arrow(new EntityArrow(EntityTypes.ARROW, nmsShooter, nmsShooter.getWorld()) {
			@Override
			protected void a(MovingObjectPosition movingObjectPosition) {
			}

			@Override
			protected ItemStack getItemStack() {
				return null;
			}
		}, nmsShooter), damage);
	}

	@Override
	public boolean damageFixed(@NotNull Entity entity, @NotNull Player damager, float damage) {
		return ((CraftEntity) entity).getHandle().damageEntity(new EntityDamageSource("player", ((CraftPlayer) damager).getHandle()) {
			{
				setIgnoreArmor();
				setStarvation();
			}
		}, damage);
	}

	private static final ItemStack SPLASH_POTION = new ItemStack(Items.SPLASH_POTION);

	@Override
	public boolean damageMagic(@NotNull Entity entity, @Nullable Player damager, boolean ignoreArmor, float damage) {
		final net.minecraft.server.v1_15_R1.Entity nmsEntity = ((CraftEntity) entity).getHandle();
		final net.minecraft.server.v1_15_R1.EntityPlayer nmsDamager = damager != null ? ((CraftPlayer) damager).getHandle() : null;
		return nmsEntity.damageEntity(ignoreArmor ? DamageSource.MAGIC : (
				nmsDamager != null ?
						new EntityDamageSourceIndirect("magic", setItem(new EntityPotion(nmsEntity.getWorld(), nmsDamager)), nmsDamager)
						: new EntityDamageSourceIndirect("magic", setItem(new EntityPotion(nmsEntity.getWorld(), nmsEntity.locX(), nmsEntity.locY(), nmsEntity.locZ())), null)
		), damage);
	}

	private EntityPotion setItem(final EntityPotion potion) {
		potion.setItem(SPLASH_POTION);
		return potion;
	}
}
