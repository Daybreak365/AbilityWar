package daybreak.abilitywar.utils.base.minecraft.damage.v1_17_R1;

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
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
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
				nmsEntity.getWorld(), nmsSource, nmsSource.locX(), nmsSource.locY(), nmsSource.locZ(), 0f
		);
		return nmsEntity.damageEntity(explosion.b(), damage);
	}

	@Override
	public boolean damageThorn(@NotNull Entity entity, @NotNull LivingEntity damager, float damage) {
		return ((CraftEntity) entity).getHandle().damageEntity(new EntityDamageSource("thorns", ((CraftLivingEntity) damager).getHandle()) {
			{
				D();
				setMagic();
			}
		}, damage);
	}

	@Override
	public boolean damageArrow(@NotNull Entity entity, @NotNull LivingEntity shooter, float damage) {
		final net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
		final EntityLiving nmsShooter = ((CraftLivingEntity) shooter).getHandle();
		return nmsEntity.damageEntity(DamageSource.arrow(new EntityArrow(EntityTypes.d, nmsShooter, nmsShooter.getWorld()) {
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

	private static final ItemStack SPLASH_POTION = new ItemStack(Items.sr);

	@Override
	public boolean damageMagic(@NotNull Entity entity, @Nullable Player damager, boolean ignoreArmor, float damage) {
		final net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
		final net.minecraft.server.level.EntityPlayer nmsDamager = damager != null ? ((CraftPlayer) damager).getHandle() : null;
		return nmsEntity.damageEntity(ignoreArmor ? DamageSource.o : (
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
