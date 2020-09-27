package daybreak.abilitywar.ability;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.event.AbilityDestroyEvent;
import daybreak.abilitywar.ability.event.AbilityEvent;
import daybreak.abilitywar.ability.event.AbilityRestrictionEvent;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.ability.AbilitySettings;
import daybreak.abilitywar.config.enums.CooldownDecrease;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.GameTimer;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.event.participant.ParticipantEvent;
import daybreak.abilitywar.game.list.changeability.ChangeAbilityWar;
import daybreak.abilitywar.game.list.standard.StandardGame;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.game.module.Wreck;
import daybreak.abilitywar.game.module.EventManager;
import daybreak.abilitywar.game.module.EventManager.EventObserver;
import daybreak.abilitywar.utils.base.BracketReplacer;
import daybreak.abilitywar.utils.base.TimeUtil;
import daybreak.abilitywar.utils.base.collect.Pair;
import daybreak.abilitywar.utils.base.collect.QueueOnIterateHashSet;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer.Observer;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.io.FileUtil;
import daybreak.abilitywar.utils.base.logging.Logger;
import daybreak.abilitywar.utils.base.math.FastMath;
import daybreak.abilitywar.utils.base.reflect.ReflectionUtil;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;

/**
 * {@link AbilityWar} 플러그인에서 사용하는 <strong>모든 능력</strong>의 기반이 되는 클래스입니다.
 * <p>
 * 만들어진 <strong>모든 능력은 반드시 {@link AbilityFactory}에 등록되어야 합니다.</strong>
 * <p>
 * <ul>
 * {@link AbilityFactory#registerAbility}
 * </ul>
 * {@link StandardGame}, {@link ChangeAbilityWar} 등에서 사용할 능력은 추가적으로
 * {@link AbilityList}에 등록해야 합니다.
 * <p>
 * <ul>
 * {@link AbilityList#registerAbility}
 * </ul>
 *
 * @author Daybreak 새벽
 */
public abstract class AbilityBase {

	private static final Logger logger = Logger.getLogger(AbilityBase.class);

	public static final AbilitySettings abilitySettings = new AbilitySettings(FileUtil.newFile("abilitysettings.yml"));
	public static final BracketReplacer SQUARE_BRACKET = new BracketReplacer('[', ']'), ROUND_BRACKET = new BracketReplacer('(', ')');

	public static Iterator<String> getExplanation(final AbilityRegistration registration) {
		return new Iterator<String>() {
			private final Function<String, String> valueProvider = new Function<String, String>() {
				@Override
				public String apply(String string) {
					try {
						final Field field = registration.getAbilityClass().getDeclaredField(string);
						if (Modifier.isStatic(field.getModifiers())) {
							try {
								return String.valueOf(ReflectionUtil.setAccessible(field).get(null));
							} catch (IllegalAccessException ignored) {
							}
						}
					} catch (NoSuchFieldException ignored) {
					}
					return "?";
				}
			};
			private final String[] explanation = registration.getManifest().explain();
			private int cursor = 0;

			@Override
			public boolean hasNext() {
				return cursor < explanation.length;
			}

			@Override
			public String next() {
				final int index = cursor;
				cursor++;
				final String line = explanation[index];
				if (registration.getExplain().needsReplace(index)) {
					return ROUND_BRACKET.replaceAll(SQUARE_BRACKET.replaceAll(line, valueProvider), valueProvider);
				} else {
					return line;
				}
			}
		};
	}

	public static <T extends AbilityBase> T create(final Class<T> abilityClass, final Participant participant) throws IllegalAccessException, InvocationTargetException, InstantiationException {
		Preconditions.checkNotNull(abilityClass);
		Preconditions.checkNotNull(participant);
		if (!AbilityFactory.isRegistered(abilityClass)) throw new IllegalArgumentException(abilityClass.getSimpleName() + " 능력은 AbilityFactory에 등록되지 않은 능력입니다.");
		return abilityClass.cast(AbilityFactory.getRegistration(abilityClass).getConstructor().newInstance(participant));
	}

	public static AbilityBase create(final AbilityRegistration registration, final Participant participant) throws IllegalAccessException, InvocationTargetException, InstantiationException {
		Preconditions.checkNotNull(registration);
		Preconditions.checkNotNull(participant);
		return registration.getConstructor().newInstance(participant);
	}

