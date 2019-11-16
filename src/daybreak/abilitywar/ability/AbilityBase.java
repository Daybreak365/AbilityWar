package daybreak.abilitywar.ability;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.game.games.changeability.ChangeAbilityWar;
import daybreak.abilitywar.game.games.mode.AbstractGame;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.game.games.standard.DefaultGame;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.game.manager.object.WRECK;
import daybreak.abilitywar.game.manager.passivemanager.PassiveExecutor;
import daybreak.abilitywar.game.manager.passivemanager.PassiveManager;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.library.tItle.Actionbar;
import daybreak.abilitywar.utils.math.NumberUtil;
import daybreak.abilitywar.utils.thread.AbilityWarThread;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.MainHand;

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
public abstract class AbilityBase implements PassiveExecutor {

	private static final Logger logger = Logger.getLogger(AbilityBase.class.getName());

	private final Participant participant;
	private final String[] explain;
	private final AbilityManifest manifest;
	private final AbstractGame game;
	private final Map<Class<? extends Event>, Method> eventhandlers;
	private final HashSet<AbstractGame.TimerBase> timers = new HashSet<>();

	private boolean restricted = true;

	/**
	 * {@link AbilityBase}의 기본 생성자입니다.
	 *
	 * @param participant 능력을 소유하는 참가자
	 * @param explain     능력 설명
	 * @throws IllegalStateException 게임이 진행중이지 않거나 능력이 {@link AbilityFactory}에 등록되지
	 *                               않았을 경우 예외가 발생합니다.
	 */
	public AbilityBase(Participant participant, String... explain) throws IllegalStateException {
		this.participant = participant;
		this.explain = explain;
		if (!AbilityWarThread.isGameTaskRunning()) {
			throw new IllegalStateException("게임이 진행되고 있지 않습니다.");
		}
		this.game = AbilityWarThread.getGame();
		if (!AbilityFactory.isRegistered(getClass())) {
			throw new IllegalStateException("AbilityFactory에 등록되지 않은 능력입니다.");
		}
		AbilityRegistration<?> registry = AbilityFactory.getRegisteration(getClass());
		this.manifest = registry.getManifest();
		this.eventhandlers = registry.getEventhandlers();

		PassiveManager passiveManager = game.getPassiveManager();
		for (Class<? extends Event> eventClass : eventhandlers.keySet()) {
			passiveManager.register(eventClass, this);
		}
	}

	@Override
	public void execute(Event event) {
		if (restricted)
			return;
		Class<? extends Event> eventClass = event.getClass();
		if (eventhandlers.containsKey(eventClass)) {
			Method method = eventhandlers.get(eventClass);
			method.setAccessible(true);
			try {
				method.invoke(this, event);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
				logger.log(Level.SEVERE,
						method.getDeclaringClass().getName() + ":" + method.getName() + "를 호출하는 도중 오류가 발생하였습니다.");
			}
		}
	}

	/**
	 * 액티브 스킬 발동을 위해 사용됩니다.
	 *
	 * @param mt 플레이어가 클릭할 때 {@link MainHand}에 들고 있었던 아이템
	 * @param ct 클릭의 종류
	 * @return 능력 발동 여부
	 */
	public abstract boolean ActiveSkill(MaterialType mt, ClickType ct);

	/**
	 * 타겟팅 스킬 발동을 위해 사용됩니다.
	 *
	 * @param mt     플레이어가 클릭할 때 {@link MainHand}에 들고 있었던 아이템
	 * @param entity 타겟팅의 대상, 타겟팅의 대상이 없을 경우 null이 될 수 있습니다. null 체크가 필요합니다.
	 */
	public abstract void TargetSkill(MaterialType mt, LivingEntity entity);

	/**
	 * 능력 제한이 해제될 경우 호출됩니다.
	 */
	protected void onRestrictClear() {}

	/**
	 * 더 이상 사용되지 않는 {@link AbilityBase}를 제거할 때 사용됩니다.
	 * <p>
	 * {@link Participant#removeAbility()}를 통해 {@link Participant}의 능력을 제거할 때 호출됩니다.
	 * 절대 임의로 호출하지 마십시오.
	 */
	public final void destroy() {
		game.getPassiveManager().unregisterAll(this);
		for (AbstractGame.TimerBase timer : timers) {
			timer.stopTimer(true);
		}
	}

