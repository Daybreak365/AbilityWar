package daybreak.abilitywar.ability.list.clown.v1_12_R1;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.list.clown.AbstractClown;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.collect.Pair;
import daybreak.abilitywar.utils.base.reflect.ReflectionUtil.FieldUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import net.minecraft.server.v1_12_R1.DataWatcher.Item;
import net.minecraft.server.v1_12_R1.DataWatcherObject;
import net.minecraft.server.v1_12_R1.DataWatcherRegistry;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EnumItemSlot;
import net.minecraft.server.v1_12_R1.ItemStack;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityMetadata;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.UUID;

public class Clown extends AbstractClown {

	private static final DataWatcherObject<Byte> BYTE_DATA_WATCHER_OBJECT;

	static {
		try {
			BYTE_DATA_WATCHER_OBJECT = FieldUtil.getStaticValue(Entity.class, "Z");
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private final Map<UUID, Pair<CraftPlayer, ChannelOutboundHandlerAdapter>> channelHandlers = new HashMap<>();

	public Clown(Participant participant) {
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
					for (PacketPlayOutEntityEquipment packet : new PacketPlayOutEntityEquipment[]{
							new PacketPlayOutEntityEquipment(getPlayer().getEntityId(), EnumItemSlot.MAINHAND, ItemStack.a),
							new PacketPlayOutEntityEquipment(getPlayer().getEntityId(), EnumItemSlot.OFFHAND, ItemStack.a),
							new PacketPlayOutEntityEquipment(getPlayer().getEntityId(), EnumItemSlot.HEAD, ItemStack.a),
							new PacketPlayOutEntityEquipment(getPlayer().getEntityId(), EnumItemSlot.CHEST, ItemStack.a),
							new PacketPlayOutEntityEquipment(getPlayer().getEntityId(), EnumItemSlot.LEGS, ItemStack.a),
							new PacketPlayOutEntityEquipment(getPlayer().getEntityId(), EnumItemSlot.FEET, ItemStack.a)
					}) {
						player.getHandle().playerConnection.sendPacket(packet);
					}
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
				player.getHandle().playerConnection.networkManager.channel.pipeline().remove(channelHandlers.remove(player.getUniqueId()).getRight());
			} catch (NoSuchElementException ignored) {
			}
		}
	}

	@Override
	protected void hide0() {
		final CraftPlayer craftPlayer = (CraftPlayer) getPlayer();
		craftPlayer.getHandle().getDataWatcher().set(new DataWatcherObject<>(10, DataWatcherRegistry.b), 0);
		craftPlayer.getHandle().setInvisible(true);
		final PacketPlayOutEntityEquipment[] packets = {
				new PacketPlayOutEntityEquipment(getPlayer().getEntityId(), EnumItemSlot.MAINHAND, ItemStack.a),
				new PacketPlayOutEntityEquipment(getPlayer().getEntityId(), EnumItemSlot.OFFHAND, ItemStack.a),
				new PacketPlayOutEntityEquipment(getPlayer().getEntityId(), EnumItemSlot.HEAD, ItemStack.a),
				new PacketPlayOutEntityEquipment(getPlayer().getEntityId(), EnumItemSlot.CHEST, ItemStack.a),
				new PacketPlayOutEntityEquipment(getPlayer().getEntityId(), EnumItemSlot.LEGS, ItemStack.a),
				new PacketPlayOutEntityEquipment(getPlayer().getEntityId(), EnumItemSlot.FEET, ItemStack.a)
		};
		injectSelf();
		for (CraftPlayer player : ((CraftServer) Bukkit.getServer()).getOnlinePlayers()) {
			if (player.equals(getPlayer())) continue;
			for (PacketPlayOutEntityEquipment packet : packets) {
				player.getHandle().playerConnection.sendPacket(packet);
			}
			injectPlayer(player);
		}
	}

