package daybreak.abilitywar.utils.base.minecraft.nms.v1_17_R1;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.utils.base.minecraft.nms.IHologram;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatBaseComponent.ChatSerializer;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutEntityTeleport;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class HologramImpl implements IHologram, Listener {

	private final EntityArmorStand armorStand;
	private Set<CraftPlayer> viewers = new HashSet<>();
	private final PacketPlayOutSpawnEntityLiving packetPlayOutSpawnEntityLiving;
	private final PacketPlayOutEntityDestroy packetPlayOutEntityDestroy;

	HologramImpl(World world, double x, double y, double z, String text) {
		this.armorStand = new EntityArmorStand(((CraftWorld) world).getHandle(), x, y, z);
		this.packetPlayOutSpawnEntityLiving = new PacketPlayOutSpawnEntityLiving(armorStand);
		this.packetPlayOutEntityDestroy = new PacketPlayOutEntityDestroy(armorStand.getId());
		armorStand.setInvisible(true);
		armorStand.setCustomNameVisible(true);
		armorStand.setCustomName(ChatSerializer.a("{\"text\":\"" + text + "\"}"));
		armorStand.setMarker(true);
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
	}

	HologramImpl(World world, double x, double y, double z) {
		this(world, x, y, z, "");
	}

	@EventHandler
	private void onPlayerQuit(PlayerQuitEvent e) {
		viewers.remove(e.getPlayer());
	}

	@EventHandler
	private void onPlayerRespawn(PlayerRespawnEvent e) {
		if (viewers.contains(e.getPlayer())) {
			new BukkitRunnable() {
				@Override
				public void run() {
					display0((CraftPlayer) e.getPlayer());
				}
			}.runTaskLater(AbilityWar.getPlugin(), 2L);
		}
	}

	@EventHandler
	private void onPlayerTeleport(PlayerTeleportEvent e) {
		if (viewers.contains(e.getPlayer())) {
			final net.minecraft.world.level.World nmsWorld = armorStand.getWorld();
			if (nmsWorld != null) {
				final World bukkitWorld = nmsWorld.getWorld();
				if (bukkitWorld != null && !bukkitWorld.equals(e.getFrom().getWorld()) && bukkitWorld.equals(e.getTo().getWorld())) {
					new BukkitRunnable() {
						@Override
						public void run() {
							display0((CraftPlayer) e.getPlayer());
						}
					}.runTaskLater(AbilityWar.getPlugin(), 2L);
				}
			}
		}
	}

	@Override
	public void display(Player player) throws IllegalStateException {
		if (viewers == null) throw new IllegalStateException("Hologram unregistered");
		final CraftPlayer craftPlayer = (CraftPlayer) player;
		if (viewers.add(craftPlayer)) {
			display0(craftPlayer);
		}
	}

	private void display0(CraftPlayer player) {
		final PlayerConnection connection = player.getHandle().b;
		connection.sendPacket(packetPlayOutSpawnEntityLiving);
		connection.sendPacket(new PacketPlayOutEntityMetadata(armorStand.getId(), armorStand.getDataWatcher(), true));
	}

	@Override
	public void hide(Player player) throws IllegalStateException {
		if (viewers == null) throw new IllegalStateException("Hologram unregistered");
		final CraftPlayer craftPlayer = (CraftPlayer) player;
		if (viewers.remove(craftPlayer)) {
			craftPlayer.getHandle().b.sendPacket(packetPlayOutEntityDestroy);
		}
	}

	@Override
	public void teleport(World world, double x, double y, double z, float yaw, float pitch) throws IllegalStateException {
		if (viewers == null) throw new IllegalStateException("Hologram unregistered");
		//armorStand.die();
		armorStand.t = ((CraftWorld) world).getHandle();
		armorStand.setLocation(x, y, z, yaw, pitch);
		for (CraftPlayer craftPlayer : viewers) {
			craftPlayer.getHandle().b.sendPacket(new PacketPlayOutEntityTeleport(armorStand));
		}
	}

	@Override
	public Location getLocation() {
		return new Location(armorStand.t.getWorld(), armorStand.locX(), armorStand.locY(), armorStand.locZ(), armorStand.getYRot(), armorStand.getXRot());
	}

	@Override
	public String getText() {
		final IChatBaseComponent component = armorStand.getCustomName();
		return component != null ? component.getText() : "";
	}

	@Override
	public void setText(String text) throws IllegalStateException {
		if (viewers == null) throw new IllegalStateException("Hologram unregistered");
		armorStand.setCustomName(ChatSerializer.a("{\"text\":\"" + text + "\"}"));
		for (CraftPlayer craftPlayer : viewers) {
			craftPlayer.getHandle().b.sendPacket(new PacketPlayOutEntityMetadata(armorStand.getId(), armorStand.getDataWatcher(), true));
		}
	}

	@Override
	public void unregister() throws IllegalStateException {
		if (viewers == null) throw new IllegalStateException("Hologram unregistered");
		HandlerList.unregisterAll(this);
		for (Iterator<CraftPlayer> iterator = viewers.iterator(); iterator.hasNext(); ) {
			iterator.next().getHandle().b.sendPacket(packetPlayOutEntityDestroy);
			iterator.remove();
		}
		viewers = null;
	}

	@Override
	public boolean isUnregistered() {
		return viewers == null;
	}

}
