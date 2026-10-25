package daybreak.abilitywar.game.manager.effect;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.effect.registry.ApplicationMethod;
import daybreak.abilitywar.game.manager.effect.registry.EffectConstructor;
import daybreak.abilitywar.game.manager.effect.registry.EffectManifest;
import daybreak.abilitywar.game.manager.effect.registry.EffectRegistry;
import daybreak.abilitywar.game.manager.effect.registry.EffectRegistry.EffectRegistration;
import daybreak.abilitywar.game.manager.effect.registry.EffectType;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.base.minecraft.version.NMSVersion;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@EffectManifest(name = "공포", displayName = "§b공포", method = ApplicationMethod.UNIQUE_LONGEST, type = {
		EffectType.MOVEMENT_RESTRICTION
}, description = {
		"시전자의 반대 방향을 바라보며, 이동 속도가 느려지고 실명합니다."
})
public class Fear extends AbstractGame.Effect implements Listener {

	public static final EffectRegistration<Fear> registration = EffectRegistry.registerEffect(Fear.class);

	public static void apply(Participant participant, TimeUnit timeUnit, int duration, Entity from) {
		registration.apply(participant, timeUnit, duration, "default", from);
	}

	private final Participant participant;
	private final ArmorStand hologram;
	private final Entity from;
	private int stack = 0;
	private boolean direction = true;
	private PotionEffect previousBlindness;
	private int elapsedTicks, blindnessTicks;
	private boolean ownsBlindness;

	@EffectConstructor(name = "default")
	public Fear(Participant participant, TimeUnit timeUnit, int duration, Entity from) {
		participant.getGame().super(registration, participant, timeUnit.toTicks(duration));
		this.participant = participant;
		final Player player = participant.getPlayer();
		this.hologram = player.getWorld().spawn(player.getLocation(), ArmorStand.class);
		hologram.setVisible(false);
		hologram.setGravity(false);
		hologram.setInvulnerable(true);
		NMS.removeBoundingBox(hologram);
		hologram.setCustomNameVisible(true);
		hologram.setCustomName("§b공포");
		this.from = from;
		setPeriod(TimeUnit.TICKS, 1);
	}

	@Override
	protected void onStart() {
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		previousBlindness = participant.getPlayer().getPotionEffect(PotionEffectType.BLINDNESS);
		applyBlindness();
	}

	private void applyBlindness() {
		final Player player = participant.getPlayer();
		final PotionEffect current = player.getPotionEffect(PotionEffectType.BLINDNESS);
		// Short effects never reach full fog on modern clients. Keep one continuous
		// effect with a fade-out margin, then remove only the effect we installed.
		final int duration = Math.max(1, getCount()) + 20;
		if (current != null && (current.getAmplifier() > 0 || current.getDuration() < 0 || current.getDuration() >= duration)) return;
		if (player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, duration, 0, true, false))) {
			ownsBlindness = true;
			blindnessTicks = duration;
		}
	}

	@Override
	protected void onCountSet() {
		super.onCountSet();
		applyBlindness();
	}

	private void clearBlindness() {
		final Player player = participant.getPlayer();
		final PotionEffect current = player.getPotionEffect(PotionEffectType.BLINDNESS);
		if (ownsBlindness && current != null && current.getAmplifier() == 0
				&& current.isAmbient() && !current.hasParticles()
				&& Math.abs(current.getDuration() - blindnessTicks) <= 2) {
			player.removePotionEffect(PotionEffectType.BLINDNESS);
			if (previousBlindness != null) {
				final int left = previousBlindness.getDuration() < 0 ? previousBlindness.getDuration() : previousBlindness.getDuration() - elapsedTicks;
				if (left != 0 && (left > 0 || previousBlindness.getDuration() < 0)) {
					player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, left, previousBlindness.getAmplifier(),
							previousBlindness.isAmbient(), previousBlindness.hasParticles()));
				}
			}
		}
		ownsBlindness = false;
	}

	private Vector createDirection() {
		return participant.getPlayer().getLocation().toVector().subtract(from.getLocation().toVector());
	}

	@Override
	protected void run(int count) {
		super.run(count);
		elapsedTicks++;
		if (ownsBlindness) blindnessTicks--;
		if (!participant.getPlayer().hasPotionEffect(PotionEffectType.BLINDNESS)) applyBlindness();
		if (hologram.isValid()) {
			if (direction) stack++;
			else stack--;
			if (stack <= 0 || stack >= 30) {
				this.direction = !direction;
			}
			final Location location = participant.getPlayer().getLocation().clone().add(0, (ServerVersion.isAboveOrEqual(NMSVersion.v1_17_R1) ? 4 : 2) + (stack * 0.008), 0);
			hologram.teleport(location);
		}
		final Vector direction = createDirection();
		NMS.rotateHead(participant.getPlayer(), participant.getPlayer(), LocationUtil.getYaw(direction), LocationUtil.getPitch(direction));
	}

	@Override
	protected void onEnd() {
		onSilentEnd();
	}

	@Override
	protected void onSilentEnd() {
		clearBlindness();
		hologram.remove();
		HandlerList.unregisterAll(this);
		super.onSilentEnd();
	}

}
