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
import daybreak.abilitywar.utils.library.PotionEffects;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

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
	}

	private Vector createDirection() {
		return participant.getPlayer().getLocation().toVector().subtract(from.getLocation().toVector());
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
		final Vector direction = createDirection();
		NMS.rotateHead(participant.getPlayer(), participant.getPlayer(), LocationUtil.getYaw(direction), LocationUtil.getPitch(direction));
		PotionEffects.BLINDNESS.addPotionEffect(participant.getPlayer(), 2, 1, true);
	}

	@Override
	protected void onEnd() {
		hologram.remove();
		HandlerList.unregisterAll(this);
		super.onEnd();
	}

	@Override
	protected void onSilentEnd() {
		hologram.remove();
		HandlerList.unregisterAll(this);
		super.onSilentEnd();
	}

}
