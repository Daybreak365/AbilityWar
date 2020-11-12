package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.SubscribeEvent.Priority;
import daybreak.abilitywar.ability.decorator.TargetHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Effect;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.annotations.Support;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.base.minecraft.version.NMSVersion;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.WeakHashMap;

@AbilityManifest(name = "좀비", rank = Rank.A, species = Species.UNDEAD, explain = {
		"좀비가 당신을 타게팅하지 않습니다. 다른 플레이어를 철괴로 우클릭하면 주변",
		"$[RADIUS]칸 안에 무적 §5좀비§f $[ZOMBIE_COUNT]마리를 소환합니다. $[COOLDOWN]",
		"능력으로 소환된 좀비에게 공격당한 플레이어는 5초간 §5감염 §f효과가 생기며,",
		"다른 플레이어가 나를 공격할 경우 좀비의 타겟이 그 플레이어로 변경됩니다.",
		"§7상태 이상 §8- §5감염§f: 간헐적으로 시야가 돌아가며, 대미지를 25% 줄여받습니다."
})
@Support.Version(min = NMSVersion.v1_9_R1, max = NMSVersion.v1_14_R1)
public class Zombie extends AbilityBase implements TargetHandler {

	private static final SettingObject<Integer> COOLDOWN = abilitySettings.new SettingObject<Integer>(Zombie.class, "cooldown", 100, "# 쿨타임") {

		@Override
		public boolean condition(Integer arg0) {
			return arg0 >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};

	private static final SettingObject<Integer> DURATION = abilitySettings.new SettingObject<Integer>(Zombie.class, "duration", 10, "# 지속 시간") {

		@Override
		public boolean condition(Integer arg0) {
			return arg0 >= 1;
		}

	};

	private static final SettingObject<Double> RADIUS = abilitySettings.new SettingObject<Double>(Zombie.class, "radius", 10.0, "# 스킬 반경") {

		@Override
		public boolean condition(Double arg0) {
			return arg0 >= 1;
		}

	};

	private static final SettingObject<Integer> ZOMBIE_COUNT = abilitySettings.new SettingObject<Integer>(Zombie.class, "zombie-count", 6, "# 생성할 좀비 수") {

		@Override
		public boolean condition(Integer arg0) {
			return arg0 >= 1;
		}

	};

	public Zombie(Participant participant) {
		super(participant);
	}

	@SubscribeEvent(priority = Priority.HIGHEST)
	private void onMobTarget(EntityTargetLivingEntityEvent e) {
		if (getPlayer().equals(e.getTarget()) && e.getEntityType().equals(EntityType.ZOMBIE)) {
			e.setCancelled(true);
		}
		if (zombies.contains(e.getEntity()) && target != null && !target.equals(e.getTarget())) {
			e.setTarget(target);
		}
	}

	private final double radius = RADIUS.getValue();
	private final int zombieCount = ZOMBIE_COUNT.getValue();
	private final Set<org.bukkit.entity.Zombie> zombies = new HashSet<>(zombieCount);
	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN.getValue());
	private Player target;
	private final Duration skill = new Duration(DURATION.getValue() * 20, cooldownTimer) {

		@Override
		protected void onDurationStart() {
			final double criterionY = getPlayer().getLocation().getY();
			for (final Location location : LocationUtil.getRandomLocations(getPlayer().getLocation(), radius, zombieCount)) {
				org.bukkit.entity.Zombie zombie = getPlayer().getWorld().spawn(LocationUtil.floorY(location, criterionY), org.bukkit.entity.Zombie.class);
				zombie.setBaby(false);
				zombie.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.65);
				zombies.add(zombie);
			}
			for (org.bukkit.entity.Zombie zombie : zombies) {
				zombie.setTarget(target);
			}
		}

		@Override
		protected void onDurationProcess(int count) {
			for (org.bukkit.entity.Zombie zombie : zombies) {
				if (ServerVersion.getVersion() >= 10) zombie.setInvulnerable(true);
				zombie.setFireTicks(0);
				final AttributeInstance movement = zombie.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
				if (movement.getValue() > 0.3) {
					movement.setBaseValue(Math.max(0.3, movement.getValue() - 0.001));
				}
			}
		}

		@Override
		protected void onDurationEnd() {
			for (org.bukkit.entity.Zombie zombie : zombies) {
				zombie.remove();
			}
			zombies.clear();
			target = null;
		}

