package daybreak.abilitywar.ability.list.hermit.v1_19_R1;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.list.hermit.AbstractHermit;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.collect.Pair;
import daybreak.abilitywar.utils.base.reflect.ReflectionUtil.FieldUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.protocol.game.PacketPlayOutEntityEquipment;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.syncher.DataWatcher.Item;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.UUID;

public class Hermit extends AbstractHermit {

	private static final DataWatcherObject<Byte> BYTE_DATA_WATCHER_OBJECT;
	private static final List<com.mojang.datafixers.util.Pair<EnumItemSlot, ItemStack>> NULL_PAIR_LIST = Arrays.asList(
			com.mojang.datafixers.util.Pair.of(EnumItemSlot.MAINHAND, ItemStack.EMPTY),
			com.mojang.datafixers.util.Pair.of(EnumItemSlot.OFFHAND, ItemStack.EMPTY),
			com.mojang.datafixers.util.Pair.of(EnumItemSlot.FEET, ItemStack.EMPTY),
			com.mojang.datafixers.util.Pair.of(EnumItemSlot.LEGS, ItemStack.EMPTY),
			com.mojang.datafixers.util.Pair.of(EnumItemSlot.CHEST, ItemStack.EMPTY),
			com.mojang.datafixers.util.Pair.of(EnumItemSlot.HEAD, ItemStack.EMPTY)
	);

