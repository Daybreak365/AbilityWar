package Marlang.AbilityWar.GameManager.Game;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.EventExecutor;

import Marlang.AbilityWar.AbilityWar;
import Marlang.AbilityWar.GameManager.Manager.AbilitySelect;
import Marlang.AbilityWar.GameManager.Manager.DeathManager;
import Marlang.AbilityWar.GameManager.Manager.Firewall;
import Marlang.AbilityWar.GameManager.Manager.GameListener;
import Marlang.AbilityWar.GameManager.Object.Participant;

abstract public class AbstractGame extends Thread implements Listener, EventExecutor {
	
	private static List<String> Spectators = new ArrayList<String>();
	
	private List<Participant> Participants = setupParticipants();
	
	@SuppressWarnings("unused")
	private GameListener gameListener = new GameListener(this);
	
	private DeathManager deathManager = new DeathManager(this);
	
	@SuppressWarnings("unused")
	private Firewall fireWall = new Firewall(this);
	
	private AbilitySelect abilitySelect;
	
	private boolean Restricted = true;
	
	private boolean GameStarted = false;
	
	protected Integer Seconds = 0;
	
	@Override
	public void run() {
		if(gameCondition()) {
			if(getAbilitySelect() == null || (getAbilitySelect() != null && getAbilitySelect().isEnded())) {
				Seconds++;
				progressGame(Seconds);
			}
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
	abstract protected List<Participant> setupParticipants();
	
	/**
	 * AbilitySelect 초깃값 설정
	 * 능력 할당을 하지 않을 예정이라면 null을 반환해도 됩니다.
	 */
	abstract protected AbilitySelect setupAbilitySelect();
	
	/**
	 * 게임중 플레이어가 사망했을 경우 호출됨
	 */
	abstract public void onPlayerDeath(PlayerDeathEvent e);
	
	/**
	 * 플레이어에게 기본 킷을 지급합니다.
	 * @param p	킷을 지급할 플레이어
	 */
	abstract public void GiveDefaultKit(Player p);
	
	/**
	 * 모든 플레이어들에게 기본 킷을 지급합니다.
	 */
	public void GiveDefaultKit() {
		for(Participant p : getParticipants()) {
			GiveDefaultKit(p.getPlayer());
		}
	}
	
	/**
	 * 관전자 목록을 반환합니다.
	 * @return	관전자 목록
	 */
	public static List<String> getSpectators() {
		return Spectators;
	}
	
	/**
	 * 참여자 목록을 반환합니다.
	 * @return	참여자 목록
	 */
	public List<Participant> getParticipants() {
		return Participants;
	}
	
	/**
	 * 대상 플레이어의 참여 여부를 반환합니다.
	 * @param p	대상 플레이어
	 * @return	대상 플레이어의 참여 여부
	 */
	public boolean isParticipating(Player p) {
		return Participant.checkParticipant(p);
	}
	
	/**
	 * DeathManager를 반환합니다.
	 * @return	DeathManager
	 */
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
	
	protected void startAbilitySelect() {
		this.abilitySelect = setupAbilitySelect();
	}
	
	protected void setGameStarted(boolean gameStarted) {
		GameStarted = gameStarted;
	}
	
	abstract protected void onEnd();
	
}
