package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.function.Predicate;

@AbilityManifest(name = "피해망상", rank = Rank.C, species = Species.HUMAN, explain = {
		"§7패시브 §8- §c망상§f: 주변의 모든 플레이어가 자신을 쳐다보는 것 같이 보입니다.",
		"§7패시브 §8- §c도피§f: 다른 플레이어가 자신에게 다가오면 대상으로부터 멀어지는",
		"방향으로 조금 돌진합니다."
})
@Beta
public class Paranoia extends AbilityBase {

	private final Predicate<Entity> ONLY_PARTICIPANTS = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			return (!(entity instanceof Player)) || (getGame().isParticipating(entity.getUniqueId())
					&& (!(getGame() instanceof DeathManager.Handler) || !((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
					&& getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue());
		}
	};
	private final AbilityTimer passive = new AbilityTimer() {
		@Override
		protected void run(int count) {
			final Location playerLocation = getPlayer().getLocation();
			for (final Player target : LocationUtil.getNearbyEntities(Player.class, playerLocation, 20, 20, ONLY_PARTICIPANTS)) {
				if (getPlayer().equals(target)) continue;
				final Location targetLocation = target.getLocation();
				final double x = playerLocation.getX() - targetLocation.getX(), y = playerLocation.getY() - targetLocation.getY(), z = playerLocation.getZ() - targetLocation.getZ();
				final float yaw = (float) Math.toDegrees((Math.atan2(-x, z) + 6.283185307179586D) % 6.283185307179586D);
				if (x == 0.0D && z == 0.0D) NMS.rotateHead(getPlayer(), target, yaw, y > 0.0D ? -90 : 90);
				else
					NMS.rotateHead(getPlayer(), target, yaw, (float) Math.toDegrees(Math.atan(-y / Math.sqrt((x * x) + (z * z)))));
			}
		}
	}.setPeriod(TimeUnit.TICKS, 1);

	public Paranoia(Participant participant) {
		super(participant);
	}

	@SubscribeEvent
	private void onPlayerMove(final PlayerMoveEvent e) {
		final Player target = e.getPlayer();
		final Location toLocation = e.getTo();
		if (getPlayer().equals(target) || !target.getWorld().equals(getPlayer().getWorld()) || toLocation == null) return;
		final Location playerLocation = getPlayer().getLocation(), fromLocation = e.getFrom();
		final double fromDistance = playerLocation.distanceSquared(fromLocation), toDistance = playerLocation.distanceSquared(toLocation);
		if (toDistance <= 36 && toDistance < fromDistance) {
			getPlayer().setVelocity(new Vector(playerLocation.getX() - toLocation.getX(), playerLocation.getY() - toLocation.getY(), playerLocation.getZ() - toLocation.getZ()).normalize().multiply(.15).setY(getPlayer().getVelocity().getY()));
		}
	}

	@Override
	protected void onUpdate(Update update) {
		passive.start();
	}

}
