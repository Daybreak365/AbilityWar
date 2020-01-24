package daybreak.abilitywar.game.games.mode;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityBase.ClickType;
import daybreak.abilitywar.game.events.participant.ParticipantAbilitySetEvent;
import daybreak.abilitywar.game.manager.object.CommandHandler;
import daybreak.abilitywar.game.manager.object.DeathManager;
import daybreak.abilitywar.game.manager.object.EffectManager;
import daybreak.abilitywar.game.manager.passivemanager.PassiveManager;
import daybreak.abilitywar.utils.thread.OverallTimer;
import daybreak.abilitywar.utils.versioncompat.VersionUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static daybreak.abilitywar.utils.base.Precondition.checkNotNull;

public abstract class AbstractGame extends OverallTimer implements Listener, EffectManager.Handler, CommandHandler {

	public interface Observer {
		void update(GAME_UPDATE update);
	}

	public enum GAME_UPDATE {START, END}

	private final ArrayList<Observer> observers = new ArrayList<>();

	/**
	 * 게임이 종료될 때 등록 해제되어야 하는 {@link Listener}를 등록합니다.
	 */
	public final void attachObserver(Observer observer) {
		if (!observers.contains(checkNotNull(observer))) {
			observers.add(observer);
		}
	}

	private boolean restricted = true;
	private boolean gameStarted = false;

	private ParticipantStrategy participantStrategy;
	private final PassiveManager passiveManager = new PassiveManager(this);
	private final EffectManager effectManager = new EffectManager(this);

	public AbstractGame(Collection<Player> players) {
		this.participantStrategy = new ParticipantStrategy.DEFAULT_MANAGEMENT(this, players);
	}

	public void setParticipantStrategy(ParticipantStrategy participantStrategy) {
		this.participantStrategy = checkNotNull(participantStrategy);
	}

	/**
	 * PassiveManager을 반환합니다.
	 * <p>
	 * null을 반환하지 않습니다.
	 */
	public PassiveManager getPassiveManager() {
		return passiveManager;
	}

	/**
	 * EffectManager를 반환합니다.
	 * <p>
	 * null을 반환하지 않습니다.
	 */
	public EffectManager getEffectManager() {
		return effectManager;
	}

	/**
	 * 참여자 목록을 반환합니다.
	 *
	 * @return 참여자 목록
	 */
	public Collection<Participant> getParticipants() {
		return participantStrategy.getParticipants();
	}

	/**
	 * {@link Player}를 기반으로 하는 {@link Participant}를 탐색합니다.
	 *
	 * @param player 탐색할 플레이어
	 * @return 존재할 경우 {@link Participant}를 반환합니다. 존재하지 않을 경우 null을 반환합니다.
	 * null을 반환할 수 있습니다.
	 */
	public final Participant getParticipant(Player player) {
		return participantStrategy.getParticipant(player.getUniqueId());
	}

	/**
	 * 해당 {@link UUID}를 가지고 있는 {@link Player}를 기반으로 하는 {@link Participant}를 탐색합니다.
	 *
	 * @param uuid 탐색할 플레이어의 UUID
	 * @return 존재할 경우 {@link Participant}를 반환합니다. 존재하지 않을 경우 null을 반환합니다.
	 * null을 반환할 수 있습니다.
	 */
	public final Participant getParticipant(UUID uuid) {
		return participantStrategy.getParticipant(uuid);
	}

	/**
	 * 대상 플레이어의 참여 여부를 반환합니다.
	 *
	 * @param player 대상 플레이어
	 * @return 대상 플레이어의 참여 여부
	 */
	public boolean isParticipating(Player player) {
		return participantStrategy.isParticipating(player.getUniqueId());
	}

	/**
	 * 대상 플레이어의 참여 여부를 반환합니다.
	 *
	 * @param uuid 대상 플레이어의 UniqueId
	 * @return 대상 플레이어의 참여 여부
	 */
	public boolean isParticipating(UUID uuid) {
		return participantStrategy.isParticipating(uuid);
	}

	public void addParticipant(Player player) {
		participantStrategy.addParticipant(player);
	}

	public boolean isRestricted() {
		return restricted;
	}

	public void setRestricted(boolean restricted) {
		this.restricted = restricted;
		for (Participant participant : getParticipants()) {
			if (participant.hasAbility()) {
				participant.ability.setRestricted(restricted);
			}
		}
	}

	public boolean isGameStarted() {
		return gameStarted;
	}

	protected void startGame() {
		this.gameStarted = true;
		observers.forEach(observer -> observer.update(GAME_UPDATE.START));
	}

	@Override
	protected void onEnd() {
		stopTimers();
		HandlerList.unregisterAll(this);
		observers.forEach(observer -> observer.update(GAME_UPDATE.END));
	}

	public class Participant implements Listener, Observer {

		private final Attributes attributes = new Attributes();
		private Player player;

		protected Participant(Player player) {
			this.player = player;
			attachObserver(this);
			Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		}

		@Override
		public void update(GAME_UPDATE update) {
			if (update.equals(GAME_UPDATE.END)) {
				HandlerList.unregisterAll(this);
			}
		}

