package daybreak.abilitywar.game.manager.object;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.config.Configuration.Settings.DeveloperSettings;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.GameManager;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.game.manager.gui.AbilityGUI;
import daybreak.abilitywar.game.manager.gui.tip.AbilityTipGUI;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil.Josa;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;

public interface CommandHandler {

	default void executeCommand(CommandType commandType, CommandSender sender, String command, String[] args, Plugin plugin) {
		switch (commandType) {
			case ABI: {
				if (args.length == 0) {
					if (sender instanceof Player)
						Messager.sendErrorMessage(sender, "사용법 §7: §f/" + command + " util abi <대상/@a> §7또는 §f/" + command + " util abi <대상/@a> [능력]");
					else Messager.sendErrorMessage(sender, "사용법 §7: §f/" + command + " util abi <대상/@a> [능력]");
					return;
				}
				if (args.length == 1) {
					if (sender instanceof Player) {
						final Player player = (Player) sender;
						if (args[0].equalsIgnoreCase("@a")) {
							new AbilityGUI(player, GameManager.getGame(), plugin).openGUI(1);
						} else {
							final Player targetPlayer = Bukkit.getPlayerExact(args[0]);
							if (targetPlayer != null) {
								final AbstractGame game = GameManager.getGame();
								if (game.isParticipating(targetPlayer)) {
									new AbilityGUI(player, game.getParticipant(targetPlayer), game, plugin).openGUI(1);
								} else
									Messager.sendErrorMessage(player, targetPlayer.getName() + "님은 탈락했거나 게임에 참여하지 않았습니다.");
							} else
								Messager.sendErrorMessage(player, args[0] + KoreanUtil.getJosa(args[0], Josa.은는) + " 존재하지 않는 플레이어입니다.");
						}
					} else Messager.sendErrorMessage(sender, "사용법 §7: §f/" + command + " util abi <대상/@a> [능력]");
				} else {
					final String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

					if (AbilityList.isRegistered(name)) {
						if (args[0].equalsIgnoreCase("@a")) {
							try {
								final Class<? extends AbilityBase> clazz = AbilityList.getByString(name);
								for (Participant participant : GameManager.getGame().getParticipants()) {
									participant.setAbility(clazz);
								}
								Bukkit.broadcastMessage("§e" + sender.getName() + "§a님이 §f모든 참가자§a에게 능력을 임의로 부여하였습니다.");
							} catch (ReflectiveOperationException e) {
								Messager.sendErrorMessage(sender, "능력 설정 도중 오류가 발생하였습니다.");
								if (DeveloperSettings.isEnabled()) e.printStackTrace();
							}
						} else {
							final Player targetPlayer = Bukkit.getPlayerExact(args[0]);
							if (targetPlayer != null) {
								final AbstractGame game = GameManager.getGame();
								if (game.isParticipating(targetPlayer)) {
									try {
										game.getParticipant(targetPlayer).setAbility(AbilityList.getByString(name));
										Bukkit.broadcastMessage("§e" + sender.getName() + "§a님이 §f" + targetPlayer.getName() + "§a님에게 능력을 임의로 부여하였습니다.");
									} catch (ReflectiveOperationException e) {
										Messager.sendErrorMessage(sender, "능력 설정 도중 오류가 발생하였습니다.");
										if (DeveloperSettings.isEnabled()) e.printStackTrace();
									}
								} else
									Messager.sendErrorMessage(sender, targetPlayer.getName() + "님은 탈락했거나 게임에 참여하지 않았습니다.");
							} else
								Messager.sendErrorMessage(sender, args[0] + KoreanUtil.getJosa(args[0], Josa.은는) + " 존재하지 않는 플레이어입니다.");
						}
					} else
						Messager.sendErrorMessage(sender, name + KoreanUtil.getJosa(name, Josa.은는) + " 존재하지 않는 능력입니다.");
				}
			}
			break;
			case ABLIST: {
				sender.sendMessage("§2===== §a능력자 목록 §2=====");
				int count = 0;
				for (Participant participant : GameManager.getGame().getParticipants()) {
					if (participant.hasAbility()) {
						count++;
						final String name = participant.getAbility().getName();
						sender.sendMessage("§e" + count + ". §f" + participant.getPlayer().getName() + " §7: §c" + name);
					}
				}
				if (count == 0) sender.sendMessage("§f능력자가 발견되지 않았습니다.");

				sender.sendMessage("§2========================");

				Bukkit.broadcastMessage("§f" + sender.getName() + "§a님이 참가자들의 능력을 확인하였습니다.");
			}
			break;
			case ABILITY_CHECK: {
				final Player player = (Player) sender;
				if (GameManager.isGameRunning()) {
					final AbstractGame game = GameManager.getGame();
					if (game.isParticipating(player)) {
						final AbilityBase ability = game.getParticipant(player).getAbility();
						if (ability != null) {
							for (String line : Formatter.formatAbilityInfo(ability)) {
								player.sendMessage(line);
							}
						} else {
							Messager.sendErrorMessage(sender, "능력이 할당되지 않았습니다.");
						}
					} else {
						Messager.sendErrorMessage(sender, "게임에 참가하고 있지 않습니다.");
					}
				} else {
					Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않습니다.");
				}
			}
			break;
			case ABILITY_SUMMARIZE: {
				final Player player = (Player) sender;
				if (GameManager.isGameRunning()) {
					final AbstractGame game = GameManager.getGame();
					if (game.isParticipating(player)) {
						final AbilityBase ability = game.getParticipant(player).getAbility();
						if (ability != null) {
							if (ability.hasSummarize()) {
								for (String line : Formatter.formatSummarize(ability)) {
									player.sendMessage(line);
								}
							} else Messager.sendErrorMessage(sender, "능력 요약이 작성되지 않은 능력입니다.");
						} else Messager.sendErrorMessage(sender, "능력이 할당되지 않았습니다.");
					} else Messager.sendErrorMessage(sender, "게임에 참가하고 있지 않습니다.");
				} else Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않습니다.");
			}
			break;
			case TIP_CHECK: {
				final Player player = (Player) sender;
				if (GameManager.isGameRunning()) {
					final AbstractGame game = GameManager.getGame();
					if (game.isParticipating(player)) {
						final AbilityBase ability = game.getParticipant(player).getAbility();
						if (ability != null) {
							new AbilityTipGUI(player, ability.getRegistration(), plugin).openGUI(1);
						} else {
							Messager.sendErrorMessage(sender, "능력이 할당되지 않았습니다. §8(§7/aw abtip <능력> 명령어를 사용하세요.§8)");
						}
					} else {
						Messager.sendErrorMessage(sender, "게임에 참가하고 있지 않습니다. §8(§7/aw abtip <능력> 명령어를 사용하세요.§8)");
					}
				} else {
					Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않습니다. §8(§7/aw abtip <능력> 명령어를 사용하세요.§8)");
				}
			}
			break;
		}
	}

	default List<String> tabComplete(CommandType commandType, CommandSender sender, String command, String[] args, Plugin plugin) {
		if (commandType == CommandType.ABI) {
			return AbilityList.getComplete(String.join(" ", Arrays.copyOfRange(args, 3, args.length)));
		}
		return null;
	}

	enum CommandType {
		ABI,
		ABLIST,
		ABILITY_CHECK,
		ABILITY_SUMMARIZE,
		TIP_CHECK
	}

}
