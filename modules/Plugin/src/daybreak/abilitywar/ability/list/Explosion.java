package daybreak.abilitywar.ability.list;

import com.google.common.base.Strings;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.object.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.geometry.Line;
import daybreak.abilitywar.utils.base.math.geometry.location.LocationIterator;
import daybreak.abilitywar.utils.base.minecraft.nms.IHologram;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
import daybreak.abilitywar.utils.library.SoundLib;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;

@AbilityManifest(name = "익스플로젼", rank = Rank.S, species = Species.HUMAN, explain = {
		"생명체를 공격하면 대상에게 §c폭발 표식§f이 쌓이며, §c표식§f이 $[MAX_STACK_CONFIG]개 이상 모이면",
		"폭발합니다. 철괴를 우클릭해 능력을 사용하면 §610초§f간 쌓아야 하는 최대 §c표식§f이 1개",
		"줄어들며, 지속 중에 생명체를 공격하면 주변 6칸 이내의 모든 생명체에게",
		"동시에 §c표식§f이 쌓입니다. $[COOLDOWN_CONFIG]",
		"폭발 대미지를 받지 않습니다."
})
@Beta
public class Explosion extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Explosion.class, "COOLDOWN", 40,
			"# 스킬 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}
	};

	public static final SettingObject<Integer> MAX_STACK_CONFIG = abilitySettings.new SettingObject<Integer>(Explosion.class, "MAXSTACK", 3,
			"# 쌓을 수 있는 최대 스택 수를 설정합니다.") {

		@Override
		public boolean condition(Integer value) {
			return value >= 2 && value <= 10;
		}

	};

	private static final RGB RED = RGB.of(209, 28, 8);
	private static final float EXPLOSION_POWER = 0.75f;

	public Explosion(Participant participant) {
		super(participant);
	}

	private final int MAX_STACK = MAX_STACK_CONFIG.getValue();
	private int maxStack = MAX_STACK;

	private final Predicate<Entity> predicate = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			if (entity.equals(getPlayer()) || entity.isDead() || !entity.isValid() || entity.hasMetadata("powderkeg")) return false;
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
	private final Map<UUID, Stack> stackMap = new HashMap<>();

	@SubscribeEvent(onlyRelevant = true)
	private void onEntityDamage(EntityDamageEvent e) {
		if (e.getCause() == DamageCause.BLOCK_EXPLOSION || e.getCause() == DamageCause.ENTITY_EXPLOSION) e.setCancelled(true);
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onEntityDamage(EntityDamageByBlockEvent e) {
		if (e.getCause() == DamageCause.BLOCK_EXPLOSION || e.getCause() == DamageCause.ENTITY_EXPLOSION) e.setCancelled(true);
	}

	@SubscribeEvent
	private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if ((e.getDamager() instanceof Projectile ? getPlayer().equals(((Projectile) e.getDamager()).getShooter()) : getPlayer().equals(e.getDamager())) && e.getEntity() instanceof LivingEntity && !e.isCancelled() && predicate.test(e.getEntity())) {
			if (duration.isRunning()) {
				for (LivingEntity livingEntity : LocationUtil.getNearbyEntities(LivingEntity.class, getPlayer().getLocation(), 6, 6, predicate)) {
					addStack(livingEntity, 1);
				}
			} else {
				addStack((LivingEntity) e.getEntity(), 1);
			}
		}
		if (getPlayer().equals(e.getEntity()) && e.getCause() == DamageCause.BLOCK_EXPLOSION || e.getCause() == DamageCause.ENTITY_EXPLOSION) e.setCancelled(true);
	}

	private void addStack(LivingEntity entity, int amount) {
		if (entity.isDead() || !entity.isValid()) return;
		if (stackMap.containsKey(entity.getUniqueId())) {
			stackMap.get(entity.getUniqueId()).addStack(amount);
		} else {
			final Stack stack = new Stack(entity);
			stack.start();
			stack.addStack(amount);
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

	@SubscribeEvent(onlyRelevant = true)
	private void onPlayerJoin(PlayerJoinEvent e) {
		for (Stack stack : stackMap.values()) {
			stack.hologram.display(getPlayer());
		}
	}

	private final Cooldown cooldown = new Cooldown(COOLDOWN_CONFIG.getValue());
	private final Duration duration = new Duration(10, cooldown) {
		@Override
		protected void onDurationStart() {
			maxStack = MAX_STACK - 1;
		}
		@Override
		protected void onDurationProcess(int count) {
			if (count % 2 == 0) SoundLib.ENTITY_GENERIC_EXPLODE.playSound(getPlayer());
		}
		@Override
		protected void onDurationEnd() {
			maxStack = MAX_STACK;
		}
		@Override
		protected void onDurationSilentEnd() {
			maxStack = MAX_STACK;
		}
	};

	@SubscribeEvent
	private void onProjectileHit(ProjectileHitEvent e) {
		if (e.getEntity().hasMetadata("explosion")) {
			e.getEntity().remove();
			final Location location = e.getHitEntity() != null ? e.getHitEntity().getLocation() : (e.getHitBlock() != null ? e.getHitBlock().getLocation() : null);
			if (location != null) {
				location.getWorld().createExplosion(location.getX(), location.getY(), location.getZ(), EXPLOSION_POWER, false, true);
				if (e.getHitEntity() != null) {
					e.getHitEntity().setVelocity(e.getHitEntity().getVelocity().setY(0));
				}
			}
		}
	}

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT) {
			if (clickType == ClickType.RIGHT_CLICK) {
				if (!duration.isDuration() && !cooldown.isCooldown()) {
					duration.start();
					return true;
				}
			} else if (clickType == ClickType.LEFT_CLICK) {
				new PowderKeg(getPlayer().getLocation());
				return true;
			}
		}
		return false;
	}

	private class Stack extends AbilityTimer {

		private final LivingEntity entity;
		private final IHologram hologram;
		private int stack = 0;

		private Stack(LivingEntity entity) {
			super(60);
			setPeriod(TimeUnit.TICKS, 6);
			this.entity = entity;
			this.hologram = NMS.newHologram(entity.getWorld(), entity.getLocation().getX(), entity.getLocation().getY() + entity.getEyeHeight() + 0.6, entity.getLocation().getZ(), Strings.repeat("§c▣", stack).concat(Strings.repeat("§c□", maxStack - stack)));
			hologram.display(getPlayer());
			stackMap.put(entity.getUniqueId(), this);
		}

		@Override
		protected void run(int count) {
			hologram.teleport(entity.getWorld(), entity.getLocation().getX(), entity.getLocation().getY() + entity.getEyeHeight() + 0.6, entity.getLocation().getZ(), entity.getLocation().getYaw(), 0);
		}

		private void addStack(int amount) {
			setCount(60);
			stack = Math.min(maxStack, stack + amount);
			hologram.setText(Strings.repeat("§c▣", stack).concat(Strings.repeat("§c□", maxStack - stack)));
			if (stack >= maxStack) {
				final Location location = entity.getLocation();
				entity.getWorld().createExplosion(location.getX(), location.getY(), location.getZ(), EXPLOSION_POWER, false, true);
				entity.setVelocity(entity.getVelocity().setY(0));
				stop(false);
				for (LivingEntity livingEntity : LocationUtil.getNearbyEntities(LivingEntity.class, entity.getLocation(), 6, 6, predicate)) {
					if (livingEntity.equals(entity)) continue;
					if (stackMap.containsKey(livingEntity.getUniqueId())) {
						stackMap.get(livingEntity.getUniqueId()).addStack(1);
					}
				}
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

	private final FixedMetadataValue NULL_VALUE = new FixedMetadataValue(AbilityWar.getPlugin(), null);
	private final Map<ArmorStand, PowderKeg> powderKegs = new HashMap<>();

	private class PowderKeg extends AbilityTimer implements Listener {

		private final Set<PowderKeg> connected = new HashSet<>();
		private final ArmorStand armorStand;

		private PowderKeg(final Location location) {
			setPeriod(TimeUnit.TICKS, 8);
			this.armorStand = location.getWorld().spawn(location, ArmorStand.class);
			armorStand.setGravity(false);
			armorStand.setMetadata("powderkeg", NULL_VALUE);
			armorStand.getEquipment().setHelmet(new ItemStack(Material.TNT));
			powderKegs.put(armorStand, this);
			for (ArmorStand nearby : LocationUtil.getNearbyEntities(ArmorStand.class, location, 9, 9, new Predicate<Entity>() {
				@Override
				public boolean test(Entity entity) {
					return !armorStand.equals(entity) && entity instanceof ArmorStand && powderKegs.containsKey(entity);
				}
			})) {
				final PowderKeg powderKeg = powderKegs.get(nearby);
				connected.add(powderKeg);
				powderKeg.connected.add(this);
			}
			Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
			start();
		}

		@Override
		protected void run(int count) {
			for (PowderKeg other : connected) {
				for (LocationIterator iterator = Line.iteratorBetween(armorStand.getLocation(), other.armorStand.getLocation(), 10); iterator.hasNext();) {
					ParticleLib.REDSTONE.spawnParticle(iterator.next(), RED);
				}
			}
		}

		@EventHandler
		private void onEntityDamage(EntityDamageEvent e) {
			if (e.getEntity().equals(armorStand)) e.setCancelled(true);
		}

		@EventHandler
		private void onEntityDamageByBlock(EntityDamageByBlockEvent e) {
			if (e.getEntity().equals(armorStand)) e.setCancelled(true);
		}

		@EventHandler
		private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
			if (e.getEntity().equals(armorStand) && !e.getDamager().equals(getPlayer())) {
				e.setCancelled(true);
			}
		}

		@EventHandler
		private void onEntityDeath(EntityDeathEvent e) {
			if (e.getEntity().equals(armorStand)) {
				stop(false);
			}
		}

		@Override
		protected void onEnd() {
			final Location loc = armorStand.getLocation();
			onSilentEnd();
			ParticleLib.EXPLOSION_LARGE.spawnParticle(loc);
			loc.getWorld().createExplosion(loc, 1.0f);
			for (LivingEntity nearby : LocationUtil.getNearbyEntities(LivingEntity.class, loc, 5, 5, predicate)) {
				nearby.setNoDamageTicks(0);
				nearby.getWorld().createExplosion(nearby.getLocation(), 1.0f);
				nearby.setVelocity(nearby.getVelocity().setY(0));
			}
			new AbilityTimer(2) {
				@Override
				protected void run(int count) {}
				@Override
				protected void onEnd() {
					for (PowderKeg powderKeg : connected) {
						powderKeg.stop(false);
					}
				}
			}.setPeriod(TimeUnit.TICKS, 1).start();
		}
		@Override
		protected void onSilentEnd() {
			armorStand.remove();
			powderKegs.remove(armorStand);
			HandlerList.unregisterAll(this);
		}
	}

}
