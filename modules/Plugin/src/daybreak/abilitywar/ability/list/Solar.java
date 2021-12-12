package daybreak.abilitywar.ability.list;

import com.google.common.base.Strings;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.effect.Rooted;
import daybreak.abilitywar.game.manager.effect.Stun;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.VectorUtil;
import daybreak.abilitywar.utils.base.math.VectorUtil.Vectors;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.base.math.geometry.Crescent;
import daybreak.abilitywar.utils.base.minecraft.nms.IHologram;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

@AbilityManifest(name = "솔라", rank = Rank.A, species = Species.OTHERS, explain = {
		"§7패시브 §8- §f광명§f: 표식이 부여된 생명체는 이동 속도가 느려지며, 세계의 시간을 점점",
		" 낮으로 바꿉니다. 표식이 세 개 이상 쌓이면 대상의 표식이 초기화되고 대상을",
		" 1초간 §5속박§f시키며, 흑점 폭발의 쿨타임이 10초 단축되고 §e흡수 체력§8(§7최대 6칸§8)",
		" 반 칸을 얻습니다. 10초간 표식이 추가로 쌓이지 않으면 표식이 초기화됩니다.",
		"§7공격 무기 §8- §f빛의 검§f: 대상을 근접 공격하면 광명 표식을 하나 부여합니다.",
		" 이 공격으로 표식을 세 개 쌓으면 해당 공격은 1.4배의 대미지를 냅니다.",
		"§7철괴 우클릭 §8- §f흑점 폭발§f: 주변 7칸 내의 모든 생명체에게 표식 두 개를 부여하고",
		" 4초간 §5실명§f시킵니다. 이후 자신은 §e흡수 체력§8(§7최대 6칸§8)§f 한 칸 반을 얻습니다.",
		" 낮에만 사용할 수 있습니다. $[COOLDOWN_CONFIG]"
})
@Beta
public class Solar extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Lunar.class, "cooldown", 30,
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

	private static final Crescent crescent = Crescent.of(1, 20);
	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue(), 50);

	public Solar(Participant participant) {
		super(participant);
	}

	private final Map<UUID, Stack> stackMap = new HashMap<>();
	private final Predicate<Entity> predicate = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			if (entity.equals(getPlayer())) return false;
			if (entity instanceof ArmorStand) return false;
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
	private int particleSide = 45;

	@SubscribeEvent(onlyRelevant = true)
	private void onPlayerJoin(PlayerJoinEvent e) {
		for (Stack stack : stackMap.values()) {
			stack.hologram.display(getPlayer());
		}
	}

	private static boolean isDay(long time) {
		return time <= 12300 || time >= 23850;
	}

	private void updateTime(World world) {
		final long diff = 2000 - world.getTime();
		if (diff < 0) {
			if (-diff > 2000) world.setTime(world.getTime() + 1500);
			else world.setTime(2000);
		} else if (diff > 0) {
			if (diff > 2000) world.setTime(world.getTime() - 1500);
			else world.setTime(2000);
		}
	}

	private class CutParticle extends AbilityTimer {

		private final Vector axis;
		private final Vector vector;
		private final Vectors crescentVectors;

		private CutParticle(double angle) {
			super(4);
			setPeriod(TimeUnit.TICKS, 1);
			this.axis = VectorUtil.rotateAroundAxis(VectorUtil.rotateAroundAxisY(getPlayer().getLocation().getDirection().setY(0).normalize(), 90), getPlayer().getLocation().getDirection().setY(0).normalize(), angle);
			this.vector = getPlayer().getLocation().getDirection().setY(0).normalize().multiply(0.5);
			this.crescentVectors = crescent.clone()
					.rotateAroundAxisY(-getPlayer().getLocation().getYaw())
					.rotateAroundAxis(getPlayer().getLocation().getDirection().setY(0).normalize(), (180 - angle) % 180)
					.rotateAroundAxis(axis, -75);
		}

		@Override
		protected void run(int count) {
			Location baseLoc = getPlayer().getLocation().clone().add(vector).add(0, 1.3, 0);
			for (Location loc : crescentVectors.toLocations(baseLoc)) {
				ParticleLib.REDSTONE.spawnParticle(loc, RGB.WHITE);
			}
			crescentVectors.rotateAroundAxis(axis, 40);
		}

	}

	@SubscribeEvent
	private void onAttack(EntityDamageByEntityEvent e) {
		if (getPlayer().equals(e.getDamager()) && e.getEntity() instanceof LivingEntity && !e.isCancelled() && predicate.test(e.getEntity())) {
			new CutParticle(particleSide).start();
			SoundLib.ENTITY_PLAYER_ATTACK_SWEEP.playSound(getPlayer());
			particleSide *= -1;
			if (stackMap.containsKey(e.getEntity().getUniqueId())) {
				if (stackMap.get(e.getEntity().getUniqueId()).addStack()) {
					e.setDamage(e.getDamage() * 1.4);
					if (getGame().isParticipating(e.getEntity().getUniqueId()))
						Rooted.apply(getGame().getParticipant(e.getEntity().getUniqueId()), TimeUnit.TICKS, 20);
				}
			} else new Stack((LivingEntity) e.getEntity()).start();
		}
	}

	@SubscribeEvent
	private void onDeath(EntityDeathEvent e) {
		if (stackMap.containsKey(e.getEntity().getUniqueId())) stackMap.get(e.getEntity().getUniqueId()).stop(true);
	}

	@SubscribeEvent
	private void onDeath(PlayerDeathEvent e) {
		if (stackMap.containsKey(e.getEntity().getUniqueId())) stackMap.get(e.getEntity().getUniqueId()).stop(true);
	}

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !cooldownTimer.isCooldown()) {
			if (isDay(getPlayer().getWorld().getTime())) {
				new SolarSkill(7).start();
				cooldownTimer.start();
				return true;
			} else {
				getPlayer().sendMessage("§e낮§6에만 사용할 수 있는 능력입니다.");
			}
		}
		return false;
	}

	private class SolarSkill extends AbilityTimer {

		private final Location baseLocation;
		private final double radius;

		private SolarSkill(double radius) {
			super(TaskType.NORMAL,12);
			setPeriod(TimeUnit.TICKS, 1);
			this.baseLocation = getPlayer().getLocation().clone();
			this.radius = radius;
		}

		@Override
		protected void onStart() {
			if (NMS.getAbsorptionHearts(getPlayer()) < 12) {
				NMS.setAbsorptionHearts(getPlayer(), Math.min(NMS.getAbsorptionHearts(getPlayer()) + 3f, 12));
			}
			for (LivingEntity entity : LocationUtil.getNearbyEntities(LivingEntity.class, getPlayer().getLocation(), radius, radius, predicate)) {
				PotionEffects.BLINDNESS.addPotionEffect(entity, 80, 0, true);
				entity.setFireTicks(200);
				if (stackMap.containsKey(entity.getUniqueId())) {
					Stack stack = stackMap.get(entity.getUniqueId());
					if (stack.addStack()) {
						if (getGame().isParticipating(entity.getUniqueId()))
							Stun.apply(getGame().getParticipant(entity.getUniqueId()), TimeUnit.TICKS, 15);
						new Stack(entity).start();
					} else {
						if (stack.addStack()) {
							if (getGame().isParticipating(entity.getUniqueId()))
								Stun.apply(getGame().getParticipant(entity.getUniqueId()), TimeUnit.TICKS, 15);
						}
					}
				} else {
					Stack stack = new Stack(entity);
					stack.start();
					stack.addStack();
				}
			}
		}

		@Override
		protected void run(int count) {
			if (count <= 5) {
				final Location loc = baseLocation.clone();
				loc.setY(loc.getY() + 5 - count);
				ParticleLib.EXPLOSION_HUGE.spawnParticle(loc);
				SoundLib.ENTITY_GENERIC_EXPLODE.playSound(loc);
			}
			double playerY = getPlayer().getLocation().getY();
			for (Iterator<Location> iterator = Circle.iteratorOf(baseLocation, ((double) count / getMaximumCount()) * radius, count * 14); iterator.hasNext(); ) {
				Location loc = iterator.next();
				loc.setY(LocationUtil.getFloorYAt(loc.getWorld(), playerY, loc.getBlockX(), loc.getBlockZ()) + 0.1);
				ParticleLib.FLAME.spawnParticle(loc, 0, 0, 0, 1, 0.001);
			}
		}
	}

	private class Stack extends AbilityTimer {

		private final LivingEntity entity;
		private final IHologram hologram;
		private int stack = 0;

		private Stack(LivingEntity entity) {
			super(40);
			setPeriod(TimeUnit.TICKS, 4);
			this.entity = entity;
			this.hologram = NMS.newHologram(entity.getWorld(), entity.getLocation().getX(), entity.getLocation().getY() + entity.getEyeHeight() + 0.6, entity.getLocation().getZ(), Strings.repeat("§e●", stack).concat(Strings.repeat("§e○", 3 - stack)));
			hologram.display(getPlayer());
			stackMap.put(entity.getUniqueId(), this);
			addStack();
		}

		@Override
		protected void run(int count) {
			hologram.teleport(entity.getWorld(), entity.getLocation().getX(), entity.getLocation().getY() + entity.getEyeHeight() + 0.6, entity.getLocation().getZ(), entity.getLocation().getYaw(), 0);
			PotionEffects.SLOW.addPotionEffect(entity, 50, 1, true);
		}

		private boolean addStack() {
			updateTime(getPlayer().getWorld());
			setCount(40);
			stack++;
			hologram.setText(Strings.repeat("§e●", stack).concat(Strings.repeat("§e○", 3 - stack)));
			if (stack >= 3) {
				stop(false);
				if (cooldownTimer.isRunning()) cooldownTimer.setCount(Math.max(cooldownTimer.getCount() - 10, 0));
				if (entity instanceof Player && NMS.getAbsorptionHearts(getPlayer()) < 12) {
					NMS.setAbsorptionHearts(getPlayer(), Math.min(NMS.getAbsorptionHearts(getPlayer()) + 1f, 12));
				}
				return true;
			} else {
				entity.setVelocity(new Vector());
				return false;
			}
		}

		@Override
		protected void onEnd() {
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			hologram.unregister();
			stackMap.remove(entity.getUniqueId());
		}
	}

}
