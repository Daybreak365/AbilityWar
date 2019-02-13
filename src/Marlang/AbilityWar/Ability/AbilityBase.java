package Marlang.AbilityWar.Ability;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import Marlang.AbilityWar.Ability.Timer.CooldownTimer;
import Marlang.AbilityWar.Ability.Timer.DurationTimer;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.TimerBase;
import Marlang.AbilityWar.Utils.Validate;

abstract public class AbilityBase {
	
	private Player player;
	private String AbilityName;
	private Rank Rank;
	private String[] Explain;
	
	private boolean Restricted = true;
	
	protected AbilityBase(Player player, String AbilityName, Rank Rank, String... Explain) {
		this.player = player;
		this.AbilityName = AbilityName;
		this.Rank = Rank;
		this.Explain = Explain;
	}
	
	abstract public boolean ActiveSkill(ActiveMaterialType mt, ActiveClickType ct);
	
	abstract public void PassiveSkill(Event event);
	
	abstract public void AbilityEvent(EventType type);
	
	public enum EventType {
		
		AbilityDelete,
		RestrictClear;
		
	}
	
	/**
	 * 플레이어 능력 삭제시 사용
	 */
	public void DeleteAbility() {
		AbilityEvent(EventType.AbilityDelete);
		
		for(TimerBase timer : getTimers()) {
			timer.StopTimer(true);
		}
		
		this.player = null;
	}
	
	private List<TimerBase> getTimers() {
		ArrayList<TimerBase> Timers = new ArrayList<TimerBase>();
		
		for(Field field : this.getClass().getDeclaredFields()) {
			try {
				field.setAccessible(true);
				if(field.getType().isAssignableFrom(TimerBase.class)) {
					Timers.add((TimerBase) field.get(this));
				} else if(field.getType().isAssignableFrom(DurationTimer.class)) {
					Timers.add((DurationTimer) field.get(this));
				} else if(field.getType().isAssignableFrom(CooldownTimer.class)) {
					Timers.add((CooldownTimer) field.get(this));
				}
				field.setAccessible(false);
			} catch (IllegalArgumentException | IllegalAccessException | NullPointerException exception) {
				Messager.sendErrorMessage("Reflection Error");
			}
		}
		
		return Timers;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public String getAbilityName() {
		return AbilityName;
	}
	
	public Rank getRank() {
		return Rank;
	}
	
	public String[] getExplain() {
		return Explain;
	}
	
	protected void setExplain(String... Explain) {
		this.Explain = Explain;
	}
	
	public boolean isRestricted() {
		return Restricted;
	}
	
	public void setRestricted(boolean restricted) {
		Restricted = restricted;
		
		if(!restricted) {
			AbilityEvent(EventType.RestrictClear);
		}
	}
	
	public void updatePlayer(Player player) throws IllegalArgumentException {
		Validate.NotNull(player);
		
		this.player = player;
	}
	
	public enum ActiveClickType {
		RightClick, LeftClick;
	}
	
	public enum ActiveMaterialType {
		
		Iron_Ingot(Material.IRON_INGOT),
		Gold_Ingot(Material.GOLD_INGOT);
		
		Material material;
		
		private ActiveMaterialType(Material material) {
			this.material = material;
		}
		
		public Material getMaterial() {
			return material;
		}
		
	}
	
	public enum Rank {

		God(ChatColor.translateAlternateColorCodes('&', "&c신 등급")),
		S(ChatColor.translateAlternateColorCodes('&', "&dS 등급")),
		A(ChatColor.translateAlternateColorCodes('&', "&aA 등급")),
		B(ChatColor.translateAlternateColorCodes('&', "&bB 등급")),
		C(ChatColor.translateAlternateColorCodes('&', "&eC 등급")),
		D(ChatColor.translateAlternateColorCodes('&', "&7D 등급"));
		
		String RankName;
		
		private Rank(String RankName) {
			this.RankName = RankName;
		}
		
		public String getRankName() {
			return RankName;
		}
		
	}
	
}
