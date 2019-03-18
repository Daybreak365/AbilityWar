package Marlang.AbilityWar.Ability;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.bukkit.ChatColor;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AbilityManifest {

	/**
	 * 능력 이름
	 */
	public String Name();
	
	/**
	 * 능력 랭크
	 */
	public Rank Rank();

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
