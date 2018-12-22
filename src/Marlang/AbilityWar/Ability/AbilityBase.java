package Marlang.AbilityWar.Ability;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

abstract public class AbilityBase {
	
	Player player;
	String AbilityName;
	Rank Rank;
	String[] Explain;
	
	boolean Restricted = true;
	
	public AbilityBase(String AbilityName, Rank Rank, String... Explain) {
		this.AbilityName = AbilityName;
		this.Rank = Rank;
		this.Explain = Explain;
	}
	
	abstract public void ActiveSkill(ActiveMaterialType mt, ActiveClickType ct);
	
	abstract public void PassiveSkill(Event event);
	
	public Player getPlayer() {
		return player;
	}
	
	public String getAbilityName() {
		return AbilityName;
	}
	
	public void setPlayer(Player player) {
		this.player = player;
	}
	
	public Rank getRank() {
		return Rank;
	}
	
	public String[] getExplain() {
		return Explain;
	}
	
	public boolean isRestricted() {
		return Restricted;
	}
	
	public void setRestricted(boolean restricted) {
		Restricted = restricted;
	}
	
	public static enum ActiveClickType {
		RightClick, LeftClick;
	}
	
	public static enum ActiveMaterialType {
		
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
	
	public static enum Rank {
		
		S(ChatColor.translateAlternateColorCodes('&', "&dS 珐农")),
		A(ChatColor.translateAlternateColorCodes('&', "&aA 珐农")),
		B(ChatColor.translateAlternateColorCodes('&', "&bB 珐农")),
		C(ChatColor.translateAlternateColorCodes('&', "&eC 珐农")),
		D(ChatColor.translateAlternateColorCodes('&', "&7D 珐农"));
		
		String RankName;
		
		private Rank(String RankName) {
			this.RankName = RankName;
		}
		
		public String getRankName() {
			return RankName;
		}
		
	}
	
}
