package daybreak.abilitywar.utils.base.minecraft.nms.v1_19_R2;

import com.mojang.datafixers.util.Pair;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketListenerPlayOut;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutEntityEquipment;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import java.util.*;
import java.util.function.Supplier;

/** Per-viewer packets: never mutate a packet shared by the server's other viewers. */
public final class InvisiblePlayer {
	private final Supplier<Player> ownerSupplier;
	private Player owner;
	private final Map<UUID, Channel> channels = new HashMap<>();
	private final String handlerName;
	private volatile boolean hiding;
	private boolean previouslyInvisible;
	private static final List<Pair<EnumItemSlot, ItemStack>> EMPTY = Arrays.asList(
			Pair.of(EnumItemSlot.MAINHAND, ItemStack.EMPTY), Pair.of(EnumItemSlot.OFFHAND, ItemStack.EMPTY),
			Pair.of(EnumItemSlot.FEET, ItemStack.EMPTY), Pair.of(EnumItemSlot.LEGS, ItemStack.EMPTY),
			Pair.of(EnumItemSlot.CHEST, ItemStack.EMPTY), Pair.of(EnumItemSlot.HEAD, ItemStack.EMPTY));

	public InvisiblePlayer(Player owner) {
		this(() -> owner);
	}
	public InvisiblePlayer(Supplier<Player> ownerSupplier) {
		this.ownerSupplier = Objects.requireNonNull(ownerSupplier, "ownerSupplier");
		this.owner = Objects.requireNonNull(ownerSupplier.get(), "owner");
		this.handlerName = "abilitywar-invisible-" + UUID.randomUUID();
	}
	private void refreshOwner() {
		final Player currentOwner = Objects.requireNonNull(ownerSupplier.get(), "owner");
		if (currentOwner == owner) return;
		// A reconnect replaces CraftPlayer and its entity ID. Rebuild every viewer's filter.
		removeAllHandlers();
		if (hiding) owner.setInvisible(previouslyInvisible);
		owner = currentOwner;
		if (hiding) {
			// The reconnected player may have saved our invisibility flag while disconnecting.
			// Keep the original state so show() can still restore it correctly.
			owner.setArrowsInBody(0);
			owner.setInvisible(true);
			for (Player viewer : Bukkit.getOnlinePlayers()) add(viewer);
		}
	}
	public void hide() {
		refreshOwner();
		if (!hiding) previouslyInvisible = owner.isInvisible();
		hiding = true;
		owner.setArrowsInBody(0);
		owner.setInvisible(true);
		for (Player viewer : Bukkit.getOnlinePlayers()) add(viewer);
	}
	public void add(Player viewer) {
		refreshOwner();
		if (!hiding || !viewer.isOnline() || channels.containsKey(viewer.getUniqueId())) return;
		final CraftPlayer craft = (CraftPlayer) viewer;
		final Channel channel = ChannelUtil.getChannel(craft.getHandle());
		final int entityId = owner.getEntityId();
		final boolean self = viewer.getUniqueId().equals(owner.getUniqueId());
		channels.put(viewer.getUniqueId(), channel);
		channel.eventLoop().execute(() -> {
			if (!hiding || !channel.isOpen()) return;
			channel.pipeline().addBefore("packet_handler", handlerName, new ChannelOutboundHandlerAdapter() {
				@Override public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
					if (hiding) packet = hidePacket(packet, entityId, self);
					super.write(ctx, packet, promise);
				}
			});
			if (!self) craft.getHandle().connection.send(new PacketPlayOutEntityEquipment(entityId, EMPTY));
		});
	}
	@SuppressWarnings("unchecked")
	static Object hidePacket(Object packet, int entityId, boolean self) {
		if (packet instanceof PacketPlayOutEntityEquipment && ((PacketPlayOutEntityEquipment) packet).getEntity() == entityId && !self) {
			packet = new PacketPlayOutEntityEquipment(entityId, EMPTY);
		} else if (packet instanceof PacketPlayOutEntityMetadata && ((PacketPlayOutEntityMetadata) packet).id() == entityId) {
			final PacketPlayOutEntityMetadata metadata = (PacketPlayOutEntityMetadata) packet;
			final List<DataWatcher.b<?>> values = new ArrayList<>(metadata.packedItems());
			for (int i = 0; i < values.size(); i++) {
				final DataWatcher.b<?> value = values.get(i);
				// Entity shared flags are protocol metadata index 0.
				if (value.id() == 0 && value.serializer() == DataWatcherRegistry.BYTE) {
					values.set(i, new DataWatcher.b<>(0, DataWatcherRegistry.BYTE, (byte) ((Byte) value.value() | 0x20)));
				}
			}
			packet = new PacketPlayOutEntityMetadata(entityId, values);
		}
		return packet;
	}

	public void remove(Player viewer) {
		final Channel channel = channels.remove(viewer.getUniqueId());
		if (channel != null) channel.eventLoop().execute(() -> {
			if (channel.pipeline().get(handlerName) != null) channel.pipeline().remove(handlerName);
		});
	}
	private void removeAllHandlers() {
		for (Channel channel : channels.values()) channel.eventLoop().execute(() -> {
			if (channel.pipeline().get(handlerName) != null) channel.pipeline().remove(handlerName);
		});
		channels.clear();
	}
	public void show() {
		refreshOwner();
		if (!hiding) return;
		hiding = false;
		final net.minecraft.server.level.EntityPlayer handle = ((CraftPlayer) owner).getHandle();
		final List<Pair<EnumItemSlot, ItemStack>> equipment = new ArrayList<>();
		for (Pair<EnumItemSlot, ItemStack> entry : EMPTY) equipment.add(Pair.of(entry.getFirst(), ((net.minecraft.world.entity.EntityLiving) handle).getItemBySlot(entry.getFirst()).copy()));
		for (Player viewer : Bukkit.getOnlinePlayers()) {
			remove(viewer);
			((CraftPlayer) viewer).getHandle().connection.send(new PacketPlayOutEntityEquipment(owner.getEntityId(), equipment));
		}
		removeAllHandlers();
		owner.setInvisible(previouslyInvisible);
	}
}