		private long lastClick = System.currentTimeMillis();

		@EventHandler
		private void onPlayerLogin(PlayerLoginEvent e) {
			if (e.getPlayer().getUniqueId().equals(player.getUniqueId())) {
				this.player = e.getPlayer();
			}
		}

		@EventHandler
		private void onPlayerInteract(PlayerInteractEvent e) {
			Player p = e.getPlayer();
			if (p.equals(getPlayer())) {
				Material material = VersionUtil.getItemInHand(p).getType();
				ClickType clickType = e.getAction().equals(Action.RIGHT_CLICK_AIR)
						|| e.getAction().equals(Action.RIGHT_CLICK_BLOCK) ? ClickType.RIGHT_CLICK
						: ClickType.LEFT_CLICK;
				if (attributes.SKILL_MATERIALS.set.contains(material)) {
					if (hasAbility()) {
						AbilityBase ability = getAbility();
						if (!ability.isRestricted()) {
							long current = System.currentTimeMillis();
							if (current - lastClick >= 250) {
								this.lastClick = current;
								if (ability.ActiveSkill(material, clickType)) {
									ability.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&d능력을 사용하였습니다."));
								}
							}
						}
					}
				}
			}
		}

		@EventHandler
		private void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
			Player p = e.getPlayer();
			if (p.equals(getPlayer()) && !e.isCancelled() && hasAbility()) {
				Material material = VersionUtil.getItemInHand(p).getType();
				if (attributes.SKILL_MATERIALS.set.contains(material)) {
					AbilityBase ability = this.getAbility();
					if (!ability.isRestricted()) {
						long current = System.currentTimeMillis();
						if (current - lastClick >= 250) {
							if (ability.ActiveSkill(material, ClickType.RIGHT_CLICK)) {
								ability.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&d능력을 사용하였습니다."));
							}
							Entity targetEntity = e.getRightClicked();
							if (targetEntity instanceof LivingEntity) {
								if (targetEntity instanceof Player) {
									Player targetPlayer = (Player) targetEntity;
									if (isParticipating(targetPlayer) && (!(this instanceof DeathManager.Handler) || !((DeathManager.Handler) this).getDeathManager().isDead(targetPlayer))) {
										this.lastClick = current;
										ability.TargetSkill(material, targetPlayer);
									}
								} else {
									LivingEntity target = (LivingEntity) targetEntity;
									this.lastClick = current;
									ability.TargetSkill(material, target);
								}
							}
						}
					}
				}
			}
		}

		private AbilityBase ability;


		/**
		 * 플레이어에게 새 능력을 부여합니다.
		 *
		 * @param abilityClass 부여할 능력의 클래스
		 */
		public void setAbility(Class<? extends AbilityBase> abilityClass)
				throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
				IllegalArgumentException, InvocationTargetException {
			AbilityBase oldAbility = null;
			if (hasAbility())
				oldAbility = removeAbility();

			Constructor<? extends AbilityBase> constructor = abilityClass.getConstructor(Participant.class);
			AbilityBase ability = constructor.newInstance(this);
			ability.setRestricted(isRestricted() || !isGameStarted());
			this.ability = ability;
			Bukkit.getPluginManager().callEvent(new ParticipantAbilitySetEvent(this, oldAbility, ability));
		}

		/**
		 * 플레이어에게 해당 능력을 그대로 적용합니다.
		 * @param ability 	부여할 능력
		 */
		public void setAbility(AbilityBase ability) {
			AbilityBase oldAbility = null;
			if (hasAbility())
				oldAbility = removeAbility();

			ability.setRestricted(isRestricted() || !isGameStarted());
			this.ability = ability;
			Bukkit.getPluginManager().callEvent(new ParticipantAbilitySetEvent(this, oldAbility, ability));
		}

		public boolean hasAbility() {
			return ability != null;
		}

		public AbilityBase getAbility() {
			return ability;
		}

		/**
		 * 참가자의 능력을 제거합니다.
		 *
		 * @return 제거된 능력
		 */
		public AbilityBase removeAbility() {
			AbilityBase ability = getAbility();
			if (ability != null) {
				ability.destroy();
				this.ability = null;
			}
			return ability;
		}

		public Player getPlayer() {
			return player;
		}

		public Attributes attributes() {
			return attributes;
		}

		public class Attributes {
			public final Attribute<Boolean> TEAM_CHAT = new Attribute<>(false);
			public final Attribute<Boolean> TARGETABLE = new Attribute<>(true);
			public final SetAttribute<Material> SKILL_MATERIALS = new SetAttribute<>(Material.IRON_INGOT, Material.GOLD_INGOT);
		}

		public class Attribute<T> {

			private final T defaultValue;
			private T value;

			private Attribute(T defaultValue) {
				this.defaultValue = defaultValue;
				this.value = defaultValue;
			}

			public T getDefaultValue() {
				return defaultValue;
			}

			public T getValue() {
				return value;
			}

