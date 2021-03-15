package daybreak.abilitywar.utils.base.minecraft.inventory;

import kotlin.NotImplementedError;
import org.bukkit.Bukkit;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Inventories {

	public static final Inventory common = Bukkit.createInventory(null, 9);

	private Inventories() {
	}

	public static void setItem(final @NotNull PlayerInventory inventory, final @NotNull EquipmentSlot slot, final @Nullable ItemStack item) {
		switch (slot) {
			case HAND:
				inventory.setItemInMainHand(item);
				break;
			case OFF_HAND:
				inventory.setItemInOffHand(item);
				break;
			case FEET:
				inventory.setBoots(item);
				break;
			case LEGS:
				inventory.setLeggings(item);
				break;
			case CHEST:
				inventory.setChestplate(item);
				break;
			case HEAD:
				inventory.setHelmet(item);
				break;
			default:
				throw new NotImplementedError();
		}
	}

	public static ItemStack getItem(final @NotNull PlayerInventory inventory, final @NotNull EquipmentSlot slot) {
		switch(slot) {
			case HAND:
				return inventory.getItemInMainHand();
			case OFF_HAND:
				return inventory.getItemInOffHand();
			case FEET:
				return inventory.getBoots();
			case LEGS:
				return inventory.getLeggings();
			case CHEST:
				return inventory.getChestplate();
			case HEAD:
				return inventory.getHelmet();
			default:
				throw new NotImplementedError();
		}
	}

	public static void setItem(final @NotNull EntityEquipment inventory, final @NotNull EquipmentSlot slot, final @Nullable ItemStack item) {
		switch(slot) {
			case HAND:
				inventory.setItemInMainHand(item);
				break;
			case OFF_HAND:
				inventory.setItemInOffHand(item);
				break;
			case FEET:
				inventory.setBoots(item);
				break;
			case LEGS:
				inventory.setLeggings(item);
				break;
			case CHEST:
				inventory.setChestplate(item);
				break;
			case HEAD:
				inventory.setHelmet(item);
				break;
			default:
				throw new NotImplementedError();
		}
	}

	public static ItemStack getItem(final @NotNull EntityEquipment inventory, final @NotNull EquipmentSlot slot) {
		switch(slot) {
			case HAND:
				return inventory.getItemInMainHand();
			case OFF_HAND:
				return inventory.getItemInOffHand();
			case FEET:
				return inventory.getBoots();
			case LEGS:
				return inventory.getLeggings();
			case CHEST:
				return inventory.getChestplate();
			case HEAD:
				return inventory.getHelmet();
			default:
				throw new NotImplementedError();
		}
	}

}
