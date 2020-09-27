package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.Tips;
import daybreak.abilitywar.ability.Tips.Description;
import daybreak.abilitywar.ability.Tips.Difficulty;
import daybreak.abilitywar.ability.Tips.Level;
import daybreak.abilitywar.ability.Tips.Stats;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.geometry.Boundary.BoundingBox;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.function.Predicate;

@AbilityManifest(name = "고슴도치", rank = Rank.C, species = Species.ANIMAL, explain = {
		"가시로 가까이에 있는 모든 생명체를 찌릅니다.",
		"아야! 그것 참 아프겠네요."
})
@Tips(tip = {
		"아야! 상대를 당신의 뾰족한 가시로 찔러 아프게 하세요."
}, strong = {
		@Description(subject = "좁은 공간", explain = {
				"한두 칸의 좁은 공간에서는 고슴도치가 상대를 지속적으로 공격할 수 있기",
				"때문에, 그나마 강력해집니다."
		})
}, stats = @Stats(offense = Level.THREE, survival = Level.ZERO, crowdControl = Level.ZERO, mobility = Level.ZERO, utility = Level.ZERO), difficulty = Difficulty.EASY)
public class Hedgehog extends AbilityBase {

	public static final SettingObject<Double> DAMAGE_CONFIG = abilitySettings.new SettingObject<Double>(Hedgehog.class, "Damage", 2.0,
			"# 대미지") {

		@Override
		public boolean condition(Double value) {
			return value >= 0;
		}

	};

	public Hedgehog(Participant participant) {
		super(participant);
	}

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

	private final BoundingBox boundingBox = new BoundingBox() {
		private double minX = -0.600000012, minY = 0, minZ = -0.600000012, maxX = 0.6500000012, maxY = 1.799999952, maxZ = 0.600000012;

		@Override
		public double getMinX() {
			return getCenter().getX() + minX;
		}

		@Override
		public double getMinY() {
			return getCenter().getY() + minY;
		}

		@Override
		public double getMinZ() {
			return getCenter().getZ() + minZ;
		}

		@Override
		public double getMaxX() {
			return getCenter().getX() + maxX;
		}

		@Override
		public double getMaxY() {
			return getCenter().getY() + maxY;
		}

		@Override
		public double getMaxZ() {
			return getCenter().getZ() + maxZ;
		}

		@Override
		public BoundingBox expand(double negativeX, double negativeY, double negativeZ, double positiveX, double positiveY, double positiveZ) {
			if (negativeX == 0.0D && negativeY == 0.0D && negativeZ == 0.0D && positiveX == 0.0D && positiveY == 0.0D && positiveZ == 0.0D) {
				return this;
			}
			double newMinX = minX - negativeX, newMinY = minY - negativeY, newMinZ = minZ - negativeZ, newMaxX = maxX + positiveX, newMaxY = maxY + positiveY, newMaxZ = maxZ + positiveZ;

			if (newMinX > newMaxX) {
				final double centerX = getPlayer().getLocation().getX();
				if (newMaxX >= centerX) {
					newMinX = newMaxX;
				} else if (newMinX <= centerX) {
					newMaxX = newMinX;
				} else {
					newMinX = centerX;
					newMaxX = centerX;
				}
			}
			if (newMinY > newMaxY) {
				final double centerY = getPlayer().getLocation().getY();
				if (newMaxY >= centerY) {
					newMinY = newMaxY;
				} else if (newMinY <= centerY) {
					newMaxY = newMinY;
				} else {
					newMinY = centerY;
					newMaxY = centerY;
				}
			}
			if (newMinZ > newMaxZ) {
				final double centerZ = getPlayer().getLocation().getZ();
				if (newMaxZ >= centerZ) {
					newMinZ = newMaxZ;
				} else if (newMinZ <= centerZ) {
					newMaxZ = newMinZ;
				} else {
					newMinZ = centerZ;
					newMaxZ = centerZ;
				}
			}

			this.minX = newMinX;
			this.minY = newMinY;
			this.minZ = newMinZ;
			this.maxX = newMaxX;
			this.maxY = newMaxY;
			this.maxZ = newMaxZ;
			return this;
		}

		@Override
		public Location getCenter() {
			return getPlayer().getLocation();
		}

	};

	private final AbilityTimer passive = new AbilityTimer() {
		@Override
		protected void run(int count) {
			if (!getPlayer().isDead()) {
				final double damage = DAMAGE_CONFIG.getValue();
				for (LivingEntity livingEntity : LocationUtil.getConflictingEntities(LivingEntity.class, boundingBox, predicate)) {
					livingEntity.setNoDamageTicks(0);
					livingEntity.damage(damage, getPlayer());
				}
			}
		}
	}.setPeriod(TimeUnit.TICKS, 7).register();

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			passive.start();
			NMS.setArrowsInBody(getPlayer(), 25);
		}
	}

}
