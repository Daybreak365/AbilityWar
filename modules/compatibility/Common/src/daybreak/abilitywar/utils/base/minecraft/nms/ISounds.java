package daybreak.abilitywar.utils.base.minecraft.nms;

import org.bukkit.entity.Player;

public interface ISounds {

	void playSound(Player player, String sound, double x, double y, double z, float volume, float pitch);

	void playSound(String sound, double x, double y, double z, float volume, float pitch);

}
