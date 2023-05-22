package daybreak.abilitywar;

import daybreak.abilitywar.Command.Condition;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityBase.Cooldown;
import daybreak.abilitywar.ability.AbilityBase.Duration;
import daybreak.abilitywar.ability.AbilityFactory;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.event.AbilityCooldownResetEvent;
import daybreak.abilitywar.addon.installer.AddonsGUI;
import daybreak.abilitywar.addon.installer.info.Addons;
import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.Configuration.Settings.AprilSettings;
import daybreak.abilitywar.config.Configuration.Settings.DeveloperSettings;
import daybreak.abilitywar.config.enums.ConfigNodes;
import daybreak.abilitywar.config.wizard.AbilitySettingWizard;
import daybreak.abilitywar.config.wizard.DeathWizard;
import daybreak.abilitywar.config.wizard.GameSettingWizard;
import daybreak.abilitywar.config.wizard.GameWizard;
import daybreak.abilitywar.config.wizard.InvincibilityWizard;
import daybreak.abilitywar.config.wizard.KitPresetWizard;
import daybreak.abilitywar.config.wizard.KitWizard;
import daybreak.abilitywar.config.wizard.SpawnWizard;
import daybreak.abilitywar.config.wizard.TeamPresetWizard;
import daybreak.abilitywar.config.wizard.WorldResetWizard;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.GameTimer;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.GameManager;
import daybreak.abilitywar.game.list.mix.synergy.SynergyFactory;
import daybreak.abilitywar.game.list.mix.synergy.game.SynergyBlackListGUI;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.game.manager.GameFactory;
import daybreak.abilitywar.game.manager.GameFactory.GameRegistration;
import daybreak.abilitywar.game.manager.SpectatorManager;
import daybreak.abilitywar.game.manager.effect.registry.EffectRegistry;
import daybreak.abilitywar.game.manager.gui.AbilityListGUI;
import daybreak.abilitywar.game.manager.gui.BlackListGUI;
import daybreak.abilitywar.game.manager.gui.EffectsListGUI;
import daybreak.abilitywar.game.manager.gui.GameModeGUI;
import daybreak.abilitywar.game.manager.gui.InstallGUI;
import daybreak.abilitywar.game.manager.gui.SpectatorGUI;
import daybreak.abilitywar.game.manager.gui.SynergyListGUI;
import daybreak.abilitywar.game.manager.gui.tip.AbilityTipGUI;
import daybreak.abilitywar.game.manager.object.AbilitySelect;
import daybreak.abilitywar.game.manager.object.CommandHandler;
import daybreak.abilitywar.game.manager.object.CommandHandler.CommandType;
import daybreak.abilitywar.game.manager.object.DefaultKitHandler;
import daybreak.abilitywar.game.module.DummyManager;
import daybreak.abilitywar.game.module.Invincibility;
import daybreak.abilitywar.game.script.AbstractScript;
import daybreak.abilitywar.game.script.ScriptWizard;
import daybreak.abilitywar.game.script.manager.ScriptManager;
import daybreak.abilitywar.game.specialthanks.SpecialThanks;
import daybreak.abilitywar.game.specialthanks.SpecialThanksGUI;
import daybreak.abilitywar.game.team.interfaces.Members;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.patch.Patchable;
import daybreak.abilitywar.patch.list.Patches;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.TimeUtil;
import daybreak.abilitywar.utils.base.collect.Pair;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil.Josa;
import daybreak.abilitywar.utils.base.logging.Logger;
import daybreak.abilitywar.utils.base.math.NumberUtil;
import daybreak.abilitywar.utils.base.minecraft.SkinInfo;
import daybreak.abilitywar.utils.base.minecraft.WorldReset;
import daybreak.abilitywar.utils.base.minecraft.entity.health.Healths;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Commands implements CommandExecutor, TabCompleter {

	private static final Logger logger = Logger.getLogger(Commands.class);

	private final AbilityWar plugin;
	private final Command mainCommand;

	public Command getMainCommand() {
		return mainCommand;
	}

	Commands(AbilityWar plugin) {
		this.plugin = plugin;
		this.mainCommand = new Command() {
			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				if (args.length == 0) {
					sender.sendMessage(Formatter.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁"));
					sender.sendMessage("§e버전 §7: §f" + plugin.getDescription().getVersion());
					sender.sendMessage("§b개발자 §7: §fDaybreak 새벽");
					sender.sendMessage("§9디스코드 §7: §f새벽§7#0833");
					sender.sendMessage("§3§o/" + command + " help §7§o로 명령어 도움말을 확인하세요.");
					return true;
				}
				return false;
			}
		};
		mainCommand.addSubCommand("help", new Command() {
			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				if (args.length > 0) {
					if (NumberUtil.isInt(args[0])) {
						sendCommandHelp(sender, command, Integer.parseInt(args[0]));
					} else {
						Messager.sendErrorMessage(sender, "존재하지 않는 페이지입니다.");
					}
				} else {
					sendCommandHelp(sender, command, 1);
				}
				return true;
			}

			private void sendCommandHelp(CommandSender sender, String command, int page) {
				final int allPage = 4;
				switch (page) {
					case 1:
						sender.sendMessage(new String[]{Formatter.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁"),
								"§b/" + command + " help <페이지> §7로 더 많은 명령어를 확인하세요! ( §b" + page + " 페이지 §7/ §b" + allPage + " 페이지 §7)",
								Formatter.formatCommand(command, "start", "게임을 시작시킵니다.", true),
								Formatter.formatCommand(command, "stop", "게임을 중지시킵니다.", true),
								Formatter.formatCommand(command, "check", "자신의 능력을 확인합니다.", false),
								Formatter.formatCommand(command, "yes", "자신의 능력을 확정합니다.", false),
								Formatter.formatCommand(command, "no", "자신의 능력을 변경합니다.", false),
								Formatter.formatCommand(command, "abilities", "능력자 전쟁 능력 목록을 확인합니다.", false),
								Formatter.formatCommand(command, "abilities [능력]", "[능력] 능력의 정보를 확인합니다.", false),
								Formatter.formatCommand(command, "specialthanks", "능력자 전쟁 플러그인에 기여한 사람들을 확인합니다.", false)});
						break;
					case 2:
						sender.sendMessage(new String[]{Formatter.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁"),
								"§b/" + command + " help <페이지> §7로 더 많은 명령어를 확인하세요! ( §b" + page + " 페이지 §7/ §b" + allPage + " 페이지 §7)",
								Formatter.formatCommand(command, "team", "팀 게임의 명령어 목록을 확인합니다.", false),
								Formatter.formatCommand(command, "skip", "모든 유저의 능력을 강제로 확정합니다.", true),
								Formatter.formatCommand(command, "anew", "모든 유저의 능력을 새로 뽑습니다.", true),
								Formatter.formatCommand(command, "config", "능력자 전쟁 콘피그 명령어를 확인합니다.", true),
								Formatter.formatCommand(command, "util", "능력자 전쟁 유틸 명령어를 확인합니다.", true),
								Formatter.formatCommand(command, "script", "능력자 전쟁 스크립트 편집을 시작합니다.", true),
								Formatter.formatCommand(command, "gamemode", "게임 모드 설정 GUI를 엽니다.", true),
								Formatter.formatCommand(command, "gamemode [모드]", "게임 모드를 [모드]로 변경합니다.", true)});
						break;
					case 3:
						sender.sendMessage(new String[]{Formatter.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁"),
								"§b/" + command + " help <페이지> §7로 더 많은 명령어를 확인하세요! ( §b" + page + " 페이지 §7/ §b" + allPage + " 페이지 §7)",
								Formatter.formatCommand(command, "abtip", "자신의 능력 팁을 확인합니다.", true),
								Formatter.formatCommand(command, "abtip [능력]", "[능력] 능력의 팁을 확인합니다.", true),
								Formatter.formatCommand(command, "sum", "능력 요악을 확인합니다.", false),
								Formatter.formatCommand(command, "synergies", "능력자 전쟁 시너지 목록을 확인합니다.", false),
								Formatter.formatCommand(command, "synergies [시너지]", "[시너지] 시너지의 정보를 확인합니다.", false),
								Formatter.formatCommand(command, "install", "버전 목록 및 설치 GUI를 엽니다.", true),
								Formatter.formatCommand(command, "patch", "쾌적한 플레이를 위한 패치를 진행합니다.", true),
								Formatter.formatCommand(command, "addon", "추천 애드온 목록 GUI를 엽니다.", true)});
						break;
					case 4:
						sender.sendMessage(new String[]{Formatter.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁"),
								"§b/" + command + " help <페이지> §7로 더 많은 명령어를 확인하세요! ( §b" + page + " 페이지 §7/ §b" + allPage + " 페이지 §7)",
								Formatter.formatCommand(command, "debug", "디버그 명령어를 도움말을 확인합니다.", true),
								Formatter.formatCommand(command, "effects", "상태 이상 목록 GUI를 엽니다.", true),
								Formatter.formatCommand(command, "discord", "디스코드", false)});
						break;
					default:
						Messager.sendErrorMessage(sender, "존재하지 않는 페이지입니다.");
						break;
				}
			}

		});
		mainCommand.addSubCommand("start", new Command(Condition.OP) {
			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				if (!GameManager.isGameRunning()) {
					try {
						if (GameManager.startGame(args)) {
							Bukkit.broadcastMessage("§f관리자 §e" + sender.getName() + "§f님이 게임을 시작시켰습니다.");
						}
					} catch (IllegalArgumentException e) {
						Messager.sendErrorMessage(sender, e.getMessage());
					}
				} else {
					Messager.sendErrorMessage(sender, "게임이 이미 진행되고 있습니다.");
				}
				return true;
			}
		});
		mainCommand.addSubCommand("stop", new Command(Condition.OP) {
			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				if (GameManager.isGameRunning()) {
					Bukkit.broadcastMessage("§f관리자 §e" + sender.getName() + "§f님이 게임을 중지시켰습니다.");
					GameManager.stopGame();
				} else {
					Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않습니다.");
				}
				return true;
			}
		});
		mainCommand.addSubCommand("check", new Command(Condition.PLAYER) {
			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				if (GameManager.isGameRunning()) {
					GameManager.getGame().executeCommand(CommandType.ABILITY_CHECK, sender, command, args, plugin);
				} else Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않습니다.");
				return true;
			}
		});
		mainCommand.addSubCommand("yes", new Command(Condition.PLAYER) {
			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				Player player = (Player) sender;
				if (GameManager.isGameOf(AbilitySelect.Handler.class)) {
					AbstractGame game = GameManager.getGame();
					AbilitySelect abilitySelect = ((AbilitySelect.Handler) game).getAbilitySelect();
					if (abilitySelect != null) {
						if (game.isParticipating(player)) {
							Participant participant = game.getParticipant(player);
							if (abilitySelect.isStarted() && !abilitySelect.isEnded()) {
								if (abilitySelect.isSelector(participant)) {
									if (!abilitySelect.hasDecided(participant)) {
										abilitySelect.decideAbility(participant);
									} else player.sendMessage("§c이미 능력 선택을 마치셨습니다.");
								} else Messager.sendErrorMessage(sender, "당신은 능력을 선택하고 있지 않습니다.");
							} else Messager.sendErrorMessage(sender, "능력을 선택하고 있지 않습니다.");
						} else Messager.sendErrorMessage(sender, "게임에 참가하고 있지 않습니다.");
					} else Messager.sendErrorMessage(sender, "능력 선택을 할 수 없는 게임입니다.");
				} else Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않거나 능력 선택을 할 수 없는 게임입니다.");
				return true;
			}
		});
		mainCommand.addSubCommand("no", new Command(Condition.PLAYER) {
			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				Player player = (Player) sender;
				if (GameManager.isGameOf(AbilitySelect.Handler.class)) {
					AbstractGame game = GameManager.getGame();
					AbilitySelect abilitySelect = ((AbilitySelect.Handler) game).getAbilitySelect();
					if (abilitySelect != null) {
						if (game.isParticipating(player)) {
							Participant participant = game.getParticipant(player);
							if (abilitySelect.isStarted() && !abilitySelect.isEnded()) {
								if (abilitySelect.isSelector(participant)) {
									if (!abilitySelect.hasDecided(participant)) {
										abilitySelect.alterAbility(participant);
									} else player.sendMessage("§c이미 능력 선택을 마치셨습니다.");
								} else Messager.sendErrorMessage(sender, "당신은 능력을 선택하고 있지 않습니다.");
							} else Messager.sendErrorMessage(sender, "능력을 선택하고 있지 않습니다.");
						} else Messager.sendErrorMessage(sender, "게임에 참가하고 있지 않습니다.");
					} else Messager.sendErrorMessage(sender, "능력 선택을 할 수 없는 게임입니다.");
				} else Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않거나 능력 선택을 할 수 없는 게임입니다.");
				return true;
			}
		});
		mainCommand.addSubCommand("abilities", new Command() {
			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				if (args.length == 0) {
					if (sender instanceof Player) new AbilityListGUI((Player) sender, AbilityWar.getPlugin()).openGUI(1); else Messager.sendErrorMessage(sender, "사용법 §7: §f/" + command + " abilities <능력>");
				} else {
					final String name = String.join(" ", args);
					if (AbilityFactory.isRegistered(name) && AbilityList.isRegistered(name)) {
						for (String line : Formatter.formatInfo(AbilityFactory.getByName(name))) {
							sender.sendMessage(line);
						}
					} else Messager.sendErrorMessage(sender, name + KoreanUtil.getJosa(name, Josa.은는) + " 존재하지 않는 능력입니다.");
				}
				return true;
			}
		});
		mainCommand.addSubCommand("synergies", new Command() {
			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				if (args.length == 0) {
					if (sender instanceof Player) new SynergyListGUI((Player) sender, AbilityWar.getPlugin()).openGUI(1); else Messager.sendErrorMessage(sender, "사용법 §7: §f/" + command + " synergies <시너지>");
				} else {
					final String name = String.join(" ", args);
					final AbilityRegistration registration = SynergyFactory.getByName(name);
					if (registration != null) {
						sender.sendMessage(Formatter.formatTitle(32, ChatColor.DARK_GREEN, ChatColor.GREEN, "시너지 정보"));
						final Pair<AbilityRegistration, AbilityRegistration> base = SynergyFactory.getSynergyBase(registration);
						sender.sendMessage("§f시너지: §a" + base.getLeft().getManifest().name() + " §f+ §a" + base.getRight().getManifest().name());
						sender.sendMessage("§a---------------------------------");
						final AbilityManifest manifest = registration.getManifest();
						sender.sendMessage("§b" + manifest.name() + " " + manifest.rank().getRankName() + " " + manifest.species().getSpeciesName());
						for (final Iterator<String> iterator = AbilityBase.getExplanation(registration); iterator.hasNext(); ) {
							sender.sendMessage(ChatColor.RESET + iterator.next());
						}
						sender.sendMessage("§2§m§l-----------------------------");
					} else
						Messager.sendErrorMessage(sender, name + KoreanUtil.getJosa(name, Josa.은는) + " 존재하지 않는 시너지입니다.");
				}
				return true;
			}
		});
		mainCommand.addSubCommand("sum", new Command(Condition.PLAYER) {
			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				if (GameManager.isGameRunning()) {
					GameManager.getGame().executeCommand(CommandType.ABILITY_SUMMARIZE, sender, command, args, plugin);
				} else Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않습니다.");
				return true;
			}
		});
		mainCommand.addSubCommand("abtip", new Command(Condition.PLAYER) {
			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				if (args.length == 0) {
					if (GameManager.isGameRunning()) {
						GameManager.getGame().executeCommand(CommandType.TIP_CHECK, sender, command, args, plugin);
					} else Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않습니다. §8(§7/aw abtip <능력> 명령어를 사용하세요.§8)");
				} else {
					final String name = String.join(" ", args);
					if (AbilityFactory.isRegistered(name) && AbilityList.isRegistered(name)) {
						new AbilityTipGUI((Player) sender, AbilityFactory.getByName(name), plugin).openGUI(1);
					} else Messager.sendErrorMessage(sender, name + KoreanUtil.getJosa(name, Josa.은는) + " 존재하지 않는 능력입니다.");
				}
				return true;
			}
		});
		mainCommand.addSubCommand("abtips", new Command() {
			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				if (args.length == 0) {
					if (sender instanceof Player) new AbilityListGUI((Player) sender, AbilityWar.getPlugin()).openGUI(1); else Messager.sendErrorMessage(sender, "사용법 §7: §f/" + command + " abilities <능력>");
					return true;
				} else {
					final String name = String.join(" ", args);
					if (AbilityFactory.isRegistered(name) && AbilityList.isRegistered(name)) {
						for (String line : Formatter.formatTip(AbilityFactory.getByName(name))) {
							sender.sendMessage(line);
						}
					} else Messager.sendErrorMessage(sender, name + KoreanUtil.getJosa(name, Josa.은는) + " 존재하지 않는 능력입니다.");
				}
				return true;
			}
		});
		mainCommand.addSubCommand("skip", new Command(Condition.OP) {
			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				if (GameManager.isGameOf(AbilitySelect.Handler.class)) {
					AbilitySelect abilitySelect = ((AbilitySelect.Handler) GameManager.getGame()).getAbilitySelect();
					if (abilitySelect != null) {
						if (abilitySelect.isStarted() && !abilitySelect.isEnded()) {
							abilitySelect.skip(sender.getName());
						} else {
							Messager.sendErrorMessage(sender, "능력을 선택하고 있지 않습니다.");
						}
					} else Messager.sendErrorMessage(sender, "능력 선택을 할 수 없는 게임입니다.");
				} else Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않거나 능력 선택을 할 수 없는 게임입니다.");
				return true;
			}
		});
		mainCommand.addSubCommand("anew", new Command(Condition.OP) {
			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				if (GameManager.isGameOf(AbilitySelect.Handler.class)) {
					AbilitySelect abilitySelect = ((AbilitySelect.Handler) GameManager.getGame()).getAbilitySelect();
					if (abilitySelect != null) {
						abilitySelect.reset();
						Bukkit.broadcastMessage("§f관리자 §e" + sender.getName() + "§f님이 모든 참가자의 능력을 재추첨했습니다.");
					} else Messager.sendErrorMessage(sender, "능력 선택을 할 수 없는 게임입니다.");
				} else Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않거나 능력 선택을 할 수 없는 게임입니다.");
				return true;
			}
		});
		final Command configCommand = new Command(Condition.OP, Condition.PLAYER) {
			{
				addSubCommand("kit", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						Player player = (Player) sender;
						if (args.length > 0) {
							String name = String.join(" ", args);
							if (AbilityFactory.isRegistered(name)) {
								new KitWizard(player, plugin, AbilityFactory.getByName(name)).show();
							} else {
								player.sendMessage(ChatColor.RED + name + KoreanUtil.getJosa(name, Josa.은는) + " 존재하지 않는 능력입니다.");
							}
						} else {
							new KitWizard(player, plugin).show();
						}
						return true;
					}
				});
				addSubCommand("spawn", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						new SpawnWizard((Player) sender, plugin).show();
						return true;
					}
				});
				addSubCommand("inv", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						new InvincibilityWizard((Player) sender, plugin).show();
						return true;
					}
				});
				addSubCommand("game", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						new GameWizard((Player) sender, plugin).show();
						return true;
					}
				});
				addSubCommand("games", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						new GameSettingWizard((Player) sender, plugin).openGUI(1);
						return true;
					}
				});
				addSubCommand("death", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						new DeathWizard((Player) sender, plugin).show();
						return true;
					}
				});
				addSubCommand("ability", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						new AbilitySettingWizard((Player) sender, plugin).openGUI(1);
						return true;
					}
				});
				addSubCommand("teampreset", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						new TeamPresetWizard((Player) sender, plugin).openGUI(1);
						return true;
					}
				});
				addSubCommand("kitpreset", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						new KitPresetWizard((Player) sender, plugin).openGUI(1);
						return true;
					}
				});
				addSubCommand("blacklist", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						if (args.length == 0) {
							if (sender instanceof Player) {
								new BlackListGUI((Player) sender, plugin).openGUI(1);
							} else Messager.sendErrorMessage(sender, "사용법 §7: §f/" + command + " config blacklist [대상]");
						} else if ("synergy".equalsIgnoreCase(args[0]) || "synergies".equalsIgnoreCase(args[0])) {
							if (sender instanceof Player) {
								new SynergyBlackListGUI((Player) sender, plugin).openGUI(1);
							}
						} else {
							String name = String.join(" ", args);
							if (AbilityFactory.isRegistered(name)) {
								if (Settings.toggleBlacklist(name)) sender.sendMessage("§c블랙리스트에 추가됨§7: §f" + name);
								else sender.sendMessage("§a블랙리스트에서 제거됨§7: §f" + name);
							} else
								Messager.sendErrorMessage(sender, name + KoreanUtil.getJosa(name, Josa.은는) + " 존재하지 않는 능력입니다.");
						}
						return true;
					}
				});
				addSubCommand("worldreset", new Command(Condition.PLAYER) {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						new WorldResetWizard((Player) sender, plugin).openGUI(1);
						return true;
					}
				});
				addSubCommand("developer", new Command() {
					private final Map<String, Long> typed = new HashMap<>();
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						if (!typed.containsKey(sender.getName())) {
							typed.put(sender.getName(), System.currentTimeMillis());
							if (AprilSettings.isEnabled()) {
								sender.sendMessage("§f정말 §3개발자 설정§f을 §c비활성화§f하려면 §e/" + command + " config developer §f명령어를 다시 사용하세요.");
							} else {
								sender.sendMessage("§3개발자 설정§f을 §a활성화§f하면 테스트 중인 §3베타 §f능력, 게임 모드 등이 §a활성화§f되며,");
								sender.sendMessage("§f'§3베타§f'는 현재 실험 중이며, 불안정하거나 오류가 있을 수 있다는 것을 의미합니다.");
								sender.sendMessage("§f정말 §3개발자 설정§f을 §a활성화§f하려면 §e/" + command + " config developer §f명령어를 다시 사용하세요.");
							}
							sender.sendMessage("§710초 §f안에 다시 한번 입력해주세요. §8(§7설정이 변경될 때 서버가 리로드됩니다.§8)");
						} else {
							if (System.currentTimeMillis() - typed.remove(sender.getName()) <= 10000) {
								Configuration.modifyProperty(ConfigNodes.DEVELOPER, !DeveloperSettings.isEnabled());
								Bukkit.broadcastMessage(Messager.defaultPrefix + "서버를 다시 불러옵니다.");
								Bukkit.reload();
								Bukkit.broadcastMessage(Messager.defaultPrefix + "서버를 다시 불러왔습니다.");
							} else {
								sender.sendMessage("§7만료되었습니다.");
							}
						}
						return true;
					}
				});
			}

			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				Player player = (Player) sender;
				if (args.length > 0) {
					if (NumberUtil.isInt(args[0])) {
						sendConfigCommandHelp(player, command, Integer.parseInt(args[0]));
					} else
						Messager.sendErrorMessage(player, "'§4" + args[0] + "§c'" + KoreanUtil.getJosa(args[0], Josa.은는) + " 존재하지 않는 콘피그입니다.");
				} else sendConfigCommandHelp(player, command, 1);
				return true;
			}

			private void sendConfigCommandHelp(CommandSender sender, String label, int page) {
				final int allPage = 3;
				switch (page) {
					case 1:
						sender.sendMessage(new String[]{Formatter.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁 콘피그"),
								"§b/" + label + " config <페이지> §7로 더 많은 명령어를 확인하세요! ( §b" + page + " 페이지 §7/ §b" + allPage + " 페이지 §7)",
								Formatter.formatCommand(label + " config", "kit", "능력자 전쟁 기본템을 설정합니다.", true),
								Formatter.formatCommand(label + " config", "kit <능력 이름>", "능력별 기본템을 설정합니다.", true),
								Formatter.formatCommand(label + " config", "spawn", "능력자 전쟁 스폰을 설정합니다.", true),
								Formatter.formatCommand(label + " config", "inv", "초반 무적을 설정합니다.", true),
								Formatter.formatCommand(label + " config", "game", "게임의 전반적인 부분들을 설정합니다.", true),
								Formatter.formatCommand(label + " config", "death", "플레이어 사망에 관련된 콘피그를 설정합니다.", true)});
						break;
					case 2:
						sender.sendMessage(new String[]{Formatter.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁 콘피그"),
								"§b/" + label + " config <페이지> §7로 더 많은 명령어를 확인하세요! ( §b" + page + " 페이지 §7/ §b" + allPage + " 페이지 §7)",
								Formatter.formatCommand(label + " config", "ability", "능력별 설정을 변경합니다.", true),
								Formatter.formatCommand(label + " config", "games", "게임별 설정을 변경합니다.", true),
								Formatter.formatCommand(label + " config", "teampreset", "팀 프리셋 설정 GUI를 엽니다.", true),
								Formatter.formatCommand(label + " config", "kitpreset", "기본 아이템 프리셋 설정 GUI를 엽니다.", true),
								Formatter.formatCommand(label + " config", "blacklist", "능력 블랙리스트 설정 GUI를 엽니다.", true),
								Formatter.formatCommand(label + " config", "blacklist [능력]", "[능력] 능력의 블랙 상태를 토글합니다.", true)});
						break;
					case 3:
						sender.sendMessage(new String[]{Formatter.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁 콘피그"),
								"§b/" + label + " config <페이지> §7로 더 많은 명령어를 확인하세요! ( §b" + page + " 페이지 §7/ §b" + allPage + " 페이지 §7)",
								Formatter.formatCommand(label + " config", "blacklist synergy", "시너지 블랙리스트 설정 GUI를 엽니다.", true),
								Formatter.formatCommand(label + " config", "developer", "개발자 모드를 토글합니다.", true)});
						break;
					default:
						Messager.sendErrorMessage(sender, "존재하지 않는 페이지입니다.");
						break;
				}
			}
		};
		mainCommand.addSubCommand("config", configCommand);
		final Command utilCommand = new Command(Condition.OP) {
			{
				addSubCommand("abi", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						if (GameManager.isGameRunning()) {
							GameManager.getGame().executeCommand(CommandHandler.CommandType.ABI, sender, command, args, plugin);
						} else Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않습니다.");
						return true;
					}
				});
				addSubCommand("inv", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						if (GameManager.isGameOf(Invincibility.Handler.class)) {
							if (GameManager.getGame().isGameStarted()) {
								Invincibility invincibility = ((Invincibility.Handler) GameManager.getGame()).getInvincibility();
								if (invincibility.isEnabled()) {
									invincibility.stop();
									Bukkit.broadcastMessage("§f" + sender.getName() + "§a님이 무적 상태를 §f비활성화§a하셨습니다.");
								} else {
									if (args.length >= 1) {
										if (NumberUtil.isInt(args[0])) {
											int duration = Integer.parseInt(args[0]);
											invincibility.start(duration);
											Bukkit.broadcastMessage("§f" + sender.getName() + "§a님이 무적 상태를 §f" + TimeUtil.parseTimeAsString(duration) + "§a간 §f활성화§a하셨습니다.");
										} else {
											Messager.sendErrorMessage(sender, "시간은 자연수로 입력되어야 합니다.");
										}
									} else {
										invincibility.start(true);
										Bukkit.broadcastMessage("§f" + sender.getName() + "§a님이 무적 상태를 §f활성화§a하셨습니다.");
									}
								}
							} else Messager.sendErrorMessage(sender, "§c게임이 아직 시작되지 않았습니다.");
						} else Messager.sendErrorMessage(sender, "§c게임이 진행되고 있지 않거나 무적 기능을 사용할 수 있는 게임이 아닙니다.");
						return true;
					}
				});
				addSubCommand("spec", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						if (args.length == 0) {
							if (sender instanceof Player) {
								SpectatorGUI gui = new SpectatorGUI((Player) sender, plugin);
								gui.openGUI(1);
							} else Messager.sendErrorMessage(sender, "사용법 §7: §f/" + command + " util spec [대상]");
						} else {
							for (String arg : args) {
								if (SpectatorManager.isSpectator(arg)) {
									SpectatorManager.removeSpectator(arg);
									sender.sendMessage("§f" + arg + "§a님은 지금부터 게임에 참여할 수 있습니다.");
								} else {
									Player target = Bukkit.getPlayerExact(arg);
									if (target != null) {
										SpectatorManager.addSpectator(target.getName());
										sender.sendMessage("§f" + target.getName() + "§c님은 더 이상 게임에 참여하지 않습니다.");
									} else
										Messager.sendErrorMessage(sender, arg + KoreanUtil.getJosa(arg, Josa.은는) + " 존재하지 않는 플레이어입니다.");
								}
							}
						}
						return true;
					}
				});
				addSubCommand("ablist", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						if (GameManager.isGameRunning()) {
							GameManager.getGame().executeCommand(CommandHandler.CommandType.ABLIST, sender, command, args, plugin);
						} else {
							Messager.sendErrorMessage(sender, "§c게임이 진행되고 있지 않습니다.");
						}
						return true;
					}
				});
				addSubCommand("check", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						if (GameManager.isGameRunning()) {
							if (args.length != 0) {
								final Player target = Bukkit.getPlayerExact(args[0]);
								if (target != null) {
									final AbstractGame game = GameManager.getGame();
									if (game.isParticipating(target)) {
										final AbilityBase ability = game.getParticipant(target).getAbility();
										if (ability != null) {
											sender.sendMessage(Formatter.formatTitle(44, ChatColor.GOLD, ChatColor.WHITE, ChatColor.YELLOW + target.getName() + ChatColor.WHITE + " 능력 정보"));
											sender.sendMessage("§b" + ability.getName() + " " + (ability.isRestricted() ? "§f[§7능력 비활성화됨§f]" : "§f[§a능력 활성화됨§f]") + " " + ability.getRank().getRankName() + " " + ability.getSpecies().getSpeciesName());
											for (Iterator<String> iterator = ability.getExplanation(); iterator.hasNext(); ) {
												sender.sendMessage(iterator.next());
											}
											sender.sendMessage("§6---------------------------------------");
										} else Messager.sendErrorMessage(sender, target.getName() + "님은 능력이 없습니다.");
										Bukkit.broadcastMessage("§f" + sender.getName() + "§a님이 §f" + target.getName() + "§a님의 능력을 확인하였습니다.");
									} else Messager.sendErrorMessage(sender, target.getName() + "님은 탈락했거나 게임에 참여하지 않았습니다.");
								} else Messager.sendErrorMessage(sender, args[0] + KoreanUtil.getJosa(args[0], Josa.은는) + " 존재하지 않는 플레이어입니다.");
							} else Messager.sendErrorMessage(sender, "사용법 §7: §f/" + command + " util check <대상>");
						} else Messager.sendErrorMessage(sender, "§c게임이 진행되고 있지 않습니다.");
						return true;
					}
				});
				addSubCommand("blacklist", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						Messager.sendErrorMessage(sender, "'§4/" + command + " config blacklist§c' §c명령어를 사용해주세요.");
						return true;
					}
				});
				addSubCommand("resetcool", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						if (GameManager.isGameRunning()) {
							if (args.length == 0) {
								GameManager.getGame().stopTimers(Cooldown.CooldownTimer.class);
								for (Participant participant : GameManager.getGame().getParticipants()) {
									if (participant.hasAbility()) {
										Bukkit.getPluginManager().callEvent(new AbilityCooldownResetEvent(participant.getAbility()));
									}
								}
								Bukkit.broadcastMessage("§f" + sender.getName() + "§a님이 능력 쿨타임을 모두 초기화하였습니다.");
							} else {
								final Player target = Bukkit.getPlayerExact(args[0]);
								if (target != null) {
									final AbstractGame game = GameManager.getGame();
									if (game.isParticipating(target)) {
										final AbilityBase ability = game.getParticipant(target).getAbility();
										if (ability != null) {
											for (GameTimer timer : ability.getTimers()) {
												if (timer instanceof Cooldown.CooldownTimer) {
													timer.stop(false);
												}
											}
											Bukkit.getPluginManager().callEvent(new AbilityCooldownResetEvent(ability));
										}
										Bukkit.broadcastMessage("§f" + sender.getName() + "§a님이 §f" + target.getName() + "§a님의 능력 쿨타임을 초기화하였습니다.");
									} else Messager.sendErrorMessage(sender, target.getName() + "님은 탈락했거나 게임에 참여하지 않았습니다.");
								} else Messager.sendErrorMessage(sender, args[0] + KoreanUtil.getJosa(args[0], Josa.은는) + " 존재하지 않는 플레이어입니다.");
							}
						} else Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않습니다.");
						return true;
					}
				});
				addSubCommand("resetduration", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						if (GameManager.isGameRunning()) {
							if (args.length == 0) {
								GameManager.getGame().stopTimers(Duration.DurationTimer.class);
								Bukkit.broadcastMessage("§f" + sender.getName() + "§a님이 능력 지속시간을 모두 초기화하였습니다.");
							} else {
								final Player target = Bukkit.getPlayerExact(args[0]);
								if (target != null) {
									final AbstractGame game = GameManager.getGame();
									if (game.isParticipating(target)) {
										final AbilityBase ability = game.getParticipant(target).getAbility();
										if (ability != null) {
											for (GameTimer timer : ability.getTimers()) {
												if (timer instanceof Duration.DurationTimer) {
													timer.stop(true);
												}
											}
										}
										Bukkit.broadcastMessage("§f" + sender.getName() + "§a님이 §f" + target.getName() + "§a님의 능력 지속시간을 초기화하였습니다.");
									} else Messager.sendErrorMessage(sender, target.getName() + "님은 탈락했거나 게임에 참여하지 않았습니다.");
								} else Messager.sendErrorMessage(sender, args[0] + KoreanUtil.getJosa(args[0], Josa.은는) + " 존재하지 않는 플레이어입니다.");
							}
						} else Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않습니다.");
						return true;
					}
				});
				addSubCommand("kit", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						if (GameManager.isGameOf(DefaultKitHandler.class)) {
							if (args.length == 0)
								Messager.sendErrorMessage(sender, "사용법 §7: §f/" + command + " util kit <대상/@a>");
							else {
								AbstractGame game = GameManager.getGame();
								DefaultKitHandler handler = (DefaultKitHandler) game;
								if (args[0].equalsIgnoreCase("@a")) {
									Bukkit.broadcastMessage("§f" + sender.getName() + "§a님이 §f모든 참가자§a에게 기본템을 다시 지급하였습니다.");
									handler.giveDefaultKit(game.getParticipants());
								} else {
									Player target = Bukkit.getPlayerExact(args[0]);
									if (target != null) {
										if (GameManager.getGame().isParticipating(target)) {
											handler.giveDefaultKit(GameManager.getGame().getParticipant(target));
											SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(target);
											Bukkit.broadcastMessage("§f" + sender.getName() + "§a님이 §f" + target.getName() + "§a님에게 기본템을 다시 지급하였습니다.");
										} else
											Messager.sendErrorMessage(sender, target.getName() + "님은 탈락했거나 게임에 참가하지 않았습니다.");
									} else
										Messager.sendErrorMessage(sender, args[0] + KoreanUtil.getJosa(args[0], Josa.은는) + " 존재하지 않는 플레이어입니다.");
								}
							}
						} else Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않거나 기본 킷을 지급할 수 있는 게임이 아닙니다.");
						return true;
					}
				});
				addSubCommand("team", new Command() {
					{
						addSubCommand("list", new Command() {
							@Override
							protected boolean onCommand(CommandSender sender, String command, String[] args) {
								if (GameManager.isGameOf(Teamable.class)) {
									final Teamable teamGame = (Teamable) GameManager.getGame();
									final ArrayList<Members> teams = new ArrayList<>(teamGame.getTeams());
									final int maxPage = (teams.size() / 8) + (teams.size() % 8 == 0 ? 0 : 1);
									int page = 1;
									if (args.length >= 1) {
										if (NumberUtil.isInt(args[0])) {
											page = Integer.parseInt(args[0]);
										} else {
											Messager.sendErrorMessage(sender, "페이지는 자연수로 입력되어야 합니다.");
										}
									}

									if (page > 0 && page <= maxPage) {
										sender.sendMessage("§6===== §e팀 목록 §f(" + page + "페이지 / " + maxPage + "페이지) §6=====");
										for (int i = (page * 8) - 8; i < (page * 8); i++) {
											if (i >= teams.size()) {
												break;
											}
											Members team = teams.get(i);
											sender.sendMessage(ChatColor.YELLOW + "● " + ChatColor.WHITE + team.getName() + ChatColor.WHITE + " (" + team.getDisplayName() + ChatColor.WHITE + ")");
										}
										sender.sendMessage("§6========================================");
									} else {
										Messager.sendErrorMessage(sender, "존재하지 않는 페이지입니다.");
									}
								} else Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않거나 팀 기능을 사용할 수 있는 게임이 아닙니다.");
								return true;
							}
						});
						addSubCommand("set", new Command() {
							@Override
							protected boolean onCommand(CommandSender sender, String command, String[] args) {
								if (GameManager.isGameOf(Teamable.class)) {
									final Teamable teamGame = (Teamable) GameManager.getGame();
									if (args.length >= 2) {
										Player targetPlayer = Bukkit.getPlayerExact(args[0]);
										if (targetPlayer != null) {
											if (teamGame.isParticipating(targetPlayer)) {
												Participant target = teamGame.getParticipant(targetPlayer);
												if (teamGame.teamExists(args[1])) {
													Members team = teamGame.getTeam(args[1]);
													if (!teamGame.hasTeam(target) || !teamGame.getTeam(target).equals(team)) {
														teamGame.setTeam(target, team);
														sender.sendMessage("§e" + targetPlayer.getName() + "§f님의 팀을 " + team.getName() + " §f(" + team.getDisplayName() + "§f)" + KoreanUtil.getJosa(team.getName(), Josa.으로로) + " 설정하였습니다.");
													} else
														sender.sendMessage("§e" + targetPlayer.getName() + "§f님은 이미 " + team.getName() + " §f(" + team.getDisplayName() + "§f) 팀의 일원입니다.");
												} else
													Messager.sendErrorMessage(sender, args[1] + KoreanUtil.getJosa(args[1], Josa.은는) + " 존재하지 않는 팀입니다.");
											} else
												Messager.sendErrorMessage(sender, targetPlayer.getName() + "님은 탈락했거나 게임에 참가하지 않았습니다.");
										} else
											Messager.sendErrorMessage(sender, args[0] + KoreanUtil.getJosa(args[0], Josa.은는) + " 존재하지 않는 플레이어입니다.");
									} else
										Messager.sendErrorMessage(sender, "사용법 §7: §f/" + command + " util team set <대상> <팀 이름>");
								} else Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않거나 팀 기능을 사용할 수 있는 게임이 아닙니다.");
								return true;
							}
						});
						addSubCommand("new", new Command() {
							@Override
							protected boolean onCommand(CommandSender sender, String command, String[] args) {
								if (GameManager.isGameOf(Teamable.class)) {
									final Teamable teamGame = (Teamable) GameManager.getGame();
									if (args.length >= 2) {
										try {
											Members team = teamGame.newTeam(args[0], ChatColor.translateAlternateColorCodes('&', args[1]));
											Bukkit.broadcastMessage("§e" + sender.getName() + "§f님이 " + team.getName() + "§f(" + team.getDisplayName() + "§f) 팀을 새로 만들었습니다.");
										} catch (IllegalStateException | IllegalArgumentException e) {
											Messager.sendErrorMessage(sender, e.getMessage());
										}
									} else
										Messager.sendErrorMessage(sender, "사용법 §7: §f/" + command + " util team new <팀 이름> <팀 별명>");
								} else Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않거나 팀 기능을 사용할 수 있는 게임이 아닙니다.");
								return true;
							}
						});
						addSubCommand("remove", new Command() {
							@Override
							protected boolean onCommand(CommandSender sender, String command, String[] args) {
								if (GameManager.isGameOf(Teamable.class)) {
									final Teamable teamGame = (Teamable) GameManager.getGame();
									if (args.length >= 1) {
										if (teamGame.teamExists(args[0])) {
											Members team = teamGame.getTeam(args[0]);
											teamGame.removeTeam(team);
											Bukkit.broadcastMessage("§e" + sender.getName() + "§f님이 " + team.getName() + "§f(" + team.getDisplayName() + "§f) 팀을 삭제했습니다.");
										} else Messager.sendErrorMessage(sender, args[0] + "은(는) 존재하지 않는 팀입니다.");
									} else
										Messager.sendErrorMessage(sender, "사용법 §7: §f/" + command + " util team remove <팀 이름>");
								} else Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않거나 팀 기능을 사용할 수 있는 게임이 아닙니다.");
								return true;
							}
						});
						addSubCommand("divide", new Command() {
							@Override
							protected boolean onCommand(CommandSender sender, String command, String[] args) {
								if (GameManager.isGameOf(Teamable.class)) {
									final Teamable teamGame = (Teamable) GameManager.getGame();
									for (Participant participant : teamGame.getParticipants()) {
										teamGame.setTeam(participant, null);
									}
									Bukkit.broadcastMessage("§e" + sender.getName() + "§f님이 모든 참가자를 각각 하나의 팀으로 나누었습니다.");
								} else Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않거나 팀 기능을 사용할 수 있는 게임이 아닙니다.");
								return true;
							}
						});
					}

					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						if (GameManager.isGameOf(Teamable.class)) {
							if (args.length > 0) {
								if (NumberUtil.isInt(args[0])) {
									sendTeamUtilCommandHelp(sender, command, Integer.parseInt(args[0]));
								} else
									Messager.sendErrorMessage(sender, "'§4" + args[0] + "§c'" + KoreanUtil.getJosa(args[0], Josa.은는) + " 존재하지 않는 유틸입니다.");
							} else sendTeamUtilCommandHelp(sender, command, 1);
						} else Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않거나 팀 기능을 사용할 수 있는 게임이 아닙니다.");
						return true;
					}

					private void sendTeamUtilCommandHelp(CommandSender sender, String label, int page) {
						final int allPage = 1;
						if (page == 1) {
							sender.sendMessage(new String[]{
									Formatter.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁 팀 유틸"),
									"§b/" + label + " util team <페이지> §7로 더 많은 명령어를 확인하세요! ( §b" + page + " 페이지 §7/ §b" + allPage + " 페이지 §7)",
									Formatter.formatCommand(label + " util team", "list [페이지]", "팀 목록을 확인합니다.", true),
									Formatter.formatCommand(label + " util team", "set <대상> <팀 이름>", "대상의 팀을 설정합니다.", true),
									Formatter.formatCommand(label + " util team", "new <팀 이름> <팀 별명>", "새로운 팀을 만듭니다.", true),
									Formatter.formatCommand(label + " util team", "remove <팀 이름>", "팀을 삭제합니다.", true),
									Formatter.formatCommand(label + " util team", "divide", "모든 플레이어를 각각 하나의 팀으로 나눕니다.", true)
							});
						} else Messager.sendErrorMessage(sender, "존재하지 않는 페이지입니다.");
					}

				});
			}

			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				if (args.length > 0) {
					if (NumberUtil.isInt(args[0])) {
						sendUtilCommandHelp(sender, command, Integer.parseInt(args[0]));
					} else
						Messager.sendErrorMessage(sender, "'§4" + args[0] + "§c'" + KoreanUtil.getJosa(args[0], Josa.은는) + " 존재하지 않는 유틸입니다.");
				} else sendUtilCommandHelp(sender, command, 1);
				return true;
			}

			private void sendUtilCommandHelp(CommandSender sender, String label, int page) {
				final int allPage = 2;
				switch (page) {
					case 1:
						sender.sendMessage(new String[]{Formatter.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁 유틸"),
								"§b/" + label + " util <페이지> §7로 더 많은 명령어를 확인하세요! ( §b" + page + " 페이지 §7/ §b" + allPage + " 페이지 §7)",
								Formatter.formatCommand(label + " util", "abi <대상/@a>", "능력 부여 GUI를 엽니다.", true),
								Formatter.formatCommand(label + " util", "abi <대상/@a> [능력]", "대상에게 [능력] 능력을 부여합니다.", true),
								Formatter.formatCommand(label + " util", "inv [시간(초)]", "무적 상태를 토글합니다.", true),
								Formatter.formatCommand(label + " util", "spec", "관전자 설정 GUI를 엽니다.", true),
								Formatter.formatCommand(label + " util", "spec [대상]", "[대상]의 관전 상태를 토글합니다.", true),
								Formatter.formatCommand(label + " util", "ablist", "능력자 목록을 확인합니다.", true)});
						break;
					case 2:
						sender.sendMessage(new String[]{Formatter.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁 유틸"),
								"§b/" + label + " util <페이지> §7로 더 많은 명령어를 확인하세요! ( §b" + page + " 페이지 §7/ §b" + allPage + " 페이지 §7)",
								Formatter.formatCommand(label + " util", "check <대상>", "대상의 능력을 확인합니다.", true),
								Formatter.formatCommand(label + " util", "resetcool", "참가자들의 능력 쿨타임을 초기화시킵니다.", true),
								Formatter.formatCommand(label + " util", "resetcool <대상>", "대상의 능력 쿨타임을 초기화시킵니다.", true),
								Formatter.formatCommand(label + " util", "resetduration", "참가자들의 능력 지속시간을 초기화시킵니다.", true),
								Formatter.formatCommand(label + " util", "resetduration <대상>", "대상의 능력 지속시간을 초기화시킵니다.", true),
								Formatter.formatCommand(label + " util", "kit <대상/@a>", "대상에게 기본템을 다시 지급합니다.", true)});
						break;
					case 3:
						sender.sendMessage(new String[]{Formatter.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁 유틸"),
								"§b/" + label + " util <페이지> §7로 더 많은 명령어를 확인하세요! ( §b" + page + " 페이지 §7/ §b" + allPage + " 페이지 §7)",
								Formatter.formatCommand(label + " util", "team", "팀 유틸 명령어를 확인합니다.", true)});
						break;
					default:
						Messager.sendErrorMessage(sender, "존재하지 않는 페이지입니다.");
						break;
				}
			}
		};
		mainCommand.addSubCommand("util", utilCommand);
		mainCommand.addSubCommand("debug", new Command(Condition.OP) {
			{
				addSubCommand("dummy", new Command(Condition.PLAYER) {
					private final String dummyPrefix = "§5[§d더미§5] §f";
					{
						addSubCommand("create", new Command() {
							@Override
							protected boolean onCommand(CommandSender sender, String command, String[] args) {
								if (GameManager.isGameRunning()) {
									final AbstractGame game = GameManager.getGame();
									final DummyManager dummyManager = game.getModule(DummyManager.class);
									if (dummyManager != null) {
										try {
											if (args.length == 0) {
												dummyManager.createDummy(((Player) sender).getLocation());
												sender.sendMessage(dummyPrefix + "연습용 봇 하나가 소환되었습니다.");
											} else {
												final SkinInfo skinInfo = SpecialThanks.getSkinInfo(args[0]);
												if (skinInfo != null) {
													dummyManager.createDummy(((Player) sender).getLocation(), skinInfo);
													sender.sendMessage(dummyPrefix + "연습용 봇 하나가 소환되었습니다.");
												} else Messager.sendErrorMessage(sender, "존재하지 않는 스킨입니다.");
											}
										} catch (RuntimeException e) {
											sender.sendMessage(dummyPrefix + "연습용 봇 소환 중 문제가 발생했습니다.");
											e.printStackTrace();
										}
									} else Messager.sendErrorMessage(sender, "연습용 봇을 지원하는 게임이 아닙니다.");
								} else Messager.sendErrorMessage(sender, "게임이 진행중이지 않습니다.");
								return true;
							}
						});
						addSubCommand("clear", new Command() {
							@Override
							protected boolean onCommand(CommandSender sender, String command, String[] args) {
								if (GameManager.isGameRunning()) {
									final AbstractGame game = GameManager.getGame();
									final DummyManager dummyManager = game.getModule(DummyManager.class);
									if (dummyManager != null) {
										sender.sendMessage(dummyPrefix + "연습용 봇 §d" + dummyManager.clearDummies() + "개§f를 제거했습니다.");
									} else Messager.sendErrorMessage(sender, "연습용 봇을 지원하는 게임이 아닙니다.");
								} else Messager.sendErrorMessage(sender, "게임이 진행중이지 않습니다.");
								return true;
							}
						});
					}
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						if (args.length > 0) {
							Messager.sendErrorMessage(sender, "'§4" + args[0] + "§c'" + KoreanUtil.getJosa(args[0], Josa.은는) + " 존재하지 않는 연습용 봇 명령어입니다.");
						} else {
							sender.sendMessage(new String[]{Formatter.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "연습용 봇 명령어"),
									Formatter.formatCommand(command + " debug dummy", "create", "연습용 봇을 하나 소환합니다.", true),
									Formatter.formatCommand(command + " debug dummy", "clear", "연습용 봇을 모두 제거합니다.", true)});
						}
						return true;
					}

				});
				addSubCommand("gravitate", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						if (args.length > 0) {
							final Player target = Bukkit.getPlayerExact(args[0]);
							if (target != null) {
								target.setGravity(true);
								sender.sendMessage("§f" + target.getName() + "§a" + KoreanUtil.getJosa(target.getName().replaceAll("_", ""), Josa.을를) + " 중력에 영향받도록 설정했습니다.");
							} else Messager.sendErrorMessage(sender, args[0] + KoreanUtil.getJosa(args[0], Josa.은는) + " 존재하지 않는 플레이어입니다.");
						} else Messager.sendErrorMessage(sender, "사용법 §7: §f/" + command + " debug gravitate <대상>");
						return true;
					}
				});
				addSubCommand("sethealth", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						if (args.length > 1) {
							final Player target = Bukkit.getPlayerExact(args[0]);
							if (target != null) {
								try {
									double health = Double.valueOf(args[1]);
									if (health < 0) {
										Messager.sendErrorMessage(sender, "체력은 0 이상의 수로 입력되어야 합니다.");
										return true;
									}
									health = Healths.setHealth(target, health);
									sender.sendMessage("§f" + target.getName() + "§a의 체력을 §e" + health + "§a" + KoreanUtil.getJosa(String.valueOf(health), Josa.으로로) + " 설정했습니다.");
								} catch (NumberFormatException e) {
									 Messager.sendErrorMessage(sender, "체력은 0 이상의 수로 입력되어야 합니다.");
								}
							} else Messager.sendErrorMessage(sender, args[0] + KoreanUtil.getJosa(args[0], Josa.은는) + " 존재하지 않는 플레이어입니다.");
						} else Messager.sendErrorMessage(sender, "사용법 §7: §f/" + command + " debug sethealth <대상> <체력>");
						return true;
					}
				});
				addSubCommand("respawn", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						if (args.length > 0) {
							final Player target = Bukkit.getPlayerExact(args[0]);
							if (target != null) {
								if (target.isDead()) {
									NMS.respawn(target);
									sender.sendMessage("§f" + target.getName() + "§a" + KoreanUtil.getJosa(target.getName().replaceAll("_", ""), Josa.을를) + " 리스폰시켰습니다.");
								} else Messager.sendErrorMessage(sender, target.getName() + KoreanUtil.getJosa(target.getName().replaceAll("_", ""), Josa.은는) + " 사망하지 않았습니다.");
							} else Messager.sendErrorMessage(sender, args[0] + KoreanUtil.getJosa(args[0].replaceAll("_", ""), Josa.은는) + " 존재하지 않는 플레이어입니다.");
						} else Messager.sendErrorMessage(sender, "사용법 §7: §f/" + command + " debug respawn <대상>");
						return true;
					}
				});
			}

			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				if (args.length > 0) {
					if (NumberUtil.isInt(args[0])) {
						sendHelp(sender, command, Integer.parseInt(args[0]));
					} else Messager.sendErrorMessage(sender, "'§4" + args[0] + "§c'" + KoreanUtil.getJosa(args[0], Josa.은는) + " 존재하지 않는 디버그 명령어입니다.");
				} else sendHelp(sender, command, 1);
				return true;
			}

			private void sendHelp(CommandSender sender, String label, int page) {
				final int allPage = 1;
				switch (page) {
					case 1:
						sender.sendMessage(new String[]{Formatter.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁 디버그"),
								"§b/" + label + " debug <페이지> §7로 더 많은 명령어를 확인하세요! ( §b" + page + " 페이지 §7/ §b" + allPage + " 페이지 §7)",
								Formatter.formatCommand(label + " debug", "dummy", "연습용 봇 명령어 도움말을 확인합니다.", true),
								Formatter.formatCommand(label + " debug", "gravitate <대상>", "대상이 중력에 영향받도록 설정합니다.", true),
								Formatter.formatCommand(label + " debug", "respawn <대상>", "사망한 대상을 리스폰시킵니다.", true),
								Formatter.formatCommand(label + " debug", "sethealth <대상> <체력>", "대상의 체력을 설정합니다.", true)});
						break;
					default:
						Messager.sendErrorMessage(sender, "존재하지 않는 페이지입니다.");
						break;
				}
			}
		});
		mainCommand.addSubCommand("script", new Command(Condition.PLAYER, Condition.OP) {
			private final Pattern SCRIPT_NAME_PATTERN = Pattern.compile("^[가-힣a-zA-Z0-9_]+$");

			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				if (args.length > 1) {
					try {
						Class<? extends AbstractScript> scriptClass = ScriptManager.getScriptClass(args[0]);
						if (scriptClass != null) {
							if (SCRIPT_NAME_PATTERN.matcher(args[1]).find() && args[1].length() <= 10) {
								File file = new File("plugins/" + plugin.getName() + "/Script/" + args[1] + ".yml");
								if (!file.exists()) {
									new ScriptWizard((Player) sender, plugin, scriptClass, args[1]).openGUI(1);
								} else {
									Messager.sendErrorMessage(sender, "§e" + args[1] + ".yml §f스크립트 파일이 이미 존재합니다.");
								}
							} else {
								Messager.sendErrorMessage(sender, "§c'§4" + args[1] + "§c'" + KoreanUtil.getJosa(args[1], Josa.은는) + " 사용할 수 없는 이름입니다.");
							}
						} else {
							Messager.sendErrorMessage(sender, "§c'§4" + args[0] + "§c'" + KoreanUtil.getJosa(args[0], Josa.은는) + " 존재하지 않는 스크립트 유형입니다.");
						}
					} catch (IllegalArgumentException ex) {
						Messager.sendErrorMessage(sender, "사용할 수 없는 스크립트 유형입니다.");
						if (ex.getMessage() != null && !ex.getMessage().isEmpty()) {
							logger.error(ex.getMessage());
						}
					}
				} else Messager.sendErrorMessage(sender, "사용법 §7: §f/" + command + " script <유형> <이름>");
				return true;
			}
		});
		mainCommand.addSubCommand("gamemode", new Command(Condition.OP) {
			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				if (args.length == 0) {
					if (sender instanceof Player) {
						new GameModeGUI((Player) sender, plugin).openGUI(1);
					} else Messager.sendErrorMessage(sender, "사용법 §7: §f/" + command + " gamemode [게임 모드]");
				} else {
					String name = String.join(" ", args);
					if (GameFactory.isRegistered(name)) {
						final GameRegistration<?> registration = GameFactory.getByName(name);
						Configuration.modifyProperty(ConfigNodes.GAME_MODE, registration.getGameClass().getName());
						try {
							Configuration.update();
							sender.sendMessage("§2게임 모드§a가 §f" + registration.getManifest().name() + "§a" + KoreanUtil.getJosa(registration.getManifest().name(), Josa.으로로) + " 변경되었습니다.");
						} catch (IOException | InvalidConfigurationException e) {
							e.printStackTrace();
							sender.sendMessage("§2게임 모드§a를 변경하는 도중 오류가 발생하였습니다.");
						}
					} else
						Messager.sendErrorMessage(sender, name + KoreanUtil.getJosa(name, Josa.은는) + " 존재하지 않는 게임 모드입니다.");
				}
				return true;
			}
		});
		mainCommand.addSubCommand("install", new Command(Condition.OP, Condition.PLAYER) {
			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				try {
					new InstallGUI((Player) sender, plugin, plugin.getInstaller()).openGUI(1);
				} catch (IllegalStateException e) {
					Messager.sendErrorMessage(sender, "아직 버전 목록이 불러와지지 않았습니다.");
				}
				return true;
			}
		});
		mainCommand.addSubCommand("team", new Command(Condition.PLAYER) {
			{
				addSubCommand("chat", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						final Player player = (Player) sender;
						if (GameManager.isGameOf(Teamable.class)) {
							AbstractGame game = GameManager.getGame();
							if (game.isParticipating(player)) {
								Participant participant = game.getParticipant(player);
								if (participant.attributes().TEAM_CHAT.setValue(!participant.attributes().TEAM_CHAT.getValue())) {
									player.sendMessage("§5[§d팀§5] §f팀 채팅이 비활성화되었습니다.");
								} else {
									player.sendMessage("§5[§d팀§5] §f팀 채팅이 활성화되었습니다.");
								}
							} else Messager.sendErrorMessage(sender, "게임에 참가하고 있지 않습니다.");
						} else Messager.sendErrorMessage(player, "게임이 진행되고 있지 않거나 팀 기능을 사용할 수 있는 게임이 아닙니다.");
						return true;
					}
				});
				addSubCommand("info", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						Player player = (Player) sender;
						if (GameManager.isGameOf(Teamable.class)) {
							final Teamable teamGame = (Teamable) GameManager.getGame();
							if (teamGame.isParticipating(player)) {
								Participant participant = teamGame.getParticipant(player);
								if (teamGame.hasTeam(participant)) {
									for (String str : Formatter.formatTeamInfo(teamGame.getTeam(participant))) {
										player.sendMessage(str);
									}
								} else Messager.sendErrorMessage(player, "팀에 소속되지 않았습니다.");
							} else Messager.sendErrorMessage(sender, "게임에 참가하고 있지 않습니다.");
						} else Messager.sendErrorMessage(player, "게임이 진행되고 있지 않거나 팀 기능을 사용할 수 있는 게임이 아닙니다.");
						return true;
					}
				});
			}

			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				Player p = (Player) sender;
				if (GameManager.isGameOf(Teamable.class)) {
					final Teamable teamGame = (Teamable) GameManager.getGame();
					if (teamGame.isParticipating(p)) {
						if (args.length > 0) {
							if (NumberUtil.isInt(args[0])) {
								sendTeamCommandHelp(sender, command, Integer.parseInt(args[0]));
							} else
								Messager.sendErrorMessage(sender, "'§4" + args[0] + "§c'" + KoreanUtil.getJosa(args[0], Josa.은는) + " 존재하지 않는 팀 명령어입니다.");
						} else sendTeamCommandHelp(sender, command, 1);
					} else Messager.sendErrorMessage(sender, "게임에 참가하고 있지 않습니다.");
				} else Messager.sendErrorMessage(p, "게임이 진행되고 있지 않거나 팀 기능을 사용할 수 있는 게임이 아닙니다.");
				return true;
			}

			private void sendTeamCommandHelp(CommandSender sender, String label, int page) {
				final int allPage = 1;
				if (page == 1) {
					sender.sendMessage(new String[]{
							Formatter.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁 팀 명령어"),
							"§b/" + label + " team <페이지> §7로 더 많은 명령어를 확인하세요! ( §b" + page + " 페이지 §7/ §b" + allPage + " 페이지 §7)",
							Formatter.formatCommand(label + " team", "chat", "팀 채팅을 토글합니다.", false),
							Formatter.formatCommand(label + " team", "info", "속해있는 팀 정보를 확인합니다.", false)
					});
				} else Messager.sendErrorMessage(sender, "존재하지 않는 페이지입니다.");
			}

		});
		mainCommand.addSubCommand("specialthanks", new Command(Condition.PLAYER) {
			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				SpecialThanksGUI.openGUI((Player) sender);
				return true;
			}
		});
		mainCommand.addSubCommand("addon", new Command(Condition.OP, Condition.PLAYER) {
			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				try {
					new AddonsGUI((Player) sender, Addons.getInstance(), plugin).openGUI(1);
				} catch (IllegalStateException e) {
					Messager.sendErrorMessage(sender, "아직 추천 애드온 목록이 불러와지지 않았습니다.");
				}
				return true;
			}
		});
		mainCommand.addSubCommand("patch", new Command(Condition.OP) {
			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				sender.sendMessage(Messager.defaultPrefix + "패치를 시작합니다.");
				int applied = 0;
				boolean notApplied = false;
				for (final Patchable patch : Patches.VALUES) {
					if (!patch.isValid() || patch.isApplied()) continue;
					notApplied = true;
					sender.sendMessage("§a" + patch.getName() + " §f패치 중...");
					patch.apply();
					if (patch.isApplied()) {
						sender.sendMessage("§2완료");
						applied++;
					} else {
						sender.sendMessage("§c실패");
					}
				}
				if (applied == 0) {
					if (notApplied) {
						sender.sendMessage(Messager.defaultPrefix + applied + "개의 패치를 완료했습니다.");
					} else {
						sender.sendMessage(Messager.defaultPrefix + "적용할 패치가 없습니다.");
					}
				} else {
					sender.sendMessage(Messager.defaultPrefix + applied + "개의 패치를 완료했습니다.");
				}
				return true;
			}
		});
		mainCommand.addSubCommand("effects", new Command() {
			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				if (args.length == 0) {
					if (sender instanceof Player) new EffectsListGUI((Player) sender, AbilityWar.getPlugin()).openGUI(1); else Messager.sendErrorMessage(sender, "사용법 §7: §f/" + command + " effects <상태 이상>");
				} else {
					final String name = String.join(" ", args);
					if (EffectRegistry.isRegistered(name)) {

					} else Messager.sendErrorMessage(sender, name + KoreanUtil.getJosa(name, Josa.은는) + " 존재하지 않는 능력입니다.");
				}
				return true;
			}
		});
		mainCommand.addSubCommand("worldreset", new Command(Condition.OP) {
			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				if (args.length == 0) {
					sender.sendMessage(Messager.defaultPrefix + "월드 초기화 시작");
					WorldReset.resetWorlds();
					sender.sendMessage(Messager.defaultPrefix + "월드 초기화 완료");
				} else {
					final World world = Bukkit.getWorld(args[0]);
					if (world == null) {
						sender.sendMessage(Messager.defaultPrefix + args[0] + KoreanUtil.getJosa(args[0], Josa.은는) +  " 없는 월드입니다.");
						return true;
					}
					sender.sendMessage(Messager.defaultPrefix + world.getName() + " 월드 초기화 시작");
					WorldReset.resetWorld(world);
					sender.sendMessage(Messager.defaultPrefix + world.getName() + " 월드 초기화 완료");
				}
				return true;
			}
		});
		mainCommand.addSubCommand("discord", new Command() {
			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				if (args.length == 0) {
					sender.sendMessage(Formatter.formatTitle(ChatColor.DARK_AQUA, ChatColor.WHITE, "디스코드"));
					sender.sendMessage("§b개발자 DM§7: §f새벽§7#0833");
					sender.sendMessage("§b디스코드§7: §fhttps://discord.gg/zK9ZJFBcwf");
					return true;
				}
				return false;
			}
		});
	}

	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
		mainCommand.handleCommand(sender, label, args);
		return true;
	}

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command cmd, String label, String[] args) {
		if (label.equalsIgnoreCase("abilitywar") || label.equalsIgnoreCase("ability") || label.equalsIgnoreCase("aw")
				|| label.equalsIgnoreCase("va") || label.equalsIgnoreCase("능력자")) {
			switch (args.length) {
				case 1:
					List<String> subCommands = Messager.asList("start", "stop", "check", "yes", "no", "abilities", "synergies", "effects", "abtip", "sum", "skip", "anew",
							"config", "util", "script", "gamemode", "worldreset", "install", "team", "specialthanks", "addon", "patch", "debug", "discord");
					if (args[0].isEmpty()) {
						return subCommands;
					} else {
						subCommands.removeIf(command -> !command.toLowerCase().startsWith(args[0].toLowerCase()));
						return subCommands;
					}
				case 2:
					if (args[0].equalsIgnoreCase("config")) {
						List<String> configs = Messager.asList("kit", "spawn", "inv", "game", "death", "ability", "games", "worldreset", "teampreset", "kitpreset", "blacklist", "developer");
						if (args[1].isEmpty()) {
							return configs;
						} else {
							configs.removeIf(config -> !config.toLowerCase().startsWith(args[1].toLowerCase()));
							return configs;
						}
					} else if (args[0].equalsIgnoreCase("util")) {
						List<String> utils = Messager.asList("abi", "spec", "ablist", "check", "resetcool",
								"resetduration", "kit", "inv", "team");
						if (args[1].isEmpty()) {
							return utils;
						} else {
							utils.removeIf(util -> !util.toLowerCase().startsWith(args[1].toLowerCase()));
							return utils;
						}
					} else if (args[0].equalsIgnoreCase("script")) {
						List<String> scripts = new ArrayList<>(ScriptManager.getRegisteredScriptNames());
						if (args[1].isEmpty()) {
							return scripts;
						} else {
							scripts.removeIf(script -> !script.toLowerCase().startsWith(args[1].toLowerCase()));
							return scripts;
						}
					} else if (args[0].equalsIgnoreCase("team")) {
						List<String> commands = Messager.asList("chat", "info");
						if (args[1].isEmpty()) {
							return commands;
						} else {
							commands.removeIf(util -> !util.toLowerCase().startsWith(args[1].toLowerCase()));
							return commands;
						}
					} else if (args[0].equalsIgnoreCase("debug")) {
						final List<String> commands = Messager.asList("dummy", "gravitate", "respawn", "sethealth");
						if (args[1].isEmpty()) {
							return commands;
						} else {
							commands.removeIf(part -> !part.toLowerCase().startsWith(args[1].toLowerCase()));
							return commands;
						}
					}
					break;
				case 3:
					if (args[0].equalsIgnoreCase("util")) {
						if (args[1].equalsIgnoreCase("abi") || args[1].equalsIgnoreCase("kit")) {
							List<String> players = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
							players.add("@a");
							players.sort(String::compareToIgnoreCase);
							if (args[2].isEmpty()) {
								return players;
							} else {
								players.removeIf(name -> !name.toLowerCase().startsWith(args[2].toLowerCase()));
								return players;
							}
						} else if (args[1].equalsIgnoreCase("check") || args[1].equalsIgnoreCase("resetcool") || args[1].equalsIgnoreCase("resetduration")) {
							List<String> players = Bukkit.getOnlinePlayers().stream().map(Player::getName).sorted(String::compareToIgnoreCase).collect(Collectors.toList());
							if (args[2].isEmpty()) {
								return players;
							} else {
								players.removeIf(name -> !name.toLowerCase().startsWith(args[2].toLowerCase()));
								return players;
							}
						} else if (args[1].equalsIgnoreCase("team")) {
							List<String> teamUtils = Messager.asList("list", "set", "new", "remove", "divide");
							if (args[2].isEmpty()) {
								return teamUtils;
							} else {
								teamUtils.removeIf(util -> !util.toLowerCase().startsWith(args[2].toLowerCase()));
								return teamUtils;
							}
						}
					} else if (args[0].equalsIgnoreCase("debug")) {
						if (args[1].equalsIgnoreCase("dummy")) {
							final List<String> commands = Messager.asList("create", "clear");
							if (args[2].isEmpty()) {
								return commands;
							} else {
								commands.removeIf(part -> !part.toLowerCase().startsWith(args[2].toLowerCase()));
								return commands;
							}
						}
					} else if ("config".equalsIgnoreCase(args[0])) {
						if ("blacklist".equalsIgnoreCase(args[1])) {
							if (args[2].isEmpty() || "synergy".startsWith(args[2])) {
								return Collections.singletonList("synergy");
							}
						}
					}
					break;
				case 4:
					if (args[0].equalsIgnoreCase("util")) {
						if (args[1].equalsIgnoreCase("team")) {
							if (args[2].equalsIgnoreCase("set")) {
								List<String> players = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
								players.sort(String::compareToIgnoreCase);
								if (args[3].isEmpty()) {
									return players;
								} else {
									players.removeIf(name -> !name.toLowerCase().startsWith(args[3].toLowerCase()));
									return players;
								}
							} else if (args[2].equalsIgnoreCase("remove")) {
								if (GameManager.isGameOf(Teamable.class)) {
									final Teamable teamGame = (Teamable) GameManager.getGame();
									return teamGame.getTeams().stream().map(Members::getName).collect(Collectors.toList());
								}
							}
						}
					}
					break;
				case 5:
					if (args[0].equalsIgnoreCase("util")) {
						if (args[1].equalsIgnoreCase("team") && args[2].equalsIgnoreCase("set")) {
							if (GameManager.isGameOf(Teamable.class)) {
								final Teamable teamGame = (Teamable) GameManager.getGame();
								return teamGame.getTeams().stream().map(Members::getName).collect(Collectors.toList());
							}
						}
					}
			}
			if (args.length >= 2) {
				if (args[0].equalsIgnoreCase("abilities") || args[0].equalsIgnoreCase("abtip")) {
					return AbilityList.getComplete(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
				} else if (args[0].equalsIgnoreCase("synergies")) {
					return SynergyFactory.getComplete(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
				}
			}
			if (args.length >= 4) {
				if (args[0].equalsIgnoreCase("util") && args[1].equalsIgnoreCase("abi")) {
					if (GameManager.isGameRunning()) {
						return GameManager.getGame().tabComplete(CommandHandler.CommandType.ABI, sender, label, args, plugin);
					}
				}
			}
		}
		return null;
	}

}
