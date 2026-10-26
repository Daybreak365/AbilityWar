package daybreak.abilitywar.utils.base.minecraft.nms.v1_20_R4;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.utils.base.minecraft.nms.IHologram;
import net.minecraft.network.chat.Component;

import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
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

	private static final class HologramStand extends ArmorStand {
		HologramStand(net.minecraft.world.level.Level level, double x, double y, double z) { super(level, x, y, z); }
		void changeLevel(net.minecraft.world.level.Level level) { setLevel(level); }
	}

	private final HologramStand armorStand;
	private Set<CraftPlayer> viewers = new HashSet<>();
	private final ClientboundRemoveEntitiesPacket packetPlayOutEntityDestroy;

	HologramImpl(World world, double x, double y, double z, String text) {
		this.armorStand = new HologramStand(((CraftWorld) world).getHandle(), x, y, z);
		this.packetPlayOutEntityDestroy = new ClientboundRemoveEntitiesPacket(((Entity) armorStand).getId());
		armorStand.setInvisible(true);
		((Entity) armorStand).setCustomNameVisible(true);
		((Entity) armorStand).setCustomName(Component.literal(text));
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
			final net.minecraft.world.level.Level nmsWorld = ((Entity) armorStand).level();
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
		if (viewers == null || !viewers.contains(player) || !player.getWorld().equals(getLocation().getWorld())) return;
		final ServerGamePacketListenerImpl connection = player.getHandle().connection;
		connection.send(Packets.spawn(armorStand));
		connection.send(new ClientboundSetEntityDataPacket(((Entity) armorStand).getId(), ((Entity) armorStand).getEntityData().getNonDefaultValues()));
	}

	@Override
	public void hide(Player player) throws IllegalStateException {
		if (viewers == null) throw new IllegalStateException("Hologram unregistered");
		final CraftPlayer craftPlayer = (CraftPlayer) player;
		if (viewers.remove(craftPlayer)) {
			craftPlayer.getHandle().connection.send(packetPlayOutEntityDestroy);
		}
	}

	@Override
	public void teleport(World world, double x, double y, double z, float yaw, float pitch) throws IllegalStateException {
		if (viewers == null) throw new IllegalStateException("Hologram unregistered");
		final boolean changedWorld = !world.equals(getLocation().getWorld());
		if (changedWorld) {
			for (CraftPlayer viewer : viewers) viewer.getHandle().connection.send(packetPlayOutEntityDestroy);
		}
		armorStand.changeLevel(((CraftWorld) world).getHandle());
		((Entity) armorStand).moveTo(x, y, z, yaw, pitch);
		ClientboundTeleportEntityPacket teleportPacket = null;
		for (CraftPlayer craftPlayer : viewers) {
			if (changedWorld) display0(craftPlayer);
			else if (craftPlayer.getWorld().equals(world)) {
				if (teleportPacket == null) teleportPacket = Packets.teleport(armorStand);
				craftPlayer.getHandle().connection.send(teleportPacket);
			}
		}
	}

	@Override
	public Location getLocation() {
		return new Location(((Entity) armorStand).level().getWorld(), ((Entity) armorStand).getX(), ((Entity) armorStand).getY(), ((Entity) armorStand).getZ(), ((Entity) armorStand).getYRot(), ((Entity) armorStand).getXRot());
	}

	@Override
	public String getText() {
		final Component component = ((Entity) armorStand).getCustomName();
		return component != null ? component.getString() : "";
	}

	@Override
	public void setText(String text) throws IllegalStateException {
		if (viewers == null) throw new IllegalStateException("Hologram unregistered");
		((Entity) armorStand).setCustomName(Component.literal(text));
		ClientboundSetEntityDataPacket textPacket = null;
		for (CraftPlayer craftPlayer : viewers) {
			if (textPacket == null) textPacket = new ClientboundSetEntityDataPacket(((Entity) armorStand).getId(), ((Entity) armorStand).getEntityData().getNonDefaultValues());
			craftPlayer.getHandle().connection.send(textPacket);
		}
	}

	@Override
	public void unregister() throws IllegalStateException {
		if (viewers == null) throw new IllegalStateException("Hologram unregistered");
		HandlerList.unregisterAll(this);
		for (Iterator<CraftPlayer> iterator = viewers.iterator(); iterator.hasNext(); ) {
			iterator.next().getHandle().connection.send(packetPlayOutEntityDestroy);
			iterator.remove();
		}
		viewers = null;
	}

	@Override
	public boolean isUnregistered() {
		return viewers == null;
	}

}
