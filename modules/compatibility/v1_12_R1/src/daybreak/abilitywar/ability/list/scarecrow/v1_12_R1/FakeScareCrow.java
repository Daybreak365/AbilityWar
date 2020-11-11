package daybreak.abilitywar.ability.list.scarecrow.v1_12_R1;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import daybreak.abilitywar.ability.list.scarecrow.FakeScareCrowEntity;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.minecraft.nms.v1_12_R1.network.EmptyNetworkHandler;
import daybreak.abilitywar.utils.base.minecraft.nms.v1_12_R1.network.EmptyNetworkManager;
import net.minecraft.server.v1_12_R1.DataWatcherObject;
import net.minecraft.server.v1_12_R1.DataWatcherRegistry;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.EnumProtocolDirection;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import net.minecraft.server.v1_12_R1.NetworkManager;
import net.minecraft.server.v1_12_R1.Packet;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntity.PacketPlayOutEntityLook;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_12_R1.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import net.minecraft.server.v1_12_R1.PlayerInteractManager;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.UUID;

public class FakeScareCrow extends EntityPlayer implements FakeScareCrowEntity {

	private static GameProfile createProfile(final String name) {
		final GameProfile profile = new GameProfile(UUID.randomUUID(), ChatColor.RED + name);
		profile.getProperties().put("textures", new Property("textures", TEXTURE, SIGNATURE));
		return profile;
	}

	private final Location location;

	public FakeScareCrow(final ScareCrow scareCrow, final MinecraftServer server, final WorldServer world, final Location location) {
		super(server, world, createProfile(scareCrow.getPlayer().getName()), new PlayerInteractManager(world));
		try {
			final NetworkManager networkManager = new EmptyNetworkManager(EnumProtocolDirection.CLIENTBOUND);
			this.playerConnection = new EmptyNetworkHandler(server, networkManager, this);
			networkManager.setPacketListener(playerConnection);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		this.location = location.clone();
		setPositionRotation(this.location.getX(), this.location.getY(), this.location.getZ(), this.location.getYaw(), this.location.getPitch());
		setHeadRotation(location.getYaw());
	}

	@Override
	public void B_() {}

	@Override
	public void playerTick() {}

	@Override
	public void display(Player player) {
		final PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
		getDataWatcher().set(new DataWatcherObject<>(13, DataWatcherRegistry.a), SKIN_BIT_LAYER);
		final byte fixedYaw = (byte) (this.location.getYaw() * (256F / 360F));
		final Packet<?>[] packets = new Packet<?>[] {
				new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, this),
				new PacketPlayOutNamedEntitySpawn(this),
				new PacketPlayOutEntityTeleport(this),
				new PacketPlayOutEntityLook(getId(), fixedYaw, (byte) (this.location.getPitch() * (256F / 360F)), this.onGround),
				new PacketPlayOutEntityHeadRotation(this, fixedYaw),
				new PacketPlayOutEntityMetadata(getId(), getDataWatcher(), true)
		};
		for (Packet<?> packet : packets) {
			playerConnection.sendPacket(packet);
		}
	}

	@Override
	public void remove() {}

	@Override
	public void hide(Player player) {
		die();
		final PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
		playerConnection.sendPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, FakeScareCrow.this));
		playerConnection.sendPacket(new PacketPlayOutEntityDestroy(getId()));
	}

	@Override
	public void lookAt(Player receiver, Location location) {
		final PlayerConnection connection = ((CraftPlayer) receiver).getHandle().playerConnection;
		final Vector look = new Vector(location.getX() - this.location.getX(), location.getY() - this.location.getY(), location.getZ() - this.location.getZ());
		final byte fixedYaw = (byte) (LocationUtil.getYaw(look) * (256F / 360F));
		connection.sendPacket(new PacketPlayOutEntityLook(getId(), fixedYaw, (byte) (LocationUtil.getPitch(look) * (256F / 360F)), this.onGround));
		connection.sendPacket(new PacketPlayOutEntityHeadRotation(this, fixedYaw));
	}
}
