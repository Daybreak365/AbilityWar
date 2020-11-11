package daybreak.abilitywar.ability.list.scarecrow.v1_12_R1;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.list.scarecrow.AbstractScareCrow;
import daybreak.abilitywar.ability.list.scarecrow.FakeScareCrowEntity;
import daybreak.abilitywar.ability.list.scarecrow.ScareCrowEntity;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.minecraft.nms.v1_12_R1.packet.PacketHandler;
import daybreak.abilitywar.utils.base.reflect.ReflectionUtil.FieldUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_12_R1.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Function;

public class ScareCrow extends AbstractScareCrow {

	private final PacketHandler scarecrow = new PacketHandler(new Function<CraftPlayer, ChannelHandler>() {
		@Override
		public ChannelHandler apply(CraftPlayer player) {
			return new ChannelOutboundHandlerAdapter() {
				@Override
				public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
					if (packet instanceof PacketPlayOutNamedEntitySpawn) {
						final ScareCrowEntity entity = getEntityById(FieldUtil.getValue(packet, "a"));
						if (entity != null) {
							final EntityScareCrow impl = (EntityScareCrow) entity;
							ctx.write(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, impl));
							super.write(ctx, packet, promise);
							for (PacketPlayOutEntityEquipment equipmentPacket : impl.equipmentPackets) {
								ctx.write(equipmentPacket);
							}
							new BukkitRunnable() {
								@Override
								public void run() {
									ctx.write(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, impl));
								}
							}.runTaskLater(AbilityWar.getPlugin(), 100L);
							return;
						}
					}
					super.write(ctx, packet, promise);
				}
			};
		}
	});

	public ScareCrow(Participant participant) {
		super(participant);
		for (CraftPlayer player : ((CraftServer) Bukkit.getServer()).getOnlinePlayers()) {
			scarecrow.attach(player);
		}
	}

	@Override
	protected ScareCrowEntity createScareCrow(Location location) {
		return new EntityScareCrow(this, ((CraftServer) Bukkit.getServer()).getServer(), ((CraftWorld) getPlayer().getWorld()).getHandle(), location);
	}

	@Override
	protected FakeScareCrowEntity createFakeScareCrow(Location location) {
		return new FakeScareCrow(this, ((CraftServer) Bukkit.getServer()).getServer(), ((CraftWorld) getPlayer().getWorld()).getHandle(), location);
	}

	@SubscribeEvent
	private void onJoin(PlayerJoinEvent e) {
		scarecrow.attach((CraftPlayer) e.getPlayer());
	}

	@SubscribeEvent
	private void onQuit(PlayerQuitEvent e) {
		scarecrow.detach(e.getPlayer().getUniqueId());
	}

	@Override
	protected void onUpdate(Update update) {
		super.onUpdate(update);
		if (update == Update.ABILITY_DESTROY) {
			scarecrow.detachAll();
		}
	}
}
