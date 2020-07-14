package daybreak.abilitywar.game.list.blind;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityBase.CooldownTimer;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.Configuration.Settings.DeathSettings;
import daybreak.abilitywar.config.Configuration.Settings.InvincibilitySettings;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.Observer;
import daybreak.abilitywar.game.Game;
import daybreak.abilitywar.game.GameAliases;
import daybreak.abilitywar.game.GameManager;
import daybreak.abilitywar.game.GameManifest;
import daybreak.abilitywar.game.event.GameCreditEvent;
import daybreak.abilitywar.game.interfaces.Winnable;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.game.manager.effect.Bleed;
import daybreak.abilitywar.game.manager.effect.Stun;
import daybreak.abilitywar.game.manager.object.AbilitySelect;
import daybreak.abilitywar.game.manager.object.DeathManager;
import daybreak.abilitywar.game.manager.object.DefaultKitHandler;
import daybreak.abilitywar.game.manager.object.InfiniteDurability;
import daybreak.abilitywar.game.manager.object.Invincibility;
import daybreak.abilitywar.game.script.manager.ScriptManager;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil.Josa;
import daybreak.abilitywar.utils.base.logging.Logger;
import daybreak.abilitywar.utils.base.minecraft.PlayerCollector;
import daybreak.abilitywar.utils.base.minecraft.compat.nms.NMS;
import daybreak.abilitywar.utils.library.SoundLib;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import javax.naming.OperationNotSupportedException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

@GameManifest(name = "블라인드 능력 전쟁", description = {
		"§f능력 설명이 보이지 않는다!",
		"§f내 능력을 모르는 상태로 플레이하는 능력자 게임",
		"§f추가 컨텐츠가 있습니다.",
		"§f모두를 탈락시키고 최후의 1인으로 남는 플레이어가 승리합니다.", "",
		"§a● §f일부 콘피그가 임의로 변경될 수 있습니다."
})
@GameAliases({"블능전", "블라인드"})
public class BlindAbilityWar extends Game implements DefaultKitHandler, Winnable, Observer {

	private static final Logger logger = Logger.getLogger(BlindAbilityWar.class);

