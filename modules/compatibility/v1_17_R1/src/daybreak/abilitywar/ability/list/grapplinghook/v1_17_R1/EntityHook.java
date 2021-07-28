package daybreak.abilitywar.ability.list.grapplinghook.v1_17_R1;

import daybreak.abilitywar.utils.base.minecraft.ability.list.grapplinghook.HookEntity;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.EntityFishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.level.gameevent.GameEvent;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;

public class EntityHook extends EntityFishingHook implements HookEntity {

	private static final DataWatcherObject<Integer> HOOKED_ENTITY = EntityFishingHook.f;

	private final Entity target;

	EntityHook(World world, EntityHuman entityhuman, Location location) {
		super(entityhuman, world, 0, 0);
		this.target = new EntityStand(world, location);
		this.av = target;
		this.getDataWatcher().set(HOOKED_ENTITY, target.getId() + 1);
		setPosition(location.getX(), location.getY(), location.getZ());
		world.addEntity(this);
	}

	EntityHook(World world, EntityHuman entityhuman, LivingEntity targetEntity) {
		super(entityhuman, world, 0, 0);
		this.target = ((CraftLivingEntity) targetEntity).getHandle();
		this.av = target;
		this.getDataWatcher().set(HOOKED_ENTITY, target.getId() + 1);
		setPosition(target.locX(), target.locY(), target.locZ());
		world.addEntity(this);
	}

	@Override
	public void tick() {
		this.getDataWatcher().set(HOOKED_ENTITY, target.getId() + 1);
	}

	@Override
	public int a(ItemStack stack) {
		return 0;
	}

	@Override
	public void a(GameEvent gameevent) {
		if (gameevent == GameEvent.s) {
			if (target instanceof EntityStand) target.die();
		}
		super.a(gameevent);
	}

}
