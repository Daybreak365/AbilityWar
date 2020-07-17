package daybreak.abilitywar.utils.base.minecraft.nms;

import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface INMS {

	void respawn(Player player);

	void clearTitle(Player player);

	void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut);

	void sendActionbar(Player player, String string, int fadeIn, int stay, int fadeOut);

	float getAttackCooldown(Player player);

	void rotateHead(Player receiver, Entity entity, float yaw, float pitch);

	IHologram newHologram(World world, double x, double y, double z, String text);

	IHologram newHologram(World world, double x, double y, double z);

	float getAbsorptionHearts(Player player);

	void setAbsorptionHearts(Player player, float absorptionHearts);

	void broadcastEntityEffect(Entity entity, byte status);

	void moveEntity(Entity entity, double x, double y, double z, float yaw, float pitch, boolean onGround);

	void removeBoundingBox(ArmorStand armorStand);

}