	private static final Random random = new Random();
	private final GameTimer blindRoulette = new GameTimer(TaskType.INFINITE, -1) {
		@Override
		protected void run(int count) {
			final List<Participant> participants = new ArrayList<>(getParticipants().size());
			for (Participant participant : getParticipants()) {
				if (getDeathManager().isExcluded(participant.getPlayer()) || !participant.hasAbility()) continue;
				participants.add(participant);
			}
			final Note C = Note.natural(0, Tone.C), E = Note.natural(0, Tone.E);
			final ChatColor[] chatColors = {
					ChatColor.YELLOW,
					ChatColor.RED,
					ChatColor.GOLD,
					ChatColor.LIGHT_PURPLE,
					ChatColor.DARK_PURPLE
			};
			new GameTimer(TaskType.NORMAL, 15) {
				@Override
				protected void run(int count) {
					final String title = chatColors[random.nextInt(chatColors.length)] + participants.get(random.nextInt(participants.size())).getPlayer().getName();
					for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
						NMS.sendTitle(onlinePlayer, title, "", 0, 6, 0);
						SoundLib.PIANO.playInstrument(onlinePlayer, C);
						SoundLib.PIANO.playInstrument(onlinePlayer, E);
					}
				}

				@Override
				protected void onEnd() {
					final Participant target = participants.get(random.nextInt(participants.size()));
					final String title = "§e" + target.getPlayer().getName();
					for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
						NMS.sendTitle(onlinePlayer, title, "", 0, 40, 0);
					}
					final Roulette[] roulettes = Roulette.values();
					new GameTimer(TaskType.NORMAL, 15) {
						@Override
						protected void run(int count) {
							final String subtitle = chatColors[random.nextInt(chatColors.length)] + roulettes[random.nextInt(roulettes.length)].getDisplayName(participants.get(random.nextInt(participants.size())));
							for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
								NMS.sendTitle(onlinePlayer, title, subtitle, 0, 6, 0);
								SoundLib.PIANO.playInstrument(onlinePlayer, C);
								SoundLib.PIANO.playInstrument(onlinePlayer, E);
							}
						}

						@Override
						protected void onEnd() {
							final Roulette roulette = roulettes[random.nextInt(roulettes.length)];
							final Participant castingTarget = participants.get(random.nextInt(participants.size()));
							final String subtitle = ChatColor.GOLD + roulette.getDisplayName(castingTarget);
							for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
								NMS.sendTitle(onlinePlayer, title, subtitle, 0, 5, 0);
							}
							new GameTimer(TaskType.NORMAL, 5) {
								ChatColor first = ChatColor.GOLD;
								ChatColor second = ChatColor.RED;

								@Override
								protected void run(int count) {
									for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
										NMS.sendTitle(onlinePlayer, first + target.getPlayer().getName(), second + roulette.getDisplayName(castingTarget), 0, 11, 0);
									}
									if (first == ChatColor.GOLD) {
										this.first = ChatColor.RED;
										this.second = ChatColor.GOLD;
									} else {
										this.first = ChatColor.GOLD;
										this.second = ChatColor.RED;
									}
								}

								@Override
								protected void onEnd() {
									for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
										NMS.clearTitle(onlinePlayer);
									}
									roulette.apply(target, castingTarget);
								}
							}.setPeriod(TimeUnit.TICKS, 10).start();
						}
					}.setInitialDelay(TimeUnit.SECONDS, 1).setPeriod(TimeUnit.TICKS, 2).start();
				}
			}.setPeriod(TimeUnit.TICKS, 2).start();
		}

		@Override
		protected void onEnd() {
			for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
				NMS.clearTitle(onlinePlayer);
			}
		}

		@Override
		protected void onSilentEnd() {
			for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
				NMS.clearTitle(onlinePlayer);
			}
		}
	}.setInitialDelay(TimeUnit.SECONDS, 15).setPeriod(TimeUnit.SECONDS, 30);

	public BlindAbilityWar() {
		super(PlayerCollector.EVERY_PLAYER_EXCLUDING_SPECTATORS());
		setRestricted(InvincibilitySettings.isEnabled());
		attachObserver(this);
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
	}

	private static List<String> getBlindInfo(AbilityBase ability) {
		List<String> list = Messager.asList(
				Formatter.formatTitle(32, ChatColor.GREEN, ChatColor.YELLOW, "능력 정보"),
				"§b§kBLIND§r " + (ability.isRestricted() ? "§f[§7능력 비활성화됨§f]" : "§f[§a능력 활성화됨§f]") + " §8§k---§r §7§kBLIND§r",
				"§kBLINDBLINDBLINDBLINDBLINDBLIND");
		list.add("§a--------------------------------");
		return list;
	}

	@Override
	protected void progressGame(int seconds) {
		switch (seconds) {
			case 1:
				List<String> lines = Messager.asList("§8==== §7게임 참여자 목록 §8====");
				int count = 0;
				for (Participant p : getParticipants()) {
					count++;
					lines.add("§0" + count + ". §f" + p.getPlayer().getName());
				}
				lines.add("§7총 인원수 : " + count + "명");
				lines.add("§8==========================");

				for (String line : lines) {
					Bukkit.broadcastMessage(line);
				}

				if (getParticipants().size() < 2) {
					stop();
					Bukkit.broadcastMessage("§c최소 참가자 수를 충족하지 못하여 게임을 중지합니다. §8(§72명§8)");
				}
				break;
			case 3:
				lines = Messager.asList(
						"§0Blind §f- §8블라인드 능력 전쟁",
						"§7버전 §7: §f" + AbilityWar.getPlugin().getDescription().getVersion(),
						"§7개발자 §7: §fDaybreak 새벽",
						"§9디스코드 §7: §f새벽§7#5908"
				);

				GameCreditEvent event = new GameCreditEvent(this);
				Bukkit.getPluginManager().callEvent(event);
				lines.addAll(event.getCredits());

				for (String line : lines) {
					Bukkit.broadcastMessage(line);
				}
				break;
			case 5:
				if (Settings.getDrawAbility()) {
					for (String line : Messager.asList(
							"§f플러그인에 총 §7" + AbilityList.nameValues().size() + "개§f의 능력이 등록되어 있습니다.",
							"§7능력을 무작위로 할당합니다...")) {
						Bukkit.broadcastMessage(line);
					}
					try {
						startAbilitySelect();
					} catch (OperationNotSupportedException ignored) {
					}
				}
				break;
			case 6:
				if (Settings.getDrawAbility()) {
					Bukkit.broadcastMessage("§f모든 참가자가 능력을 §7확정§f했습니다.");
				} else {
					Bukkit.broadcastMessage("§f능력자 게임 설정에 따라 §7능력§f을 추첨하지 않습니다.");
				}
				break;
			case 8:
				Bukkit.broadcastMessage("§7잠시 후 게임이 시작됩니다.");
				break;
			case 10:
				Bukkit.broadcastMessage("§7게임이 §05§7초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 11:
				Bukkit.broadcastMessage("§7게임이 §04§7초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 12:
				Bukkit.broadcastMessage("§7게임이 §03§7초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 13:
				Bukkit.broadcastMessage("§7게임이 §02§7초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 14:
				Bukkit.broadcastMessage("§7게임이 §01§7초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 15:
				for (String line : Messager.asList(
						"§7■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■",
						"§f             §0Blind §f- §8블라인드 능력 전쟁  ",
						"§f                    게임 시작                ",
						"§7■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■")) {
					Bukkit.broadcastMessage(line);
				}

				giveDefaultKit(getParticipants());

				if (Settings.getSpawnEnable()) {
					Location spawn = Settings.getSpawnLocation();
					for (Participant participant : getParticipants()) {
						participant.getPlayer().teleport(spawn);
					}
				}

				if (Settings.getNoHunger()) {
					Bukkit.broadcastMessage("§2배고픔 무제한§a이 적용됩니다.");
				} else {
					Bukkit.broadcastMessage("§4배고픔 무제한§0이 적용되지 않습니다.");
				}

				if (Settings.getInfiniteDurability()) {
					attachObserver(new InfiniteDurability());
				} else {
					Bukkit.broadcastMessage("§4내구도 무제한§0이 적용되지 않습니다.");
				}

				if (Settings.getClearWeather()) {
					for (World w : Bukkit.getWorlds()) {
						w.setStorm(false);
					}
				}

				getInvincibility().attachObserver(new Invincibility.Observer() {
					@Override
					public void onStart() {
						blindRoulette.stop(false);
					}

					@Override
					public void onEnd() {
						blindRoulette.start();
					}
				});
				if (isRestricted()) {
					getInvincibility().start(false);
				} else {
					Bukkit.broadcastMessage("§4초반 무적§0이 적용되지 않습니다.");
					setRestricted(false);
				}

				ScriptManager.runAll(this);

				startGame();
				break;
		}
	}

	@EventHandler
	private void onPlayerQuit(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		if (isParticipating(player)) {
			Participant quitParticipant = getParticipant(player);
			getDeathManager().Operation(quitParticipant);
			Player winner = null;
			for (Participant participant : getParticipants()) {
				if (!getDeathManager().isExcluded(player)) {
					if (winner == null) {
						winner = player;
					} else {
						return;
					}
				}
			}
			if (winner != null) Win(getParticipant(winner));
		}
	}

	@Override
	public DeathManager newDeathManager() {
		return new DeathManager(this) {
			public void Operation(Participant victim) {
				switch (DeathSettings.getOperation()) {
					case 탈락:
						Eliminate(victim);
						excludedPlayers.add(victim.getPlayer().getUniqueId());
						break;
					case 관전모드:
					case 없음:
						victim.getPlayer().setGameMode(GameMode.SPECTATOR);
						excludedPlayers.add(victim.getPlayer().getUniqueId());
						break;
				}
				Player winner = null;
				for (Participant participant : getParticipants()) {
					Player player = participant.getPlayer();
					if (!isExcluded(player)) {
						if (winner == null) {
							winner = player;
						} else {
							return;
						}
					}
				}
				if (winner != null) Win(getParticipant(winner));
			}
		};
	}

	@Override
	public void executeCommand(CommandType commandType, CommandSender sender, String command, String[] args, Plugin plugin) {
		if (commandType == CommandType.ABILITY_CHECK) {
			final Player player = (Player) sender;
			if (GameManager.isGameRunning()) {
				final AbstractGame game = GameManager.getGame();
				if (game.isParticipating(player)) {
					final Participant participant = game.getParticipant(player);
					if (participant.hasAbility()) {
						for (String line : getBlindInfo(participant.getAbility())) {
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
		} else super.executeCommand(commandType, sender, command, args, plugin);
	}

	@Override
	public AbilitySelect newAbilitySelect() {
		return new AbilitySelect(this, getParticipants(), 2) {

			private List<Class<? extends AbilityBase>> abilities;

			@Override
			protected void drawAbility(Collection<? extends Participant> selectors) {
				abilities = AbilitySelectStrategy.EVERY_ABILITY_EXCLUDING_BLACKLISTED.getAbilities();
				if (getSelectors().size() <= abilities.size()) {
					Random random = new Random();

					for (Participant participant : selectors) {
						Class<? extends AbilityBase> abilityClass = abilities.get(random.nextInt(abilities.size()));
						try {
							participant.setAbility(abilityClass);
							abilities.remove(abilityClass);

							participant.getPlayer().sendMessage(new String[]{
									"§7능력이 할당되었습니다. §8/aw check§f로 확인 할 수 있습니다.",
									"§8/aw yes §f명령어를 사용하여 능력을 확정합니다.",
									"§8/aw no §f명령어를 사용하여 능력을 변경합니다."
							});
						} catch (IllegalAccessException | SecurityException | InstantiationException | IllegalArgumentException | InvocationTargetException e) {
							logger.error(ChatColor.YELLOW + participant.getPlayer().getName() + ChatColor.WHITE + "님에게 능력을 할당하는 도중 오류가 발생하였습니다.");
							logger.error("문제가 발생한 능력: " + ChatColor.AQUA + abilityClass.getName());
						}
					}
				} else if (abilities.size() > 0) {
					Random random = new Random();

					for (Participant participant : selectors) {
						Class<? extends AbilityBase> abilityClass = abilities.get(random.nextInt(abilities.size()));
						try {
							participant.setAbility(abilityClass);
							participant.getPlayer().sendMessage(new String[]{
									"§7능력이 할당되었습니다. §8/aw check§f로 확인 할 수 있습니다.",
									"§8/aw yes §f명령어를 사용하여 능력을 확정합니다.",
									"§8/aw no §f명령어를 사용하여 능력을 변경합니다."
							});
						} catch (IllegalAccessException | SecurityException | InstantiationException | IllegalArgumentException | InvocationTargetException e) {
							logger.error(ChatColor.YELLOW + participant.getPlayer().getName() + ChatColor.WHITE + "님에게 능력을 할당하는 도중 오류가 발생하였습니다.");
							logger.error("문제가 발생한 능력: " + ChatColor.AQUA + abilityClass.getName());
						}
					}
				} else {
					Messager.broadcastErrorMessage("사용 가능한 능력이 없습니다.");
					GameManager.stopGame();
				}
			}

			@Override
			protected boolean changeAbility(Participant participant) {
				Player p = participant.getPlayer();

				if (abilities.size() > 0) {
					Random random = new Random();

					if (participant.hasAbility()) {
						final AbilityRegistration oldAbility = participant.getAbility().getRegistration();
						final Class<? extends AbilityBase> abilityClass = abilities.get(random.nextInt(abilities.size()));
						try {
							abilities.remove(abilityClass);
							abilities.add(oldAbility.getAbilityClass());

							participant.getPlayer().sendMessage("§7바꾸기 전의 능력은 §8" + oldAbility.getManifest().name() + "§7" + KoreanUtil.getJosa(oldAbility.getManifest().name(), Josa.이었였) + "습니다.");
							participant.setAbility(abilityClass);

							return true;
						} catch (Exception e) {
							logger.error(ChatColor.YELLOW + p.getName() + ChatColor.WHITE + "님의 능력을 변경하는 도중 오류가 발생하였습니다.");
							logger.error(ChatColor.WHITE + "문제가 발생한 능력: " + ChatColor.AQUA + abilityClass.getName());
						}
					}
				} else {
					Messager.sendErrorMessage(p, "능력을 변경할 수 없습니다.");
				}

				return false;
			}
		};
	}

	@Override
	public void update(GameUpdate update) {
		if (update == GameUpdate.END) {
			HandlerList.unregisterAll(this);
		}
	}

	private enum Roulette {
		ABILITY_RANK_REVEAL {
			@Override
			protected void apply(Participant target, Participant castingTarget) {
				if (target.hasAbility()) {
					Bukkit.broadcastMessage("§e" + target.getPlayer().getName() + "§f님의 능력 등급은 " + target.getAbility().getRank().getRankName() + "§f입니다.");
				}
			}

			@Override
			protected String getDisplayName(Participant castingTarget) {
				return "능력 등급 공개";
			}
		},
		ABILITY_SPECIES_REVEAL {
			@Override
			protected void apply(Participant target, Participant castingTarget) {
				if (target.hasAbility()) {
					Bukkit.broadcastMessage("§e" + target.getPlayer().getName() + "§f님의 능력 종은 " + target.getAbility().getSpecies().getSpeciesName() + "§f입니다.");
				}
			}

			@Override
			protected String getDisplayName(Participant castingTarget) {
				return "능력 종 공개";
			}
		},
		ONE_SECOND_STUN {
			@Override
			protected void apply(Participant target, Participant castingTarget) {
				Stun.apply(target, TimeUnit.SECONDS, 1);
			}

			@Override
			protected String getDisplayName(Participant castingTarget) {
				return "1초 스턴";
			}
		},
		TWO_SECONDS_STUN {
			@Override
			protected void apply(Participant target, Participant castingTarget) {
				Stun.apply(target, TimeUnit.SECONDS, 2);
			}

			@Override
			protected String getDisplayName(Participant castingTarget) {
				return "2초 스턴";
			}
		},
		THREE_SECONDS_STUN {
			@Override
			protected void apply(Participant target, Participant castingTarget) {
				Stun.apply(target, TimeUnit.SECONDS, 3);
			}

			@Override
			protected String getDisplayName(Participant castingTarget) {
				return "3초 스턴";
			}
		},
		FOUR_SECONDS_STUN {
			@Override
			protected void apply(Participant target, Participant castingTarget) {
				Stun.apply(target, TimeUnit.SECONDS, 4);
			}

			@Override
			protected String getDisplayName(Participant castingTarget) {
				return "4초 스턴";
			}
		},
		FIVE_SECONDS_BLEED {
			@Override
			protected void apply(Participant target, Participant castingTarget) {
				Bleed.apply(target, TimeUnit.SECONDS, 5);
			}

			@Override
			protected String getDisplayName(Participant castingTarget) {
				return "5초 출혈";
			}
		},
		SIX_SECONDS_BLEED {
			@Override
			protected void apply(Participant target, Participant castingTarget) {
				Bleed.apply(target, TimeUnit.SECONDS, 6);
			}

			@Override
			protected String getDisplayName(Participant castingTarget) {
				return "6초 출혈";
			}
		},
		FULL_HEAL {
			@Override
			protected void apply(Participant target, Participant castingTarget) {
				target.getPlayer().setHealth(target.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
			}

			@Override
			protected String getDisplayName(Participant castingTarget) {
				return "체력 풀 회복";
			}
		},
		THREE_HEART_HEAL {
			@Override
			protected void apply(Participant target, Participant castingTarget) {
				target.getPlayer().setHealth(Math.min(target.getPlayer().getHealth() + 6, target.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
			}

			@Override
			protected String getDisplayName(Participant castingTarget) {
				return "체력 세 칸 회복";
			}
		},
		FOUR_HEART_HEAL {
			@Override
			protected void apply(Participant target, Participant castingTarget) {
				target.getPlayer().setHealth(Math.min(target.getPlayer().getHealth() + 8, target.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
			}

			@Override
			protected String getDisplayName(Participant castingTarget) {
				return "체력 네 칸 회복";
			}
		},
		ABSORB_TWO_HEARTS {
			@Override
			protected void apply(Participant target, Participant castingTarget) {
				NMS.setAbsorptionHearts(target.getPlayer(), NMS.getAbsorptionHearts(target.getPlayer()) + 4);
			}

			@Override
			protected String getDisplayName(Participant castingTarget) {
				return "흡수 체력 두 칸 추가";
			}
		},
		TELEPORT_RANDOM {
			@Override
			protected void apply(Participant target, Participant castingTarget) {
				target.getPlayer().teleport(castingTarget.getPlayer());
			}

			@Override
			protected String getDisplayName(Participant castingTarget) {
				return castingTarget.getPlayer().getName() + "에게 순간 이동";
			}
		},
		TELEPORT_HERE_RANDOM {
			@Override
			protected void apply(Participant target, Participant castingTarget) {
				castingTarget.getPlayer().teleport(target.getPlayer());
			}

			@Override
			protected String getDisplayName(Participant castingTarget) {
				return castingTarget.getPlayer().getName() + KoreanUtil.getJosa(castingTarget.getPlayer().getName(), Josa.을를) + " 내 위치로 순간 이동";
			}
		},
		RESET_COOLDOWN {
			@Override
			protected void apply(Participant target, Participant castingTarget) {
				if (target.hasAbility()) {
					for (GameTimer timer : target.getAbility().getTimers()) {
						if (timer instanceof CooldownTimer) {
							timer.stop(false);
						}
					}
				}
			}

			@Override
			protected String getDisplayName(Participant castingTarget) {
				return "쿨타임 초기화";
			}
		};

		protected abstract void apply(Participant target, Participant castingTarget);

		protected abstract String getDisplayName(Participant castingTarget);
	}

}
