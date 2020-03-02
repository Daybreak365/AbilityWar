package daybreak.abilitywar.config.wizard;

import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.Configuration.Settings.InvincibilitySettings;
import daybreak.abilitywar.config.enums.ConfigNodes;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.TimeUtil;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.item.ItemLib;
import daybreak.abilitywar.utils.library.item.ItemLib.ItemColor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class InvincibilityWizard extends SettingWizard {

	public InvincibilityWizard(Player player, Plugin plugin) {
		super(player, 27, ChatColor.translateAlternateColorCodes('&', "&2&l초반 무적 설정"), plugin);
	}

	private Unit unit = Unit.MINUTE;

	private enum Unit {

		SECOND("초", 1) {
			@Override
			public Unit getNext() {
				return Unit.MINUTE;
			}
		},
		MINUTE("분", 60) {
			@Override
			public Unit getNext() {
				return Unit.HOUR;
			}
		},
		HOUR("시간", 3600) {
			@Override
			public Unit getNext() {
				return Unit.SECOND;
			}
		};

		private final String name;
		private final int unit;

		Unit(String name, int unit) {
			this.name = name;
			this.unit = unit;
		}

		public String getName() {
			return name;
		}

		public int getUnit() {
			return unit;
		}

		public abstract Unit getNext();

	}

	@Override
	void openGUI(Inventory gui) {
		ItemStack deco = MaterialX.WHITE_STAINED_GLASS_PANE.parseItem();
		ItemMeta decoMeta = deco.getItemMeta();
		decoMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&f"));
		deco.setItemMeta(decoMeta);

		for (int i = 0; i < 27; i++) {
			if (i == 11) {
				boolean isEnabled = Settings.InvincibilitySettings.isEnabled();
				ItemColor color = isEnabled ? ItemColor.LIME : ItemColor.RED;
				ItemStack stack = ItemLib.WOOL.getItemStack(color);
				ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b초반 무적"));
				meta.setLore(Messager.asList(ChatColor.translateAlternateColorCodes('&',
						"&7상태 : " + (isEnabled ? "&a활성화" : "&c비활성화"))));
				stack.setItemMeta(meta);

				gui.setItem(i, stack);
			} else if (i == 14) {
				ItemStack stack = MaterialX.CLOCK.parseItem();
				ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b초반 무적 시간"));
				meta.setLore(Messager.asList(
						ChatColor.translateAlternateColorCodes('&', "&7지속 시간 : &a" + TimeUtil.parseTimeAsString(InvincibilitySettings.getDuration())),
						" ",
						ChatColor.translateAlternateColorCodes('&', "&c우클릭         &6» &e+ 1" + unit.getName()),
						ChatColor.translateAlternateColorCodes('&', "&cSHIFT + 우클릭 &6» &e+ 5" + unit.getName()),
						ChatColor.translateAlternateColorCodes('&', "&c좌클릭         &6» &e- 1" + unit.getName()),
						ChatColor.translateAlternateColorCodes('&', "&cSHIFT + 좌클릭 &6» &e- 5" + unit.getName())));
				stack.setItemMeta(meta);

				gui.setItem(i, stack);
			} else if (i == 15) {
				ItemStack stack = MaterialX.PAPER.parseItem();
				ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b단위"));

				List<String> lore = Messager.asList(ChatColor.translateAlternateColorCodes('&', "&f초반 무적 시간을 조정할 때 어떤 단위로 조정할지 설정합니다."), "");
				for (Unit unit : Unit.values()) {
					lore.add(ChatColor.translateAlternateColorCodes('&', (unit.equals(this.unit) ? "&a" : "&7") + unit.getName()));
				}
				meta.setLore(lore);
				stack.setItemMeta(meta);

				gui.setItem(i, stack);
			} else {
				gui.setItem(i, deco);
			}
		}

		player.openInventory(gui);
	}

	@Override
	void onClick(InventoryClickEvent e, Inventory gui) {
		e.setCancelled(true);
		ItemStack currentItem = e.getCurrentItem();
		if (currentItem != null) {
			if (currentItem.hasItemMeta() && currentItem.getItemMeta().hasDisplayName()) {
				switch (currentItem.getItemMeta().getDisplayName()) {
					case "§b초반 무적":
						Configuration.modifyProperty(ConfigNodes.GAME_INVINCIBILITY_ENABLE,
								!Settings.InvincibilitySettings.isEnabled());
						Show();
						break;
					case "§b초반 무적 시간":
						int duration = Settings.InvincibilitySettings.getDuration();
						switch (e.getClick()) {
							case RIGHT:
								Configuration.modifyProperty(ConfigNodes.GAME_INVINCIBILITY_DURATION, duration + unit.getUnit());
								break;
							case SHIFT_RIGHT:
								Configuration.modifyProperty(ConfigNodes.GAME_INVINCIBILITY_DURATION, duration + (unit.getUnit() * 5));
								break;
							case LEFT:
								Configuration.modifyProperty(ConfigNodes.GAME_INVINCIBILITY_DURATION, duration >= 2 ? duration - unit.getUnit() : 1);
								break;
							case SHIFT_LEFT:
								Configuration.modifyProperty(ConfigNodes.GAME_INVINCIBILITY_DURATION, duration >= ((unit.getUnit() * 5) + 1) ? duration - (unit.getUnit() * 5) : 1);
								break;
							default:
								break;
						}
						Show();
						break;
					case "§b단위":
						this.unit = unit.getNext();
						Show();
						break;
				}
			}
		}
	}

}
