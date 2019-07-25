package DayBreak.AbilityWar.Game.Games.ChangeAbility;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import DayBreak.AbilityWar.AbilityWar;
import DayBreak.AbilityWar.Ability.AbilityList;
import DayBreak.AbilityWar.Config.AbilityWarSettings;
import DayBreak.AbilityWar.Game.Games.GameCreditEvent;
import DayBreak.AbilityWar.Game.Games.Mode.GameManifest;
import DayBreak.AbilityWar.Game.Games.Mode.WinnableGame;
import DayBreak.AbilityWar.Game.Manager.InfiniteDurability;
import DayBreak.AbilityWar.Utils.FireworkUtil;
import DayBreak.AbilityWar.Utils.Messager;
import DayBreak.AbilityWar.Utils.Library.SoundLib;
import DayBreak.AbilityWar.Utils.Math.NumberUtil;
import DayBreak.AbilityWar.Utils.Thread.AbilityWarThread;
import DayBreak.AbilityWar.Utils.Thread.OverallTimer;
import DayBreak.AbilityWar.Utils.Thread.TimerBase;

/**
 * 체인지 능력 전쟁
 * @author DayBreak 새벽
 */
@GameManifest(Name = "체인지 능력 전쟁 (Beta)", Description = { "§f일정 시간마다 바뀌는 능력을 가지고 플레이하는 심장 쫄깃한 모드입니다.", "§f모든 플레이어에게는 일정량의 생명이 주어지며, 죽을 때마다 생명이 소모됩니다.", "§f생명이 모두 소모되면 설정에 따라 게임에서 탈락합니다.", "§f모두를 탈락시키고 최후의 1인으로 남는 플레이어가 승리합니다.", "", "§a● §f스크립트가 적용되지 않습니다.",
														"§a● §f일부 콘피그가 임의로 변경될 수 있습니다.", "", "§6● §f체인지 능력전쟁 전용 콘피그가 있습니다. Config.yml을 확인해보세요."})
public class ChangeAbilityWar extends WinnableGame {
	
	public ChangeAbilityWar() {
		setRestricted(Invincible);
		registerEvent(EntityDamageEvent.class);
	}
	
	private final Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
	private final Objective lifeObjective = scoreboard.registerNewObjective("생명", "dummy");
	
	private final AbilityChanger changer = new AbilityChanger(this);
	
	private final boolean Invincible = AbilityWarSettings.getInvincibilityEnable();
	
	private final InfiniteDurability infiniteDurability = new InfiniteDurability();
	
