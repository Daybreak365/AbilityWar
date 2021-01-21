package daybreak.abilitywar.ability.list.grapplinghook.v1_16_R1;

import daybreak.abilitywar.utils.base.minecraft.ability.list.grapplinghook.HookEntity;
import net.minecraft.server.v1_16_R1.DataWatcherObject;
import net.minecraft.server.v1_16_R1.Entity;
import net.minecraft.server.v1_16_R1.EntityFishingHook;
import net.minecraft.server.v1_16_R1.EntityHuman;
import net.minecraft.server.v1_16_R1.ItemStack;
import net.minecraft.server.v1_16_R1.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Field;

public class EntityHook extends EntityFishingHook implements HookEntity {

	private static final DataWatcherObject<Integer> e;
	private static final Field hooked;

	static {
		try {
			final Field field = EntityFishingHook.class.getDeclaredField("e");
			field.setAccessible(true);
			e = (DataWatcherObject<Integer>) field.get(null);
			hooked = EntityFishingHook.class.getDeclaredField("hooked");
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private final Entity target;

	EntityHook(World world, EntityHuman entityhuman, Location location) {
		super(entityhuman, world, 0, 0);
		this.target = new EntityStand(world, location);
		hooked.setAccessible(true);
		try {
			hooked.set(this, target);
		} catch (IllegalAccessException ignored) {}
		this.getDataWatcher().set(e, target.getId() + 1);
		setPosition(location.getX(), location.getY(), location.getZ());
		world.addEntity(this);
	}

	EntityHook(World world, EntityHuman entityhuman, LivingEntity targetEntity) {
		super(entityhuman, world, 0, 0);
		this.target = ((CraftLivingEntity) targetEntity).getHandle();
		hooked.setAccessible(true);
		try {
			hooked.set(this, target);
		} catch (IllegalAccessException ignored) {}
		this.getDataWatcher().set(e, target.getId() + 1);
		setPosition(target.locX(), target.locY(), target.locZ());
		world.addEntity(this);
	}

	@Override
	public void tick() {
		this.getDataWatcher().set(e, target.getId() + 1);
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
