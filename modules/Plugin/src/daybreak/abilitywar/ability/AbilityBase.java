package daybreak.abilitywar.ability;

import com.google.common.base.Preconditions;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.event.AbilityDestroyEvent;
import daybreak.abilitywar.ability.event.AbilityEvent;
import daybreak.abilitywar.ability.event.AbilityRestrictionClearEvent;
import daybreak.abilitywar.ability.event.AbilityRestrictionSetEvent;
import daybreak.abilitywar.game.events.participant.ParticipantEvent;
import daybreak.abilitywar.game.games.changeability.ChangeAbilityWar;
import daybreak.abilitywar.game.games.mode.AbstractGame;
import daybreak.abilitywar.game.games.mode.AbstractGame.GameTimer;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.games.mode.AbstractGame.RestrictionBehavior;
import daybreak.abilitywar.game.games.standard.DefaultGame;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.game.manager.object.EventManager;
import daybreak.abilitywar.game.manager.object.WRECK;
import daybreak.abilitywar.utils.ReflectionUtil;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.base.collect.Pair;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.math.FastMath;
import daybreak.abilitywar.utils.math.NumberUtil;
import daybreak.abilitywar.utils.thread.AbilityWarThread;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.MainHand;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link AbilityWar} 플러그인에서 사용하는 <strong>모든 능력</strong>의 기반이 되는 클래스입니다.
 * <p>
 * 만들어진 <strong>모든 능력은 반드시 {@link AbilityFactory}에 등록되어야 합니다.</strong>
 * <p>
 * <ul>
 * {@link AbilityFactory#registerAbility}
 * </ul>
 * {@link DefaultGame}, {@link ChangeAbilityWar} 등에서 사용할 능력은 추가적으로
 * {@link AbilityList}에 등록해야 합니다.
 * <p>
 * <ul>
 * {@link AbilityList#registerAbility}
 * </ul>
 *
 * @author Daybreak 새벽
 */
public abstract class AbilityBase implements EventManager.Observer {

	private static final Logger logger = Logger.getLogger(AbilityBase.class.getName());

	@Beta
	public static AbilityBase create(Class<? extends AbilityBase> abilityClass, Participant participant) throws IllegalAccessException, InvocationTargetException, InstantiationException {
		Preconditions.checkNotNull(abilityClass);
		Preconditions.checkNotNull(participant);
		if (AbilityFactory.isRegistered(abilityClass)) {
			AbilityRegistration registration = AbilityFactory.getRegistration(abilityClass);
			AbilityBase abilityBase = registration.getConstructor().newInstance(participant);
			if (registration.getScheduledTimers().size() > 0) {
				Set<Field> fields = registration.getScheduledTimers();
				abilityBase.scheduledTimers = new ArrayList<>(fields.size());
				for (Field field : fields) {
					try {
						GameTimer timer = (GameTimer) ReflectionUtil.setAccessible(field).get(abilityBase);
						if (timer != null) {
							abilityBase.scheduledTimers.add(timer);
						}
					} catch (IllegalAccessException ignored) {
					}
				}
			}
			return abilityBase;
		} else {
			throw new IllegalArgumentException(abilityClass.getSimpleName() + " 능력은 AbilityFactory에 등록되지 않은 능력입니다.");
		}
	}

	private final Participant participant;
	private final DescriptionLine[] description;
	private final AbilityRegistration registration;
	private final AbilityManifest manifest;
	private final AbstractGame game;
	private final Map<Class<? extends Event>, Pair<Method, SubscribeEvent>> eventhandlers;
	private final List<GameTimer> timers = new ArrayList<>();
	private List<GameTimer> scheduledTimers = null;
	private final Set<ActionbarChannel> actionbarChannels = new HashSet<>();

	private boolean restricted;

	/**
	 * {@link AbilityBase}의 기본 생성자입니다.
	 *
	 * @param participant 능력을 소유하는 참가자
	 * @param description 능력 설명
	 * @throws IllegalStateException 게임이 진행중이지 않거나 능력이 {@link AbilityFactory}에 등록되지
	 *                               않았을 경우 예외가 발생합니다.
	 */
	public AbilityBase(Participant participant, DescriptionLine... description) throws IllegalStateException {
		this.participant = participant;
		this.description = description;
		if (!AbilityWarThread.isGameTaskRunning()) {
			throw new IllegalStateException("게임이 진행되고 있지 않습니다.");
		}
		this.game = AbilityWarThread.getGame();
		if (!AbilityFactory.isRegistered(getClass())) {
			throw new IllegalStateException("AbilityFactory에 등록되지 않은 능력입니다.");
		}
		this.registration = AbilityFactory.getRegistration(getClass());
		this.manifest = registration.getManifest();
		this.eventhandlers = registration.getEventhandlers();

		EventManager eventManager = game.getEventManager();
		for (Class<? extends Event> eventClass : eventhandlers.keySet()) {
			eventManager.register(eventClass, this);
		}

		this.restricted = game.isRestricted() || !game.isGameStarted();
	}

	/**
	 * {@link AbilityBase}의 기본 생성자입니다.
	 *
	 * @param participant 능력을 소유하는 참가자
	 * @param description 능력 설명
	 * @throws IllegalStateException 게임이 진행중이지 않거나 능력이 {@link AbilityFactory}에 등록되지
	 *                               않았을 경우 예외가 발생합니다.
	 */
	public AbilityBase(Participant participant, String... description) throws IllegalStateException {
		this(participant, new DescriptionLine(description));
	}

	@Override
	public void onEvent(Event event) {
		if (restricted) return;
		Class<? extends Event> eventClass = event.getClass();
		if (eventhandlers.containsKey(eventClass)) {
			Pair<Method, SubscribeEvent> pair = eventhandlers.get(eventClass);
			SubscribeEvent subscribeEvent = pair.getRight();
			if (subscribeEvent.onlyRelevant()
					&& ((event instanceof AbilityEvent && !equals(((AbilityEvent) event).getAbility()))
					|| (event instanceof ParticipantEvent && !getParticipant().equals(((ParticipantEvent) event).getParticipant()))
					|| (event instanceof PlayerEvent && !getPlayer().equals(((PlayerEvent) event).getPlayer()))
					|| (event instanceof EntityEvent && !getPlayer().equals(((EntityEvent) event).getEntity())))) {
				return;
			}
			if (subscribeEvent.ignoreCancelled() && event instanceof Cancellable && ((Cancellable) event).isCancelled()) {
				return;
			}
			Method method = pair.getLeft();
			try {
				ReflectionUtil.setAccessible(method).invoke(this, event);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
				logger.log(Level.SEVERE, method.getDeclaringClass().getName() + ":" + method.getName() + "를 호출하는 도중 오류가 발생하였습니다.", ex);
			}
		}
	}

	/**
	 * 액티브 스킬 발동을 위해 사용됩니다.
	 *
	 * @param materialType 플레이어가 클릭할 때 {@link MainHand}에 들고 있었던 아이템
	 * @param clickType    클릭의 종류
	 * @return 능력 발동 여부
	 */
	public abstract boolean ActiveSkill(Material materialType, ClickType clickType);

	/**
	 * 타겟팅 스킬 발동을 위해 사용됩니다.
	 *
	 * @param materialType 플레이어가 클릭할 때 {@link MainHand}에 들고 있었던 아이템
	 * @param entity       타겟팅의 대상, 타겟팅의 대상이 없을 경우 null이 될 수 있습니다. null 체크가 필요합니다.
	 */
	public abstract void TargetSkill(Material materialType, LivingEntity entity);

	/**
	 * 능력 제한이 해제될 경우 호출됩니다.
	 */
	@Deprecated
	public void onRestrictClear() {
	}

	/**
	 * 더 이상 사용되지 않는 {@link AbilityBase}를 제거할 때 사용됩니다.
	 * <p>
	 * {@link Participant#removeAbility()}를 통해 {@link Participant}의 능력을 제거할 때 호출됩니다.
	 * 절대 임의로 호출하지 마십시오.
	 */
	public final void destroy() {
		Bukkit.getPluginManager().callEvent(new AbilityDestroyEvent(this));
		game.getEventManager().unregisterAll(this);
		for (GameTimer timer : timers) {
			if (timer instanceof Timer) ((Timer) timer).destroyed = true;
			timer.stop(true);
		}
		for (ActionbarChannel channel : actionbarChannels) {
			channel.unregister();
		}
	}

	public AbilityRegistration getRegistration() {
		return registration;
	}

	/**
	 * 능력을 소유하는 플레이어를 반환합니다.
	 */
	public final Player getPlayer() {
		return participant.getPlayer();
	}

	/**
	 * 능력을 소유하는 참가자를 반환합니다.
	 */
	public final Participant getParticipant() {
		return participant;
	}

	protected final DescriptionLine getDescriptionLine(int index) {
		return description[index];
	}

	/**
	 * 능력의 설명을 반환합니다.
	 */
	public final List<String> getDescription() {
		List<String> explain = new ArrayList<>();
		for (DescriptionLine line : description) {
			Collections.addAll(explain, line.strings);
		}
		return explain;
	}

	/**
	 * 능력의 이름을 반환합니다.
	 */
	public final String getName() {
		return manifest.Name();
	}

	/**
	 * 능력의 등급을 반환합니다.
	 */
	public final Rank getRank() {
		return manifest.Rank();
	}

	/**
	 * 능력의 종족을 반환합니다.
	 */
	public final Species getSpecies() {
		return manifest.Species();
	}

	/**
	 * 이 능력이 사용되는 게임을 반환합니다.
	 */
	protected final AbstractGame getGame() {
		return game;
	}

	/**
	 * 능력의 제한 여부를 반환합니다.
	 */
	public final boolean isRestricted() {
		return restricted;
	}

	private final Queue<GameTimer> pausedTimers = new LinkedList<>();

	/**
	 * 능력의 제한 여부를 설정합니다.
	 */
	public final void setRestricted(boolean restricted) {
		this.restricted = restricted;
		if (restricted) {
			for (GameTimer timer : timers) {
				if (timer.getBehavior() == RestrictionBehavior.STOP_START) {
					timer.stop(true);
				} else {
					timer.pause();
					pausedTimers.add(timer);
				}
			}
			for (ActionbarChannel channel : actionbarChannels) {
				channel.update(null);
			}
			Bukkit.getPluginManager().callEvent(new AbilityRestrictionSetEvent(this));
		} else {
			while (!pausedTimers.isEmpty()) {
				pausedTimers.poll().resume();
			}
			if (scheduledTimers != null) {
				for (GameTimer timer : scheduledTimers) {
					timer.start();
				}
			}
			Bukkit.getPluginManager().callEvent(new AbilityRestrictionClearEvent(this));
			onRestrictClear();
		}
	}

	public final ActionbarChannel newActionbarChannel() {
		ActionbarChannel channel = participant.actionbar().newChannel();
		actionbarChannels.add(channel);
		return channel;
	}

	public static class DescriptionLine {

		private String[] strings;

		public DescriptionLine(String... strings) {
			this.strings = strings;
		}

		public void setStrings(String... strings) {
			this.strings = strings;
		}

		public String[] getStrings() {
			return strings;
		}

	}

	public enum ClickType {LEFT_CLICK, RIGHT_CLICK}

	/**
	 * 쿨타임 타이머
	 * 능력의 쿨타임을 관리하기 위해 만들어진 타이머입니다.
	 *
	 * @author Daybreak 새벽
	 */
	public final class CooldownTimer extends Timer {

		private final ActionbarChannel actionbarChannel = newActionbarChannel();
		private final String abilityName;
		private boolean sendActionbar = true;

		public CooldownTimer(int cooldown, String abilityName) {
			super(TaskType.REVERSE, (WRECK.isEnabled(AbilityWarThread.getGame()) ? (cooldown / 10) : cooldown));
			setBehavior(RestrictionBehavior.PAUSE_RESUME);
			this.abilityName = abilityName;
		}

		public CooldownTimer(int cooldown) {
			this(cooldown, "");
		}

		public boolean isCooldown() {
			if (isRunning()) {
				if (getPlayer() != null) {
					getPlayer().sendMessage(toString(ChatColor.WHITE));
				}
			}
			return isRunning();
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
			Player player = getPlayer();
			if (player != null) {
				String message = ChatColor.translateAlternateColorCodes('&', "&a능력을 다시 사용할 수 있습니다.");
				player.sendMessage(message);
				actionbarChannel.update(message, 2);
			}
		}

		@Override
		public void onSilentEnd() {
			actionbarChannel.update(null);
		}

		@Override
		public String toString() {
			return toString(ChatColor.GOLD);
		}

		public String toString(ChatColor timeColor) {
			if (!abilityName.isEmpty()) {
				return ChatColor.RED.toString() + abilityName + " 쿨타임 " + ChatColor.WHITE.toString() + ": " + timeColor.toString() + NumberUtil.parseTimeString(getCount());
			} else {
				return ChatColor.RED.toString() + "쿨타임 " + ChatColor.WHITE.toString() + ": " + timeColor.toString() + NumberUtil.parseTimeString(getCount());
			}
		}

		@Override
		public CooldownTimer setPeriod(TimeUnit timeUnit, int period) {
			return this;
		}

		@Override
		public CooldownTimer setDelay(TimeUnit timeUnit, int period) {
			return this;
		}

	}

	/**
	 * Duration Timer (지속시간 타이머)
	 * 능력의 지속시간을 관리하고 능력을 발동시키기 위해 만들어진 타이머입니다.
	 *
	 * @author Daybreak 새벽
	 */
	public abstract class DurationTimer extends Timer {

		private final ActionbarChannel actionbarChannel = newActionbarChannel();
		private final int duration;
		private final String abilityName;
		private final CooldownTimer cooldownTimer;
		private int period = 20;

		public DurationTimer(int duration, CooldownTimer cooldownTimer, String abilityName) {
			super(TaskType.REVERSE, duration);
			this.duration = duration;
			this.abilityName = abilityName;
			this.cooldownTimer = cooldownTimer;
		}

		public DurationTimer(int duration, CooldownTimer cooldownTimer) {
			this(duration, cooldownTimer, "");
		}

		public DurationTimer(int duration) {
			this(duration, null);
		}

		protected void onDurationStart() {
		}

		protected abstract void onDurationProcess(int count);

		protected void onDurationEnd() {
		}

		protected void onDurationSilentEnd() {
		}

		public final boolean isDuration() {
			if (isRunning()) {
				if (getPlayer() != null) {
					getPlayer().sendMessage(toString(ChatColor.WHITE));
				}
			}
			return isRunning();
		}

		@Override
		public DurationTimer setPeriod(TimeUnit timeUnit, int period) {
			Preconditions.checkNotNull(timeUnit);
			this.period = timeUnit.toTicks(period);
			super.setPeriod(TimeUnit.TICKS, FastMath.gcd(this.period, 20));
			return this;
		}

		@Override
		public DurationTimer setDelay(TimeUnit timeUnit, int period) {
			super.setDelay(timeUnit, period);
			return this;
		}

		@Override
		protected final void onStart() {
			onDurationStart();
		}

		private int tick = 0;

		@Override
		protected final void run(int count) {
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
				onDurationProcess(count);
				if (tick >= 20) {
					tick = 0;
				}
			}
		}

		@Override
		protected final void onEnd() {
			Player player = getPlayer();
			if (player != null) {
				onDurationEnd();

				String message = ChatColor.translateAlternateColorCodes('&', "&6지속 시간&f이 종료되었습니다.");
				player.sendMessage(message);
				actionbarChannel.update(message, 2);

				if (cooldownTimer != null) {
					cooldownTimer.start();
				}
			}
		}

		@Override
		protected final void onSilentEnd() {
			actionbarChannel.update(null);
		}

		@Override
		public final String toString() {
			return toString(ChatColor.YELLOW);
		}

		public final String toString(ChatColor timeColor) {
			if (!abilityName.isEmpty()) {
				return ChatColor.GOLD.toString() + abilityName + " 지속 시간 " + ChatColor.WHITE.toString() + ": " + timeColor.toString() + NumberUtil.parseTimeString(getFixedCount());
			} else {
				return ChatColor.GOLD.toString() + "지속 시간 " + ChatColor.WHITE.toString() + ": " + timeColor.toString() + NumberUtil.parseTimeString(getFixedCount());
			}
		}

	}

	public abstract class Timer extends GameTimer {

		private boolean destroyed;

		public Timer(TaskType taskType, int maximumCount) {
			game.super(taskType, maximumCount);
			timers.add(this);
		}

		public Timer(int maximumCount) {
			this(TaskType.REVERSE, maximumCount);
		}

		public Timer() {
			this(TaskType.INFINITE, -1);
		}

		@Override
		public Timer setPeriod(TimeUnit timeUnit, int period) {
			super.setPeriod(timeUnit, period);
			return this;
		}

		@Override
		public Timer setDelay(TimeUnit timeUnit, int period) {
			super.setDelay(timeUnit, period);
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

	}

}
