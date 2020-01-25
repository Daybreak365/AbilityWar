package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.event.AbilityRestrictionClearEvent;
import daybreak.abilitywar.config.ability.AbilitySettings;
import daybreak.abilitywar.game.games.mode.AbstractGame;
import daybreak.abilitywar.utils.DamageUtil;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.math.LocationUtil;
import daybreak.abilitywar.utils.math.geometry.Circle;
import daybreak.abilitywar.utils.math.geometry.Line;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@AbilityManifest(Name = "뱀파이어", Rank = AbilityManifest.Rank.A, Species = AbilityManifest.Species.UNDEAD)
public class Vampire extends AbilityBase {

	public static final AbilitySettings.SettingObject<Integer> CooldownConfig = new AbilitySettings.SettingObject<Integer>(Vampire.class, "Cool", 160,
			"# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public static final AbilitySettings.SettingObject<Integer> DurationConfig = new AbilitySettings.SettingObject<Integer>(Vampire.class, "Duration", 4,
			"# 지속시간 (초 단위)",
			"# 지속시간이 변할 경우 흡혈 횟수 또한 동일하게 변경됨. ( 1초 = 1회, 4초 = 4회 )") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public static final AbilitySettings.SettingObject<Integer> DistanceConfig = new AbilitySettings.SettingObject<Integer>(Vampire.class, "Distance", 7,
			"# 스킬 거리 (기본값: 7)") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public Vampire(AbstractGame.Participant participant) throws IllegalStateException {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 " + DurationConfig.getValue() + "초간 " + DistanceConfig.getValue() + "칸 안에 있는 생명체들에게서"),
				ChatColor.translateAlternateColorCodes('&', "&f체력을 &c반칸&f씩 " + DurationConfig.getValue() + "번 흡혈합니다. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&e밤&f에는 쿨타임이 더 빠르게 끝나며, 체력을 매번 &c반칸&f씩 더 흡혈합니다."));
	}

	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());
	private final int distance = DistanceConfig.getValue();
	private final Circle circle = Circle.of(distance, distance * 15);
	private static final ParticleLib.RGB COLOR_BLOOD_RED = new ParticleLib.RGB(138, 7, 7);
	private final DurationTimer skill = new DurationTimer(DurationConfig.getValue() * 10, cooldownTimer) {
		int count;
		HashMap<Damageable, Line> lines;
		ArrayList<Player> instrumentListeners;
		int blood;

		public void Target() {
			lines = new HashMap<>();
			instrumentListeners = new ArrayList<>();
			for (Damageable damageable : LocationUtil.getNearbyDamageableEntities(getPlayer(), distance, 250)) {
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
					lines.put(damageable, new Line(damageable.getLocation(), getPlayer().getLocation()));
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
			for (Map.Entry<Damageable, Line> entry : lines.entrySet()) {
				Location startLocation = entry.getKey().getLocation();
				try {
					ParticleLib.HEART.spawnParticle(entry.getValue().setVector(startLocation, getPlayer().getLocation()).getLocation(startLocation, count - 1));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}.setPeriod(2);

	private boolean isNight(long time) {
		return time > 12300 && time < 23850;
	}

	private final Timer passive = new Timer() {
		@Override
		protected void onProcess(int count) {
			if (cooldownTimer.isRunning()) {
				long time = getPlayer().getWorld().getTime();
				if (isNight(time)) {
					cooldownTimer.setCount(Math.max(cooldownTimer.getCount() - 1, 0));
				}
			}
		}
	}.setPeriod(10);

	@SubscribeEvent(onlyRelevant = true)
	public void onRestrictionClear(AbilityRestrictionClearEvent e) {
		passive.startTimer();
	}

	@Override
	public boolean ActiveSkill(Material materialType, ClickType ct) {
		if (materialType.equals(Material.IRON_INGOT) && ct.equals(ClickType.RIGHT_CLICK) && !skill.isDuration() && !cooldownTimer.isCooldown()) {
			skill.startTimer();
			return true;
		}
		return false;
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {

	}

}
