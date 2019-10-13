package daybreak.abilitywar.game.games.changeability;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.config.AbilityWarSettings.Settings;
import daybreak.abilitywar.config.AbilityWarSettings.Settings.ChangeAbilityWarSettings;
import daybreak.abilitywar.config.AbilityWarSettings.Settings.DeathSettings;
import daybreak.abilitywar.game.events.GameCreditEvent;
import daybreak.abilitywar.game.events.ParticipantDeathEvent;
import daybreak.abilitywar.game.games.mode.GameManifest;
import daybreak.abilitywar.game.games.mode.WinnableGame;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.game.manager.DeathManager;
import daybreak.abilitywar.game.manager.InfiniteDurability;
import daybreak.abilitywar.game.manager.SpectatorManager;
import daybreak.abilitywar.utils.FireworkUtil;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.language.KoreanUtil;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.math.NumberUtil;
import daybreak.abilitywar.utils.thread.AbilityWarThread;
import daybreak.abilitywar.utils.thread.Timer;
import daybreak.abilitywar.utils.thread.TimerBase;
import daybreak.abilitywar.utils.versioncompat.ServerVersion;

/**
 * 체인지 능력 전쟁
 * @author DayBreak 새벽
 */
@GameManifest(Name = "체인지 능력 전쟁 (Beta)", Description = { "§f일정 시간마다 바뀌는 능력을 가지고 플레이하는 심장 쫄깃한 모드입니다.", "§f모든 플레이어에게는 일정량의 생명이 주어지며, 죽을 때마다 생명이 소모됩니다.", "§f생명이 모두 소모되면 설정에 따라 게임에서 탈락합니다.", "§f모두를 탈락시키고 최후의 1인으로 남는 플레이어가 승리합니다.", "", "§a● §f스크립트가 적용되지 않습니다.",
														"§a● §f일부 콘피그가 임의로 변경될 수 있습니다.", "", "§6● §f체인지 능력전쟁 전용 콘피그가 있습니다. Config.yml을 확인해보세요."})
public class ChangeAbilityWar extends WinnableGame {

	public ChangeAbilityWar() {
		setRestricted(Invincible);
		this.maxLife = ChangeAbilityWarSettings.getLife();
	}
	
	@SuppressWarnings("deprecation")
	private final Objective lifeObjective = ServerVersion.getVersion() >= 13 ?
			getScoreboardManager().getScoreboard().registerNewObjective("생명", "dummy", ChatColor.translateAlternateColorCodes('&', "&c생명"))
			: getScoreboardManager().getScoreboard().registerNewObjective("생명", "dummy");
	
	private final AbilityChanger changer = new AbilityChanger(this);
	
	private final boolean Invincible = Settings.getInvincibilityEnable();
	
	private final InfiniteDurability infiniteDurability = new InfiniteDurability();
	
	private final TimerBase NoHunger = new TimerBase() {
		
		@Override
		public void onStart() {
			Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&a배고픔 무제한이 적용됩니다."));
		}
		
		@Override
		public void TimerProcess(Integer Seconds) {
			for(Participant p : getParticipants()) {
				p.getPlayer().setFoodLevel(19);
			}
		}
		
		@Override
		public void onEnd() {}
	};
	
	@Override
	protected void progressGame(Integer Seconds) {
		switch(Seconds) {
			case 1:
				broadcastPlayerList();
				if(getParticipants().size() < 2) {
					AbilityWarThread.StopGame();
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c최소 참가자 수를 충족하지 못하여 게임을 중지합니다. &8(&72명&8)"));
				}
				break;
			case 5:
				broadcastPluginDescription();
				break;
			case 10:
				broadcastAbilityReady();
				break;
			case 13:
				scoreboardSetup();
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&7스코어보드 &f설정중..."));
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&d잠시 후 &f게임이 시작됩니다."));
				break;
			case 16:
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f게임이 &55&f초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 17:
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f게임이 &54&f초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 18:
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f게임이 &53&f초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 19:
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f게임이 &52&f초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 20:
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f게임이 &51&f초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 21:
				GameStart();
				break;
		}
	}
	
	private final int maxLife;
	
	private void scoreboardSetup() {
		lifeObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
		if(ServerVersion.getVersion() < 13) lifeObjective.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c생명"));
		for(Participant p : getParticipants()) {
			Score score = lifeObjective.getScore(p.getPlayer().getName());
			score.setScore(maxLife);
		}
	}
	
	private final boolean Eliminate = ChangeAbilityWarSettings.getEliminate();
	
	private final List<Participant> NoLife = new ArrayList<Participant>();

