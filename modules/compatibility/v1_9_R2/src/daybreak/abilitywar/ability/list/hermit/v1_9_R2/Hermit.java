package daybreak.abilitywar.ability.list.hermit.v1_9_R2;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.AbstractGame.RestrictionBehavior;
import daybreak.abilitywar.utils.base.TimeUtil;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer.TaskType;
import daybreak.abilitywar.utils.base.reflect.ReflectionUtil.FieldUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.server.v1_9_R2.DataWatcher.Item;
import net.minecraft.server.v1_9_R2.DataWatcherObject;
import net.minecraft.server.v1_9_R2.DataWatcherRegistry;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.EnumItemSlot;
import net.minecraft.server.v1_9_R2.ItemStack;
import net.minecraft.server.v1_9_R2.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_9_R2.PacketPlayOutEntityMetadata;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_9_R2.CraftServer;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Arrow;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

@AbilityManifest(name = "헤르밋", rank = Rank.A, species = Species.HUMAN, explain = {
		"비전투 상태로 20초가 지날 경우 은신합니다. 은신 상태에서는",
		"갑옷과 들고 있는 아이템이 상대에게 보이지 않으며, 몸이 투명해집니다.",
		"공격을 당하거나 공격을 할 경우 발각되고, 은신 상태가 해제됩니다."
})
public class Hermit extends AbilityBase {

	private static final ItemStack NULL_ITEMSTACK = new ItemStack((net.minecraft.server.v1_9_R2.Item) null);
	private static final DataWatcherObject<Byte> BYTE_DATA_WATCHER_OBJECT;

