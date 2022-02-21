package daybreak.abilitywar.ability.list;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.config.enums.CooldownDecrease;
import daybreak.abilitywar.game.AbstractGame.Effect;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.effect.event.ParticipantEffectApplyEvent;
import daybreak.abilitywar.game.manager.effect.registry.EffectType;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.FastMath;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.VectorUtil;
import daybreak.abilitywar.utils.base.minecraft.entity.health.event.PlayerSetHealthEvent;
import daybreak.abilitywar.utils.base.minecraft.nms.IWorldBorder;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.library.ParticleLib;
import kotlin.ranges.RangesKt;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Predicate;

@AbilityManifest(name = "버서커", rank = Rank.S, species = Species.HUMAN, explain = {
		"§7철괴 우클릭 §8- §c불굴의 의지§f: $[MAX_DURATION_CONFIG]초간 이동을 방해하는 모든 §5상태 이상§f을 해제하고",
		" 이동 속도가 65% 증가하며, 근접 공격이 강화되어 $[DAMAGE_FACTOR_CONFIG]%의 추가 피해를 입힙니다.",
		" 불굴의 의지가 지속되는 도중에는 체력이 반 칸 아래로 떨어지지 않습니다.",
		" 지속 도중 능력을 다시 사용하거나 강화 피해를 입히면 스킬이 즉시 종료되며,",
		" 스킬이 종료된 이후 스킬이 지속된 시간에 비례해 그로기 상태 §8(§7공격 불능§8)§f가",
		" 됩니다. $[COOLDOWN_CONFIG]",
		"§7패시브 §8- §c전사의 피§f: 원거리 공격력이 절반으로 감소합니다. 5칸 밖의 적에게",
		" 받는 대미지를 최대 75%까지 거리에 따라 줄여받습니다. 잃은 체력에 비례하여",
		" 입히는 근접 대미지가 1.35배까지 증가합니다."
})
public class Berserker extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> MAX_DURATION_CONFIG = abilitySettings.new SettingObject<Integer>(Berserker.class, "max-duration", 8,
			"# 불굴의 의지 최대 지속 (초 단위)") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	public static final SettingObject<Integer> DAMAGE_FACTOR_CONFIG = abilitySettings.new SettingObject<Integer>(Berserker.class, "damage-factor", 100,
			"# 불굴의 의지 추가 대미지 계수", "# 125인 경우 125% 추가 대미지") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Berserker.class, "cooldown", 60,
			"# 불굴의 의지 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};

	public static final AttributeModifier SPEED_MODIFIER = new AttributeModifier(UUID.fromString("d91738be-1af2-4799-a813-23f8109b3670"), "berserker", .65, Operation.ADD_SCALAR);

	@Nullable
	private static Entity getDamager(final Entity damager) {
		if (damager instanceof Projectile) {
			final ProjectileSource shooter = ((Projectile) damager).getShooter();
			return shooter instanceof Entity ? (Entity) shooter : null;
		} else return damager;
	}

	private final Cooldown cooldown = new Cooldown(COOLDOWN_CONFIG.getValue(), CooldownDecrease._50);
	private WillOfIron willOfIron = null;
	private Groggy groggy = null;
	private final int maxDuration = MAX_DURATION_CONFIG.getValue();

	public Berserker(Participant participant) {
		super(participant);
	}

	@SubscribeEvent(ignoreCancelled = true)
	private void onPlayerAttack(final EntityDamageByEntityEvent e) {
		if (getPlayer().equals(e.getDamager())) {
			e.setDamage(e.getDamage() * (1 + ((1 - getPlayer().getHealth() / getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) * .35)));
		}
	}

	public class WillOfIron extends AbilityTimer implements Listener {

		private final BossBar bossBar;
		private int count = 0, attacks;
		private final IWorldBorder worldBorder;

		private WillOfIron(int duration, int attacks) {
			super(TaskType.NORMAL, duration * 20);
			setPeriod(TimeUnit.TICKS, 1);
			this.bossBar = Bukkit.createBossBar("불굴의 의지", BarColor.RED, BarStyle.SEGMENTED_6);
			Preconditions.checkArgument(attacks > 0, "'attacks' must be 1 or greater");
			this.attacks = attacks;
			this.worldBorder = NMS.createWorldBorder(getPlayer().getWorld().getWorldBorder());
			worldBorder.setWarningDistance(Integer.MAX_VALUE);
			NMS.setWorldBorder(getPlayer(), worldBorder);
			Berserker.this.willOfIron = this;
		}

		@Override
		protected void onStart() {
			bossBar.setProgress(0);
			bossBar.addPlayer(getPlayer());
			bossBar.setVisible(true);
			Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
			getParticipant().removeEffects(new Predicate<Effect>() {
				@Override
				public boolean test(Effect effect) {
					final ImmutableSet<EffectType> effectType = effect.getRegistration().getEffectType();
					return effectType.contains(EffectType.MOVEMENT_RESTRICTION) || effectType.contains(EffectType.MOVEMENT_INTERRUPT);
				}
			});
			try {
				getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).addModifier(SPEED_MODIFIER);
			} catch (IllegalArgumentException ignored) {
			}
		}

		@Override
		protected void run(int count) {
			this.count++;
			bossBar.setProgress(RangesKt.coerceIn(count / (double) getMaximumCount(), 0, 1));
		}

		@EventHandler(ignoreCancelled = true)
		private void onEntityDamageByEntity(final EntityDamageByEntityEvent e) {
			if (getPlayer().equals(e.getDamager())) {
				e.setDamage(e.getDamage() * (1 + (DAMAGE_FACTOR_CONFIG.getValue() / 100.0)));
				final Entity entity = e.getEntity();
				entity.getWorld().strikeLightningEffect(entity.getLocation());
				if (--attacks <= 0) {
					new Groggy(60 + willOfIron.count / 3).start();
					stop(false);
				}
			}
			onEntityDamage(e);
		}

		@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
		private void onEntityDamageByBlock(EntityDamageByBlockEvent e) {
			onEntityDamage(e);
		}

		@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
		private void onEntityDamage(EntityDamageEvent e) {
			if (getPlayer().equals(e.getEntity()) && getPlayer().getHealth() - e.getFinalDamage() <= 0) {
				e.setCancelled(true);
				getPlayer().setHealth(1);
				NMS.broadcastEntityEffect(getPlayer(), (byte) 2);
			}
		}

		@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
		private void onPlayerSetHealth(PlayerSetHealthEvent e) {
			if (e.getHealth() <= 0.0) {
				e.setHealth(1);
				e.setCancelled(true);
			}
		}

		@EventHandler
		private void onPlayerJoin(final PlayerJoinEvent e) {
			if (getPlayer().getUniqueId().equals(e.getPlayer().getUniqueId()) && isRunning()) {
				bossBar.addPlayer(e.getPlayer());
			}
		}

		@EventHandler
		private void onPlayerQuit(final PlayerQuitEvent e) {
			if (getPlayer().getUniqueId().equals(e.getPlayer().getUniqueId())) {
				bossBar.removePlayer(e.getPlayer());
			}
		}

		@EventHandler
		private void onPlayerVelocity(final PlayerVelocityEvent e) {
			if (getPlayer().getUniqueId().equals(e.getPlayer().getUniqueId())) {
				e.setCancelled(true);
			}
		}

		@EventHandler
		private void onEffectApply(final ParticipantEffectApplyEvent e) {
			if (getParticipant().equals(e.getParticipant())) {
				final ImmutableSet<EffectType> effectType = e.getEffectType().getEffectType();
				if (effectType.contains(EffectType.MOVEMENT_RESTRICTION) || effectType.contains(EffectType.MOVEMENT_INTERRUPT)) {
					e.setCancelled(true);
				}
			}
		}

		public void cancel() {
			if (getCount() >= 40) {
				stop(false);
			}
		}

		@Override
		protected void onEnd() {
			bossBar.removeAll();
			cooldown.start();
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			NMS.resetWorldBorder(getPlayer());
			HandlerList.unregisterAll(this);
			Berserker.this.willOfIron = null;
			bossBar.removeAll();
			getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).removeModifier(SPEED_MODIFIER);
		}

		@Override
		protected void onPause() {
			bossBar.removeAll();
		}

		@Override
		protected void onResume() {
			bossBar.addPlayer(getPlayer());
		}

	}

	private class Groggy extends AbilityTimer implements Listener {

		private final BossBar bossBar;

		private Groggy(final int ticks) {
			super(TaskType.REVERSE, ticks);
			setPeriod(TimeUnit.TICKS, 1);
			this.bossBar = Bukkit.createBossBar("그로기§8(§7공격 불능§8)", BarColor.WHITE, BarStyle.SEGMENTED_6);
			Berserker.this.groggy = this;
		}

		@Override
		protected void onStart() {
			bossBar.setProgress(0);
			bossBar.addPlayer(getPlayer());
			bossBar.setVisible(true);
			Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		}

		@EventHandler
		private void onPlayerJoin(final PlayerJoinEvent e) {
			if (getPlayer().getUniqueId().equals(e.getPlayer().getUniqueId()) && isRunning()) {
				bossBar.addPlayer(e.getPlayer());
			}
		}

		@EventHandler
		private void onPlayerQuit(final PlayerQuitEvent e) {
			if (getPlayer().getUniqueId().equals(e.getPlayer().getUniqueId())) {
				bossBar.removePlayer(e.getPlayer());
			}
		}

		@EventHandler
		private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
			if (getPlayer().equals(getDamager(e.getDamager()))) {
				e.setCancelled(true);
			}
		}

		@Override
		protected void run(int count) {
			bossBar.setProgress(RangesKt.coerceIn(count / (double) getMaximumCount(), 0, 1));
		}

		@Override
		protected void onEnd() {
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			bossBar.removeAll();
			HandlerList.unregisterAll(this);
			Berserker.this.groggy = null;
		}

		@Override
		protected void onPause() {
			bossBar.removeAll();
		}

		@Override
		protected void onResume() {
			bossBar.addPlayer(getPlayer());
		}

	}

	@Override
	public boolean ActiveSkill(Material material, ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK) {
			if (willOfIron != null) {
				willOfIron.cancel();
			} else if (groggy == null) {
				if (!cooldown.isCooldown()) {
					new WillOfIron(maxDuration, 1).start();
				}
			}
		}
		return false;
	}

	private final Predicate<Entity> predicate = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			if (entity.equals(getPlayer())) return false;
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
			return true;
		}
	};

	private long lastParticle = System.currentTimeMillis();

	@SubscribeEvent(onlyRelevant = true)
	private void onEntityDamageByEntity(final EntityDamageByEntityEvent e) {
		final Entity damager = getDamager(e.getDamager());
		if (damager != null && !getPlayer().equals(damager)) {
			final double distance = Math.min(10, damager.getLocation().distance(getPlayer().getLocation()));
			if (distance >= 5) {
				e.setDamage(e.getDamage() * Math.max((14 - distance) / 10, .75));
				final long current = System.currentTimeMillis();
				if (current - lastParticle >= 250) {
					this.lastParticle = current;
					final Location playerLocation = getPlayer().getLocation();
					final double divided = Math.PI / 13, minusYaw = -LocationUtil.getYaw(damager.getLocation().toVector().subtract(playerLocation.toVector()));
					for (double radians = 0; radians <= 1.5707963267948966192313216916398; radians += divided) {
						double sin = FastMath.sin(radians), z = FastMath.cos(radians);
						for (double phi = divided; phi < 6.283185307179586476925286766559; phi += divided) {
							ParticleLib.REDSTONE.spawnParticle(playerLocation.clone().add(VectorUtil.rotateAroundAxisY(new Vector(sin * FastMath.cos(phi), sin * FastMath.sin(phi), z), minusYaw)).add(0, 1, 0), RGB.RED);
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	private void onProjectileAttack(final EntityDamageByEntityEvent e) {
		if (getPlayer().equals(getDamager(e.getDamager()))) {
			if (e.getCause() == DamageCause.PROJECTILE) {
				e.setDamage(e.getDamage() / 2);
			}
		}
	}

}
