package DayBreak.AbilityWar.Game.Games.Mode;

import static DayBreak.AbilityWar.Utils.Validate.notNull;

import java.lang.reflect.Constructor;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.plugin.EventExecutor;

import DayBreak.AbilityWar.AbilityWar;
import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Ability.AbilityBase.ClickType;
import DayBreak.AbilityWar.Ability.AbilityBase.MaterialType;
import DayBreak.AbilityWar.Config.AbilityWarSettings;
import DayBreak.AbilityWar.Game.Events.GameEndEvent;
import DayBreak.AbilityWar.Game.Events.GameReadyEvent;
import DayBreak.AbilityWar.Game.Events.GameStartEvent;
import DayBreak.AbilityWar.Game.Manager.DeathManager;
import DayBreak.AbilityWar.Game.Manager.EffectManager;
import DayBreak.AbilityWar.Game.Manager.Firewall;
import DayBreak.AbilityWar.Game.Manager.Invincibility;
import DayBreak.AbilityWar.Game.Manager.ScoreboardManager;
import DayBreak.AbilityWar.Game.Manager.WRECK;
import DayBreak.AbilityWar.Game.Manager.PassiveManager.PassiveManager;
import DayBreak.AbilityWar.Utils.Messager;
import DayBreak.AbilityWar.Utils.Thread.Timer;
import DayBreak.AbilityWar.Utils.Thread.TimerBase;
import DayBreak.AbilityWar.Utils.VersionCompat.VersionUtil;

public abstract class AbstractGame extends Timer implements Listener {

	private final Map<String, Participant> participants = initParticipants();
	
	/**
	 * {@link String}이 key이고 {@link Participant}가 value인 맵의 초기값을 설정하기 위해 사용되는 메소드입니다.
	 * @return 맵이 Unmodifiable {@link Map}으로 반환되어 요소가 변경될 수 없습니다.
	 * @throws
	 */
	private final Map<String, Participant> initParticipants() {
		Map<String, Participant> initial = new HashMap<>();
		for(Player p : notNull(initPlayers())) initial.put(p.getUniqueId().toString(), new Participant(this, p));
		return Collections.unmodifiableMap(initial);
	}
	
	/**
	 * 게임에 참가할 {@link Player} {@link List} 초깃값 설정
	 * @NotNull
	 */
	protected abstract List<Player> initPlayers();

	private final List<Listener> registeredListeners = new ArrayList<>();
	/**
	 * 게임이 종료될 때 등록 해제되어야 하는 {@link Listener}를 등록합니다.
	 */
	public final void registerListener(Listener listener) {
		Bukkit.getPluginManager().registerEvents(notNull(listener), AbilityWar.getPlugin());
		registeredListeners.add(listener);
	}

	private final DeathManager deathManager = notNull(setupDeathManager());
	
	private final Invincibility invincibility = new Invincibility(this);
	
	private final EffectManager effectManager = new EffectManager(this);
	
	private final WRECK wreck = new WRECK(this);
	
	private final ScoreboardManager scoreboardManager = new ScoreboardManager(this);
	
	private final PassiveManager passiveManager = new PassiveManager(this);
	
	@SuppressWarnings("unused")
	private final Firewall fireWall = new Firewall(this);
	
	private AbilitySelect abilitySelect = null;
	
	private boolean Restricted = true;
	
	private boolean GameStarted = false;
	
	private int Seconds = 0;

	@Override
	protected void onStart() {
		Bukkit.getPluginManager().callEvent(new GameReadyEvent(this));
		registerListener(this);
	}
	
	@Override
	protected void TimerProcess(Integer count) {
		if(getAbilitySelect() == null || (getAbilitySelect() != null && getAbilitySelect().isEnded())) {
			Seconds++;
			progressGame(Seconds);
		}
	}

	@Override
	protected void onEnd() {
		TimerBase.ResetTasks();
		HandlerList.unregisterAll(this);
		for(Listener lis : registeredListeners) HandlerList.unregisterAll(lis);
		this.scoreboardManager.Clear();
		this.onGameEnd();
		Bukkit.getPluginManager().callEvent(new GameEndEvent(this));
	}
	
