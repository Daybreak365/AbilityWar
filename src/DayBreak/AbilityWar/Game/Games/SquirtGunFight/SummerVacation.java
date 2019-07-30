package DayBreak.AbilityWar.Game.Games.SquirtGunFight;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import DayBreak.AbilityWar.AbilityWar;
import DayBreak.AbilityWar.Config.AbilityWarSettings;
import DayBreak.AbilityWar.Game.Games.GameCreditEvent;
import DayBreak.AbilityWar.Game.Games.Mode.GameManifest;
import DayBreak.AbilityWar.Game.Games.Mode.WinnableGame;
import DayBreak.AbilityWar.Game.Manager.InfiniteDurability;
import DayBreak.AbilityWar.Utils.FireworkUtil;
import DayBreak.AbilityWar.Utils.Messager;
import DayBreak.AbilityWar.Utils.Library.EffectLib;
import DayBreak.AbilityWar.Utils.Library.SoundLib;
import DayBreak.AbilityWar.Utils.Thread.AbilityWarThread;
import DayBreak.AbilityWar.Utils.Thread.Timer;
import DayBreak.AbilityWar.Utils.Thread.TimerBase;

/**
 * 체인지 능력 전쟁
 * @author DayBreak 새벽
 */
@GameManifest(Name = "신나는 여름 휴가", Description = { "§f신나는 물총싸움 뿌슝빠슝! 지금 바로 즐겨보세요!", "", "§a● §f스크립트가 적용되지 않습니다.",
														"§a● §f일부 콘피그가 임의로 변경될 수 있습니다.", "", "§6● §f신나는 여름 휴가 전용 콘피그가 있습니다. Config.yml을 확인해보세요."})
public class SummerVacation extends WinnableGame {
	
	public SummerVacation() {
		setRestricted(Invincible);
		this.MaxKill = AbilityWarSettings.SummerVacation_getKill();
	}
	
	private final boolean Invincible = AbilityWarSettings.getInvincibilityEnable();

