package daybreak.abilitywar.game.manager.effect;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.game.AbstractGame.Effect;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.effect.registry.ApplicationMethod;
import daybreak.abilitywar.game.manager.effect.registry.EffectManifest;
import daybreak.abilitywar.game.manager.effect.registry.EffectRegistry;
import daybreak.abilitywar.game.manager.effect.registry.EffectRegistry.EffectRegistration;
import daybreak.abilitywar.game.manager.effect.registry.EffectType;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Random;

@EffectManifest(name = "감염", displayName = "§5감염", method = ApplicationMethod.UNIQUE_STACK, type = {
		EffectType.SIGHT_CONTROL
}, description = {
		"간헐적으로 시야가 임의의 방향으로 돌아가며, 대미지를 25% 줄여받습니다.",
		"감염 효과를 중복으로 받으면 지속 시간이 쌓입니다."
})
public class Infection extends Effect implements Listener {

	public static final EffectRegistration<Infection> registration = EffectRegistry.registerEffect(Infection.class);

	public static void apply(Participant participant, TimeUnit timeUnit, int duration) {
		registration.apply(participant, timeUnit, duration);
	}

	private static final Random random = new Random();

	private final Participant participant;

	public Infection(final Participant participant, final TimeUnit timeUnit, final int duration) {
		participant.getGame().super(registration, participant, timeUnit.toTicks(duration) / 4);
		this.participant = participant;
		setPeriod(TimeUnit.TICKS, 4);
	}

	@EventHandler
	private void onPlayerDeath(PlayerDeathEvent e) {
		if (participant.getPlayer().getUniqueId().equals(e.getEntity().getUniqueId())) {
			stop(false);
		}
	}

	@EventHandler
	private void onEntityDamage(final EntityDamageEvent e) {
		if (participant.getPlayer().equals(e.getEntity())) {
			e.setDamage(e.getDamage() * .75);
		}
	}

	@Override
	protected void onStart() {
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
	}

	@Override
	protected void run(int count) {
		super.run(count);
		if (Math.random() <= 0.65) {
			final Player player = participant.getPlayer();
			final Location location = player.getLocation();
			float yaw = location.getYaw() + random.nextInt(130) - 65;
			if (yaw > 180 || yaw < -180) {
				float mod = yaw % 180;
				if (mod < 0) {
					yaw = 180 + mod;
				} else if (mod > 0) {
					yaw = -180 + mod;
				}
			}
			NMS.rotateHead(player, player, yaw, location.getPitch() + random.nextInt(90) - 45);
		}
	}

	@Override
	protected void onEnd() {
		HandlerList.unregisterAll(this);
		super.onEnd();
	}

	@Override
	protected void onSilentEnd() {
		HandlerList.unregisterAll(this);
		super.onSilentEnd();
	}

}