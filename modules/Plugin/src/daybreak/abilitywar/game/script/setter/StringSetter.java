package daybreak.abilitywar.game.script.setter;

import daybreak.abilitywar.game.script.ScriptWizard;
import daybreak.abilitywar.utils.base.Messager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StringSetter extends Setter<String> {

	public StringSetter(String Key, String Default, ScriptWizard Wizard) {
		super(Key, Default, Wizard);

		registerEvent(AsyncPlayerChatEvent.class);
	}

	@Override
	public void execute(Listener listener, Event event) {
		if (event instanceof AsyncPlayerChatEvent) {
			AsyncPlayerChatEvent e = (AsyncPlayerChatEvent) event;
			if (e.getPlayer().equals(getWizard().getPlayer())) {
				if (Setting) {
					Setting = false;
					e.setCancelled(true);

					if (!e.getMessage().equals("%")) {
						//값이 업데이트되면 인벤토리가 자동으로 열림
						this.setValue(e.getMessage());
					} else {
						this.updateGUI();
					}
				}
			}
		}
	}

	private boolean Setting = false;

	@Override
	public void onClick(ClickType click) {
		Setting = true;
		getWizard().safeClose();
		getWizard().getPlayer().sendMessage("§f변경할 §6텍스트§f를 채팅창에 입력해주세요. 취소하려면 §e%§f를 입력해주세요.");
	}

	@Override
	public ItemStack getItem() {
		ItemStack string = new ItemStack(Material.PAPER);
		ItemMeta stringMeta = string.getItemMeta();
		stringMeta.setDisplayName(ChatColor.AQUA + this.getKey());
		stringMeta.setLore(Messager.asList(
				"§f\"" + this.getValue() + "§f\"",
				"",
				"§6텍스트§f를 변경하려면 클릭하세요."
		));

		string.setItemMeta(stringMeta);

		return string;
	}

}
