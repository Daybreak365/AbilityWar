package daybreak.abilitywar.utils.base.minecraft.compat;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface NMS {

	void respawn(Player player);

	void sendActionbar(Player player, String string, int fadeIn, int stay, int fadeOut);

	float getAttackCooldown(Player player);

	void rotateHead(Player receiver, Entity entity, float yaw, float pitch);

	Hologram newHologram(World world, double x, double y, double z, String text);

	Hologram newHologram(World world, double x, double y, double z);

	float getAbsorptionHearts(Player player);

	void setAbsorptionHearts(Player player, float absorptionHearts);

}
