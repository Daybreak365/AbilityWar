package daybreak.abilitywar.game.list.mix.synergy.game;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.game.Game;
import daybreak.abilitywar.game.GameManifest;
import daybreak.abilitywar.game.ParticipantStrategy;
import daybreak.abilitywar.game.event.participant.ParticipantAbilitySetEvent;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;
import daybreak.abilitywar.utils.annotations.Beta;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@GameManifest(name = "시너지 능력자", description = {

})
@Beta
public class SynergyGame extends Game {

	public SynergyGame(Collection<Player> players) {
		super(players);
	}

	@Override
	public Collection<SynergyParticipant> getParticipants() {
		return ((SynergyParticipantStrategy) participantStrategy).getParticipants();
	}

	@Override
	public SynergyParticipant getParticipant(Player player) {
		return ((SynergyParticipantStrategy) participantStrategy).getParticipant(player.getUniqueId());
	}

	@Override
	public SynergyParticipant getParticipant(UUID uuid) {
		return ((SynergyParticipantStrategy) participantStrategy).getParticipant(uuid);
	}

	@Override
	protected void progressGame(int seconds) {

	}

	@Override
	protected ParticipantStrategy newParticipantStrategy(Collection<Player> players) {
		return new SynergyParticipantStrategy(players);
	}

	public class SynergyParticipant extends Participant {

		private Synergy ability = null;
		private final Attributes attributes = new Attributes();

		protected SynergyParticipant(@NotNull Player player) {
			super(player);
		}

		@Override
		public void setAbility(AbilityRegistration registration) throws ReflectiveOperationException {
			if (!Synergy.class.isAssignableFrom(registration.getAbilityClass())) throw new IllegalArgumentException("ability must be instance of Synergy");
			final Synergy oldAbility = removeAbility();
			final Synergy ability = (Synergy) AbilityBase.create(registration, this);
			ability.setRestricted(false);
			this.ability = ability;
			Bukkit.getPluginManager().callEvent(new ParticipantAbilitySetEvent(this, oldAbility, ability));
		}

		@Override
		public boolean hasAbility() {
			return ability != null;
		}

		@Override
		@Nullable
		public Synergy getAbility() {
			return ability;
		}

		@Override
		@Nullable
		public Synergy removeAbility() {
			final Synergy ability = this.ability;
			if (ability != null) {
				ability.destroy();
				this.ability = null;
			}
			return ability;
		}

		@Override
		public Attributes attributes() {
			return attributes;
		}
	}

	protected class SynergyParticipantStrategy implements ParticipantStrategy {

		private final Map<UUID, SynergyParticipant> participants = new HashMap<>();

		public SynergyParticipantStrategy(Collection<Player> players) {
			for (Player player : players) {
				participants.put(player.getUniqueId(), new SynergyParticipant(player));
			}
		}

		@Override
		public Collection<SynergyParticipant> getParticipants() {
			return Collections.unmodifiableCollection(participants.values());
		}

		@Override
		public boolean isParticipating(UUID uuid) {
			return participants.containsKey(uuid);
		}

		@Override
		public SynergyParticipant getParticipant(UUID uuid) {
			return participants.get(uuid);
		}

		@Override
		public void addParticipant(Player player) throws UnsupportedOperationException {
			throw new UnsupportedOperationException("참가자를 추가할 수 없습니다.");
		}

		@Override
		public void removeParticipant(UUID uuid) throws UnsupportedOperationException {
			throw new UnsupportedOperationException("참가자를 제거할 수 없습니다.");
		}

	}

}
