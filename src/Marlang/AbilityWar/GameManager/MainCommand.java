package Marlang.AbilityWar.GameManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import Marlang.AbilityWar.AbilityWar;
import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.Timer.CooldownTimer;
import Marlang.AbilityWar.Config.AbilitySettings;
import Marlang.AbilityWar.Config.AbilityWarSettings;
import Marlang.AbilityWar.Config.SettingWizard;
import Marlang.AbilityWar.GameManager.Game.Game;
import Marlang.AbilityWar.GameManager.Manager.AbilitySelect;
import Marlang.AbilityWar.GameManager.Manager.GUI.AbilityGUI;
import Marlang.AbilityWar.GameManager.Manager.GUI.BlackListGUI;
import Marlang.AbilityWar.GameManager.Manager.GUI.SpecialThanksGUI;
import Marlang.AbilityWar.GameManager.Manager.GUI.SpectatorGUI;
import Marlang.AbilityWar.GameManager.Script.Script;
import Marlang.AbilityWar.GameManager.Script.ScriptException;
import Marlang.AbilityWar.GameManager.Script.ScriptWizard;
import Marlang.AbilityWar.GameManager.Script.Objects.AbstractScript;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Library.SoundLib;
import Marlang.AbilityWar.Utils.Math.NumberUtil;
import Marlang.AbilityWar.Utils.Thread.AbilityWarThread;

