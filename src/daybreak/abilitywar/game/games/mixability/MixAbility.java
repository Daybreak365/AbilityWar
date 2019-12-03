package daybreak.abilitywar.game.games.mixability;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.config.AbilityWarSettings;
import daybreak.abilitywar.game.events.GameCreditEvent;
import daybreak.abilitywar.game.games.mode.AbstractGame;
import daybreak.abilitywar.game.games.mode.GameManifest;
import daybreak.abilitywar.game.games.standard.Game;
import daybreak.abilitywar.game.manager.object.AbilitySelect;
import daybreak.abilitywar.game.manager.object.DeathManager;
import daybreak.abilitywar.game.manager.object.DefaultKitHandler;
import daybreak.abilitywar.game.manager.object.InfiniteDurability;
import daybreak.abilitywar.game.script.Script;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.PlayerCollector;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.message.KoreanUtil;
import daybreak.abilitywar.utils.thread.AbilityWarThread;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

@GameManifest(Name = "믹스 능력자 전쟁 (BETA)", Description = {"§f두가지의 능력을 섞어서 사용하는 게임 모드입니다."})
public class MixAbility extends Game implements DefaultKitHandler {

	public MixAbility() {
		super(PlayerCollector.EVERY_PLAYER_EXCLUDING_SPECTATORS());
		setRestricted(invincible);
	}

    private boolean invincible = AbilityWarSettings.Settings.InvincibilitySettings.isEnabled();

