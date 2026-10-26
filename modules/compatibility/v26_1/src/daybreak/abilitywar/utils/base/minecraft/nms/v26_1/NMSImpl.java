package daybreak.abilitywar.utils.base.minecraft.nms.v26_1;

import daybreak.abilitywar.utils.base.minecraft.SkinInfo;
import daybreak.abilitywar.utils.base.minecraft.boundary.EntityBoundingBox;
import daybreak.abilitywar.utils.base.minecraft.nms.*;
import daybreak.abilitywar.utils.library.MaterialX;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket.Action;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket.Rot;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import net.minecraft.world.InteractionHand;

import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import static daybreak.abilitywar.utils.base.minecraft.item.Skulls.LINK_HEAD;
import static daybreak.abilitywar.utils.base.minecraft.item.Skulls.customSkulls;

public class NMSImpl implements INMS {
	private static final Method SET_IN_GROUND;
	static {
		try {
			SET_IN_GROUND = net.minecraft.world.entity.projectile.arrow.AbstractArrow.class.getDeclaredMethod("setInGround", boolean.class);
			SET_IN_GROUND.setAccessible(true);
		} catch (ReflectiveOperationException e) { throw new ExceptionInInitializerError(e); }
	}

	@Override
	public void respawn(Player player) {
		((CraftPlayer) player).getHandle().connection.handleClientCommand(new ServerboundClientCommandPacket(Action.PERFORM_RESPAWN));
	}

	@Override
	public void clearTitle(Player player) {
		final ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
		if (connection == null) return;
		connection.send(new ClientboundClearTitlesPacket(false));
	}

