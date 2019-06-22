package DayBreak.AbilityWar.Game.Games;

import java.lang.reflect.Constructor;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
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

import DayBreak.AbilityWar.AbilityWar;
import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Ability.AbilityBase.ClickType;
import DayBreak.AbilityWar.Ability.AbilityBase.MaterialType;
import DayBreak.AbilityWar.Game.Manager.DeathManager;
import DayBreak.AbilityWar.Game.Manager.Firewall;
import DayBreak.AbilityWar.Game.Manager.GameListener;
import DayBreak.AbilityWar.Game.Manager.Invincibility;
import DayBreak.AbilityWar.Utils.Messager;
import DayBreak.AbilityWar.Utils.Thread.TimerBase;
import DayBreak.AbilityWar.Utils.VersionCompat.VersionUtil;

abstract public class AbstractGame extends Thread implements Listener, EventExecutor {
	
	private static List<String> Spectators = new ArrayList<String>();
	
	public static boolean isSpectator(String name) {
		return Spectators.contains(name);
	}
	
	public static void addSpectator(String name) {
		if(!Spectators.contains(name)) {
			Spectators.add(name);
		}
	}
	
	public static void removeSpectator(String name) {
		Spectators.remove(name);
	}
	
	public static List<String> getSpectators() {
		return Spectators;
	}
	
	private final List<Participant> Participants = setupParticipants();
	
	@SuppressWarnings("unused")
	private final GameListener gameListener = new GameListener(this);
	
	private final DeathManager deathManager = new DeathManager(this);

	private final Invincibility invincibility = new Invincibility(this);
	
	@SuppressWarnings("unused")
	private final Firewall fireWall = new Firewall(this);
	
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
	
