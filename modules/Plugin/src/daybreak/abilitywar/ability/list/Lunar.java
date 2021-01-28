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
import daybreak.abilitywar.game.manager.effect.Stun;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.FastMath;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.VectorUtil;
import daybreak.abilitywar.utils.base.math.VectorUtil.Vectors;
import daybreak.abilitywar.utils.base.math.geometry.Crescent;
import daybreak.abilitywar.utils.base.minecraft.nms.IHologram;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.World;
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
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

@AbilityManifest(name = "루나", rank = Rank.A, species = Species.OTHERS, explain = {
		"§7패시브 §8- §e달 표식§f: 다른 생명체에게 표식을 쌓을 때마다 나에게로 끌어오며,",
		"세계의 시간을 점점 밤으로 바꿉니다. 표식이 다섯 개 이상 쌓이면 대상의 표식이",
		"초기화되고 대상을 0.75초간 기절시키며, 월광의 쿨타임을 10초 단축합니다.",
		"대상에게 6초간 표식이 추가로 쌓이지 않을 경우 표식이 초기화됩니다.",
		"§7공격 무기 §8- §e명월도§f: 대상을 근접 공격할 때마다 대상에게 표식을 하나 쌓습니다.",
		"이 공격으로 표식을 다섯 개 이상 쌓으면 해당 공격은 1.4배의 대미지를 냅니다.",
		"§7철괴 우클릭 §8- §e월광§f: 주변 6칸 내의 모든 생명체에게 표식 두 개를 부여하고",
		"4초간의 구속 디버프를 줍니다. 밤에만 사용할 수 있습니다. $[COOLDOWN_CONFIG]"
})
public class Lunar extends AbilityBase implements ActiveHandler {

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

	private static final RGB MOONLIGHT_COLOUR = RGB.of(235, 200, 21);
	private static final Crescent crescent = Crescent.of(1, 20);
	private static final Crescent BIG_CRESCENT = Crescent.of(5, 50);
	private static final Note[] NOTES = {
			Note.natural(0, Tone.E),
			Note.sharp(1, Tone.G),
			Note.natural(1, Tone.A),
			Note.sharp(1, Tone.C)
	};
	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue(), 50);

	public Lunar(Participant participant) {
		super(participant);
	}

	private final Map<UUID, Stack> stackMap = new HashMap<>();
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
	private int particleSide = 45;

	@SubscribeEvent(onlyRelevant = true)
	private void onPlayerJoin(PlayerJoinEvent e) {
		for (Stack stack : stackMap.values()) {
			stack.hologram.display(getPlayer());
		}
	}

	private static boolean isNight(long time) {
		return time > 12300 && time < 23850;
	}

	private void updateTime(World world) {
		final long diff = 15000 - world.getTime();
		if (diff < 0) {
			if (-diff > 1000) world.setTime(world.getTime() - 1500);
			else world.setTime(world.getTime() + diff);
		} else if (diff > 0) {
			if (diff > 1000) world.setTime(world.getTime() + 1500);
			else world.setTime(world.getTime() + diff);
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
				ParticleLib.REDSTONE.spawnParticle(loc, MOONLIGHT_COLOUR);
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
						Stun.apply(getGame().getParticipant(e.getEntity().getUniqueId()), TimeUnit.TICKS, 15);
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
			if (isNight(getPlayer().getWorld().getTime())) {
				new LunarSkill(6).start();
				cooldownTimer.start();
				return true;
			} else {
				getPlayer().sendMessage("§e밤§6에만 사용할 수 있는 능력입니다.");
			}
		}
		return false;
	}

	private class SoundTimer extends AbilityTimer {

		private final Player center;

		private SoundTimer(Player center) {
			super(TaskType.NORMAL, NOTES.length);
			setPeriod(TimeUnit.TICKS, 3);
			this.center = center;
		}

		@Override
		protected void run(int count) {
			SoundLib.BELL.playInstrument(center.getLocation(), .4f, NOTES[count - 1]);
		}

	}

	private class LunarSkill extends AbilityTimer {

		private final Location baseLocation;
		private final double radius;

		private LunarSkill(double radius) {
			super(2);
			setPeriod(TimeUnit.TICKS, 4);
			this.baseLocation = getPlayer().getLocation().clone();
			this.radius = radius;
		}

		@Override
		protected void onStart() {
			new SoundTimer(getPlayer()).start();
			for (LivingEntity entity : LocationUtil.getNearbyEntities(LivingEntity.class, getPlayer().getLocation(), radius, radius, predicate)) {
				PotionEffects.SLOW.addPotionEffect(entity, 80, 1, true);
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
			final double divided = 6.283185307179586 / 6;
			for (int i = 1; i <= 6; i++) {
				final double radians = divided * i;
				final Vector offset = new Vector(FastMath.cos(radians) * radius, 0, FastMath.sin(radians) * radius);
				for (Location loc : BIG_CRESCENT.clone().rotateAroundAxisZ(90).rotateAroundAxisY(-Math.toDegrees(radians)).toLocations(baseLocation)) {
					ParticleLib.REDSTONE.spawnParticle(loc.add(offset), MOONLIGHT_COLOUR);
				}
			}
		}
	}

	private class Stack extends AbilityTimer {

		private final LivingEntity entity;
		private final IHologram hologram;
		private int stack = 0;

		private Stack(LivingEntity entity) {
			super(30);
			setPeriod(TimeUnit.TICKS, 4);
			this.entity = entity;
			this.hologram = NMS.newHologram(entity.getWorld(), entity.getLocation().getX(), entity.getLocation().getY() + entity.getEyeHeight() + 0.6, entity.getLocation().getZ(), Strings.repeat("§e✦", stack).concat(Strings.repeat("§e✧", 5 - stack)));
			hologram.display(getPlayer());
			stackMap.put(entity.getUniqueId(), this);
			addStack();
		}

		@Override
		protected void run(int count) {
			hologram.teleport(entity.getWorld(), entity.getLocation().getX(), entity.getLocation().getY() + entity.getEyeHeight() + 0.6, entity.getLocation().getZ(), entity.getLocation().getYaw(), 0);
		}

		private boolean addStack() {
			updateTime(getPlayer().getWorld());
			setCount(30);
			stack++;
			hologram.setText(Strings.repeat("§e✦", stack).concat(Strings.repeat("§e✧", 5 - stack)));
			if (stack >= 5) {
				stop(false);
				if (cooldownTimer.isRunning()) cooldownTimer.setCount(Math.max(cooldownTimer.getCount() - 10, 0));
				return true;
			} else {
				entity.setVelocity(getPlayer().getLocation().toVector().subtract(entity.getLocation().toVector()).multiply(0.6).setY(0));
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
