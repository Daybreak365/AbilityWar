package daybreak.abilitywar.utils.base.minecraft.damage.v1_16_R2;

import daybreak.abilitywar.utils.base.minecraft.damage.IDamages;
import net.minecraft.server.v1_16_R2.DamageSource;
import net.minecraft.server.v1_16_R2.EntityArrow;
import net.minecraft.server.v1_16_R2.EntityDamageSource;
import net.minecraft.server.v1_16_R2.EntityDamageSourceIndirect;
import net.minecraft.server.v1_16_R2.EntityLiving;
import net.minecraft.server.v1_16_R2.EntityPotion;
import net.minecraft.server.v1_16_R2.EntityTypes;
import net.minecraft.server.v1_16_R2.Explosion;
import net.minecraft.server.v1_16_R2.Explosion.Effect;
import net.minecraft.server.v1_16_R2.ItemStack;
import net.minecraft.server.v1_16_R2.Items;
import net.minecraft.server.v1_16_R2.MovingObjectPosition;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DamageImpl implements IDamages {
	@Override
	public boolean damageExplosion(@NotNull Entity entity, @NotNull Entity source, float damage) {
		final net.minecraft.server.v1_16_R2.Entity nmsEntity = ((CraftEntity) entity).getHandle(), nmsSource = ((CraftEntity) source).getHandle();
		final Explosion explosion = new Explosion(
				nmsEntity.world, nmsSource, null, null,
				nmsSource.locX(), nmsSource.locY(), nmsSource.locZ(), 0f, false, Effect.NONE
		);
		return nmsEntity.damageEntity(explosion.b(), damage);
	}

	@Override
	public boolean damageThorn(@NotNull Entity entity, @NotNull LivingEntity damager, float damage) {
		return ((CraftEntity) entity).getHandle().damageEntity(new EntityDamageSource("thorns", ((CraftLivingEntity) damager).getHandle()) {
			{
				x();
				setMagic();
			}
		}, damage);
	}

	@Override
	public boolean damageArrow(@NotNull Entity entity, @NotNull LivingEntity shooter, float damage) {
		final net.minecraft.server.v1_16_R2.Entity nmsEntity = ((CraftEntity) entity).getHandle();
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
	public boolean damageFixed(@NotNull Entity entity, @NotNull LivingEntity damager, float damage) {
		return ((CraftEntity) entity).getHandle().damageEntity(new EntityDamageSource(damager instanceof Player ? "player" : "mob", ((CraftLivingEntity) damager).getHandle()) {
			{
				setIgnoreArmor();
				setStarvation();
			}
		}, damage);
	}

	private static final ItemStack SPLASH_POTION = new ItemStack(Items.SPLASH_POTION);

	@Override
	public boolean damageMagic(@NotNull Entity entity, @Nullable Player damager, boolean ignoreArmor, float damage) {
		final net.minecraft.server.v1_16_R2.Entity nmsEntity = ((CraftEntity) entity).getHandle();
		final net.minecraft.server.v1_16_R2.EntityPlayer nmsDamager = damager != null ? ((CraftPlayer) damager).getHandle() : null;
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
