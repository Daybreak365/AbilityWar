package daybreak.abilitywar.utils.base.minecraft.damage.v1_18_R1;

import daybreak.abilitywar.utils.base.minecraft.damage.IDamages;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.damagesource.EntityDamageSourceIndirect;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.projectile.EntityArrow;
import net.minecraft.world.entity.projectile.EntityPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.MovingObjectPosition;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class DamageImpl implements IDamages {
	@Override
	public boolean damageExplosion(@NotNull Entity entity, @NotNull Entity source, float damage) {
		final net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle(), nmsSource = ((CraftEntity) source).getHandle();
		final Explosion explosion = new Explosion(
				nmsEntity.getLevel(), nmsSource, nmsSource.getX(), nmsSource.getY(), nmsSource.getZ(), 0f
		);
		return nmsEntity.hurt(explosion.getDamageSource(), damage);
	}

	@Override
	public boolean damageThorn(@NotNull Entity entity, @NotNull LivingEntity damager, float damage) {
		final DamageSource source = new EntityDamageSource("thorns", ((CraftLivingEntity) damager).getHandle()).setThorns();
		source.setMagic();
		return ((CraftEntity) entity).getHandle().hurt(source, damage);
	}

	@Override
	public boolean damageArrow(@NotNull Entity entity, @NotNull LivingEntity shooter, float damage) {
		final net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
		final EntityLiving nmsShooter = ((CraftLivingEntity) shooter).getHandle();
		return nmsEntity.hurt(DamageSource.arrow(new EntityArrow(EntityTypes.ARROW, nmsShooter, ((net.minecraft.world.entity.Entity) nmsShooter).getLevel()) {
			@Override
			protected void onHit(MovingObjectPosition movingObjectPosition) {
			}

			@Override
			protected ItemStack getPickupItem() {
				return null;
			}
		}, nmsShooter), damage);
	}

	@Override
	public boolean damageFixed(@NotNull Entity entity, @NotNull LivingEntity damager, float damage) {
		return ((CraftEntity) entity).getHandle().hurt(new EntityDamageSource(damager instanceof Player ? "player" : "mob", ((CraftLivingEntity) damager).getHandle()) {
			{
				try {
					// Protected DamageSource members in the 1.18 runtime mappings.
					java.lang.reflect.Method armor = DamageSource.class.getDeclaredMethod("m");
					java.lang.reflect.Method magic = DamageSource.class.getDeclaredMethod("p");
					armor.setAccessible(true);
					magic.setAccessible(true);
					armor.invoke(this);
					magic.invoke(this);
				} catch (ReflectiveOperationException error) {
					throw new IllegalStateException("Cannot configure fixed damage", error);
				}
			}
		}, damage);
	}

	private static final ItemStack SPLASH_POTION = new ItemStack(Items.SPLASH_POTION);

	@Override
	public boolean damageMagic(@NotNull Entity entity, @Nullable Player damager, boolean ignoreArmor, float damage) {
		final net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
		final net.minecraft.server.level.EntityPlayer nmsDamager = damager != null ? ((CraftPlayer) damager).getHandle() : null;
		return nmsEntity.hurt(ignoreArmor ? DamageSource.MAGIC : (
				nmsDamager != null ?
						new EntityDamageSourceIndirect("magic", setItem(new EntityPotion(nmsEntity.getLevel(), nmsDamager)), nmsDamager)
						: new EntityDamageSourceIndirect("magic", setItem(new EntityPotion(nmsEntity.getLevel(), nmsEntity.getX(), nmsEntity.getY(), nmsEntity.getZ())), null)
		), damage);
	}

	private EntityPotion setItem(final EntityPotion potion) {
		((net.minecraft.world.entity.projectile.EntityProjectileThrowable) potion).setItem(SPLASH_POTION);
		return potion;
	}
}