	protected abstract void onGameEnd();
	
	/**
	 * 게임 진행
	 */
	protected abstract void progressGame(Integer Seconds);
	
	/**
	 * AbilitySelect 초깃값 설정
	 * @Nullable 능력 할당이 필요하지 않을 경우 null을 반환하세요.
	 */
	protected abstract AbilitySelect setupAbilitySelect();

	/**
	 * DeathManager 초깃값 설정
	 * @NotNull
	 */
	protected DeathManager setupDeathManager() {
		return new DeathManager(this);
	}
	
	/**
	 * 플레이어에게 기본 킷을 지급합니다.
	 * @param p	킷을 지급할 플레이어
	 */
	public abstract void GiveDefaultKit(Player p);
	
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
	public Collection<Participant> getParticipants() {
		return participants.values();
	}

	protected ScoreboardManager getScoreboardManager() {
		return scoreboardManager;
	}

	/**
	 * {@link Player}를 기반으로 하는 {@link Participant}를 탐색합니다.
	 * @param player	탐색할 플레이어
	 * @return			존재할 경우 {@link Participant}를 반환합니다.
	 * 					존재하지 않을 경우 null을 반환합니다.
	 * @Nullable
	 */
	public final Participant getParticipant(final Player player) {
		String key = player.getUniqueId().toString();
		if(participants.containsKey(key)) return participants.get(key);
		return null;
	}

