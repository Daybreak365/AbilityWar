package daybreak.abilitywar.ability.list.grapplinghook.v1_19_R3;

import daybreak.abilitywar.utils.base.minecraft.ability.list.grapplinghook.HookEntity;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.EntityFishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.IWorldWriter;
import net.minecraft.world.level.World;
import net.minecraft.world.level.gameevent.GameEvent;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;

public class EntityHook extends EntityFishingHook implements HookEntity {

	private static final DataWatcherObject<Integer> HOOKED_ENTITY = EntityFishingHook.DATA_HOOKED_ENTITY;

	private final Entity target;

	EntityHook(World world, EntityHuman entityhuman, Location location) {
		super(entityhuman, world, 0, 0);
		this.target = new EntityStand(world, location);
		this.hookedIn = target;
		((Entity) this).getEntityData().set(HOOKED_ENTITY, target.getId() + 1);
		((Entity) this).setPos(location.getX(), location.getY(), location.getZ());
		((IWorldWriter) world).addFreshEntity(this);
	}

	EntityHook(World world, EntityHuman entityhuman, LivingEntity targetEntity) {
		super(entityhuman, world, 0, 0);
		this.target = ((CraftLivingEntity) targetEntity).getHandle();
		this.hookedIn = target;
		((Entity) this).getEntityData().set(HOOKED_ENTITY, target.getId() + 1);
		((Entity) this).setPos(target.getX(), target.getY(), target.getZ());
		((IWorldWriter) world).addFreshEntity(this);
	}

	@Override
	public void tick() {
		((Entity) this).getEntityData().set(HOOKED_ENTITY, target.getId() + 1);
	}

	@Override
	public int retrieve(ItemStack stack) {
		return 0;
	}

	@Override
	public void gameEvent(GameEvent gameevent) {
		if (gameevent == GameEvent.ENTITY_DIE) {
			if (target instanceof EntityStand) target.discard();
		}
		super.gameEvent(gameevent);
	}

	@Override
	public void die() {
		((Entity) this).discard();
	}
}
