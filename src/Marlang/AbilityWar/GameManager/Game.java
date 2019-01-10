package Marlang.AbilityWar.GameManager;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import Marlang.AbilityWar.AbilityWar;
import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.AbilityList;
import Marlang.AbilityWar.Utils.AbilityWarThread;
import Marlang.AbilityWar.Utils.EffectUtil;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.TimerBase;

/**
 * 게임 관리 클래스
 * @author _Marlang 말랑
 */
public class Game extends Thread {
	
	private static AbilityWar Plugin;
	
	public static void Initialize(AbilityWar Plugin) {
		Game.Plugin = Plugin;
	}
	
	int Seconds = 0;

	ArrayList<Player> Players = new ArrayList<Player>();
	ArrayList<Player> Spectators = new ArrayList<Player>();
	
	HashMap<Player, AbilityBase> Abilities = new HashMap<Player, AbilityBase>();
	
	Invincibility invincibility = new Invincibility();
	
	boolean GameStarted = false;
	
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
				broadcastAbilityReady();
				break;
			case 13:
				readyAbility();
				break;
			case 15:
				Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "모든 플레이어가 능력을 &b확정&f했습니다."));
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
				ChatColor.translateAlternateColorCodes('&', "&e버전 &7: &f" + Plugin.getDescription().getVersion()),
				ChatColor.translateAlternateColorCodes('&', "&b개발자 &7: &f_Marlang"),
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
		setGameStarted(true);
		
		Messager.broadcastStringList(Messager.getStringList(
				ChatColor.translateAlternateColorCodes('&', "&e■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■"),
				ChatColor.translateAlternateColorCodes('&', "&f                            &cAbilityWar &f- &6능력자 전쟁              "),
				ChatColor.translateAlternateColorCodes('&', "&f                                   게임 시작                            "),
				ChatColor.translateAlternateColorCodes('&', "&e■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■")));
		
		ArrayList<ItemStack> DefaultKit = AbilityWar.getSetting().getDefaultKit();
		
		for(Player p : Players) {
			p.getInventory().clear();
			for(ItemStack is : DefaultKit) {
				p.getInventory().addItem(is);
			}
			
			p.setLevel(0);
			if(AbilityWar.getSetting().getStartLevel() > 0) {
				p.giveExpLevels(AbilityWar.getSetting().getStartLevel());
				EffectUtil.sendSound(p, Sound.ENTITY_PLAYER_LEVELUP);
			}
			
			if(AbilityWar.getSetting().getSpawnEnable()) {
				p.teleport(AbilityWar.getSetting().getSpawnLocation());
			}
		}
		
		if(AbilityWar.getSetting().getNoHunger()) {
			NoHunger.setPeriod(1);
			NoHunger.StartTimer();
		} else {
			Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4배고픔 무제한&c이 적용되지 않습니다."));
		}
		
		if(AbilityWar.getSetting().getInvincibilityEnable()) {
			invincibility.StartTimer();
		} else {
			Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4초반 무적&c이 적용되지 않습니다."));
			for(AbilityBase Ability : AbilityWarThread.getGame().getAbilities().values()) {
				Ability.setRestricted(false);
			}
		}
	}
	
	public void readyAbility() {
		AbilityWarThread.toggleAbilitySelectTask(true);
		AbilityWarThread.getAbilitySelect().randomAbilityToAll();
	}
	
	public Invincibility getInvincibility() {
		return invincibility;
	}
	
	public ArrayList<Player> getPlayers() {
		return Players;
	}
	
	public HashMap<Player, AbilityBase> getAbilities() {
		return Abilities;
	}
	
	public void PrintAbilities() {
		for(AbilityBase Ability : Abilities.values()) {
			Bukkit.broadcastMessage(Ability.getPlayer().getName() + " : " + Ability.getAbilityName());
		}
	}
	
	public void addAbility(AbilityBase Ability) {
		if(Ability.getPlayer() != null) {
			Abilities.put(Ability.getPlayer(), Ability);
		}
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
			if(!getSpectators().contains(p)) {
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
	
	public ArrayList<Player> getSpectators() {
		return Spectators;
	}
	
	public void toggleAbilityRestrict(boolean bool) {
		for(AbilityBase a : Abilities.values()) {
			a.setRestricted(bool);
		}
	}
	
}
