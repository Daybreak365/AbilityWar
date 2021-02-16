package daybreak.abilitywar.utils.base.minecraft.nms;

import daybreak.abilitywar.utils.base.minecraft.SkinInfo;
import daybreak.abilitywar.utils.base.minecraft.boundary.EntityBoundingBox;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
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

	IDummy createDummy(Location location);
	IDummy createDummy(Location location, SkinInfo skinInfo);

	float getAbsorptionHearts(LivingEntity livingEntity);
	void setAbsorptionHearts(LivingEntity livingEntity, float absorptionHearts);

	void broadcastEntityEffect(Entity entity, byte status);

	void setLocation(Entity entity, double x, double y, double z, float yaw, float pitch);

	void removeBoundingBox(ArmorStand armorStand);
	void setArrowsInBody(Player player, int count);
	void setInvisible(Entity entity, boolean invisible);
	boolean isInvisible(Entity entity);

	void setCooldown(Player player, Material material, int ticks);
	boolean hasCooldown(Player player, Material material);
	int getCooldown(Player player, Material material);

	void fakeCollect(Entity entity, Item item);

	void clearActiveItem(LivingEntity livingEntity);
	void swingHand(LivingEntity livingEntity, Hand hand);

	EntityBoundingBox getBoundingBox(Entity entity);

	void setCamera(Player receiver, Entity entity);

	IWorldBorder createWorldBorder(final World world);
	IWorldBorder createWorldBorder(org.bukkit.WorldBorder bukkit);
	void setWorldBorder(Player receiver, final IWorldBorder worldBorder);
	void resetWorldBorder(Player receiver);

	void setInGround(Arrow arrow, boolean inGround);

}
