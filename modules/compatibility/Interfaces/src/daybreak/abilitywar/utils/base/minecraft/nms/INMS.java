package daybreak.abilitywar.utils.base.minecraft.nms;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
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

	void setLocation(Entity entity, double x, double y, double z, float yaw, float pitch);

	void removeBoundingBox(ArmorStand armorStand);
	void removeArrow(Player player);
	void setInvisible(Player player, boolean invisible);

	void setCooldown(Player player, Material material, int ticks);
	boolean hasCooldown(Player player, Material material);

	void fakeCollect(Entity entity, Item item);

	void clearActiveItem(LivingEntity livingEntity);

}
