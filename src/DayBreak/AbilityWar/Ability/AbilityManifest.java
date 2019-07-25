package DayBreak.AbilityWar.Ability;

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

	public Species Species();
	
	public enum Species {
		
		HUMAN(ChatColor.translateAlternateColorCodes('&', "&f인간")),
		GOD(ChatColor.translateAlternateColorCodes('&', "&c신")),
		DEMIGOD(ChatColor.translateAlternateColorCodes('&', "&7데미&c갓")),
		ANIMAL(ChatColor.translateAlternateColorCodes('&', "&2동물")),
		OTHERS(ChatColor.translateAlternateColorCodes('&', "&8기타"));
		
		private String name;
		
		private Species(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
		
	}

}
