package daybreak.abilitywar.ability.list.grapplinghook.v1_13_R2;

import daybreak.abilitywar.utils.base.minecraft.ability.list.grapplinghook.HookEntity;
import net.minecraft.server.v1_13_R2.DataWatcherObject;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityFishingHook;
import net.minecraft.server.v1_13_R2.EntityHuman;
import net.minecraft.server.v1_13_R2.ItemStack;
import net.minecraft.server.v1_13_R2.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Field;

public class EntityHook extends EntityFishingHook implements HookEntity {

	private static final DataWatcherObject<Integer> b;

	static {
		try {
			final Field field = EntityFishingHook.class.getDeclaredField("b");
			field.setAccessible(true);
			b = (DataWatcherObject<Integer>) field.get(null);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private final Entity target;

	EntityHook(World world, EntityHuman entityhuman, Location location) {
		super(world, entityhuman);
		this.target = new EntityStand(world, location);
		this.hooked = target;
		this.getDataWatcher().set(b, target.getId() + 1);
		setPosition(location.getX(), location.getY(), location.getZ());
		world.addEntity(this);
	}

	EntityHook(World world, EntityHuman entityhuman, LivingEntity targetEntity) {
		super(world, entityhuman);
		this.target = ((CraftLivingEntity) targetEntity).getHandle();
		this.hooked = target;
		this.getDataWatcher().set(b, target.getId() + 1);
		setPosition(target.locX, target.locY, target.locZ);
		world.addEntity(this);
	}

	@Override
	public void tick() {
		this.hooked = target;
		this.getDataWatcher().set(b, target.getId() + 1);
	}

	@Override
	public int b(ItemStack stack) {
		return 0;
	}

	@Override
	public void die() {
		if (target instanceof EntityStand) target.die();
		super.die();
	}
}