	private final Function<String, String> fieldValueProvider = new Function<String, String>() {
		@Override
		public String apply(String string) {
			try {
				final Field field = AbilityBase.this.getClass().getDeclaredField(string);
				if (Modifier.isStatic(field.getModifiers())) {
					try {
						return String.valueOf(ReflectionUtil.setAccessible(field).get(null));
					} catch (IllegalAccessException ignored) {
					}
				} else {
					try {
						return String.valueOf(ReflectionUtil.setAccessible(field).get(AbilityBase.this));
					} catch (IllegalAccessException ignored) {
					}
				}
			} catch (NoSuchFieldException ignored) {
			}
			return "?";
		}
	};

	private final Participant participant;
	private final AbstractGame game;
	private final AbilityRegistration registration;
	private final AbilityManifest manifest;
	private String[] explanation = null;
	private final Multimap<Class<? extends Event>, EventObserver> eventhandlers = HashMultimap.create();
	private final Set<AbilityTimer> timers = new QueueOnIterateHashSet<>(), runningTimers = new QueueOnIterateHashSet<>();
	private final Queue<AbilityTimer> pausedTimers = new LinkedList<>();
	private final List<ActionbarChannel> actionbarChannels = new LinkedList<>();
	private final Restriction restriction;
	private boolean destroyed;

	/**
	 * {@link AbilityBase}의 기본 생성자입니다.
	 *
	 * @param participant 능력을 소유하는 참가자
	 * @throws IllegalStateException 능력이 {@link AbilityFactory}에 등록되지 않았을 경우 예외가 발생합니다.
	 */
	protected AbilityBase(final Participant participant) throws IllegalStateException {
		this.participant = participant;
		this.game = participant.getGame();
		if (!game.isRunning()) throw new IllegalStateException("게임이 진행 되고 있지 않습니다.");
		if (!AbilityFactory.isRegistered(getClass())) throw new IllegalStateException("AbilityFactory에 등록되지 않은 능력입니다.");
		this.registration = AbilityFactory.getRegistration(getClass());
		this.manifest = registration.getManifest();
		final EventManager eventManager = game.getEventManager();
		for (final Entry<Class<? extends Event>, Pair<Method, SubscribeEvent>> entry : registration.getEventhandlers().entries()) {
			final Pair<Method, SubscribeEvent> pair = entry.getValue();
			final SubscribeEvent subscriber = pair.getRight();
			final Method method = pair.getLeft();
			final EventObserver observer = new EventObserver(entry.getKey(), subscriber.priority()) {
				@Override
				protected void onEvent(final Event event) {
					if (isRestricted()) return;
					if (subscriber.onlyRelevant()
							&& ((event instanceof AbilityEvent && !AbilityBase.this.equals(((AbilityEvent) event).getAbility()))
							|| (event instanceof ParticipantEvent && !getParticipant().equals(((ParticipantEvent) event).getParticipant()))
							|| (event instanceof PlayerEvent && !getPlayer().equals(((PlayerEvent) event).getPlayer()))
							|| (event instanceof EntityEvent && !getPlayer().equals(((EntityEvent) event).getEntity())))) {
						return;
					}
					if (subscriber.ignoreCancelled() && event instanceof Cancellable && ((Cancellable) event).isCancelled()) return;
					try {
						ReflectionUtil.setAccessible(method).invoke(AbilityBase.this, event);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
						logger.log(Level.SEVERE, method.getDeclaringClass().getName() + ":" + method.getName() + "를 호출하는 도중 오류가 발생하였습니다.", ex);
					}
				}
			};
			eventhandlers.put(entry.getKey(), observer);
			eventManager.register(observer);
		}
		this.restriction = new Restriction();
	}

