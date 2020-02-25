package daybreak.abilitywar.utils.base.minecraft.compat.v1_13_R1;

import daybreak.abilitywar.utils.base.minecraft.compat.Hologram;
import net.minecraft.server.v1_13_R1.IChatBaseComponent;
import net.minecraft.server.v1_13_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_13_R1.PacketPlayInClientCommand;
import net.minecraft.server.v1_13_R1.PacketPlayInClientCommand.EnumClientCommand;
import net.minecraft.server.v1_13_R1.PacketPlayOutEntity.PacketPlayOutEntityLook;
import net.minecraft.server.v1_13_R1.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_13_R1.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_13_R1.PacketPlayOutTitle;
import net.minecraft.server.v1_13_R1.PacketPlayOutTitle.EnumTitleAction;
import net.minecraft.server.v1_13_R1.PlayerConnection;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class NMSImpl implements daybreak.abilitywar.utils.base.minecraft.compat.NMS {

	@Override
	public void respawn(Player player) {
		((CraftPlayer) player).getHandle().playerConnection.a(new PacketPlayInClientCommand(EnumClientCommand.PERFORM_RESPAWN));
	}

	@Override
	public void sendActionbar(Player player, String string, int fadeIn, int stay, int fadeOut) {
		PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
		IChatBaseComponent component = ChatSerializer.a("{\"text\":\"" + string + "\"}");
		connection.sendPacket(new PacketPlayOutTitle(EnumTitleAction.TIMES, component, fadeIn, stay, fadeOut));
		connection.sendPacket(new PacketPlayOutTitle(EnumTitleAction.ACTIONBAR, component, fadeIn, stay, fadeOut));
	}

	@Override
	public float getAttackCooldown(Player player) {
		return ((CraftPlayer) player).getHandle().r(0f);
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

}