	static {
		try {
			BYTE_DATA_WATCHER_OBJECT = FieldUtil.getStaticValue(Entity.class, "Z");
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private final Map<UUID, Pair<CraftPlayer, ChannelOutboundHandlerAdapter>> channelHandlers = new HashMap<>();

	public Hermit(Participant participant) {
		super(participant);
	}

	@SubscribeEvent
	private void onJoin(PlayerJoinEvent e) {
		if (!isHiding()) return;
		final CraftPlayer player = (CraftPlayer) e.getPlayer();
		new BukkitRunnable() {
			@Override
			public void run() {
				if (player.equals(getPlayer())) {
					injectSelf();
				} else {
					player.getHandle().connection.send(new PacketPlayOutEntityEquipment(getPlayer().getEntityId(), NULL_PAIR_LIST));
					injectPlayer(player);
				}
			}
		}.runTaskLater(AbilityWar.getPlugin(), 2L);
	}

	@SubscribeEvent
	private void onQuit(PlayerQuitEvent e) {
		final CraftPlayer player = (CraftPlayer) e.getPlayer();
		if (channelHandlers.containsKey(player.getUniqueId())) {
			try {
				player.getHandle().connection.connection.channel.pipeline().remove(channelHandlers.remove(player.getUniqueId()).getRight());
			} catch (NoSuchElementException ignored) {
			}
		}
	}

	@Override
	protected void hide0() {
		final CraftPlayer craftPlayer = (CraftPlayer) getPlayer();
		((Entity) craftPlayer.getHandle()).getEntityData().set(new DataWatcherObject<>(13, DataWatcherRegistry.INT), 0);
		((Entity) craftPlayer.getHandle()).setInvisible(true);
		injectSelf();
		final PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(getPlayer().getEntityId(), NULL_PAIR_LIST);
		for (CraftPlayer player : ((CraftServer) Bukkit.getServer()).getOnlinePlayers()) {
			if (player.equals(getPlayer())) continue;
			player.getHandle().connection.send(packet);
			try {
				injectPlayer(player);
			} catch (NoSuchElementException ignored) {}
		}
	}

	@Override
	protected void show0() {
		final PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(getPlayer().getEntityId(), Arrays.asList(
				com.mojang.datafixers.util.Pair.of(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(getPlayer().getInventory().getItemInMainHand())),
				com.mojang.datafixers.util.Pair.of(EnumItemSlot.OFFHAND, CraftItemStack.asNMSCopy(getPlayer().getInventory().getItemInOffHand())),
				com.mojang.datafixers.util.Pair.of(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(getPlayer().getInventory().getBoots())),
				com.mojang.datafixers.util.Pair.of(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(getPlayer().getInventory().getLeggings())),
				com.mojang.datafixers.util.Pair.of(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(getPlayer().getInventory().getChestplate())),
				com.mojang.datafixers.util.Pair.of(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(getPlayer().getInventory().getHelmet()))
		));
		for (Entry<UUID, Pair<CraftPlayer, ChannelOutboundHandlerAdapter>> entry : channelHandlers.entrySet()) {
			final CraftPlayer player = entry.getValue().getLeft();
			try {
				player.getHandle().connection.connection.channel.pipeline().remove(entry.getValue().getRight());
			} catch (NoSuchElementException ignored) {
			}
			if (!player.isValid()) continue;
			player.getHandle().connection.send(packet);
		}
		channelHandlers.clear();
		new BukkitRunnable() {
			@Override
			public void run() {
				((Entity) ((CraftPlayer) getPlayer()).getHandle()).setInvisible(false);
			}
		}.runTaskLater(AbilityWar.getPlugin(), 2L);
	}

	private void injectPlayer(CraftPlayer player) {
		if (!player.isValid()) return;
		if (channelHandlers.containsKey(player.getUniqueId())) {
			final Pair<CraftPlayer, ChannelOutboundHandlerAdapter> pair = channelHandlers.get(player.getUniqueId());
			if (!pair.getLeft().isValid()) {
				try {
					pair.getLeft().getHandle().connection.connection.channel.pipeline().remove(pair.getRight());
				} catch (NoSuchElementException ignored) {
				}
			} else return;
		}
		final ChannelOutboundHandlerAdapter handler = new ChannelOutboundHandlerAdapter() {
			@Override
			public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
				if (packet instanceof PacketPlayOutEntityEquipment) {
					if ((int) FieldUtil.getValue(packet, "b") == getPlayer().getEntityId()) {
						FieldUtil.setValue(packet, "c", NULL_PAIR_LIST);
					}
				} else if (packet instanceof PacketPlayOutEntityMetadata) {
					if ((int) FieldUtil.getValue(packet, "a") == getPlayer().getEntityId()) {
						List<Item<?>> items = FieldUtil.getValue(packet, "b");
						if (items.size() != 0) {
							Item<?> item = items.get(0);
							if (BYTE_DATA_WATCHER_OBJECT.equals(item.getAccessor())) {
								Item<Byte> byteItem = (Item<Byte>) item;
								byteItem.setValue((byte) (byteItem.getValue() | 1 << 5));
								((Entity) ((CraftPlayer) getPlayer()).getHandle()).setInvisible(true);
							}
						}
					}
				}
				super.write(ctx, packet, promise);
			}
		};
		channelHandlers.put(player.getUniqueId(), Pair.of(player, handler));
		player.getHandle().connection.connection.channel.pipeline().addBefore("packet_handler", hashCode() + ":" + player.getName(), handler);
	}

	private void injectSelf() {
		final CraftPlayer player = (CraftPlayer) getPlayer();
		if (!player.isValid()) return;
		if (channelHandlers.containsKey(player.getUniqueId())) {
			final Pair<CraftPlayer, ChannelOutboundHandlerAdapter> pair = channelHandlers.get(player.getUniqueId());
			if (!pair.getLeft().isValid()) {
				try {
					pair.getLeft().getHandle().connection.connection.channel.pipeline().remove(pair.getRight());
				} catch (NoSuchElementException ignored) {
				}
			} else return;
		}
		final ChannelOutboundHandlerAdapter handler = new ChannelOutboundHandlerAdapter() {
			@Override
			public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
				if (packet instanceof PacketPlayOutEntityMetadata) {
					if ((int) FieldUtil.getValue(packet, "a") == getPlayer().getEntityId()) {
						List<Item<?>> items = FieldUtil.getValue(packet, "b");
						if (items.size() != 0) {
							Item<?> item = items.get(0);
							if (BYTE_DATA_WATCHER_OBJECT.equals(item.getAccessor())) {
								Item<Byte> byteItem = (Item<Byte>) item;
								byteItem.setValue((byte) (byteItem.getValue() | 1 << 5));
								((Entity) ((CraftPlayer) getPlayer()).getHandle()).setInvisible(true);
							}
						}
					}
				}
				super.write(ctx, packet, promise);
			}
		};
		channelHandlers.put(player.getUniqueId(), Pair.of(player, handler));
		player.getHandle().connection.connection.channel.pipeline().addBefore(player.getHandle().connection.connection.channel.eventLoop(), "packet_handler", hashCode() + ":" + player.getName(), handler);
	}

}