	/**
	 * 해당 {@link UUID}를 가지고 있는 {@link Player}를 기반으로 하는 {@link Participant}를 탐색합니다.
	 * @param player	탐색할 플레이어
	 * @return			존재할 경우 {@link Participant}를 반환합니다.
	 * 					존재하지 않을 경우 null을 반환합니다.
	 * @Nullable
	 */
	public final Participant getParticipant(final UUID uuid) {
		String key = uuid.toString();
		if(participants.containsKey(key)) return participants.get(key);
		return null;
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
	 * @NotNull
	 */
	public DeathManager getDeathManager() {
		return deathManager;
	}

	/**
	 * EffectManager를 반환합니다.
	 * @NotNull
	 */
	public EffectManager getEffectManager() {
		return effectManager;
	}

	/**
	 * WRECK을 반환합니다.
	 * @NotNull
	 */
	public WRECK getWRECK() {
		return wreck;
	}

	/**
	 * PassiveManager을 반환합니다.
	 * @NotNull
	 */
	public PassiveManager getPassiveManager() {
		return passiveManager;
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
	 * AbilitySelect를 반환합니다.
	 * @Nullable 능력 할당 전이거나 능력 할당 기능을 사용하지 않을 경우 null을 반환합니다.
	 */
	public AbilitySelect getAbilitySelect() {
		return abilitySelect;
	}
	
	/**
	 * Invincibility를 반환합니다.
	 * @NotNull
	 */
	public Invincibility getInvincibility() {
		return invincibility;
	}

	protected int getSeconds() {
		return Seconds;
	}

	protected void setSeconds(Integer seconds) {
		Seconds = seconds;
	}

	protected void startAbilitySelect() {
		this.abilitySelect = setupAbilitySelect();
	}
	
	protected void startGame() {
		GameStarted = true;
		wreck.noticeIfEnabled();
		this.getScoreboardManager().Initialize();
		Bukkit.getPluginManager().callEvent(new GameStartEvent(this));
	}

	@EventHandler
	public void onWeatherChange(WeatherChangeEvent e) {
		if(GameStarted && AbilityWarSettings.getClearWeather()) e.setCancelled(true);
	}
	
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent e) {
		if(AbilityWarSettings.getNoHunger()) {
			e.setCancelled(true);
			
			Player p = (Player) e.getEntity();
			p.setFoodLevel(19);
		}
	}
	
	public class Participant implements EventExecutor {
	
		private Player player;

		private Participant(AbstractGame game, Player player) {
			this.player = player;

			Bukkit.getPluginManager().registerEvent(PlayerLoginEvent.class, game, EventPriority.HIGH, this, AbilityWar.getPlugin());
			Bukkit.getPluginManager().registerEvent(PlayerInteractEvent.class, game, EventPriority.HIGH, this, AbilityWar.getPlugin());
			Bukkit.getPluginManager().registerEvent(PlayerInteractAtEntityEvent.class, game, EventPriority.HIGH, this, AbilityWar.getPlugin());
		}

		private Instant lastClick = Instant.now();

		@Override
		public void execute(Listener listener, Event event) throws EventException {
			if (event instanceof PlayerLoginEvent) {
				PlayerLoginEvent e = (PlayerLoginEvent) event;
				if (e.getPlayer().getUniqueId().equals(player.getUniqueId())) {
					this.player = e.getPlayer();
				}
			} else if (event instanceof PlayerInteractEvent) {
				PlayerInteractEvent e = (PlayerInteractEvent) event;

				Player p = e.getPlayer();
				if (p.equals(getPlayer())) {
					MaterialType mt = parseMaterialType(VersionUtil.getItemInHand(p).getType());
					ClickType ct = e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK) ? ClickType.RightClick : ClickType.LeftClick;
					if (mt != null) {
						if (hasAbility()) {
							AbilityBase Ability = this.getAbility();
							if (!Ability.isRestricted()) {
								Instant Now = Instant.now();
								long Duration = java.time.Duration.between(lastClick, Now).toMillis();
								if (Duration >= 250) {
									this.lastClick = Now;
									ActiveSkill(Ability, mt, ct);
								}
							}
						}
					}
				}
			} else if (event instanceof PlayerInteractAtEntityEvent) {
				PlayerInteractAtEntityEvent e = (PlayerInteractAtEntityEvent) event;

				Player p = e.getPlayer();
				if (p.equals(getPlayer())) {
					MaterialType mt = parseMaterialType(VersionUtil.getItemInHand(p).getType());
					if(mt != null && !e.isCancelled() && this.hasAbility()) {
						AbilityBase Ability = this.getAbility();
						if(!Ability.isRestricted()) {
							Instant Now = Instant.now();
							long Duration = java.time.Duration.between(lastClick, Now).toMillis();
							if (Duration >= 250) {
								Entity targetEntity = e.getRightClicked();
								if(targetEntity instanceof LivingEntity) {
									if(targetEntity instanceof Player) {
										Player targetPlayer = (Player) targetEntity;
										if(AbstractGame.this.isParticipating(targetPlayer)) {
											this.lastClick = Now;
											Ability.TargetSkill(mt, targetPlayer);
										}
									} else {
										LivingEntity target = (LivingEntity) targetEntity;
										this.lastClick = Now;
										Ability.TargetSkill(mt, target);
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
			if(hasAbility()) removeAbility();
			
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
				getAbility().destroy();
				ability = null;
			}
		}
		
		public Player getPlayer() {
			return player;
		}

	}
	
	public abstract class AbilitySelect extends TimerBase {

		private final int changeCount = initChangeCount();
		
		/**
		 * 능력 변경 가능 횟수를 설정합니다.
		 */
		protected abstract int initChangeCount();
		
		private final Map<Participant, Integer> selectors = setupSelectors();

		private final Map<Participant, Integer> setupSelectors() {
			Map<Participant, Integer> initial = new HashMap<>();
			for(Participant p : notNull(initSelectors())) initial.put(p, changeCount);
			return initial;
		}
		
		/**
		 * 능력을 선택할 {@link Participant} 목록을 설정합니다.
		 * @NotNull
		 */
		protected abstract Collection<Participant> initSelectors();
		
		/**
		 * 능력을 선택할 {@link Participant} 목록을 반환합니다.
		 */
		public final Collection<Participant> getSelectors() {
			return selectors.keySet();
		}

		/**
		 * {@link Participant}에게 남은 능력 변경 횟수를 설정합니다.
		 */
		private final void setRemainingChangeCount(Participant participant, int count) {
			selectors.put(participant, count);
			
			if(count == 0) {
				Player p = participant.getPlayer();

				Messager.sendMessage(p, ChatColor.translateAlternateColorCodes('&', "&6능력이 확정되셨습니다. 다른 플레이어를 기다려주세요."));
				
				Messager.broadcastStringList(Messager.getStringList(
						ChatColor.translateAlternateColorCodes('&', "&e" + p.getName() + "&f님이 능력을 확정하셨습니다."),
						ChatColor.translateAlternateColorCodes('&', "&a남은 인원 &7: &f" + getLeftPlayersCount() + "명")));
			}
		}

		/**
		 * 능력을 아직 결정하지 않은 참가자의 수를 반환합니다.
		 */
		private final int getLeftPlayersCount() {
			int count = 0;
			for(Participant p : getSelectors()) if(!hasDecided(p)) count++;
			return count;
		}
		
		/**
		 * {@link Participant}의 능력 선택 여부를 반환합니다.
		 * 능력을 선택중인 {@link Participant}가 아닐 경우 false를 반환합니다.
		 */
		public final boolean hasDecided(final Participant participant) {
			if(selectors.containsKey(participant)) {
				return selectors.get(participant) <= 0;
			} else {
				return false;
			}
		}

		/**
		 * 능력 선택 중 {@link Participant}의 능력을 변경합니다.
		 */
		public final void alterAbility(Participant participant) {
			if(isSelector(participant) && !hasDecided(participant)) {
				setRemainingChangeCount(participant, selectors.get(participant) - 1);
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

		/**
		 * 참가자들의 초기 능력을 설정합니다.
		 */
		protected abstract void drawAbility(Collection<Participant> selectors);
		
		/**
		 * 능력 선택 중 {@link Participant}의 능력을 변경합니다.
		 */
		protected abstract boolean changeAbility(Participant participant);
		
		/**
		 * 능력 선택 중 {@link Participant}의 능력을 결정합니다.
		 * 능력을 결정하면 더 이상 능력을 변경할 수 없습니다.
		 */
		public final void decideAbility(Participant participant) {
			if(isSelector(participant)) setRemainingChangeCount(participant, 0);
		}
		
		/**
		 * {@link Participant}가 능력 선택에 참여한 참가자인지의 여부를 반환합니다.
		 */
		public final boolean isSelector(Participant participant) {
			return selectors.containsKey(participant);
		}

		/**
		 * 모든 참가자의 능력을 강제로 결정합니다.
		 * @param admin		출력할 관리자의 이름
		 */
		public final void Skip(String admin) {
			for(Participant p : getSelectors()) if(!hasDecided(p)) decideAbility(p);

			Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f관리자 &e" + admin + "&f님이 모든 플레이어의 능력을 강제로 확정시켰습니다."));
			this.StopTimer(false);
		}

		protected AbilitySelect() {
			drawAbility(getSelectors());
			StartTimer();
		}
		
		@Override
		public void onStart() {}
		
		@Override
		public void TimerProcess(Integer Seconds) {
			if(!isEveryoneSelected()) {
				if(Seconds % 20 == 0) {
					Messager.broadcastStringList(Messager.getStringList(
							ChatColor.translateAlternateColorCodes('&', "&c아직 모든 유저가 능력을 확정하지 않았습니다."),
							ChatColor.translateAlternateColorCodes('&', "&c/ability yes나 /ability no 명령어로 능력을 확정해주세요.")));
				}
			} else {
				this.StopTimer(false);
			}
		}

		/**
		 * 능력을 선택중인 모든 참가자가 능력을 결정했는지의 여부를 반환합니다.
		 */
		private final boolean isEveryoneSelected() {
			for(Participant Key : getSelectors()) if(!hasDecided(Key)) return false;
			return true;
		}
		
		@Override
		public void onEnd() {
			Ended = true;
			onSelectEnd();
		}

		protected abstract void onSelectEnd();

		private boolean Ended = false;
		
		public boolean isEnded() {
			return Ended;
		}
		
	}

}
