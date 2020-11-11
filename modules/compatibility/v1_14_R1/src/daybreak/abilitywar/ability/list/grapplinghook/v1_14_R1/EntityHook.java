package daybreak.abilitywar.ability.list.grapplinghook.v1_14_R1;

import daybreak.abilitywar.ability.list.grapplinghook.HookEntity;
import net.minecraft.server.v1_14_R1.World;
import net.minecraft.server.v1_14_R1.DataWatcherObject;
import net.minecraft.server.v1_14_R1.EntityFishingHook;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.ItemStack;
import org.bukkit.Location;

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

	private final EntityStand stand;

	EntityHook(World world, EntityHuman entityhuman, Location location) {
		super(entityhuman, world, 0, 0);
		this.stand = new EntityStand(world, location);
		this.hooked = stand;
		this.getDataWatcher().set(b, stand.getId() + 1);
		setPosition(location.getX(), location.getY(), location.getZ());
		world.addEntity(this);
	}

	@Override
	public void tick() {
		this.hooked = stand;
		this.getDataWatcher().set(b, stand.getId() + 1);
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