	@Override
	protected void progressGame(int seconds) {
		switch (seconds) {
			case 1:
				ArrayList<String> lines = Messager.asList(ChatColor.translateAlternateColorCodes('&', "&6==== &e게임 참여자 목록 &6===="));
				int count = 0;
				for (Participant p : getParticipants()) {
					count++;
					lines.add(ChatColor.translateAlternateColorCodes('&', "&a" + count + ". &f" + p.getPlayer().getName()));
				}
				lines.add(ChatColor.translateAlternateColorCodes('&', "&e총 인원수 : " + count + "명"));
				lines.add(ChatColor.translateAlternateColorCodes('&', "&6=========================="));

				for (String line : lines) {
					Bukkit.broadcastMessage(line);
				}

				if (getParticipants().size() < 1) {
					AbilityWarThread.StopGame();
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c최소 참가자 수를 충족하지 못하여 게임을 중지합니다. &8(&71명&8)"));
				}
				break;
			case 3:
				lines = Messager.asList(
						ChatColor.translateAlternateColorCodes('&', "&cAbilityWar &f- &6능력자 전쟁"),
						ChatColor.translateAlternateColorCodes('&', "&e버전 &7: &f" + AbilityWar.getPlugin().getDescription().getVersion()),
						ChatColor.translateAlternateColorCodes('&', "&b개발자 &7: &fDaybreak 새벽"),
						ChatColor.translateAlternateColorCodes('&', "&9디스코드 &7: &f새벽&7#5908")
				);

				GameCreditEvent event = new GameCreditEvent();
				Bukkit.getPluginManager().callEvent(event);
				lines.addAll(event.getCreditList());

				for (String line : lines) {
					Bukkit.broadcastMessage(line);
				}
				break;
			case 5:
				try {
					for(Participant p : getParticipants()) {
						p.setAbility(Mix.class);
					}
				} catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException ignored) {}
				break;
			case 6:
				if (AbilityWarSettings.Settings.getDrawAbility()) {
					startAbilitySelect();
				}
				break;
			case 7:
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e잠시 후 게임이 시작됩니다."));
				break;
			case 9:
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e게임이 &c5&e초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 10:
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e게임이 &c4&e초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 11:
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e게임이 &c3&e초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 12:
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e게임이 &c2&e초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 13:
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e게임이 &c1&e초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 14:
				for (String line : Messager.asList(
						ChatColor.translateAlternateColorCodes('&', "&e■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■"),
						ChatColor.translateAlternateColorCodes('&', "&f             &cMixAbility &f- &6믹스 능력자  "),
						ChatColor.translateAlternateColorCodes('&', "&f                    게임 시작                "),
						ChatColor.translateAlternateColorCodes('&', "&e■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■"))) {
					Bukkit.broadcastMessage(line);
				}

				giveDefaultKit(getParticipants());

				if (AbilityWarSettings.Settings.getSpawnEnable()) {
					Location spawn = AbilityWarSettings.Settings.getSpawnLocation();
					for (Participant participant : getParticipants()) {
						participant.getPlayer().teleport(spawn);
					}
				}

				if (AbilityWarSettings.Settings.getNoHunger()) {
					new TimerBase() {
						@Override
						public void onStart() {
							Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&a배고픔 무제한이 적용됩니다."));
						}

						@Override
						public void onProcess(int count) {
							for (Participant participant : getParticipants()) {
								participant.getPlayer().setFoodLevel(19);
							}
						}
					}.setPeriod(1).startTimer();
				} else {
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4배고픔 무제한&c이 적용되지 않습니다."));
				}

				if (AbilityWarSettings.Settings.getInfiniteDurability()) {
					attachObserver(new InfiniteDurability());
				} else {
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4내구도 무제한&c이 적용되지 않습니다."));
				}

				if (AbilityWarSettings.Settings.getClearWeather()) {
					for (World w : Bukkit.getWorlds()) {
						w.setStorm(false);
					}
				}

				if (invincible) {
					getInvincibility().Start(false);
				} else {
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4초반 무적&c이 적용되지 않습니다."));
					setRestricted(false);
				}

				Script.RunAll(this);

				startGame();
				break;
		}
	}

	@Override
	public void executeCommand(CommandType commandType, Player player, String[] args, Plugin plugin) {
		switch (commandType) {
			case ABI:
				if (args[0].equalsIgnoreCase("@a")) {
					MixAbilityGUI gui = new MixAbilityGUI(player, plugin);
					gui.openAbilityGUI(1);
				} else {
					Player targetPlayer = Bukkit.getPlayerExact(args[0]);
					if (targetPlayer != null) {
						AbstractGame game = AbilityWarThread.getGame();
						if (game.isParticipating(targetPlayer)) {
							AbstractGame.Participant target = game.getParticipant(targetPlayer);
							MixAbilityGUI gui = new MixAbilityGUI(player, target, plugin);
							gui.openAbilityGUI(1);
						} else {
							Messager.sendErrorMessage(player, targetPlayer.getName() + "님은 탈락했거나 게임에 참여하지 않았습니다.");
						}
					} else {
						Messager.sendErrorMessage(player, args[0] + "은(는) 존재하지 않는 플레이어입니다.");
					}
				}
				break;
			case ABLIST:
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2===== &a능력자 목록 &2====="));
				int count = 0;
				for (AbstractGame.Participant participant : AbilityWarThread.getGame().getParticipants()) {
					Mix mix = (Mix) participant.getAbility();
					if (mix.hasAbility()) {
						count++;
						player.sendMessage(ChatColor.translateAlternateColorCodes('&',
								"&e" + count + ". &f" + participant.getPlayer().getName() + " &7: &c" + mix.getFirst().getName() + " &f+ &c" + mix.getSecond().getName()));
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

    @Override
    protected AbilitySelect setupAbilitySelect() {
        return null;
    }

    @Override
	protected DeathManager setupDeathManager() {
		return new DeathManager(this) {
			@Override
			protected String AbilityReveal(Participant victim) {
				Mix mix = (Mix) victim.getAbility();
				if (mix.hasAbility()) {
					String name = mix.getFirst().getName() + " + " + mix.getSecond().getName();
					return ChatColor.translateAlternateColorCodes('&',
							"&f[&c능력&f] &c" + victim.getPlayer().getName() + "&f님의 능력은 &e" + name + "&f" + KoreanUtil.getNeededJosa(name, KoreanUtil.Josa.이었였) + "습니다.");
				} else {
					return ChatColor.translateAlternateColorCodes('&',
							"&f[&c능력&f] &c" + victim.getPlayer().getName() + "&f님은 능력이 없습니다.");
				}
			}
		};
	}

}