	protected final void registerTimer(AbstractGame.TimerBase timer) {
		timers.add(timer);
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

	/**
	 * 능력의 설명을 반환합니다.
	 */
	public final String[] getExplain() {
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

	/**
	 * 능력의 제한 여부를 설정합니다.
	 */
	public final void setRestricted(boolean restricted) {
		this.restricted = restricted;
		if (restricted) {
			for (AbstractGame.TimerBase timer : timers) {
				timer.stopTimer(true);
			}
		} else {
			onRestrictClear();
		}
	}

	public enum ClickType {LEFT_CLICK, RIGHT_CLICK}

	public enum MaterialType {

		IRON_INGOT(Material.IRON_INGOT), GOLD_INGOT(Material.GOLD_INGOT);

		private final Material material;

		MaterialType(Material material) {
			this.material = material;
		}

		public Material getMaterial() {
			return material;
		}

		public static MaterialType valueOf(Material material) {
			for (MaterialType type : values()) {
				if (type.material.equals(material)) {
					return type;
				}
			}
			return null;
		}

	}

	/**
	 * 쿨타임 타이머
	 * 능력의 쿨타임을 관리하기 위해 만들어진 타이머입니다.
	 *
	 * @author Daybreak 새벽
	 */
	public final class CooldownTimer extends AbstractGame.TimerBase {

		private final String abilityName;
		private boolean sendActionbar = true;

		public CooldownTimer(int cooldown, String abilityName) {
			game.super((AbilityWarThread.getGame() instanceof WRECK.Handler && ((WRECK.Handler) AbilityWarThread.getGame()).isWRECKEnabled()) ? (cooldown / 10) : cooldown);
			this.abilityName = abilityName;
			registerTimer(this);
		}

		public CooldownTimer(int cooldown) {
			this(cooldown, "");
		}

		public boolean isCooldown() {
			if (isRunning()) {
				Player player = getPlayer();
				if (player != null) {
					player.sendMessage(toString(ChatColor.WHITE));
				}
			}

			return isRunning();
		}

		@Override
		public void onProcess(int count) {
			if (sendActionbar) {
				new Actionbar(toString(), 0, 20, 0).sendTo(getPlayer());
			}
			if (count == (getMaxCount() / 2) || (count <= 5 && count >= 1)) {
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
				if (sendActionbar) {
					new Actionbar(message, 0, 20, 0).sendTo(getPlayer());
				}
			}
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
		public CooldownTimer setPeriod(int period) {
			return this;
		}

		public CooldownTimer setSendActionbar(boolean sendActionbar) {
			this.sendActionbar = sendActionbar;
			return this;
		}

	}

	/**
	 * Duration TimerBase (지속시간 타이머)
	 * 능력의 지속시간을 관리하고 능력을 발동시키기 위해 만들어진 타이머입니다.
	 *
	 * @author Daybreak 새벽
	 */
	public abstract class DurationTimer extends AbstractGame.TimerBase {

		private final int duration;
		private final String abilityName;
		private final CooldownTimer cooldownTimer;
		private boolean sendActionbar = true;

		public DurationTimer(int duration, String abilityName, CooldownTimer cooldownTimer) {
			game.super(duration);
			this.duration = duration;
			this.abilityName = abilityName;
			this.cooldownTimer = cooldownTimer;
			registerTimer(this);
		}
		public DurationTimer(int duration, String abilityName) {
			this(duration, "", null);
		}
		public DurationTimer(int duration, CooldownTimer cooldownTimer) {
			this(duration, "", cooldownTimer);
		}
		public DurationTimer(int duration) {
			this(duration, "", null);
		}

		protected void onDurationStart() {}
		protected abstract void onDurationProcess(int seconds);
		protected void onDurationEnd() {}

		public final boolean isDuration() {
			if (isRunning()) {
				Player player = getPlayer();
				if (player != null) {
					player.sendMessage(toString(ChatColor.WHITE));
				}
			}

			return isRunning();
		}

		@Override
		public final DurationTimer setPeriod(int period) {
			super.setPeriod(period);
			return this;
		}

		public final DurationTimer setSendActionbar(boolean sendActionbar) {
			this.sendActionbar = sendActionbar;
			return this;
		}

		@Override
		protected final void onStart() {
			counted = new ArrayList<>();
			onDurationStart();
		}

		private ArrayList<Integer> counted = new ArrayList<>();

		@Override
		protected final void onProcess(int count) {
			onDurationProcess(count);
			Player target = getPlayer();
			if (sendActionbar) {
				new Actionbar(toString(), 0, 20, 0).sendTo(target);
			}
			final int fixedCount = getFixedCount();
			if ((fixedCount == (duration / 2) && !counted.contains(fixedCount)) || (fixedCount <= 5 && fixedCount >= 1 && !counted.contains(fixedCount))) {
				counted.add(fixedCount);
				target.sendMessage(toString(ChatColor.WHITE));
				SoundLib.BLOCK_NOTE_BLOCK_HAT.playSound(target);
			}
		}

		@Override
		protected final void onEnd() {
			Player player = getPlayer();
			if (player != null) {
				onDurationEnd();

				String message = ChatColor.translateAlternateColorCodes('&', "&6지속 시간&f이 종료되었습니다.");
				player.sendMessage(message);
				if (sendActionbar) {
					new Actionbar(message, 0, 20, 0).sendTo(getPlayer());
				}

				if (cooldownTimer != null) {
					cooldownTimer.startTimer();
				}
			}
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

	public abstract class Timer extends AbstractGame.TimerBase {

		public Timer(int duration) {
			game.super(duration);
			registerTimer(this);
		}

		public Timer() {
			game.super();
			registerTimer(this);
		}

		@Override
		public final Timer setPeriod(int period) {
			super.setPeriod(period);
			return this;
		}

	}

}