	/**
	 *
	 * @param clazz				이벤트 클래스
	 * @param onlyRelevant		이 능력 또는 이 능력의 주인과 관련 없는 이벤트 무시 여부
	 * @param ignoreCancelled	취소된 이벤트 무시 여부
	 * @param priority			이벤트 우선 순위
	 */
	protected <T extends Event> void subscribeEvent(final Class<T> clazz, final EventConsumer<T> consumer, final boolean onlyRelevant, final boolean ignoreCancelled, final int priority) {
		Preconditions.checkNotNull(clazz);
		Preconditions.checkNotNull(consumer);
		final EventObserver observer = new EventObserver(clazz, priority) {
			@Override
			protected void onEvent(final Event event) {
				if (isRestricted()) return;
				if (onlyRelevant
						&& ((event instanceof AbilityEvent && !AbilityBase.this.equals(((AbilityEvent) event).getAbility()))
						|| (event instanceof ParticipantEvent && !getParticipant().equals(((ParticipantEvent) event).getParticipant()))
						|| (event instanceof PlayerEvent && !getPlayer().equals(((PlayerEvent) event).getPlayer()))
						|| (event instanceof EntityEvent && !getPlayer().equals(((EntityEvent) event).getEntity())))) {
					return;
				}
				if (ignoreCancelled && event instanceof Cancellable && ((Cancellable) event).isCancelled()) return;
				try {
					consumer.onEvent(clazz.cast(event));
				} catch (Exception ex) {
					logger.log(Level.SEVERE, AbilityBase.this.getClass().getName() + ":" + consumer.toString() + "를 호출하는 도중 오류가 발생하였습니다.", ex);
				}
			}
		};
		eventhandlers.put(clazz, observer);
		game.getEventManager().register(observer);
	}

	public interface EventConsumer<T> {
		void onEvent(final T event);
	}

	public enum Update {
		RESTRICTION_SET,
		RESTRICTION_CLEAR,
		ABILITY_DESTROY
	}

	protected void onUpdate(Update update) {
	}

	/**
	 * 더 이상 사용되지 않는 {@link AbilityBase}를 제거할 때 사용됩니다.
	 * <p>
	 * {@link Participant#removeAbility()}를 통해 {@link Participant}의 능력을 제거할 때 호출됩니다.
	 * 절대 임의로 호출하지 마십시오.
	 */
	public final void destroy() {
		this.destroyed = true;
		this.restriction.isRestricted = true;
		onUpdate(Update.ABILITY_DESTROY);
		Bukkit.getPluginManager().callEvent(new AbilityDestroyEvent(this));
		for (EventObserver eventObserver : eventhandlers.values()) {
			game.getEventManager().unregister(eventObserver);
		}
		for (AbilityTimer abilityTimer : runningTimers) {
			abilityTimer.stop(true);
		}
		for (GameTimer timer : pausedTimers) {
			timer.stop(true);
		}
		for (ActionbarChannel channel : actionbarChannels) {
			channel.unregister();
		}
	}

	@NotNull
	public final AbilityRegistration getRegistration() {
		return registration;
	}

	/**
	 * 능력을 소유하는 플레이어를 반환합니다.
	 */
	@NotNull
	public final Player getPlayer() {
		return participant.getPlayer();
	}

	/**
	 * 능력을 소유하는 참가자를 반환합니다.
	 */
	@NotNull
	public final Participant getParticipant() {
		return participant;
	}

	/**
	 * 능력의 설명을 반환합니다.
	 */
	public final Iterator<String> getExplanation() {
		if (explanation == null) {
			this.explanation = new String[manifest.explain().length];
			System.arraycopy(manifest.explain(), 0, explanation, 0, explanation.length);
			for (int i = 0; i < explanation.length; i++) {
				explanation[i] = SQUARE_BRACKET.replaceAll(explanation[i], fieldValueProvider);
			}
		}

		return new Iterator<String>() {
			private int cursor = 0;

			@Override
			public boolean hasNext() {
				return cursor < explanation.length;
			}

			@Override
			public String next() {
				return ROUND_BRACKET.replaceAll(explanation[cursor++], fieldValueProvider);
			}
		};
	}

	/**
	 * 능력의 이름을 반환합니다.
	 */
	@NotNull
	public final String getName() {
		return manifest.name();
	}

	/**
	 * 능력의 표시 이름을 반환합니다.
	 */
	@NotNull
	public String getDisplayName() {
		return manifest.name();
	}

	/**
	 * 능력의 등급을 반환합니다.
	 */
	@NotNull
	public final Rank getRank() {
		return manifest.rank();
	}

	/**
	 * 능력의 종족을 반환합니다.
	 */
	@NotNull
	public final Species getSpecies() {
		return manifest.species();
	}