	static {
		try {
			BYTE_DATA_WATCHER_OBJECT = FieldUtil.getStaticValue(Entity.class, "ay");
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private final Map<UUID, ChannelOutboundHandlerAdapter> channelHandlers = new HashMap<>();
	private final ActionbarChannel cooldownActionbarChannel = newActionbarChannel();
	private final ActionbarChannel actionbarChannel = newActionbarChannel();
	private final Timer cooldown = new Timer(TaskType.REVERSE, 20) {
		@Override
		protected void onStart() {
			show();
		}

		@Override
		protected void run(int count) {
			cooldownActionbarChannel.update(ChatColor.RED.toString() + "은신까지 " + ChatColor.WHITE.toString() + ": " + ChatColor.GOLD + TimeUtil.parseTimeAsString(getCount()));
		}

		@Override
		protected void onCountSet() {
			cooldownActionbarChannel.update(ChatColor.RED.toString() + "은신까지 " + ChatColor.WHITE.toString() + ": " + ChatColor.GOLD + TimeUtil.parseTimeAsString(getCount()));
		}

		@Override
		protected void onEnd() {
			cooldownActionbarChannel.update(null);
			hide();
		}
	}.setBehavior(RestrictionBehavior.PAUSE_RESUME);

	public Hermit(Participant participant) {
		super(participant);
	}

	@SubscribeEvent
	private void onJoin(PlayerJoinEvent e) {
		final CraftPlayer player = (CraftPlayer) e.getPlayer();
		if (player.equals(getPlayer())) return;
		injectPlayer(player);
	}

	@SubscribeEvent
	private void onQuit(PlayerQuitEvent e) {
		final CraftPlayer player = (CraftPlayer) e.getPlayer();
		if (player.equals(getPlayer())) return;
		if (channelHandlers.containsKey(player.getUniqueId())) {
			player.getHandle().playerConnection.networkManager.channel.pipeline().remove(channelHandlers.get(player.getUniqueId()));
			channelHandlers.remove(player.getUniqueId());
		}
	}

	@SubscribeEvent
	private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (getPlayer().equals(e.getEntity()) || getPlayer().equals(e.getDamager()) || (e.getDamager() instanceof Arrow && getPlayer().equals(((Arrow) e.getDamager()).getShooter()))) {
			if (!cooldown.start()) {
				cooldown.setCount(20);
			}
		}
	}

	private void hide() {
		actionbarChannel.update("§7은신 중");
		getParticipant().attributes().TARGETABLE.setValue(false);
		final CraftPlayer craftPlayer = (CraftPlayer) getPlayer();
		craftPlayer.getHandle().getDataWatcher().set(new DataWatcherObject<>(9, DataWatcherRegistry.b), 0);
		craftPlayer.getHandle().setInvisible(true);
		final PacketPlayOutEntityEquipment[] packets = {
				new PacketPlayOutEntityEquipment(getPlayer().getEntityId(), EnumItemSlot.MAINHAND, NULL_ITEMSTACK),
				new PacketPlayOutEntityEquipment(getPlayer().getEntityId(), EnumItemSlot.OFFHAND, NULL_ITEMSTACK),
				new PacketPlayOutEntityEquipment(getPlayer().getEntityId(), EnumItemSlot.HEAD, NULL_ITEMSTACK),
				new PacketPlayOutEntityEquipment(getPlayer().getEntityId(), EnumItemSlot.CHEST, NULL_ITEMSTACK),
				new PacketPlayOutEntityEquipment(getPlayer().getEntityId(), EnumItemSlot.LEGS, NULL_ITEMSTACK),
				new PacketPlayOutEntityEquipment(getPlayer().getEntityId(), EnumItemSlot.FEET, NULL_ITEMSTACK)
		};
		for (CraftPlayer player : ((CraftServer) Bukkit.getServer()).getOnlinePlayers()) {
			if (player.equals(getPlayer())) continue;
			for (PacketPlayOutEntityEquipment packet : packets) {
				player.getHandle().playerConnection.sendPacket(packet);
			}
			injectPlayer(player);
		}
	}

	private void show() {
		actionbarChannel.update("§6발각됨");
		getParticipant().attributes().TARGETABLE.setValue(true);
		final PacketPlayOutEntityEquipment[] packets = {
				new PacketPlayOutEntityEquipment(getPlayer().getEntityId(), EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(getPlayer().getInventory().getItemInMainHand())),
				new PacketPlayOutEntityEquipment(getPlayer().getEntityId(), EnumItemSlot.OFFHAND, CraftItemStack.asNMSCopy(getPlayer().getInventory().getItemInOffHand())),
				new PacketPlayOutEntityEquipment(getPlayer().getEntityId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(getPlayer().getInventory().getHelmet())),
				new PacketPlayOutEntityEquipment(getPlayer().getEntityId(), EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(getPlayer().getInventory().getChestplate())),
				new PacketPlayOutEntityEquipment(getPlayer().getEntityId(), EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(getPlayer().getInventory().getLeggings())),
				new PacketPlayOutEntityEquipment(getPlayer().getEntityId(), EnumItemSlot.FEET, CraftItemStack.asNMSCopy(getPlayer().getInventory().getBoots()))
		};
		for (Entry<UUID, ChannelOutboundHandlerAdapter> entry : channelHandlers.entrySet()) {
			CraftPlayer player = (CraftPlayer) Bukkit.getPlayer(entry.getKey());
			if (player != null) {
				player.getHandle().playerConnection.networkManager.channel.pipeline().remove(entry.getValue());
				for (PacketPlayOutEntityEquipment packet : packets) {
					player.getHandle().playerConnection.sendPacket(packet);
				}
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

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			if (!cooldown.isRunning()) {
				hide();
			}
		} else if (update == Update.RESTRICTION_SET || update == Update.ABILITY_DESTROY) {
			show();
			actionbarChannel.update(null);
		}
	}

	private void injectPlayer(CraftPlayer player) {
		final ChannelOutboundHandlerAdapter handler = new ChannelOutboundHandlerAdapter() {
			@Override
			public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
				if (packet instanceof PacketPlayOutEntityEquipment) {
					if ((int) FieldUtil.getValue(packet, "a") == getPlayer().getEntityId()) {
						FieldUtil.setValue(packet, "c", NULL_ITEMSTACK);
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
		channelHandlers.put(player.getUniqueId(), handler);
		player.getHandle().playerConnection.networkManager.channel.pipeline().addBefore("packet_handler", hashCode() + ":" + player.getName(), handler);
	}

}