	/**
	 * setupPlayers()에서 얻은 플레이어 목록을 바탕으로 Participant 목록을 만들어 반환합니다.
	 * 반환된 목록은 Read-Only 목록으로 요소가 변경될 수 없습니다.
	 */
	private List<Participant> setupParticipants() {
		List<Participant> list = new ArrayList<Participant>();
		
		for(Player p : setupPlayers()) {
			list.add(new Participant(this, p));
		}
		
		return Collections.unmodifiableList(list);
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
	
	public Invincibility getInvincibility() {
		return invincibility;
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

		private Participant(AbstractGame game, Player player) {
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
					MaterialType mt = parseMaterialType(VersionUtil.getItemInHand(p).getType());
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
						MaterialType mt = parseMaterialType(VersionUtil.getItemInHand(p).getType());
						if(mt != null) {
							if(!e.isCancelled()) {
								if(this.hasAbility()) {
									AbilityBase Ability = this.getAbility();
									if(!Ability.isRestricted()) {
										Instant Now = Instant.now();
										long Duration = java.time.Duration.between(lastClick, Now).toMillis();
										if (Duration >= 250) {
											Entity target = e.getEntity();
											
											if(target instanceof Player) {
												Player t = (Player) target;
												if(AbstractGame.this.isParticipating(t)) {
													this.lastClick = Now;
													Ability.TargetSkill(mt, e.getEntity());
												}
											} else {
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

			ability.setRestricted(isRestricted() || !isGameStarted());

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

			ability.setRestricted(isRestricted() || !isGameStarted());
			
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
				getAbility().Remove();
				ability = null;
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
	
	abstract public class AbilitySelect extends TimerBase {
		
		private HashMap<Participant, Integer> Selectors = new HashMap<Participant, Integer>();
		
		public HashMap<Participant, Integer> getMap() {
			return Selectors;
		}

		public List<Participant> getSelectors() {
			return new ArrayList<Participant>(Selectors.keySet());
		}
		
		public boolean hasDecided(Participant p) {
			return Selectors.get(p) <= 0;
		}
		
		private void setRemainingChangeCount(Participant participant, int count) {
			Selectors.put(participant, count);
			
			if(count == 0) {
				Player p = participant.getPlayer();

				Messager.sendMessage(p, ChatColor.translateAlternateColorCodes('&', "&6능력이 확정되셨습니다. 다른 플레이어를 기다려주세요."));
				
				Messager.broadcastStringList(Messager.getStringList(
						ChatColor.translateAlternateColorCodes('&', "&e" + p.getName() + "&f님이 능력을 확정하셨습니다."),
						ChatColor.translateAlternateColorCodes('&', "&a남은 인원 &7: &f" + getLeftPlayers() + "명")));
			}
		}
		
		private void setDecided(Participant participant) {
			setRemainingChangeCount(participant, 0);
		}
		
		public boolean isSelector(Participant participant) {
			return Selectors.containsKey(participant);
		}
		
		protected AbilitySelect() {
			final int ChangeCount = getChangeCount();
			
			for(Participant p : setupPlayers()) {
				Selectors.put(p, ChangeCount);
			}
			
			this.drawAbility();
			this.StartTimer();
		}
		
		public void decideAbility(Participant participant) {
			if(isSelector(participant)) {
				setDecided(participant);
			}
		}
		
		private int getLeftPlayers() {
			int i = 0;
			for(Participant p : Selectors.keySet()) {
				if(!hasDecided(p)) {
					i++;
				}
			}
			
			return i;
		}
		
		public void Skip(String admin) {
			for(Participant p : Selectors.keySet()) {
				if(!hasDecided(p)) {
					decideAbility(p);
				}
			}

			Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f관리자 &e" + admin + "&f님이 모든 플레이어의 능력을 강제로 확정시켰습니다."));
			this.StopTimer(false);
		}
		
		/**
		 * 능력 변경 가능 횟수를 반환합니다.
		 */
		abstract protected int getChangeCount();
		
		abstract protected void drawAbility();
		
		abstract protected boolean changeAbility(Participant participant);
		
		abstract protected List<Participant> setupPlayers();
		
		abstract protected void onSelectEnd();
		
		public void alterAbility(Participant participant) {
			if(isSelector(participant) && !hasDecided(participant)) {
				setRemainingChangeCount(participant, Selectors.get(participant) - 1);
				if(changeAbility(participant)) {
					Player p = participant.getPlayer();
					
					if(!hasDecided(participant)) {
						Messager.sendStringList(p, Messager.getStringList(
								ChatColor.translateAlternateColorCodes('&', "&a당신에게 능력이 할당되었습니다. &e/ability check&f로 확인 할 수 있습니다."),
								ChatColor.translateAlternateColorCodes('&', "&e/ability yes &f명령어를 사용하면 능력을 확정합니다."),
								ChatColor.translateAlternateColorCodes('&', "&e/ability no &f명령어를 사용하면 능력을 변경할 수 있습니다.")));
					} else {
						Messager.sendMessage(p, ChatColor.translateAlternateColorCodes('&', "&a당신의 능력이 변경되었습니다. &e/ability check&f로 확인 할 수 있습니다."));
					}
				}
			}
		}
		
		private boolean Ended = false;
		
		public boolean isEnded() {
			return Ended;
		}
		
		private boolean isEveryoneSelected() {
			for(Participant Key : getSelectors()) {
				if(!hasDecided(Key)) {
					return false;
				}
			}
			
			return true;
		}
		
		private int Count = 0;
		
		@Override
		public void onStart() {}
		
		@Override
		public void TimerProcess(Integer Seconds) {
			if(!isEveryoneSelected()) {
				Count++;
				
				if(Count >= 20) {
					Messager.broadcastStringList(Messager.getStringList(
							ChatColor.translateAlternateColorCodes('&', "&c아직 모든 유저가 능력을 확정하지 않았습니다."),
							ChatColor.translateAlternateColorCodes('&', "&c/ability yes나 /ability no 명령어로 능력을 확정해주세요.")));
					Count = 0;
				}
			} else {
				this.StopTimer(false);
			}
		}
		
		@Override
		public void onEnd() {
			Ended = true;
			onSelectEnd();
		}
		
	}
	
}
