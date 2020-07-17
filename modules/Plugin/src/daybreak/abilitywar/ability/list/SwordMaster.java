package daybreak.abilitywar.ability.list;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.config.enums.CooldownDecrease;
import daybreak.abilitywar.game.AbstractGame.CustomEntity;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.interfaces.TeamGame;
import daybreak.abilitywar.game.manager.object.DeathManager;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.VectorUtil;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.base.minecraft.entity.decorator.Deflectable;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.library.MaterialX;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

@AbilityManifest(name = "소드 마스터", rank = Rank.S, species = Species.HUMAN, explain = {
		"BETA"
})
@Beta
public class SwordMaster extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> BACKSTEP_COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(SwordMaster.class, "COOLDOWN.BACKSTEP", 10,
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

	private static final Set<Material> materials = ImmutableSet.of(MaterialX.WOODEN_SWORD.parseMaterial(), Material.STONE_SWORD, Material.IRON_SWORD, MaterialX.GOLDEN_SWORD.parseMaterial(), Material.DIAMOND_SWORD);
	private static final Set<Material> canGoThrough = ImmutableSet.of(
			MaterialX.TALL_GRASS.parseMaterial(), MaterialX.GRASS.parseMaterial(), MaterialX.VINE.parseMaterial(), MaterialX.SUNFLOWER.parseMaterial(),
			MaterialX.ALLIUM.parseMaterial(), MaterialX.AZURE_BLUET.parseMaterial(), MaterialX.BLUE_ORCHID.parseMaterial(), MaterialX.ORANGE_TULIP.parseMaterial(),
			MaterialX.OXEYE_DAISY.parseMaterial(), MaterialX.PINK_TULIP.parseMaterial(), MaterialX.POPPY.parseMaterial(), MaterialX.RED_TULIP.parseMaterial(),
			MaterialX.WHITE_TULIP.parseMaterial(), MaterialX.SNOW.parseMaterial(), MaterialX.WATER.parseMaterial()
	);
	private static final EulerAngle DEFAULT_EULER_ANGLE = new EulerAngle(Math.toRadians(-10), 0, 0);
	private final CooldownTimer backstepCool = new CooldownTimer(BACKSTEP_COOLDOWN_CONFIG.getValue(), CooldownDecrease._25);
	private final int stacksToCharge = 3, maxSwords = 10;
	private final Circle[] circles = newCircleArray(maxSwords);
	private final ActionbarChannel actionbarChannel = newActionbarChannel();
	private final Swords swords = new Swords();
	private Charge charge = null;
	private final Timer chargeTimer = new Timer() {
		@Override
		protected void run(int count) {
			final PlayerInventory inventory = getPlayer().getInventory();
			if (charge != null) {
				if (inventory.getHeldItemSlot() != charge.slot || inventory.getItemInMainHand().getType() != charge.material) {
					charge = null;
					actionbarChannel.update(formatActionbarMessage(0));
				} else {
					charge.addCount();
				}
			} else {
				if (materials.contains(inventory.getItemInMainHand().getType()) && swords.swords.size() < maxSwords) {
					charge = new Charge(inventory.getItemInMainHand().getType(), inventory.getHeldItemSlot());
				}
			}
		}
	}.setPeriod(TimeUnit.TICKS, 5);

	public SwordMaster(Participant participant) {
		super(participant);
	}

	private static Circle[] newCircleArray(final int maxAmount) {
		final Circle[] circles = new Circle[maxAmount];
		for (int i = 0; i < circles.length; i++) {
			circles[i] = Circle.of(2.5, i + 1);
		}
		return circles;
	}

	@Override
	public boolean usesMaterial(Material material) {
		return super.usesMaterial(material) || materials.contains(material);
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onPlayerMove(PlayerMoveEvent e) {
		if (getPlayer().getVelocity().getY() > 0.4 && getPlayer().isSneaking() && !backstepCool.isCooldown()) {
			getPlayer().setVelocity(getPlayer().getLocation().getDirection().normalize().multiply(-1).setY(0.3));
			backstepCool.start();
		}
	}

	@Override
	public boolean ActiveSkill(Material material, ClickType clickType) {
		if (materials.contains(material) && clickType == ClickType.RIGHT_CLICK) {
			swords.shoot();
		}
		return false;
	}

	private String formatActionbarMessage(int chargeCount) {
		return "§b" + Strings.repeat(">", chargeCount) + "§7" + Strings.repeat(">", stacksToCharge - chargeCount) + " §9" + Strings.repeat("●", swords.swords.size()) + "§9" + Strings.repeat("○", maxSwords - swords.swords.size());
	}

	@SubscribeEvent
	private void onArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
		if (e.getRightClicked().hasMetadata("SwordMaster")) e.setCancelled(true);
	}

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			actionbarChannel.update(formatActionbarMessage(0));
			chargeTimer.start();
			swords.start();
		}
	}

	private class Charge {

		private final Material material;
		private final int slot;
		private int count = 0;

		private Charge(Material material, int slot) {
			this.material = material;
			this.slot = slot;
		}

		public void addCount() {
			if (++count > stacksToCharge) {
				charge = null;
				swords.add(new Sword(material, getPlayer().getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue() / 3));
				actionbarChannel.update(formatActionbarMessage(0));
				return;
			}
			actionbarChannel.update(formatActionbarMessage(count));
		}

	}

	private class Swords extends Timer {

		private final List<Sword> swords = new ArrayList<Sword>() {
			@Override
			public boolean add(Sword o) {
				if (size() < maxSwords) return super.add(o);
				return false;
			}
		};

		private Swords() {
			setPeriod(TimeUnit.TICKS, 1);
		}

		private void add(Sword sword) {
			swords.add(sword);
		}

		private void shoot() {
			if (!swords.isEmpty()) {
				final Sword sword = swords.remove(0);
				final Location playerLocation = getPlayer().getLocation();
				sword.shoot(playerLocation.getDirection(), playerLocation.getYaw(), playerLocation.getPitch(), true);
				actionbarChannel.update(formatActionbarMessage(0));
			}
		}

		@Override
		protected void run(int count) {
			if (swords.isEmpty()) return;

			final Vector direction = getPlayer().getLocation().getDirection().setY(0).normalize();
			final Vector left = VectorUtil.rotateAroundAxisY(direction.clone(), 90);
			final List<Vector> circle = circles[swords.size() - 1].clone().rotateAroundAxisY(-getPlayer().getLocation().getYaw()).rotateAroundAxis(left, 180);
			final Location playerLocation = getPlayer().getLocation().clone().add(direction.clone().multiply(0.1)).add(left.clone().multiply(0.5));
			final EulerAngle eulerAngle = new EulerAngle(Math.toRadians(playerLocation.getPitch() - 10), 0, 0);
			for (int i = 0; i < (circle.size() - 1); i++) {
				final Vector vector = circle.get(i);
				final ArmorStand armorStand = swords.get(i).armorStand;
				armorStand.setRightArmPose(eulerAngle);
				NMS.moveEntity(armorStand, playerLocation.getX() + vector.getX(), playerLocation.getY() + vector.getY(), playerLocation.getZ() + vector.getZ(), playerLocation.getYaw(), playerLocation.getPitch(), false);
			}
		}

		@Override
		protected void onStart() {
			for (Sword sword : swords) {
				sword.newArmorStand();
			}
		}

		@Override
		protected void onEnd() {
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			for (Sword sword : swords) {
				sword.armorStand.remove();
			}
		}
	}

	private class Sword extends Timer {

		private final Player owner;
		private final Predicate<Entity> predicate;
		private final Material material;
		private final double damage;
		private ArmorStand armorStand;
		private Runnable runnable;
		private Vector direction;
		private SwordEntity swordEntity;

		private Sword(Player owner, Material material, double damage) {
			super(TaskType.NORMAL, 200);
			this.owner = owner;
			this.predicate = new Predicate<Entity>() {
				@Override
				public boolean test(Entity entity) {
					if (entity.equals(owner)) return false;
					if (entity instanceof Player) {
						if (!getGame().isParticipating(entity.getUniqueId())
								|| (getGame() instanceof DeathManager.Handler && ((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
								|| !getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue()) {
							return false;
						}
						if (getGame() instanceof TeamGame) {
							final TeamGame teamGame = (TeamGame) getGame();
							final Participant entityParticipant = getGame().getParticipant(entity.getUniqueId());
							final Participant participant = getGame().getParticipant(owner.getUniqueId());
							if (participant != null) {
								return !teamGame.hasTeam(entityParticipant) || !teamGame.hasTeam(participant) || (!teamGame.getTeam(entityParticipant).equals(teamGame.getTeam(participant)));
							}
						}
					}
					return true;
				}
			};
			setPeriod(TimeUnit.TICKS, 1);
			this.material = material;
			this.damage = damage;
			newArmorStand();
		}

		private Sword(Material material, double damage) {
			this(getPlayer(), material, damage);
		}

		private void newArmorStand() {
			this.armorStand = owner.getWorld().spawn(owner.getLocation(), ArmorStand.class);
			armorStand.setMarker(true);
			NMS.removeBoundingBox(armorStand);
			armorStand.setMetadata("SwordMaster", new FixedMetadataValue(AbilityWar.getPlugin(), null));
			armorStand.setVisible(false);
			armorStand.setInvulnerable(true);
			armorStand.getEquipment().setItemInMainHand(new ItemStack(material));
			armorStand.setGravity(false);
			armorStand.setRightArmPose(DEFAULT_EULER_ANGLE);
		}

		private void shoot(Vector vector, float yaw, float pitch, boolean startFromShooter) {
			this.direction = vector.normalize();
			this.swordEntity = new SwordEntity(owner.getWorld(), owner.getLocation().getX(), owner.getLocation().getY(), owner.getLocation().getZ());
			armorStand.setRightArmPose(new EulerAngle(Math.toRadians(pitch - 10), 0, 0));
			if (startFromShooter) armorStand.teleport(owner.getLocation());
			this.runnable = new Runnable() {
				private final Vector right = VectorUtil.rotateAroundAxisY(direction.clone(), -90).multiply(0.4);

				@Override
				public void run() {
					for (int i = 0; i < 2; i++) {
						final Location location = armorStand.getLocation().add(direction);
						final Location swordLocation = location.clone().add(0, 0.8, 0).add(direction.clone().multiply(0.75)).add(right);
						final Block block = swordLocation.getBlock();
						if (!block.isEmpty() && !canGoThrough.contains(block.getType())) {
							stop(false);
							return;
						}
						swordEntity.setLocation(swordLocation);
						NMS.moveEntity(armorStand, location.getX(), location.getY(), location.getZ(), yaw, pitch, false);
						for (LivingEntity livingEntity : LocationUtil.getConflictingEntities(LivingEntity.class, armorStand, predicate)) {
							if (!livingEntity.hasMetadata("SwordMaster")) {
								livingEntity.setNoDamageTicks(0);
								livingEntity.damage(damage, owner);
								stop(false);
								return;
							}
						}
					}
				}
			};
			start();
		}

		@Override
		protected void run(int count) {
			runnable.run();
		}

		@Override
		protected void onEnd() {
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			armorStand.remove();
			if (swordEntity != null) swordEntity.remove();
		}

		public class SwordEntity extends CustomEntity implements Deflectable {

			public SwordEntity(World world, double x, double y, double z) {
				getGame().super(world, x, y, z);
			}

			@Override
			public Vector getDirection() {
				return direction.clone();
			}

			@Override
			public void onDeflect(Participant deflector, Vector newDirection) {
				stop(false);
				final Player deflectedPlayer = deflector.getPlayer();
				new Sword(deflectedPlayer, material, damage).shoot(newDirection, LocationUtil.getYaw(newDirection), LocationUtil.getPitch(newDirection), true);
			}

			@Override
			public ProjectileSource getShooter() {
				return owner;
			}

		}

	}
}
