package abilitywar.utils.base.minecraft.nms.v1_20_R1;

import daybreak.abilitywar.utils.base.minecraft.SkinInfo;
import daybreak.abilitywar.utils.base.minecraft.boundary.EntityBoundingBox;
import daybreak.abilitywar.utils.base.minecraft.nms.Hand;
import daybreak.abilitywar.utils.base.minecraft.nms.IDummy;
import daybreak.abilitywar.utils.base.minecraft.nms.IHologram;
import daybreak.abilitywar.utils.base.minecraft.nms.INMS;
import daybreak.abilitywar.utils.base.minecraft.nms.IWorldBorder;
import daybreak.abilitywar.utils.base.minecraft.nms.PickupStatus;
import daybreak.abilitywar.utils.base.minecraft.nms.SteeringDirection;
import net.minecraft.network.chat.IChatBaseComponent.ChatSerializer;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.PacketPlayInClientCommand;
import net.minecraft.network.protocol.game.PacketPlayInClientCommand.EnumClientCommand;
import net.minecraft.network.protocol.game.PacketPlayOutCamera;
import net.minecraft.network.protocol.game.PacketPlayOutCollect;
import net.minecraft.network.protocol.game.PacketPlayOutEntity.PacketPlayOutEntityLook;
import net.minecraft.network.protocol.game.PacketPlayOutEntityHeadRotation;
import net.minecraft.network.protocol.game.PacketPlayOutEntityTeleport;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.EnumHand;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.item.ItemCooldown;
import net.minecraft.world.item.ItemCooldown.Info;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AxisAlignedBB;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftArrow;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftMagicNumbers;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

import java.lang.reflect.Field;

public class NMSImpl implements INMS {

	@Override
	public void respawn(Player player) {
		((CraftPlayer) player).getHandle().connection.handleClientCommand(new PacketPlayInClientCommand(EnumClientCommand.PERFORM_RESPAWN));
	}

	@Override
	public void clearTitle(Player player) {
		final PlayerConnection connection = ((CraftPlayer) player).getHandle().connection;
		if (connection == null) return;
		connection.send(new ClientboundClearTitlesPacket(false));
	}

