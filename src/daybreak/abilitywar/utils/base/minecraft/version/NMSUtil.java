package daybreak.abilitywar.utils.base.minecraft.version;

import daybreak.abilitywar.utils.ReflectionUtil.FieldUtil;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.math.LocationUtil;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NMSUtil {

	private static Method sendPacket = null;
	private static Method a = null;

	public static Object getHandle(Object object) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		return object.getClass().getMethod("getHandle").invoke(object);
	}

	public static void sendPacket(Player player, Object packet) {
		try {
			Object handle = getHandle(player);
			Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
			if (sendPacket == null) {
				sendPacket = playerConnection.getClass().getMethod("sendPacket", NMSUtil.getNMSClass("Packet"));
			}
			sendPacket.invoke(playerConnection, packet);
		} catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException | InvocationTargetException ignored) {
		}
	}

	public static void a(Player player, Object packet) {
		try {
			Object handle = getHandle(player);
			Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
			if (a == null) {
				a = playerConnection.getClass().getMethod("a", packet.getClass());
			}
			a.invoke(playerConnection, packet);
		} catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException | InvocationTargetException ignored) {
		}
	}

	/**
	 * net.minecraft.server
	 */
	public static Class<?> getNMSClass(String className) {
		try {
			return Class.forName("net.minecraft.server." + ServerVersion.getMajorVersion() + "." + className);
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException("An error occurred while finding NMS class.", ex);
		}
	}

	/**
	 * org.bukkit.craftbukkit
	 */
	public static Class<?> getOBCClass(String className) {
		try {
			return Class.forName("org.bukkit.craftbukkit." + ServerVersion.getMajorVersion() + "." + className);
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException("An error occurred while finding OBC class.", ex);
		}
	}

	public static class PlayerUtil {

		private static final Logger logger = Logger.getLogger(PlayerUtil.class.getName());

		private static final Class<?>
				PacketPlayInClientCommand = NMSUtil.getNMSClass("PacketPlayInClientCommand"),
				EnumClientCommand = PacketPlayInClientCommand.getDeclaredClasses()[0];
		private static Constructor<?> newClientCommand;
		private static Object PERFORM_RESPAWN;

		static {
			try {
				newClientCommand = PacketPlayInClientCommand.getConstructor(EnumClientCommand);
				PERFORM_RESPAWN = EnumClientCommand.getField("PERFORM_RESPAWN").get(null);
			} catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException e) {
				logger.log(Level.SEVERE, "리스폰 기능을 초기화하는 도중 오류가 발생하였습니다.");
			}
		}

		public static void respawn(Player player) {
			try {
				a(player, newClientCommand.newInstance(PERFORM_RESPAWN));
			} catch (IllegalAccessException | InstantiationException | InvocationTargetException ex) {
				logger.log(Level.SEVERE, player.getName() + " 플레이어를 리스폰하는 도중 오류가 발생하였습니다.");
			}
		}

		private static final Class<?> PacketPlayOutTitle = NMSUtil.getNMSClass("PacketPlayOutTitle");
		private static Constructor<?> newTitlePacket = null;
		private static Object TIMES = null;
		private static Object ACTIONBAR = null;

		static {
			try {
				newTitlePacket = PacketPlayOutTitle.getConstructor(
						PacketPlayOutTitle.getDeclaredClasses()[0],
						IChatBaseComponent.IChatBaseComponent, int.class, int.class, int.class);
				TIMES = PacketPlayOutTitle.getDeclaredClasses()[0].getField("TIMES").get(null);
				ACTIONBAR = PacketPlayOutTitle.getDeclaredClasses()[0].getField("ACTIONBAR").get(null);
			} catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException e) {
				logger.log(Level.SEVERE, "액션바 메시지 전송 기능을 초기화하는 도중 오류가 발생하였습니다.");
			}
		}

		public static void sendActionbar(Player player, String message, int fadeIn, int stay, int fadeOut) {
			try {
				Object actionbar = IChatBaseComponent.of(message);
				sendPacket(player, newTitlePacket.newInstance(TIMES, actionbar, fadeIn, stay, fadeOut));
				sendPacket(player, newTitlePacket.newInstance(ACTIONBAR, actionbar, fadeIn, stay, fadeOut));
			} catch (IllegalAccessException | InvocationTargetException | InstantiationException | NullPointerException e) {
				logger.log(Level.SEVERE, "액션바 메시지를 보내는 도중 " + e.getClass().getSimpleName() + " 오류가 발생하였습니다.");
			}
		}

		private static Class<?> entityHumanClass = getNMSClass("EntityHuman");
		private static Method attackCooldownMethod;

		static {
			try {
				switch (ServerVersion.getVersion()) {
					case 12:
						attackCooldownMethod = entityHumanClass.getDeclaredMethod("n", float.class);
						break;
					case 13:
						attackCooldownMethod = entityHumanClass.getDeclaredMethod("r", float.class);
						break;
					case 14:
					case 15:
						attackCooldownMethod = entityHumanClass.getDeclaredMethod("s", float.class);
						break;
				}
			} catch (NoSuchMethodException e) {
				logger.log(Level.SEVERE, "공격 쿨타임 확인 기능을 준비하는 도중 " + e.getClass().getSimpleName() + " 오류가 발생하였습니다.");
			}
		}

		public static float getAttackCooldown(Player player) {
			try {
				return (float) attackCooldownMethod.invoke(getHandle(player), 0f);
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				return -1;
			}
		}

	}

	public static class EntityUtil {

		private static final Logger logger = Logger.getLogger(EntityUtil.class.getName());

		private static final Class<?>
				EntityMetadataClass = getNMSClass("PacketPlayOutEntityMetadata"),
				DataWatcher = getNMSClass("DataWatcher"),
				entityClass = getNMSClass("Entity");
		private static Constructor<?> newEntityMetadata;

		private static Method getId, getDataWatcher;

		static {
			try {
				newEntityMetadata = EntityMetadataClass.getConstructor(int.class, DataWatcher, boolean.class);
				getId = entityClass.getMethod("getId");
				getDataWatcher = entityClass.getMethod("getDataWatcher");
			} catch (NoSuchMethodException e) {
				logger.log(Level.SEVERE, "엔티티 유틸을 초기화하는 도중 오류가 발생하였습니다.");
			}
		}

		public static int getId(Entity entity) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
			return (int) getId.invoke(getHandle(entity));
		}

		public static Object getDataWatcher(Entity entity) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
			return getDataWatcher.invoke(getHandle(entity));
		}

		private static final Class<?>
				EntityLookClass = getNMSClass("PacketPlayOutEntity$PacketPlayOutEntityLook"),
				EntityHeadRotationClass = getNMSClass("PacketPlayOutEntityHeadRotation");
		private static Constructor<?> newEntityLook, newEntityHeadRotation, newEntityTeleport;

		static {
			try {
				newEntityLook = EntityLookClass.getConstructor(int.class, byte.class, byte.class, boolean.class);
				newEntityHeadRotation = EntityHeadRotationClass.getConstructor(entityClass, byte.class);
				newEntityTeleport = getNMSClass("PacketPlayOutEntityTeleport").getConstructor(getNMSClass("Entity"));
			} catch (NoSuchMethodException e) {
				logger.log(Level.SEVERE, "엔티티 방향 기능을 초기화하는 도중 오류가 발생하였습니다.");
			}
		}

		public static void rotateHead(Player receiver, Entity entity, float yaw, float pitch) {
			try {
				int entityId = getId(entity);
				Object handle = getHandle(entity);
				byte fixedYaw = getFixedRotation(yaw);
				sendPacket(receiver, newEntityTeleport.newInstance(handle));
				sendPacket(receiver, newEntityLook.newInstance(entityId, fixedYaw, getFixedRotation(pitch), entity.isOnGround()));
				sendPacket(receiver, newEntityHeadRotation.newInstance(handle, fixedYaw));
			} catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
				logger.log(Level.SEVERE, "엔티티 방향 기능을 사용하는 도중 " + e.getClass().getSimpleName() + " 오류가 발생하였습니다.");
			}
		}

		public static void rotateHead(Player receiver, Entity entity, Vector direction) {
			rotateHead(receiver, entity, LocationUtil.getYaw(direction), LocationUtil.getPitch(direction));
		}

		private static byte getFixedRotation(float f) {
			return (byte) (f * (256F / 360F));
		}

		@Beta
		public static void setGlowing(Player receiver, Entity entity, boolean glowing) {
			try {
				Object dataWatcher = getDataWatcher(entity);
				Map<Object, Object> map = FieldUtil.getValue(dataWatcher, "d");

				Object item = map.get(0);
				byte initialBitMask = FieldUtil.getValue(item, "b");
				byte bitMaskIndex = 6;
				if (glowing) FieldUtil.setValue(item, "b", (byte) (initialBitMask | 1 << bitMaskIndex));
				else FieldUtil.setValue(item, "b", (byte) (initialBitMask & ~(1 << bitMaskIndex)));

				sendPacket(receiver, newEntityMetadata.newInstance(getId(entity), dataWatcher, true));
			} catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | NoSuchFieldException | InstantiationException e) {
				e.printStackTrace();
			}
		}

	}

	public static class IChatBaseComponent {

		private static final Logger logger = Logger.getLogger(IChatBaseComponent.class.getName());
		public static final Class<?> IChatBaseComponent = getNMSClass("IChatBaseComponent");
		private static Method newIChatBaseComponent = null;

		static {
			try {
				newIChatBaseComponent = IChatBaseComponent.getDeclaredClasses()[0].getMethod("a", String.class);
			} catch (NoSuchMethodException e) {
				logger.log(Level.SEVERE, "IChatBaseComponent를 초기화하는 도중 오류가 발생하였습니다.");
			}
		}

		public static Object of(String string) throws InvocationTargetException, IllegalAccessException {
			return newIChatBaseComponent.invoke(null, "{\"text\": \"" + string + "\"}");
		}

	}

	public static class Hologram {

		private static final Class<?> CraftWorld = getOBCClass("CraftWorld"),
				World = getNMSClass("World"),
				EntityArmorStand = getNMSClass("EntityArmorStand"),
				PacketPlayOutSpawnEntityLiving = getNMSClass("PacketPlayOutSpawnEntityLiving"),
				PacketPlayOutEntityDestroy = getNMSClass("PacketPlayOutEntityDestroy"),
				PacketPlayOutEntityMetadata = getNMSClass("PacketPlayOutEntityMetadata"),
				PacketPlayOutEntityTeleport = getNMSClass("PacketPlayOutEntityTeleport"),
				Entity = getNMSClass("Entity"),
				DataWatcher = getNMSClass("DataWatcher"),
				EntityLiving = getNMSClass("EntityLiving");
		private static Constructor<?> EntityArmorStandConstructor = null,
				PacketPlayOutSpawnEntityLivingConstructor = null,
				PacketPlayOutEntityDestroyConstructor = null,
				PacketPlayOutEntityMetadataConstructor = null,
				PacketPlayOutEntityTeleportConstructor = null;
		private static Method setInvisible = null, setCustomNameVisible = null,
				setCustomName = null, getId = null, getDataWatcher = null,
				setLocation = null;

		static {
			try {
				EntityArmorStandConstructor = EntityArmorStand.getConstructor(World, double.class, double.class, double.class);
				PacketPlayOutSpawnEntityLivingConstructor = PacketPlayOutSpawnEntityLiving.getConstructor(EntityLiving);
				PacketPlayOutEntityDestroyConstructor = PacketPlayOutEntityDestroy.getConstructor(int[].class);
				PacketPlayOutEntityMetadataConstructor = PacketPlayOutEntityMetadata.getConstructor(int.class, DataWatcher, boolean.class);
				PacketPlayOutEntityTeleportConstructor = PacketPlayOutEntityTeleport.getConstructor(Entity);
				setInvisible = EntityArmorStand.getMethod("setInvisible", boolean.class);
				setCustomNameVisible = EntityArmorStand.getMethod("setCustomNameVisible", boolean.class);
				setLocation = Entity.getMethod("setLocation", double.class, double.class, double.class, float.class, float.class);
				try {
					setCustomName = EntityArmorStand.getMethod("setCustomName", String.class);
				} catch (NoSuchMethodException x) {
					setCustomName = EntityArmorStand.getMethod("setCustomName", IChatBaseComponent.IChatBaseComponent);
				}
				getId = EntityArmorStand.getMethod("getId");
				getDataWatcher = Entity.getMethod("getDataWatcher");
			} catch (NoSuchMethodException ignored) {
			}
		}

		private Location location;
		private String text;
		private final Object armorStand;
		private final int id;
		private final Object packetPlayOutSpawnEntityLiving;
		private final Object packetPlayOutEntityDestroy;
		private final Set<Player> viewers = new HashSet<>();

		public Hologram(Location location, String text) {
			this.location = location;
			this.text = text;
			try {
				this.armorStand = EntityArmorStandConstructor.newInstance(getHandle(CraftWorld.cast(location.getWorld())), location.getX(), location.getY(), location.getZ());
				setInvisible.invoke(armorStand, true);
				setCustomNameVisible.invoke(armorStand, true);
				if (setCustomName.getParameterTypes()[0].equals(String.class)) {
					setCustomName.invoke(armorStand, text);
				} else {
					setCustomName.invoke(armorStand, IChatBaseComponent.of(text));
				}
				this.id = (int) getId.invoke(armorStand);
				this.packetPlayOutSpawnEntityLiving = PacketPlayOutSpawnEntityLivingConstructor.newInstance(armorStand);
				this.packetPlayOutEntityDestroy = PacketPlayOutEntityDestroyConstructor.newInstance((Object) new int[]{id});
			} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
				throw new RuntimeException("An error occurred while creating the hologram.", e);
			}
		}

		public void display(Player... players) {
			try {
				for (Player player : players) {
					if (viewers.add(player)) {
						sendPacket(player, packetPlayOutSpawnEntityLiving);
						updateMetadata(player);
					}
				}
			} catch (InvocationTargetException | IllegalAccessException | InstantiationException ignored) {
			}
		}

		public void hide(Player... players) {
			for (Player player : players) {
				if (viewers.remove(player)) {
					sendPacket(player, packetPlayOutEntityDestroy);
				}
			}
		}

		public void setLocation(Location location) {
			try {
				setLocation.invoke(armorStand, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
				this.location = location;
				updateLocation();
			} catch (InvocationTargetException | IllegalAccessException | InstantiationException ignored) {
			}
		}

		public void setText(String text) {
			try {
				if (setCustomName.getParameterTypes()[0].equals(String.class)) {
					setCustomName.invoke(armorStand, text);
				} else {
					setCustomName.invoke(armorStand, IChatBaseComponent.of(text));
				}
				this.text = text;
				updateMetadata();
			} catch (InvocationTargetException | IllegalAccessException | InstantiationException ignored) {
			}
		}

		public Location getLocation() {
			return location;
		}

		public String getText() {
			return text;
		}

		private void updateMetadata() throws IllegalAccessException, InvocationTargetException, InstantiationException {
			Object packet = PacketPlayOutEntityMetadataConstructor.newInstance(id, getDataWatcher.invoke(armorStand), true);
			for (Player player : viewers) {
				sendPacket(player, packet);
			}
		}

		private void updateMetadata(Player player) throws IllegalAccessException, InvocationTargetException, InstantiationException {
			sendPacket(player, PacketPlayOutEntityMetadataConstructor.newInstance(id, getDataWatcher.invoke(armorStand), true));
		}

		private void updateLocation() throws IllegalAccessException, InvocationTargetException, InstantiationException {
			for (Player player : viewers) {
				sendPacket(player, PacketPlayOutEntityTeleportConstructor.newInstance(armorStand));
			}
		}

	}

}
