package Marlang.AbilityWar.GameManager.Game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.EventExecutor;

import Marlang.AbilityWar.AbilityWar;
import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.GameManager.GameListener;
import Marlang.AbilityWar.GameManager.Manager.AbilitySelect;
import Marlang.AbilityWar.GameManager.Manager.DeathManager;
import Marlang.AbilityWar.GameManager.Manager.Firewall;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.TimerBase;

abstract public class AbstractGame extends Thread implements Listener, EventExecutor {
	
	private static List<String> Spectators = new ArrayList<String>();
	
	private List<Player> Participants = setupParticipants();
	
	private HashMap<Player, AbilityBase> Abilities = new HashMap<Player, AbilityBase>();
	
	private GameListener gameListener = new GameListener(this);
	
	private DeathManager deathManager = new DeathManager(this);
	
	private Firewall fireWall = new Firewall(this);
	
	private AbilitySelect abilitySelect;
	
	private boolean Restricted = true;
	
	private boolean GameStarted = false;
	
	protected Integer Seconds = 0;
	
	@Override
	public void run() {
		if(gameCondition()) {
			Seconds++;
			progressGame(Seconds);
		}
	}
	
	/**
	 * Register Event Handler
	 */
	protected void registerEvent(Class<? extends Event> event) {
		Bukkit.getPluginManager().registerEvent(event, this, EventPriority.HIGHEST, this, AbilityWar.getPlugin());
	}
	
	/**
	 * 게임 진행
	 */
	abstract protected void progressGame(Integer Seconds);
	
	/**
	 * 게임 진행 조건
	 */
	abstract protected boolean gameCondition();
	
	/**
	 * 참여자 초깃값 설정
	 */
	abstract protected List<Player> setupParticipants();
	
	/**
	 * AbilitySelect 초깃값 설정
	 * 능력 할당을 하지 않을 예정이라면 null을 반환해도 됩니다.
	 */
	abstract protected AbilitySelect setupAbilitySelect();
	
	/**
	 * 게임중 플레이어가 사망했을 경우 호출됨
	 */
	abstract public void onPlayerDeath(PlayerDeathEvent e);
	
	public void addAbility(AbilityBase Ability) {
		if(isRestricted()) {
			Ability.setRestricted(true);
		} else {
			if(isGameStarted()) {
				Ability.setRestricted(false);
			} else {
				Ability.setRestricted(true);
			}
		}
		
		Abilities.put(Ability.getPlayer(), Ability);
	}
	
	public void removeAbility(Player p) {
		if(Abilities.containsKey(p)) {
			AbilityBase Ability = Abilities.get(p);
			Ability.DeleteAbility();
			Abilities.remove(p);
		}
	}
	
	public static List<String> getSpectators() {
		return Spectators;
	}
	
	public List<Player> getParticipants() {
		return Participants;
	}
	
	public HashMap<Player, AbilityBase> getAbilities() {
		return Abilities;
	}
	
	public DeathManager getDeathManager() {
		return deathManager;
	}
	
	public boolean isRestricted() {
		return Restricted;
	}
	
	public void setRestricted(boolean restricted) {
		Restricted = restricted;
	}
	
	public boolean isGameStarted() {
		return GameStarted;
	}
	
	/**
	 * AbilitySelect를 받아옵니다.
	 * @return AbilitySelect (사용하지 않을 경우 Null 반환)
	 */
	public AbilitySelect getAbilitySelect() {
		return abilitySelect;
	}
	
	protected void setAbilitySelect(AbilitySelect abilitySelect) {
		this.abilitySelect = abilitySelect;
	}
	
	protected void setGameStarted(boolean gameStarted) {
		GameStarted = gameStarted;
	}
	
	protected Firewall getFireWall() {
		return fireWall;
	}
	
	protected GameListener getGameListener() {
		return gameListener;
	}
	
	public void GameEnd() {
		TimerBase.ResetTasks();
		HandlerList.unregisterAll(this);
		Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&7게임이 초기화되었습니다."));
	}
	
}
