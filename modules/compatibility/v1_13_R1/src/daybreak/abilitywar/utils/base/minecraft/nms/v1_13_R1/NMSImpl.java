package daybreak.abilitywar.utils.base.minecraft.nms.v1_13_R1;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import daybreak.abilitywar.utils.base.minecraft.SkinInfo;
import daybreak.abilitywar.utils.base.minecraft.boundary.EntityBoundingBox;
import daybreak.abilitywar.utils.base.minecraft.nms.*;
import daybreak.abilitywar.utils.base.reflect.ReflectionUtil;
import daybreak.abilitywar.utils.library.MaterialX;
import net.minecraft.server.v1_13_R1.*;
import net.minecraft.server.v1_13_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_13_R1.ItemCooldown.Info;
import net.minecraft.server.v1_13_R1.PacketPlayInClientCommand.EnumClientCommand;
import net.minecraft.server.v1_13_R1.PacketPlayOutEntity.PacketPlayOutEntityLook;
import net.minecraft.server.v1_13_R1.PacketPlayOutTitle.EnumTitleAction;
import net.minecraft.server.v1_13_R1.PacketPlayOutWorldBorder.EnumWorldBorderAction;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_13_R1.CraftServer;
import org.bukkit.craftbukkit.v1_13_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R1.entity.*;
import org.bukkit.craftbukkit.v1_13_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Base64;
import java.util.UUID;

import static daybreak.abilitywar.utils.base.minecraft.item.Skulls.LINK_HEAD;
import static daybreak.abilitywar.utils.base.minecraft.item.Skulls.customSkulls;

public class NMSImpl implements INMS {

	@Override
	public void respawn(Player player) {
		((CraftPlayer) player).getHandle().playerConnection.a(new PacketPlayInClientCommand(EnumClientCommand.PERFORM_RESPAWN));
	}

	@Override
	public void clearTitle(Player player) {
		final PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
		if (connection == null) return;
		connection.sendPacket(new PacketPlayOutTitle(EnumTitleAction.CLEAR, null));
	}

