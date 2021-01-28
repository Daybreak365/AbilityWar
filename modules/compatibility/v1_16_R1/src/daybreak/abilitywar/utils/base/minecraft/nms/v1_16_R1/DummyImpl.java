package daybreak.abilitywar.utils.base.minecraft.nms.v1_16_R1;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.utils.base.minecraft.SkinInfo;
import daybreak.abilitywar.utils.base.minecraft.nms.IDummy;
import daybreak.abilitywar.utils.base.minecraft.nms.IHologram;
import daybreak.abilitywar.utils.base.minecraft.nms.v1_16_R1.network.EmptyNetworkHandler;
import daybreak.abilitywar.utils.base.minecraft.nms.v1_16_R1.network.EmptyNetworkManager;
import net.minecraft.server.v1_16_R1.DataWatcherObject;
import net.minecraft.server.v1_16_R1.DataWatcherRegistry;
import net.minecraft.server.v1_16_R1.EntityPlayer;
import net.minecraft.server.v1_16_R1.EnumGamemode;
import net.minecraft.server.v1_16_R1.EnumProtocolDirection;
import net.minecraft.server.v1_16_R1.MinecraftServer;
import net.minecraft.server.v1_16_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_16_R1.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_16_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_16_R1.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.server.v1_16_R1.PlayerConnection;
import net.minecraft.server.v1_16_R1.PlayerInteractManager;
import net.minecraft.server.v1_16_R1.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.CraftServer;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class DummyImpl extends EntityPlayer implements IDummy {

	private static GameProfile createProfile(final SkinInfo skinInfo) {
		final GameProfile profile = new GameProfile(UUID.randomUUID(), skinInfo.getName());
		profile.getProperties().put("textures", new Property("textures", skinInfo.getValue(), skinInfo.getSignature()));
		return profile;
	}

	private final JavaPlugin plugin = AbilityWar.getPlugin();
	private final IHologram hologram;
	private double elapsedSeconds = 1, damages = 0;
	private int untilReset = -1;
	private final EmptyNetworkManager networkManager;

	public DummyImpl(final MinecraftServer server, final WorldServer world, final Location location, final SkinInfo skinInfo) {
		super(server, world, createProfile(skinInfo), new PlayerInteractManager(world));
		this.playerInteractManager.setGameMode(EnumGamemode.SURVIVAL);
		try {
			this.networkManager = new EmptyNetworkManager(EnumProtocolDirection.CLIENTBOUND);
			this.playerConnection = new EmptyNetworkHandler(server, networkManager, this);
			networkManager.setPacketListener(playerConnection);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		getDataWatcher().set(new DataWatcherObject<>(13, DataWatcherRegistry.a), SKIN_BIT_LAYER);
		new BukkitRunnable() {
			@Override
			public void run() {
				world.addEntity(DummyImpl.this, SpawnReason.CUSTOM);
			}
		}.runTaskLater(AbilityWar.getPlugin(), 2L);
		setPosition(location.getX(), location.getY(), location.getZ());
		this.invulnerableTicks = 0;
		this.hologram = new HologramImpl(world.getWorld(), locX(), locY() + 2, locZ(), IDLE_MESSAGE);
	}

	public DummyImpl(final MinecraftServer server, final WorldServer world, final Location location) {
		this(server, world, location, DEFAULT_SKIN);
	}

	@Override
	public void tick() {
		if (!isAlive() || hologram.isUnregistered() || !plugin.isEnabled()) {
			remove();
			return;
		}
		hologram.teleport(getWorld().getWorld(), locX(), locY() + 2, locZ(), yaw, pitch);
		if (untilReset > 0) {
			elapsedSeconds += .05;
			if (--untilReset <= 0) {
				this.untilReset = -1;
				this.elapsedSeconds = 1;
				this.damages = 0;
			}
		}
		super.tick();
	}

	@Override
	public void addDamage(final double damage) {
		this.untilReset = TICKS_TO_RESET;
		this.damages += damage;
		updateHologram();
	}

	@Override
	public void display(final Player player) {
		hologram.display(player);
		final PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
		playerConnection.sendPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, this));
		playerConnection.sendPacket(new PacketPlayOutNamedEntitySpawn(this));
		new BukkitRunnable() {
			@Override
			public void run() {
				playerConnection.sendPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, DummyImpl.this));
			}
		}.runTaskLater(AbilityWar.getPlugin(), 50L);
	}

	private void updateHologram() {
		if (hologram.isUnregistered()) {
			return;
		}
		if (untilReset > 0) {
			hologram.setText("§7DPS§8: §f" + floor(damages / elapsedSeconds) + " §f| §7합계§8: §f" + floor(damages));
		} else {
			hologram.setText(IDLE_MESSAGE);
		}
	}

	private double floor(final double a) {
		return Math.floor(a * 1000) / 1000;
	}

	@Override
	public CraftPlayer getBukkitEntity() {
		return super.getBukkitEntity();
	}

	@Override
	public void remove() {
		die();
		((WorldServer) getWorld()).removeEntity(this);
		if (!hologram.isUnregistered()) {
			hologram.unregister();
		}
		final PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(getId());
		for (CraftPlayer player : ((CraftServer) Bukkit.getServer()).getOnlinePlayers()) {
			player.getHandle().playerConnection.sendPacket(packet);
		}
	}

}
