package daybreak.abilitywar.game.list.mix.changemix;

import com.google.common.base.Strings;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.game.event.GameCreditEvent;
import daybreak.abilitywar.game.list.changeability.ChangeAbilityWar;
import daybreak.abilitywar.game.list.mix.AbstractTeamMix;
import daybreak.abilitywar.game.list.mix.Mix;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;
import daybreak.abilitywar.game.list.mix.synergy.SynergyFactory;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.game.manager.object.AbilitySelect;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.module.Invincibility;
import daybreak.abilitywar.game.team.TeamGame.Winnable;
import daybreak.abilitywar.game.team.event.ParticipantTeamChangedEvent;
import daybreak.abilitywar.game.team.event.TeamCreatedEvent;
import daybreak.abilitywar.game.team.event.TeamRemovedEvent;
import daybreak.abilitywar.game.team.interfaces.Members;
import daybreak.abilitywar.game.module.InfiniteDurability;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.TimeUtil;
import daybreak.abilitywar.utils.base.collect.Pair;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil;
import daybreak.abilitywar.utils.base.minecraft.PlayerCollector;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TeamChangeMix extends AbstractTeamMix implements Winnable {

	private final boolean invincible = Settings.InvincibilitySettings.isEnabled();

	private final Objective lifeObjective = getScoreboardManager().registerNewObjective("생명", "dummy", "§c생명");

	private final MixAbilityChanger changer = addModule(new MixAbilityChanger(this));
	private final int maxLife;
	private final Set<Members> noLife = new HashSet<Members>() {
		@Override
		public boolean add(Members members) {
			if (super.add(members)) {
				if (lifeObjective.getScoreboard() == null) return true;
				lifeObjective.getScore(getScoreboardName(members)).setScore(0);
				for (Participant member : members.getMembers()) {
					getDeathManager().Operation(member);
				}
				return true;
			} else return false;
		}
	};

	public TeamChangeMix(final String[] args) {
		super(PlayerCollector.EVERY_PLAYER_EXCLUDING_SPECTATORS(), args);
		this.maxLife = ChangeAbilityWar.MAX_LIFE.getValue();
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
	}

	@EventHandler
	private void onTeamCreated(TeamCreatedEvent e) {
		lifeObjective.getScore(getScoreboardName(e.getTeam())).setScore(maxLife);
	}

	@EventHandler
	private void onTeamRemoved(TeamRemovedEvent e) {
		lifeObjective.getScore(getScoreboardName(e.getTeam())).setScore(0);
	}

	@EventHandler
	private void onParticipantTeamChanged(ParticipantTeamChangedEvent e) {
		if (isGameStarted()) checkWinner();
	}

	private String getScoreboardName(final Members team) {
		return team.getName() + "§8(" + team.getDisplayName() + "§8)";
	}

	@Override
	protected void progressGame(int seconds) {
		switch (seconds) {
			case 1:
				List<String> lines = Messager.asList("§d==== §f게임 참여자 목록 §d====");
				int count = 0;
				for (Participant p : getParticipants()) {
					count++;
					lines.add("§5" + count + ". §f" + p.getPlayer().getName());
				}
				lines.add("§f총 인원수 §5: §d" + count + "명");
				lines.add("§d==========================");

				for (String line : lines) {
					Bukkit.broadcastMessage(line);
				}
				if (getParticipants().size() < 2) {
					stop();
					Bukkit.broadcastMessage("§c최소 참가자 수를 충족하지 못하여 게임을 중지합니다. §8(§72명§8)");
				}
				break;
			case 3:
				ArrayList<String> msg = new ArrayList<>();
				msg.add("§5§l체인지! §d§l믹스 §f§l전쟁");
				msg.add("§e플러그인 버전 §7: §f" + AbilityWar.getPlugin().getDescription().getVersion());
				msg.add("§b모드 개발자 §7: §fDaybreak 새벽");
				msg.add("§9디스코드 §7: §f새벽§7#5908");

				GameCreditEvent event = new GameCreditEvent(this);
				Bukkit.getPluginManager().callEvent(event);
				msg.addAll(event.getCredits());

				for (String m : msg) {
					Bukkit.broadcastMessage(m);
				}
				break;
			case 5:
				Bukkit.broadcastMessage("§f플러그인에 총 §d" + AbilityList.nameValues().size() + "개§f의 능력이 등록되어 있습니다.");
				Bukkit.broadcastMessage("§7게임 시작시 §f첫번째 능력§7이 할당되며, 이후 §f" + TimeUtil.parseTimeAsString(changer.getPeriod()) + "§7마다 능력이 변경됩니다.");
				break;
			case 7:
				Bukkit.broadcastMessage("§d잠시 후 §f게임이 시작됩니다.");
				break;
			case 8:
				Bukkit.broadcastMessage("§f게임이 §55§f초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 9:
				Bukkit.broadcastMessage("§f게임이 §54§f초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 10:
				Bukkit.broadcastMessage("§f게임이 §53§f초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 11:
				Bukkit.broadcastMessage("§f게임이 §52§f초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 12:
				Bukkit.broadcastMessage("§f게임이 §51§f초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 13:
				for (String m : new String[]{
						"§d■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■",
						"§f                §5§l체인지! §d§l믹스 §f§l전쟁",
						"§f                    게임 시작                ",
						"§d■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■"}) {
					Bukkit.broadcastMessage(m);
				}
				SoundLib.ENTITY_WITHER_SPAWN.broadcastSound();

				giveDefaultKit(getParticipants());

				if (Settings.getNoHunger()) {
					Bukkit.broadcastMessage("§2배고픔 무제한§a이 적용됩니다.");
				} else {
					Bukkit.broadcastMessage("§4배고픔 무제한§c이 적용되지 않습니다.");
				}

				getInvincibility().attachObserver(new Invincibility.Observer() {
					@Override
					public void onStart() {
						changer.stop();
					}

					@Override
					public void onEnd() {
						changer.start();
					}
				});
				if (invincible) {
					getInvincibility().start(false);
				} else {
					changer.start();
					Bukkit.broadcastMessage("§4초반 무적§c이 적용되지 않습니다.");
					setRestricted(false);
				}

				if (Settings.getInfiniteDurability()) {
					addModule(new InfiniteDurability());
				} else {
					Bukkit.broadcastMessage("§4내구도 무제한§c이 적용되지 않습니다.");
				}

				for (World w : Bukkit.getWorlds()) {
					if (Settings.getClearWeather()) {
						w.setStorm(false);
					}
				}

				startGame();
				Bukkit.broadcastMessage("§7스코어보드 §f설정 중...");
				lifeObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
				for (Members team : getTeams()) {
					lifeObjective.getScore(getScoreboardName(team)).setScore(maxLife);
				}
				break;
			case 14:
				checkWinner();
				break;
		}
	}

	@EventHandler
	private void onPlayerQuit(PlayerQuitEvent e) {
		final Participant quit = getParticipant(e.getPlayer());
		if (quit != null) {
			final Members team = getTeam(quit);
			if (team != null && lifeObjective.getScoreboard() != null) {
				final Score score = lifeObjective.getScore(getScoreboardName(team));
				if (score.isScoreSet()) {
					getDeathManager().Operation(quit);
					if (team.isExcluded() || isAllOffline(team, e.getPlayer())) {
						score.setScore(0);
						noLife.add(team);
					}
				}
				checkWinner();
			}
		}
	}

	private void checkWinner() {
		Members winTeam = null;
		for (Members team : getTeams()) {
			if (!noLife.contains(team)) {
				if (isAllOffline(team, null) || team.isExcluded()) {
					noLife.add(team);
					continue;
				}
				if (winTeam == null) {
					winTeam = team;
				} else return;
			}
		}
		if (winTeam == null) Win("§f없음 §8(§7무승부§8)"); else Win(winTeam);
	}

	private boolean isAllOffline(final Members team, final Player quit) {
		for (Participant member : team.getMembers()) {
			if (member.getPlayer().isOnline() && !member.getPlayer().equals(quit)) return false;
		}
		return true;
	}

	@Override
	protected @NotNull DeathManager newDeathManager() {
		return new DeathManager(this) {
			@Override
			public void Operation(Participant victim) {
				final Members team = getTeam(victim);
				try {
					final Score score = lifeObjective.getScore(getScoreboardName(team));
					if (score.isScoreSet()) {
						int life = score.getScore();
						if (life >= 1) {
							score.setScore(--life);
							if (maxLife <= 10) {
								victim.getPlayer().sendMessage("§f남은 생명: §c" + Strings.repeat("§c♥", life) + Strings.repeat("§c♡", maxLife - life));
							} else {
								victim.getPlayer().sendMessage("§f남은 생명: §c" + life);
							}
						}
						if (score.getScore() <= 0) {
							noLife.add(team);
							super.Operation(victim);

							checkWinner();
						}
					}
				} catch (IllegalStateException ignored) {}
			}

			@Override
			protected String getRevealMessage(Participant victim) {
				Mix mix = (Mix) victim.getAbility();
				if (mix.hasAbility()) {
					if (mix.hasSynergy()) {
						Synergy synergy = mix.getSynergy();
						Pair<AbilityRegistration, AbilityRegistration> base = SynergyFactory.getSynergyBase(synergy.getRegistration());
						String name = synergy.getName() + " (" + base.getLeft().getManifest().name() + " + " + base.getRight().getManifest().name() + ")";
						return "§f[§c능력§f] §c" + victim.getPlayer().getName() + "§f님의 능력은 §e" + name + "§f" + KoreanUtil.getJosa(name, KoreanUtil.Josa.이었였) + "습니다.";
					} else {
						String name = mix.getFirst().getName() + " + " + mix.getSecond().getName();
						return "§f[§c능력§f] §c" + victim.getPlayer().getName() + "§f님의 능력은 §e" + name + "§f" + KoreanUtil.getJosa(name, KoreanUtil.Josa.이었였) + "습니다.";
					}
				} else {
					return "§f[§c능력§f] §c" + victim.getPlayer().getName() + "§f님은 능력이 없습니다.";
				}
			}
		};
	}

	@Override
	public AbilitySelect newAbilitySelect() {
		return null;
	}

	@Override
	protected void onEnd() {
		HandlerList.unregisterAll(this);
		lifeObjective.unregister();
		super.onEnd();
	}
}
