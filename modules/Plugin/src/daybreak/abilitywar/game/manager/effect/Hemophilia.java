package daybreak.abilitywar.game.manager.effect;

import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.effect.Bleed.ParticipantBleed;
import daybreak.abilitywar.game.manager.effect.registry.ApplicationMethod;
import daybreak.abilitywar.game.manager.effect.registry.EffectManifest;
import daybreak.abilitywar.game.manager.effect.registry.EffectRegistry;
import daybreak.abilitywar.game.manager.effect.registry.EffectRegistry.EffectRegistration;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;

@EffectManifest(name = "혈사병", displayName = "§4혈사병", method = ApplicationMethod.UNIQUE_LONGEST)
public class Hemophilia extends AbstractGame.Effect {

	public static final EffectRegistration<Hemophilia> registration = EffectRegistry.registerEffect(Hemophilia.class);

	public static void apply(Participant participant, TimeUnit timeUnit, int duration) {
		registration.apply(participant, timeUnit, duration);
	}

	private final Participant participant;

	public Hemophilia(Participant participant, TimeUnit timeUnit, int duration) {
		participant.getGame().super(registration, participant, timeUnit.toTicks(duration) / 5);
		this.participant = participant;
		setPeriod(TimeUnit.TICKS, 5);
	}

	@Override
	protected void run(int count) {
		super.run(count);
		for (ParticipantBleed effect : participant.getEffects(Bleed.registration)) {
			if (effect.getDamage() > 0.25) {
				effect.setDamage(0.25);
			}
			effect.setCount(Math.max(3, (int) Math.ceil(10.0 / effect.getPeriod())));
		}
	}

	@Override
	protected void onEnd() {
		super.onEnd();
		participant.removeEffects(Bleed.registration);
	}
}
