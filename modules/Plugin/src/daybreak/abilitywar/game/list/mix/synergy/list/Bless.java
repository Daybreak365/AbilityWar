package daybreak.abilitywar.game.list.mix.synergy.list;

import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.LocationUtil.Predicates;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import java.util.Iterator;
import java.util.function.Predicate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

@AbilityManifest(name = "축복", rank = Rank.S, species = Species.GOD, explain = {
		"철괴를 우클릭하면 광범위한 지역을 축복하여",
		"모두가 대미지를 받지 않는 지역을 만들어냅니다. $[CooldownConfig]",
		"지역은 점점 줄어들며, 지속 시간이 끝나면 사라집니다."
})
public class Bless extends Synergy implements ActiveHandler {

	public static final SettingObject<Integer> CooldownConfig = synergySettings.new SettingObject<Integer>(Bless.class, "Cooldown", 80,
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
	private static final Circle headCircle = Circle.of(0.5, 10);
	private static final RGB PINK = RGB.of(255, 189, 235);
	private static final Note D = Note.natural(0, Tone.D);
	private static final Note FSharp = Note.sharp(1, Tone.F);
	private static final Note LowA = Note.natural(0, Tone.A);
	private static final Note A = Note.natural(1, Tone.A);
	private static final Predicate<Entity> ONLY_PARTICIPANTS = Predicates.PARTICIPANTS();
	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());
	private double currentRadius;
	private Location center = null;
	private final DurationTimer skill = new DurationTimer(250, cooldownTimer) {

		private int count;
		private int soundCount;

		@Override
		public void onDurationStart() {
			count = 1;
			soundCount = 1;
			currentRadius = 35;
			center = getPlayer().getLocation();
		}

		@Override
		public void onDurationProcess(int seconds) {
			if (count <= 10) {
				double playerY = getPlayer().getLocation().getY();
				for (Iterator<Location> iterator = Circle.iteratorOf(center, currentRadius * (count / 10.0), count * 16); iterator.hasNext(); ) {
					Location loc = iterator.next();
					loc.setY(LocationUtil.getFloorYAt(loc.getWorld(), playerY, loc.getBlockX(), loc.getBlockZ()) + 0.1);
					ParticleLib.REDSTONE.spawnParticle(loc, PINK);
				}

				final Note note;
				switch (count) {
					case 1:
					case 4:
					case 7:
					case 10:
						note = D;
						break;
					case 2:
					case 6:
					case 8:
						note = FSharp;
						break;
					case 3:
					case 5:
					case 9:
						note = LowA;
						break;
					default:
						note = null;
						break;
				}

				SoundLib.BELL.playInstrument(LocationUtil.getNearbyPlayers(center, 20, 20), note);
			} else {
				if (currentRadius > 1) currentRadius -= 0.115;
				double playerY = getPlayer().getLocation().getY();
				for (Iterator<Location> iterator = Circle.iteratorOf(center, currentRadius, (int) (currentRadius * 16)); iterator.hasNext(); ) {
					Location loc = iterator.next();
					loc.setY(LocationUtil.getFloorYAt(loc.getWorld(), playerY, loc.getBlockX(), loc.getBlockZ()) + 0.1);
					ParticleLib.REDSTONE.spawnParticle(loc, PINK);
				}

				if (soundCount % 5 == 0) {
					soundCount = 1;
					for (LivingEntity livingEntity : LocationUtil.getEntitiesInCircle(LivingEntity.class, center, currentRadius, ONLY_PARTICIPANTS)) {
						PotionEffects.GLOWING.addPotionEffect(livingEntity, 4, 0, true);
						if (livingEntity instanceof Player) SoundLib.BELL.playInstrument((Player) livingEntity, A);
					}
				} else {
					for (LivingEntity livingEntity : LocationUtil.getEntitiesInCircle(LivingEntity.class, center, currentRadius, ONLY_PARTICIPANTS)) {
						PotionEffects.GLOWING.addPotionEffect(livingEntity, 4, 0, true);
					}
				}
				soundCount++;
			}
			ParticleLib.NOTE.spawnParticle(getPlayer().getEyeLocation().clone().add(0, 0.6, 0).add(headCircle.get(count % 10)));
			count++;
		}

		@Override
		public void onDurationEnd() {
			for (Player player : LocationUtil.getEntitiesInCircle(Player.class, center, currentRadius, ONLY_PARTICIPANTS)) {
				SoundLib.BELL.playInstrument(player, D);
				SoundLib.BELL.playInstrument(player, FSharp);
				SoundLib.BELL.playInstrument(player, A);
			}
			center = null;
		}

		@Override
		public void onDurationSilentEnd() {
			center = null;
		}

	}.setPeriod(TimeUnit.TICKS, 2);

	public Bless(Participant participant) {
		super(participant);
	}

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT)) {
			if (clickType.equals(ClickType.RIGHT_CLICK)) {
				if (!skill.isDuration() && !cooldownTimer.isCooldown()) {
					skill.start();

					return true;
				}
			}
		}

		return false;
	}

	@SubscribeEvent
	public void onEntityDamage(EntityDamageEvent e) {
		if (center != null) {
			if (LocationUtil.isInCircle(center, e.getEntity().getLocation(), currentRadius)) {
				ParticleLib.HEART.spawnParticle(e.getEntity().getLocation(), 2, 2, 2, 5);
				e.setCancelled(true);
			}
		}
	}

	@SubscribeEvent
	public void onEntityDamage(EntityDamageByEntityEvent e) {
		if (center != null) {
			if (LocationUtil.isInCircle(center, e.getEntity().getLocation(), currentRadius)) {
				ParticleLib.HEART.spawnParticle(e.getEntity().getLocation(), 2, 2, 2, 5);
				e.setCancelled(true);
			}
		}
	}

	@SubscribeEvent
	public void onEntityDamage(EntityDamageByBlockEvent e) {
		if (center != null) {
			if (LocationUtil.isInCircle(center, e.getEntity().getLocation(), currentRadius)) {
				ParticleLib.HEART.spawnParticle(e.getEntity().getLocation(), 2, 2, 2, 5);
				e.setCancelled(true);
			}
		}
	}

}
