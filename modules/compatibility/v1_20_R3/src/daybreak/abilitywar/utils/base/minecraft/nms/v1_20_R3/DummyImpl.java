package daybreak.abilitywar.utils.base.minecraft.nms.v1_20_R3;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.utils.base.minecraft.SkinInfo;
import daybreak.abilitywar.utils.base.minecraft.nms.IDummy;
import daybreak.abilitywar.utils.base.minecraft.nms.IHologram;
import daybreak.abilitywar.utils.base.minecraft.nms.v1_20_R3.network.EmptyNetworkHandler;
import daybreak.abilitywar.utils.base.minecraft.nms.v1_20_R3.network.EmptyNetworkManager;
import net.minecraft.network.protocol.EnumProtocolDirection;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkProviderServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.level.EnumGamemode;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R3.CraftServer;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
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

	public DummyImpl(final MinecraftServer server, final WorldServer world, final Location location, final SkinInfo skinInfo) {
		super(server, world, createProfile(skinInfo), ClientInformation.createDefault());
		this.gameMode.changeGameModeForPlayer(EnumGamemode.SURVIVAL);
		this.hologram = new HologramImpl(world.getWorld(), ((Entity) this).getX(), ((Entity) this).getY() + 2, ((Entity) this).getZ(), IDLE_MESSAGE);
		try {
			final EmptyNetworkManager emptyManager = new EmptyNetworkManager(EnumProtocolDirection.CLIENTBOUND);
			this.connection = new EmptyNetworkHandler(server, emptyManager, this);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		((Entity) this).getEntityData().set(new DataWatcherObject<>(17, DataWatcherRegistry.BYTE), SKIN_BIT_LAYER);
		new BukkitRunnable() {
			@Override
			public void run() {
				world.addFreshEntity(DummyImpl.this, SpawnReason.CUSTOM);
			}
		}.runTaskLater(AbilityWar.getPlugin(), 2L);
		((Entity) this).setPos(location.getX(), location.getY(), location.getZ());
		this.spawnInvulnerableTime = 0;
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
		hologram.teleport(((World) ((Entity) this).level()).getWorld(), ((Entity) this).getX(), ((Entity) this).getY() + 2, ((Entity) this).getZ(), ((Entity) this).getYRot(), ((Entity) this).getXRot());
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
		final PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().connection;
		((ServerPlayerConnection) playerConnection).send(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.a.ADD_PLAYER, this));
		((ServerPlayerConnection) playerConnection).send(new PacketPlayOutSpawnEntity(this));
		hologram.display(player);
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
	public UUID getUniqueID() {
		return ((Entity) this).getUUID();
	}

	@Override
	public void remove() {
		((Entity) this).discard();
		((ChunkProviderServer) ((GeneratorAccess) ((Entity) this).level()).getChunkSource()).removeEntity(this);
		if (!hologram.isUnregistered()) {
			hologram.unregister();
		}
		final PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(((Entity) this).getId());
		for (CraftPlayer player : ((CraftServer) Bukkit.getServer()).getOnlinePlayers()) {
			((ServerPlayerConnection) player.getHandle().connection).send(packet);
		}
	}

	@Override
	public boolean isAlive() {
		return ((EntityLiving) this).isAlive();
	}
}
