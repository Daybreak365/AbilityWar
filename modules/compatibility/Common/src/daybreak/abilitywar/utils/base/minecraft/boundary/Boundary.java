package daybreak.abilitywar.utils.base.minecraft.boundary;

import com.google.common.base.Enums;
import com.google.common.base.Optional;
import org.bukkit.entity.EntityType;

import java.util.EnumMap;

public class Boundary {

	private static final EnumMap<EntityType, BoundaryData> BOUNDARY_DATA_MAP = new EnumMap<>(EntityType.class);

	public static final BoundaryData AREA_EFFECT_CLOUD = new BoundaryData("AREA_EFFECT_CLOUD", -3, 0, -3, 3, 0.5, 3);
	public static final BoundaryData ARMOR_STAND = new BoundaryData("ARMOR_STAND", -0.25, 0, -0.25, 0.25, 1.975000024, 0.25);
	public static final BoundaryData ARROW = new BoundaryData("ARROW", -0.25, 0, -0.25, 0.25, 0.5, 0.25);
	public static final BoundaryData BAT = new BoundaryData("BAT", -0.25, 0, -0.25, 0.25, 0.899999976, 0.25);
	public static final BoundaryData BEE = new BoundaryData("BEE", -0.349999994, 0, -0.349999994, 0.349999994, 0.600000024, 0.349999994);
	public static final BoundaryData BLAZE = new BoundaryData("BLAZE", -0.300000012, 0, -0.300000012, 0.300000012, 1.799999952, 0.300000012);
	public static final BoundaryData BOAT = new BoundaryData("BOAT", -0.6875, 0, -0.6875, 0.6875, 0.5625, 0.6875);
	public static final BoundaryData CAT = new BoundaryData("CAT", -0.300000012, 0, -0.300000012, 0.300000012, 0.699999988, 0.300000012);
	public static final BoundaryData CAVE_SPIDER = new BoundaryData("CAVE_SPIDER", -0.349999994, 0, -0.349999994, 0.349999994, 0.5, 0.349999994);
	public static final BoundaryData CHICKEN = new BoundaryData("CHICKEN", -0.200000003, 0, -0.200000003, 0.200000003, 0.699999988, 0.200000003);
	public static final BoundaryData COD = new BoundaryData("COD", -0.25, 0, -0.25, 0.25, 0.300000012, 0.25);
	public static final BoundaryData COW = new BoundaryData("COW", -0.449999988, 0, -0.449999988, 0.449999988, 1.399999976, 0.449999988);
	public static final BoundaryData CREEPER = new BoundaryData("CREEPER", -0.300000012, 0, -0.300000012, 0.300000012, 1.700000048, 0.300000012);
	public static final BoundaryData DOLPHIN = new BoundaryData("DOLPHIN", -0.449999988, 0, -0.449999988, 0.449999988, 0.600000024, 0.449999988);
	public static final BoundaryData DONKEY = new BoundaryData("DONKEY", -0.698242188, 0, -0.698242188, 0.698242188, 1.5, 0.698242188);
	public static final BoundaryData DRAGON_FIREBALL = new BoundaryData("DRAGON_FIREBALL", -0.5, 0, -0.5, 0.5, 1, 0.5);
	public static final BoundaryData DROPPED_ITEM = new BoundaryData("DROPPED_ITEM", -0.125, 0, -0.125, 0.125, 0.25, 0.125);
	public static final BoundaryData DROWNED = new BoundaryData("DROWNED", -0.300000012, 0, -0.300000012, 0.300000012, 1.950000048, 0.300000012);
	public static final BoundaryData EGG = new BoundaryData("EGG", -0.125, 0, -0.125, 0.125, 0.25, 0.125);
	public static final BoundaryData ELDER_GUARDIAN = new BoundaryData("ELDER_GUARDIAN", -0.998749971, 0, -0.998749971, 0.998749971, 1.997499943, 0.998749971);
	public static final BoundaryData ENDERMAN = new BoundaryData("ENDERMAN", -0.300000012, 0, -0.300000012, 0.300000012, 2.900000095, 0.300000012);
	public static final BoundaryData ENDERMITE = new BoundaryData("ENDERMITE", -0.200000003, 0, -0.200000003, 0.200000003, 0.300000012, 0.200000003);
	public static final BoundaryData ENDER_CRYSTAL = new BoundaryData("ENDER_CRYSTAL", -1, 0, -1, 1, 2, 1);
	public static final BoundaryData ENDER_DRAGON = new BoundaryData("ENDER_DRAGON", -8, 0, -8, 8, 8, 8);
	public static final BoundaryData ENDER_PEARL = new BoundaryData("ENDER_PEARL", -0.125, 0, -0.125, 0.125, 0.25, 0.125);
	public static final BoundaryData ENDER_SIGNAL = new BoundaryData("ENDER_SIGNAL", -0.125, 0, -0.125, 0.125, 0.25, 0.125);
	public static final BoundaryData EVOKER = new BoundaryData("EVOKER", -0.300000012, 0, -0.300000012, 0.300000012, 1.950000048, 0.300000012);
	public static final BoundaryData EVOKER_FANGS = new BoundaryData("EVOKER_FANGS", -0.25, 0, -0.25, 0.25, 0.800000012, 0.25);
	public static final BoundaryData EXPERIENCE_ORB = new BoundaryData("EXPERIENCE_ORB", -0.25, 0, -0.25, 0.25, 0.5, 0.25);
	public static final BoundaryData FALLING_BLOCK = new BoundaryData("FALLING_BLOCK", -0.49000001, 0, -0.49000001, 0.49000001, 0.980000019, 0.49000001);
	public static final BoundaryData FIREBALL = new BoundaryData("FIREBALL", -0.5, 0, -0.5, 0.5, 1, 0.5);
	public static final BoundaryData FIREWORK = new BoundaryData("FIREWORK", -0.125, 0, -0.125, 0.125, 0.25, 0.125);
	public static final BoundaryData FOX = new BoundaryData("FOX", -0.300000012, 0, -0.300000012, 0.300000012, 0.699999988, 0.300000012);
	public static final BoundaryData GHAST = new BoundaryData("GHAST", -2, 0, -2, 2, 4, 2);
	public static final BoundaryData GIANT = new BoundaryData("GIANT", -1.799999952, 0, -1.799999952, 1.799999952, 12, 1.799999952);
	public static final BoundaryData GUARDIAN = new BoundaryData("GUARDIAN", -0.425000012, 0, -0.425000012, 0.425000012, 0.850000024, 0.425000012);
	public static final BoundaryData HOGLIN = new BoundaryData("HOGLIN", -0.6982421875, 0.0, -0.6982421875, 0.6982421875, 1.399999976158142, 0.6982421875);
	public static final BoundaryData HORSE = new BoundaryData("HORSE", -0.698242188, 0, -0.698242188, 0.698242188, 1.600000024, 0.698242188);
	public static final BoundaryData HUSK = new BoundaryData("HUSK", -0.300000012, 0, -0.300000012, 0.300000012, 1.950000048, 0.300000012);
	public static final BoundaryData ILLUSIONER = new BoundaryData("ILLUSIONER", -0.300000012, 0, -0.300000012, 0.300000012, 1.950000048, 0.300000012);
	public static final BoundaryData IRON_GOLEM = new BoundaryData("IRON_GOLEM", -0.699999988, 0, -0.699999988, 0.699999988, 2.700000048, 0.699999988);
	public static final BoundaryData ITEM_FRAME = new BoundaryData("ITEM_FRAME", -0.375, -0.03125, -0.375, 0.375, 0.03125, 0.375);
	public static final BoundaryData LLAMA = new BoundaryData("LLAMA", -0.449999988, 0, -0.449999988, 0.449999988, 1.870000005, 0.449999988);
	public static final BoundaryData LLAMA_SPIT = new BoundaryData("LLAMA_SPIT", -0.125, 0, -0.125, 0.125, 0.25, 0.125);
	public static final BoundaryData MAGMA_CUBE = new BoundaryData("MAGMA_CUBE", -0.260099977, 0, -0.260099977, 0.260099977, 0.520199955, 0.260099977);
	public static final BoundaryData MINECART = new BoundaryData("MINECART", -0.49000001, 0, -0.49000001, 0.49000001, 0.699999988, 0.49000001);
	public static final BoundaryData MINECART_CHEST = new BoundaryData("MINECART_CHEST", -0.49000001, 0, -0.49000001, 0.49000001, 0.699999988, 0.49000001);
	public static final BoundaryData MINECART_COMMAND = new BoundaryData("MINECART_COMMAND", -0.49000001, 0, -0.49000001, 0.49000001, 0.699999988, 0.49000001);
	public static final BoundaryData MINECART_FURNACE = new BoundaryData("MINECART_FURNACE", -0.49000001, 0, -0.49000001, 0.49000001, 0.699999988, 0.49000001);
	public static final BoundaryData MINECART_HOPPER = new BoundaryData("MINECART_HOPPER", -0.49000001, 0, -0.49000001, 0.49000001, 0.699999988, 0.49000001);
	public static final BoundaryData MINECART_MOB_SPAWNER = new BoundaryData("MINECART_MOB_SPAWNER", -0.49000001, 0, -0.49000001, 0.49000001, 0.699999988, 0.49000001);
	public static final BoundaryData MINECART_TNT = new BoundaryData("MINECART_TNT", -0.49000001, 0, -0.49000001, 0.49000001, 0.699999988, 0.49000001);
	public static final BoundaryData MULE = new BoundaryData("MULE", -0.698242188, 0, -0.698242188, 0.698242188, 1.600000024, 0.698242188);
	public static final BoundaryData MUSHROOM_COW = new BoundaryData("MUSHROOM_COW", -0.449999988, 0, -0.449999988, 0.449999988, 1.399999976, 0.449999988);
	public static final BoundaryData OCELOT = new BoundaryData("OCELOT", -0.300000012, 0, -0.300000012, 0.300000012, 0.699999988, 0.300000012);
	public static final BoundaryData PANDA = new BoundaryData("PANDA", -0.649999976, 0, -0.649999976, 0.649999976, 1.25, 0.649999976);
	public static final BoundaryData PARROT = new BoundaryData("PARROT", -0.25, 0, -0.25, 0.25, 0.899999976, 0.25);
	public static final BoundaryData PHANTOM = new BoundaryData("PHANTOM", -0.449999988, 0, -0.449999988, 0.449999988, 0.5, 0.449999988);
	public static final BoundaryData PIG = new BoundaryData("PIG", -0.449999988, 0, -0.449999988, 0.449999988, 0.899999976, 0.449999988);
	public static final BoundaryData PIGLIN = new BoundaryData("PIGLIN", -0.30000001192092896, 0.0, -0.30000001192092896, 0.30000001192092896, 1.9500000476837158, 0.30000001192092896);
	public static final BoundaryData PIGLIN_BRUTE = new BoundaryData("PIGLIN_BRUTE", -0.30000001192092896, 0.0, -0.30000001192092896, 0.30000001192092896, 1.9500000476837158, 0.30000001192092896);
	public static final BoundaryData PILLAGER = new BoundaryData("PILLAGER", -0.300000012, 0, -0.300000012, 0.300000012, 1.950000048, 0.300000012);
	public static final BoundaryData PLAYER = new BoundaryData("PLAYER", -0.300000012, 0, -0.300000012, 0.300000012, 1.799999952, 0.300000012);
	public static final BoundaryData POLAR_BEAR = new BoundaryData("POLAR_BEAR", -0.699999988, 0, -0.699999988, 0.699999988, 1.399999976, 0.699999988);
	public static final BoundaryData PRIMED_TNT = new BoundaryData("PRIMED_TNT", -0.49000001, 0, -0.49000001, 0.49000001, 0.980000019, 0.49000001);
	public static final BoundaryData PUFFERFISH = new BoundaryData("PUFFERFISH", -0.349999994, 0, -0.349999994, 0.349999994, 0.699999988, 0.349999994);
	public static final BoundaryData RABBIT = new BoundaryData("RABBIT", -0.200000003, 0, -0.200000003, 0.200000003, 0.5, 0.200000003);
	public static final BoundaryData RAVAGER = new BoundaryData("RAVAGER", -0.975000024, 0, -0.975000024, 0.975000024, 2.200000048, 0.975000024);
	public static final BoundaryData SALMON = new BoundaryData("SALMON", -0.349999994, 0, -0.349999994, 0.349999994, 0.400000006, 0.349999994);
	public static final BoundaryData SHEEP = new BoundaryData("SHEEP", -0.449999988, 0, -0.449999988, 0.449999988, 1.299999952, 0.449999988);
	public static final BoundaryData SHULKER = new BoundaryData("SHULKER", -0.5, 0, -0.5, 0.5, 1, 0.5);
	public static final BoundaryData SHULKER_BULLET = new BoundaryData("SHULKER_BULLET", -0.15625, 0, -0.15625, 0.15625, 0.3125, 0.15625);
	public static final BoundaryData SILVERFISH = new BoundaryData("SILVERFISH", -0.200000003, 0, -0.200000003, 0.200000003, 0.300000012, 0.200000003);
	public static final BoundaryData SKELETON = new BoundaryData("SKELETON", -0.300000012, 0, -0.300000012, 0.300000012, 1.99000001, 0.300000012);
	public static final BoundaryData SKELETON_HORSE = new BoundaryData("SKELETON_HORSE", -0.698242188, 0, -0.698242188, 0.698242188, 1.600000024, 0.698242188);
	public static final BoundaryData SLIME = new BoundaryData("SLIME", -0.520199955, 0, -0.520199955, 0.520199955, 1.040399909, 0.520199955);
	public static final BoundaryData SMALL_FIREBALL = new BoundaryData("SMALL_FIREBALL", -0.15625, 0, -0.15625, 0.15625, 0.3125, 0.15625);
	public static final BoundaryData SNOWBALL = new BoundaryData("SNOWBALL", -0.125, 0, -0.125, 0.125, 0.25, 0.125);
	public static final BoundaryData SNOWMAN = new BoundaryData("SNOWMAN", -0.349999994, 0, -0.349999994, 0.349999994, 1.899999976, 0.349999994);
	public static final BoundaryData SPECTRAL_ARROW = new BoundaryData("SPECTRAL_ARROW", -0.25, 0, -0.25, 0.25, 0.5, 0.25);
	public static final BoundaryData SPIDER = new BoundaryData("SPIDER", -0.699999988, 0, -0.699999988, 0.699999988, 0.899999976, 0.699999988);
	public static final BoundaryData SPLASH_POTION = new BoundaryData("SPLASH_POTION", -0.125, 0, -0.125, 0.125, 0.25, 0.125);
	public static final BoundaryData SQUID = new BoundaryData("SQUID", -0.400000006, 0, -0.400000006, 0.400000006, 0.800000012, 0.400000006);
	public static final BoundaryData STRAY = new BoundaryData("STRAY", -0.300000012, 0, -0.300000012, 0.300000012, 1.99000001, 0.349999994);
	public static final BoundaryData STRIDER = new BoundaryData("STRIDER", -0.44999998807907104, 0.0, -0.44999998807907104, 0.44999998807907104, 1.7000000476837158, 0.44999998807907104);
	public static final BoundaryData THROWN_EXP_BOTTLE = new BoundaryData("THROWN_EXP_BOTTLE", -0.125, 0, -0.125, 0.125, 0.25, 0.125);
	public static final BoundaryData TRADER_LLAMA = new BoundaryData("TRADER_LLAMA", -0.449999988, 0, -0.449999988, 0.449999988, 1.870000005, 0.449999988);
	public static final BoundaryData TRIDENT = new BoundaryData("TRIDENT", -0.25, 0, -0.25, 0.25, 0.5, 0.25);
	public static final BoundaryData TROPICAL_FISH = new BoundaryData("TROPICAL_FISH", -0.25, 0, -0.25, 0.25, 0.400000006, 0.25);
	public static final BoundaryData TURTLE = new BoundaryData("TURTLE", -0.600000024, 0, -0.600000024, 0.600000024, 0.400000006, 0.600000024);
	public static final BoundaryData VEX = new BoundaryData("VEX", -0.200000003, 0, -0.200000003, 0.200000003, 0.800000012, 0.200000003);
	public static final BoundaryData VILLAGER = new BoundaryData("VILLAGER", -0.300000012, 0, -0.300000012, 0.300000012, 1.950000048, 0.300000012);
	public static final BoundaryData VINDICATOR = new BoundaryData("VINDICATOR", -0.300000012, 0, -0.300000012, 0.300000012, 1.950000048, 0.300000012);
	public static final BoundaryData WANDERING_TRADER = new BoundaryData("WANDERING_TRADER", -0.300000012, 0, -0.300000012, 0.300000012, 1.950000048, 0.300000012);
	public static final BoundaryData WITCH = new BoundaryData("WITCH", -0.300000012, 0, -0.300000012, 0.300000012, 1.950000048, 0.300000012);
	public static final BoundaryData WITHER = new BoundaryData("WITHER", -0.449999988, 0, -0.449999988, 0.449999988, 3.5, 0.449999988);
	public static final BoundaryData WITHER_SKELETON = new BoundaryData("WITHER_SKELETON", -0.349999994, 0, -0.349999994, 0.349999994, 2.400000095, 0.349999994);
	public static final BoundaryData WITHER_SKULL = new BoundaryData("WITHER_SKULL", -0.15625, 0, -0.15625, 0.15625, 0.3125, 0.15625);
	public static final BoundaryData WOLF = new BoundaryData("WOLF", -0.300000012, 0, -0.300000012, 0.300000012, 0.850000024, 0.300000012);
	public static final BoundaryData ZOGLIN = new BoundaryData("ZOGLIN", -0.6982421875, 0.0, -0.6982421875, 0.6982421875, 1.399999976158142, 0.6982421875);
	public static final BoundaryData ZOMBIE = new BoundaryData("ZOMBIE", -0.300000012, 0, -0.300000012, 0.300000012, 1.950000048, 0.300000012);
	public static final BoundaryData ZOMBIE_HORSE = new BoundaryData("ZOMBIE_HORSE", -0.698242188, 0, -0.698242188, 0.698242188, 1.600000024, 0.698242188);
	public static final BoundaryData ZOMBIE_VILLAGER = new BoundaryData("ZOMBIE_VILLAGER", -0.300000012, 0, -0.300000012, 0.300000012, 1.950000048, 0.300000012);
	public static final BoundaryData ZOMBIFIED_PIGLIN = new BoundaryData(new String[]{"PIG_ZOMBIE", "ZOMBIFIED_PIGLIN"}, -0.30000001192092896, 0.0, -0.30000001192092896, 0.30000001192092896, 1.9500000476837158, 0.30000001192092896);
	public static final BoundaryData UNKNOWN = new BoundaryData(0, 0, 0, 0, 0, 0);

