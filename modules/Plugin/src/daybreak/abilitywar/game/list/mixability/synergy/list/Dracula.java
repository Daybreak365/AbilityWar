package daybreak.abilitywar.game.list.mixability.synergy.list;

import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.Scheduled;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.list.mixability.synergy.Synergy;
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
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

@AbilityManifest(name = "드라큘라 백작", rank = AbilityManifest.Rank.S, species = AbilityManifest.Species.UNDEAD, explain = {
		"철괴를 우클릭하면 $[DurationConfig]초간 $[DistanceConfig]칸 안에 있는 생명체들에게서",
		"체력을 §c반칸§f씩 $[DurationConfig]번 흡혈합니다. $[CooldownConfig]",
		"§e밤§f에는 쿨타임이 더 빠르게 끝나며, 체력을 매번 §c반 칸§f씩 더 흡혈합니다.",
		"철괴를 좌클릭하면 자신의 체력을 세 칸 소모하여 주위에 있는 생명체들에게",
		"$[DurationConfig]초간 $[DurationConfig]번에 걸쳐 매번 한칸 반의 방어력 관통",
		"대미지를 줍니다."
})
public class Dracula extends Synergy implements ActiveHandler {

	public static final SettingObject<Integer> CooldownConfig = synergySettings.new SettingObject<Integer>(Dracula.class, "Cool", 160,
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

	public static final SettingObject<Integer> DurationConfig = synergySettings.new SettingObject<Integer>(Dracula.class, "Duration", 5,
			"# 지속시간 (초 단위)",
			"# 지속시간이 변할 경우 흡혈 횟수 또한 동일하게 변경됨. ( 1초 = 1회, 5초 = 5회 )") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

	};

	public static final SettingObject<Integer> DistanceConfig = synergySettings.new SettingObject<Integer>(Dracula.class, "Distance", 7,
			"# 스킬 거리 (기본값: 7)") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

	};
	private static final RGB COLOR_BLOOD_RED = new RGB(138, 7, 7);
	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());
	private final int distance = DistanceConfig.getValue();
	private final Circle circle = Circle.of(distance, distance * 15);
	private final DurationTimer skill = new DurationTimer(DurationConfig.getValue() * 10, cooldownTimer, "흡혈") {
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
	private final DurationTimer rightSkill = new DurationTimer(DurationConfig.getValue() * 10, cooldownTimer, "공격") {
		int count;
		List<Damageable> targets;
		List<Player> instrumentListeners;

		public void Target() {
			targets = LocationUtil.getNearbyDamageableEntities(getPlayer(), distance, 250);
			instrumentListeners = new ArrayList<>();
			for (Damageable damageable : targets) {
				if (!damageable.isDead() && DamageUtil.canDamage(getPlayer(), damageable, EntityDamageEvent.DamageCause.MAGIC, 1)) {
					damageable.damage(0);
					damageable.setHealth(Math.max(damageable.getHealth() - 3, 0));
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
				count = 1;
			}
			for (Location location : circle.toLocations(getPlayer().getLocation()).floor(getPlayer().getLocation().getY())) {
				ParticleLib.REDSTONE.spawnParticle(location, COLOR_BLOOD_RED);
			}
			for (Damageable target : targets) {
				Location startLocation = target.getLocation();
				ParticleLib.HEART.spawnParticle(getPlayer().getLocation().clone().add(Line.vectorAt(getPlayer().getLocation(), startLocation, 10, count - 1)));
			}
		}
	}.setPeriod(TimeUnit.TICKS, 2);
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

	public Dracula(AbstractGame.Participant participant) {
		super(participant);
	}

	private boolean isNight(long time) {
		return time > 12300 && time < 23850;
	}

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT)) {
			if (clickType.equals(ClickType.RIGHT_CLICK)) {
				if (!skill.isDuration() && !rightSkill.isDuration() && !cooldownTimer.isCooldown()) {
					skill.start();
					return true;
				}
			} else if (clickType.equals(ClickType.LEFT_CLICK)) {
				if (!rightSkill.isDuration() && !skill.isDuration() && !cooldownTimer.isCooldown()) {
					if (getPlayer().getHealth() > 6) {
						getPlayer().setHealth(Math.max(getPlayer().getHealth() - 6, 0));
						rightSkill.start();
						return true;
					} else getPlayer().sendMessage(ChatColor.RED + "세 칸 이상의 체력이 필요합니다.");
				}
			}
		}
		return false;
	}

}
