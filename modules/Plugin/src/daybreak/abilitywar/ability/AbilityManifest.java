package daybreak.abilitywar.ability;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AbilityManifest {

	String name();

	Rank rank();

	enum Rank {

		SPECIAL("§cSPECIAL 등급"),
		S("§dS 등급"),
		A("§aA 등급"),
		B("§bB 등급"),
		C("§eC 등급");

		private final String rankName;

		Rank(String rankName) {
			this.rankName = rankName;
		}

		public String getRankName() {
			return rankName;
		}

	}

	Species species();

	enum Species {

		SPECIAL("§e특별 능력"),
		HUMAN("§f인간"),
		GOD("§c신"),
		DEMIGOD("§c반신§7반인"),
		ANIMAL("§2동물"),
		UNDEAD("§c언데드"),
		OTHERS("§8기타");

		private final String speciesName;

		Species(String speciesName) {
			this.speciesName = speciesName;
		}

		public String getSpeciesName() {
			return speciesName;
		}

	}

	String[] explain() default {};

}
