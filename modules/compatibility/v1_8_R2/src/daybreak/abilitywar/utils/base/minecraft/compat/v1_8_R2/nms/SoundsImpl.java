package daybreak.abilitywar.utils.base.minecraft.compat.v1_8_R2.nms;

import daybreak.abilitywar.utils.base.minecraft.compat.nms.Sounds;
import net.minecraft.server.v1_8_R2.PacketPlayOutNamedSoundEffect;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R2.CraftServer;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class SoundsImpl implements Sounds {

	private final Map<String, String> soundMap = new HashMap<>();

	{
		for (Sound sound : Sound.values()) {
			soundMap.put(sound.name(), sound.minecraftKey);
		}
	}

	@Override
	public void playSound(Player player, String sound, double x, double y, double z, float volume, float pitch) {
		if (soundMap.containsKey(sound)) {
			((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutNamedSoundEffect(soundMap.get(sound), x, y, z, volume, pitch));
		}
	}

	@Override
	public void playSound(String sound, double x, double y, double z, float volume, float pitch) {
		PacketPlayOutNamedSoundEffect packet = new PacketPlayOutNamedSoundEffect(soundMap.get(sound), x, y, z, volume, pitch);
		for (CraftPlayer player : ((CraftServer) Bukkit.getServer()).getOnlinePlayers()) {
			player.getHandle().playerConnection.sendPacket(packet);
		}
	}

}