	@Override
	protected DeathManager setupDeathManager() {
		return new DeathManager(this) {
			@EventHandler
			protected void onDeath(PlayerDeathEvent e) {
				Player victimPlayer = e.getEntity();
				Player killerPlayer = victimPlayer.getKiller();
				if(victimPlayer.getLastDamageCause() != null) {
					DamageCause Cause = victimPlayer.getLastDamageCause().getCause();

					if(killerPlayer != null) {
						e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&a" + killerPlayer.getName() + "&f님이 &c" + victimPlayer.getName() + "&f님을 죽였습니다."));
					} else {
						if(Cause.equals(DamageCause.CONTACT)) {
							e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 찔려 죽었습니다."));
						} else if(Cause.equals(DamageCause.FALL)) {
							e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 떨어져 죽었습니다."));
						} else if(Cause.equals(DamageCause.FALLING_BLOCK)) {
							e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 떨어지는 블록에 맞아 죽었습니다."));
						} else if(Cause.equals(DamageCause.SUFFOCATION)) {
							e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 끼여 죽었습니다."));
						} else if(Cause.equals(DamageCause.DROWNING)) {
							e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 익사했습니다."));
						} else if(Cause.equals(DamageCause.ENTITY_EXPLOSION)) {
							e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 폭발했습니다."));
						} else if(Cause.equals(DamageCause.LAVA)) {
							e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 용암에 빠져 죽었습니다."));
						} else if(Cause.equals(DamageCause.FIRE) || Cause.equals(DamageCause.FIRE_TICK)) {
							e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 노릇노릇하게 구워졌습니다."));
						} else {
							e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 죽었습니다."));
						}
					}
				} else {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 죽었습니다."));
				}

				if(DeathSettings.getItemDrop()) {
					e.setKeepInventory(false);
					victimPlayer.getInventory().clear();
				} else {
					e.setKeepInventory(true);
				}

				if(isParticipating(victimPlayer)) {
					Participant victim = getParticipant(victimPlayer);
					
					Bukkit.getPluginManager().callEvent(new ParticipantDeathEvent(victim));
					
					if(DeathSettings.getAbilityReveal()) {
						if(victim.hasAbility()) {
							String name = victim.getAbility().getName();
							if(name != null) {
								Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f[&c능력&f] &c" + victimPlayer.getName() + "&f님의 능력은 " + KoreanUtil.getCompleteWord("&e" + name, "&f이었", "&f였") + "습니다."));
							}
						} else {
							Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f[&c능력&f] &c" + victimPlayer.getName() + "&f님은 능력이 없습니다."));
						}
					}
				}

				Participant VictimPart = getParticipant(victimPlayer);
				if(VictimPart != null) {
					Score score = lifeObjective.getScore(victimPlayer.getName());
					if(score.isScoreSet()) {
						if(score.getScore() >= 1) score.setScore(score.getScore() - 1);
						if(score.getScore() <= 0) {
							NoLife.add(VictimPart);
							if(Eliminate) getDeathManager().Eliminate(victimPlayer);

							Participant hasLife = null;
							int count = 0;
							for(Participant p : getParticipants()) {
								if(!NoLife.contains(p)) {
									hasLife = p;
									count++;
								}
							}
							
							if(count == 1 && hasLife != null) {
								Victory(hasLife);
							}
						}
					}
				}
			}
		};
	}
	
	public void broadcastPlayerList() {
		int Count = 0;
		
        ArrayList<String> msg = new ArrayList<String>();
		
		msg.add(ChatColor.translateAlternateColorCodes('&', "&d==== &f게임 참여자 목록 &d===="));
		for(Participant p : getParticipants()) {
			Count++;
			msg.add(ChatColor.translateAlternateColorCodes('&', "&5" + Count + ". &f" + p.getPlayer().getName()));
		}
		msg.add(ChatColor.translateAlternateColorCodes('&', "&f총 인원수 &5: &d" + Count + "명"));
		msg.add(ChatColor.translateAlternateColorCodes('&', "&d=========================="));
		
		for (String m : msg) {
			Bukkit.broadcastMessage(m);
		}
	}
	
	public void broadcastPluginDescription() {
		ArrayList<String> msg = new ArrayList<>();
		msg.add(ChatColor.translateAlternateColorCodes('&', "&5&l체인지! &d&l능력 &f&l전쟁"));
		msg.add(ChatColor.translateAlternateColorCodes('&', "&e플러그인 버전 &7: &f" + AbilityWar.getPlugin().getDescription().getVersion()));
		msg.add(ChatColor.translateAlternateColorCodes('&', "&b모드 개발자 &7: &fDayBreak 새벽"));
		msg.add(ChatColor.translateAlternateColorCodes('&', "&9디스코드 &7: &fDayBreak&7#5908"));
		
		GameCreditEvent event = new GameCreditEvent();
		Bukkit.getPluginManager().callEvent(event);
		
		for(String str : event.getCreditList()) {
			msg.add(str);
		}

		for (String m : msg) {
			Bukkit.broadcastMessage(m);
		}
	}
	
