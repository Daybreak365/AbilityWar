package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.Tips;
import daybreak.abilitywar.ability.Tips.Difficulty;
import daybreak.abilitywar.ability.Tips.Level;
import daybreak.abilitywar.ability.Tips.Stats;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.FastMath;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.geometry.Line;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.library.MaterialX;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

@AbilityManifest(name = "황제", rank = Rank.B, species = Species.HUMAN, explain = {
		"철괴를 우클릭하면 앞으로 돌진하는 방패 부대를 내보내 앞에 있는 모든",
		"생명체와 물체를 밀쳐냅니다. $[COOLDOWN_CONFIG]"
}, summarize = {
		"§7철괴 우클릭§f 시 전방으로 돌진하는 §e방패 부대§f를 출격시켜 엔티티들을 §b밀쳐냅니다§f."
})
@Tips(tip = {
		"위협적인 상대가 다가올 때, 상대의 스킬에 당했을 때 방패 부대를",
		"이용해 밀쳐내세요. 높은 곳에서 상대를 밀쳐내 낙하 대미지를 받게",
		"할 수도 있습니다."
}, strong = {}, weak = {}, stats = @Stats(offense = Level.ZERO, survival = Level.FOUR, crowdControl = Level.THREE, mobility = Level.ZERO, utility = Level.FIVE), difficulty = Difficulty.EASY)
public class Emperor extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Emperor.class, "cooldown", 50,
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

	public Emperor(Participant participant) {
		super(participant);
	}

	private final Set<ArmorStand> armorStands = new HashSet<>();

	private final Predicate<Entity> predicate = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			if (entity.equals(getPlayer())) return false;
			if (armorStands.contains(entity)) return false;
			return (!(entity instanceof Player)) || (getGame().isParticipating(entity.getUniqueId())
					&& (!(getGame() instanceof DeathManager.Handler) || !((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
					&& getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue());
		}
	};

	private static final double radians = Math.toRadians(90);
	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());
	private final Duration skill = new Duration(140, cooldownTimer) {

		private Vector direction;
		private ArmorStand center;
		private HashMap<ArmorStand, Vector> diff;
		private Vector push;

		@Override
		protected void onDurationStart() {
			final Location playerLocation = getPlayer().getLocation();
			direction = playerLocation.getDirection();
			final Location lineTarget = playerLocation.clone().add(direction.clone().setY(0).normalize().multiply(3.75));
			for (Vector vector : Line.between(playerLocation, lineTarget, 5)) {
				final double originX = vector.getX();
				final double originZ = vector.getZ();
				armorStands.add(getPlayer().getWorld().spawn(playerLocation.clone().add(vector.clone()
						.setX(rotateX(originX, originZ, radians))
						.setZ(rotateZ(originX, originZ, radians))), ArmorStand.class)
				);
				armorStands.add(getPlayer().getWorld().spawn(playerLocation.clone().add(vector.clone()
						.setX(rotateX(originX, originZ, radians * 3))
						.setZ(rotateZ(originX, originZ, radians * 3))), ArmorStand.class)
				);
			}
			final Vector centerVector = lineTarget.toVector();
			this.center = getPlayer().getWorld().spawn(lineTarget, ArmorStand.class);
			if (ServerVersion.getVersion() >= 10) {
				center.setInvulnerable(true);
				center.setCollidable(false);
			}
			center.setVisible(false);

			final EulerAngle eulerAngle = new EulerAngle(Math.toRadians(270), Math.toRadians(270), 0);
			diff = new HashMap<>();
			for (ArmorStand armorStand : armorStands) {
				if (ServerVersion.getVersion() >= 10) {
					armorStand.setInvulnerable(true);
					armorStand.setCollidable(false);
				}
				armorStand.setBasePlate(false);
				armorStand.setArms(true);
				armorStand.setVisible(false);
				armorStand.setRightArmPose(eulerAngle);
				armorStand.setGravity(false);
				final EntityEquipment equipment = armorStand.getEquipment();
				equipment.setItemInMainHand(new ItemStack(Material.SHIELD));
				equipment.setHelmet(MaterialX.GOLDEN_HELMET.createItem());
				diff.put(armorStand, armorStand.getLocation().toVector().subtract(centerVector).add(direction.clone()));
			}
			gravityFalse = false;
			push = direction.clone().multiply(2).setY(0);
		}

		private boolean gravityFalse;

		@Override
		protected void onDurationProcess(int count) {
			if (count >= 120) {
				center.setVelocity(direction);
				Location centerLocation = center.getLocation();
				for (ArmorStand armorStand : armorStands) {
					if (!armorStand.equals(center) && diff.containsKey(armorStand)) {
						armorStand.teleport(centerLocation.clone().add(diff.get(armorStand)));
					}
				}
			} else if (!gravityFalse) {
				center.setGravity(false);
				gravityFalse = true;
			}
			for (ArmorStand armorStand : armorStands) {
				for (Entity entity : LocationUtil.getConflictingEntities(Entity.class, armorStand, predicate)) {
					entity.setVelocity(push);
				}
			}
		}
		@Override
		protected void onDurationEnd() {
			for (ArmorStand armorStand : armorStands) {
				armorStand.remove();
			}
			center.remove();
			armorStands.clear();
		}

		@Override
		protected void onDurationSilentEnd() {
			for (ArmorStand armorStand : armorStands) {
				armorStand.remove();
			}
			center.remove();
			armorStands.clear();
		}
	}.setPeriod(TimeUnit.TICKS, 1);

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !skill.isDuration() && !cooldownTimer.isCooldown()) {
			skill.start();
			return true;
		}
		return false;
	}

	@SubscribeEvent
	private void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
		if (armorStands.contains(e.getRightClicked())) e.setCancelled(true);
	}

	@SubscribeEvent
	private void onEntityDamage(EntityDamageEvent e) {
		if (skill.isRunning() && armorStands.contains(e.getEntity())) {
			e.setCancelled(true);
		}
	}

	@SubscribeEvent
	private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		onEntityDamage(e);
	}

	@SubscribeEvent
	private void onEntityDamageByBlock(EntityDamageByBlockEvent e) {
		onEntityDamage(e);
	}

	private double rotateX(double x, double z, double radians) {
		return (x * FastMath.cos(radians)) + (z * FastMath.sin(radians));
	}

	private double rotateZ(double x, double z, double radians) {
		return (-x * FastMath.sin(radians)) + (z * FastMath.cos(radians));
	}

}
