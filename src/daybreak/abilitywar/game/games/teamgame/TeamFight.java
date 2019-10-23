package daybreak.abilitywar.game.games.teamgame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.config.AbilityWarSettings.Settings;
import daybreak.abilitywar.game.events.GameCreditEvent;
import daybreak.abilitywar.game.games.mode.GameManifest;
import daybreak.abilitywar.game.games.mode.PlayerStrategy;
import daybreak.abilitywar.game.games.mode.TeamGame;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.game.manager.AbilitySelect;
import daybreak.abilitywar.game.manager.DeathManager;
import daybreak.abilitywar.game.manager.DefaultKitHandler;
import daybreak.abilitywar.game.manager.InfiniteDurability;
import daybreak.abilitywar.game.manager.SpectatorManager;
import daybreak.abilitywar.game.script.Script;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.thread.AbilityWarThread;
import daybreak.abilitywar.utils.thread.TimerBase;

@GameManifest(Name = "팀 전투", Description = { "§f능력자 전쟁을 팀 대항전으로 플레이할 수 있습니다." })
public class TeamFight extends TeamGame implements DefaultKitHandler {

	public TeamFight() {
		super(new PlayerStrategy() {
			@Override
			public Collection<Player> getPlayers() {
				List<Player> players = new ArrayList<Player>();
				for (Player p : Bukkit.getOnlinePlayers()) {
					if (!SpectatorManager.isSpectator(p.getName())) {
						players.add(p);
					}
				}
				return players;
			}
		});
		setRestricted(Invincible);
	}

	private boolean Invincible = Settings.getInvincibilityEnable();

	private final InfiniteDurability infiniteDurability = new InfiniteDurability();

	private final TimerBase NoHunger = new TimerBase() {

		@Override
		public void onStart() {
			Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&a배고픔 무제한이 적용됩니다."));
		}

		@Override
		public void onProcess(int Seconds) {
			for (Participant p : getParticipants()) {
				p.getPlayer().setFoodLevel(19);
			}
		}

		@Override
		public void onEnd() {
		}
	};

	@Override
	protected void progressGame(Integer Seconds) {
		switch (Seconds) {
		case 1:
			broadcastPlayerList();
			if (getParticipants().size() > 0 && getParticipants().size() % 2 != 0) {
				AbilityWarThread.StopGame();
				Bukkit.broadcastMessage(
						ChatColor.translateAlternateColorCodes('&', "&c팀 전투는 2명 이상의 짝수 인원에서만 플레이할 수 있습니다."));
			}
			break;
		case 5:
			broadcastPluginDescription();
			break;
		case 8:
			this.initializeTeam();
			break;
		case 10:
			broadcastAbilityReady();
			break;
		case 13:
			if (Settings.getDrawAbility()) {
				startAbilitySelect();
			}
			break;
		case 15:
			if (Settings.getDrawAbility()) {
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f모든 참가자가 능력을 &b확정&f했습니다."));
			} else {
				Bukkit.broadcastMessage(
						ChatColor.translateAlternateColorCodes('&', "&f능력자 게임 설정에 따라 &b능력&f을 추첨하지 않습니다."));
			}
			break;
		case 17:
			Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e잠시 후 게임이 시작됩니다."));
			break;
		case 20:
			Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e게임이 &c5&e초 후에 시작됩니다."));
			SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
			break;
		case 21:
			Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e게임이 &c4&e초 후에 시작됩니다."));
			SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
			break;
		case 22:
			Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e게임이 &c3&e초 후에 시작됩니다."));
			SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
			break;
		case 23:
			Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e게임이 &c2&e초 후에 시작됩니다."));
			SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
			break;
		case 24:
			Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e게임이 &c1&e초 후에 시작됩니다."));
			SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
			break;
		case 25:
			GameStart();
			break;
		}
	}

