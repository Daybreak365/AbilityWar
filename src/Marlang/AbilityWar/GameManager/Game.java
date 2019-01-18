package Marlang.AbilityWar.GameManager;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import Marlang.AbilityWar.AbilityWar;
import Marlang.AbilityWar.API.GameAPI;
import Marlang.AbilityWar.API.Events.AbilityWarProgressEvent;
import Marlang.AbilityWar.API.Events.AbilityWarProgressEvent.Progress;
import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.AbilityList;
import Marlang.AbilityWar.Config.AbilityWarSettings;
import Marlang.AbilityWar.GameManager.Manager.DeathManager;
import Marlang.AbilityWar.GameManager.Manager.Invincibility;
import Marlang.AbilityWar.Utils.AbilityWarThread;
import Marlang.AbilityWar.Utils.EffectUtil;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.TimerBase;

/**
 * 게임 관리 클래스
 * @author _Marlang 말랑
 */
public class Game extends Thread {
	
	int Seconds = 0;

	private ArrayList<Player> Players = new ArrayList<Player>();
	
	private static ArrayList<String> Spectators = new ArrayList<String>();
	
	private HashMap<Player, AbilityBase> Abilities = new HashMap<Player, AbilityBase>();
	
	private Invincibility invincibility = new Invincibility();
	
	private DeathManager deathManager = new DeathManager();
	
	private GameAPI gameAPI = new GameAPI(this);
	
	private boolean GameStarted = false;
	
	TimerBase NoHunger = new TimerBase() {
		
		@Override
		public void TimerStart() {
			Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&a배고픔 무제한이 적용됩니다."));
		}
		
		@Override
		public void TimerProcess(Integer Seconds) {
			for(Player p : getPlayers()) {
				p.setFoodLevel(19);
			}
		}
		
		@Override
		public void TimerEnd() {}
	};
	
	@Override
	public void run() {
		if(AbilityWarThread.getAbilitySelect() == null) {
			Seconds++;
			GameProgress(Seconds);
		}
	}
	
	public void GameProgress(Integer Seconds) {
		switch(Seconds) {
			case 1:
				SetupPlayers();
				broadcastPlayerList();
				if(Players.size() < 1) {
					AbilityWarThread.toggleGameTask(false);
					Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c최소 플레이어 수를 충족하지 못하여 게임을 중지합니다."));
				}
				break;
			case 5:
				broadcastPluginDescription();
				break;
			case 10:
				if(AbilityWarSettings.getDrawAbility()) {
					broadcastAbilityReady();
				} else {
					this.Seconds += 4;
				}
				break;
			case 13:
				if(AbilityWarSettings.getDrawAbility()) {
					readyAbility();
					
					AbilityWarProgressEvent event = new AbilityWarProgressEvent(Progress.AbilitySelect_STARTED, getGameAPI());
					Bukkit.getPluginManager().callEvent(event);
				}
				break;
			case 15:
				if(AbilityWarSettings.getDrawAbility()) {
					Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "모든 플레이어가 능력을 &b확정&f했습니다."));
					
					AbilityWarProgressEvent event = new AbilityWarProgressEvent(Progress.AbilitySelect_FINISHED, getGameAPI());
					Bukkit.getPluginManager().callEvent(event);
				} else {
					Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f능력자 게임 설정에 따라 &b능력&f을 추첨하지 않습니다."));
				}
				break;
			case 17:
				Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e잠시 후 게임이 시작됩니다."));
				break;
			case 20:
				Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e게임이 &c5&e초 후에 시작됩니다."));
				EffectUtil.broadcastSound(Sound.BLOCK_NOTE_HARP);
				break;
			case 21:
				Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e게임이 &c4&e초 후에 시작됩니다."));
				EffectUtil.broadcastSound(Sound.BLOCK_NOTE_HARP);
				break;
			case 22:
				Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e게임이 &c3&e초 후에 시작됩니다."));
				EffectUtil.broadcastSound(Sound.BLOCK_NOTE_HARP);
				break;
			case 23:
				Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e게임이 &c2&e초 후에 시작됩니다."));
				EffectUtil.broadcastSound(Sound.BLOCK_NOTE_HARP);
				break;
			case 24:
				Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e게임이 &c1&e초 후에 시작됩니다."));
				EffectUtil.broadcastSound(Sound.BLOCK_NOTE_HARP);
				break;
			case 25:
				GameStart();
				
