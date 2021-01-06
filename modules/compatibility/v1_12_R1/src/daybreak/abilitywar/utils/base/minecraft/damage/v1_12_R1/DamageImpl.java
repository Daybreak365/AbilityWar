package daybreak.abilitywar.utils.base.minecraft.damage.v1_12_R1;

import daybreak.abilitywar.utils.base.minecraft.damage.Damages.INSTANCE.Flag;
import daybreak.abilitywar.utils.base.minecraft.damage.IDamages;
import net.minecraft.server.v1_12_R1.CombatMath;
import net.minecraft.server.v1_12_R1.DamageSource;
import net.minecraft.server.v1_12_R1.EnchantmentManager;
import net.minecraft.server.v1_12_R1.EntityArrow;
import net.minecraft.server.v1_12_R1.EntityDamageSource;
import net.minecraft.server.v1_12_R1.EntityDamageSourceIndirect;
import net.minecraft.server.v1_12_R1.EntityLiving;
import net.minecraft.server.v1_12_R1.EntityPotion;
import net.minecraft.server.v1_12_R1.GenericAttributes;
import net.minecraft.server.v1_12_R1.ItemStack;
import net.minecraft.server.v1_12_R1.Items;
import net.minecraft.server.v1_12_R1.MobEffect;
import net.minecraft.server.v1_12_R1.MobEffects;
import net.minecraft.server.v1_12_R1.MovingObjectPosition;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DamageImpl implements IDamages {

	public double getFinalDamage(@NotNull LivingEntity victim, @NotNull LivingEntity damager, double damage, int flags) {
		final EntityLiving nmsVictim = ((CraftLivingEntity) victim).getHandle(), nmsDamager = ((CraftLivingEntity) damager).getHandle();
		if (Flag.hasFlag(flags, Flag.ARMOR)) {
			damage -= (damage - CombatMath.a((float) damage, nmsVictim.getArmorStrength(), (float) nmsVictim.getAttributeInstance(GenericAttributes.i).getValue()));
		}
		if (Flag.hasFlag(flags, Flag.RESISTANCE)) {
			final MobEffect resistance = nmsVictim.getEffect(MobEffects.RESISTANCE);
			if (resistance != null) {
				damage -= (damage - ((damage * (float) (25 - ((resistance.getAmplifier() + 1) * 5))) / 25.0));
			}
		}
		if (Flag.hasFlag(flags, Flag.ENCHANTMENT)) {
			final int i = EnchantmentManager.a(nmsVictim.getArmorItems(), new EntityDamageSource("", nmsDamager));
			if (i > 0) {
				damage -= (damage - CombatMath.a((float) damage, i));
			}
		}
		if (Flag.hasFlag(flags, Flag.ABSORPTION)) {
			damage -= Math.max(damage - Math.max(damage - nmsVictim.getAbsorptionHearts(), 0.0), 0.0);
		}
		return damage;
	}

	@Override
	public boolean damageArrow(@NotNull Entity entity, @NotNull LivingEntity shooter, float damage) {
		final net.minecraft.server.v1_12_R1.Entity nmsEntity = ((CraftEntity) entity).getHandle();
		final EntityLiving nmsShooter = ((CraftLivingEntity) shooter).getHandle();
		return nmsEntity.damageEntity(DamageSource.arrow(new EntityArrow(nmsShooter.getWorld(), nmsShooter) {
			@Override
			protected void a(MovingObjectPosition movingObjectPosition) {
			}

			@Override
			protected ItemStack j() {
				return null;
			}
		}, nmsShooter), damage);
	}

	@Override
	public boolean damageFixed(@NotNull Entity entity, @NotNull LivingEntity damager, float damage) {
		return ((CraftEntity) entity).getHandle().damageEntity(new EntityDamageSource(damager instanceof Player ? "player" : "mob", ((CraftLivingEntity) damager).getHandle()) {
			{
				setIgnoreArmor();
				m();
			}
		}, damage);
	}

	private static final ItemStack SPLASH_POTION = new ItemStack(Items.SPLASH_POTION);

	@Override
	public boolean damageMagic(@NotNull Entity entity, @Nullable Player damager, boolean ignoreArmor, float damage) {
		final net.minecraft.server.v1_12_R1.Entity nmsEntity = ((CraftEntity) entity).getHandle();
		final net.minecraft.server.v1_12_R1.EntityPlayer nmsDamager = damager != null ? ((CraftPlayer) damager).getHandle() : null;
		return nmsEntity.damageEntity(ignoreArmor ? DamageSource.MAGIC : (
				nmsDamager != null ?
						new EntityDamageSourceIndirect("magic", new EntityPotion(nmsEntity.getWorld(), nmsDamager, SPLASH_POTION), nmsDamager)
						: new EntityDamageSourceIndirect("magic", new EntityPotion(nmsEntity.getWorld(), nmsEntity.locX, nmsEntity.locY, nmsEntity.locZ, SPLASH_POTION), null)
		), damage);
	}
}