	@NotNull
	public final AbilityManifest getManifest() {
		return manifest;
	}

	/**
	 * 이 능력이 사용되는 게임을 반환합니다.
	 */
	@NotNull
	protected final AbstractGame getGame() {
		return game;
	}

	/**
	 * 능력의 제한 여부를 반환합니다.
	 */
	public final boolean isRestricted() {
		return restriction.updateState();
	}

	/**
	 * 능력의 제한 여부를 설정합니다.
	 */
	public final void setRestricted(final boolean restricted) {
		restriction.setRestricted(restricted);
	}

	public final Restriction getRestriction() {
		return restriction;
	}

	public Set<GameTimer> getTimers() {
		return Collections.unmodifiableSet(timers);
	}

	public Set<GameTimer> getRunningTimers() {
		return Collections.unmodifiableSet(runningTimers);
	}

	public boolean usesMaterial(Material material) {
		return registration.getMaterials().contains(material);
	}

	public final ActionbarChannel newActionbarChannel() {
		if (destroyed) throw new IllegalStateException();
		final ActionbarChannel channel = participant.actionbar().newChannel();
		actionbarChannels.add(channel);
		return channel;
	}

	public enum ClickType {LEFT_CLICK, RIGHT_CLICK}

	public enum RestrictionBehavior {STOP, PAUSE_RESUME}

	public abstract class AbilityTimer extends GameTimer {

		@NotNull
		private RestrictionBehavior behavior = RestrictionBehavior.STOP;

		public AbilityTimer(final @NotNull TaskType taskType, final int maximumCount) {
			game.super(taskType, maximumCount);
			attachObserver(new SimpleTimer.Observer() {
				@Override
				public void onStart() {
					runningTimers.add(AbilityTimer.this);
				}

				@Override
				public void onEnd() {
					runningTimers.remove(AbilityTimer.this);
				}

				@Override
				public void onSilentEnd() {
					runningTimers.remove(AbilityTimer.this);
				}

				@Override
				public void onResume() {
					runningTimers.add(AbilityTimer.this);
				}

				@Override
				public void onPause() {
					runningTimers.remove(AbilityTimer.this);
				}
			});
		}

		public AbilityTimer(int maximumCount) {
			this(TaskType.REVERSE, maximumCount);
		}

		public AbilityTimer() {
			this(TaskType.INFINITE, -1);
		}

		@NotNull
		@Override
		public AbilityTimer setPeriod(@NotNull TimeUnit timeUnit, int period) {
			super.setPeriod(timeUnit, period);
			return this;
		}

		@NotNull
		@Override
		public AbilityTimer setInitialDelay(@NotNull TimeUnit timeUnit, int initialDelay) {
			super.setInitialDelay(timeUnit, initialDelay);
			return this;
		}

		public AbilityTimer setBehavior(RestrictionBehavior behavior) {
			this.behavior = Preconditions.checkNotNull(behavior);
			return this;
		}

		public RestrictionBehavior getBehavior() {
			return behavior;
		}

		public AbilityTimer register() {
			timers.add(this);
			return this;
		}

		public AbilityTimer unregister() {
			timers.remove(this);
			return this;
		}

		@Override
		public boolean start() {
			if (!destroyed) {
				return super.start();
			} else {
				return false;
			}
		}

		@Override
		public boolean resume() {
			if (!destroyed) {
				return super.resume();
			} else {
				return false;
			}
		}

	}

	/**
	 * 쿨타임 관리 클래스
	 * 타이머를 내부적으로 관리합니다.
	 *
	 * @author Daybreak 새벽
	 */
	public class Cooldown {

		private int cooldown;
		private final String name;
		private CooldownTimer timer;

		public Cooldown(final int cooldown, final String name, final int maxDecrease) {
			this.cooldown = (int) (Wreck.isEnabled(getGame()) ? cooldown * Wreck.calculateDecreasedAmount(Math.min(100, Math.max(0, maxDecrease))) : cooldown);
			this.timer = new CooldownTimer(this.cooldown);
			this.name = name;
		}

		public Cooldown(final int cooldown, final String name, final CooldownDecrease maxDecrease) {
			this(cooldown, name, maxDecrease.getPercentage());
		}

