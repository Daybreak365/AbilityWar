package daybreak.abilitywar.ability.list;

import com.google.common.collect.ImmutableSet;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.FastMath;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.VectorUtil;
import daybreak.abilitywar.utils.base.minecraft.boundary.CenteredBoundingBox;
import daybreak.abilitywar.utils.base.minecraft.damage.Damages;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.library.item.EnchantLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

@AbilityManifest(name = "에오스", rank = Rank.A, species = Species.GOD, explain = {

})
@Beta
public class Eos extends AbilityBase implements ActiveHandler {

	private static final Component[] components = new Component[]{
			new Component(RGB.of(3, 102, 252), 2.8, 1.35),
			new Component(RGB.of(3, 102, 252), 2.8, 1.3),
			new Component(RGB.of(80, 118, 242), 2.8, 1.25),
			new Component(RGB.of(80, 118, 242), 2.8, 1.2),
			new Component(RGB.of(92, 183, 242), 2.8, 1.15),
			new Component(RGB.of(92, 183, 242), 2.8, 1.1),
			new Component(RGB.WHITE, 2.8, 1.05),
			new Component(RGB.WHITE, 2.8, 1)
	};

	private static final Set<Material> swords;

	static {
		if (MaterialX.NETHERITE_SWORD.isSupported()) {
			swords = ImmutableSet.of(MaterialX.WOODEN_SWORD.getMaterial(), Material.STONE_SWORD, Material.IRON_SWORD, MaterialX.GOLDEN_SWORD.getMaterial(), Material.DIAMOND_SWORD, MaterialX.NETHERITE_SWORD.getMaterial());
		} else {
			swords = ImmutableSet.of(MaterialX.WOODEN_SWORD.getMaterial(), Material.STONE_SWORD, Material.IRON_SWORD, MaterialX.GOLDEN_SWORD.getMaterial(), Material.DIAMOND_SWORD);
		}
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
	private boolean state = false;

	public Eos(Participant participant) {
		super(participant);
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onPlayerInteract(final PlayerInteractEvent e) {
		if (e.getItem() != null && swords.contains(e.getItem().getType())) {
			if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
				cut();
			} else if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				new Dash(getPlayer().getLocation().clone().add(0, 1, 0), getPlayer().getLocation().getDirection(), RGB.AQUA, RGB.WHITE).start();
			}
		}
	}

	@SubscribeEvent(ignoreCancelled = true)
	private void onEntityDamageByEntity(final EntityDamageByEntityEvent e) {
		if (getPlayer().equals(e.getDamager()) && e.getCause() == DamageCause.ENTITY_ATTACK) {
			e.setCancelled(true);
			if (swords.contains(getPlayer().getInventory().getItemInMainHand().getType())) {
				cut();
			}
		}
	}

	private long lastClick = System.currentTimeMillis();

	private void cut() {
		final long current = System.currentTimeMillis();
		if (current - lastClick >= 250) {
			this.lastClick = current;
			new Cut(getPlayer().getLocation().clone().add(0, 1.5, 0), (state = !state) ? 25 : -25, null).start();
			SoundLib.ENTITY_WITHER_SHOOT.playSound(getPlayer().getLocation());
		}
	}

	@Override
	public boolean ActiveSkill(Material material, ClickType clickType) {
		return false;
	}

	private static class Component {

		private final RGB color;
		private final double radius, length;

		private Component(RGB color, double radius, double length) {
			this.color = color;
			this.radius = radius;
			this.length = length;
		}

	}

	public class Cut extends AbilityTimer {

		private final CenteredBoundingBox boundingBox = CenteredBoundingBox.of(new Vector(), -1, -1, -1, 1, 1, 1);
		private final Location base;
		private final double angle;
		private final int sharpness;
		private final Consumer<LivingEntity> onDamage;

