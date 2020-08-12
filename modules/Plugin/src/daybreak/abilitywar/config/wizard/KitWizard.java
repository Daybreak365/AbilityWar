package daybreak.abilitywar.config.wizard;

import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.config.kitpreset.KitConfiguration;
import daybreak.abilitywar.config.kitpreset.KitConfiguration.KitSettings;
import daybreak.abilitywar.config.kitpreset.KitNodes;
import daybreak.abilitywar.config.serializable.KitPreset;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.item.ItemBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KitWizard extends SettingWizard {

	private static final ItemStack RESET = new ItemBuilder()
			.type(MaterialX.RED_WOOL)
			.displayName(ChatColor.RED + "초기화")
			.build();

	private static final ItemStack BOOTS = new ItemBuilder()
			.type(MaterialX.BLACK_STAINED_GLASS_PANE)
			.displayName(ChatColor.AQUA + "부츠")
			.lore(
					"§7부츠 아이템을 변경하려면 아이템을 들고",
					"§7여기를 클릭하세요."
			)
			.build();

	private static final ItemStack LEGGINGS = new ItemBuilder()
			.type(MaterialX.BLACK_STAINED_GLASS_PANE)
			.displayName(ChatColor.AQUA + "각반")
			.lore(
					"§7각반 아이템을 변경하려면 아이템을 들고",
					"§7여기를 클릭하세요."
			)
			.build();

	private static final ItemStack CHESTPLATE = new ItemBuilder()
			.type(MaterialX.BLACK_STAINED_GLASS_PANE)
			.displayName(ChatColor.AQUA + "흉갑")
			.lore(
					"§7흉갑 아이템을 변경하려면 아이템을 들고",
					"§7여기를 클릭하세요."
			)
			.build();

	private static final ItemStack HELMET = new ItemBuilder()
			.type(MaterialX.BLACK_STAINED_GLASS_PANE)
			.displayName(ChatColor.AQUA + "투구")
			.lore(
					"§7투구 아이템을 변경하려면 아이템을 들고",
					"§7여기를 클릭하세요."
			)
			.build();

	private static final ItemStack DECO = new ItemBuilder()
			.type(MaterialX.WHITE_STAINED_GLASS_PANE)
			.displayName("§f")
			.build();

	@Nullable
	private final AbilityRegistration registration;
	private final KitPreset kitPreset;

	public KitWizard(Player player, Plugin plugin) {
		super(player, 54, "§2§l게임 기본 아이템 설정", plugin);
		this.registration = null;
		this.kitPreset = KitSettings.getKit();
	}

	public KitWizard(Player player, Plugin plugin, @NotNull AbilityRegistration registration) {
		super(player, 54, "§2§l" + registration.getManifest().name() + " 기본 아이템 설정", plugin);
		this.registration = registration;
		this.kitPreset = KitSettings.getAbilityKit().getKits(registration.getAbilityClass().getName());
	}

	void openGUI(final Inventory gui) {
		gui.setItem(0, kitPreset.getHelmet() != null ? kitPreset.getHelmet() : HELMET);
		gui.setItem(1, kitPreset.getChestplate() != null ? kitPreset.getChestplate() : CHESTPLATE);
		gui.setItem(2, kitPreset.getLeggings() != null ? kitPreset.getLeggings() : LEGGINGS);
		gui.setItem(3, kitPreset.getBoots() != null ? kitPreset.getBoots() : BOOTS);
		for (int i = 4; i <= 8; i++) gui.setItem(i, DECO);
		for (int i = 45; i <= 52; i++) gui.setItem(i, DECO);
		gui.setItem(53, RESET);
		gui.addItem(kitPreset.getItems().toArray(new ItemStack[0]));
		player.openInventory(gui);
	}

	@Override
	void onUnregister(final Inventory gui) {
		final List<ItemStack> stacks = new ArrayList<>();
		for (int i = 9; i <= 44; i++) {
			final ItemStack stack = gui.getItem(i);
			if (!isEmpty(stack)) {
				stacks.add(stack);
			}
		}
		kitPreset.setItems(stacks);
		if (registration == null) {
			KitConfiguration.getInstance().updateProperty(KitNodes.KIT);
		} else {
			KitSettings.getAbilityKit().setKits(registration.getAbilityClass().getName(), kitPreset);
			KitConfiguration.getInstance().updateProperty(KitNodes.ABILITY_KIT);
		}
		try {
			KitConfiguration.getInstance().update();
		} catch (IOException | InvalidConfigurationException e1) {
			logger.log(Level.SEVERE, "콘피그를 업데이트하는 도중 오류가 발생하였습니다.");
		}
	}

	@Override
	void onClick(InventoryClickEvent e, Inventory gui) {
		if (gui.equals(e.getInventory())) {
			final Inventory inventory = e.getClickedInventory();
			if (!gui.equals(e.getClickedInventory())) return;
			if (e.getSlot() == 0) {
				e.setCancelled(true);
				if (MaterialX.BLACK_STAINED_GLASS_PANE.compare(e.getCursor())) return;
				if (MaterialX.BLACK_STAINED_GLASS_PANE.compare(inventory.getItem(0))) {
					if (isEmpty(e.getCursor())) return;
					kitPreset.setHelmet(e.getCursor());
					inventory.setItem(0, e.getCursor());
					e.getWhoClicked().setItemOnCursor(null);
				} else {
					final ItemStack cursor = e.getCursor();
					kitPreset.setHelmet(cursor);
					e.getWhoClicked().setItemOnCursor(inventory.getItem(0));
					inventory.setItem(0, !isEmpty(cursor) ? cursor : HELMET);
				}
				KitConfiguration.getInstance().updateProperty(KitNodes.KIT);
			} else if (e.getSlot() == 1) {
				e.setCancelled(true);
				if (MaterialX.BLACK_STAINED_GLASS_PANE.compare(e.getCursor())) return;
				if (MaterialX.BLACK_STAINED_GLASS_PANE.compare(inventory.getItem(1))) {
					if (isEmpty(e.getCursor())) return;
					kitPreset.setChestplate(e.getCursor());
					inventory.setItem(1, e.getCursor());
					e.getWhoClicked().setItemOnCursor(null);
				} else {
					final ItemStack cursor = e.getCursor();
					kitPreset.setChestplate(cursor);
					e.getWhoClicked().setItemOnCursor(inventory.getItem(1));
					inventory.setItem(1, !isEmpty(cursor) ? cursor : CHESTPLATE);
				}
				KitConfiguration.getInstance().updateProperty(KitNodes.KIT);
			} else if (e.getSlot() == 2) {
				e.setCancelled(true);
				if (MaterialX.BLACK_STAINED_GLASS_PANE.compare(e.getCursor())) return;
				if (MaterialX.BLACK_STAINED_GLASS_PANE.compare(inventory.getItem(2))) {
					if (isEmpty(e.getCursor())) return;
					kitPreset.setLeggings(e.getCursor());
					inventory.setItem(2, e.getCursor());
					e.getWhoClicked().setItemOnCursor(null);
				} else {
					final ItemStack cursor = e.getCursor();
					kitPreset.setLeggings(cursor);
					e.getWhoClicked().setItemOnCursor(inventory.getItem(2));
					inventory.setItem(2, !isEmpty(cursor) ? cursor : LEGGINGS);
				}
				KitConfiguration.getInstance().updateProperty(KitNodes.KIT);
			} else if (e.getSlot() == 3) {
				e.setCancelled(true);
				if (MaterialX.BLACK_STAINED_GLASS_PANE.compare(e.getCursor())) return;
				if (MaterialX.BLACK_STAINED_GLASS_PANE.compare(inventory.getItem(3))) {
					if (isEmpty(e.getCursor())) return;
					kitPreset.setBoots(e.getCursor());
					inventory.setItem(3, e.getCursor());
					e.getWhoClicked().setItemOnCursor(null);
				} else {
					final ItemStack cursor = e.getCursor();
					kitPreset.setBoots(cursor);
					e.getWhoClicked().setItemOnCursor(inventory.getItem(3));
					inventory.setItem(3, !isEmpty(cursor) ? cursor : BOOTS);
				}
				KitConfiguration.getInstance().updateProperty(KitNodes.KIT);
			} else if ((e.getSlot() >= 4 && e.getSlot() <= 8) || (e.getSlot() >= 45 && e.getSlot() <= 52)) {
				e.setCancelled(true);
			} else if (e.getSlot() == 53) {
				e.setCancelled(true);
				KitConfiguration.getInstance().modifyProperty(KitNodes.KIT, new KitPreset());
				show();
			}
		}
	}

	private boolean isEmpty(final ItemStack stack) {
		return stack == null || stack.getType() == Material.AIR;
	}

}