	private final Objective killObjective = getScoreboardManager().getScoreboard().registerNewObjective("킬 횟수", "dummy");
	
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
				if(getParticipants().size() < 1) {
					AbilityWarThread.StopGame();
					Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c최소 참가자 수를 충족하지 못하여 게임을 중지합니다. &8(&71명&8)"));
				}
				break;
			case 5:
				broadcastPluginDescription();
				break;
			case 10:
				broadcastAbilityReady();
				try {
					for(Participant p : getParticipants()) {
						p.setAbility(SquirtGun.class);
					}
				} catch (Exception e) {}
				break;
			case 13:
				scoreboardSetup();
				Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&7스코어보드 &f설정중..."));
				Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e잠시 후 &f게임이 시작됩니다."));
				break;
			case 16:
				Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f게임이 &e5&f초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 17:
				Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f게임이 &e4&f초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 18:
				Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f게임이 &e3&f초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 19:
				Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f게임이 &e2&f초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 20:
				Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f게임이 &e1&f초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 21:
				GameStart();
				break;
		}
	}

	private void scoreboardSetup() {
		killObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
		killObjective.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c킬 횟수"));
		for(Participant p : getParticipants()) {
			Score score = killObjective.getScore(p.getPlayer().getName());
			score.setScore(0);
		}
	}
	
	List<Participant> Killers = new ArrayList<Participant>();
	
	TimerBase Glow = new TimerBase() {
		
		@Override
		protected void onStart() {}
		
		@Override
		protected void onEnd() {}
		
		@Override
		protected void TimerProcess(Integer Seconds) {
			for(Participant p : Killers) {
				EffectLib.GLOWING.addPotionEffect(p.getPlayer(), 20, 0, true);
			}
		}
	}.setPeriod(10);
	
	private final int MaxKill;
	
	@Override
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player Victim = e.getEntity();
		if(Victim.getKiller() != null) {
			Participant VictimPart = getParticipant(Victim);
			if(VictimPart != null && Killers.contains(VictimPart)) Killers.remove(VictimPart);
			Participant Killer = getParticipant(Victim.getKiller());
			if(Killer != null && !Killer.getPlayer().equals(Victim)) {
				if(!Killers.contains(Killer)) Killers.add(Killer);
				Score score = killObjective.getScore(Killer.getPlayer().getName());
				if(score.isScoreSet()) {
					score.setScore(score.getScore() + 1);
					if(score.getScore() >= MaxKill) {
						this.Victory(Killer);
					}
				}
			}
		}
	}
	
	public void broadcastPlayerList() {
		int Count = 0;
		
        ArrayList<String> msg = new ArrayList<String>();
		
		msg.add(ChatColor.translateAlternateColorCodes('&', "&6==== &f게임 참여자 목록 &6===="));
		for(Participant p : getParticipants()) {
			Count++;
			msg.add(ChatColor.translateAlternateColorCodes('&', "&c" + Count + ". &f" + p.getPlayer().getName()));
		}
		msg.add(ChatColor.translateAlternateColorCodes('&', "&f총 인원수 &c: &e" + Count + "명"));
		msg.add(ChatColor.translateAlternateColorCodes('&', "&6==========================="));
		
		Messager.broadcastStringList(msg);
	}
	
	public void broadcastPluginDescription() {
		ArrayList<String> msg = Messager.getStringList(
				ChatColor.translateAlternateColorCodes('&', "&eSummer Vacation &f- &c여름 휴가"),
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
				ChatColor.translateAlternateColorCodes('&', "&e신나는 여름 휴가 &f모드에서는 모든 플레이어의 능력이 물총으로 고정됩니다."));
		
		Messager.broadcastStringList(msg);
	}
	
	public void GameStart() {
		Messager.broadcastStringList(Messager.getStringList(
				ChatColor.translateAlternateColorCodes('&', "&e■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■"),
				ChatColor.translateAlternateColorCodes('&', "&f           &eSummer Vacation &f- &c여름 휴가 "),
				ChatColor.translateAlternateColorCodes('&', "&f                    게임 시작                "),
				ChatColor.translateAlternateColorCodes('&', "&e■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■")));
		SoundLib.ENTITY_PLAYER_SPLASH.broadcastSound();
		
		this.GiveDefaultKit();
		
		for(Participant p : getParticipants()) {
			if(AbilityWarSettings.getSpawnEnable()) {
				p.getPlayer().teleport(AbilityWarSettings.getSpawnLocation());
			}
		}

		NoHunger.setPeriod(1);
		NoHunger.StartTimer();
		
		Glow.StartTimer();

		Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4초반 무적&c이 적용되지 않습니다."));
		for(Participant participant : this.getParticipants()) {
			if(participant.hasAbility()) {
				participant.getAbility().setRestricted(false);
			}
		}

		Bukkit.getPluginManager().registerEvents(infiniteDurability, AbilityWar.getPlugin());
		
		for(World w : Bukkit.getWorlds()) {
			if(AbilityWarSettings.getClearWeather()) {
				w.setStorm(false);
			}
		}
		
		startGame();
	}
	
	/**
	 * 기본 킷 유저 지급
	 */
	@Override
	public void GiveDefaultKit(Player p) {
		ItemStack bow = new ItemStack(Material.BOW);
		ItemMeta bowMeta = bow.getItemMeta();
		bowMeta.setUnbreakable(true);
		bowMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b물총"));
		bow.setItemMeta(bowMeta);
		List<ItemStack> DefaultKit = Arrays.asList(bow, new ItemStack(Material.ARROW, 64), new ItemStack(Material.IRON_INGOT, 64));

		if(AbilityWarSettings.getInventoryClear()) {
			p.getInventory().clear();
		}
		
		for(ItemStack is : DefaultKit) {
			p.getInventory().addItem(is);
		}

		ItemStack boots = new ItemStack(Material.IRON_BOOTS);
		ItemMeta bootsMeta = boots.getItemMeta();
		bootsMeta.setUnbreakable(true);
		bootsMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e오리발"));
		boots.setItemMeta(bootsMeta);
		boots.addEnchantment(Enchantment.BINDING_CURSE, 1);
		boots.addEnchantment(Enchantment.DEPTH_STRIDER, 3);

		ItemStack helmet = new ItemStack(Material.IRON_HELMET);
		ItemMeta helmetMeta = helmet.getItemMeta();
		helmetMeta.setUnbreakable(true);
		helmetMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b안경"));
		helmet.setItemMeta(helmetMeta);
		helmet.addEnchantment(Enchantment.BINDING_CURSE, 1);

		p.getInventory().setHelmet(helmet);
		p.getInventory().setBoots(boots);
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
	protected void onGameEnd() {
		killObjective.unregister();
		HandlerList.unregisterAll(infiniteDurability);
	}

}
