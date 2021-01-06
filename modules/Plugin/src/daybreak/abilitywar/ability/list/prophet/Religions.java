package daybreak.abilitywar.ability.list.prophet;

import com.google.common.base.Preconditions;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.module.ListenerModule;
import daybreak.abilitywar.game.module.ModuleBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@ModuleBase(Religions.class)
public class Religions implements ListenerModule {

	private final Map<Participant, Belief> beliefs = new HashMap<>();
	private final Map<Participant, Religion> religions = new HashMap<>();

	Religions() {

	}

	@Nullable
	public Religion getOwningReligion(final Participant prophet) {
		return religions.get(prophet);
	}

	public boolean isProphet(final Participant participant) {
		return religions.containsKey(participant);
	}

	@Nullable
	public Religion getReligion(final Participant participant) {
		return getBelief(participant).getReligion();
	}

	@NotNull
	public Belief getBelief(final Participant participant) {
		Belief belief = beliefs.get(participant);
		if (belief == null) {
			belief = new Belief(participant);
			beliefs.put(participant, belief);
		}
		return belief;
	}

	public class Belief {

		private final Participant participant;
		private @Nullable Religion religion;
		private int belief;

		private Belief(final Participant participant) {
			this.participant = participant;
		}

		public void addBelief(final @NotNull Religion religion, final int belief) {
			Preconditions.checkArgument(belief > 0);
			if (this.religion != null) {
				if (this.religion == religion) {
					this.belief += belief;
				} else {
					this.belief -= belief;
					if (this.belief < 0) {
						this.religion = religion;
						this.belief = -this.belief;
					}
				}
			} else {
				this.religion = religion;
				this.belief = belief;
			}
		}

		public Participant getParticipant() {
			return participant;
		}

		@Nullable
		public Religion getReligion() {
			return religion;
		}

		public boolean hasReligion() {
			return religion != null;
		}

		public int getBelief() {
			return belief;
		}

	}

	public class Religion {

		private final Participant prophet;
		private final String name;

		Religion(final Participant prophet, final String name) {
			this.prophet = prophet;
			this.name = name;
			religions.put(prophet, this);
		}

		public Participant getProphet() {
			return prophet;
		}

		public String getName() {
			return name;
		}
	}

}