		public Cooldown(final int cooldown, final String name) {
			this(cooldown, name, CooldownDecrease._100);
		}

		public Cooldown(final int cooldown, final int maxDecrease) {
			this(cooldown, "", maxDecrease);
		}

		public Cooldown(final int cooldown, final CooldownDecrease maxDecrease) {
			this(cooldown, "", maxDecrease);
		}

		public Cooldown(final int cooldown) {
			this(cooldown, "");
		}

		public boolean start() {
			return timer.start();
		}

		public boolean stop(boolean silent) {
			return timer.stop(silent);
		}

		public boolean isRunning() {
			return timer.isRunning();
		}

		public int getCooldown() {
			return cooldown;
		}

		public boolean isCooldown() {
			if (timer.isRunning()) {
				if (getPlayer() != null) {
					getPlayer().sendMessage(toString(ChatColor.WHITE));
				}
			}
			return timer.isRunning();
		}

		public int getCount() {
			return timer.getCount();
		}

		public int getMaximumCount() {
			return timer.getMaximumCount();
		}

		public void setCount(int count) {
			timer.setCount(count);
		}

		public void setCooldown(int cooldown, final int maxDecrease) {
			timer.actionbarChannel.unregister();
			timer.stop(true);
			timer.unregister();
			this.cooldown = (int) (Wreck.isEnabled(getGame()) ? cooldown * Wreck.calculateDecreasedAmount(Math.min(100, Math.max(0, maxDecrease))) : cooldown);
			this.timer = new CooldownTimer(this.cooldown);
		}

		public void setCooldown(int cooldown, CooldownDecrease maxDecrease) {
			this.setCooldown(cooldown, maxDecrease.getPercentage());
		}

		public void setCooldown(int cooldown) {
			this.setCooldown(cooldown, 100);
		}

		@Override
		public String toString() {
			return toString(ChatColor.GOLD);
		}

		public String toString(ChatColor timeColor) {
			if (name != null && !name.isEmpty()) {
				return ChatColor.RED.toString() + name + " 쿨타임 " + ChatColor.WHITE.toString() + ": " + timeColor.toString() + TimeUtil.parseTimeAsString(timer.getCount());
			} else {
				return ChatColor.RED.toString() + "쿨타임 " + ChatColor.WHITE.toString() + ": " + timeColor.toString() + TimeUtil.parseTimeAsString(timer.getCount());
			}
		}

		public Cooldown attachObserver(final SimpleTimer.Observer observer) {
			timer.attachObserver(observer);
			return this;
		}

		public Cooldown detachObserver(final SimpleTimer.Observer observer) {
			timer.detachObserver(observer);
			return this;
		}

		public class CooldownTimer extends AbilityTimer {

			private final ActionbarChannel actionbarChannel = newActionbarChannel();

			public CooldownTimer(int cooldown) {
				super(TaskType.REVERSE, cooldown);
				setBehavior(RestrictionBehavior.PAUSE_RESUME);
				register();
			}

			@Override
			public void run(int count) {
				actionbarChannel.update(toString());
				if (count == (getMaximumCount() / 2) || (count <= 5 && count >= 1)) {
					SoundLib.BLOCK_NOTE_BLOCK_HAT.playSound(getPlayer());
					getPlayer().sendMessage(toString(ChatColor.WHITE));
				}
			}

			@Override
			public void onEnd() {
				getPlayer().sendMessage("§a능력을 다시 사용할 수 있습니다.");
				actionbarChannel.update("§a능력을 다시 사용할 수 있습니다.", 2);
			}

			@Override
			public void onSilentEnd() {
				actionbarChannel.update(null);
			}

			@Override
			public void onCountSet() {
				actionbarChannel.update(toString());
			}

			@Override
			public String toString() {
				return toString(ChatColor.GOLD);
			}

			private String toString(ChatColor timeColor) {
				if (name != null && !name.isEmpty()) {
					return ChatColor.RED.toString() + name + " 쿨타임 " + ChatColor.WHITE.toString() + ": " + timeColor.toString() + TimeUtil.parseTimeAsString(getCount());
				} else {
					return ChatColor.RED.toString() + "쿨타임 " + ChatColor.WHITE.toString() + ": " + timeColor.toString() + TimeUtil.parseTimeAsString(getCount());
				}
			}

