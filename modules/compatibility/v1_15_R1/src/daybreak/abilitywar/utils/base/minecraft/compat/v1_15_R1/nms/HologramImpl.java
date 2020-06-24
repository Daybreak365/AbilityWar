package daybreak.abilitywar.utils.base.minecraft.compat.v1_15_R1.nms;

import daybreak.abilitywar.utils.base.minecraft.compat.nms.Hologram;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.server.v1_15_R1.EntityArmorStand;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.IChatBaseComponent;
import net.minecraft.server.v1_15_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_15_R1.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_15_R1.PlayerConnection;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class HologramImpl implements Hologram {

	private final EntityArmorStand armorStand;
	private final Set<EntityPlayer> viewers = new HashSet<>();
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
	}

	HologramImpl(World world, double x, double y, double z) {
		this(world, x, y, z, "");
	}

	@Override
	public void display(Player player) {
		EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
		if (viewers.add(entityPlayer)) {
			PlayerConnection connection = entityPlayer.playerConnection;
			connection.sendPacket(packetPlayOutSpawnEntityLiving);
			connection.sendPacket(new PacketPlayOutEntityMetadata(armorStand.getId(), armorStand.getDataWatcher(), true));
		}
	}

	@Override
	public void hide(Player player) {
		EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
		if (viewers.remove(entityPlayer)) {
			entityPlayer.playerConnection.sendPacket(packetPlayOutEntityDestroy);
		}
	}

	@Override
	public void teleport(World world, double x, double y, double z, float yaw, float pitch) {
		armorStand.spawnIn(((CraftWorld) world).getHandle());
		armorStand.setLocation(x, y, z, yaw, pitch);
		for (EntityPlayer entityPlayer : viewers) {
			entityPlayer.playerConnection.sendPacket(new PacketPlayOutEntityTeleport(armorStand));
		}
	}

	@Override
	public Location getLocation() {
		return new Location(armorStand.world.getWorld(), armorStand.locX(), armorStand.locY(), armorStand.locZ(), armorStand.yaw, armorStand.pitch);
	}

	@Override
	public void setText(String text) {
		armorStand.setCustomName(ChatSerializer.a("{\"text\":\"" + text + "\"}"));
		for (EntityPlayer entityPlayer : viewers) {
			entityPlayer.playerConnection.sendPacket(new PacketPlayOutEntityMetadata(armorStand.getId(), armorStand.getDataWatcher(), true));
		}
	}

	@Override
	public String getText() {
		IChatBaseComponent component = armorStand.getCustomName();
		return component != null ? component.getText() : "";
	}

}
