package daybreak.abilitywar.game.manager.effect;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.effect.registry.ApplicationMethod;
import daybreak.abilitywar.game.manager.effect.registry.EffectManifest;
import daybreak.abilitywar.game.manager.effect.registry.EffectRegistry;
import daybreak.abilitywar.game.manager.effect.registry.EffectRegistry.EffectRegistration;
import daybreak.abilitywar.game.manager.effect.registry.EffectType;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.base.minecraft.version.NMSVersion;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

@EffectManifest(name = "제압", displayName = "§c제압", method = ApplicationMethod.UNIQUE_LONGEST, type = {
		EffectType.ABILITY_RESTRICTION
}, description = {
		"능력을 비활성화합니다."
})
public class Oppress extends AbstractGame.Effect implements Listener {

	public static final EffectRegistration<Oppress> registration = EffectRegistry.registerEffect(Oppress.class);

	public static void apply(Participant participant, TimeUnit timeUnit, int duration) {
		registration.apply(participant, timeUnit, duration);
	}

	private final Participant participant;
	private final ArmorStand hologram;
	private int stack = 0;
	private boolean direction = true;

	public Oppress(Participant participant, TimeUnit timeUnit, int duration) {
		participant.getGame().super(registration, participant, timeUnit.toTicks(duration));
		this.participant = participant;
		final Player player = participant.getPlayer();
		this.hologram = player.getWorld().spawn(player.getLocation(), ArmorStand.class);
		hologram.setVisible(false);
		hologram.setGravity(false);
		hologram.setInvulnerable(true);
		NMS.removeBoundingBox(hologram);
		hologram.setCustomNameVisible(true);
		hologram.setCustomName("§c제압됨");
		setPeriod(TimeUnit.TICKS, 1);
	}

	@Override
	protected void onStart() {
		restrict(true);
	}

	private void restrict(boolean restrict) {
		final AbilityBase ability = participant.getAbility();
		if (ability != null) ability.setRestricted(restrict);
	}

	@Override
	protected void run(int count) {
		super.run(count);
		if (hologram.isValid()) {
			if (direction) stack++;
			else stack--;
			if (stack <= 0 || stack >= 30) {
				this.direction = !direction;
			}
			final Location location = participant.getPlayer().getLocation().clone().add(0, (ServerVersion.isAboveOrEqual(NMSVersion.v1_17_R1) ? 4 : 2) + (stack * 0.008), 0);
			hologram.teleport(location);
		}
		restrict(true);
	}

	@Override
	protected void onEnd() {
		hologram.remove();
		HandlerList.unregisterAll(this);
		restrict(false);
		super.onEnd();
	}

	@Override
	protected void onSilentEnd() {
		hologram.remove();
		HandlerList.unregisterAll(this);
		restrict(false);
		super.onSilentEnd();
	}

}
