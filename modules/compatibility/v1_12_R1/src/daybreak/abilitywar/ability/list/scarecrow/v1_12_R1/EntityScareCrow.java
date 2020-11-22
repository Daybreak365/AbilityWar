package daybreak.abilitywar.ability.list.scarecrow.v1_12_R1;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.list.scarecrow.ScareCrowEntity;
import daybreak.abilitywar.utils.base.minecraft.nms.v1_12_R1.network.EmptyNetworkHandler;
import daybreak.abilitywar.utils.base.minecraft.nms.v1_12_R1.network.EmptyNetworkManager;
import net.minecraft.server.v1_12_R1.DamageSource;
import net.minecraft.server.v1_12_R1.DataWatcherObject;
import net.minecraft.server.v1_12_R1.DataWatcherRegistry;
import net.minecraft.server.v1_12_R1.EntityHuman;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.EnumItemSlot;
import net.minecraft.server.v1_12_R1.EnumProtocolDirection;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import net.minecraft.server.v1_12_R1.Packet;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntity.PacketPlayOutEntityLook;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_12_R1.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import net.minecraft.server.v1_12_R1.PlayerInteractManager;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class  EntityScareCrow extends EntityPlayer implements ScareCrowEntity {

	private static GameProfile createProfile(final Player skinOwner) {
		final GameProfile profile = new GameProfile(UUID.randomUUID(), "§ " + skinOwner.getName());
		final PropertyMap properties = ((CraftPlayer) skinOwner).getProfile().getProperties();
		for (Property textures : properties.get("textures")) {
			profile.getProperties().put("textures", textures);
			break;
		}
		return profile;
	}

	private final EmptyNetworkManager networkManager;
	private final JavaPlugin plugin = AbilityWar.getPlugin();
	private final Location location;
	final PacketPlayOutEntityEquipment[] equipmentPackets;

	public EntityScareCrow(final ScareCrow scareCrow, final MinecraftServer server, final WorldServer world, final Location location) {
		super(server, world, createProfile(scareCrow.getPlayer()), new PlayerInteractManager(world));
		try {
			this.networkManager = new EmptyNetworkManager(EnumProtocolDirection.CLIENTBOUND);
			this.playerConnection = new EmptyNetworkHandler(server, networkManager, this);
			networkManager.setPacketListener(playerConnection);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		this.location = location.clone();
		setPositionRotation(this.location.getX(), this.location.getY(), this.location.getZ(), this.location.getYaw(), this.location.getPitch());
		world.addEntity(EntityScareCrow.this, SpawnReason.CUSTOM);
		final byte fixedYaw = (byte) (this.location.getYaw() * (256F / 360F));
		getDataWatcher().set(new DataWatcherObject<>(13, DataWatcherRegistry.a), SKIN_BIT_LAYER);
		final int id = getId();
		final PlayerInventory inventory = scareCrow.getPlayer().getInventory();
		this.equipmentPackets = new PacketPlayOutEntityEquipment[] {
				new PacketPlayOutEntityEquipment(id, EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(inventory.getItemInMainHand())),
				new PacketPlayOutEntityEquipment(id, EnumItemSlot.OFFHAND, CraftItemStack.asNMSCopy(inventory.getItemInOffHand())),
				new PacketPlayOutEntityEquipment(id, EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(inventory.getHelmet())),
				new PacketPlayOutEntityEquipment(id, EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(inventory.getChestplate())),
				new PacketPlayOutEntityEquipment(id, EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(inventory.getLeggings())),
				new PacketPlayOutEntityEquipment(id, EnumItemSlot.FEET, CraftItemStack.asNMSCopy(inventory.getBoots()))
		};
		final Packet<?>[] packets = new Packet<?>[] {
				new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, this),
				new PacketPlayOutNamedEntitySpawn(this),
				new PacketPlayOutEntityTeleport(this),
				new PacketPlayOutEntityLook(id, fixedYaw, (byte) (this.location.getPitch() * (256F / 360F)), this.onGround),
				new PacketPlayOutEntityHeadRotation(this, fixedYaw),
				new PacketPlayOutEntityMetadata(id, getDataWatcher(), true)
		};
		for (EntityHuman player : getWorld().players) {
			if (!(player instanceof EntityPlayer)) continue;
			final PlayerConnection connection = ((EntityPlayer) player).playerConnection;
			for (Packet<?> packet : packets) {
				connection.sendPacket(packet);
			}
			for (PacketPlayOutEntityEquipment packet : equipmentPackets) {
				connection.sendPacket(packet);
			}
		}
		new BukkitRunnable() {
			@Override
			public void run() {
				final PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, EntityScareCrow.this);
				for (CraftPlayer player : ((CraftServer) Bukkit.getServer()).getOnlinePlayers()) {
					player.getHandle().playerConnection.sendPacket(packet);
				}
			}
		}.runTaskLater(AbilityWar.getPlugin(), 100L);
		this.invulnerableTicks = 0;
	}

	@Override
	public void B_() {
		if (!plugin.isEnabled()) {
			die();
			return;
		}
		if (!this.isNoGravity()) {
			a(0f, 0f, 0f);
		}
		setHeadRotation(location.getYaw());
		super.B_();
		super.Y();
	}

	@Override
	public void playerTick() {}

	@Override
	public void die(DamageSource damagesource) {
		networkManager.setConnected(false);
		super.die(damagesource);
	}

	@Override
	public void die() {
		networkManager.setConnected(false);
		super.die();
	}

	@Override
	public void remove() {
		getWorld().removeEntity(this);
	}

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
		new BukkitRunnable() {
			@Override
			public void run() {
				playerConnection.sendPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, EntityScareCrow.this));
			}
		}.runTaskLater(AbilityWar.getPlugin(), 100L);
	}

}