	@Override
	public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
		player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
	}

	@Override
	public void sendActionbar(Player player, String string, int fadeIn, int stay, int fadeOut) {
		final PlayerConnection connection = ((CraftPlayer) player).getHandle().connection;
		if (connection == null) return;
		connection.send(new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut));
		connection.send(new ClientboundSetActionBarTextPacket(ChatSerializer.fromJson("{\"text\":\"" + string + "\"}")));
	}

	@Override
	public float getAttackCooldown(Player player) {
		return ((CraftHumanEntity) player).getHandle().getAttackStrengthScale(0f);
	}

	@Override
	public void rotateHead(Player receiver, Entity entity, float yaw, float pitch) {
		final PlayerConnection connection = ((CraftPlayer) receiver).getHandle().connection;
		if (connection == null) return;
		connection.send(new PacketPlayOutEntityTeleport(((CraftEntity) entity).getHandle()));
		final byte fixedYaw = (byte) (yaw * (256F / 360F));
		connection.send(new PacketPlayOutEntityLook(entity.getEntityId(), fixedYaw, (byte) (pitch * (256F / 360F)), entity.isOnGround()));
		connection.send(new PacketPlayOutEntityHeadRotation(((CraftEntity) entity).getHandle(), fixedYaw));
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
		return ((CraftLivingEntity) livingEntity).getHandle().getAbsorptionAmount();
	}

	@Override
	public void setAbsorptionHearts(LivingEntity livingEntity, float absorptionHearts) {
		((CraftLivingEntity) livingEntity).getHandle().setAbsorptionAmount(absorptionHearts);
	}

	@Override
	public void broadcastEntityEffect(Entity entity, byte status) {
		final net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
		nmsEntity.getLevel().broadcastEntityEvent(nmsEntity, status);
	}

	@Override
	public void setLocation(Entity entity, double x, double y, double z, float yaw, float pitch) {
		((CraftEntity) entity).getHandle().absMoveTo(x, y, z, yaw, pitch);
	}

	@Override
	public void removeBoundingBox(ArmorStand armorStand) {
		((CraftArmorStand) armorStand).getHandle().setMarker(true);
	}

	@Override
	public void setArrowsInBody(Player player, int count) {
		if (count < 0) throw new IllegalStateException("count cannot be negative.");
		((net.minecraft.world.entity.Entity) ((CraftPlayer) player).getHandle()).getEntityData().set(new DataWatcherObject<>(12, DataWatcherRegistry.INT), count);
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
		((CraftHumanEntity) player).getHandle().getCooldowns().addCooldown(CraftMagicNumbers.getItem(material), ticks);
	}

	@Override
	public boolean hasCooldown(Player player, Material material) {
		return ((CraftHumanEntity) player).getHandle().getCooldowns().isOnCooldown(CraftMagicNumbers.getItem(material));
	}

	@Override
	public int getCooldown(Player player, Material material) {
		final ItemCooldown cooldownTracker = ((CraftHumanEntity) player).getHandle().getCooldowns();
		final Info cooldown = cooldownTracker.cooldowns.get(CraftMagicNumbers.getItem(material));
		return cooldown == null ? 0 : Math.max(0, cooldown.endTime - cooldownTracker.tickCount);
	}

	@Override
	public void fakeCollect(Entity entity, Item item) {
		final PacketPlayOutCollect packet = new PacketPlayOutCollect(item.getEntityId(), entity.getEntityId(), item.getItemStack().getAmount());
		for (final CraftPlayer player : ((CraftServer) Bukkit.getServer()).getOnlinePlayers()) {
			final PlayerConnection connection = player.getHandle().connection;
			if (connection == null) continue;
			connection.send(packet);
		}
	}

	@Override
	public void clearActiveItem(LivingEntity livingEntity) {
		((CraftLivingEntity) livingEntity).getHandle().stopUsingItem();
	}

	@Override
	public void swingHand(LivingEntity livingEntity, Hand hand) {
		((CraftLivingEntity) livingEntity).getHandle().swing(hand == Hand.MAIN_HAND ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND, true);
	}

	@Override
	public EntityBoundingBox getBoundingBox(Entity entity) {
		final net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
		final AxisAlignedBB boundingBox = nmsEntity.getBoundingBox();
		final double locX = nmsEntity.getX(), locY = nmsEntity.getY(), locZ = nmsEntity.getZ();
		return new EntityBoundingBox(entity, boundingBox.minX - locX, boundingBox.minY - locY, boundingBox.minZ - locZ, boundingBox.maxX - locX, boundingBox.maxY - locY, boundingBox.maxZ - locZ);
	}

	@Override
	public void setCamera(Player receiver, Entity entity) {
		((CraftPlayer) receiver).getHandle().connection.send(new PacketPlayOutCamera(((CraftEntity) entity).getHandle()));
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
		((CraftPlayer) receiver).getHandle().connection.send(new ClientboundInitializeBorderPacket((WorldBorder) worldBorder));
	}

	@Override
	public void resetWorldBorder(Player receiver) {
		final EntityPlayer nms = ((CraftPlayer) receiver).getHandle();
		final net.minecraft.world.level.World world = nms.getLevel();
		if (world != null) {
			nms.connection.send(new ClientboundInitializeBorderPacket(world.getWorldBorder()));
		}
	}

	@Override
	public void setInGround(Arrow arrow, boolean inGround) {
		((CraftArrow) arrow).getHandle().inGround = inGround;
	}

	@Override
	public boolean isArrow(Entity entity) {
		return entity instanceof AbstractArrow;
	}

	@Override
	public void setPickupStatus(Projectile arrow, PickupStatus pickupStatus) {
		if (!isArrow(arrow)) throw new IllegalArgumentException("arrow must be an instance of AbstractArrow");
		((AbstractArrow) arrow).setPickupStatus(AbstractArrow.PickupStatus.values()[pickupStatus.ordinal()]);
	}

	@Override
	public PickupStatus getPickupStatus(Projectile arrow) {
		if (!isArrow(arrow)) throw new IllegalArgumentException("arrow must be an instance of AbstractArrow");
		return PickupStatus.values()[((AbstractArrow) arrow).getPickupStatus().ordinal()];
	}

	private static final Field JUMPING;

	static {
		try {
			JUMPING = EntityLiving.class.getDeclaredField("bn");
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
		return SteeringDirection.get(nms.xxa, nms.zza);
	}
}
