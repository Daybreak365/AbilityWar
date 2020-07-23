package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

@Beta
@AbilityManifest(name = "유령", rank = Rank.A, species = Species.OTHERS, explain = {
		"BETA"
})
public class Ghost extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_INCREASE_CONFIG = abilitySettings.new SettingObject<Integer>(Ghost.class, "CooldownIncrease", 3,
			"# 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};

	private static final Vector ZERO_VECTOR = new Vector();

	public Ghost(Participant participant) throws IllegalStateException {
		super(participant);
	}

	private final int cooldownIncrease = COOLDOWN_INCREASE_CONFIG.getValue();
	private Location targetLocation;
	private final AbilityTimer skill = new AbilityTimer() {
		private GameMode originalMode;
		private float flySpeed;

		@Override
		protected void onStart() {
			this.originalMode = getPlayer().getGameMode();
			this.flySpeed = getPlayer().getFlySpeed();
			getParticipant().attributes().TARGETABLE.setValue(false);
			getPlayer().setGameMode(GameMode.SPECTATOR);
		}

		@Override
		protected void run(int count) {
			if (getPlayer().getSpectatorTarget() != null) getPlayer().setSpectatorTarget(null);
			getPlayer().setFlySpeed(0f);
			if (targetLocation != null && count <= 30) {
				final Location playerLocation = getPlayer().getLocation();
				getPlayer().setVelocity(validateVector(targetLocation.toVector().subtract(playerLocation.toVector()).multiply(0.5)));
				if (playerLocation.distanceSquared(targetLocation) < 1) {
					stop(false);
					getPlayer().teleport(targetLocation.setDirection(getPlayer().getLocation().getDirection()));
				}
			} else {
				stop(true);
				if (targetLocation != null)
					getPlayer().teleport(targetLocation.setDirection(getPlayer().getLocation().getDirection()));
			}
		}

		@Override
		protected void onEnd() {
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			getPlayer().setGameMode(originalMode);
			getPlayer().setVelocity(ZERO_VECTOR);
			getPlayer().setFlySpeed(flySpeed);
			getParticipant().attributes().TARGETABLE.setValue(true);
		}
	}.setPeriod(TimeUnit.TICKS, 1).register();
	private final Cooldown cooldownTimer = new Cooldown(0);
	private int currentCooldown = 0;

	@SubscribeEvent(onlyRelevant = true)
	private void onMove(PlayerMoveEvent e) {
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onGameModeChange(PlayerGameModeChangeEvent e) {
		if (skill.isRunning() && getPlayer().getGameMode() == GameMode.SPECTATOR) e.setCancelled(true);
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onPlayerTeleport(PlayerTeleportEvent e) {
		if (skill.isRunning() && getPlayer().getGameMode() == GameMode.SPECTATOR) e.setCancelled(true);
	}

	private static Vector validateVector(Vector vector) {
		if (Math.abs(vector.getX()) > Double.MAX_VALUE) vector.setX(0);
		if (Math.abs(vector.getY()) > Double.MAX_VALUE) vector.setY(0);
		if (Math.abs(vector.getZ()) > Double.MAX_VALUE) vector.setZ(0);
		return vector;
	}

	@Override
	public boolean ActiveSkill(Material material, ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !skill.isRunning()) {
			if (!cooldownTimer.isCooldown()) {
				Block lastEmpty = null;
				try {
					for (BlockIterator iterator = new BlockIterator(getPlayer().getWorld(), getPlayer().getLocation().toVector(), getPlayer().getLocation().getDirection(), 1, 7); iterator.hasNext(); ) {
						Block block = iterator.next();
						if (!block.getType().isSolid()) {
							lastEmpty = block;
						}
					}
				} catch (IllegalStateException ignored) {
				}
				if (lastEmpty != null) {
					this.targetLocation = lastEmpty.getLocation();
					skill.start();
					cooldownTimer.setCooldown(currentCooldown += cooldownIncrease);
					cooldownTimer.start();
					return true;
				} else {
					getPlayer().sendMessage(ChatColor.RED + "바라보는 방향에 이동할 수 있는 곳이 없습니다.");
				}
			}
		}
		return false;
	}

}
