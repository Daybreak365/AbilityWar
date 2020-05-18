package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.LocationUtil.Locations;
import daybreak.abilitywar.utils.base.math.LocationUtil.Predicates;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.base.math.geometry.Line;
import daybreak.abilitywar.utils.base.math.geometry.Sphere;
import daybreak.abilitywar.utils.base.math.geometry.location.LocationIterator;
import daybreak.abilitywar.utils.base.minecraft.DamageUtil;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

@AbilityManifest(name = "영혼수확자", rank = Rank.S, species = Species.GOD, explain = {
		"생명체가 사망하면 그 위치에 1분간 영혼이 남습니다.",
		"영혼에 다가가면 수확하며, 영혼 주인의 최대 체력에 비례해 내 체력을 회복합니다.",
		"철괴를 우클릭하면 주변 $[DistanceConfig]칸 안의 모든 플레이어를 7초간 끌어당기며,",
		"생명력이 약할수록 더욱 빠르게 끌어당깁니다. $[CooldownConfig]"
})
@Beta
public class SoulReaper extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> CooldownConfig = abilitySettings.new SettingObject<Integer>(SoulReaper.class, "Cooldown", 180,
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

	public static final SettingObject<Integer> DistanceConfig = abilitySettings.new SettingObject<Integer>(SoulReaper.class, "Distance", 7,
			"# 능력 거리 설정") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};
	private final Map<Participant, ExecuteTimer> executes = new HashMap<>();

	private static final RGB BLACK = RGB.of(1, 1, 1);
	private static final RGB SOUL_COLOUR = RGB.of(1, 17, 48);

	private final int distance = DistanceConfig.getValue();

	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());
	private final Predicate<Entity> STRICT = Predicates.STRICT(getPlayer());
	private final Sphere sphere = Sphere.of(0.07, 4);
	private final Circle circle = Circle.of(distance, 100);
	private final DurationTimer skillTimer = new DurationTimer(140, cooldownTimer) {
		private Location center;
		private Locations locations;

		@Override
		protected void onDurationStart() {
			this.center = getPlayer().getLocation();
			this.locations = circle.toLocations(center);
		}

		@Override
		protected void onDurationProcess(int count) {
			for (Location loc : locations) {
				ParticleLib.REDSTONE.spawnParticle(loc, BLACK);
			}
			for (Player player : LocationUtil.getEntitiesInCircle(Player.class, center, distance, STRICT)) {
				if (count % (int) (((player.getHealth() / player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) * 10) + 3) == 0) {
					try {
						player.setVelocity(center.toVector().subtract(player.getLocation().toVector()).normalize().multiply(0.35).setY(player.getVelocity().getY()));
					} catch (Exception ignored) {
					}
					for (LocationIterator iterator = Line.iteratorBetween(player.getLocation(), center, 7); iterator.hasNext(); ) {
						ParticleLib.REDSTONE.spawnParticle(iterator.next(), BLACK);
					}
				}
			}
		}

		@Override
		protected void onDurationEnd() {
			for (Player player : LocationUtil.getEntitiesInCircle(Player.class, center, distance, STRICT)) {
				if (!DamageUtil.canDamage(getPlayer(), player, DamageCause.MAGIC, 1000)) continue;
				Participant participant = getGame().getParticipant(player);
				if (!executes.containsKey(participant)) {
					new ExecuteTimer(participant).start();
				}
			}
		}
	}.setPeriod(TimeUnit.TICKS, 1);

	public SoulReaper(AbstractGame.Participant participant) {
		super(participant);
	}

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT)) {
			if (clickType.equals(ClickType.RIGHT_CLICK) && !skillTimer.isDuration() && !cooldownTimer.isCooldown()) {
				skillTimer.start();
			}
		}
		return false;
	}

	private void gainHealth(double amount) {
		if (!getPlayer().isDead()) {
			getPlayer().setHealth(Math.min(getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), getPlayer().getHealth() + amount));
		}
	}

	@SubscribeEvent
	private void onPlayerDeath(PlayerDeathEvent e) {
		if (getGame().isParticipating(e.getEntity()) && !executes.containsKey(getGame().getParticipant(e.getEntity())) && !getPlayer().equals(e.getEntity())) {
			Locations locations = sphere.toLocations(e.getEntity().getLocation().clone().add(0, 1, 0));
			new Timer(1200) {
				@Override
				protected void run(int count) {
					for (Location location : locations) {
						ParticleLib.REDSTONE.spawnParticle(getPlayer(), location, SOUL_COLOUR);
						if (location.distanceSquared(getPlayer().getLocation()) <= 1.2) {
							stop(false);
							gainHealth(8);
							break;
						}
					}
				}
			}.setPeriod(TimeUnit.TICKS, 1).start();
		}
	}

	@SubscribeEvent
	private void onEntityDeath(EntityDeathEvent e) {
		Locations locations = sphere.toLocations(e.getEntity().getLocation().clone().add(0, 1, 0));
		new Timer(1200) {
			@Override
			protected void run(int count) {
				for (Location location : locations) {
					ParticleLib.REDSTONE.spawnParticle(getPlayer(), location, SOUL_COLOUR);
					if (location.distanceSquared(getPlayer().getLocation()) <= 1.2) {
						stop(false);
						double soul = (int) e.getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
						if (e.getEntity() instanceof Animals) soul /= 7.0;
						else if (e.getEntity() instanceof Monster) soul /= 5.0;
						gainHealth(soul);
						break;
					}
				}
			}
		}.setPeriod(TimeUnit.TICKS, 1).start();
	}

	private class ExecuteTimer extends Timer {

		private final Participant target;

		private ExecuteTimer(Participant target) {
			this.target = target;
			setPeriod(TimeUnit.TICKS, 3);
		}

		@Override
		protected void onStart() {
			executes.put(target, this);
		}

		@Override
		protected void run(int count) {
			final double newHealth = Math.max(0.0, target.getPlayer().getHealth() - 1.0);
			target.getPlayer().setHealth(newHealth);
			gainHealth(0.5);
			if (newHealth == 0.0 || getPlayer().isDead()) {
				stop(false);
			}
		}

		@Override
		protected void onEnd() {
			executes.remove(target);
		}

		@Override
		protected void onSilentEnd() {
			executes.remove(target);
		}

	}

}
