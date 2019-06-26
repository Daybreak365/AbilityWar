package DayBreak.AbilityWar.Ability;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import DayBreak.AbilityWar.Ability.AbilityManifest.Rank;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame.Participant;
import DayBreak.AbilityWar.Utils.Messager;
import DayBreak.AbilityWar.Utils.Validate;
import DayBreak.AbilityWar.Utils.Thread.TimerBase;

/**
 * 능력의 기반이 되는 클래스입니다.
 */
abstract public class AbilityBase {
	
	private Participant participant;
	private final String[] explain;
	private final String name;
	private final Rank rank;
	
	private boolean Restricted = true;
	
	/**
	 * 능력의 기본 생성자입니다.
	 * @param player		능력을 소유하는 플레이어
	 * @param AbilityName	능력 이름
	 * @param Rank			능력 랭크
	 * @param Explain		능력 설명
	 */
	public AbilityBase(Participant participant, String... explain) {
		this.participant = participant;
		this.explain = explain;
		
		AbilityManifest manifest = this.getClass().getAnnotation(AbilityManifest.class);
		
		if(manifest != null) {
			this.name = manifest.Name();
			this.rank = manifest.Rank();
		} else {
			this.name = null;
			this.rank = null;
		}
	}
	
	/**
	 * 액티브 스킬 발동을 위해 사용됩니다.
	 * GameListener에서 호출합니다.
	 * @param mt	플레이어가 클릭할 때 손에 있었던 아이템	
	 * @param ct	클릭의 종류
	 * @return		능력 발동 여부
	 */
	abstract public boolean ActiveSkill(MaterialType mt, ClickType ct);
	
	/**
	 * 패시브 스킬 발동을 위해 사용됩니다.
	 * GameListener에서 호출합니다.
	 * 패시브 이벤트는 GameListener.registerPassive(Class<? extends Event> clazz)로 등록할 수 있습니다.
	 * @param event		패시브 이벤트
	 */
	abstract public void PassiveSkill(Event event);
	
	/**
	 * 타겟팅 스킬 발동을 위해 사용됩니다.
	 * GameListener에서 호출합니다.
	 * @param mt		플레이어가 타겟팅을 할 때 손에 들고 있었던 아이템
	 * @param entity	타겟팅의 대상, 타겟팅의 대상이 없을 경우 null이 들어올 수 있습니다.
	 * 					null 체크가 필요합니다.
	 */
	abstract public void TargetSkill(MaterialType mt, Entity entity);
	
	/**
	 * 능력 제한이 해제될 경우 호출됩니다.
	 */
	abstract protected void onRestrictClear();
	
	/**
	 * 플레이어 능력 삭제시 사용됩니다.
	 * 플레이어의 능력이 변경될 때 자동으로 호출됩니다.
	 */
	public void Remove() {
		this.StopAllTimers();
		
		this.participant = null;
	}

	private void StopAllTimers() {
		for(TimerBase timer : getTimers()) {
			timer.StopTimer(true);
		}
	}
	
	/**
	 * Reflection으로 능력에 사용되는 TimerBase 또는 TimerBase를 부모 클래스로 하는 모든 타이머를 반환합니다.
	 * @return 능력에 사용되는 TimerBase 목록
	 */
	private List<TimerBase> getTimers() {
		ArrayList<TimerBase> Timers = new ArrayList<TimerBase>();
		
		for(Field field : this.getClass().getDeclaredFields()) {
			try {
				field.setAccessible(true);
				Class<?> type = field.getType();
				Class<?> superClass = type.getSuperclass();
				if(type.equals(TimerBase.class) ||(superClass != null && superClass.equals(TimerBase.class))) {
					Timers.add((TimerBase) field.get(this));
				}
				field.setAccessible(false);
			} catch (IllegalArgumentException | IllegalAccessException | NullPointerException exception) {
				Messager.sendErrorMessage("Reflection Error");
			}
		}
		
		return Timers;
	}
	
	/**
	 * 능력을 소유하는 플레이어를 반환합니다.
	 */
	public Player getPlayer() {
		return participant.getPlayer();
	}

	/**
	 * 능력을 소유하는 참가자를 반환합니다.
	 */
	public Participant getParticipant() {
		return participant;
	}

	/**
	 * 능력의 설명을 반환합니다.
	 */
	public String[] getExplain() {
		return explain;
	}

	/**
	 * 능력의 이름을 반환합니다.
	 * 능력 클래스에 AbilityManifest 어노테이션이 존재하지 않을 경우 null을 반환할 수 있습니다.
	 */
	public String getName() {
		return name;
	}

	/**
	 * 능력의 등급을 반환합니다.
	 * 능력 클래스에 AbilityManifest 어노테이션이 존재하지 않을 경우 null을 반환할 수 있습니다.
	 */
	public Rank getRank() {
		return rank;
	}

	/**
	 * 능력의 제한 여부를 반환합니다.
	 */
	public boolean isRestricted() {
		return Restricted;
	}
	
	/**
	 * 능력의 제한 여부를 설정합니다.
	 */
	public void setRestricted(boolean restricted) {
		this.Restricted = restricted;
		
		if(restricted) {
			this.StopAllTimers();
		} else {
			this.onRestrictClear();
		}
	}
	
	/**
	 * 일정 시간동안 유지되는 능력의 제한 여부를 설정합니다.
	 * restricted가 true일 경우 능력을 seconds초간 제한시키고, 후에 제한을 해제합니다.
	 * false인 경우에는 seconds초간 제한을 해제하고, 후에 능력을 제한합니다.
	 */
	public void setRestricted(boolean restricted, int seconds) {
		new TimerBase(seconds) {
			
			@Override
			protected void onStart() {
				setRestricted(restricted);
			}
			
			@Override
			protected void TimerProcess(Integer Seconds) {}

			@Override
			protected void onEnd() {
				setRestricted(!restricted);
			}
			
		}.StartTimer();
	}
	
	/**
	 * 능력을 소유하는 플레이어를 변경합니다.
	 * @param player						능력을 소유할 플레이어
	 * @throws IllegalArgumentException		플레이어가 null일 경우 발생
	 */
	public void updateParticipant(Participant participant) throws IllegalArgumentException {
		Validate.NotNull(participant);
		
		this.participant = participant;
	}
	
	public enum ClickType {
		/**
		 * 우클릭
		 */
		RightClick,
		/**
		 * 좌클릭
		 */
		LeftClick;
	}
	
	public enum MaterialType {
		
		/**
		 * 철괴
		 */
		Iron_Ingot(Material.IRON_INGOT),
		/**
		 * 금괴
		 */
		Gold_Ingot(Material.GOLD_INGOT);
		
		private Material material;
		
		private MaterialType(Material material) {
			this.material = material;
		}
		
		public Material getMaterial() {
			return material;
		}
		
	}
	
}
