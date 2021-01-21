package daybreak.abilitywar.ability.list.grapplinghook.v1_10_R1;

import daybreak.abilitywar.utils.base.minecraft.ability.list.grapplinghook.HookEntity;
import net.minecraft.server.v1_10_R1.DataWatcherObject;
import net.minecraft.server.v1_10_R1.Entity;
import net.minecraft.server.v1_10_R1.EntityFishingHook;
import net.minecraft.server.v1_10_R1.EntityHuman;
import net.minecraft.server.v1_10_R1.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Field;

public class EntityHook extends EntityFishingHook implements HookEntity {

	private static final DataWatcherObject<Integer> c;

	static {
		try {
			final Field field = EntityFishingHook.class.getDeclaredField("c");
			field.setAccessible(true);
			c = (DataWatcherObject<Integer>) field.get(null);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private final Entity target;

	EntityHook(World world, EntityHuman entityhuman, Location location) {
		super(world, entityhuman);
		this.target = new EntityStand(world, location);
		this.hooked = target;
		this.getDataWatcher().set(c, target.getId() + 1);
		setPosition(location.getX(), location.getY(), location.getZ());
		world.addEntity(this);
	}

	EntityHook(World world, EntityHuman entityhuman, LivingEntity targetEntity) {
		super(world, entityhuman);
		this.target = ((CraftLivingEntity) targetEntity).getHandle();
		this.hooked = target;
		this.getDataWatcher().set(c, target.getId() + 1);
		setPosition(target.locX, target.locY, target.locZ);
		world.addEntity(this);
	}

	@Override
	public void m() {
		this.hooked = target;
		this.getDataWatcher().set(c, target.getId() + 1);
	}

	@Override
	public int j() {
		return 0;
	}

	@Override
	public void die() {
		if (target instanceof EntityStand) target.die();
		super.die();
	}
}
