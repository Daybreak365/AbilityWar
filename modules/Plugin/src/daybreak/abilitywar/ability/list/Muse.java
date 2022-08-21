package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import kotlin.ranges.RangesKt;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.function.Predicate;

@AbilityManifest(name = "뮤즈", rank = Rank.S, species = Species.GOD, explain = {
		"§7철괴 우클릭 §8- §d축복§f: 자신을 중심으로 모두가 대미지를 받지 않는 지역을",
		" 만들어내며, 지역은 점차 축소됩니다. 지역 내에서 무시된 피해량의 10%만큼",
		" 체력을 회복하고, 체력이 가득 찬 경우 §e흡수 체력§7(§f최대 5칸§7)§f으로 회복합니다."
}, summarize = {
		"§7철괴 우클릭§f으로 §b무적 지대§f를 생성합니다. §b지역§f은 점차 축소됩니다.",
		"§b지역§f 내에서 무시된 피해량 일부를 §d회복§f하고, 5칸까지 §e흡수 체력§f으로 오버힐 가능합니다."
})
public class Muse extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Muse.class, "cooldown", 80,
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

	public Muse(Participant participant) {
		super(participant);
	}

	private static final Note D = Note.natural(0, Tone.D), FSharp = Note.sharp(1, Tone.F), LowA = Note.natural(0, Tone.A), A = Note.natural(1, Tone.A);

	private static final Circle headCircle = Circle.of(0.5, 10);
	private static final RGB PINK = RGB.of(255, 189, 235);
	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());
	private final Predicate<Entity> ONLY_PARTICIPANTS = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			return (!(entity instanceof Player)) || (getGame().isParticipating(entity.getUniqueId())
					&& (!(getGame() instanceof DeathManager.Handler) || !((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
					&& getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue());
		}
	};
	private double currentRadius;
	private Location center = null;

	private final Duration skill = new Duration(120, cooldownTimer) {

		private int count;
		private int soundCount;

		@Override
		public void onDurationStart() {
			count = 1;
			soundCount = 1;
			currentRadius = 11;
			center = getPlayer().getLocation();
		}

		@Override
		public void onDurationProcess(int seconds) {
			if (count <= 10) {
				double playerY = getPlayer().getLocation().getY();
				for (Iterator<Location> iterator = Circle.iteratorOf(center, count, count * 16); iterator.hasNext(); ) {
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

				SoundLib.BELL.playInstrument(LocationUtil.getNearbyEntities(Player.class, center, 20, 20, ONLY_PARTICIPANTS), note);
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

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT) {
			if (clickType == ClickType.RIGHT_CLICK) {
				if (!skill.isDuration() && !cooldownTimer.isCooldown()) {
					skill.start();
					return true;
				}
			}
		}

		return false;
	}

	@SubscribeEvent(ignoreCancelled = true, priority = 6, childs = {EntityDamageByEntityEvent.class, EntityDamageByBlockEvent.class})
	public void onEntityDamage(EntityDamageEvent e) {
		if (center != null) {
			if (LocationUtil.isInCircle(center, e.getEntity().getLocation(), currentRadius)) {
				ParticleLib.HEART.spawnParticle(e.getEntity().getLocation(), 2, 2, 2, 5);
				e.setCancelled(true);
				final double amount = e.getFinalDamage() / 10;
				final EntityRegainHealthEvent event = new EntityRegainHealthEvent(getPlayer(), amount, RegainReason.CUSTOM);
				Bukkit.getPluginManager().callEvent(event);
				if (!event.isCancelled()) {
					final double maxHealth = getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
					final double empty = maxHealth - getPlayer().getHealth();
					if (empty >= event.getAmount()) {
						getPlayer().setHealth(RangesKt.coerceIn(getPlayer().getHealth() + event.getAmount(), 0, maxHealth));
					} else {
						getPlayer().setHealth(maxHealth);
						if (NMS.getAbsorptionHearts(getPlayer()) < 10) {
							NMS.setAbsorptionHearts(getPlayer(), Math.min(NMS.getAbsorptionHearts(getPlayer()) + (float) (event.getAmount() - empty), 10));
						}
					}
				}
			}
		}
	}

}