	public void broadcastPlayerList() {
		int Count = 0;

		ArrayList<String> msg = new ArrayList<String>();

		msg.add(ChatColor.translateAlternateColorCodes('&', "&6==== &e게임 참여자 목록 &6===="));
		for (Participant p : getParticipants()) {
			Count++;
			msg.add(ChatColor.translateAlternateColorCodes('&', "&a" + Count + ". &f" + p.getPlayer().getName()));
		}
		msg.add(ChatColor.translateAlternateColorCodes('&', "&e총 인원수 : " + Count + "명"));
		msg.add(ChatColor.translateAlternateColorCodes('&', "&6=========================="));

		for (String m : msg) {
			Bukkit.broadcastMessage(m);
		}
	}

	public void broadcastPluginDescription() {
		ArrayList<String> msg = Messager.asList(
				ChatColor.translateAlternateColorCodes('&', "&cAbilityWar &f- &6능력자 전쟁"),
				ChatColor.translateAlternateColorCodes('&',
						"&e버전 &7: &f" + AbilityWar.getPlugin().getDescription().getVersion()),
				ChatColor.translateAlternateColorCodes('&', "&b개발자 &7: &fDayBreak 새벽"),
				ChatColor.translateAlternateColorCodes('&', "&9디스코드 &7: &fDayBreak&7#5908"));

		GameCreditEvent event = new GameCreditEvent();
		Bukkit.getPluginManager().callEvent(event);

		for (String str : event.getCreditList()) {
			msg.add(str);
		}

		for (String m : msg) {
			Bukkit.broadcastMessage(m);
		}
	}

	public void broadcastAbilityReady() {
		ArrayList<String> msg = Messager.asList(
				ChatColor.translateAlternateColorCodes('&',
						"&f플러그인에 총 &b" + AbilityList.nameValues().size() + "개&f의 능력이 등록되어 있습니다."),
				ChatColor.translateAlternateColorCodes('&', "&7능력을 무작위로 할당합니다..."));

		for (String m : msg) {
			Bukkit.broadcastMessage(m);
		}
	}

	public void GameStart() {
		for (String m : new String[] {
				ChatColor.translateAlternateColorCodes('&', "&e■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■"),
				ChatColor.translateAlternateColorCodes('&', "&f             &cAbilityWar &f- &6능력자 전쟁  "),
				ChatColor.translateAlternateColorCodes('&', "&f                    게임 시작                "),
				ChatColor.translateAlternateColorCodes('&', "&e■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■") }) {
			Bukkit.broadcastMessage(m);
		}

		giveDefaultKit(getParticipants());

		for (Participant p : getParticipants()) {
			if (Settings.getSpawnEnable()) {
				p.getPlayer().teleport(Settings.getSpawnLocation());
			}
		}

		if (Settings.getNoHunger()) {
			NoHunger.setPeriod(1);
			NoHunger.StartTimer();
		} else {
			Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4배고픔 무제한&c이 적용되지 않습니다."));
		}

		if (Invincible) {
			getInvincibility().Start(false);
		} else {
			Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4초반 무적&c이 적용되지 않습니다."));
			for (Participant participant : this.getParticipants()) {
				if (participant.hasAbility()) {
					participant.getAbility().setRestricted(false);
				}
			}
		}

		if (Settings.getInfiniteDurability()) {
			registerListener(infiniteDurability);
		} else {
			Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4내구도 무제한&c이 적용되지 않습니다."));
		}

		for (World w : Bukkit.getWorlds()) {
			if (Settings.getClearWeather()) {
				w.setStorm(false);
			}
		}

		Script.RunAll(this);

		startGame();
	}

	/**
	 * 기본 킷 유저 지급
	 */
	@Override
	public void giveDefaultKit(Player p) {
		List<ItemStack> DefaultKit = Settings.getDefaultKit();

		if (Settings.getInventoryClear()) {
			p.getInventory().clear();
		}

		for (ItemStack is : DefaultKit) {
			p.getInventory().addItem(is);
		}

		p.setLevel(0);
		if (Settings.getStartLevel() > 0) {
			p.giveExpLevels(Settings.getStartLevel());
			SoundLib.ENTITY_PLAYER_LEVELUP.playSound(p);
		}
	}