public class MainCommand implements CommandExecutor, TabCompleter {
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		parseCommand(sender, label, args);
		return true;
	}
	
	private void parseCommand(CommandSender sender, String label, String[] split) {
		if(split.length == 0) {
			sendHelpCommand(sender, label, 1);
		} else {
			if(split[0].equalsIgnoreCase("help")) {
				if(split.length > 1) {
					if(NumberUtil.isInt(split[1])) {
						sendHelpCommand(sender, label, Integer.valueOf(split[1]));
					} else {
						Messager.sendErrorMessage(sender, "Á¸ÀçÇÏÁö ¾Ê´Â ÆäÀÌÁöÀÔ´Ï´Ù.");
					}
				} else {
					sendHelpCommand(sender, label, 1);
				}
			} else if(split[0].equalsIgnoreCase("start")) {
				if(sender.isOp()) {
					if(!AbilityWarThread.isGameTaskRunning()) {
						AbilityWarThread.startGame(new Game());
						Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f°ü¸®ÀÚ &e" + sender.getName() + "&f´ÔÀÌ °ÔÀÓÀ» ½ÃÀÛ½ÃÄ×½À´Ï´Ù."));
					} else {
						Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c´É·ÂÀÚ ÀüÀïÀÌ ÀÌ¹Ì ÁøÇàµÇ°í ÀÖ½À´Ï´Ù."));
					}
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&cÀÌ ¸í·É¾î¸¦ »ç¿ëÇÏ·Á¸é OP ±ÇÇÑÀÌ ÀÖ¾î¾ß ÇÕ´Ï´Ù."));
				}
			} else if(split[0].equalsIgnoreCase("stop")) {
				if(sender.isOp()) {
					if(AbilityWarThread.isGameTaskRunning()) {
						Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f°ü¸®ÀÚ &e" + sender.getName() + "&f´ÔÀÌ °ÔÀÓÀ» ÁßÁö½ÃÄ×½À´Ï´Ù."));
							
						AbilityWarThread.stopGame();
					} else {
						Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c´É·ÂÀÚ ÀüÀïÀÌ ÁøÇàµÇ°í ÀÖÁö ¾Ê½À´Ï´Ù."));
					}
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&cÀÌ ¸í·É¾î¸¦ »ç¿ëÇÏ·Á¸é OP ±ÇÇÑÀÌ ÀÖ¾î¾ß ÇÕ´Ï´Ù."));
				}
			} else if(split[0].equalsIgnoreCase("reload")) {
				if(sender.isOp()) {
					AbilityWarSettings.Refresh();
					AbilitySettings.Refresh();
					Script.LoadAll();
					Messager.sendMessage(sender, ChatColor.translateAlternateColorCodes('&', "&2´É·ÂÀÚ ÀüÀï&aÀÌ ¸®·ÎµåµÇ¾ú½À´Ï´Ù."));
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&cÀÌ ¸í·É¾î¸¦ »ç¿ëÇÏ·Á¸é OP ±ÇÇÑÀÌ ÀÖ¾î¾ß ÇÕ´Ï´Ù."));
				}
			} else if(split[0].equalsIgnoreCase("config")) {
				if(sender instanceof Player) {
					Player p = (Player) sender;
					if(p.isOp()) {
						
						if(split.length > 1) {
							parseConfigCommand(p, label, Messager.removeFirstArg(split));
						} else {
							sendHelpConfigCommand(p, label, 1);
						}
					} else {
						Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&cÀÌ ¸í·É¾î¸¦ »ç¿ëÇÏ·Á¸é OP ±ÇÇÑÀÌ ÀÖ¾î¾ß ÇÕ´Ï´Ù."));
					}
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&cÄÜ¼Ö¿¡¼­ »ç¿ëÇÒ ¼ö ¾ø´Â ¸í·É¾îÀÔ´Ï´Ù!"));
				}
			} else if(split[0].equalsIgnoreCase("check")) {
				if(sender instanceof Player) {
					Player p = (Player) sender;
					if(AbilityWarThread.isGameTaskRunning()) {
						if(AbilityWarThread.getGame().getAbilities().containsKey(p)) {
							AbilityBase Ability = AbilityWarThread.getGame().getAbilities().get(p);
							Messager.sendStringList(p, Messager.formatAbility(Ability));
						} else {
							Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c´ç½Å¿¡°Ô ´É·ÂÀÌ ÇÒ´çµÇÁö ¾Ê¾Ò½À´Ï´Ù."));
						}
					} else {
						Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c´É·ÂÀÚ ÀüÀïÀÌ ÁøÇàµÇ°í ÀÖÁö ¾Ê½À´Ï´Ù."));
					}
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&cÄÜ¼Ö¿¡¼­ »ç¿ëÇÒ ¼ö ¾ø´Â ¸í·É¾îÀÔ´Ï´Ù!"));
				}
			} else if(split[0].equalsIgnoreCase("yes")) {
				if(sender instanceof Player) {
					Player p = (Player) sender;
					if(AbilityWarThread.isGameTaskRunning()) {
						if(AbilityWarThread.getGame().getAbilities().containsKey(p)) {
							AbilitySelect select = AbilityWarThread.getGame().getAbilitySelect();
							if(select != null && !select.isEnded()) {
								if(!select.hasDecided(p)) {
									select.decideAbility(p, true);
								} else {
									Messager.sendMessage(p, ChatColor.translateAlternateColorCodes('&', "&cÀÌ¹Ì ´É·Â ¼±ÅÃÀ» ¸¶Ä¡¼Ì½À´Ï´Ù."));
								}
							} else {
								Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c´É·ÂÀ» ¼±ÅÃÇÏ´Â ÁßÀÌ ¾Æ´Õ´Ï´Ù."));
							}
						} else {
							Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c´ç½Å¿¡°Ô ´É·ÂÀÌ ÇÒ´çµÇÁö ¾Ê¾Ò½À´Ï´Ù."));
						}
					} else {
						Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c´É·ÂÀÚ ÀüÀïÀÌ ÁøÇàµÇ°í ÀÖÁö ¾Ê½À´Ï´Ù."));
					}
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&cÄÜ¼Ö¿¡¼­ »ç¿ëÇÒ ¼ö ¾ø´Â ¸í·É¾îÀÔ´Ï´Ù!"));
				}
			} else if(split[0].equalsIgnoreCase("no")) {
				if(sender instanceof Player) {
					Player p = (Player) sender;
					if(AbilityWarThread.isGameTaskRunning()) {
						if(AbilityWarThread.getGame().getAbilities().containsKey(p)) {
							AbilitySelect select = AbilityWarThread.getGame().getAbilitySelect();
							if(select != null && !select.isEnded()) {
								if(!select.hasDecided(p)) {
									select.changeAbility(p);
								} else {
									Messager.sendMessage(p, ChatColor.translateAlternateColorCodes('&', "&cÀÌ¹Ì ´É·Â ¼±ÅÃÀ» ¸¶Ä¡¼Ì½À´Ï´Ù."));
								}
							} else {
								Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c´É·ÂÀ» ¼±ÅÃÇÏ´Â ÁßÀÌ ¾Æ´Õ´Ï´Ù."));
							}
						} else {
							Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c´ç½Å¿¡°Ô ´É·ÂÀÌ ÇÒ´çµÇÁö ¾Ê¾Ò½À´Ï´Ù."));
						}
					} else {
						Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c´É·ÂÀÚ ÀüÀïÀÌ ÁøÇàµÇ°í ÀÖÁö ¾Ê½À´Ï´Ù."));
					}
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&cÄÜ¼Ö¿¡¼­ »ç¿ëÇÒ ¼ö ¾ø´Â ¸í·É¾îÀÔ´Ï´Ù!"));
				}
			} else if(split[0].equalsIgnoreCase("skip")) {
				if(sender.isOp()) {
					if(AbilityWarThread.isGameTaskRunning()) {
						AbilitySelect select = AbilityWarThread.getGame().getAbilitySelect();
						if(select != null && !select.isEnded()) {
							select.Skip(sender.getName());
						} else {
							Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c´É·ÂÀ» ¼±ÅÃÇÏ´Â ÁßÀÌ ¾Æ´Õ´Ï´Ù."));
						}
					} else {
						Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c´É·ÂÀÚ ÀüÀïÀÌ ÁøÇàµÇ°í ÀÖÁö ¾Ê½À´Ï´Ù."));
					}
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&cÀÌ ¸í·É¾î¸¦ »ç¿ëÇÏ·Á¸é OP ±ÇÇÑÀÌ ÀÖ¾î¾ß ÇÕ´Ï´Ù."));
				}
			} else if(split[0].equalsIgnoreCase("util")) {
				if(sender instanceof Player) {
					Player p = (Player) sender;
					if(p.isOp()) {
						
						if(split.length > 1) {
							parseUtilCommand(p, label, Messager.removeFirstArg(split));
						} else {
							sendHelpUtilCommand(p, label, 1);
						}
					} else {
						Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&cÀÌ ¸í·É¾î¸¦ »ç¿ëÇÏ·Á¸é OP ±ÇÇÑÀÌ ÀÖ¾î¾ß ÇÕ´Ï´Ù."));
					}
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&cÄÜ¼Ö¿¡¼­ »ç¿ëÇÒ ¼ö ¾ø´Â ¸í·É¾îÀÔ´Ï´Ù!"));
				}
			} else if(split[0].equalsIgnoreCase("script")) {
				if(sender instanceof Player) {
					Player p = (Player) sender;
					if(p.isOp()) {
						if(split.length > 2) {
							try {
								Class<? extends AbstractScript> scriptClass = Script.getScriptClass(split[1]);
								if(Pattern.compile("^[°¡-ÆRa-zA-Z0-9_]+$").matcher(split[2]).find()) {
									File file = new File("plugins/" + AbilityWar.getPlugin().getName() + "/Script/" + split[2] + ".yml");
									if(!file.exists()) {
										ScriptWizard wizard = new ScriptWizard(p, AbilityWar.getPlugin(), scriptClass, split[2]);
										wizard.openScriptWizard(1);
									} else {
										Messager.sendMessage(p, ChatColor.translateAlternateColorCodes('&', "&e" + split[2] + ".yml &f½ºÅ©¸³Æ® ÆÄÀÏÀÌ ÀÌ¹Ì Á¸ÀçÇÕ´Ï´Ù."));
									}
								} else {
									Messager.sendMessage(p, ChatColor.translateAlternateColorCodes('&', "&e" + split[2] + "&fÀº(´Â) »ç¿ëÇÒ ¼ö ¾ø´Â ÀÌ¸§ÀÔ´Ï´Ù."));
								}
							} catch(ClassNotFoundException | ScriptException ex) {
								Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&cÁ¸ÀçÇÏÁö ¾Ê´Â ½ºÅ©¸³Æ® À¯ÇüÀÔ´Ï´Ù."));
							} catch(IllegalArgumentException ex) {
								Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&c»ç¿ëÇÒ ¼ö ¾ø´Â ½ºÅ©¸³Æ® À¯ÇüÀÔ´Ï´Ù."));
							}
						} else {
							Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "»ç¿ë¹ı &7: &f/" + label + " script <À¯Çü> <ÀÌ¸§>"));
						}
					} else {
						Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&cÀÌ ¸í·É¾î¸¦ »ç¿ëÇÏ·Á¸é OP ±ÇÇÑÀÌ ÀÖ¾î¾ß ÇÕ´Ï´Ù."));
					}
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&cÄÜ¼Ö¿¡¼­ »ç¿ëÇÒ ¼ö ¾ø´Â ¸í·É¾îÀÔ´Ï´Ù!"));
				}
			} else if(split[0].equalsIgnoreCase("specialthanks")) {
				if(sender instanceof Player) {
					Player p = (Player) sender;
					SpecialThanksGUI gui = new SpecialThanksGUI(p, AbilityWar.getPlugin());
					gui.openGUI(1);
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&cÄÜ¼Ö¿¡¼­ »ç¿ëÇÒ ¼ö ¾ø´Â ¸í·É¾îÀÔ´Ï´Ù!"));
				}
			} else {
				Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "Á¸ÀçÇÏÁö ¾Ê´Â ¼­ºê ¸í·É¾îÀÔ´Ï´Ù."));
			}
			
		}
	}

	private void parseConfigCommand(Player p, String label, String[] args) {
		SettingWizard wizard = new SettingWizard(p, AbilityWar.getPlugin());
		if(args[0].equalsIgnoreCase("kit")) {
			wizard.openKitGUI();
		} else if(args[0].equalsIgnoreCase("spawn")) {
			wizard.openSpawnGUI();
		} else if(args[0].equalsIgnoreCase("inv")) {
			wizard.openInvincibilityGUI();
		} else if(args[0].equalsIgnoreCase("game")) {
			wizard.openGameGUI();
		} else if(args[0].equalsIgnoreCase("death")) {
			wizard.openDeathGUI();
		} else {
			if(NumberUtil.isInt(args[0])) {
				sendHelpConfigCommand(p, label, Integer.valueOf(args[0]));
			} else {
				Messager.sendErrorMessage(p, "Á¸ÀçÇÏÁö ¾Ê´Â ÄÜÇÇ±×ÀÔ´Ï´Ù.");
			}
		}
	}

	private void parseUtilCommand(Player p, String label, String[] args) {
		if(args[0].equalsIgnoreCase("abi")) {
			if(AbilityWarThread.isGameTaskRunning()) {
				if(args.length < 2) {
					Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "»ç¿ë¹ı &7: &f/" + label + " util abi <´ë»ó>"));
				} else {
					if(args[1].equalsIgnoreCase("@a")) {
						AbilityGUI gui = new AbilityGUI(p, AbilityWar.getPlugin());
						gui.openAbilityGUI(1);
					} else {
						if(Bukkit.getPlayerExact(args[1]) != null) {
							Player target = Bukkit.getPlayerExact(args[1]);
							if(AbilityWarThread.getGame().getParticipants().contains(target)) {
								AbilityGUI gui = new AbilityGUI(p, target, AbilityWar.getPlugin());
								gui.openAbilityGUI(1);
							} else {
								Messager.sendErrorMessage(p, target.getName() + "´ÔÀº Å»¶ôÇß°Å³ª °ÔÀÓ¿¡ Âü¿©ÇÏÁö ¾Ê¾Ò½À´Ï´Ù.");
							}
						} else {
							Messager.sendErrorMessage(p, args[1] + "Àº(´Â) Á¸ÀçÇÏÁö ¾Ê´Â ÇÃ·¹ÀÌ¾îÀÔ´Ï´Ù.");
						}
					}
				}
			} else {
				Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&c´É·ÂÀÚ ÀüÀïÀÌ ÁøÇàµÇ°í ÀÖÁö ¾Ê½À´Ï´Ù."));
			}
		} else if(args[0].equalsIgnoreCase("spec")) {
			SpectatorGUI gui = new SpectatorGUI(p, AbilityWar.getPlugin());
			gui.openSpectateGUI(1);
		} else if(args[0].equalsIgnoreCase("ablist")) {
			if(AbilityWarThread.isGameTaskRunning()) {
				ArrayList<String> msg = new ArrayList<String>();
				msg.add(ChatColor.translateAlternateColorCodes('&', "&2===== &a´É·ÂÀÚ ¸ñ·Ï &2====="));

				Integer Count = 0;
				for(Player player : AbilityWarThread.getGame().getAbilities().keySet()) {
					Count++;
					AbilityBase Ability = AbilityWarThread.getGame().getAbilities().get(player);
					msg.add(ChatColor.translateAlternateColorCodes('&', "&e" + Count + ". &f" + player.getName() + " &7: &c" + Ability.getAbilityName()));
				}
				
				if(Count.equals(0)) {
					msg.add(ChatColor.translateAlternateColorCodes('&', "&f´É·ÂÀÚ°¡ ¹ß°ßµÇÁö ¾Ê¾Ò½À´Ï´Ù."));
				}
				
				msg.add(ChatColor.translateAlternateColorCodes('&', "&2========================"));
				
				Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f" + p.getName() + "&a´ÔÀÌ ÇÃ·¹ÀÌ¾îµéÀÇ ´É·ÂÀ» È®ÀÎÇÏ¿´½À´Ï´Ù."));
				
				for(String m : msg) {
					Messager.sendMessage(p, m);
				}
			} else {
				Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&c´É·ÂÀÚ ÀüÀïÀÌ ÁøÇàµÇ°í ÀÖÁö ¾Ê½À´Ï´Ù."));
			}
		} else if(args[0].equalsIgnoreCase("blacklist")) {
			BlackListGUI gui = new BlackListGUI(p, AbilityWar.getPlugin());
			gui.openBlackListGUI(1);
		} else if(args[0].equalsIgnoreCase("resetcool")) {
			if(AbilityWarThread.isGameTaskRunning()) {
				CooldownTimer.CoolReset();
				Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f" + p.getName() + "&a´ÔÀÌ ÇÃ·¹ÀÌ¾îµéÀÇ ´É·Â ÄğÅ¸ÀÓÀ» ÃÊ±âÈ­ÇÏ¿´½À´Ï´Ù."));
			} else {
				Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&c´É·ÂÀÚ ÀüÀïÀÌ ÁøÇàµÇ°í ÀÖÁö ¾Ê½À´Ï´Ù."));
			}
		} else if(args[0].equalsIgnoreCase("kit")) {
			if(AbilityWarThread.isGameTaskRunning()) {
				if(args.length < 2) {
					Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "»ç¿ë¹ı &7: &f/" + label + " util kit <´ë»ó>"));
				} else {
					if(args[1].equalsIgnoreCase("@a")) {
						Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f" + p.getName() + "&a´ÔÀÌ &fÀüÃ¼ À¯Àú&a¿¡°Ô ±âº»ÅÛÀ» ´Ù½Ã Áö±ŞÇÏ¿´½À´Ï´Ù."));
						AbilityWarThread.getGame().GiveDefaultKit();
					} else {
						if(Bukkit.getPlayerExact(args[1]) != null) {
							Player target = Bukkit.getPlayerExact(args[1]);
							if(AbilityWarThread.getGame().getParticipants().contains(target)) {
								AbilityWarThread.getGame().GiveDefaultKit(target);
								SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(target);
								Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f" + p.getName() + "&a´ÔÀÌ &f" + target.getName() + "&a´Ô¿¡°Ô ±âº»ÅÛÀ» ´Ù½Ã Áö±ŞÇÏ¿´½À´Ï´Ù."));
							} else {
								Messager.sendErrorMessage(p, target.getName() + "´ÔÀº Å»¶ôÇß°Å³ª °ÔÀÓ¿¡ Âü¿©ÇÏÁö ¾Ê¾Ò½À´Ï´Ù.");
							}
						} else {
							Messager.sendErrorMessage(p, args[1] + "Àº(´Â) Á¸ÀçÇÏÁö ¾Ê´Â ÇÃ·¹ÀÌ¾îÀÔ´Ï´Ù.");
						}
					}
				}
			} else {
				Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&c´É·ÂÀÚ ÀüÀïÀÌ ÁøÇàµÇ°í ÀÖÁö ¾Ê½À´Ï´Ù."));
			}
		} else {
			if(NumberUtil.isInt(args[0])) {
				sendHelpUtilCommand(p, label, Integer.valueOf(args[0]));
			} else {
				Messager.sendErrorMessage(p, "Á¸ÀçÇÏÁö ¾Ê´Â À¯Æ¿ÀÔ´Ï´Ù.");
			}
		}
	}
	
	private void sendHelpCommand(CommandSender sender, String label, Integer Page) {
		int AllPage = 3;
		
		switch(Page) {
			case 1:
				Messager.sendStringList(sender, Messager.getStringList(
						Messager.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "´É·ÂÀÚ ÀüÀï"),
						ChatColor.translateAlternateColorCodes('&', "&b/" + label + " help <ÆäÀÌÁö> &7·Î ´õ ¸¹Àº ¸í·É¾î¸¦ È®ÀÎÇÏ¼¼¿ä! ( &b" + Page + " ÆäÀÌÁö &7/ &b" + AllPage + " ÆäÀÌÁö &7)"),
						Messager.formatCommand(label, "start", "´É·ÂÀÚ ÀüÀïÀ» ½ÃÀÛ½ÃÅµ´Ï´Ù.", true),
						Messager.formatCommand(label, "stop", "´É·ÂÀÚ ÀüÀïÀ» ÁßÁö½ÃÅµ´Ï´Ù.", true),
						Messager.formatCommand(label, "check", "ÀÚ½ÅÀÇ ´É·ÂÀ» È®ÀÎÇÕ´Ï´Ù.", false),
						Messager.formatCommand(label, "yes", "ÀÚ½ÅÀÇ ´É·ÂÀ» È®Á¤ÇÕ´Ï´Ù.", false),
						Messager.formatCommand(label, "no", "ÀÚ½ÅÀÇ ´É·ÂÀ» º¯°æÇÕ´Ï´Ù.", false)));
				break;
			case 2:
				Messager.sendStringList(sender, Messager.getStringList(
						Messager.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "´É·ÂÀÚ ÀüÀï"),
						ChatColor.translateAlternateColorCodes('&', "&b/" + label + " help <ÆäÀÌÁö> &7·Î ´õ ¸¹Àº ¸í·É¾î¸¦ È®ÀÎÇÏ¼¼¿ä! ( &b" + Page + " ÆäÀÌÁö &7/ &b" + AllPage + " ÆäÀÌÁö &7)"),
						Messager.formatCommand(label, "skip", "¸ğµç À¯ÀúÀÇ ´É·ÂÀ» °­Á¦·Î È®Á¤ÇÕ´Ï´Ù.", true),
						Messager.formatCommand(label, "reload", "´É·ÂÀÚ ÀüÀï ÄÜÇÇ±×¸¦ ¸®·ÎµåÇÕ´Ï´Ù.", true),
						Messager.formatCommand(label, "config", "´É·ÂÀÚ ÀüÀï ÄÜÇÇ±× ¸í·É¾î¸¦ È®ÀÎÇÕ´Ï´Ù.", true),
						Messager.formatCommand(label, "util", "´É·ÂÀÚ ÀüÀï À¯Æ¿ ¸í·É¾î¸¦ È®ÀÎÇÕ´Ï´Ù.", true),
						Messager.formatCommand(label, "script", "´É·ÂÀÚ ÀüÀï ½ºÅ©¸³Æ® ÆíÁıÀ» ½ÃÀÛÇÕ´Ï´Ù.", true)));
				break;
			case 3:
				Messager.sendStringList(sender, Messager.getStringList(
						Messager.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "´É·ÂÀÚ ÀüÀï"),
						ChatColor.translateAlternateColorCodes('&', "&b/" + label + " help <ÆäÀÌÁö> &7·Î ´õ ¸¹Àº ¸í·É¾î¸¦ È®ÀÎÇÏ¼¼¿ä! ( &b" + Page + " ÆäÀÌÁö &7/ &b" + AllPage + " ÆäÀÌÁö &7)"),
						Messager.formatCommand(label, "specialthanks", "´É·ÂÀÚ ÀüÀï ÇÃ·¯±×ÀÎ¿¡ ±â¿©ÇÑ »ç¶÷µéÀ» È®ÀÎÇÕ´Ï´Ù.", false)));
				break;
			default:
				Messager.sendErrorMessage(sender, "Á¸ÀçÇÏÁö ¾Ê´Â ÆäÀÌÁöÀÔ´Ï´Ù.");
				break;
		}
	}

	private void sendHelpConfigCommand(CommandSender sender, String label, Integer Page) {
		int AllPage = 1;
		
		switch(Page) {
			case 1:
				Messager.sendStringList(sender, Messager.getStringList(
						Messager.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "´É·ÂÀÚ ÀüÀï ÄÜÇÇ±×"),
						ChatColor.translateAlternateColorCodes('&', "&b/" + label + " config <ÆäÀÌÁö> &7·Î ´õ ¸¹Àº ¸í·É¾î¸¦ È®ÀÎÇÏ¼¼¿ä! ( &b" + Page + " ÆäÀÌÁö &7/ &b" + AllPage + " ÆäÀÌÁö &7)"),
						Messager.formatCommand(label + " config", "kit", "´É·ÂÀÚ ÀüÀï ±âº»ÅÛÀ» ¼³Á¤ÇÕ´Ï´Ù.", true),
						Messager.formatCommand(label + " config", "spawn", "´É·ÂÀÚ ÀüÀï ½ºÆùÀ» ¼³Á¤ÇÕ´Ï´Ù.", true),
						Messager.formatCommand(label + " config", "inv", "ÃÊ¹İ ¹«ÀûÀ» ¼³Á¤ÇÕ´Ï´Ù.", true),
						Messager.formatCommand(label + " config", "game", "°ÔÀÓÀÇ Àü¹İÀûÀÎ ºÎºĞµéÀ» ¼³Á¤ÇÕ´Ï´Ù.", true),
						Messager.formatCommand(label + " config", "death", "ÇÃ·¹ÀÌ¾î »ç¸Á¿¡ °ü·ÃµÈ ÄÜÇÇ±×¸¦ ¼³Á¤ÇÕ´Ï´Ù.", true)));
				break;
			default:
				Messager.sendErrorMessage(sender, "Á¸ÀçÇÏÁö ¾Ê´Â ÆäÀÌÁöÀÔ´Ï´Ù.");
				break;
		}
	}

	private void sendHelpUtilCommand(CommandSender sender, String label, Integer Page) {
		int AllPage = 2;
		
		switch(Page) {
			case 1:
				Messager.sendStringList(sender, Messager.getStringList(
						Messager.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "´É·ÂÀÚ ÀüÀï À¯Æ¿"),
						ChatColor.translateAlternateColorCodes('&', "&b/" + label + " util <ÆäÀÌÁö> &7·Î ´õ ¸¹Àº ¸í·É¾î¸¦ È®ÀÎÇÏ¼¼¿ä! ( &b" + Page + " ÆäÀÌÁö &7/ &b" + AllPage + " ÆäÀÌÁö &7)"),
						Messager.formatCommand(label + " util", "abi <´ë»ó/@a>", "´ë»ó¿¡°Ô ´É·ÂÀ» ÀÓÀÇ·Î ºÎ¿©ÇÕ´Ï´Ù.", true),
						Messager.formatCommand(label + " util", "spec", "°üÀüÀÚ ¼³Á¤ GUI¸¦ ¶ç¿ó´Ï´Ù.", true),
						Messager.formatCommand(label + " util", "ablist", "´É·ÂÀÚ ¸ñ·ÏÀ» È®ÀÎÇÕ´Ï´Ù.", true),
						Messager.formatCommand(label + " util", "blacklist", "´É·Â ºí·¢¸®½ºÆ® ¼³Á¤ GUI¸¦ ¶ç¿ó´Ï´Ù.", true),
						Messager.formatCommand(label + " util", "resetcool", "ÇÃ·¹ÀÌ¾îµéÀÇ ´É·Â ÄğÅ¸ÀÓÀ» ÃÊ±âÈ­½ÃÅµ´Ï´Ù.", true)));
				break;
			case 2:
				Messager.sendStringList(sender, Messager.getStringList(
						Messager.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "´É·ÂÀÚ ÀüÀï À¯Æ¿"),
						ChatColor.translateAlternateColorCodes('&', "&b/" + label + " util <ÆäÀÌÁö> &7·Î ´õ ¸¹Àº ¸í·É¾î¸¦ È®ÀÎÇÏ¼¼¿ä! ( &b" + Page + " ÆäÀÌÁö &7/ &b" + AllPage + " ÆäÀÌÁö &7)"),
						Messager.formatCommand(label + " util", "kit <´ë»ó>", "´ë»ó¿¡°Ô ±âº»ÅÛÀ» ´Ù½Ã Áö±ŞÇÕ´Ï´Ù.", true)));
				break;
			default:
				Messager.sendErrorMessage(sender, "Á¸ÀçÇÏÁö ¾Ê´Â ÆäÀÌÁöÀÔ´Ï´Ù.");
				break;
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return parseTabComplete(sender, label, args);
	}
	
	private List<String> parseTabComplete(CommandSender sender, String label, String[] args) {
		if(label.equalsIgnoreCase("abilitywar") || label.equalsIgnoreCase("ability")
		|| label.equalsIgnoreCase("aw") || label.equalsIgnoreCase("va")) {
			switch(args.length) {
				case 1:
					ArrayList<String> Complete = Messager.getStringList(
							"start", "stop", "check", "yes", "no",
							"skip", "reload", "config", "util", "script", "specialthanks");
					
					if(args[0].isEmpty()) {
						return Complete;
					} else {
						return Complete.stream().filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
					}
				case 2:
					if(args[0].equalsIgnoreCase("config")) {
						ArrayList<String> Config = Messager.getStringList(
								"kit", "spawn", "inv", "game", "death");
						if(args[1].isEmpty()) {
							return Config;
						} else {
							return Config.stream().filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
						}
					} else if(args[0].equalsIgnoreCase("util")) {
						ArrayList<String> Util = Messager.getStringList(
								"abi", "spec", "ablist", "blacklist", "resetcool", "kit");
						if(args[1].isEmpty()) {
							return Util;
						} else {
							return Util.stream().filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
						}
					} else if(args[0].equalsIgnoreCase("script")) {
						List<String> list = Script.getRegisteredScripts();
						
						if(args[1].isEmpty()) {
							return list;
						} else {
							return list.stream().filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
						}
					}
				case 3:
					if(args[0].equalsIgnoreCase("util") && (args[1].equalsIgnoreCase("abi") || args[1].equalsIgnoreCase("kit"))) {
						ArrayList<String> Players = new ArrayList<String>();
						for(Player p : Bukkit.getOnlinePlayers()) Players.add(p.getName());
						Players.add("@a");
						Players.sort(new Comparator<String>() {
							
							public int compare(String obj1, String obj2) {
								return obj1.compareToIgnoreCase(obj2);
							}
							
						});
						

						if(args[2].isEmpty()) {
							return Players;
						} else {
							return Players.stream().filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase())).collect(Collectors.toList());
						}
					}
					
			}
		}

		return Messager.getStringList();
	}
	
}
