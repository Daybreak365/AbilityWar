package Marlang.AbilityWar.GameManager.Game;

import java.lang.reflect.Constructor;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.EventExecutor;

import Marlang.AbilityWar.AbilityWar;
import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.AbilityBase.ClickType;
import Marlang.AbilityWar.Ability.AbilityBase.MaterialType;
import Marlang.AbilityWar.GameManager.Manager.AbilitySelect;
import Marlang.AbilityWar.GameManager.Manager.DeathManager;
import Marlang.AbilityWar.GameManager.Manager.Firewall;
import Marlang.AbilityWar.GameManager.Manager.GameListener;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.VersionCompat.PlayerCompat;

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
	
	private Integer Seconds = 0;
	
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
	abstract protected List<Player> setupPlayers();
	
	private List<Participant> setupParticipants() {
		List<Participant> participantList = new ArrayList<Participant>();
		
		for(Player p : setupPlayers()) {
			participantList.add(new Participant(this, p));
		}
		
		return participantList;
	}
	
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

	private HashMap<String, Participant> participantCache = new HashMap<String, Participant>();
	
	/**
	 * 해당 플레이어를 기반으로 하는 참여자를 반환합니다.
	 * @param player	탐색할 플레이어
	 * @return			참여자가 존재할 경우 참여자를 반환합니다.
	 * 					참여자가 존재하지 않을 경우 null을 반환합니다.
	 * 					null 체크가 필요합니다.
	 */
	public Participant getParticipant(Player player) {
		String Key = player.getUniqueId().toString();
		if(participantCache.containsKey(Key)) {
			return participantCache.get(Key);
		} else {
			for(Participant participant : getParticipants()) {
				if(participant.getPlayer().equals(player)) {
					participantCache.put(Key, participant);
					return participant;
				}
			}

			participantCache.put(Key, null);
			return null;
		}
	}
	
	/**
	 * 대상 플레이어의 참여 여부를 반환합니다.
	 * @param p	대상 플레이어
	 * @return	대상 플레이어의 참여 여부
	 */
	public boolean isParticipating(Player player) {
		return getParticipant(player) != null;
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
	
	protected Integer getSeconds() {
		return Seconds;
	}

	protected void setSeconds(Integer seconds) {
		Seconds = seconds;
	}

	protected void startAbilitySelect() {
		this.abilitySelect = setupAbilitySelect();
	}
	
	protected void setGameStarted(boolean gameStarted) {
		GameStarted = gameStarted;
	}
	
	abstract protected void onEnd();
	
	public class Participant implements EventExecutor {
	
		private Player player;
		private final AbstractGame game;

		private Participant(AbstractGame game, Player player) {
			this.game = game;
			this.player = player;

			Bukkit.getPluginManager().registerEvent(PlayerLoginEvent.class, game, EventPriority.HIGH, this, AbilityWar.getPlugin());
			Bukkit.getPluginManager().registerEvent(PlayerInteractEvent.class, game, EventPriority.HIGH, this, AbilityWar.getPlugin());
			Bukkit.getPluginManager().registerEvent(EntityDamageByEntityEvent.class, game, EventPriority.HIGH, this, AbilityWar.getPlugin());
		}

		private Instant lastClick = Instant.now();

		@Override
		public void execute(Listener listener, Event event) throws EventException {
			if (event instanceof PlayerLoginEvent) {
				PlayerLoginEvent e = (PlayerLoginEvent) event;
				if (e.getPlayer().getUniqueId().equals(player.getUniqueId())) {
					this.setPlayer(e.getPlayer());
				}
			} else if (event instanceof PlayerInteractEvent) {
				PlayerInteractEvent e = (PlayerInteractEvent) event;

				Player p = e.getPlayer();
				if (p.equals(getPlayer())) {
					MaterialType mt = parseMaterialType(PlayerCompat.getItemInHand(p).getType());
					ClickType ct = e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK) ? ClickType.RightClick : ClickType.LeftClick;
					if (mt != null) {
						if (hasAbility()) {
							if (!getAbility().isRestricted()) {
								Instant Now = Instant.now();
								long Duration = java.time.Duration.between(lastClick, Now).toMillis();
								if (Duration >= 250) {
									this.lastClick = Now;
									ActiveSkill(getAbility(), mt, ct);

									if (ct.equals(ClickType.LeftClick)) {
										getAbility().TargetSkill(mt, null);
									}
								}
							}
						}
					}
				}
			} else if (event instanceof EntityDamageByEntityEvent) {
				EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
				
				if(e.getDamager() instanceof Player) {
					Player p = (Player) e.getDamager();
					if (p.equals(getPlayer())) {
						MaterialType mt = parseMaterialType(PlayerCompat.getItemInHand(p).getType());
						if(mt != null) {
							if(!e.isCancelled()) {
								if(this.hasAbility()) {
									AbilityBase Ability = this.getAbility();
									if(!Ability.isRestricted()) {
										Instant Now = Instant.now();
										long Duration = java.time.Duration.between(lastClick, Now).toMillis();
										if (Duration >= 250) {
											this.lastClick = Now;
											Ability.TargetSkill(mt, e.getEntity());
										}
									}
								}
							}
						}
					}
				}
			}
		}

		private void ActiveSkill(AbilityBase Ability, MaterialType mt, ClickType ct) {
			boolean Executed = Ability.ActiveSkill(mt, ct);

			if (Executed) {
				Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&d능력을 사용하였습니다."));
			}
		}

		private MaterialType parseMaterialType(Material m) {
			for (MaterialType Type : MaterialType.values()) {
				if (Type.getMaterial().equals(m)) {
					return Type;
				}
			}

			return null;
		}

		private AbilityBase ability;

		/**
		 * 플레이어에게 해당 능력을 부여합니다.
		 * @param p            	능력을 부여할 플레이어
		 * @param abilityClass 	부여할 능력의 종류 (능력 클래스)
		 * @throws Exception 	능력을 부여하는 도중 오류가 발생하였을 경우
		 */
		public void setAbility(Class<? extends AbilityBase> abilityClass) throws Exception {
			if(hasAbility()) {
				removeAbility();
			}
			
			Constructor<? extends AbilityBase> constructor = abilityClass.getConstructor(Participant.class);
			AbilityBase ability = constructor.newInstance(this);
			
			if(this.game.isRestricted()) {
				ability.setRestricted(true);
			} else {
				if(this.game.isGameStarted()) {
					ability.setRestricted(false);
				} else {
					ability.setRestricted(true);
				}
			}

			this.ability = ability;
		}

		/**
		 * 플레이어에게 해당 능력을 부여합니다.
		 * @param ability	부여할 능력
		 */
		public void setAbility(AbilityBase ability) {
			if(hasAbility()) {
				removeAbility();
			}
			
			if(this.game.isRestricted()) {
				ability.setRestricted(true);
			} else {
				if(this.game.isGameStarted()) {
					ability.setRestricted(false);
				} else {
					ability.setRestricted(true);
				}
			}
			
			this.ability = ability;
		}

		public boolean hasAbility() {
			return ability != null;
		}

		public AbilityBase getAbility() {
			return ability;
		}
		
		public void removeAbility() {
			if(getAbility() != null) {
				getAbility().DeleteAbility();
				ability = null;
			}
		}
		
		/**
		 * 이 플레이어의 능력을 to에게 옮깁니다.
		 * to가 게임에 참여하고 있지 않거나 이 플레이어에게 능력이 없을 경우 아무 작업도 하지 않습니다.
		 */
		public void transferAbility(Participant target) {
			if(hasAbility()) {
				AbilityBase Ability = getAbility();
				removeAbility();
				
				Ability.updateParticipant(target);
				
				target.setAbility(Ability);
			}
		}
		
		/**
		 * one.getPlayer()와 two.getPlayer()의 능력을 서로 뒤바꿉니다.
		 * @param one	첫번째 플레이어
		 * @param two	두번째 플레이어
		 */
		public void swapAbility(Participant target) {
			if(hasAbility() && target.hasAbility()) {
				AbilityBase first = getAbility();
				AbilityBase second = target.getAbility();
				
				removeAbility();
				target.removeAbility();
				
				first.updateParticipant(target);
				second.updateParticipant(this);
				
				this.setAbility(second);
				target.setAbility(first);
			}
		}
		
		public Player getPlayer() {
			return player;
		}

		private void setPlayer(Player player) {
			this.player = player;
		}

	}
	
}
