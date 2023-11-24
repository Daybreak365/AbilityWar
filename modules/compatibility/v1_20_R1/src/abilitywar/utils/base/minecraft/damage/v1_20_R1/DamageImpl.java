package abilitywar.utils.base.minecraft.damage.v1_20_R1;

import daybreak.abilitywar.utils.base.minecraft.damage.IDamages;
import daybreak.abilitywar.utils.base.reflect.ReflectionUtil;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.projectile.EntityArrow;
import net.minecraft.world.entity.projectile.EntityPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.MovingObjectPosition;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;

public class DamageImpl implements IDamages {
	@Override
	public boolean damageExplosion(@NotNull Entity entity, @NotNull Entity source, float damage) {
		final net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle(), nmsSource = ((CraftEntity) source).getHandle();
		final Explosion explosion = new Explosion(
				nmsEntity.level(), nmsSource, nmsSource.getX(), nmsSource.getY(), nmsSource.getZ(), 0f
		);
		return nmsEntity.hurt(explosion.getDamageSource(), damage);
	}

	@Override
	public boolean damageThorn(@NotNull Entity entity, @NotNull LivingEntity damager, float damage) {
		return ((CraftEntity) entity).getHandle().hurt(new EntityDamageSource("thorns", ((CraftLivingEntity) damager).getHandle()) {
			{
				setThorns();
				setMagic();
			}
		}, damage);
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
				// 버전별 호환 필요
				try {
					//bypassEnchantments();
					ReflectionUtil.setAccessible(DamageSource.class.getDeclaredMethod("n")).invoke(this);
					//bypassArmor();
					ReflectionUtil.setAccessible(DamageSource.class.getDeclaredMethod("r")).invoke(this);
				} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}
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
		potion.setItem(SPLASH_POTION);
		return potion;
	}
}