	private final TimerBase NoHunger = new TimerBase() {
		
		@Override
		public void onStart() {
			Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&a배고픔 무제한이 적용됩니다."));
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
	protected boolean gameCondition() {
		return true;
	}
	
	@Override
	protected void progressGame(Integer Seconds) {
		switch(Seconds) {
			case 1:
				broadcastPlayerList();
				if(getParticipants().size() < 2) {
					AbilityWarThread.StopGame();
					Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c최소 참가자 수를 충족하지 못하여 게임을 중지합니다. &8(&72명&8)"));
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
				Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f잠시 후 게임이 시작됩니다."));
				break;
			case 16:
				Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f게임이 &55&f초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 17:
				Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f게임이 &54&f초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 18:
				Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f게임이 &53&f초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 19:
				Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f게임이 &52&f초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 20:
				Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f게임이 &51&f초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 21:
				GameStart();
				break;
		}
	}
	
	public void scoreboardSetup() {
		for(Player p : Bukkit.getOnlinePlayers()) {
			p.setScoreboard(scoreboard);
		}
		lifeObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
		lifeObjective.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c생명"));
		final int maxLife = AbilityWarSettings.ChangeAbilityWar_getLife();
		for(Participant p : getParticipants()) {
			Score score = lifeObjective.getScore(p.getPlayer().getName());
			score.setScore(maxLife);
		}
	}
	
	private final boolean Eliminate = AbilityWarSettings.ChangeAbilityWar_getEliminate();
	
	private final List<Participant> NoLife = new ArrayList<Participant>();
	
	@Override
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player Victim = e.getEntity();
		Participant VictimPart = getParticipant(Victim);
		if(VictimPart != null) {
			Score score = lifeObjective.getScore(Victim.getName());
			if(score.isScoreSet()) {
				if(score.getScore() >= 1) score.setScore(score.getScore() - 1);
				if(score.getScore() <= 0) {
					NoLife.add(VictimPart);
					if(Eliminate) getDeathManager().Eliminate(Victim);

					Participant hasLife = null;
					int count = 0;
					for(Participant p : getParticipants()) {
						if(!NoLife.contains(p)) {
							hasLife = p;
							count++;
						}
					}
					
					if(count == 1 && hasLife != null) {
						this.Victory(hasLife);
					}
				}
			}
			
			if(Victim.getKiller() != null) {
				SoundLib.ENTITY_ARROW_HIT_PLAYER.playSound(Victim.getKiller());
			}
		}
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
		msg.add(ChatColor.translateAlternateColorCodes('&', "&d==========================="));
		
		Messager.broadcastStringList(msg);
	}
	
	public void broadcastPluginDescription() {
		ArrayList<String> msg = Messager.getStringList(
				ChatColor.translateAlternateColorCodes('&', "&5&l체인지! &d&l능력 &f&l전쟁"),
				ChatColor.translateAlternateColorCodes('&', "&e플러그인 버전 &7: &f" + AbilityWar.getPlugin().getDescription().getVersion()),
				ChatColor.translateAlternateColorCodes('&', "&b모드 개발자 &7: &fDayBreak 새벽"),
				ChatColor.translateAlternateColorCodes('&', "&9디스코드 &7: &fDayBreak&7#5908"));
		
		GameCreditEvent event = new GameCreditEvent();
		Bukkit.getPluginManager().callEvent(event);
		
		for(String str : event.getCreditList()) {
			msg.add(str);
		}
		
		Messager.broadcastStringList(msg);
	}
	
	public void broadcastAbilityReady() {
		ArrayList<String> msg = Messager.getStringList(
				ChatColor.translateAlternateColorCodes('&', "&f플러그인에 총 &d" + AbilityList.nameValues().size() + "개&f의 능력이 등록되어 있습니다."),
				ChatColor.translateAlternateColorCodes('&', "&7게임 시작시 &f첫번째 능력&7이 할당되며, 이후 &f" + NumberUtil.parseTimeString(changer.getPeriod()) + "&7마다 능력이 변경됩니다."));
		
		Messager.broadcastStringList(msg);
	}
	
	public void GameStart() {
		Messager.broadcastStringList(Messager.getStringList(
				ChatColor.translateAlternateColorCodes('&', "&d■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■"),
				ChatColor.translateAlternateColorCodes('&', "&f                              &5&l체인지! &d&l능력 &f&l전쟁             "),
				ChatColor.translateAlternateColorCodes('&', "&f                                  게임 시작                             "),
				ChatColor.translateAlternateColorCodes('&', "&d■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■")));
		SoundLib.ENTITY_WITHER_SPAWN.broadcastSound();
		
		this.GiveDefaultKit();
		
		for(Participant p : getParticipants()) {
			if(AbilityWarSettings.getSpawnEnable()) {
				p.getPlayer().teleport(AbilityWarSettings.getSpawnLocation());
			}
		}
		
		if(AbilityWarSettings.getNoHunger()) {
			NoHunger.setPeriod(1);
			NoHunger.StartTimer();
		} else {
			Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4배고픔 무제한&c이 적용되지 않습니다."));
		}
		
		if(Invincible) {
			getInvincibility().Start(false);
		} else {
			Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4초반 무적&c이 적용되지 않습니다."));
			for(Participant participant : this.getParticipants()) {
				if(participant.hasAbility()) {
					participant.getAbility().setRestricted(false);
				}
			}
		}
		
		if(AbilityWarSettings.getInfiniteDurability()) {
			Bukkit.getPluginManager().registerEvents(infiniteDurability, AbilityWar.getPlugin());
		} else {
			Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4내구도 무제한&c이 적용되지 않습니다."));
		}
		
		for(World w : Bukkit.getWorlds()) {
			if(AbilityWarSettings.getClearWeather()) {
				w.setStorm(false);
			}
		}
		
		changer.StartTimer();
		
		setGameStarted(true);
	}
	
	/**
	 * 기본 킷 유저 지급
	 */
	@Override
	public void GiveDefaultKit(Player p) {
		List<ItemStack> DefaultKit = AbilityWarSettings.getDefaultKit();

		if(AbilityWarSettings.getInventoryClear()) {
			p.getInventory().clear();
		}
		
		for(ItemStack is : DefaultKit) {
			p.getInventory().addItem(is);
		}
		
		p.setLevel(0);
		if(AbilityWarSettings.getStartLevel() > 0) {
			p.giveExpLevels(AbilityWarSettings.getStartLevel());
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
			new OverallTimer(5) {
				
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
		Messager.broadcastMessage(builder.toString());
	}

	@Override
	protected List<Player> setupPlayers() {
		List<Player> Players = new ArrayList<Player>();
		
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(!isSpectator(p.getName())) {
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
	public void execute(Listener listener, Event event) throws EventException {
		if(event instanceof EntityDamageEvent) {
			EntityDamageEvent e = (EntityDamageEvent) event;
			
			if(e.getEntity() instanceof Player) {
				if(this.getInvincibility().isInvincible()) {
					e.setCancelled(true);
				}
			}
		}
	}

	@Override
	protected void onEnd() {
		HandlerList.unregisterAll(infiniteDurability);
	}

}
