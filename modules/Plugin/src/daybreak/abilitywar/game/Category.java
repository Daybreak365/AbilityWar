package daybreak.abilitywar.game;

import daybreak.abilitywar.utils.library.MaterialX;
import org.bukkit.Material;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Category {
	enum GameCategory {
		GAME(Material.DIAMOND_SWORD, "게임"),
		MINIGAME(Material.FEATHER, "미니 게임"),
		DEBUG(MaterialX.COMMAND_BLOCK.getMaterial(), "디버그");

		private final Material icon;
		private final String displayName;

		GameCategory(final Material icon, final String displayName) {
			this.icon = icon;
			this.displayName = displayName;
		}

		public Material getIcon() {
			return icon;
		}

		public String getDisplayName() {
			return displayName;
		}
	}

	GameCategory value();
}
