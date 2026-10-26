package daybreak.abilitywar.utils.base.minecraft.damage.v1_21_R6;

import daybreak.abilitywar.utils.base.minecraft.damage.IDamages;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class DamageImpl implements IDamages {
	private static net.minecraft.world.entity.Entity handle(Entity entity) { return ((CraftEntity) entity).getHandle(); }
	private static boolean hurt(Entity entity, DamageSource source, float damage) {
		final net.minecraft.world.entity.Entity target = handle(entity);
		return target.hurtServer((ServerLevel) target.level(), source, damage);
	}
	@Override public boolean damageExplosion(Entity entity, Entity source, float damage) {
		return hurt(entity, handle(entity).damageSources().explosion(handle(source), handle(source)), damage);
	}
	@Override public boolean damageThorn(Entity entity, LivingEntity damager, float damage) {
		return hurt(entity, handle(entity).damageSources().thorns(handle(damager)), damage);
	}
	@Override public boolean damageArrow(Entity entity, LivingEntity shooter, float damage) {
		final net.minecraft.world.entity.projectile.Arrow arrow = new net.minecraft.world.entity.projectile.Arrow(EntityType.ARROW, handle(shooter).level());
		return hurt(entity, handle(entity).damageSources().arrow(arrow, handle(shooter)), damage);
	}
	@Override public boolean damageFixed(Entity entity, LivingEntity damager, float damage) {
		final net.minecraft.world.entity.Entity target = handle(entity);
		final net.minecraft.resources.ResourceKey<DamageType> type = damager instanceof Player ? DamageTypes.PLAYER_ATTACK : DamageTypes.MOB_ATTACK;
		final DamageSource source = new DamageSource(target.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(type), handle(damager)) {
			@Override public boolean is(TagKey<DamageType> tag) {
				return tag == DamageTypeTags.BYPASSES_ARMOR || tag == DamageTypeTags.BYPASSES_ENCHANTMENTS || super.is(tag);
			}
		};
		return hurt(entity, source, damage);
	}
	@Override public boolean damageMagic(Entity entity, Player damager, boolean ignoreArmor, float damage) {
		final net.minecraft.world.entity.Entity target = handle(entity);
		final DamageSource source = new DamageSource(target.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.MAGIC), damager == null ? null : handle(damager)) {
			@Override public boolean is(TagKey<DamageType> tag) {
				return tag == DamageTypeTags.BYPASSES_ARMOR ? ignoreArmor : super.is(tag);
			}
		};
		return hurt(entity, source, damage);
	}
}
