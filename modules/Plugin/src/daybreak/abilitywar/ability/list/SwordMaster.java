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
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.annotations.Support;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.VectorUtil;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.base.minecraft.entity.decorator.Deflectable;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.base.minecraft.server.ServerType;
import daybreak.abilitywar.utils.library.MaterialX;
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
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@AbilityManifest(name = "소드 마스터", rank = Rank.S, species = Species.HUMAN, explain = {
		"BETA"
})
@Beta
@Support.Server(ServerType.PAPER)
public class SwordMaster extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> BACKSTEP_COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(SwordMaster.class, "COOLDOWN.BACKSTEP", 10,
			"# 백스텝 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};

	public static final SettingObject<Integer> ULTIMATE_COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(SwordMaster.class, "COOLDOWN.ULTIMATE", 50,
			"# 난사 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};

	private static final Set<Material> materials = MaterialX.NETHERITE_SWORD.isSupported() ?
			ImmutableSet.of(MaterialX.WOODEN_SWORD.getMaterial(), Material.STONE_SWORD, Material.IRON_SWORD, MaterialX.GOLDEN_SWORD.getMaterial(), Material.DIAMOND_SWORD, MaterialX.NETHERITE_SWORD.getMaterial())
			:
			ImmutableSet.of(MaterialX.WOODEN_SWORD.getMaterial(), Material.STONE_SWORD, Material.IRON_SWORD, MaterialX.GOLDEN_SWORD.getMaterial(), Material.DIAMOND_SWORD);
	private static final EulerAngle DEFAULT_EULER_ANGLE = new EulerAngle(Math.toRadians(-10), 0, 0);
	private final Cooldown backstepCool = new Cooldown(BACKSTEP_COOLDOWN_CONFIG.getValue(), CooldownDecrease._25), ultimateCool = new Cooldown(ULTIMATE_COOLDOWN_CONFIG.getValue());
	private final int stacksToCharge = 3, maxSwords = 10;
	private final Circle[] circles = newCircleArray(maxSwords);
	private final ActionbarChannel actionbarChannel = newActionbarChannel();
	private final Swords swords = new Swords();
	private Charge charge = null;
	private final AbilityTimer chargeTimer = new AbilityTimer() {
		@Override
		protected void run(int count) {
			final PlayerInventory inventory = getPlayer().getInventory();
			if (charge != null) {
				if (inventory.getHeldItemSlot() != charge.slot || inventory.getItemInMainHand().getType() != charge.stack.getType()) {
					charge = null;
					actionbarChannel.update(formatActionbarMessage(0));
				} else {
					charge.addCount();
				}
			} else {
				if (materials.contains(inventory.getItemInMainHand().getType()) && swords.swords.size() < maxSwords) {
					charge = new Charge(inventory.getItemInMainHand(), inventory.getHeldItemSlot());
				}
			}
		}
	}.setPeriod(TimeUnit.TICKS, 5).register();

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
			getPlayer().setVelocity(getPlayer().getLocation().getDirection().normalize().multiply(-1).setY(0.45));
			backstepCool.start();
		}
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onPlayerToggleSneak(PlayerToggleSneakEvent e) {
		if (e.isSneaking() && getPlayer().getLocation().getPitch() >= 80 && !ultimateCool.isCooldown()) {
			swords.ultimate();
			ultimateCool.start();
		}
	}

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
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

		private final ItemStack stack;
		private final int slot;
		private int count = 0;

		private Charge(ItemStack stack, int slot) {
			this.stack = stack;
			this.slot = slot;
		}

		public void addCount() {
			if (++count > stacksToCharge) {
				charge = null;
				swords.add(new Sword(stack, getPlayer().getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue() / 3));
				actionbarChannel.update(formatActionbarMessage(0));
				return;
			}
			actionbarChannel.update(formatActionbarMessage(count));
		}

	}

	private class Swords extends AbilityTimer {

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
				sword.shoot(playerLocation.getDirection(), playerLocation.getYaw(), playerLocation.getPitch());
				actionbarChannel.update(formatActionbarMessage(0));
			}
		}

		private void ultimate() {
			if (swords.size() < maxSwords) return;
			for (Iterator<Sword> iterator = swords.iterator(); iterator.hasNext();) {
				iterator.next().ultimate();
				iterator.remove();
			}
			actionbarChannel.update(formatActionbarMessage(0));
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
				NMS.setLocation(armorStand, playerLocation.getX() + vector.getX(), playerLocation.getY() + vector.getY(), playerLocation.getZ() + vector.getZ(), playerLocation.getYaw(), playerLocation.getPitch());
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

	private class Sword extends AbilityTimer {

		private final Player owner;
		private final Predicate<Entity> predicate;
		private final ItemStack stack;
		private final double damage;
		private ArmorStand armorStand;
		private SkillHandler skillHandler;
		private Vector direction;
		private SwordEntity swordEntity;

		private Sword(Player owner, ItemStack stack, double damage, int maximumCount) {
			super(TaskType.NORMAL, maximumCount);
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
						if (getGame() instanceof Teamable) {
							final Teamable teamGame = (Teamable) getGame();
							final Participant entityParticipant = teamGame.getParticipant(entity.getUniqueId()), participant = teamGame.getParticipant(owner.getUniqueId());
							if (participant != null) {
								return !teamGame.hasTeam(entityParticipant) || !teamGame.hasTeam(participant) || (!teamGame.getTeam(entityParticipant).equals(teamGame.getTeam(participant)));
							}
						}
					}
					return true;
				}
			};
			setPeriod(TimeUnit.TICKS, 1);
			this.stack = stack.clone();
			this.damage = damage;
			newArmorStand();
		}

		private Sword(Player owner, ItemStack stack, double damage) {
			this(owner, stack, damage, 200);
		}

		private Sword(ItemStack stack, double damage) {
			this(getPlayer(), stack, damage);
		}

		private void newArmorStand() {
			this.armorStand = owner.getWorld().spawn(owner.getLocation(), ArmorStand.class);
			armorStand.setFireTicks(Integer.MAX_VALUE);
			NMS.removeBoundingBox(armorStand);
			armorStand.setMetadata("SwordMaster", new FixedMetadataValue(AbilityWar.getPlugin(), null));
			armorStand.setVisible(false);
			armorStand.setInvulnerable(true);
			armorStand.getEquipment().setItemInMainHand(stack);
			armorStand.setGravity(false);
			armorStand.setRightArmPose(DEFAULT_EULER_ANGLE);
		}

		private void shoot(Vector vector, float yaw, float pitch) {
			this.direction = vector.normalize();
			this.swordEntity = new SwordEntity(owner.getWorld(), owner.getLocation().getX(), owner.getLocation().getY(), owner.getLocation().getZ());
			armorStand.setRightArmPose(new EulerAngle(Math.toRadians(pitch - 10), 0, 0));
			armorStand.teleport(owner.getLocation());
			this.skillHandler = new SkillHandler() {
				private final Vector right = VectorUtil.rotateAroundAxisY(direction.clone(), -90).multiply(0.4);

				@Override
				public void run(final int count) {
					for (int i = 0; i < 2; i++) {
						final Location location = armorStand.getLocation().add(direction);
						final Location swordLocation = location.clone().add(0, 0.8, 0).add(direction.clone().multiply(0.75)).add(right);
						final Block block = swordLocation.getBlock();
						if (!block.isEmpty() && block.getType().isSolid()) {
							stop(false);
							return;
						}
						swordEntity.setLocation(swordLocation);
						NMS.setLocation(armorStand, location.getX(), location.getY(), location.getZ(), yaw, pitch);
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

		private void ultimate() {
			armorStand.setRightArmPose(new EulerAngle(Math.toRadians(80), 0, 0));
			final Location teleportDest = armorStand.getLocation();
			teleportDest.setY(LocationUtil.getFloorYAt(teleportDest.getWorld(), getPlayer().getLocation().getY(), teleportDest.getBlockX(), teleportDest.getBlockZ()) - 0.5);
			armorStand.teleport(teleportDest);
			armorStand.getWorld().strikeLightningEffect(armorStand.getLocation());
			this.skillHandler = new SkillHandler() {
				@Override
				public void run(final int count) {
				}
			};
			start();
		}

		@Override
		protected void run(int count) {
			skillHandler.run(count);
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
				new Sword(deflectedPlayer, stack, damage).shoot(newDirection, LocationUtil.getYaw(newDirection), LocationUtil.getPitch(newDirection));
			}

			@Override
			public ProjectileSource getShooter() {
				return owner;
			}

		}

	}

	private interface SkillHandler {
		void run(final int count);
	}

}
