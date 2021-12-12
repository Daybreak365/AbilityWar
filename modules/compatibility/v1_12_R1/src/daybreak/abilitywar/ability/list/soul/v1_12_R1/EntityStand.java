package daybreak.abilitywar.ability.list.soul.v1_12_R1;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.utils.base.minecraft.item.Skulls;
import net.minecraft.server.v1_12_R1.EntityArmorStand;
import net.minecraft.server.v1_12_R1.EntityHuman;
import net.minecraft.server.v1_12_R1.EnumHand;
import net.minecraft.server.v1_12_R1.EnumInteractionResult;
import net.minecraft.server.v1_12_R1.EnumItemSlot;
import net.minecraft.server.v1_12_R1.Vec3D;
import net.minecraft.server.v1_12_R1.World;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class EntityStand extends EntityArmorStand {

	private final JavaPlugin plugin = AbilityWar.getPlugin();
	private final EntityGhost ghost;

	EntityStand(World world, EntityGhost ghost, Location location, Color color) {
		super(world, location.getX(), location.getY(), location.getZ());
		this.ghost = ghost;
		setNoGravity(true);
		setInvisible(true);
		setMarker(true);
		setSlot(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(Skulls.createCustomSkull("21db11f0e3b0672a18a63a71ffa0952451c4df9580f1145a6db01fe6e9077cac")));
		final org.bukkit.inventory.ItemStack chestplate = new org.bukkit.inventory.ItemStack(Material.LEATHER_CHESTPLATE);
		final LeatherArmorMeta meta = (LeatherArmorMeta) chestplate.getItemMeta();
		meta.setColor(color);
		chestplate.setItemMeta(meta);
		setSlot(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(chestplate));
		world.addEntity(this);
	}

	@Override
	public void B_() {
		if (!plugin.isEnabled()) {
			die();
			return;
		}
		setHeadRotation(ghost.getHeadRotation());
		if (!ghost.passengers.contains(this)) {
			this.a(ghost, true);
		}
		this.fireTicks = Integer.MAX_VALUE;
		super.B_();
	}

	@Override
	public EnumInteractionResult a(EntityHuman entityhuman, Vec3D vec3d, EnumHand enumhand) {
		return EnumInteractionResult.PASS;
	}

}
