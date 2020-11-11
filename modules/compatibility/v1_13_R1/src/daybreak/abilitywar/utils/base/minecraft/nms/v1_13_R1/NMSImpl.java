package daybreak.abilitywar.utils.base.minecraft.nms.v1_13_R1;

import daybreak.abilitywar.utils.base.minecraft.nms.IDummy;
import daybreak.abilitywar.utils.base.minecraft.nms.IHologram;
import daybreak.abilitywar.utils.base.minecraft.nms.INMS;
import net.minecraft.server.v1_13_R1.DataWatcherObject;
import net.minecraft.server.v1_13_R1.DataWatcherRegistry;
import net.minecraft.server.v1_13_R1.EntityArmorStand;
import net.minecraft.server.v1_13_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_13_R1.ItemCooldown;
import net.minecraft.server.v1_13_R1.ItemCooldown.Info;
import net.minecraft.server.v1_13_R1.PacketPlayInClientCommand;
import net.minecraft.server.v1_13_R1.PacketPlayInClientCommand.EnumClientCommand;
import net.minecraft.server.v1_13_R1.PacketPlayOutCollect;
import net.minecraft.server.v1_13_R1.PacketPlayOutEntity.PacketPlayOutEntityLook;
import net.minecraft.server.v1_13_R1.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_13_R1.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_13_R1.PacketPlayOutTitle;
import net.minecraft.server.v1_13_R1.PacketPlayOutTitle.EnumTitleAction;
import net.minecraft.server.v1_13_R1.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_13_R1.CraftServer;
import org.bukkit.craftbukkit.v1_13_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_13_R1.util.CraftMagicNumbers;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class NMSImpl implements INMS {

	@Override
	public void respawn(Player player) {
		((CraftPlayer) player).getHandle().playerConnection.a(new PacketPlayInClientCommand(EnumClientCommand.PERFORM_RESPAWN));
	}

	@Override
	public void clearTitle(Player player) {
		final PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
		if (connection == null) return;
		connection.sendPacket(new PacketPlayOutTitle(EnumTitleAction.CLEAR, null));
	}

	@Override
	public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
		player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
	}

	@Override
	public void sendActionbar(Player player, String string, int fadeIn, int stay, int fadeOut) {
		final PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
		if (connection == null) return;
		connection.sendPacket(new PacketPlayOutTitle(fadeIn, stay, fadeOut));
		connection.sendPacket(new PacketPlayOutTitle(EnumTitleAction.ACTIONBAR, ChatSerializer.a("{\"text\":\"" + string + "\"}"), fadeIn, stay, fadeOut));
	}

	@Override
	public float getAttackCooldown(Player player) {
		return ((CraftPlayer) player).getHandle().r(0f);
	}

	@Override
	public void rotateHead(Player receiver, Entity entity, float yaw, float pitch) {
		final PlayerConnection connection = ((CraftPlayer) receiver).getHandle().playerConnection;
		if (connection == null) return;
		connection.sendPacket(new PacketPlayOutEntityTeleport(((CraftEntity) entity).getHandle()));
		final byte fixedYaw = (byte) (yaw * (256F / 360F));
		connection.sendPacket(new PacketPlayOutEntityLook(entity.getEntityId(), fixedYaw, (byte) (pitch * (256F / 360F)), entity.isOnGround()));
		connection.sendPacket(new PacketPlayOutEntityHeadRotation(((CraftEntity) entity).getHandle(), fixedYaw));
	}

	@Override
	public IHologram newHologram(World world, double x, double y, double z, String text) {
		return new HologramImpl(world, x, y, z, text);
	}

	@Override
	public IHologram newHologram(World world, double x, double y, double z) {
		return new HologramImpl(world, x, y, z);
	}

	@Override
	public IDummy createDummy(Location location) {
		return new DummyImpl(((CraftServer) Bukkit.getServer()).getServer(), ((CraftWorld) location.getWorld()).getHandle(), location);
	}

	@Override
	public float getAbsorptionHearts(LivingEntity livingEntity) {
		return ((CraftLivingEntity) livingEntity).getHandle().getAbsorptionHearts();
	}

	@Override
	public void setAbsorptionHearts(LivingEntity livingEntity, float absorptionHearts) {
		((CraftLivingEntity) livingEntity).getHandle().setAbsorptionHearts(absorptionHearts);
	}

	@Override
	public void broadcastEntityEffect(Entity entity, byte status) {
		final net.minecraft.server.v1_13_R1.Entity nmsEntity = ((CraftEntity) entity).getHandle();
		nmsEntity.getWorld().broadcastEntityEffect(nmsEntity, status);
	}

	@Override
	public void setLocation(Entity entity, double x, double y, double z, float yaw, float pitch) {
		((CraftEntity) entity).getHandle().setLocation(x, y, z, yaw, pitch);
	}

	@Override
	public void removeBoundingBox(ArmorStand armorStand) {
		final EntityArmorStand nmsArmorStand = ((CraftArmorStand) armorStand).getHandle();
		nmsArmorStand.getDataWatcher().set(EntityArmorStand.a, (byte) (nmsArmorStand.getDataWatcher().get(EntityArmorStand.a) | 16));
		nmsArmorStand.setSize(0F, 0F);
	}

	@Override
	public void setArrowsInBody(Player player, int count) {
		if (count < 0) throw new IllegalStateException("count cannot be negative.");
		((CraftPlayer) player).getHandle().getDataWatcher().set(new DataWatcherObject<>(10, DataWatcherRegistry.b), count);
	}

	@Override
	public void setInvisible(Entity entity, boolean invisible) {
		((CraftEntity) entity).getHandle().setInvisible(invisible);
	}

	@Override
	public boolean isInvisible(Entity entity) {
		return ((CraftEntity) entity).getHandle().isInvisible();
	}

	@Override
	public void setCooldown(Player player, Material material, int ticks) {
		((CraftPlayer) player).getHandle().getCooldownTracker().a(CraftMagicNumbers.getItem(material), ticks);
	}

	@Override
	public boolean hasCooldown(Player player, Material material) {
		return ((CraftPlayer) player).getHandle().getCooldownTracker().a(CraftMagicNumbers.getItem(material));
	}

	@Override
	public int getCooldown(Player player, Material material) {
		final ItemCooldown cooldownTracker = ((CraftPlayer) player).getHandle().getCooldownTracker();
		final Info cooldown = cooldownTracker.cooldowns.get(CraftMagicNumbers.getItem(material));
		return cooldown == null ? 0 : Math.max(0, cooldown.endTick - cooldownTracker.currentTick);
	}

	@Override
	public void fakeCollect(Entity entity, Item item) {
		final PacketPlayOutCollect packet = new PacketPlayOutCollect(item.getEntityId(), entity.getEntityId(), item.getItemStack().getAmount());
		for (final CraftPlayer player : ((CraftServer) Bukkit.getServer()).getOnlinePlayers()) {
			final PlayerConnection connection = player.getHandle().playerConnection;
			if (connection == null) continue;
			connection.sendPacket(packet);
		}
	}

	@Override
	public void clearActiveItem(LivingEntity livingEntity) {
		((CraftLivingEntity) livingEntity).getHandle().clearActiveItem();
	}

}
