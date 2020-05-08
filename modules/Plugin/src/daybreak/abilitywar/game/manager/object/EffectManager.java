package daybreak.abilitywar.game.manager.object;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.list.VictoryBySword;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer.TaskType;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.EventExecutor;

public class EffectManager implements EventExecutor {

	private final AbstractGame game;

	public EffectManager(AbstractGame game) {
		this.game = game;
		Bukkit.getPluginManager().registerEvent(PlayerMoveEvent.class, game, EventPriority.HIGHEST, this, AbilityWar.getPlugin());

		registerCondition(new EffectCondition() {
			@Override
			protected boolean checkCondition(Participant p, EffectType type) {
				return !type.equals(EffectType.STUN) || !p.hasAbility()
						|| !p.getAbility().getClass().equals(VictoryBySword.class);
			}
		});
	}

	private final Set<Participant> STUN = new HashSet<>();

	public void Stun(Player player, int tick) {
		if (game.isParticipating(player)) {
			Participant participant = game.getParticipant(player);
			for (EffectCondition condition : conditions) {
				if (!condition.checkCondition(participant, EffectType.STUN))
					return;
			}

			ActionbarChannel actionbarChannel = participant.actionbar().newChannel();
			game.new GameTimer(TaskType.REVERSE, tick) {
				@Override
				protected void onStart() {
					STUN.add(participant);
				}

				@Override
				protected void onEnd() {
					STUN.remove(participant);
					actionbarChannel.unregister();
				}

				@Override
				protected void run(int count) {
					actionbarChannel.update(ChatColor.translateAlternateColorCodes('&', "&e기절&f: " + (count / 20.0) + "초"));
				}
			}.setPeriod(TimeUnit.TICKS, 1).start();
		}
	}

	private final Set<EffectCondition> conditions = new HashSet<>();

	public void registerCondition(EffectCondition condition) {
		conditions.add(condition);
	}

	public abstract class EffectCondition {

		protected abstract boolean checkCondition(Participant p, EffectType type);

	}

	private enum EffectType {STUN}

	@Override
	public void execute(Listener listener, Event event) {
		if (event instanceof PlayerMoveEvent) {
			PlayerMoveEvent e = (PlayerMoveEvent) event;
			Player p = e.getPlayer();
			if (game.isParticipating(p)) {
				Participant part = game.getParticipant(p);
				if (STUN.contains(part)) {
					e.setTo(e.getFrom());
				}
			}
		}
	}

	public interface Handler {
		EffectManager getEffectManager();
	}

}
