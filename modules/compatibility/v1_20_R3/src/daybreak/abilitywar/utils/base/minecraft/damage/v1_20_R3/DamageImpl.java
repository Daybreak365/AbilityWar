package daybreak.abilitywar.utils.base.minecraft.damage.v1_20_R3;

import daybreak.abilitywar.utils.base.minecraft.damage.IDamages;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.projectile.EntityArrow;
import net.minecraft.world.entity.projectile.EntityPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.MovingObjectPosition;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

public class DamageImpl implements IDamages {
	@Override
	public boolean damageExplosion(@NotNull Entity entity, @NotNull Entity source, float damage) {
		final net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle(), nmsSource = ((CraftEntity) source).getHandle();
		final Explosion explosion = new Explosion(
				nmsEntity.level(), nmsSource, nmsSource.getX(), nmsSource.getY(), nmsSource.getZ(), 0f, false, Explosion.Effect.KEEP
		);
		return nmsEntity.hurt(((CraftEntity) entity).getHandle().damageSources().explosion(explosion), damage);
	}

	@Override
	public boolean damageThorn(@NotNull Entity entity, @NotNull LivingEntity damager, float damage) {
		return ((CraftEntity) entity).getHandle().hurt(((CraftEntity) entity).getHandle().damageSources().thorns(((CraftLivingEntity) damager).getHandle()), damage);
	}

	@Override
	public boolean damageArrow(@NotNull Entity entity, @NotNull LivingEntity shooter, float damage) {
		final net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
		final EntityLiving nmsShooter = ((CraftLivingEntity) shooter).getHandle();
		return nmsEntity.hurt(((CraftEntity) entity).getHandle().damageSources().arrow(new EntityArrow(EntityTypes.ARROW, ((net.minecraft.world.entity.Entity) nmsShooter).level(), ItemStack.EMPTY) {
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
		if (damager instanceof Player) {
			final DamageSource source = ((CraftEntity) entity).getHandle().damageSources().playerAttack(((CraftHumanEntity) damager).getHandle()).poison();
			return ((CraftEntity) entity).getHandle().hurt(source, damage);
		} else {
			final DamageSource source = ((CraftEntity) entity).getHandle().damageSources().mobAttack(((CraftLivingEntity) damager).getHandle()).poison();
			return ((CraftEntity) entity).getHandle().hurt(source, damage);
		}
	}

	private static final ItemStack SPLASH_POTION = new ItemStack(Items.SPLASH_POTION);

	@Override
	@SuppressWarnings("unchecked")
	public boolean damageMagic(@NotNull Entity entity, @Nullable Player damager, boolean ignoreArmor, float damage) {
		final net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
		final net.minecraft.server.level.EntityPlayer nmsDamager = damager != null ? ((CraftPlayer) damager).getHandle() : null;
		final Holder<DamageType> magicHolder;
		try {
			final Field dtField = DamageSources.class.getDeclaredField("damageTypes");
			dtField.setAccessible(true);
			final Object damageTypes = dtField.get(((CraftEntity) entity).getHandle().damageSources());
			magicHolder = (Holder<DamageType>) IRegistry.class.getDeclaredMethod("getHolderOrThrow", ResourceKey.class).invoke(damageTypes, DamageTypes.MAGIC);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
		final DamageSource damageSource = nmsDamager == null ? new DamageSource(magicHolder) : new DamageSource(magicHolder, ((CraftEntity) damager).getHandle());
		if (ignoreArmor) damageSource.poison();
		return nmsEntity.hurt(damageSource, damage);
	}

	private EntityPotion setItem(final EntityPotion potion) {
		potion.setItem(SPLASH_POTION);
		return potion;
	}
}