	@Override
	public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
		player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
	}

	@Override
	public void sendActionbar(Player player, String string, int fadeIn, int stay, int fadeOut) {
		final ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
		if (connection == null) return;
		connection.send(new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut));
		connection.send(new ClientboundSetActionBarTextPacket(Component.literal(string)));
	}

	@Override
	public float getAttackCooldown(Player player) {
		return ((CraftHumanEntity) player).getHandle().getAttackStrengthScale(0f);
	}

	@Override
	public void rotateHead(Player receiver, Entity entity, float yaw, float pitch) {
		final ServerGamePacketListenerImpl connection = ((CraftPlayer) receiver).getHandle().connection;
		if (connection == null) return;
		connection.send(Packets.teleport(((CraftEntity) entity).getHandle()));
		final byte fixedYaw = (byte) (yaw * (256F / 360F));
		connection.send(new Rot(entity.getEntityId(), fixedYaw, (byte) (pitch * (256F / 360F)), entity.isOnGround()));
		connection.send(new ClientboundRotateHeadPacket(((CraftEntity) entity).getHandle(), fixedYaw));
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
		if (!Float.isFinite(absorptionHearts) || absorptionHearts < 0) throw new IllegalArgumentException("absorptionHearts must be finite and non-negative");
		final net.minecraft.world.entity.LivingEntity handle = ((CraftLivingEntity) livingEntity).getHandle();
		final net.minecraft.world.entity.ai.attributes.AttributeInstance maximum = handle.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_ABSORPTION);
		final net.minecraft.resources.Identifier key = net.minecraft.resources.Identifier.fromNamespaceAndPath("abilitywar", "absorption");
		maximum.removeModifier(key);
		if (absorptionHearts > maximum.getValue()) {
			maximum.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(key,
					absorptionHearts - maximum.getValue(), net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
		}
		handle.setAbsorptionAmount(absorptionHearts);
	}

	@Override
	public void broadcastEntityEffect(Entity entity, byte status) {
		final net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
		nmsEntity.level().broadcastEntityEvent(nmsEntity, status);
	}

	@Override
	public void setLocation(Entity entity, double x, double y, double z, float yaw, float pitch) {
		((CraftEntity) entity).getHandle().snapTo(x, y, z, yaw, pitch);
	}

	@Override
	public void removeBoundingBox(ArmorStand armorStand) {
		((CraftArmorStand) armorStand).getHandle().setMarker(true);
	}

	@Override
	public void setArrowsInBody(Player player, int count) {
		if (count < 0) throw new IllegalStateException("count cannot be negative.");
		player.setArrowsInBody(count);
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
		player.setCooldown(material, ticks);
	}

	@Override
	public boolean hasCooldown(Player player, Material material) {
		return player.hasCooldown(material);
	}

	@Override
	public int getCooldown(Player player, Material material) {
		return player.getCooldown(material);
	}

	@Override
	public void fakeCollect(Entity entity, Item item) {
		final ClientboundTakeItemEntityPacket packet = new ClientboundTakeItemEntityPacket(item.getEntityId(), entity.getEntityId(), item.getItemStack().getAmount());
		for (final CraftPlayer player : ((CraftServer) Bukkit.getServer()).getOnlinePlayers()) {
			final ServerGamePacketListenerImpl connection = player.getHandle().connection;
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
		((CraftLivingEntity) livingEntity).getHandle().swing(hand == Hand.MAIN_HAND ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, true);
	}

	@Override
	public EntityBoundingBox getBoundingBox(Entity entity) {
		final net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
		final AABB boundingBox = nmsEntity.getBoundingBox();
		final double locX = nmsEntity.getX(), locY = nmsEntity.getY(), locZ = nmsEntity.getZ();
		return new EntityBoundingBox(entity, boundingBox.minX - locX, boundingBox.minY - locY, boundingBox.minZ - locZ, boundingBox.maxX - locX, boundingBox.maxY - locY, boundingBox.maxZ - locZ);
	}

	@Override
	public void setCamera(Player receiver, Entity entity) {
		((CraftPlayer) receiver).getHandle().connection.send(new ClientboundSetCameraPacket(((CraftEntity) entity).getHandle()));
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
		if (!(worldBorder instanceof WorldBorderImpl)) throw new IllegalArgumentException("worldBorder must be a v26_1 border");
		receiver.setWorldBorder(((WorldBorderImpl) worldBorder).border);
	}

	@Override
	public void resetWorldBorder(Player receiver) {
		receiver.setWorldBorder(null);
	}

	@Override
	public void setInGround(Arrow arrow, boolean inGround) {
		try {
			SET_IN_GROUND.invoke(((CraftArrow) arrow).getHandle(), inGround);
		} catch (ReflectiveOperationException e) { throw new IllegalStateException(e); }
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

	@Override
	public boolean isJumpingInVehicle(LivingEntity livingEntity) {
		if (livingEntity instanceof Player) {
			return ((CraftPlayer) livingEntity).getHandle().getLastClientInput().jump();
		}
		return ((CraftLivingEntity) livingEntity).getHandle().isJumping();
	}

	@Override
	public SteeringDirection getSteeringDirection(LivingEntity livingEntity) {
		if (livingEntity instanceof Player) {
			final net.minecraft.world.entity.player.Input input = ((CraftPlayer) livingEntity).getHandle().getLastClientInput();
			return SteeringDirection.get((input.left() ? 1 : 0) - (input.right() ? 1 : 0),
					(input.forward() ? 1 : 0) - (input.backward() ? 1 : 0));
		}
		final net.minecraft.world.entity.LivingEntity nms = ((CraftLivingEntity) livingEntity).getHandle();
		return SteeringDirection.get(nms.xxa, nms.zza);
	}

	@Override
	public SkullMeta setOwningPlayer(SkullMeta skullMeta, String name) {
		skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(name));
		return skullMeta;
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
		final PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
		final PlayerTextures textures = profile.getTextures();
		try {
			textures.setSkin(new URL(url));
		} catch (MalformedURLException ignored) {}
		profile.setTextures(textures);
		meta.setOwnerProfile(profile);
		stack.setItemMeta(meta);
		customSkulls.put(key, stack);
		return stack;
	}

}
