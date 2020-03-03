package daybreak.abilitywar.utils.base.minecraft.compat.nms;

import org.bukkit.entity.Player;

public interface Sounds {

	void playSound(Player player, String sound, double x, double y, double z, float volume, float pitch);

	void playSound(String sound, double x, double y, double z, float volume, float pitch);

}