		private Cut(Location base, double angle, Consumer<LivingEntity> onDamage) {
			super(angle < 0 ? TaskType.REVERSE : TaskType.NORMAL, 3);
			setPeriod(TimeUnit.TICKS, 1);
			this.base = base;
			this.angle = angle;
			this.sharpness = getPlayer().getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.DAMAGE_ALL);
			this.onDamage = onDamage;
		}

		@Override
		protected void run(int count) {
			final Vector direction = base.getDirection().setY(0).normalize(), axis = VectorUtil.rotateAroundAxisY(direction.clone(), 90);
			for (int i = 0; i < 10; i++) {
				final double radians = 0.10471975511965977461542144610932 * ((count - 1) * 10 + i);
				final double cos = FastMath.cos(radians), sin = FastMath.sin(radians);
				for (Component component : components) {
					final Location loc = base.clone().add(
							VectorUtil.rotateAroundAxis(
									VectorUtil.rotateAroundAxis(
											VectorUtil.rotateAroundAxisY(new Vector(cos, 0, component.length * sin), -base.getYaw()), direction, angle
									), axis, base.getPitch()
							).multiply(component.radius)
					);
					ParticleLib.REDSTONE.spawnParticle(loc, component.color);
					boundingBox.setCenter(loc);
					for (LivingEntity entity : LocationUtil.getConflictingEntities(LivingEntity.class, getPlayer().getWorld(), boundingBox, predicate)) {
						Damages.damageMagic(entity, getPlayer(), false, (float) EnchantLib.getDamageWithSharpnessEnchantment(getPlayer().getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue(), sharpness));
						if (onDamage != null) onDamage.accept(entity);
					}
				}
			}

		}

	}

	private final double startRadius = 1.25, distance = 12.5;

	public class Dash extends AbilityTimer {

		private final Vector direction;
		private final double yaw;
		private final RGB color, circleColor;
		private Location lastLocation;
		private double radius = startRadius, radians = 0.0;

		private Dash(Location startLocation, Vector direction, RGB color, RGB circleColor) {
			super(5);
			setPeriod(TimeUnit.TICKS, 1);
			this.direction = direction.setY(0).normalize().multiply(distance / 5);
			this.yaw = LocationUtil.getYaw(direction);
			this.color = color;
			this.circleColor = circleColor;
			this.lastLocation = startLocation;
		}

		@Override
		protected void run(int count) {
			final Location newLocation = lastLocation.clone().add(direction);
			for (Iterator<Location> iterator = new Iterator<Location>() {
				private final Vector vectorBetween = newLocation.toVector().subtract(lastLocation.toVector()), unit = vectorBetween.clone().normalize().multiply(.1);
				private final int amount = (int) (vectorBetween.length() / 0.1);
				private int cursor = 0;

				@Override
				public boolean hasNext() {
					return cursor < amount;
				}

				@Override
				public Location next() {
					if (cursor >= amount) throw new NoSuchElementException();
					cursor++;
					return lastLocation.clone().add(unit.clone().multiply(cursor));
				}
			}; iterator.hasNext(); ) {
				Location location = iterator.next();
				ParticleLib.REDSTONE.spawnParticle(location, color);
				radians += 0.15707963267948966192313216916398;
				radius -= (startRadius / (distance * 10));
				ParticleLib.REDSTONE.spawnParticle(location.clone().add(
						VectorUtil.rotateAroundAxisY(new Vector(FastMath.cos(radians), FastMath.sin(radians), 0), -yaw).multiply(radius)
				), circleColor);
			}
			lastLocation = newLocation;
			getPlayer().setVelocity(direction);
			if (count % 2 != 0) {
				final Location base = newLocation.clone();
				base.setDirection(direction);
				new Cut(base, (state = !state) ? 25 : -25, null).start();
				SoundLib.ENTITY_WITHER_SHOOT.playSound(base);
			}
		}

		@Override
		protected void onEnd() {
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			getPlayer().setVelocity(VectorUtil.ZERO);
		}
	}

}