	public void broadcastAbilityReady() {

		for (String m : new String[] {
				ChatColor.translateAlternateColorCodes('&', "&f플러그인에 총 &d" + AbilityList.nameValues().size() + "개&f의 능력이 등록되어 있습니다."),
				ChatColor.translateAlternateColorCodes('&', "&7게임 시작시 &f첫번째 능력&7이 할당되며, 이후 &f" + NumberUtil.parseTimeString(changer.getPeriod()) + "&7마다 능력이 변경됩니다.")}) {
			Bukkit.broadcastMessage(m);
		}
	}
	
	public void GameStart() {
		for (String m : new String[] {
				ChatColor.translateAlternateColorCodes('&', "&d■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■"),
				ChatColor.translateAlternateColorCodes('&', "&f                &5&l체인지! &d&l능력 &f&l전쟁"),
				ChatColor.translateAlternateColorCodes('&', "&f                    게임 시작                "),
				ChatColor.translateAlternateColorCodes('&', "&d■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■")}) {
			Bukkit.broadcastMessage(m);
		}
		SoundLib.ENTITY_WITHER_SPAWN.broadcastSound();
		
		this.GiveDefaultKit();
		
		for(Participant p : getParticipants()) {
			if(Settings.getSpawnEnable()) {
				p.getPlayer().teleport(Settings.getSpawnLocation());
			}
		}
		
		if(Settings.getNoHunger()) {
			NoHunger.setPeriod(1);
			NoHunger.StartTimer();
		} else {
			Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4배고픔 무제한&c이 적용되지 않습니다."));
		}
		
		if(Invincible) {
			getInvincibility().Start(false);
		} else {
			Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4초반 무적&c이 적용되지 않습니다."));
			for(Participant participant : this.getParticipants()) {
				if(participant.hasAbility()) {
					participant.getAbility().setRestricted(false);
				}
			}
		}
		
		if(Settings.getInfiniteDurability()) {
			registerListener(infiniteDurability);
		} else {
			Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4내구도 무제한&c이 적용되지 않습니다."));
		}
		
		for(World w : Bukkit.getWorlds()) {
			if(Settings.getClearWeather()) {
				w.setStorm(false);
			}
		}
		
		changer.StartTimer();
		
		startGame();
	}
	
	/**
	 * 기본 킷 유저 지급
	 */
	@Override
	public void GiveDefaultKit(Player p) {
		List<ItemStack> DefaultKit = Settings.getDefaultKit();

		if(Settings.getInventoryClear()) {
			p.getInventory().clear();
		}
		
		for(ItemStack is : DefaultKit) {
			p.getInventory().addItem(is);
		}
		
		p.setLevel(0);
		if(Settings.getStartLevel() > 0) {
			p.giveExpLevels(Settings.getStartLevel());
			SoundLib.ENTITY_PLAYER_LEVELUP.playSound(p);
		}
	}

	@Override
	protected void onVictory(Participant... participants) {
		Messager.clearChat();
		StringBuilder builder = new StringBuilder();
		builder.append(ChatColor.translateAlternateColorCodes('&', "&5&l우승자&f: "));
		
		StringJoiner joiner = new StringJoiner("§f, §d", "§d", "§f.");
		for(Participant participant : participants) {
			Player p = participant.getPlayer();
			SoundLib.UI_TOAST_CHALLENGE_COMPLETE.playSound(p);
			joiner.add(p.getName());
			new Timer(5) {
				
				@Override
				protected void onStart() {}
				
				@Override
				protected void onEnd() {}
				
				@Override
				protected void TimerProcess(Integer Seconds) {
					FireworkUtil.spawnWinnerFirework(p.getEyeLocation().add(0, 1, 0));
				}
			}.setPeriod(8).StartTimer();
		}
		
		builder.append(joiner.toString());
		Bukkit.broadcastMessage(builder.toString());
	}

	@Override
	protected List<Player> initPlayers() {
		List<Player> Players = new ArrayList<Player>();
		
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(!SpectatorManager.isSpectator(p.getName())) {
				Players.add(p);
			}
		}
		
		return Players;
	}
	
	@Override
	protected AbilitySelect setupAbilitySelect() {
		return null;
	}
	
	@Override
	protected void onGameEnd() {
		lifeObjective.unregister();
	}

}
