package daybreak.abilitywar.utils.base.minecraft.damage.v1_11_R1;

import daybreak.abilitywar.utils.base.minecraft.damage.IDamages;
import net.minecraft.server.v1_11_R1.DamageSource;
import net.minecraft.server.v1_11_R1.EntityArrow;
import net.minecraft.server.v1_11_R1.EntityDamageSource;
import net.minecraft.server.v1_11_R1.EntityLiving;
import net.minecraft.server.v1_11_R1.ItemStack;
import net.minecraft.server.v1_11_R1.MovingObjectPosition;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class DamageImpl implements IDamages {
	@Override
	public boolean damageArrow(Entity entity, LivingEntity shooter, float damage) {
		final net.minecraft.server.v1_11_R1.Entity nmsEntity = ((CraftEntity) entity).getHandle();
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
	public boolean damageFixed(Entity entity, Player damager, float damage) {
		return ((CraftEntity) entity).getHandle().damageEntity(new EntityDamageSource("player", ((CraftPlayer) damager).getHandle()) {
			{
				setIgnoreArmor();
				m();
			}
		}, damage);
	}
}
