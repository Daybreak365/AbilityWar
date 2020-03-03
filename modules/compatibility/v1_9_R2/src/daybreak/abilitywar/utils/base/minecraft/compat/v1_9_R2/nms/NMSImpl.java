package daybreak.abilitywar.utils.base.minecraft.compat.v1_9_R2.nms;

import daybreak.abilitywar.utils.base.minecraft.compat.nms.Hologram;
import daybreak.abilitywar.utils.base.minecraft.compat.nms.NMS;
import net.minecraft.server.v1_9_R2.IChatBaseComponent;
import net.minecraft.server.v1_9_R2.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_9_R2.MinecraftKey;
import net.minecraft.server.v1_9_R2.PacketPlayInClientCommand;
import net.minecraft.server.v1_9_R2.PacketPlayInClientCommand.EnumClientCommand;
import net.minecraft.server.v1_9_R2.PacketPlayOutChat;
import net.minecraft.server.v1_9_R2.PacketPlayOutEntity.PacketPlayOutEntityLook;
import net.minecraft.server.v1_9_R2.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_9_R2.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_9_R2.PacketPlayOutNamedSoundEffect;
import net.minecraft.server.v1_9_R2.PacketPlayOutTitle;
import net.minecraft.server.v1_9_R2.PacketPlayOutTitle.EnumTitleAction;
import net.minecraft.server.v1_9_R2.PlayerConnection;
import net.minecraft.server.v1_9_R2.SoundCategory;
import net.minecraft.server.v1_9_R2.SoundEffect;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_9_R2.CraftServer;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class NMSImpl implements NMS {

	@Override
	public void respawn(Player player) {
		((CraftPlayer) player).getHandle().playerConnection.a(new PacketPlayInClientCommand(EnumClientCommand.PERFORM_RESPAWN));
	}

	private final Map<String, SoundEffect> soundMap = new HashMap<>();

	{
		for (Sound sound : Sound.values()) {
			SoundEffect effect = SoundEffect.a.get(new MinecraftKey(sound.namespacedKey));
			if (effect != null) soundMap.put(sound.name(), effect);
		}
	}

	@Override
	public void playSound(Player player, String sound, double x, double y, double z, float volume, float pitch) {
		if (soundMap.containsKey(sound)) {
			((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutNamedSoundEffect(soundMap.get(sound), SoundCategory.MASTER, x, y, z, volume, pitch));
		}
	}

	@Override
	public void playSound(String sound, double x, double y, double z, float volume, float pitch) {
		if (soundMap.containsKey(sound)) {
			PacketPlayOutNamedSoundEffect packet = new PacketPlayOutNamedSoundEffect(soundMap.get(sound), SoundCategory.MASTER, x, y, z, volume, pitch);
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
		return ((CraftPlayer) player).getHandle().o(0f);
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