	@Override
	protected void show0() {
		final PacketPlayOutEntityEquipment[] packets = {
				new PacketPlayOutEntityEquipment(getPlayer().getEntityId(), EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(getPlayer().getInventory().getItemInMainHand())),
				new PacketPlayOutEntityEquipment(getPlayer().getEntityId(), EnumItemSlot.OFFHAND, CraftItemStack.asNMSCopy(getPlayer().getInventory().getItemInOffHand())),
				new PacketPlayOutEntityEquipment(getPlayer().getEntityId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(getPlayer().getInventory().getHelmet())),
				new PacketPlayOutEntityEquipment(getPlayer().getEntityId(), EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(getPlayer().getInventory().getChestplate())),
				new PacketPlayOutEntityEquipment(getPlayer().getEntityId(), EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(getPlayer().getInventory().getLeggings())),
				new PacketPlayOutEntityEquipment(getPlayer().getEntityId(), EnumItemSlot.FEET, CraftItemStack.asNMSCopy(getPlayer().getInventory().getBoots()))
		};
		for (Entry<UUID, Pair<CraftPlayer, ChannelOutboundHandlerAdapter>> entry : channelHandlers.entrySet()) {
			final CraftPlayer player = entry.getValue().getLeft();
			try {
				player.getHandle().playerConnection.networkManager.channel.pipeline().remove(entry.getValue().getRight());
			} catch (NoSuchElementException ignored) {
			}
			if (!player.isValid()) continue;
			for (PacketPlayOutEntityEquipment packet : packets) {
				player.getHandle().playerConnection.sendPacket(packet);
			}
		}
		channelHandlers.clear();
		new BukkitRunnable() {
			@Override
			public void run() {
				((CraftPlayer) getPlayer()).getHandle().setInvisible(false);
			}
		}.runTaskLater(AbilityWar.getPlugin(), 2L);
	}

	private void injectPlayer(CraftPlayer player) {
		if (!player.isValid()) return;
		if (channelHandlers.containsKey(player.getUniqueId())) {
			final Pair<CraftPlayer, ChannelOutboundHandlerAdapter> pair = channelHandlers.get(player.getUniqueId());
			if (!pair.getLeft().isValid()) {
				try {
					pair.getLeft().getHandle().playerConnection.networkManager.channel.pipeline().remove(pair.getRight());
				} catch (NoSuchElementException ignored) {
				}
			} else return;
		}
		final ChannelOutboundHandlerAdapter handler = new ChannelOutboundHandlerAdapter() {
			@Override
			public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
				if (packet instanceof PacketPlayOutEntityEquipment) {
					if ((int) FieldUtil.getValue(packet, "a") == getPlayer().getEntityId()) {
						FieldUtil.setValue(packet, "c", ItemStack.a);
					}
				} else if (packet instanceof PacketPlayOutEntityMetadata) {
					if ((int) FieldUtil.getValue(packet, "a") == getPlayer().getEntityId()) {
						List<Item<?>> items = FieldUtil.getValue(packet, "b");
						if (items.size() != 0) {
							Item<?> item = items.get(0);
							if (BYTE_DATA_WATCHER_OBJECT.equals(item.a())) {
								Item<Byte> byteItem = (Item<Byte>) item;
								byteItem.a((byte) (byteItem.b() | 1 << 5));
								((CraftPlayer) getPlayer()).getHandle().setInvisible(true);
							}
						}
					}
				}
				super.write(ctx, packet, promise);
			}
		};
		channelHandlers.put(player.getUniqueId(), Pair.of(player, handler));
		player.getHandle().playerConnection.networkManager.channel.pipeline().addBefore("packet_handler", hashCode() + ":" + player.getName(), handler);
	}

	private void injectSelf() {
		final CraftPlayer player = (CraftPlayer) getPlayer();
		if (!player.isValid()) return;
		if (channelHandlers.containsKey(player.getUniqueId())) {
			final Pair<CraftPlayer, ChannelOutboundHandlerAdapter> pair = channelHandlers.get(player.getUniqueId());
			if (!pair.getLeft().isValid()) {
				try {
					pair.getLeft().getHandle().playerConnection.networkManager.channel.pipeline().remove(pair.getRight());
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
							if (BYTE_DATA_WATCHER_OBJECT.equals(item.a())) {
								Item<Byte> byteItem = (Item<Byte>) item;
								byteItem.a((byte) (byteItem.b() | 1 << 5));
								((CraftPlayer) getPlayer()).getHandle().setInvisible(true);
							}
						}
					}
				}
				super.write(ctx, packet, promise);
			}
		};
		channelHandlers.put(player.getUniqueId(), Pair.of(player, handler));
		player.getHandle().playerConnection.networkManager.channel.pipeline().addBefore("packet_handler", hashCode() + ":" + player.getName(), handler);
	}

}
