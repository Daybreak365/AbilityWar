package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.annotations.Support;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.NumberUtil;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.base.minecraft.FireworkUtil;
import daybreak.abilitywar.utils.base.minecraft.version.NMSVersion;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
import daybreak.abilitywar.utils.library.PotionEffects;
import org.bukkit.Color;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.FireworkExplodeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

@Support.Version(min = NMSVersion.v1_11_R1)
@AbilityManifest(name = "쇼맨쉽", rank = Rank.B, species = Species.HUMAN, explain = {
		"주변 7칸 이내에 있는 생명체 수에 따라 효과를 받습니다.",
		"플레이어는 1명, 플레이어가 아닌 생명체는 $[ENTITY_COUNT]명 취급합니다.",
		"§a0명 이상, 2명 미만 §7: §f나약함  §a2명 이상, 4명 미만 §7: §f힘 II",
		"§a4명 이상 §7: §f힘 III 및 체력이 30% 미만인 적 처형"
})
public class ShowmanShip extends AbilityBase {

	public static final SettingObject<Float> ENTITY_COUNT = abilitySettings.new SettingObject<Float>(ShowmanShip.class, "entity-count", .2f,
			"# 플레이어가 아닌 엔티티를 몇 명으로 취급할지 설정합니다.") {

		@Override
		public boolean condition(Float value) {
			return value >= 0;
		}

	};

	public ShowmanShip(Participant participant) {
		super(participant);
	}

	private static final Color[] colors = {
			Color.YELLOW, Color.RED, Color.ORANGE, Color.WHITE, Color.FUCHSIA
	};
	private static final Type[] types = {
			Type.BALL_LARGE, Type.STAR
	};
	private static final int radius = 7;
	private static final RGB WEAK = new RGB(214, 255, 212), POWER = new RGB(255, 184, 150), POWERFUL = new RGB(255, 59, 59);
	private static final Circle circle = Circle.of(radius, 100);

	private final float entityCount = ENTITY_COUNT.getValue();
	private final Map<Firework, LivingEntity> execution = new HashMap<>();
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
	private final Predicate<Entity> notEqualPredicate = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			if (entity.equals(getPlayer())) return false;
			return (!(entity instanceof Player)) || (getGame().isParticipating(entity.getUniqueId())
					&& (!(getGame() instanceof DeathManager.Handler) || !((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
					&& getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue());
		}
	};

	@SubscribeEvent
	private void onFireworkExplode(FireworkExplodeEvent e) {
		if (execution.containsKey(e.getEntity())) {
			Firework firework = e.getEntity();
			LivingEntity livingEntity = execution.get(firework);
			if (!livingEntity.isDead()) {
				livingEntity.damage(0, getPlayer());
				livingEntity.getWorld().createExplosion(livingEntity.getLocation(), 2);
				if (!livingEntity.isDead()) livingEntity.setHealth(0);
				Location location = livingEntity.getEyeLocation().clone().add(0, 0.5, 0);
				for (int i = 0; i < 4; i++) FireworkUtil.spawnRandomFirework(location, colors, colors, types, 1);
			}
			execution.remove(firework);
		}
	}

	private final AbilityTimer passive = new AbilityTimer() {

		@Override
		public void run(int count) {
			final double point = getPoint(7, 7);
			final RGB color;
			Location playerLocation = getPlayer().getLocation();
			if (point < 2) {
				PotionEffects.WEAKNESS.addPotionEffect(getPlayer(), 4, 0, true);
				color = WEAK;
			} else if (point >= 2 && point < 4) {
				PotionEffects.INCREASE_DAMAGE.addPotionEffect(getPlayer(), 4, 1, true);
				color = POWER;
			} else {
				PotionEffects.INCREASE_DAMAGE.addPotionEffect(getPlayer(), 4, 2, true);
				color = POWERFUL;
				for (LivingEntity livingEntity : LocationUtil.getEntitiesInCircle(LivingEntity.class, playerLocation, radius, predicate)) {
					if (livingEntity.getHealth() < (livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() / 3.3333333333) && !livingEntity.isDead()) {
						if (!execution.containsValue(livingEntity)) {
							Firework firework = FireworkUtil.spawnRandomFirework(livingEntity.getEyeLocation().clone().add(0, 0.5, 0), colors, colors, types, 1);
							firework.addPassenger(livingEntity);
							execution.put(firework, livingEntity);
						}
					}
				}
			}

			for (Location loc : circle.toLocations(playerLocation).floor(playerLocation.getY())) {
				ParticleLib.REDSTONE.spawnParticle(getPlayer(), loc, color);
			}
		}

	}.setPeriod(TimeUnit.TICKS, 1).register();

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			passive.start();
		}
	}

	private float getPoint(int horizontal, int vertical) {
		final Location center = getPlayer().getLocation();
		final double centerX = center.getX(), centerZ = center.getZ();
		float point = 0;
		for (Entity entity : LocationUtil.collectEntities(center, horizontal)) {
			final Location entityLocation = entity.getLocation();
			if (LocationUtil.distanceSquared2D(centerX, centerZ, entityLocation.getX(), entityLocation.getZ()) <= (horizontal * horizontal) && NumberUtil.subtract(center.getY(), entityLocation.getY()) <= vertical && (notEqualPredicate == null || notEqualPredicate.test(entity))) {
				if (entity instanceof Player) {
					point += 1f;
				} else if (entity instanceof LivingEntity) {
					point += entityCount;
				}
			}
		}
		return point;
	}

}
