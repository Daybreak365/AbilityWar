package daybreak.abilitywar.music.christmas;

import com.google.common.collect.ImmutableList;
import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder;
import daybreak.abilitywar.utils.base.random.Random;

public class ChristmasMusic {

	private static final Random random = new Random();
	public static final ImmutableList<Song> songs;

	public static Song getRandom() {
		return random.pick(songs);
	}

	static {
		final ImmutableList.Builder<Song> builder = ImmutableList.builder();
		for (final String name : new String[]{"JingleBells", "WeWishYouAMerryChristmas", "RockinAroundTheChristmasTree", "WinterWonderland", "Rudolph"}) {
			final Song song = NBSDecoder.parse(ChristmasMusic.class.getResourceAsStream("/daybreak/abilitywar/music/christmas/" + name + ".nbs"));
			if (song != null) builder.add(song);
		}
		songs = builder.build();
	}

}
