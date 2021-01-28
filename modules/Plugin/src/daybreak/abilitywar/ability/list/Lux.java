package daybreak.abilitywar.ability.list;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.module.Wreck;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

@AbilityManifest(name = "루스", rank = Rank.S, species = Species.GOD)
@Beta
public class Lux extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> DURATION_CONFIG = abilitySettings.new SettingObject<Integer>(Lux.class, "duration", 6, "# 지속 시간") {
		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}
	};

	public static final SettingObject<Double> RADIUS_CONFIG = abilitySettings.new SettingObject<Double>(Lux.class, "radius", 7.0, "# 범위") {
		@Override
		public boolean condition(Double value) {
			return value >= 1;
		}
	};

	public static final SettingObject<Integer> MAX_CHARGE_CONFIG = abilitySettings.new SettingObject<Integer>(Lux.class, "max-charge", 20, "# 최고 스택 설정") {
		@Override
		public boolean condition(Integer value) {
			return value >= 4 && value <= 50;
		}
	};

	private static final RGB PINK = RGB.of(255, 189, 235), AQUA = RGB.of(166, 255, 252);
	private static final Note FSharp = Note.sharp(1, Tone.F), ASharp = Note.sharp(1, Tone.A), CSharp = Note.sharp(1, Tone.C);

	private static final Set<Material> swords;

	static {
		if (MaterialX.NETHERITE_SWORD.isSupported()) {
			swords = ImmutableSet.of(MaterialX.WOODEN_SWORD.getMaterial(), Material.STONE_SWORD, Material.IRON_SWORD, MaterialX.GOLDEN_SWORD.getMaterial(), Material.DIAMOND_SWORD, MaterialX.NETHERITE_SWORD.getMaterial());
		} else {
			swords = ImmutableSet.of(MaterialX.WOODEN_SWORD.getMaterial(), Material.STONE_SWORD, Material.IRON_SWORD, MaterialX.GOLDEN_SWORD.getMaterial(), Material.DIAMOND_SWORD);
		}
	}

	private final Predicate<Entity> predicate = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			return (!(entity instanceof Player)) || (getGame().isParticipating(entity.getUniqueId())
					&& (!(getGame() instanceof DeathManager.Handler) || !((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
					&& getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue());
		}
	};

	private final double radius = RADIUS_CONFIG.getValue();
	private final int duration = DURATION_CONFIG.getValue(), tenTimesDuration = duration * 10, maxCharge = MAX_CHARGE_CONFIG.getValue();
	private final Circle circle = Circle.of(radius, (int) (radius * 17));
	private final Charge charge = new Charge();
	private final Set<Zone> zones = new HashSet<>();
	private boolean state = false;

	public Lux(Participant participant) {
		super(participant);
	}

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			charge.start();
		}
	}

	private class Charge extends AbilityTimer {

		private final ActionbarChannel actionbarChannel = newActionbarChannel();
		private int charges = 0;

		private Charge() {
			super();
			setPeriod(TimeUnit.TICKS, (int) (20 * Wreck.calculateDecreasedAmount(50)));
			actionbarChannel.update(toString());
		}

		private boolean subtractCharge(final int amount) {
			if (charges >= amount) {
				charges = Math.max(0, charges - amount);
				actionbarChannel.update(toString());
				return true;
			} else return false;
		}

		@Override
		protected void run(int count) {
			final int step = count % 8;
			if (step == 0) {
				charges = Math.min(maxCharge, charges + 1);
			}
			actionbarChannel.update(ChatColor.LIGHT_PURPLE + Strings.repeat(">", step) + ChatColor.GRAY + Strings.repeat(">", 7 - step) + " §f" + charges);
		}

		@Override
		protected void onSilentEnd() {
			actionbarChannel.update(null);
		}

		@Override
		public String toString() {
			final int count = getCount(), step = count >= 0 ? getCount() % 8 : 0;
			return ChatColor.LIGHT_PURPLE + Strings.repeat(">", step) + ChatColor.GRAY + Strings.repeat(">", 7 - step) + " §f" + charges;
		}
	}

	private RGB getColor(boolean bool) {
		return state ? (bool ? PINK : AQUA) : (bool ? AQUA : PINK);
	}

	@Override
	public boolean ActiveSkill(Material material, ClickType clickType) {
		if (material == Material.IRON_INGOT) {
			if (clickType == ClickType.RIGHT_CLICK) {
				if (zones.size() <= 7) {
					if (charge.subtractCharge(4)) {
						new Zone(getPlayer().getLocation());
					}
				} else {
					getPlayer().sendMessage("§f8개 §5이상의 무적 지역을 생성할 수 없습니다.");
				}
			}
		} else if (swords.contains(material)) {
			if (clickType == ClickType.RIGHT_CLICK) {
				if (charge.subtractCharge(1)) {
					this.state = !state;
					SoundLib.PIANO.playInstrument(getPlayer().getLocation(), .4f, FSharp);
					SoundLib.PIANO.playInstrument(getPlayer().getLocation(), .4f, ASharp);
					SoundLib.PIANO.playInstrument(getPlayer().getLocation(), .4f, CSharp);
				}
			}
		}
		return false;
	}

	@Override
	public boolean usesMaterial(Material material) {
		return super.usesMaterial(material) || swords.contains(material);
	}

	private class Zone extends AbilityTimer implements Listener {

		private final Location center;
		private final Map<Location, Boolean> dots = new HashMap<>();

		private Zone(final Location center) {
			super(TaskType.REVERSE, tenTimesDuration);
			setPeriod(TimeUnit.TICKS, 2);
			this.center = center.clone();
			zones.add(this);
			for (Zone zone : zones) {
				if (!this.equals(zone)) {
					zone.setCount(zone.getCount() + tenTimesDuration);
				}
				zone.recalculateDots();
			}
			start();
		}

		@Override
		protected void onStart() {
			Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		}

		@EventHandler(ignoreCancelled = true)
		private void onEntityDamage(final EntityDamageEvent e) {
			if (isApplied(e.getEntity())) {
				e.setCancelled(true);
				ParticleLib.HEART.spawnParticle(e.getEntity().getLocation(), 1, 1, 1, 3);
			}
		}

		private void recalculateDots() {
			dots.clear();
			for (Location loc : circle.toLocations(center)) {
				final int zones = getOverlappedZones(loc);
				if (zones <= 1) {
					dots.put(loc, zones != 0);
				}
			}
		}

		private boolean isApplied(final Entity entity) {
			return LocationUtil.isInCircle(center, entity.getLocation(), radius) && state == isInAnyZone(entity.getLocation());
		}

		private boolean isInAnyZone(final Location location) {
			for (Zone zone : zones) {
				if (zone.equals(this)) continue;
				if (LocationUtil.isInCircle(zone.center, location, radius)) {
					return true;
				}
			}
			return false;
		}

		private int getOverlappedZones(final Location location) {
			int count = 0;
			for (Zone zone : zones) {
				if (zone.equals(this)) continue;
				if (LocationUtil.isInCircle(zone.center, location, radius)) {
					count++;
				}
			}
			return count;
		}

		@Override
		protected void run(int count) {
			final double criterionY = getPlayer().getLocation().getY();
			for (Entry<Location, Boolean> entry : dots.entrySet()) {
				ParticleLib.REDSTONE.spawnParticle(LocationUtil.floorY(entry.getKey(), criterionY), getColor(entry.getValue()));
			}
			for (LivingEntity livingEntity : LocationUtil.getEntitiesInCircle(LivingEntity.class, center, radius, predicate)) {
				if (!isApplied(livingEntity)) continue;
				PotionEffects.GLOWING.addPotionEffect(livingEntity, 4, 0, true, false, true);
			}
		}

		@Override
		protected void onEnd() {
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			HandlerList.unregisterAll(this);
			zones.remove(this);
			for (Zone zone : zones) {
				zone.recalculateDots();
			}
		}
	}

}
