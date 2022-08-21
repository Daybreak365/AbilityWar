package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.collect.Pair;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.base.math.geometry.Line;
import daybreak.abilitywar.utils.base.math.geometry.Wing;
import daybreak.abilitywar.utils.base.minecraft.damage.Damages;
import daybreak.abilitywar.utils.base.minecraft.entity.health.Healths;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import kotlin.ranges.RangesKt;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@AbilityManifest(name = "뱀파이어", rank = AbilityManifest.Rank.A, species = AbilityManifest.Species.UNDEAD, explain = {
		"철괴를 우클릭하면 $[DURATION_CONFIG]초간 주위 $[DISTANCE_CONFIG]칸 안에 있는 생명체들에게서",
		"체력을 §c반칸§f씩 $[DURATION_CONFIG]번 흡혈합니다. $[COOLDOWN_CONFIG]",
		"§e밤§f에는 쿨타임이 더 빠르게 끝나며, 체력을 매번 §c반칸§f씩 더 흡혈해",
		"§c한칸§f씩 흡혈합니다. 능력 사용 중에는 땅 위에서 느리게 날 수 있습니다."
}, summarize = {
		"§7철괴 우클릭§f 시 저공 비행하며 광범위 내 생명체들의 체력을 §c흡혈§f합니다.",
		"§9밤§f에는 §c쿨타임§f이 줄어들고 §c흡혈량§f이 2배가 됩니다."
})
public class Vampire extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> DISTANCE_CONFIG = abilitySettings.new SettingObject<Integer>(Vampire.class, "distance", 12,
			"# 스킬 거리 (기본값: 12)") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

	};

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Vampire.class, "cooldown", 160,
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

	public static final SettingObject<Integer> DURATION_CONFIG = abilitySettings.new SettingObject<Integer>(Vampire.class, "duration", 4,
			"# 지속시간 (초 단위)",
			"# 지속시간이 변할 경우 흡혈 횟수 또한 동일하게 변경됨. ( 1초 = 1회, 4초 = 4회 )") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

	};
	private static final Pair<Wing, Wing> VAMPIRE_WING = Wing.of(new boolean[][]{
			{false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, true, true, true, true, true, false, false, false},
			{false, false, true, true, true, true, false, false, false, false, false, false},
			{false, true, true, true, true, true, false, false, false, false, false, false},
			{true, true, true, true, true, true, true, false, false, false, false, false},
			{true, true, true, true, true, true, true, true, true, true, false, false},
			{true, true, false, true, true, true, true, true, true, true, true, false},
			{true, true, false, true, true, true, true, true, true, true, true, true},
			{true, false, true, false, true, true, true, true, true, true, true, false},
			{true, true, true, false, true, false, true, false, false, false, false, false},
			{true, true, true, true, true, true, true, false, false, false, false, false},
			{true, true, true, false, false, false, false, false, false, false, false, false},
			{false, true, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false}
	});

	public Vampire(AbstractGame.Participant participant) {
		super(participant);
	}

	private final Predicate<Entity> STRICT_PREDICATE = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			if (entity.equals(getPlayer())) return false;
			if (entity instanceof Player) {
				if (!getGame().isParticipating(entity.getUniqueId())
						|| (getGame() instanceof DeathManager.Handler && ((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
						|| !getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue()) {
					return false;
				}
				if (!Damages.canDamage(entity, getPlayer(), DamageCause.MAGIC, 1)) {
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
	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());
	private final int distance = DISTANCE_CONFIG.getValue();
	private final Circle circle = Circle.of(distance, distance * 15);
	private static final RGB COLOR_BLOOD_RED = new RGB(138, 7, 7);
	private final Duration skill = new Duration(DURATION_CONFIG.getValue() * 10, cooldownTimer) {
		private int count;
		private double blood;
		private List<Damageable> targets;
		private List<Player> instrumentListeners;
		private Wing leftWing, rightWing;

		public void target() {
			this.targets = LocationUtil.getNearbyEntities(Damageable.class, getPlayer().getLocation(), distance, 250, STRICT_PREDICATE);
			this.instrumentListeners = new ArrayList<>();
			this.leftWing = VAMPIRE_WING.getLeft().clone();
			this.rightWing = VAMPIRE_WING.getRight().clone();
			for (Damageable damageable : targets) {
				if (damageable.isDead()) continue;
				damageable.damage(0);
				final double amount;
				if (isNight(getPlayer().getWorld().getTime())) {
					amount = 2;
				} else {
					amount = 1;
				}
				blood += amount;
				if (damageable instanceof Player) {
					final Player player = (Player) damageable;
					instrumentListeners.add(player);
					Healths.setHealth(player, player.getHealth() - amount);
				} else {
					damageable.setHealth(Math.max(damageable.getHealth() - amount, 0));
				}
			}
		}

		@Override
		protected void onDurationStart() {
			count = 1;
		}

		@Override
		protected void onDurationProcess(int seconds) {
			getPlayer().setFlySpeed(0.2f);
			getPlayer().setAllowFlight(true);
			getPlayer().setFlying(true);
			if (count % 5 == 0) {
				instrumentListeners.add(getPlayer());
				SoundLib.PIANO.playInstrument(instrumentListeners, Note.natural(0, Note.Tone.B));
				SoundLib.PIANO.playInstrument(instrumentListeners, Note.sharp(0, Note.Tone.D));
				if (isNight(getPlayer().getWorld().getTime())) {
					SoundLib.PIANO.playInstrument(instrumentListeners, Note.natural(0, Note.Tone.G));
				}
				SoundLib.PIANO.playInstrument(instrumentListeners, Note.sharp(0, Note.Tone.G));
				SoundLib.BASS_DRUM.playInstrument(instrumentListeners, Note.natural(0, Note.Tone.B));
			}

			if (count == 1) {
				target();
				count++;
			} else if (count < 10) {
				count++;
			} else {
				if (!getPlayer().isDead()) {
					final EntityRegainHealthEvent event = new EntityRegainHealthEvent(getPlayer(), blood, RegainReason.CUSTOM);
					Bukkit.getPluginManager().callEvent(event);
					if (!event.isCancelled()) {
						getPlayer().setHealth(RangesKt.coerceIn(getPlayer().getHealth() + event.getAmount(), 0, getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
					}
				}
				blood = 0;
				count = 1;
			}
			for (Location location : circle.toLocations(getPlayer().getLocation()).floor(getPlayer().getLocation().getY())) {
				ParticleLib.REDSTONE.spawnParticle(location, COLOR_BLOOD_RED);
			}
			for (Damageable target : targets) {
				Location startLocation = target.getLocation();
				ParticleLib.HEART.spawnParticle(startLocation.clone().add(Line.vectorAt(startLocation, getPlayer().getLocation(), 10, count - 1)));
			}

			float yaw = getPlayer().getLocation().getYaw();
			for (Location loc : leftWing.rotateAroundAxisY(-yaw + 30).toLocations(getPlayer().getLocation().clone().subtract(0, 0.5, 0))) {
				ParticleLib.REDSTONE.spawnParticle(loc, COLOR_BLOOD_RED);
			}
			leftWing.rotateAroundAxisY(yaw - 30);
			for (Location loc : rightWing.rotateAroundAxisY(-yaw - 30).toLocations(getPlayer().getLocation().clone().subtract(0, 0.5, 0))) {
				ParticleLib.REDSTONE.spawnParticle(loc, COLOR_BLOOD_RED);
			}
			rightWing.rotateAroundAxisY(yaw + 30);
			final Location playerLocation = getPlayer().getLocation();
			final double floorY = LocationUtil.getFloorYAt(getPlayer().getWorld(), playerLocation.getY(), playerLocation.getBlockX(), playerLocation.getBlockZ()) + 2;
			getPlayer().setVelocity(getPlayer().getVelocity().setY(floorY > playerLocation.getY() ? 0.05 : (floorY == playerLocation.getY() ? 0 : -0.05)));
		}

		@Override
		public void onDurationEnd() {
			onDurationSilentEnd();
		}

		@Override
		protected void onDurationSilentEnd() {
			getPlayer().setFlying(false);
			getPlayer().setFlySpeed(.1f);
			GameMode mode = getPlayer().getGameMode();
			getPlayer().setAllowFlight(mode != GameMode.SURVIVAL && mode != GameMode.ADVENTURE);
		}

	}.setPeriod(TimeUnit.TICKS, 2);

	private boolean isNight(long time) {
		return time > 12300 && time < 23850;
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onMove(PlayerMoveEvent e) {
		if (!skill.isRunning()) return;
		final Location playerLocation = getPlayer().getLocation();
		final double floorY = LocationUtil.getFloorYAt(getPlayer().getWorld(), playerLocation.getY(), playerLocation.getBlockX(), playerLocation.getBlockZ()) + 3;
		if ((e.getFrom().getY() < floorY && e.getTo().getY() > floorY) || (e.getFrom().getY() > floorY && e.getTo().getY() > e.getFrom().getY())) {
			e.getTo().setY(e.getFrom().getY());
		}
	}

	private final AbilityTimer passive = new AbilityTimer() {
		@Override
		protected void run(int count) {
			if (cooldownTimer.isRunning()) {
				long time = getPlayer().getWorld().getTime();
				if (isNight(time)) {
					cooldownTimer.setCount(Math.max(cooldownTimer.getCount() - 1, 0));
				}
			}
		}
	}.setPeriod(TimeUnit.TICKS, 10).register();

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			passive.start();
		}
	}

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !skill.isDuration() && !cooldownTimer.isCooldown()) {
			skill.start();
			return true;
		}
		return false;
	}

}
