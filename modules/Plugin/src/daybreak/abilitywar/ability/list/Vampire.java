package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.Scheduled;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.base.math.geometry.Line;
import daybreak.abilitywar.utils.base.minecraft.DamageUtil;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
import daybreak.abilitywar.utils.library.SoundLib;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

@AbilityManifest(name = "뱀파이어", rank = AbilityManifest.Rank.A, species = AbilityManifest.Species.UNDEAD, explain = {
		"철괴를 우클릭하면 $[DurationConfig]초간 $[DistanceConfig]칸 안에 있는 생명체들에게서",
		"체력을 §c반칸§f씩 $[DurationConfig]번 흡혈합니다. $[CooldownConfig]",
		"§e밤§f에는 쿨타임이 더 빠르게 끝나며, 체력을 매번 §c반칸§f씩 더 흡혈합니다."
})
public class Vampire extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> CooldownConfig = abilitySettings.new SettingObject<Integer>(Vampire.class, "Cool", 160,
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

	public static final SettingObject<Integer> DurationConfig = abilitySettings.new SettingObject<Integer>(Vampire.class, "Duration", 4,
			"# 지속시간 (초 단위)",
			"# 지속시간이 변할 경우 흡혈 횟수 또한 동일하게 변경됨. ( 1초 = 1회, 4초 = 4회 )") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

	};

	public static final SettingObject<Integer> DistanceConfig = abilitySettings.new SettingObject<Integer>(Vampire.class, "Distance", 7,
			"# 스킬 거리 (기본값: 7)") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

	};

	public Vampire(AbstractGame.Participant participant) {
		super(participant);
	}

	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());
	private final int distance = DistanceConfig.getValue();
	private final Circle circle = Circle.of(distance, distance * 15);
	private static final RGB COLOR_BLOOD_RED = new RGB(138, 7, 7);
	private final DurationTimer skill = new DurationTimer(DurationConfig.getValue() * 10, cooldownTimer) {
		int count;
		List<Damageable> targets;
		List<Player> instrumentListeners;
		int blood;

		public void Target() {
			targets = LocationUtil.getNearbyDamageableEntities(getPlayer(), distance, 250);
			instrumentListeners = new ArrayList<>();
			for (Damageable damageable : targets) {
				if (!damageable.isDead() && DamageUtil.canDamage(getPlayer(), damageable, EntityDamageEvent.DamageCause.MAGIC, 1)) {
					damageable.damage(0);
					final int amount;
					if (isNight(getPlayer().getWorld().getTime())) {
						amount = 2;
					} else {
						amount = 1;
					}
					damageable.setHealth(Math.max(damageable.getHealth() - amount, 0));
					blood += amount;
					if (damageable instanceof Player) instrumentListeners.add((Player) damageable);
				}
			}
		}

		@Override
		protected void onDurationStart() {
			count = 1;
		}

		@Override
		protected void onDurationProcess(int seconds) {
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
				Target();
				count++;
			} else if (count < 10) {
				count++;
			} else {
				if (!getPlayer().isDead()) {
					getPlayer().setHealth(Math.min(getPlayer().getHealth() + blood, 20));
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
		}
	}.setPeriod(TimeUnit.TICKS, 2);

	private boolean isNight(long time) {
		return time > 12300 && time < 23850;
	}

	@Scheduled
	private final Timer passive = new Timer() {
		@Override
		protected void run(int count) {
			if (cooldownTimer.isRunning()) {
				long time = getPlayer().getWorld().getTime();
				if (isNight(time)) {
					cooldownTimer.setCount(Math.max(cooldownTimer.getCount() - 1, 0));
				}
			}
		}
	}.setPeriod(TimeUnit.TICKS, 10);

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT) && clickType.equals(ClickType.RIGHT_CLICK) && !skill.isDuration() && !cooldownTimer.isCooldown()) {
			skill.start();
			return true;
		}
		return false;
	}

}