	@Override
	protected AbilitySelect setupAbilitySelect() {
		return new AbilitySelect(1) {

			@Override
			protected Collection<Participant> initSelectors() {
				return TeamFight.this.getParticipants();
			}

			private List<Class<? extends AbilityBase>> setupAbilities() {
				List<Class<? extends AbilityBase>> list = new ArrayList<>();
				for (String abilityName : AbilityList.nameValues()) {
					if (!Settings.isBlackListed(abilityName)) {
						list.add(AbilityList.getByString(abilityName));
					}
				}

				return list;
			}

			private List<Class<? extends AbilityBase>> abilities;

			@Override
			protected void drawAbility(Collection<Participant> selectors) {
				abilities = setupAbilities();

				if (getSelectors().size() <= abilities.size()) {
					Random random = new Random();

					for (Participant participant : selectors) {
						Player p = participant.getPlayer();

						Class<? extends AbilityBase> abilityClass = abilities.get(random.nextInt(abilities.size()));
						try {
							participant.setAbility(abilityClass);
							abilities.remove(abilityClass);

							p.sendMessage(new String[] {
									ChatColor.translateAlternateColorCodes('&',
											"&a당신에게 능력이 할당되었습니다. &e/ability check&f로 확인 할 수 있습니다."),
									ChatColor.translateAlternateColorCodes('&',
											"&e/ability yes &f명령어를 사용하면 능력을 확정합니다."),
									ChatColor.translateAlternateColorCodes('&',
											"&e/ability no &f명령어를 사용하면 능력을 변경할 수 있습니다.") });
						} catch (Exception e) {
							Messager.sendConsoleErrorMessage(
									ChatColor.translateAlternateColorCodes('&',
											"&e" + p.getName() + "&f님에게 능력을 할당하는 도중 오류가 발생하였습니다."),
									ChatColor.translateAlternateColorCodes('&',
											"&f문제가 발생한 능력: &b" + abilityClass.getName()));
						}
					}
				} else {
					Messager.broadcastErrorMessage("사용 가능한 능력의 수가 참가자의 수보다 적어 게임을 종료합니다.");
					AbilityWarThread.StopGame();
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&7게임이 초기화되었습니다."));
				}
			}

			@Override
			protected boolean changeAbility(Participant participant) {
				Player p = participant.getPlayer();

				if (abilities.size() > 0) {
					Random random = new Random();

					if (participant.hasAbility()) {
						Class<? extends AbilityBase> oldAbilityClass = participant.getAbility().getClass();
						Class<? extends AbilityBase> abilityClass = abilities.get(random.nextInt(abilities.size()));
						try {
							abilities.remove(abilityClass);
							abilities.add(oldAbilityClass);

							participant.setAbility(abilityClass);

							return true;
						} catch (Exception e) {
							Messager.sendConsoleErrorMessage(
									ChatColor.translateAlternateColorCodes('&',
											"&e" + p.getName() + "&f님에게 능력을 할당하는 도중 오류가 발생하였습니다."),
									ChatColor.translateAlternateColorCodes('&',
											"&f문제가 발생한 능력: &b" + abilityClass.getName()));
						}
					}
				} else {
					Messager.sendErrorMessage(p, "능력을 변경할 수 없습니다.");
				}

				return false;
			}

			@Override
			protected void onSelectEnd() {
			}

		};
	}

	@Override
	protected void onGameEnd() {}

	@Override
	protected List<Team> setupTeams() {
		List<Participant> participants = new ArrayList<>(getParticipants());
		int size = participants.size();

		Collections.shuffle(participants, new Random());

		List<Participant> blueMembers = new ArrayList<>(participants.subList(0, (size + 1) / 2));
		Team blueTeam = this.newTeam(ChatColor.BLUE + "파란팀", blueMembers);
		List<Participant> redMembers = new ArrayList<>(participants.subList((size + 1) / 2, size));
		Team redTeam = this.newTeam(ChatColor.RED + "빨간팀", redMembers);

		return Arrays.asList(blueTeam, redTeam);
	}

	@Override
	protected DeathManager setupDeathManager() {
		return new DeathManager(this);
	}

}
