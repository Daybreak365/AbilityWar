package daybreak.abilitywar.game.manager.effect.registry;

import daybreak.abilitywar.game.AbstractGame.Effect;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.effect.registry.EffectRegistry.EffectRegistration;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;

public enum ApplicationMethod {
	MULTIPLE {
		@Override
		public <E extends Effect> E tryApply(EffectRegistration<E> registration, Participant participant, TimeUnit timeUnit, int duration) {
			return null;
		}
	}, UNIQUE_STACK {
		@Override
		public <E extends Effect> E tryApply(EffectRegistration<E> registration, Participant participant, TimeUnit timeUnit, int duration) {
			final E primaryEffect = participant.getPrimaryEffect(registration);
			if (primaryEffect != null) {
				primaryEffect.setCount(primaryEffect.getCount() + (timeUnit.toTicks(duration) / primaryEffect.getPeriod()));
				return primaryEffect;
			}
			return null;
		}
	}, UNIQUE_LONGEST {
		@Override
		public <E extends Effect> E tryApply(EffectRegistration<E> registration, Participant participant, TimeUnit timeUnit, int duration) {
			final E primaryEffect = participant.getPrimaryEffect(registration);
			if (primaryEffect != null) {
				final int period = primaryEffect.getPeriod(), left = primaryEffect.getCount(), newLeft = timeUnit.toTicks(duration) / period;
				if (newLeft > left) {
					primaryEffect.setCount(newLeft);
				}
				return primaryEffect;
			}
			return null;
		}
	};

	abstract <E extends Effect> E tryApply(EffectRegistration<E> registration, Participant participant, TimeUnit timeUnit, int duration);
}
