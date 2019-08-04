package DayBreak.AbilityWar.Ability;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import DayBreak.AbilityWar.Ability.AbilityFactory.AbilityRegisteration;
import DayBreak.AbilityWar.Ability.AbilityManifest.Rank;
import DayBreak.AbilityWar.Ability.AbilityManifest.Species;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame.Participant;
import DayBreak.AbilityWar.Game.Manager.PassiveManager.PassiveExecutor;
import DayBreak.AbilityWar.Utils.Thread.AbilityWarThread;
import DayBreak.AbilityWar.Utils.Thread.TimerBase;

/**
 * 능력의 기반이 되는 클래스입니다.
 */
public abstract class AbilityBase implements PassiveExecutor {
	
	private final Participant participant;
	private final String[] explain;
	private final AbilityManifest manifest;
	private final AbilityRegisteration<?> registeration;
	private final AbstractGame game;
	
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

		if(AbilityWarThread.isGameTaskRunning()) {
			this.game = AbilityWarThread.getGame();
		} else {
			throw new NullPointerException("게임이 진행중일 때 AbilityBase 클래스가 객체화되어야 합니다.");
		}
		
		if(AbilityFactory.isRegistered(this.getClass())) {
			AbilityRegisteration<?> ar = AbilityFactory.getRegisteration(this.getClass());
			this.registeration = ar;
			this.manifest = ar.getManifest();
			this.eventhandlers = ar.getEventhandlers();
			
			for(Class<? extends Event> eventClass : eventhandlers.keySet()) game.getPassiveManager().register(eventClass, this);
		} else {
			throw new NullPointerException("AbilityFactory에 등록되지 않은 능력입니다.");
		}
	}
	
	/**
	 * 액티브 스킬 발동을 위해 사용됩니다.
	 * GameListener에서 호출합니다.
	 * @param mt	플레이어가 클릭할 때 손에 있었던 아이템	
	 * @param ct	클릭의 종류
	 * @return		능력 발동 여부
	 */
	public abstract boolean ActiveSkill(MaterialType mt, ClickType ct);
	
	private final Map<Class<? extends Event>, Method> eventhandlers;
	
	@Override
	public void execute(Event event) {
		if(!Restricted) {
			Class<? extends Event> eventClass = event.getClass();
			if(eventhandlers.containsKey(eventClass)) {
				try {
					eventhandlers.get(eventClass).invoke(this, event);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 타겟팅 스킬 발동을 위해 사용됩니다.
	 * GameListener에서 호출합니다.
	 * @param mt		플레이어가 타겟팅을 할 때 손에 들고 있었던 아이템
	 * @param entity	타겟팅의 대상, 타겟팅의 대상이 없을 경우 null이 들어올 수 있습니다.
	 * 					null 체크가 필요합니다.
	 */
	public abstract void TargetSkill(MaterialType mt, Entity entity);
	
	/**
	 * 능력 제한이 해제될 경우 호출됩니다.
	 */
	protected abstract void onRestrictClear();
	
	/**
	 * 플레이어 능력 삭제시 사용됩니다.
	 * 플레이어의 능력이 변경될 때 자동으로 호출됩니다.
	 */
	public void Remove() {
		game.getPassiveManager().unregisterAll(this);
		this.StopAllTimers();
	}

	private void StopAllTimers() {
		for(TimerBase timer : getTimers()) {
			timer.StopTimer(true);
		}
	}
	
	/**
	 * 능력에 사용되는 모든 타이머를 반환합니다.
	 */
	private List<TimerBase> getTimers() {
		List<TimerBase> timers = new ArrayList<>();
		for(Field f : registeration.getTimers()) {
			try {
				f.setAccessible(true);
				timers.add((TimerBase) f.get(this));
				f.setAccessible(false);
			} catch(Exception ex) {}
		}
		
		return timers;
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
	 */
	public String getName() {
		return manifest.Name();
	}

	/**
	 * 능력의 등급을 반환합니다.
	 */
	public Rank getRank() {
		return manifest.Rank();
	}

	/**
	 * 능력의 종족을 반환합니다.
	 */
	public Species getSpecies() {
		return manifest.Species();
	}

	/**
	 * 이 능력이 사용되는 게임을 반환합니다.
	 */
	protected AbstractGame getGame() {
		return game;
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
