package daybreak.abilitywar.utils.base.minecraft.compat.v1_9_R2.nms;

import daybreak.abilitywar.utils.base.minecraft.compat.nms.Sounds;
import net.minecraft.server.v1_9_R2.MinecraftKey;
import net.minecraft.server.v1_9_R2.PacketPlayOutNamedSoundEffect;
import net.minecraft.server.v1_9_R2.SoundCategory;
import net.minecraft.server.v1_9_R2.SoundEffect;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R2.CraftServer;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class SoundsImpl implements Sounds {

	private final Map<String, SoundEffect> soundMap = new HashMap<>();

	{
		for (Sound sound : Sound.values()) {
			SoundEffect effect = SoundEffect.a.get(new MinecraftKey(sound.minecraftKey));
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

}
