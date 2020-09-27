package daybreak.abilitywar.game.list.mix.debug;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.Category;
import daybreak.abilitywar.game.Category.GameCategory;
import daybreak.abilitywar.game.GameManifest;
import daybreak.abilitywar.game.ParticipantStrategy;
import daybreak.abilitywar.game.list.mix.AbstractMix;
import daybreak.abilitywar.game.module.DummyManager;
import daybreak.abilitywar.utils.base.minecraft.PlayerCollector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@GameManifest(name = "Mix Debug Mode", description = {})
@Category(GameCategory.DEBUG)
public class MixDebugMode extends AbstractMix implements AbstractGame.Observer {

	public MixDebugMode() {
		super(PlayerCollector.EVERY_PLAYER_EXCLUDING_SPECTATORS());
		attachObserver(this);
		addModule(new DummyManager(this));
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
	}

	@Override
	protected void progressGame(int seconds) {
		if (seconds == 1) {
			setRestricted(false);
			startGame();
		}
	}

	@Override
	public void update(GameUpdate update) {
		if (update == GameUpdate.END) {
			HandlerList.unregisterAll(this);
		}
	}

	@Override
	protected ParticipantStrategy newParticipantStrategy(Collection<Player> players) {
		return new DebugManagement(players);
	}

	private class DebugParticipant extends MixParticipant {
		protected DebugParticipant(@NotNull Player player) {
			super(player);
		}
	}

	private class DebugManagement extends MixParticipantStrategy {

		private final Map<UUID, DebugParticipant> participants = new HashMap<>();

		public DebugManagement(Collection<Player> players) {
			super(players);
			for (Player player : players) {
				participants.put(player.getUniqueId(), new DebugParticipant(player));
			}
		}

		@Override
		public Collection<MixParticipant> getParticipants() {
			return Collections.unmodifiableCollection(participants.values());
		}

		@Override
		public boolean isParticipating(UUID uuid) {
			return participants.containsKey(uuid);
		}

		@Override
		public MixParticipant getParticipant(UUID uuid) {
			return participants.get(uuid);
		}

		@Override
		public void addParticipant(Player player) throws UnsupportedOperationException {
			participants.putIfAbsent(player.getUniqueId(), new DebugParticipant(player));
		}

		@Override
		public void removeParticipant(UUID uuid) throws UnsupportedOperationException {
			participants.remove(uuid);
		}

	}

}
