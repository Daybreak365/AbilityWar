package daybreak.abilitywar.utils.base.minecraft.compat.nms;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public interface NMS {

	void respawn(Player player);

	void clearTitle(Player player);

	void spawnParticle(Player player, String particle, float x, float y, float z, float offsetX, float offsetY, float offsetZ, float speed, int amount, int... data);

	void spawnParticle(String particle, float x, float y, float z, float offsetX, float offsetY, float offsetZ, float speed, int amount, int... data);

	void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut);

	void sendActionbar(Player player, String string, int fadeIn, int stay, int fadeOut);

	float getAttackCooldown(Player player);

	void rotateHead(Player receiver, Entity entity, float yaw, float pitch);

	Hologram newHologram(World world, double x, double y, double z, String text);

	Hologram newHologram(World world, double x, double y, double z);

	float getAbsorptionHearts(Player player);

	void setAbsorptionHearts(Player player, float absorptionHearts);

	double getSpeed(LivingEntity entity);

	void setSpeed(LivingEntity entity, double speed);

}