			@NotNull
			@Override
			public CooldownTimer setPeriod(@NotNull TimeUnit timeUnit, int period) {
				return this;
			}

			@NotNull
			@Override
			public CooldownTimer setInitialDelay(@NotNull TimeUnit timeUnit, int initialDelay) {
				return this;
			}

		}

	}

	/**
	 * 지속 타이머
	 *
	 * @author Daybreak 새벽
	 */
	public abstract class Duration {

		private final int duration;
		private int period = 20;
		private final String name;
		private final Cooldown cooldown;
		private DurationTimer timer;
		private final Set<SimpleTimer.Observer> observers = new HashSet<>(0);

		public Duration(final int duration, final Cooldown cooldown, final String name) {
			this.duration = duration;
			this.name = Strings.nullToEmpty(name);
			this.cooldown = cooldown;
			this.timer = new DurationTimer(this.duration, FastMath.gcd(this.period, 20));
		}

		public Duration(int duration, Cooldown cooldown) {
			this(duration, cooldown, "");
		}

		public Duration(int duration) {
			this(duration, null);
		}

		protected void onDurationStart() {}
		protected abstract void onDurationProcess(int count);
		protected void onDurationEnd() {}
		protected void onDurationSilentEnd() {}

		public final boolean isDuration() {
			if (timer.isRunning()) {
				if (getPlayer() != null) {
					getPlayer().sendMessage(toString(ChatColor.WHITE));
				}
			}
			return timer.isRunning();
		}

		public void attachObserver(final SimpleTimer.Observer observer) {
			observers.add(observer);
			timer.attachObserver(observer);
		}

		public void detachObserver(final SimpleTimer.Observer observer) {
			observers.remove(observer);
			timer.detachObserver(observer);
		}

		@NotNull
		public Duration setPeriod(TimeUnit timeUnit, int period) {
			timer.actionbarChannel.unregister();
			timer.stop(true);
			timer.unregister();
			this.period = timeUnit.toTicks(period);
			this.timer = new DurationTimer(duration, FastMath.gcd(this.period, 20));
			for (Observer observer : observers) {
				timer.attachObserver(observer);
			}
			return this;
		}

		@NotNull
		public Duration setInitialDelay(TimeUnit timeUnit, int initialDelay) {
			timer.setInitialDelay(timeUnit, initialDelay);
			return this;
		}

		@NotNull
		public Duration setBehavior(RestrictionBehavior behavior) {
			timer.setBehavior(behavior);
			return this;
		}

		@NotNull
		public Duration unregister() {
			timer.unregister();
			return this;
		}

		public boolean start() {
			return timer.start();
		}

		public boolean stop(boolean silent) {
			return timer.stop(silent);
		}

		public boolean isRunning() {
			return timer.isRunning();
		}

		public int getMaximumCount() {
			return timer.getMaximumCount();
		}

		public int getCount() {
			return timer.getCount();
		}

		public int getFixedCount() {
			return timer.getFixedCount();
		}

		public void setCount(int count) {
			timer.setCount(count);
		}

		@Override
		public final String toString() {
			return toString(ChatColor.YELLOW);
		}

		public final String toString(ChatColor timeColor) {
			if (name != null && !name.isEmpty()) {
				return ChatColor.GOLD.toString() + name + " 지속 시간 " + ChatColor.WHITE.toString() + ": " + timeColor.toString() + TimeUtil.parseTimeAsString(timer.getFixedCount());
			} else {
				return ChatColor.GOLD.toString() + "지속 시간 " + ChatColor.WHITE.toString() + ": " + timeColor.toString() + TimeUtil.parseTimeAsString(timer.getFixedCount());
			}
		}

		public class DurationTimer extends AbilityTimer {

			private final ActionbarChannel actionbarChannel = newActionbarChannel();
			private final double cycle;

			public DurationTimer(final int maxCount, final int gcd) {
				super(TaskType.REVERSE, maxCount * (int) ((double) period / gcd));
				this.cycle = ((double) period / gcd);
				setPeriod(TimeUnit.TICKS, gcd);
				if (Settings.getDurationTimerBehavior()) {
					setBehavior(RestrictionBehavior.PAUSE_RESUME);
				}
				register();
			}