	@Override
	public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
		player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
	}

	@Override
	public void sendActionbar(Player player, String string, int fadeIn, int stay, int fadeOut) {
		final PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
		if (connection == null) return;
		connection.sendPacket(new PacketPlayOutTitle(fadeIn, stay, fadeOut));
		connection.sendPacket(new PacketPlayOutTitle(EnumTitleAction.ACTIONBAR, ChatSerializer.a("{\"text\":\"" + string + "\"}"), fadeIn, stay, fadeOut));
	}

	@Override
	public float getAttackCooldown(Player player) {
		return ((CraftPlayer) player).getHandle().r(0f);
	}

	@Override
	public void rotateHead(Player receiver, Entity entity, float yaw, float pitch) {
		final PlayerConnection connection = ((CraftPlayer) receiver).getHandle().playerConnection;
		if (connection == null) return;
		connection.sendPacket(new PacketPlayOutEntityTeleport(((CraftEntity) entity).getHandle()));
		final byte fixedYaw = (byte) (yaw * (256F / 360F));
		connection.sendPacket(new PacketPlayOutEntityLook(entity.getEntityId(), fixedYaw, (byte) (pitch * (256F / 360F)), entity.isOnGround()));
		connection.sendPacket(new PacketPlayOutEntityHeadRotation(((CraftEntity) entity).getHandle(), fixedYaw));
	}

	@Override
	public IHologram newHologram(World world, double x, double y, double z, String text) {
		return new HologramImpl(world, x, y, z, text);
	}

	@Override
	public IHologram newHologram(World world, double x, double y, double z) {
		return new HologramImpl(world, x, y, z);
	}

	@Override
	public IDummy createDummy(Location location) {
		return new DummyImpl(((CraftServer) Bukkit.getServer()).getServer(), ((CraftWorld) location.getWorld()).getHandle(), location);
	}

	@Override
	public IDummy createDummy(Location location, SkinInfo skinInfo) {
		return new DummyImpl(((CraftServer) Bukkit.getServer()).getServer(), ((CraftWorld) location.getWorld()).getHandle(), location, skinInfo);
	}

	@Override
	public float getAbsorptionHearts(LivingEntity livingEntity) {
		return ((CraftLivingEntity) livingEntity).getHandle().getAbsorptionHearts();
	}

	@Override
	public void setAbsorptionHearts(LivingEntity livingEntity, float absorptionHearts) {
		((CraftLivingEntity) livingEntity).getHandle().setAbsorptionHearts(absorptionHearts);
	}

	@Override
	public void broadcastEntityEffect(Entity entity, byte status) {
		final net.minecraft.server.v1_13_R1.Entity nmsEntity = ((CraftEntity) entity).getHandle();
		nmsEntity.getWorld().broadcastEntityEffect(nmsEntity, status);
	}

	@Override
	public void setLocation(Entity entity, double x, double y, double z, float yaw, float pitch) {
		((CraftEntity) entity).getHandle().setLocation(x, y, z, yaw, pitch);
	}

	@Override
	public void removeBoundingBox(ArmorStand armorStand) {
		final EntityArmorStand nmsArmorStand = ((CraftArmorStand) armorStand).getHandle();
		nmsArmorStand.getDataWatcher().set(EntityArmorStand.a, (byte) (nmsArmorStand.getDataWatcher().get(EntityArmorStand.a) | 16));
		nmsArmorStand.setSize(0F, 0F);
	}

	@Override
	public void setArrowsInBody(Player player, int count) {
		if (count < 0) throw new IllegalStateException("count cannot be negative.");
		((CraftPlayer) player).getHandle().getDataWatcher().set(new DataWatcherObject<>(10, DataWatcherRegistry.b), count);
	}

	@Override
	public void setInvisible(Entity entity, boolean invisible) {
		((CraftEntity) entity).getHandle().setInvisible(invisible);
	}

	@Override
	public boolean isInvisible(Entity entity) {
		return ((CraftEntity) entity).getHandle().isInvisible();
	}

	@Override
	public void setCooldown(Player player, Material material, int ticks) {
		((CraftPlayer) player).getHandle().getCooldownTracker().a(CraftMagicNumbers.getItem(material), ticks);
	}

	@Override
	public boolean hasCooldown(Player player, Material material) {
		return ((CraftPlayer) player).getHandle().getCooldownTracker().a(CraftMagicNumbers.getItem(material));
	}

	@Override
	public int getCooldown(Player player, Material material) {
		final ItemCooldown cooldownTracker = ((CraftPlayer) player).getHandle().getCooldownTracker();
		final Info cooldown = cooldownTracker.cooldowns.get(CraftMagicNumbers.getItem(material));
		return cooldown == null ? 0 : Math.max(0, cooldown.endTick - cooldownTracker.currentTick);
	}

	@Override
	public void fakeCollect(Entity entity, Item item) {
		final PacketPlayOutCollect packet = new PacketPlayOutCollect(item.getEntityId(), entity.getEntityId(), item.getItemStack().getAmount());
		for (final CraftPlayer player : ((CraftServer) Bukkit.getServer()).getOnlinePlayers()) {
			final PlayerConnection connection = player.getHandle().playerConnection;
			if (connection == null) continue;
			connection.sendPacket(packet);
		}
	}

	@Override
	public void clearActiveItem(LivingEntity livingEntity) {
		((CraftLivingEntity) livingEntity).getHandle().clearActiveItem();
	}

	@Override
	public void swingHand(LivingEntity livingEntity, Hand hand) {
		final EntityLiving nmsEntity = ((CraftLivingEntity) livingEntity).getHandle();
		final PacketPlayOutAnimation packet = new PacketPlayOutAnimation(nmsEntity, hand == Hand.MAIN_HAND ? 0 : 3);
		final EntityTrackerEntry entitytrackerentry = ((WorldServer) nmsEntity.getWorld()).getTracker().trackedEntities.get(nmsEntity.getId());
		if (entitytrackerentry != null) {
			entitytrackerentry.broadcastIncludingSelf(packet);
		}
	}

	@Override
	public EntityBoundingBox getBoundingBox(Entity entity) {
		final net.minecraft.server.v1_13_R1.Entity nmsEntity = ((CraftEntity) entity).getHandle();
		final AxisAlignedBB boundingBox = nmsEntity.getBoundingBox();
		final double locX = nmsEntity.locX, locY = nmsEntity.locY, locZ = nmsEntity.locZ;
		return new EntityBoundingBox(entity, boundingBox.a - locX, boundingBox.b - locY, boundingBox.c - locZ, boundingBox.d - locX, boundingBox.e - locY, boundingBox.f - locZ);
	}

	@Override
	public void setCamera(Player receiver, Entity entity) {
		((CraftPlayer) receiver).getHandle().playerConnection.sendPacket(new PacketPlayOutCamera(((CraftEntity) entity).getHandle()));
	}

	@Override
	public IWorldBorder createWorldBorder(World world) {
		return new WorldBorderImpl(world);
	}

	@Override
	public IWorldBorder createWorldBorder(org.bukkit.WorldBorder bukkit) {
		final WorldBorderImpl impl = new WorldBorderImpl(bukkit.getCenter().getWorld());
		final Location center = bukkit.getCenter();
		impl.setCenter(center.getX(), center.getZ());
		impl.setDamageAmount(bukkit.getDamageAmount());
		impl.setDamageBuffer(bukkit.getDamageBuffer());
		impl.setSize(bukkit.getSize());
		impl.setWarningDistance(bukkit.getWarningDistance());
		impl.setWarningTime(bukkit.getWarningTime());
		return impl;
	}

	@Override
	public void setWorldBorder(Player receiver, final IWorldBorder worldBorder) {
		if (!(worldBorder instanceof WorldBorder)) throw new IllegalArgumentException();
		((CraftPlayer) receiver).getHandle().playerConnection.sendPacket(new PacketPlayOutWorldBorder((WorldBorder) worldBorder, EnumWorldBorderAction.INITIALIZE));
	}

	@Override
	public void resetWorldBorder(Player receiver) {
		final EntityPlayer nms = ((CraftPlayer) receiver).getHandle();
		final net.minecraft.server.v1_13_R1.World world = nms.getWorld();
		if (world != null) {
			nms.playerConnection.sendPacket(new PacketPlayOutWorldBorder(world.getWorldBorder(), EnumWorldBorderAction.INITIALIZE));
		}
	}

	@Override
	public void setInGround(Arrow arrow, boolean inGround) {
		((CraftArrow) arrow).getHandle().inGround = inGround;
	}

	@Override
	public boolean isArrow(Entity entity) {
		return entity instanceof Arrow;
	}

	@Override
	public void setPickupStatus(Projectile arrow, PickupStatus pickupStatus) {
		if (!isArrow(arrow)) throw new IllegalArgumentException("arrow must be an instance of Arrow");
		((Arrow) arrow).setPickupStatus(Arrow.PickupStatus.values()[pickupStatus.ordinal()]);
	}

	@Override
	public PickupStatus getPickupStatus(Projectile arrow) {
		if (!isArrow(arrow)) throw new IllegalArgumentException("arrow must be an instance of Arrow");
		return PickupStatus.values()[((Arrow) arrow).getPickupStatus().ordinal()];
	}

	private static final Field JUMPING;

	static {
		try {
			JUMPING = EntityLiving.class.getDeclaredField("bg");
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isJumpingInVehicle(LivingEntity livingEntity) {
		final EntityLiving nms = ((CraftLivingEntity) livingEntity).getHandle();
		try {
			JUMPING.setAccessible(true);
			return (boolean) JUMPING.get(nms);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public SteeringDirection getSteeringDirection(LivingEntity livingEntity) {
		final EntityLiving nms = ((CraftLivingEntity) livingEntity).getHandle();
		return SteeringDirection.get(nms.bh, nms.bj);
	}

	@Override
	public ItemStack createCustomSkull(@NotNull String url) {
		final String key = url.startsWith(LINK_HEAD) ? url.substring(LINK_HEAD.length()) : url;
		final org.bukkit.inventory.ItemStack cachedSkull = customSkulls.getIfPresent(key);
		if (cachedSkull != null) return cachedSkull;
		final ItemStack stack = MaterialX.PLAYER_HEAD.createItem();
		if (url.isEmpty()) return stack;
		if (!url.startsWith(LINK_HEAD)) url = LINK_HEAD + url;
		final SkullMeta meta = (SkullMeta) stack.getItemMeta();
		final GameProfile profile = new GameProfile(UUID.randomUUID(), url);
		profile.getProperties().put("textures", new Property("textures", new String(Base64.getEncoder().encode(("{textures:{SKIN:{url:\"" + url + "\"}}}").getBytes()))));
		try {
			ReflectionUtil.FieldUtil.setValue(meta.getClass(), meta, "profile", profile);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException ignored) {}
		stack.setItemMeta(meta);
		customSkulls.put(key, stack);
		return stack;
	}

}