		@Override
		protected void onDurationSilentEnd() {
			for (org.bukkit.entity.Zombie zombie : zombies) {
				zombie.remove();
			}
			zombies.clear();
			target = null;
		}
	}.setPeriod(TimeUnit.TICKS, 1);

	@SubscribeEvent
	private void onEntityDamage(EntityDamageEvent e) {
		if (skill.isRunning()) {
			if (zombies.contains(e.getEntity())) {
				e.setCancelled(true);
			}
		}
	}

	@SubscribeEvent
	private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (skill.isRunning()) {
			if (zombies.contains(e.getEntity())) {
				e.setCancelled(true);
			} else if (zombies.contains(e.getDamager()) && getGame().isParticipating(e.getEntity().getUniqueId())) {
				infect(getGame().getParticipant(e.getEntity().getUniqueId()), TimeUnit.SECONDS, 5);
				e.setDamage(e.getDamage() / 2);
			}
			if (getPlayer().equals(e.getEntity())) {
				final Entity damager = getDamager(e.getDamager());
				if (damager instanceof Player && getGame().isParticipating(damager.getUniqueId()) && !getPlayer().equals(damager)) {
					this.target = (Player) damager;
					for (org.bukkit.entity.Zombie zombie : zombies) {
						zombie.setTarget(target);
					}
				}
			}
		}
	}

	@Nullable
	private static Entity getDamager(final Entity damager) {
		if (damager instanceof Projectile) {
			final ProjectileSource shooter = ((Projectile) damager).getShooter();
			return shooter instanceof Entity ? (Entity) shooter : null;
		} else return damager;
	}

	@SubscribeEvent
	private void onEntityDamageByBlock(EntityDamageByBlockEvent e) {
		onEntityDamage(e);
	}

	@Override
	public void TargetSkill(@NotNull Material material, @NotNull LivingEntity entity) {
		if (material == Material.IRON_INGOT && entity instanceof Player && !skill.isDuration() && !cooldownTimer.isCooldown()) {
			this.target = (Player) entity;
			skill.start();
		}
	}

	private final Map<Participant, Infection> infections = new WeakHashMap<>();

	private void infect(Participant participant, TimeUnit timeUnit, int duration) {
		if (infections.containsKey(participant)) {
			final Infection applied = infections.get(participant);
			final int toTicks = timeUnit.toTicks(duration) / 4;
			if (toTicks > applied.getCount()) {
				applied.setCount(toTicks);
			}
		} else {
			new Infection(participant, timeUnit, duration).start();
		}
	}

	private static final Random random = new Random();

	public class Infection extends Effect implements Listener {

		private final Participant participant;

		private Infection(final Participant participant, final TimeUnit timeUnit, final int duration) {
			Zombie.this.getGame().super(participant, "§5감염", TaskType.REVERSE, timeUnit.toTicks(duration) / 4);
			this.participant = participant;
			setPeriod(TimeUnit.TICKS, 4);
		}

		@EventHandler
		private void onPlayerDeath(PlayerDeathEvent e) {
			if (participant.getPlayer().getUniqueId().equals(e.getEntity().getUniqueId())) {
				stop(false);
			}
		}

		@EventHandler
		private void onEntityDamage(final EntityDamageEvent e) {
			if (participant.getPlayer().equals(e.getEntity())) {
				e.setDamage(e.getDamage() * .75);
			}
		}

		@Override
		protected void onStart() {
			Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
			infections.put(participant, this);
		}

		@Override
		protected void run(int count) {
			super.run(count);
			if (Math.random() <= 0.65) {
				final Player player = participant.getPlayer();
				final Location location = player.getLocation();
				float yaw = location.getYaw() + random.nextInt(130) - 65;
				if (yaw > 180 || yaw < -180) {
					float mod = yaw % 180;
					if (mod < 0) {
						yaw = 180 + mod;
					} else if (mod > 0) {
						yaw = -180 + mod;
					}
				}
				NMS.rotateHead(player, player, yaw, location.getPitch() + random.nextInt(90) - 45);
			}
		}

		@Override
		protected void onEnd() {
			infections.remove(participant);
			HandlerList.unregisterAll(this);
			super.onEnd();
		}

		@Override
		protected void onSilentEnd() {
			infections.remove(participant);
			HandlerList.unregisterAll(this);
			super.onSilentEnd();
		}

	}

}