			public T setValue(T value) {
				T origin = this.value;
				this.value = value;
				return origin;
			}

		}

		public class SetAttribute<E> {

			private HashSet<E> set;

			@SafeVarargs
			private SetAttribute(E... defaultElements) {
				this.set = new HashSet<>();
				Collections.addAll(set, defaultElements);
			}

			public Set<E> getView() {
				return Collections.unmodifiableSet(set);
			}

			public void addValue(E element) {
				set.add(element);
			}

			public void removeValue(E element) {
				set.remove(element);
			}

		}

	}

	private final ArrayList<TimerBase> timerTasks = new ArrayList<>();

	/**
	 * 현재 실행중인 모든 {@link TimerBase}를 반환합니다.
	 */
	public Collection<TimerBase> getTimers() {
		return Collections.unmodifiableList(new ArrayList<>(timerTasks));
	}

	/**
	 * 해당 타입의 {@link TimerBase}를 모두 종료합니다.
	 *
	 * @param timerType 종료할 타이머 타입
	 */
	public void stopTimers(Class<? extends TimerBase> timerType) {
		for (TimerBase timer : getTimers()) {
			if (timerType.isAssignableFrom(timer.getClass())) {
				timer.stopTimer(false);
			}
		}
	}

	/**
	 * 현재 실행중인 {@link TimerBase}를 모두 종료합니다.
	 */
	public void stopTimers() {
		for (TimerBase timer : getTimers()) {
			timer.stopTimer(true);
		}
		timerTasks.clear();
	}

	public abstract class TimerBase {

		private int task = -1;

		private boolean isInfinite;

		private int maxCount;
		private int count;
		private int period = 20;

		/**
		 * {@link TimerBase}가 실행될 때 호출됩니다.
		 */
		protected void onStart() {
		}

		/**
		 * {@link TimerBase} 실행 이후 {@link #period}틱마다 호출됩니다.
		 * <pre>
		 * 일반 타이머
		 * <pre>
		 * 카운트 값이 {@link #maxCount}에서 시작하여 1까지 감소합니다.</pre>
		 * 무한 타이머
		 * <pre>
		 * 카운트 값이 1에서 시작하여 {@link Integer#MAX_VALUE}까지 증가합니다.</pre>
		 * </pre>
		 */
		protected abstract void onProcess(int count);

		/**
		 * {@link TimerBase}가 종료될 때 호출됩니다.
		 */
		protected void onEnd() {
		}

		/**
		 * {@link TimerBase}가 Silent로 종료될 때 호출됩니다.
		 */
		protected void onSilentEnd() {
		}

		/**
		 * {@link TimerBase}의 실행 여부를 반환합니다.
		 */
		public final boolean isRunning() {
			return task != -1;
		}

		/**
		 * {@link TimerBase}를 실행합니다.
		 */
		public final void startTimer() {
			if (AbstractGame.this.isRunning() && !isRunning()) {
				count = maxCount;
				this.task = Bukkit.getScheduler().scheduleSyncRepeatingTask(AbilityWar.getPlugin(), new TimerTask(), 0, period);
				timerTasks.add(this);
				onStart();
			}
		}

		/**
		 * {@link TimerBase}를 종료합니다.<p>
		 */
		public final void stopTimer(boolean silent) {
			if (isRunning()) {
				Bukkit.getScheduler().cancelTask(task);
				timerTasks.remove(this);
				this.task = -1;
				if (!silent) {
					onEnd();
				} else {
					onSilentEnd();
				}
			}
		}

		/**
		 * @return 타이머를 초기화할 때 설정된 Max Count를 반환합니다.
		 */
		public final int getMaxCount() {
			return maxCount;
		}

		/**
		 * @return 남은 Count를 반환합니다.
		 */
		public final int getCount() {
			return count;
		}

		protected synchronized void setCount(int count) {
			this.count = count;
		}

		/**
		 * @return 타이머가 무한 타이머인지의 여부를 반환합니다.
		 */
		public final boolean isInfinite() {
			return isInfinite;
		}

		/**
		 * @return Period에 따라 변할 수 있는 실행 주기를 계산하여 Count를 반환합니다.
		 * 1틱마다 실행되는 타이머가 count 20만큼 남았을 때 1을 반환합니다.
		 */
		public final int getFixedCount() {
			return count / (20 / period);
		}

		public TimerBase setPeriod(int period) {
			this.period = period;
			return this;
		}

		/**
		 * maxCount 이후 종료되는 일반 {@link TimerBase}를 만듭니다.
		 */
		public TimerBase(int maxCount) {
			isInfinite = false;
			this.maxCount = maxCount;
		}

		/**
		 * 종료되지 않는 무한 {@link TimerBase}를 만듭니다.
		 */
		public TimerBase() {
			isInfinite = true;
			this.maxCount = 1;
		}

		private final class TimerTask extends Thread {
			@Override
			public void run() {
				if (isInfinite) {
					onProcess(count);
					count++;
				} else {
					if (count > 0) {
						onProcess(count);
						count--;
					} else {
						stopTimer(false);
					}
				}
			}
		}

	}

}
