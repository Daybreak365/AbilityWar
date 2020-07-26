package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.object.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
import java.util.function.Predicate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@AbilityManifest(name = "카오스", rank = Rank.S, species = Species.GOD, explain = {
		"태초의 신 카오스.",
		"철괴를 우클릭하면 $[DurationConfig]초간 짙은 암흑 속으로 주변",
		"$[DurationConfig]칸 이내의 모든 물체와 생명체들을 끌어당기며",
		"대미지를 줍니다. $[COOLDOWN_CONFIG]"
})
public class Chaos extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Chaos.class, "Cooldown", 100,
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

	public static final SettingObject<Integer> DurationConfig = abilitySettings.new SettingObject<Integer>(Chaos.class, "Duration", 5,
			"# 능력 지속 시간") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	public static final SettingObject<Integer> DistanceConfig = abilitySettings.new SettingObject<Integer>(Chaos.class, "Distance", 5,
			"# 거리 설정") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	public Chaos(Participant participant) {
		super(participant);
	}

	private static final RGB BLACK = RGB.of(1, 1, 1);
	private final int distance = DistanceConfig.getValue();
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
	private final Circle CIRCLE = Circle.of(distance, distance * 4);
	private final Duration skill = new Duration(DurationConfig.getValue() * 20, cooldownTimer) {

		private Location center;
		private Circle pCircle, sCircle;

		@Override
		public void onDurationStart() {
			this.center = getPlayer().getLocation();
			this.pCircle = CIRCLE.clone();
			this.sCircle = CIRCLE.clone();
		}

		@Override
		public void onDurationProcess(int seconds) {
			ParticleLib.SMOKE_LARGE.spawnParticle(center, 1, 1, 1, 50, 0.05);
			for (Location loc : pCircle.rotateAroundAxisX(-5).rotateAroundAxisZ(5).rotateAroundAxisY(3).toLocations(center)) {
				ParticleLib.REDSTONE.spawnParticle(loc, BLACK);
			}
			for (Location loc : sCircle.rotateAroundAxisX(5).rotateAroundAxisZ(-5).rotateAroundAxisY(-6).toLocations(center)) {
				ParticleLib.REDSTONE.spawnParticle(loc, BLACK);
			}
			for (Entity entity : LocationUtil.getNearbyEntities(Entity.class, center, distance, distance, predicate)) {
				if (entity instanceof Damageable) ((Damageable) entity).damage(1);
				entity.setVelocity(center.toVector().subtract(entity.getLocation().toVector()).multiply(0.7));
			}
		}

	}.setPeriod(TimeUnit.TICKS, 1);

	@Override
	public boolean ActiveSkill(Material material, ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !skill.isDuration() && !cooldownTimer.isCooldown()) {
			skill.start();
			return true;
		}
		return false;
	}

}
