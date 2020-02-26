package daybreak.abilitywar.game.script.objects.setter.special;

import daybreak.abilitywar.game.script.ScriptWizard;
import daybreak.abilitywar.game.script.objects.setter.Setter;
import daybreak.abilitywar.utils.base.Messager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MessageSetter extends Setter<String> {

	public MessageSetter(String Key, String Default, ScriptWizard Wizard) {
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
		Player p = getWizard().getPlayer();
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f변경할 &6메시지&f를 채팅창에 입력해주세요. 취소하려면 &e%&f를 입력해주세요."));
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f메시지를 'none'으로 설정하면 메시지가 전송되지 않습니다."));
	}

	@Override
	public ItemStack getItem() {
		ItemStack string = new ItemStack(Material.PAPER);
		ItemMeta stringMeta = string.getItemMeta();
		stringMeta.setDisplayName(ChatColor.AQUA + this.getKey());
		stringMeta.setLore(Messager.asList(
				ChatColor.translateAlternateColorCodes('&', "&f\"" + this.getValue() + "&f\""),
				"",
				ChatColor.translateAlternateColorCodes('&', "&6메시지&f를 변경하려면 클릭하세요.")
		));

		string.setItemMeta(stringMeta);

		return string;
	}

}
