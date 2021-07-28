package daybreak.abilitywar.game.manager.effect;

import daybreak.abilitywar.AbilityWar;
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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

@EffectManifest(name = "기절", displayName = "§e기절", method = ApplicationMethod.UNIQUE_LONGEST, type = {
		EffectType.MOVEMENT_RESTRICTION
}, description = {
		"이동과 시야 전환이 불가능해집니다."
})
public class Stun extends AbstractGame.Effect implements Listener {

	public static final EffectRegistration<Stun> registration = EffectRegistry.registerEffect(Stun.class);

	public static void apply(Participant participant, TimeUnit timeUnit, int duration) {
		registration.apply(participant, timeUnit, duration);
	}

	private final Participant participant;
	private final ArmorStand hologram;
	private int stack = 0;
	private boolean direction = true;

	public Stun(Participant participant, TimeUnit timeUnit, int duration) {
		participant.getGame().super(registration, participant, timeUnit.toTicks(duration));
		this.participant = participant;
		final Player player = participant.getPlayer();
		this.hologram = player.getWorld().spawn(player.getLocation(), ArmorStand.class);
		hologram.setVisible(false);
		hologram.setGravity(false);
		hologram.setInvulnerable(true);
		NMS.removeBoundingBox(hologram);
		hologram.setCustomNameVisible(true);
		hologram.setCustomName("§c기절!");
		setPeriod(TimeUnit.TICKS, 1);
	}

	@Override
	protected void onStart() {
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
	}

	@EventHandler
	private void onPlayerMove(final PlayerMoveEvent e) {
		if (e.getPlayer().getUniqueId().equals(participant.getPlayer().getUniqueId())) {
			e.setTo(e.getFrom());
		}
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
