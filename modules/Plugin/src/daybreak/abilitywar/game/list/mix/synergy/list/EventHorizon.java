package daybreak.abilitywar.game.list.mix.synergy.list;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;
import daybreak.abilitywar.game.manager.object.DeathManager;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.geometry.Sphere;
import daybreak.abilitywar.utils.base.minecraft.FallingBlocks;
import daybreak.abilitywar.utils.base.minecraft.FallingBlocks.Behavior;
import daybreak.abilitywar.utils.base.minecraft.entity.decorator.Deflectable;
import daybreak.abilitywar.utils.library.BlockX;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
import java.util.LinkedList;
import java.util.function.Predicate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;

@AbilityManifest(name = "사건의 지평선", rank = Rank.S, species = Species.GOD, explain = {
		"철괴를 우클릭하면 $[DurationConfig]초간 짙은 암흑 속으로 주변의 모든 생명체와",
		"투사체, 블록들을 강한 힘으로 끌어당깁니다. $[COOLDOWN_CONFIG]"
})
public class EventHorizon extends Synergy implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = synergySettings.new SettingObject<Integer>(EventHorizon.class, "Cooldown", 80,
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

	public static final SettingObject<Integer> DurationConfig = synergySettings.new SettingObject<Integer>(EventHorizon.class, "Duration", 8,
			"# 능력 지속 시간") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};
	private static final RGB BLACK = RGB.of(1, 1, 1);
	private static final Sphere sphere = Sphere.of(10, 10);
	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());
	private final Predicate<Entity> predicate = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			if (entity.equals(getPlayer())) return false;
			if (entity instanceof Player) {
				if (!getGame().isParticipating(entity.getUniqueId())
						|| (getGame() instanceof DeathManager.Handler && ((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
						|| !getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue()) {
					return false;
				}
				if (getGame() instanceof Teamable) {
					final Teamable teamGame = (Teamable) getGame();
					final Participant entityParticipant = teamGame.getParticipant(entity.getUniqueId()), participant = getParticipant();
					return !teamGame.hasTeam(entityParticipant) || !teamGame.hasTeam(participant) || (!teamGame.getTeam(entityParticipant).equals(teamGame.getTeam(participant)));
				}
			}
			return true;
		}
	};
	private Location center;
	private final Duration skill = new Duration(DurationConfig.getValue() * 20, cooldownTimer) {

		private final int distance = 10;
		private LinkedList<Block> blocks;

		@Override
		public void onDurationStart() {
			center = getPlayer().getLocation();
			this.blocks = new LinkedList<>(LocationUtil.getBlocks3D(center, distance, false, false));
		}

		@Override
		public void onDurationProcess(int seconds) {
			ParticleLib.SMOKE_LARGE.spawnParticle(center, 2, 2, 2, 40);
			sphere.rotateAroundAxisX(3);
			sphere.rotateAroundAxisY(3);
			sphere.rotateAroundAxisZ(3);
			for (Location loc : sphere.toLocations(center)) {
				ParticleLib.REDSTONE.spawnParticle(loc, BLACK);
			}
			for (Entity entity : LocationUtil.getNearbyEntities(Entity.class, center, distance, distance, predicate)) {
				if (!entity.equals(getPlayer()) && !entity.hasMetadata("EventHorizon")) {
					if (entity instanceof Damageable) ((Damageable) entity).damage(1);
					entity.setVelocity(center.toVector().subtract(entity.getLocation().toVector()).multiply(1.2));
				}
			}
			for (Deflectable deflectable : LocationUtil.getNearbyCustomEntities(Deflectable.class, center, distance, distance, null)) {
				deflectable.onDeflect(getParticipant(), center.toVector().subtract(deflectable.getLocation().toVector()).multiply(0.3));
			}
			if (!blocks.isEmpty()) {
				for (int i = 0; i < 15; i++) {
					if (blocks.isEmpty()) break;
					Block block = blocks.remove();
					if (!BlockX.isIndestructible(block.getType())) {
						FallingBlocks.spawnFallingBlock(block, false, center.toVector().subtract(block.getLocation().toVector()), Behavior.TRUE).setMetadata("EventHorizon", new FixedMetadataValue(AbilityWar.getPlugin(), null));
						block.setType(Material.AIR);
					}
				}
			}
		}

	}.setPeriod(TimeUnit.TICKS, 1);

	public EventHorizon(Participant participant) {
		super(participant);
	}

	@SubscribeEvent
	private void onMove(PlayerMoveEvent e) {
		if (skill.isRunning() && center != null && !e.getPlayer().equals(getPlayer())) {
			if (e.getFrom().distanceSquared(center) <= 100 && e.getTo().distanceSquared(center) > 100) {
				e.setTo(e.getFrom());
			}
		}
	}

	@Override
	public boolean ActiveSkill(Material material, ClickType clickType) {
		if (material == Material.IRON_INGOT) {
			if (clickType.equals(ClickType.RIGHT_CLICK)) {
				if (!skill.isDuration() && !cooldownTimer.isCooldown()) {
					skill.start();
					return true;
				}
			}
		}

		return false;
	}

}
