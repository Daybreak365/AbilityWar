package daybreak.abilitywar.game.manager.effect;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import java.util.Map;
import java.util.WeakHashMap;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class Stun extends Effect implements Listener {

	private static final Map<Participant, Stun> stuns = new WeakHashMap<>();
	private final Participant participant;

	private Stun(Participant participant, TimeUnit timeUnit, int duration) {
		super(participant, "§e기절", TaskType.REVERSE, timeUnit.toTicks(duration));
		this.participant = participant;
		setPeriod(TimeUnit.TICKS, 1);
	}

	public static void apply(Participant participant, TimeUnit timeUnit, int duration) {
		if (stuns.containsKey(participant)) {
			Stun applied = stuns.get(participant);
			final int toTicks = timeUnit.toTicks(duration);
			if (toTicks > applied.getCount()) {
				applied.setCount(toTicks);
			}
		} else {
			new Stun(participant, timeUnit, duration).start();
		}
	}

	@Override
	protected void onStart() {
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		stuns.put(participant, this);
	}

	@EventHandler
	private void onPlayerMove(PlayerMoveEvent e) {
		if (e.getPlayer().getUniqueId().equals(participant.getPlayer().getUniqueId())) {
			e.setTo(e.getFrom());
		}
	}

	@Override
	protected void onEnd() {
		stuns.remove(participant);
		HandlerList.unregisterAll(this);
		super.onEnd();
	}

	@Override
	protected void onSilentEnd() {
		stuns.remove(participant);
		HandlerList.unregisterAll(this);
		super.onSilentEnd();
	}

}
