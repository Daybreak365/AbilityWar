package DayBreak.AbilityWar.Ability;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.MainHand;

import DayBreak.AbilityWar.AbilityWar;
import DayBreak.AbilityWar.Ability.AbilityFactory.AbilityRegisteration;
import DayBreak.AbilityWar.Ability.AbilityManifest.Rank;
import DayBreak.AbilityWar.Ability.AbilityManifest.Species;
import DayBreak.AbilityWar.Game.Games.ChangeAbility.ChangeAbilityWar;
import DayBreak.AbilityWar.Game.Games.Default.DefaultGame;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame.Participant;
import DayBreak.AbilityWar.Game.Manager.AbilityList;
import DayBreak.AbilityWar.Game.Manager.PassiveManager.PassiveExecutor;
import DayBreak.AbilityWar.Utils.Thread.AbilityWarThread;
import DayBreak.AbilityWar.Utils.Thread.TimerBase;

/**
 * {@link AbilityWar} 플러그인에서 사용하는 <strong>모든 능력</strong>의 기반이 되는 클래스입니다.
 * <p>
 * 만들어진 <strong>모든 능력은 반드시 {@link AbilityFactory}에 등록되어야 합니다.</strong>
 * <p>
 * <ul>
 * {@link AbilityFactory#registerAbility(clazz)}
 * </ul>
 * {@link DefaultGame}, {@link ChangeAbilityWar} 등에서 사용할 능력은 추가적으로
 * {@link AbilityList}에 등록해야 합니다.
 * <p>
 * <ul>
 * {@link AbilityList#registerAbility(clazz)}
 * </ul>
 * 
 * @author DayBreak 새벽
 */
public abstract class AbilityBase implements PassiveExecutor {

	private final Participant participant;
	private final String[] explain;
	private final AbilityManifest manifest;
	private final AbilityRegisteration<?> registeration;
	private final AbstractGame game;

	private boolean Restricted = true;

	/**
	 * {@link AbilityBase}의 기본 생성자입니다.
	 * 
	 * @param participant 능력을 소유하는 참가자
	 * @param explain     능력 설명
	 * 
	 * @throws IllegalStateException 게임이 진행중이지 않은 경우, {@link AbilityFactory}에 등록되지
	 *                               않은 능력일 경우
	 */
	public AbilityBase(Participant participant, String... explain) {
		this.participant = participant;
		this.explain = explain;

		if (AbilityWarThread.isGameTaskRunning()) {
			this.game = AbilityWarThread.getGame();
		} else {
			throw new IllegalStateException("게임이 진행중일 때 AbilityBase 클래스가 객체화되어야 합니다.");
		}

		if (AbilityFactory.isRegistered(this.getClass())) {
			AbilityRegisteration<?> ar = AbilityFactory.getRegisteration(this.getClass());
			this.registeration = ar;
			this.manifest = ar.getManifest();
			this.eventhandlers = ar.getEventhandlers();

			for (Class<? extends Event> eventClass : eventhandlers.keySet())
				game.getPassiveManager().register(eventClass, this);
		} else {
			throw new IllegalStateException("AbilityFactory에 등록되지 않은 능력입니다.");
		}
	}

	private final Map<Class<? extends Event>, Method> eventhandlers;

	@Override
	public void execute(Event event) {
		if (!Restricted) {
			Class<? extends Event> eventClass = event.getClass();
			if (eventhandlers.containsKey(eventClass)) {
				try {
					eventhandlers.get(eventClass).invoke(this, event);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
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
	public abstract boolean ActiveSkill(MaterialType mt, ClickType ct);

	/**
	 * 타겟팅 스킬 발동을 위해 사용됩니다.
	 * 
	 * @param materialType 플레이어가 클릭할 때 {@link MainHand}에 들고 있었던 아이템
	 * @param entity       타겟팅의 대상, 타겟팅의 대상이 없을 경우 null이 될 수 있습니다. null 체크가 필요합니다.
	 */
	public abstract void TargetSkill(MaterialType mt, LivingEntity entity);

	/**
	 * 능력 제한이 해제될 경우 호출됩니다.
	 */
	protected abstract void onRestrictClear();

	/**
	 * 더 이상 사용되지 않는 {@link AbilityBase}를 제거할 때 사용됩니다.<p>
	 * {@link Participant#removeAbility()}를 통해 {@link Participant}의 능력을 제거할 때 호출됩니다.
	 * 절대 임의로 호출하지 마십시오.
	 */
	public final void destroy() {
		game.getPassiveManager().unregisterAll(this);
		stopTimers();
	}

	private final void stopTimers() {
		for (TimerBase timer : getTimers()) {
			timer.StopTimer(true);
		}
	}

	/**
	 * 능력에 사용되는 모든 타이머를 반환합니다.
	 */
	private final List<TimerBase> getTimers() {
		List<TimerBase> timers = new ArrayList<>();
		for (Field f : registeration.getTimers()) {
			try {
				f.setAccessible(true);
				timers.add((TimerBase) f.get(this));
				f.setAccessible(false);
			} catch (Exception ex) {}
		}

		return timers;
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
		return Restricted;
	}

	/**
	 * 능력의 제한 여부를 설정합니다.
	 */
	public final void setRestricted(boolean restricted) {
		this.Restricted = restricted;

		if (restricted) {
			this.stopTimers();
		} else {
			this.onRestrictClear();
		}
	}

	public enum ClickType {
		LeftClick, RightClick;
	}

	public enum MaterialType {

		Iron_Ingot(Material.IRON_INGOT), Gold_Ingot(Material.GOLD_INGOT);

		private Material material;

		private MaterialType(Material material) {
			this.material = material;
		}

		public Material getMaterial() {
			return material;
		}

	}

}
