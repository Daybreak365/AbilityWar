package daybreak.abilitywar.game.list.changeability;

import com.google.common.base.Strings;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.game.GameSettings.Setting;
import daybreak.abilitywar.game.AbstractGame.Observer;
import daybreak.abilitywar.game.Game;
import daybreak.abilitywar.game.GameAliases;
import daybreak.abilitywar.game.GameManifest;
import daybreak.abilitywar.game.event.GameCreditEvent;
import daybreak.abilitywar.game.interfaces.Winnable;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.game.manager.object.AbilitySelect;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.manager.object.DefaultKitHandler;
import daybreak.abilitywar.game.module.Invincibility;
import daybreak.abilitywar.game.module.InfiniteDurability;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.TimeUtil;
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

/**
 * 체인지 능력 전쟁
 *
 * @author Daybreak 새벽
 */
@GameManifest(name = "체인지 능력 전쟁", description = {
		"§f일정 시간마다 바뀌는 능력을 가지고 플레이하는 심장 쫄깃한 모드입니다.",
		"§f모든 플레이어에게는 일정량의 생명이 주어지며, 죽을 때마다 생명이 소모됩니다.",
		"§f생명이 모두 소모되면 설정에 따라 게임에서 탈락합니다.",
		"§f모두를 탈락시키고 최후의 1인으로 남는 플레이어가 승리합니다.", "",
		"§a● §f스크립트가 적용되지 않습니다.",
		"§a● §f일부 콘피그가 임의로 변경될 수 있습니다.", "",
		"§6● §f체인지 능력 전쟁 전용 콘피그가 있습니다."
})
@GameAliases({"체능전", "체인지"})
public class ChangeAbilityWar extends Game implements Winnable, DefaultKitHandler, Observer {

	public static final Setting<Integer> CHANGE_PERIOD = gameSettings.new Setting<Integer>(ChangeAbilityWar.class, "period", 20, "# 능력 변경 주기 (단위: 초)") {
		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}
	};

	public static final Setting<Integer> MAX_LIFE = gameSettings.new Setting<Integer>(ChangeAbilityWar.class, "life-amount", 3, "# 죽었을 때 다시 태어날 수 있는 횟수") {
		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}
	};

	public ChangeAbilityWar() {
		super(PlayerCollector.EVERY_PLAYER_EXCLUDING_SPECTATORS());
		this.maxLife = MAX_LIFE.getValue();
		attachObserver(this);
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
	}

	private final Objective lifeObjective = getScoreboardManager().registerNewObjective("생명", "dummy", "§c생명");

	private final AbilityChanger changer = addModule(new AbilityChanger(this));
	private final boolean invincible = Settings.InvincibilitySettings.isEnabled();

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
				msg.add("§5§l체인지! §d§l능력 §f§l전쟁");
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
				Bukkit.broadcastMessage("§7스코어보드 §f설정 중...");
				lifeObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
				for (Participant participant : getParticipants()) {
					final Score score = lifeObjective.getScore(participant.getPlayer().getName());
					score.setScore(maxLife);
				}
				Bukkit.broadcastMessage("§d잠시 후 §f게임이 시작됩니다.");
				break;
			case 9:
				Bukkit.broadcastMessage("§f게임이 §55§f초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 10:
				Bukkit.broadcastMessage("§f게임이 §54§f초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 11:
				Bukkit.broadcastMessage("§f게임이 §53§f초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 12:
				Bukkit.broadcastMessage("§f게임이 §52§f초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 13:
				Bukkit.broadcastMessage("§f게임이 §51§f초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 14:
				for (String m : new String[]{
						"§d■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■",
						"§f                §5§l체인지! §d§l능력 §f§l전쟁",
						"§f                    게임 시작                ",
						"§d■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■"}) {
					Bukkit.broadcastMessage(m);
				}
				SoundLib.ENTITY_WITHER_SPAWN.broadcastSound();

				giveDefaultKit(getParticipants());

				for (Participant p : getParticipants()) {
					if (Settings.getSpawnEnable()) {
						p.getPlayer().teleport(Settings.getSpawnLocation().toBukkitLocation());
					}
				}

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
					Bukkit.broadcastMessage("§4초반 무적§c이 적용되지 않습니다.");
					changer.start();
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
				break;
		}
	}

	private final int maxLife;
	private final Set<Participant> noLife = new HashSet<>();

	@EventHandler
	private void onPlayerQuit(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		if (isParticipating(player)) {
			Participant quitParticipant = getParticipant(player);
			Score score = lifeObjective.getScore(player.getName());
			if (score.isScoreSet()) {
				score.setScore(0);
				noLife.add(quitParticipant);
				getDeathManager().Operation(quitParticipant);

				Participant winner = null;
				int count = 0;
				for (Participant participant : getParticipants()) {
					if (!noLife.contains(participant)) {
						count++;
						winner = participant;
					}
				}

				if (count == 1) {
					Win(winner);
				}
			}
		}
	}

	@Override
	protected @NotNull DeathManager newDeathManager() {
		return new DeathManager(this) {
			@Override
			public void Operation(Participant victim) {
				Player victimPlayer = victim.getPlayer();
				Score score = lifeObjective.getScore(victimPlayer.getName());
				if (score.isScoreSet()) {
					int life = score.getScore();
					if (life >= 1) {
						score.setScore(--life);
						if (maxLife <= 10) {
							victimPlayer.sendMessage("§f남은 생명: §c" + Strings.repeat("§c♥", life) + Strings.repeat("§c♡", maxLife - life));
						} else {
							victimPlayer.sendMessage("§f남은 생명: §c" + life);
						}
					}
					if (score.getScore() <= 0) {
						noLife.add(victim);
						super.Operation(victim);

						Participant winner = null;
						int count = 0;
						for (Participant participant : getParticipants()) {
							if (!noLife.contains(participant)) {
								count++;
								winner = participant;
							}
						}

						if (count == 1) {
							Win(winner);
						}
					}
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
		lifeObjective.unregister();
		super.onEnd();
	}

	@Override
	public void update(GameUpdate update) {
		if (update == GameUpdate.END) {
			HandlerList.unregisterAll(this);
		}
	}

}
