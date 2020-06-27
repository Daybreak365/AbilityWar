package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.TargetHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.annotations.Support;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion.Version;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

@AbilityManifest(name = "좀비", rank = Rank.A, species = Species.UNDEAD, explain = {
		"좀비가 당신을 타게팅하지 않습니다.",
		"다른 플레이어를 철괴로 우클릭하면 주변 $[RadiusConfig]칸 안에 속도가 점차 줄어드는",
		"$[ZombieCountConfig]마리의 §5좀비§f를 소환합니다.",
		"소환된 좀비들은 불에 타지 않고, 대미지를 받지 않으며, 대상 플레이어를 공격합니다. $[CooldownConfig]"
})
@Support(min = Version.v1_9_R1, max = Version.v1_14_R1)
public class Zombie extends AbilityBase implements TargetHandler {

	private static final SettingObject<Integer> CooldownConfig = abilitySettings.new SettingObject<Integer>(Zombie.class, "Cooldown", 100, "# 쿨타임") {

		@Override
		public boolean condition(Integer arg0) {
			return arg0 >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};

	private static final SettingObject<Integer> DurationConfig = abilitySettings.new SettingObject<Integer>(Zombie.class, "Duration", 15, "# 지속시간") {

		@Override
		public boolean condition(Integer arg0) {
			return arg0 >= 1;
		}

	};

	private static final SettingObject<Double> RadiusConfig = abilitySettings.new SettingObject<Double>(Zombie.class, "Radius", 10.0, "# 스킬 반경") {

		@Override
		public boolean condition(Double arg0) {
			return arg0 >= 1;
		}

	};

	private static final SettingObject<Integer> ZombieCountConfig = abilitySettings.new SettingObject<Integer>(Zombie.class, "ZombieCount", 15, "# 생성할 좀비 수") {

		@Override
		public boolean condition(Integer arg0) {
			return arg0 >= 1;
		}

	};

	public Zombie(Participant participant) {
		super(participant);
	}

	@SubscribeEvent
	private void onMobTarget(EntityTargetLivingEntityEvent e) {
		if (getPlayer().equals(e.getTarget()) && e.getEntityType().equals(EntityType.ZOMBIE)) {
			e.setCancelled(true);
		}
	}

	private final double radius = RadiusConfig.getValue();
	private final int zombieCount = ZombieCountConfig.getValue();
	private final Set<org.bukkit.entity.Zombie> zombies = new HashSet<>(zombieCount);
	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());
	private Player target;
	private final DurationTimer skill = new DurationTimer(DurationConfig.getValue() * 20, cooldownTimer) {

		@Override
		protected void onDurationStart() {
			for (Location location : LocationUtil.getRandomLocations(getPlayer().getLocation(), radius, zombieCount)) {
				org.bukkit.entity.Zombie zombie = getPlayer().getWorld().spawn(location, org.bukkit.entity.Zombie.class);
				zombie.setGlowing(true);
				zombie.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.75);
				zombies.add(zombie);
			}
		}

		@Override
		protected void onDurationProcess(int count) {
			for (org.bukkit.entity.Zombie zombie : zombies) {
				if (ServerVersion.getVersionNumber() >= 10) zombie.setInvulnerable(true);
				zombie.setFireTicks(0);
				zombie.setTarget(target);
				AttributeInstance movement = zombie.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
				if (movement.getValue() > 0.3) {
					movement.setBaseValue(Math.max(0.3, movement.getValue() - 0.0025));
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
		if (skill.isRunning() && zombies.contains(e.getEntity())) {
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

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
		if (materialType == Material.IRON_INGOT && entity instanceof Player && !skill.isDuration() && !cooldownTimer.isCooldown()) {
			this.target = (Player) entity;
			skill.start();
		}
	}

}
