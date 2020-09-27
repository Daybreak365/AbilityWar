package daybreak.abilitywar.ability;

import daybreak.abilitywar.utils.library.MaterialX;
import org.bukkit.ChatColor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface Tips {

	@interface Description {
		String subject();
		String[] explain();
		MaterialX icon() default MaterialX.IRON_BLOCK;
	}

	@interface Stats {
		Level offense();
		Level survival();
		Level crowdControl();
		Level mobility();
		Level utility();
	}

	enum Level {
		ZERO(ChatColor.DARK_GRAY + "□□□□□□□□□□"),
		ONE(ChatColor.DARK_RED + "■" + ChatColor.DARK_GRAY + "□□□□□□□□□"),
		TWO(ChatColor.DARK_RED + "■■" + ChatColor.DARK_GRAY + "□□□□□□□□"),
		THREE(ChatColor.RED + "■■■" + ChatColor.DARK_GRAY + "□□□□□□□"),
		FOUR(ChatColor.RED + "■■■■" + ChatColor.DARK_GRAY + "□□□□□□"),
		FIVE(ChatColor.YELLOW + "■■■■■" + ChatColor.DARK_GRAY + "□□□□□"),
		SIX(ChatColor.YELLOW + "■■■■■■" + ChatColor.DARK_GRAY + "□□□□"),
		SEVEN(ChatColor.GREEN + "■■■■■■■" + ChatColor.DARK_GRAY + "□□□"),
		EIGHT(ChatColor.GREEN + "■■■■■■■■" + ChatColor.DARK_GRAY + "□□"),
		NINE(ChatColor.DARK_GREEN + "■■■■■■■■■" + ChatColor.DARK_GRAY + "□"),
		TEN(ChatColor.DARK_GREEN + "■■■■■■■■■■");

		private final String display;

		Level(final String display) {
			this.display = display;
		}

		public String getDisplay() {
			return display;
		}
	}
	enum Difficulty {
		VERY_EASY(ChatColor.DARK_GREEN + "■" + ChatColor.DARK_GRAY + "□□□□"),
		EASY(ChatColor.GREEN + "■■" + ChatColor.DARK_GRAY + "□□□"),
		NORMAL(ChatColor.YELLOW + "■■■" + ChatColor.DARK_GRAY + "□□"),
		HARD(ChatColor.RED + "■■■■" + ChatColor.DARK_GRAY + "□"),
		VERY_HARD(ChatColor.DARK_RED + "■■■■■");

		private final String display;

		Difficulty(final String display) {
			this.display = display;
		}

		public String getDisplay() {
			return display;
		}
	}

	Difficulty difficulty();
	Stats stats();
	String[] tip() default {};
	Description[] strong() default {};
	Description[] weak() default {};
}