	public static class BoundaryData {

		public static BoundaryData of(EntityType entityType) {
			return BOUNDARY_DATA_MAP.getOrDefault(entityType, UNKNOWN);
		}

		final double minX, minY, minZ, maxX, maxY, maxZ;

		private BoundaryData(String[] entityTypes, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
			for (String entityType : entityTypes) {
				final Optional<EntityType> type = Enums.getIfPresent(EntityType.class, entityType);
				if (type.isPresent()) {
					BOUNDARY_DATA_MAP.put(type.get(), this);
				}
			}
			this.minX = minX;
			this.minY = minY;
			this.minZ = minZ;
			this.maxX = maxX;
			this.maxY = maxY;
			this.maxZ = maxZ;
		}

		private BoundaryData(String entityType, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
			final Optional<EntityType> type = Enums.getIfPresent(EntityType.class, entityType);
			if (type.isPresent()) {
				BOUNDARY_DATA_MAP.put(type.get(), this);
			}
			this.minX = minX;
			this.minY = minY;
			this.minZ = minZ;
			this.maxX = maxX;
			this.maxY = maxY;
			this.maxZ = maxZ;
		}

		private BoundaryData(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
			this.minX = minX;
			this.minY = minY;
			this.minZ = minZ;
			this.maxX = maxX;
			this.maxY = maxY;
			this.maxZ = maxZ;
		}

		public double getMinX() {
			return minX;
		}

		public double getMinY() {
			return minY;
		}

		public double getMinZ() {
			return minZ;
		}

		public double getMaxX() {
			return maxX;
		}

		public double getMaxY() {
			return maxY;
		}

		public double getMaxZ() {
			return maxZ;
		}

	}

}
