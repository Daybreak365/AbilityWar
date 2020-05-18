package daybreak.abilitywar.game.list.murdermystery;

import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum Items {

	MURDERER_SWORD(MaterialX.IRON_SWORD, "§4살인자§c의 검"),
	GOLD(MaterialX.GOLD_INGOT, "§6금"),
	NORMAL_BOW(MaterialX.BOW, "§e활"),
	DETECTIVE_BOW(MaterialX.BOW, "§6탐정 §e활");

	private final ItemStack stack;

	Items(MaterialX type, String displayName, String... lore) {
		this.stack = new ItemBuilder()
				.type(type)
				.displayName(displayName)
				.lore(lore)
				.unbreakable(true)
				.build();
	}

	public static boolean isMurdererSword(ItemStack stack) {
		return stack != null && stack.hasItemMeta() && stack.getType() == Material.IRON_SWORD && stack.getItemMeta().getDisplayName().equals("§4살인자§c의 검");
	}

	public static boolean isGold(ItemStack stack) {
		return stack != null && stack.hasItemMeta() && stack.getType() == Material.GOLD_INGOT && stack.getItemMeta().getDisplayName().equals("§6금");
	}

	public static boolean isDetectiveBow(ItemStack stack) {
		return stack != null && stack.hasItemMeta() && stack.getType() == Material.BOW && stack.getItemMeta().getDisplayName().equals("§6탐정 §e활");
	}

	public ItemStack getStack() {
		return stack;
	}

}
