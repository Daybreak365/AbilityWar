package daybreak.abilitywar.utils.base.minecraft.nms.v1_9_R1;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.utils.base.minecraft.nms.IHologram;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.server.v1_9_R1.EntityArmorStand;
import net.minecraft.server.v1_9_R1.EntityPlayer;
import net.minecraft.server.v1_9_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_9_R1.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_9_R1.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_9_R1.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_9_R1.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

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
		armorStand.setCustomName(text);
		armorStand.getDataWatcher().set(EntityArmorStand.a, (byte) (armorStand.getDataWatcher().get(EntityArmorStand.a) | 16));
		armorStand.setSize(0F, 0F);
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
	}

	HologramImpl(World world, double x, double y, double z) {
		this(world, x, y, z, "");
	}

	@EventHandler
	private void onPlayerQuit(PlayerQuitEvent e) {
		viewers.remove(e.getPlayer());
	}

	@Override
	public void display(Player player) throws IllegalStateException {
		if (viewers == null) throw new IllegalStateException("Hologram unregistered");
		final CraftPlayer craftPlayer = (CraftPlayer) player;
		if (viewers.add(craftPlayer)) {
			PlayerConnection connection = craftPlayer.getHandle().playerConnection;
			connection.sendPacket(packetPlayOutSpawnEntityLiving);
			connection.sendPacket(new PacketPlayOutEntityMetadata(armorStand.getId(), armorStand.getDataWatcher(), true));
		}
	}

	@Override
	public void hide(Player player) throws IllegalStateException {
		if (viewers == null) throw new IllegalStateException("Hologram unregistered");
		EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
		if (viewers.remove(entityPlayer)) {
			entityPlayer.playerConnection.sendPacket(packetPlayOutEntityDestroy);
		}
	}

	@Override
	public void teleport(World world, double x, double y, double z, float yaw, float pitch) throws IllegalStateException {
		if (viewers == null) throw new IllegalStateException("Hologram unregistered");
		armorStand.spawnIn(((CraftWorld) world).getHandle());
		armorStand.setLocation(x, y, z, yaw, pitch);
		for (CraftPlayer craftPlayer : viewers) {
			craftPlayer.getHandle().playerConnection.sendPacket(new PacketPlayOutEntityTeleport(armorStand));
		}
	}

	@Override
	public Location getLocation() {
		return new Location(armorStand.world.getWorld(), armorStand.locX, armorStand.locY, armorStand.locZ, armorStand.yaw, armorStand.pitch);
	}

	@Override
	public void setText(String text) throws IllegalStateException {
		if (viewers == null) throw new IllegalStateException("Hologram unregistered");
		armorStand.setCustomName(text);
		for (CraftPlayer craftPlayer : viewers) {
			craftPlayer.getHandle().playerConnection.sendPacket(new PacketPlayOutEntityMetadata(armorStand.getId(), armorStand.getDataWatcher(), true));
		}
	}

	@Override
	public String getText() {
		return armorStand.getCustomName();
	}

	@Override
	public void unregister() throws IllegalStateException {
		if (viewers == null) throw new IllegalStateException("Hologram unregistered");
		HandlerList.unregisterAll(this);
		for (Iterator<CraftPlayer> iterator = viewers.iterator(); iterator.hasNext(); ) {
			iterator.next().getHandle().playerConnection.sendPacket(packetPlayOutEntityDestroy);
			iterator.remove();
		}
		viewers = null;
	}

}
