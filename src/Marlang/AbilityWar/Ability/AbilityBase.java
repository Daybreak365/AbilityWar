package Marlang.AbilityWar.Ability;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Validate;
import Marlang.AbilityWar.Utils.Thread.TimerBase;

/**
 * 능력의 기반이 되는 클래스입니다.
 */
abstract public class AbilityBase {
	
	private Player player;
	private String AbilityName;
	private Rank Rank;
	private String[] Explain;
	
	private boolean Restricted = true;
	
	/**
	 * 능력의 기본 생성자입니다.
	 * @param player		능력을 소유하는 플레이어
	 * @param AbilityName	능력 이름
	 * @param Rank			능력 랭크
	 * @param Explain		능력 설명
	 */
	public AbilityBase(Player player, String AbilityName, Rank Rank, String... Explain) {
		this.player = player;
		this.AbilityName = AbilityName;
		this.Rank = Rank;
		this.Explain = Explain;
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
	public void DeleteAbility() {
		for(TimerBase timer : getTimers()) {
			timer.StopTimer(true);
		}
		
		this.player = null;
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
		return player;
	}

	/**
	 * 능력의 이름을 반환합니다.
	 */
	public String getAbilityName() {
		return AbilityName;
	}

	/**
	 * 능력의 랭크를 반환합니다.
	 */
	public Rank getRank() {
		return Rank;
	}

	/**
	 * 능력의 설명을 반환합니다.
	 */
	public String[] getExplain() {
		return Explain;
	}

	/**
	 * 능력의 설명을 설정합니다.
	 */
	protected void setExplain(String... Explain) {
		this.Explain = Explain;
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
		Restricted = restricted;
		
		if(!restricted) {
			onRestrictClear();
		}
	}
	
	/**
	 * 능력을 소유하는 플레이어를 변경합니다.
	 * @param player						능력을 소유할 플레이어
	 * @throws IllegalArgumentException		플레이어가 null일 경우 발생
	 */
	public void updatePlayer(Player player) throws IllegalArgumentException {
		Validate.NotNull(player);
		
		this.player = player;
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
	
	public enum Rank {
		
		/**
		 * Special 등급
		 */
		SPECIAL(ChatColor.translateAlternateColorCodes('&', "&5Special 등급")),
		/**
		 * 신 등급
		 */
		GOD(ChatColor.translateAlternateColorCodes('&', "&c신 등급")),
		/**
		 * S 등급
		 */
		S(ChatColor.translateAlternateColorCodes('&', "&dS 등급")),
		/**
		 * A 등급
		 */
		A(ChatColor.translateAlternateColorCodes('&', "&aA 등급")),
		/**
		 * B 등급
		 */
		B(ChatColor.translateAlternateColorCodes('&', "&bB 등급")),
		/**
		 * C 등급
		 */
		C(ChatColor.translateAlternateColorCodes('&', "&eC 등급")),
		/**
		 * D 등급
		 */
		D(ChatColor.translateAlternateColorCodes('&', "&7D 등급"));
		
		private String RankName;
		
		private Rank(String RankName) {
			this.RankName = RankName;
		}
		
		public String getRankName() {
			return RankName;
		}
		
	}
	
}
