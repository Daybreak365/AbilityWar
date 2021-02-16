package daybreak.abilitywar.game.manager.effect;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.effect.Bleed.ParticipantBleed;
import daybreak.abilitywar.game.manager.effect.registry.ApplicationMethod;
import daybreak.abilitywar.game.manager.effect.registry.EffectManifest;
import daybreak.abilitywar.game.manager.effect.registry.EffectRegistry;
import daybreak.abilitywar.game.manager.effect.registry.EffectRegistry.EffectRegistration;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

@EffectManifest(name = "혈사병", displayName = "§4혈사병", method = ApplicationMethod.UNIQUE_LONGEST, description = {
		"혈사병 지속 중 플레이어가 가지고 있는 모든 출혈 효과가 멈추지 않습니다.",
		"움직일 경우 최대 2초만큼 지속 시간이 증가하며, 혈사병이 종료되면",
		"모든 출혈 효과가 함께 사라집니다."
})
public class Hemophilia extends AbstractGame.Effect implements Listener {

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
	protected void onStart() {
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
	}

	@Override
	protected void run(int count) {
		super.run(count);
		for (ParticipantBleed effect : participant.getEffects(Bleed.registration)) {
			if (effect.getDamage() > 0.6) {
				effect.setDamage(0.6);
			}
			effect.setCount(Math.max(3, (int) Math.ceil(10.0 / effect.getPeriod())));
		}
	}

	private long lastMillis = System.currentTimeMillis();
	private int extended = 0;

	@EventHandler
	private void onPlayerMove(final PlayerMoveEvent e) {
		if (participant.getPlayer().equals(e.getPlayer())) {
			final Location from = e.getFrom(), to = e.getTo();
			if (to == null || (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ())) return;
			final long current = System.currentTimeMillis();
			if (current - lastMillis >= 750) {
				setCount(getCount() + 4);
				this.lastMillis = current;
				if (++extended == 2) {
					HandlerList.unregisterAll(this);
				}
			}
		}
	}

	@Override
	protected void onEnd() {
		super.onEnd();
		participant.removeEffects(Bleed.registration);
		HandlerList.unregisterAll(this);
	}

	@Override
	protected void onSilentEnd() {
		super.onSilentEnd();
		HandlerList.unregisterAll(this);
	}
}
