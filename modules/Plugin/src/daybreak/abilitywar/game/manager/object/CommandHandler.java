package daybreak.abilitywar.game.manager.object;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.GameManager;
import daybreak.abilitywar.game.manager.gui.AbilityGUI;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil.Josa;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public interface CommandHandler {

	default void executeCommand(CommandType commandType, Player player, String[] args, Plugin plugin) {
		switch (commandType) {
			case ABI:
				if (args[0].equalsIgnoreCase("@a")) {
					AbilityGUI gui = new AbilityGUI(player, plugin);
					gui.openGUI(1);
				} else {
					Player targetPlayer = Bukkit.getPlayerExact(args[0]);
					if (targetPlayer != null) {
						AbstractGame game = GameManager.getGame();
						if (game.isParticipating(targetPlayer)) {
							AbstractGame.Participant target = game.getParticipant(targetPlayer);
							AbilityGUI gui = new AbilityGUI(player, target, plugin);
							gui.openGUI(1);
						} else {
							Messager.sendErrorMessage(player, targetPlayer.getName() + "님은 탈락했거나 게임에 참여하지 않았습니다.");
						}
					} else {
						Messager.sendErrorMessage(player, args[0] + KoreanUtil.getJosa(args[0], Josa.은는) + " 존재하지 않는 플레이어입니다.");
					}
				}
				break;
			case ABLIST:
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2===== &a능력자 목록 &2====="));
				int count = 0;
				for (AbstractGame.Participant participant : GameManager.getGame().getParticipants()) {
					if (participant.hasAbility()) {
						count++;
						AbilityBase ability = participant.getAbility();
						String name = ability.getName();
						if (name != null) {
							player.sendMessage(ChatColor.translateAlternateColorCodes('&',
									"&e" + count + ". &f" + participant.getPlayer().getName() + " &7: &c" + name));
						}
					}
				}
				if (count == 0) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f능력자가 발견되지 않았습니다."));
				}

				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2========================"));

				Bukkit.broadcastMessage(
						ChatColor.translateAlternateColorCodes('&', "&f" + player.getName() + "&a님이 플레이어들의 능력을 확인하였습니다."));
				break;
		}
	}

	enum CommandType {
		ABI,
		ABLIST
	}

}
