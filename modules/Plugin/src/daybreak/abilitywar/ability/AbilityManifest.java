package daybreak.abilitywar.ability;

import org.bukkit.ChatColor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AbilityManifest {

	String Name();
	Rank Rank();

	enum Rank {

		SPECIAL(ChatColor.translateAlternateColorCodes('&', "&cSPECIAL 등급")),
		S(ChatColor.translateAlternateColorCodes('&', "&dS 등급")),
		A(ChatColor.translateAlternateColorCodes('&', "&aA 등급")),
		B(ChatColor.translateAlternateColorCodes('&', "&bB 등급")),
		C(ChatColor.translateAlternateColorCodes('&', "&eC 등급")),
		D(ChatColor.translateAlternateColorCodes('&', "&7D 등급"));

		private final String rankName;

		Rank(String rankName) {
			this.rankName = rankName;
		}

		public String getRankName() {
			return rankName;
		}

	}

	Species Species();

	enum Species {

		SPECIAL(ChatColor.translateAlternateColorCodes('&', "&e특별 능력")),
		HUMAN(ChatColor.translateAlternateColorCodes('&', "&f인간")),
		GOD(ChatColor.translateAlternateColorCodes('&', "&c신")),
		DEMIGOD(ChatColor.translateAlternateColorCodes('&', "&7데미&c갓")),
		ANIMAL(ChatColor.translateAlternateColorCodes('&', "&2동물")),
		UNDEAD(ChatColor.translateAlternateColorCodes('&', "&c언데드")),
		OTHERS(ChatColor.translateAlternateColorCodes('&', "&8기타"));

		private final String speciesName;

		Species(String speciesName) {
			this.speciesName = speciesName;
		}

		public String getSpeciesName() {
			return speciesName;
		}

	}

}
