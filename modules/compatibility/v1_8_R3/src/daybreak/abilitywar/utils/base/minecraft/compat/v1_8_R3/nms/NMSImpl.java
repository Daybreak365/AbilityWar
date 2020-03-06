package daybreak.abilitywar.utils.base.minecraft.compat.v1_8_R3.nms;

import daybreak.abilitywar.utils.base.minecraft.compat.nms.Hologram;
import daybreak.abilitywar.utils.base.minecraft.compat.nms.NMS;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.GenericAttributes;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayInClientCommand;
import net.minecraft.server.v1_8_R3.PacketPlayInClientCommand.EnumClientCommand;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntity.PacketPlayOutEntityLook;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle.EnumTitleAction;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class NMSImpl implements NMS {

	@Override
	public void respawn(Player player) {
		((CraftPlayer) player).getHandle().playerConnection.a(new PacketPlayInClientCommand(EnumClientCommand.PERFORM_RESPAWN));
	}

	private final Map<String, EnumParticle> particleMap = new HashMap<>();

	{
		for (EnumParticle particle : EnumParticle.values()) {
			particleMap.put(particle.name(), particle);
		}
	}

	@Override
	public void spawnParticle(Player player, String particle, float x, float y, float z, float offsetX, float offsetY, float offsetZ, float speed, int amount, int... data) {
		if (particleMap.containsKey(particle)) {
			((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutWorldParticles(particleMap.get(particle), true, x, y, z, offsetX, offsetY, offsetZ, speed, amount, data));
		}
	}

	@Override
	public void spawnParticle(String particle, float x, float y, float z, float offsetX, float offsetY, float offsetZ, float speed, int amount, int... data) {
		if (particleMap.containsKey(particle)) {
			PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(particleMap.get(particle), true, x, y, z, offsetX, offsetY, offsetZ, speed, amount, data);
			for (CraftPlayer player : ((CraftServer) Bukkit.getServer()).getOnlinePlayers()) {
				player.getHandle().playerConnection.sendPacket(packet);
			}
		}
	}

	@Override
	public void clearTitle(Player player) {
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutTitle(EnumTitleAction.CLEAR, null));
	}

	@Override
	public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
		PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
		connection.sendPacket(new PacketPlayOutTitle(fadeIn, stay, fadeOut));
		connection.sendPacket(new PacketPlayOutTitle(EnumTitleAction.TITLE, ChatSerializer.a("{\"text\":\"" + title + "\"}"), fadeIn, stay, fadeOut));
		connection.sendPacket(new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, ChatSerializer.a("{\"text\":\"" + subtitle + "\"}"), fadeIn, stay, fadeOut));
	}

	@Override
	public void sendActionbar(Player player, String string, int fadeIn, int stay, int fadeOut) {
		PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
		IChatBaseComponent component = ChatSerializer.a("{\"text\":\"" + string + "\"}");
		connection.sendPacket(new PacketPlayOutTitle(fadeIn, stay, fadeOut));
		connection.sendPacket(new PacketPlayOutChat(component, (byte) 2));
	}

	@Override
	public float getAttackCooldown(Player player) {
		return 1;
	}

	@Override
	public void rotateHead(Player receiver, Entity entity, float yaw, float pitch) {
		PlayerConnection connection = ((CraftPlayer) receiver).getHandle().playerConnection;
		connection.sendPacket(new PacketPlayOutEntityTeleport(((CraftEntity) entity).getHandle()));
		byte fixedYaw = (byte) (yaw * (256F / 360F));
		connection.sendPacket(new PacketPlayOutEntityLook(entity.getEntityId(), fixedYaw, (byte) (pitch * (256F / 360F)), entity.isOnGround()));
		connection.sendPacket(new PacketPlayOutEntityHeadRotation(((CraftEntity) entity).getHandle(), fixedYaw));
	}

	@Override
	public Hologram newHologram(World world, double x, double y, double z, String text) {
		return new HologramImpl(world, x, y, z, text);
	}

	@Override
	public Hologram newHologram(World world, double x, double y, double z) {
		return new HologramImpl(world, x, y, z);
	}

	@Override
	public float getAbsorptionHearts(Player player) {
		return ((CraftPlayer) player).getHandle().getAbsorptionHearts();
	}

	@Override
	public void setAbsorptionHearts(Player player, float absorptionHearts) {
		((CraftPlayer) player).getHandle().setAbsorptionHearts(absorptionHearts);
	}

	@Override
	public double getSpeed(LivingEntity entity) {
		return ((CraftLivingEntity) entity).getHandle().getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue();
	}

	@Override
	public void setSpeed(LivingEntity entity, double speed) {
		((CraftLivingEntity) entity).getHandle().getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
	}

}
