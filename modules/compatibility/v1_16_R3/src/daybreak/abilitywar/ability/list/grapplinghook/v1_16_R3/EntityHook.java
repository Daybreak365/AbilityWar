package daybreak.abilitywar.ability.list.grapplinghook.v1_16_R3;

import daybreak.abilitywar.ability.list.grapplinghook.HookEntity;
import net.minecraft.server.v1_16_R3.DataWatcherObject;
import net.minecraft.server.v1_16_R3.EntityFishingHook;
import net.minecraft.server.v1_16_R3.EntityHuman;
import net.minecraft.server.v1_16_R3.ItemStack;
import net.minecraft.server.v1_16_R3.World;
import org.bukkit.Location;

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

	private final EntityStand stand;

	EntityHook(World world, EntityHuman entityhuman, Location location) {
		super(entityhuman, world, 0, 0);
		this.stand = new EntityStand(world, location);
		hooked.setAccessible(true);
		try {
			hooked.set(this, stand);
		} catch (IllegalAccessException ignored) {}
		this.getDataWatcher().set(e, stand.getId() + 1);
		setPosition(location.getX(), location.getY(), location.getZ());
		world.addEntity(this);
	}

	@Override
	public void tick() {
		this.getDataWatcher().set(e, stand.getId() + 1);
	}

	@Override
	public int b(ItemStack stack) {
		return 0;
	}

	@Override
	public void die() {
		stand.die();
		super.die();
	}
}