			@Override
			protected final void onStart() {
				onDurationStart();
			}

			private int tick = 0;

			@Override
			public void run(int count) {
				tick += getPeriod();
				if (tick % 20 == 0) {
					actionbarChannel.update(toString());
					final int fixedCount = getFixedCount();
					if ((fixedCount == (duration / 2) || (fixedCount <= 5 && fixedCount >= 1))) {
						SoundLib.BLOCK_NOTE_BLOCK_HAT.playSound(getPlayer());
						getPlayer().sendMessage(toString(ChatColor.WHITE));
					}
				}
				if (tick % period == 0) {
					onDurationProcess((int) Math.floor(count / cycle));
					if (tick >= 20) {
						tick = 0;
					}
				}
			}

			@Override
			protected final void onEnd() {
				onDurationEnd();

				getPlayer().sendMessage("§6지속 시간§f이 종료되었습니다.");
				actionbarChannel.update("§6지속 시간§f이 종료되었습니다.", 2);

				if (cooldown != null) {
					cooldown.start();
				}
			}

			@Override
			protected final void onSilentEnd() {
				onDurationSilentEnd();
				actionbarChannel.update(null);
			}

			@Override
			public void onCountSet() {
				actionbarChannel.update(toString());
			}

			@Override
			public final String toString() {
				return toString(ChatColor.YELLOW);
			}

			public final String toString(ChatColor timeColor) {
				if (name != null && !name.isEmpty()) {
					return ChatColor.GOLD.toString() + name + " 지속 시간 " + ChatColor.WHITE.toString() + ": " + timeColor.toString() + TimeUtil.parseTimeAsString(getFixedCount());
				} else {
					return ChatColor.GOLD.toString() + "지속 시간 " + ChatColor.WHITE.toString() + ": " + timeColor.toString() + TimeUtil.parseTimeAsString(getFixedCount());
				}
			}

		}

	}

	public final class Restriction {

		private boolean isRestricted = true;
		private final Set<Restriction.Condition> conditions = new HashSet<>();

		private Restriction() {
			conditions.add(new Condition() {
				@Override
				public boolean condition() {
					return game.isRestricted() || !game.isGameStarted() || destroyed;
				}
			});
		}

		private boolean checkConditions() {
			for (Restriction.Condition condition : conditions) {
				if (condition.condition()) {
					return true;
				}
			}
			return false;
		}

		private boolean updateState(final boolean state) {
			if (this.isRestricted == state) return this.isRestricted;
			this.isRestricted = state;
			if (state) {
				for (AbilityTimer timer : timers) {
					if (timer.getBehavior() == RestrictionBehavior.STOP) {
						timer.stop(true);
					} else {
						if (timer.pause()) {
							pausedTimers.add(timer);
						}
					}
				}
				for (AbilityTimer runningTimer : runningTimers) {
					if (runningTimer.getBehavior() == RestrictionBehavior.STOP) {
						runningTimer.stop(true);
					} else {
						if (runningTimer.pause()) {
							pausedTimers.add(runningTimer);
						}
					}
				}
				for (ActionbarChannel channel : actionbarChannels) {
					channel.update(null);
				}
				onUpdate(Update.RESTRICTION_SET);
				Bukkit.getPluginManager().callEvent(new AbilityRestrictionEvent(AbilityBase.this, true));
				return true;
			} else {
				while (!pausedTimers.isEmpty()) {
					pausedTimers.poll().resume();
				}
				onUpdate(Update.RESTRICTION_CLEAR);
				Bukkit.getPluginManager().callEvent(new AbilityRestrictionEvent(AbilityBase.this, false));
				return false;
			}
		}

		public boolean updateState() {
			if (checkConditions()) {
				return updateState(true);
			} else return isRestricted;
		}

		public void setRestricted(final boolean restricted) {
			if (restricted) updateState(true);
			else updateState(checkConditions());
		}

		public abstract class Condition {

			public abstract boolean condition();

			public final AbilityBase getAbility() {
				return AbilityBase.this;
			}

			public final Condition register() {
				if (condition()) updateState(true);
				else updateState();
				conditions.add(this);
				return this;
			}

			public final void unregister() {
				conditions.remove(this);
				if (condition()) updateState();
			}

		}

	}

}