				AbilityWarProgressEvent event = new AbilityWarProgressEvent(Progress.Game_STARTED, getGameAPI());
				Bukkit.getPluginManager().callEvent(event);
				break;
		}
	}
	
	public void broadcastPlayerList() {
		int Count = 0;
		
        ArrayList<String> msg = new ArrayList<String>();
		
		msg.add(ChatColor.translateAlternateColorCodes('&', "&6==== &e게임 참여자 목록 &6===="));
		for(Player p : Players) {
			Count++;
			msg.add(ChatColor.translateAlternateColorCodes('&', "&a" + Count + ". &f" + p.getName()));
		}
		msg.add(ChatColor.translateAlternateColorCodes('&', "&e총 인원수 : " + Count + "명"));
		msg.add(ChatColor.translateAlternateColorCodes('&', "&6==========================="));
		
		Messager.broadcastStringList(msg);
	}
	
	public void broadcastPluginDescription() {
		ArrayList<String> msg = Messager.getStringList(
				ChatColor.translateAlternateColorCodes('&', "&cAbilityWar &f- &6능력자 전쟁"),
				ChatColor.translateAlternateColorCodes('&', "&e버전 &7: &f" + AbilityWar.getPlugin().getDescription().getVersion()),
				ChatColor.translateAlternateColorCodes('&', "&b개발자 &7: &f_Marlang 말랑"),
				ChatColor.translateAlternateColorCodes('&', "&9디스코드 &7: &f말랑&7#5908"));
		
		Messager.broadcastStringList(msg);
	}
	
	public void broadcastAbilityReady() {
		ArrayList<String> msg = Messager.getStringList(
				ChatColor.translateAlternateColorCodes('&', "&f플러그인에 총 &b" + AbilityList.values().size() + "개&f의 능력이 등록되어 있습니다."),
				ChatColor.translateAlternateColorCodes('&', "&7능력을 무작위로 할당합니다..."));
		
		Messager.broadcastStringList(msg);
	}
	
	public void GameStart() {
		Bukkit.getPluginManager().registerEvents(getDeathManager(), AbilityWar.getPlugin());
		
		GiveDefaultKits();
		
		for(Player p : Players) {
			if(AbilityWarSettings.getSpawnEnable()) {
				p.teleport(AbilityWarSettings.getSpawnLocation());
			}
		}
		
		if(AbilityWarSettings.getNoHunger()) {
			NoHunger.setPeriod(1);
			NoHunger.StartTimer();
		} else {
			Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4배고픔 무제한&c이 적용되지 않습니다."));
		}
		
		if(AbilityWarSettings.getInvincibilityEnable()) {
			invincibility.StartTimer();
		} else {
			Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4초반 무적&c이 적용되지 않습니다."));
			for(AbilityBase Ability : AbilityWarThread.getGame().getAbilities().values()) {
				Ability.setRestricted(false);
			}
		}
		
		if(AbilityWarSettings.getClearWeather()) {
			for(World w : Bukkit.getWorlds()) {
				w.setStorm(false);
			}
		}
		
		setGameStarted(true);
		
		Messager.broadcastStringList(Messager.getStringList(
				ChatColor.translateAlternateColorCodes('&', "&e■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■"),
				ChatColor.translateAlternateColorCodes('&', "&f                            &cAbilityWar &f- &6능력자 전쟁              "),
				ChatColor.translateAlternateColorCodes('&', "&f                                   게임 시작                            "),
				ChatColor.translateAlternateColorCodes('&', "&e■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■")));
	}

	/**
	 * 기본 킷 전체 지급
	 */
	public void GiveDefaultKits() {
		ArrayList<ItemStack> DefaultKit = AbilityWarSettings.getDefaultKit();
		
		for(Player p : Players) {
			if(AbilityWarSettings.getInventoryClear()) {
				p.getInventory().clear();
			}
			
			for(ItemStack is : DefaultKit) {
				p.getInventory().addItem(is);
			}
			
			p.setLevel(0);
			if(AbilityWarSettings.getStartLevel() > 0) {
				p.giveExpLevels(AbilityWarSettings.getStartLevel());
				EffectUtil.sendSound(p, Sound.ENTITY_PLAYER_LEVELUP);
			}
		}
	}

	/**
	 * 기본 킷 유저 지급
	 */
	public void GiveDefaultKits(Player p) {
		ArrayList<ItemStack> DefaultKit = AbilityWarSettings.getDefaultKit();

		if(AbilityWarSettings.getInventoryClear()) {
			p.getInventory().clear();
		}
		
		for(ItemStack is : DefaultKit) {
			p.getInventory().addItem(is);
		}
		
		p.setLevel(0);
		if(AbilityWarSettings.getStartLevel() > 0) {
			p.giveExpLevels(AbilityWarSettings.getStartLevel());
			EffectUtil.sendSound(p, Sound.ENTITY_PLAYER_LEVELUP);
		}
	}
	
	public void readyAbility() {
		AbilityWarThread.toggleAbilitySelectTask(true);
		AbilityWarThread.getAbilitySelect().randomAbilityToAll();
	}
	
	public Invincibility getInvincibility() {
		return invincibility;
	}
	
	public DeathManager getDeathManager() {
		return deathManager;
	}
	
	public ArrayList<Player> getPlayers() {
		return Players;
	}
	
	public HashMap<Player, AbilityBase> getAbilities() {
		return Abilities;
	}
	
	public void addAbility(AbilityBase Ability) {
		Abilities.put(Ability.getPlayer(), Ability);
	}
	
	public void removeAbility(Player p) {
		if(Abilities.containsKey(p)) {
			AbilityBase Ability = Abilities.get(p);
			Ability.DeleteAbility();
			Abilities.remove(p);
		}
	}
	
	public void SetupPlayers() {
		ArrayList<Player> Players = new ArrayList<Player>();
		
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(!getSpectators().contains(p.getName())) {
				Players.add(p);
			}
		}
		
		this.Players = Players;
	}
	
	public boolean isGameStarted() {
		return GameStarted;
	}
	
	public void setGameStarted(boolean gameStarted) {
		GameStarted = gameStarted;
	}
	
	public static ArrayList<String> getSpectators() {
		return Spectators;
	}
	
	public void toggleAbilityRestrict(boolean bool) {
		for(AbilityBase a : Abilities.values()) {
			a.setRestricted(bool);
		}
	}
	
	public GameAPI getGameAPI() {
		return gameAPI;
	}
	
}
