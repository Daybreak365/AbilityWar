package Marlang.AbilityWar.GameManager.Game;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
import Marlang.AbilityWar.GameManager.Manager.AbilitySelect;
import Marlang.AbilityWar.GameManager.Manager.DeathManager;
import Marlang.AbilityWar.GameManager.Manager.Firewall;
import Marlang.AbilityWar.GameManager.Manager.GameListener;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Thread.TimerBase;

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
	
	public boolean hasAbility(Player p) {
		return Abilities.containsKey(p) && Abilities.get(p) != null;
	}
	
	/**
	 * 플레이어에게 능력이 있을 경우 능력을 반환합니다.
	 * 플레이어에게 능력이 없을 경우 null을 반환합니다.
	 */
	public AbilityBase getAbility(Player p) {
		if(hasAbility(p)) {
			return Abilities.get(p);
		} else {
			return null;
		}
	}
	
	public void addAbility(Player p, Class<? extends AbilityBase> abilityClass) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Constructor<? extends AbilityBase> constructor = abilityClass.getConstructor(Player.class);
		AbilityBase Ability = constructor.newInstance(p);
		
		addAbility(Ability);
	}
	
	private void addAbility(AbilityBase Ability) {
		Player p = Ability.getPlayer();
		if(isParticipating(p)) {
			if(isRestricted()) {
				Ability.setRestricted(true);
			} else {
				if(isGameStarted()) {
					Ability.setRestricted(false);
				} else {
					Ability.setRestricted(true);
				}
			}
			
			if(hasAbility(p)) {
				removeAbility(p);
			}
			
			Abilities.put(p, Ability);
		} else {
			throw new IllegalArgumentException("대상이 게임에 참여중이지 않습니다.");
		}
	}
	
	public void removeAbility(Player p) {
		if(Abilities.containsKey(p)) {
			AbilityBase Ability = Abilities.get(p);
			Ability.DeleteAbility();
			Abilities.remove(p);
		}
	}
	
	/**
	 * Ability.getPlayer()의 능력을 to에게 옮깁니다.
	 * to가 게임에 참여중이지 않을 경우 아무 작업도 하지 않습니다.
	 */
	public void transferAbility(AbilityBase Ability, Player to) {
		if(isParticipating(to)) {
			removeAbility(Ability.getPlayer());
			Ability.updatePlayer(to);
			addAbility(Ability);
		}
	}
	
	public void swapAbility(AbilityBase one, AbilityBase two) {
		Player onePlayer = one.getPlayer();
		Player twoPlayer = two.getPlayer();
		
		removeAbility(onePlayer);
		removeAbility(twoPlayer);
		
		one.updatePlayer(twoPlayer);
		two.updatePlayer(onePlayer);
		
		addAbility(one);
		addAbility(two);
	}
	
	public static List<String> getSpectators() {
		return Spectators;
	}
	
	public List<Player> getParticipants() {
		return Participants;
	}
	
	public boolean isParticipating(Player p) {
		return Participants.contains(p);
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
	 * @return AbilitySelect (사용하지 않을 경우 Null 반환, 능력 추첨 전일 경우 null 반환)
	 */
	public AbilitySelect getAbilitySelect() {
		return abilitySelect;
	}
	
	public HashMap<Player, AbilityBase> getAbilities() {
		return Abilities;
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
	
	public void onGameEnd() {
		TimerBase.ResetTasks();
		HandlerList.unregisterAll(getGameListener());
		HandlerList.unregisterAll(this);
		Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&7게임이 초기화되었습니다."));
	}
	
}
