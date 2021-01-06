package daybreak.abilitywar.music;

import com.xxmicloxx.NoteBlockAPI.model.Layer;
import com.xxmicloxx.NoteBlockAPI.model.Note;
import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.utils.InstrumentUtils;
import com.xxmicloxx.NoteBlockAPI.utils.NoteUtils;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import kotlin.ranges.RangesKt;
import org.bukkit.Bukkit;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

public class MusicRadio extends SimpleTimer {

	private final Song song;

	public MusicRadio(final Song song) {
		super(TaskType.NORMAL, song.getLength() + 1);
		this.song = song;
		setPeriod(TimeUnit.TICKS, RangesKt.coerceAtLeast((int) song.getDelay(), 1));
	}

	@Override
	protected void run(int count) {
		for (Layer layer : song.getLayerHashMap().values()) {
			final Note note = layer.getNote(count - 1);
			if (note != null) {
				for (Player player : Bukkit.getOnlinePlayers()) {
					player.playSound(player.getLocation(), InstrumentUtils.getInstrument(note.getInstrument()), SoundCategory.RECORDS, 2f, NoteUtils.getPitch(note.getKey(), note.getPitch()));
				}
			}
		}
	}

}
