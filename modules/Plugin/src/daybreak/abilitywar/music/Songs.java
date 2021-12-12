package daybreak.abilitywar.music;

import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.utils.base.random.Random;

import java.util.Locale;

public class Songs {

	private static final Random random = new Random();

	static {

	}

	public enum Category {
		XMAS(
				"JingleBells",
				"RockinAroundTheChristmasTree",
				"Rudolph",
				"WeWishYouAMerryChristmas",
				"WinterWonderland"
		);

		private final String[] songs;

		Category(String... songs) {
			this.songs = songs;
		}
	}

	public static Song getRandom(Category category) {
		return NBSDecoder.parse(AbilityWar.class.getResourceAsStream("/daybreak/abilitywar/music/" + category.name().toLowerCase(Locale.ROOT) + "/" + random.pick(category.songs) + ".nbs"));
	}

}
