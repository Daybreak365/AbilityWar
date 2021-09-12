package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.event.participant.ParticipantDeathEvent;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.collect.LimitedPushingList;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;

@AbilityManifest(name = "시간 역행", rank = Rank.S, species = Species.HUMAN, explain = {
		"§7철괴 우클릭 §8- §b시간 역행§f: 시간을 역행해 $[TIME_CONFIG]초 전으로 돌아갑니다. 역행 중에는",
		" 어떠한 피해도 입지 않으며, 타게팅의 대상이 되지 않습니다. $[COOLDOWN_CONFIG]",
		"§7패시브 §8- §b완벽한 타이밍§f: 시간 역행이 사용 가능한 상태에서 §c죽을 위기§f에 처하면",
		" 자동으로 시간을 역행합니다."
})
public class TimeRewind extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(TimeRewind.class, "cooldown", 100,
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

	public static final SettingObject<Integer> TIME_CONFIG = abilitySettings.new SettingObject<Integer>(TimeRewind.class, "rewinding-time", 5,
			"# 능력을 사용했을 때 몇초 전으로 돌아갈지 설정합니다.") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	private static final RGB COLOUR = RGB.of(61, 132, 254);
	private static final FixedMetadataValue NULL_VALUE = new FixedMetadataValue(AbilityWar.getPlugin(), null);

	public TimeRewind(Participant participant) {
		super(participant);
	}

	private final Cooldown cooldown = new Cooldown(COOLDOWN_CONFIG.getValue());
	private final int time = TIME_CONFIG.getValue();

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && rewind == null) {
			rewind();
			return true;
		}
		return false;
	}

	@SubscribeEvent(onlyRelevant = true, priority = 6, ignoreCancelled = true, childs = {EntityDamageByBlockEvent.class, EntityDamageByEntityEvent.class})
	private void onEntityDamage(EntityDamageEvent e) {
		if (getPlayer().getHealth() - e.getFinalDamage() <= 0) {
			if (rewind()) {
				e.setCancelled(true);
			}
		}
	}

	@SubscribeEvent
	private void onPlayerDeath(ParticipantDeathEvent e) {
		if (e.getParticipant().equals(getParticipant())) {
			moments.clear();
		}
	}

	@SubscribeEvent
	private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.getDamager().hasMetadata("time-rewind")) {
			e.setCancelled(true);
		}
	}

	private final LimitedPushingList<Moment> moments = new LimitedPushingList<>(time * 10);
	private Rewind rewind = null;

	private boolean rewind() {
		if (rewind != null || cooldown.isCooldown()) return false;
		new Rewind().start();
		return true;
	}

	private class Rewind extends AbilityTimer implements Listener {

		private final ActionbarChannel channel;
		private final LinkedList<Moment> datas;
		private final GameMode originalMode;
		private final float originalFlySpeed;

		private Rewind() {
			super(TaskType.REVERSE, TimeRewind.this.moments.size());
			if (rewind != null) {
				throw new IllegalStateException();
			}
			setPeriod(TimeUnit.TICKS, 1);
			TimeRewind.this.rewind = this;
			this.channel = getParticipant().actionbar().newChannel();
			this.datas = new LinkedList<>(TimeRewind.this.moments);
			this.originalMode = getPlayer().getGameMode();
			this.originalFlySpeed = getPlayer().getFlySpeed();
			TimeRewind.this.moments.clear();
			setPeriod(TimeUnit.TICKS, 1);
			start();
		}

		@Override
		protected void onStart() {
			Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
			getParticipant().attributes().TARGETABLE.setValue(false);
			saveData.stop(false);
		}

		@Override
		protected void run(int count) {
			getPlayer().setGameMode(GameMode.SPECTATOR);
			getPlayer().setFlySpeed(0);
			final Moment data = datas.pollLast();
			if (data != null && !getPlayer().isDead()) {
				data.apply();
			} else {
				stop(false);
			}
		}

		@Override
		protected void onEnd() {
			cooldown.start();
			onSilentEnd();
			saveData.start();
			SoundLib.BELL.playInstrument(getPlayer(), Note.natural(0, Tone.D));
			SoundLib.BELL.playInstrument(getPlayer(), Note.sharp(0, Tone.F));
			SoundLib.BELL.playInstrument(getPlayer(), Note.natural(1, Tone.A));
			final Firework firework = getPlayer().getWorld().spawn(getPlayer().getEyeLocation(), Firework.class);
			final FireworkMeta meta = firework.getFireworkMeta();
			meta.addEffect(
					FireworkEffect.builder()
							.withColor(Color.fromRGB(32, 60, 255), Color.WHITE, Color.fromRGB(250, 213, 0))
							.with(Type.BALL)
							.build()
			);
			meta.setPower(0);
			firework.setFireworkMeta(meta);
			firework.setMetadata("time-rewind", NULL_VALUE);
			new BukkitRunnable() {
				@Override
				public void run() {
					firework.detonate();
				}
			}.runTaskLater(AbilityWar.getPlugin(), 1L);
		}

		@Override
		protected void onSilentEnd() {
			HandlerList.unregisterAll(this);
			getParticipant().attributes().TARGETABLE.setValue(true);
			channel.unregister();
			TimeRewind.this.rewind = null;
			getPlayer().setGameMode(originalMode == GameMode.SPECTATOR ? GameMode.SURVIVAL : originalMode);
			getPlayer().setFlySpeed(originalFlySpeed);
		}

		@EventHandler
		private void onEntityDamage(EntityDamageEvent e) {
			if (e.getEntity().equals(getPlayer())) {
				e.setCancelled(true);
			}
		}

		@EventHandler
		private void onEntityDamage(EntityDamageByBlockEvent e) {
			if (e.getEntity().equals(getPlayer())) {
				e.setCancelled(true);
			}
		}

		@EventHandler
		private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
			if (((getPlayer().equals(e.getDamager()) || (e.getDamager() instanceof Projectile && getPlayer().equals(((Projectile) e.getDamager()).getShooter()))) || getPlayer().equals(e.getEntity()))) {
				e.setCancelled(true);
			}
		}

	}

	private final AbilityTimer saveData = new AbilityTimer() {
		@Override
		public void run(int count) {
			moments.add(new Moment());
			if (!cooldown.isRunning()) {
				int momentCount = 0;
				final ListIterator<Moment> listIterator = new LinkedList<>(moments).listIterator(moments.size() - 1);
				if (!listIterator.hasPrevious()) return;
				listIterator.previous();
				while (listIterator.hasPrevious()) {
					if (momentCount++ > 5) return;
					final Location base;
					if (momentCount == 1) {
						base = getPlayer().getLocation();
						listIterator.previous();
					} else {
						base = listIterator.previous().location;
					}
					listIterator.next();
					final Location previous = listIterator.next().location;
					if (base.getWorld() != previous.getWorld() || base.distanceSquared(previous) > 36) return;
					for (Iterator<Location> iterator = new Iterator<Location>() {
						private final Vector vectorBetween = previous.toVector().subtract(base.toVector()), unit = vectorBetween.clone().normalize().multiply(.1);
						private final int amount = (int) (vectorBetween.length() / 0.1);
						private int cursor = 0;

						@Override
						public boolean hasNext() {
							return cursor < amount;
						}

						@Override
						public Location next() {
							if (cursor >= amount) throw new NoSuchElementException();
							cursor++;
							return base.clone().add(unit.clone().multiply(cursor));
						}
					}; iterator.hasNext(); ) {
						ParticleLib.REDSTONE.spawnParticle(iterator.next().add(0, 1, 0), COLOUR);
					}
					listIterator.previous();
					listIterator.previous();
				}
			}
		}
	}.setPeriod(TimeUnit.TICKS, 2).register();

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			saveData.start();
		} else if (update == Update.RESTRICTION_SET) {
			moments.clear();
		}
	}

	private class Moment {

		private final Player player;
		private final Location location;
		private final double health;
		private final int fireTicks;
		private final float fallDistance;
		private final Collection<PotionEffect> potionEffects;

		private Moment() {
			this.player = getPlayer();
			this.location = player.getLocation();
			this.health = player.getHealth();
			this.fireTicks = player.getFireTicks();
			this.fallDistance = player.getFallDistance();
			this.potionEffects = player.getActivePotionEffects();
		}

		private void apply() {
			getPlayer().teleport(location);
			if (health > 0.0) {
				player.setHealth(Math.min(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), health));
			}
			player.setFireTicks(fireTicks);
			player.setFallDistance(fallDistance);
			for (PotionEffect effect : player.getActivePotionEffects()) {
				player.removePotionEffect(effect.getType());
			}
			player.addPotionEffects(potionEffects);
		}

	}

}
