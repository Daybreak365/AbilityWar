package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.VectorUtil;
import daybreak.abilitywar.utils.base.minecraft.damage.Damages;
import daybreak.abilitywar.utils.base.minecraft.damage.Damages.INSTANCE.Flag;
import daybreak.abilitywar.utils.base.minecraft.item.Skulls;
import daybreak.abilitywar.utils.base.minecraft.nms.IHologram;
import daybreak.abilitywar.utils.base.minecraft.nms.IWorldBorder;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

@AbilityManifest(name = "리버레이터", rank = Rank.A, species = Species.HUMAN, explain = {
		"§7철괴 우클릭 §8- §c해방§f/§c회귀§f: 자신의 몸으로부터 영혼을 분리하여 타게팅할 수 없는",
		" 상태로 변한 후 앞으로 짧게 돌진합니다. 능력을 다시 사용하거나 10초가 지나면",
		" 영혼이 몸으로 돌아가며 해방 중 플레이어에게 입힌 모든 피해량의 25%를 다시",
		" 입힙니다. $[COOLDOWN_CONFIG]",
		"§7패시브 §8- §c분리§f: §c해방 §f중 모든 피해를 50%만 입고, 나머지 50%의 피해는 §c회귀 §f시에",
		" 받습니다. 단, §c회귀§f하며 입히는 추가 피해로 대상 플레이어가 사망한 경우 50%의",
		" 피해를 받지 않습니다."
})
public class Liberator extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Liberator.class, "cooldown", 50,
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

	public static final SettingObject<Integer> DURATION_CONFIG = abilitySettings.new SettingObject<Integer>(Liberator.class, "duration", 10,
			"# 지속 시간") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	private static final RGB DARK_RED = RGB.of(130, 1, 1);

	private final Cooldown cooldown = new Cooldown(COOLDOWN_CONFIG.getValue());
	private final int duration = DURATION_CONFIG.getValue();
	private Unbound unbound = null;

	public Liberator(Participant participant) {
		super(participant);
	}

	@Override
	public boolean ActiveSkill(Material material, ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK) {
			if (cooldown.isCooldown()) return false;
			if (unbound == null) {
				new Unbound(getPlayer().getLocation());
			} else {
				unbound.stop(false);
				return false;
			}
		}
		return false;
	}

	public class Unbound extends AbilityTimer implements Listener {

		private final ActionbarChannel channel;
		private final Location location;
		private final ArmorStand armorStand;
		private final IWorldBorder worldBorder;
		private final Map<Player, Damage> damages;
		private final List<Double> delayedDamages;
		private boolean killed = false;

		private Unbound(final Location location) {
			super(TaskType.REVERSE, duration * 10);
			if (unbound != null) {
				throw new IllegalStateException();
			}
			this.location = location;
			this.channel = getParticipant().actionbar().newChannel();
			setPeriod(TimeUnit.TICKS, 2);
			Liberator.this.unbound = this;
			this.armorStand = location.getWorld().spawn(location, ArmorStand.class);
			armorStand.setBasePlate(false);
			armorStand.setArms(true);
			armorStand.setGravity(false);
			final EntityEquipment equipment = armorStand.getEquipment();
			equipment.setArmorContents(getPlayer().getInventory().getArmorContents());
			equipment.setHelmet(Skulls.createSkull(getPlayer()));
			this.worldBorder = NMS.createWorldBorder(getPlayer().getWorld().getWorldBorder());
			worldBorder.setWarningDistance(Integer.MAX_VALUE);
			this.damages = new HashMap<>();
			this.delayedDamages = new LinkedList<>();
			start();
		}

		@Override
		protected void onStart() {
			Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
			getParticipant().attributes().TARGETABLE.setValue(false);
			getPlayer().setVelocity(getPlayer().getLocation().getDirection().multiply(1.5).setY(0));
			SoundLib.ENTITY_ZOMBIE_VILLAGER_CONVERTED.playSound(getPlayer(), Float.MAX_VALUE, 2f);
		}

		@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
		private void onEntityDamage(final EntityDamageEvent e) {
			if (armorStand.equals(e.getEntity())) {
				e.setCancelled(true);
			} else if (getPlayer().equals(e.getEntity())) {
				final double damage = e.getDamage(), now = damage / 2;
				e.setDamage(now);
				this.delayedDamages.add(damage - now);
			}
		}

		@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
		private void onEntityDamageByEntity(final EntityDamageByEntityEvent e) {
			if (!getPlayer().equals(getDamager(e.getDamager())) || getPlayer().equals(e.getEntity())) return;
			final Entity entity = e.getEntity();
			if (entity instanceof Player) {
				getDamage((Player) entity).addDamage(e.getDamage() / 4);
			}
		}

		private Damage getDamage(final Player player) {
			Damage damage = damages.get(player);
			if (damage == null) {
				damage = new Damage(player);
				damages.put(player, damage);
			}
			return damage;
		}

		@Nullable
		private Entity getDamager(final Entity damager) {
			if (damager instanceof Projectile) {
				final ProjectileSource shooter = ((Projectile) damager).getShooter();
				return shooter instanceof Entity ? (Entity) shooter : null;
			} else return damager;
		}

		@EventHandler
		private void onPlayerArmorStandManipulate(final PlayerArmorStandManipulateEvent e) {
			if (armorStand.equals(e.getRightClicked())) {
				e.setCancelled(true);
			}
		}

		@Override
		protected void run(int count) {
			channel.update("§c해방§7: §f" + (count / 10.0) + "초");
			final Vector direction = location.toVector().subtract(getPlayer().getLocation().toVector()).normalize().multiply(.75);
			if (!isValid(direction)) return;
			final Location playerLocation = getPlayer().getLocation().clone().add(0, 1, 0);
			ParticleLib.SMOKE_NORMAL.spawnParticle(playerLocation, .25, .25, .25, 10, .001);
			final Location applied = playerLocation.clone().add(direction);
			for (Iterator<Location> iterator = new Iterator<Location>() {
				private final Vector vectorBetween = applied.toVector().subtract(playerLocation.toVector()), unit = vectorBetween.clone().normalize().multiply(.1);
				private final int amount = (int) (vectorBetween.length() / .1);
				private int cursor = 0;

				@Override
				public boolean hasNext() {
					return cursor < amount;
				}

				@Override
				public Location next() {
					if (cursor >= amount) throw new NoSuchElementException();
					cursor++;
					return playerLocation.clone().add(unit.clone().multiply(cursor));
				}
			}; iterator.hasNext(); ) {
				ParticleLib.REDSTONE.spawnParticle(iterator.next(), DARK_RED);
			}
			if (count == 30) {
				NMS.setWorldBorder(getPlayer(), worldBorder);
			}
			for (Damage damage : damages.values()) {
				damage.update();
			}
		}

		private boolean isValid(final Vector vector) {
			final double x = vector.getX(), y = vector.getY(), z = vector.getZ();
			return !Double.isNaN(x) && !Double.isInfinite(x) && !Double.isNaN(y) && !Double.isInfinite(y) && !Double.isNaN(z) && !Double.isInfinite(z) && !(x == 0 && y == 0 && z == 0);
		}

		@Override
		protected void onEnd() {
			cooldown.start();
			onSilentEnd();
			ParticleLib.SMOKE_NORMAL.spawnParticle(getPlayer().getLocation().clone().add(0, 1, 0), .25, .25, .25, 200, .001);
			SoundLib.ENTITY_ZOMBIE_VILLAGER_CONVERTED.playSound(getPlayer(), Float.MAX_VALUE, 1f);
			final GameMode originalMode = getPlayer().getGameMode();
			final float originalFlySpeed = getPlayer().getFlySpeed();
			for (Entry<Player, Damage> entry : damages.entrySet()) {
				final Player player = entry.getKey();
				player.setNoDamageTicks(0);
				final Damage damage = entry.getValue();
				player.damage(damage.damage, getPlayer());
				if (player.isDead()) {
					killed = true;
				}
				damage.remove();
			}
			if (!killed) {
				for (double damage : delayedDamages) {
					getPlayer().setNoDamageTicks(0);
					getPlayer().damage(damage, getPlayer());
				}
			}
			getPlayer().setGameMode(GameMode.SPECTATOR);
			getPlayer().setFlySpeed(0);
			final Vector vector = location.toVector().subtract(getPlayer().getLocation().toVector()).multiply(.25);
			if (vector.length() > 2.5) {
				VectorUtil.validateVector(vector.normalize().multiply(2.5));
			}
			getPlayer().setVelocity(vector);
			new AbilityTimer(0) {
				@Override
				protected void onEnd() {
					getPlayer().setGameMode(originalMode == GameMode.SPECTATOR ? GameMode.SURVIVAL : originalMode);
					getPlayer().setFlySpeed(originalFlySpeed);
					getPlayer().teleport(location);
				}
			}.setInitialDelay(TimeUnit.TICKS, 7).start();
		}

		@Override
		protected void onSilentEnd() {
			getParticipant().attributes().TARGETABLE.setValue(true);
			channel.unregister();
			Liberator.this.unbound = null;
			HandlerList.unregisterAll(this);
			armorStand.remove();
			NMS.resetWorldBorder(getPlayer());
		}

		public class Damage {

			private final Player player;
			private final IHologram hologram;
			private double damage = 0;

			private Damage(final Player player) {
				this.player = player;
				this.hologram = NMS.newHologram(player.getWorld(), player.getLocation(), "§c死");
				NMS.removeBoundingBox(armorStand);
			}

			private void addDamage(final double damage) {
				this.damage += damage;
			}

			private void update() {
				hologram.teleport(player.getLocation().clone().add(0, 2, 0));
				if (player.getHealth() - Damages.getFinalDamage(player, damage, Flag.ALL) < 0) {
					hologram.display(getPlayer());
				} else {
					hologram.hide(getPlayer());
				}
			}

			private void remove() {
				hologram.unregister();
			}

		}
	}

}
