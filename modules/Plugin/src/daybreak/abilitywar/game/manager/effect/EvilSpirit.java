package daybreak.abilitywar.game.manager.effect;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.library.SoundLib.SimpleSound;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;
import java.util.function.Predicate;

public class EvilSpirit extends AbstractGame.Effect implements Listener {

	private static final Map<Participant, EvilSpirit> evilSpirits = new WeakHashMap<>();

	public static void apply(final @NotNull Participant participant, final @NotNull TimeUnit timeUnit, final int duration) {
		if (evilSpirits.containsKey(participant)) {
			final EvilSpirit applied = evilSpirits.get(participant);
			final int toTicks = timeUnit.toTicks(duration) / 4;
			if (toTicks > applied.getCount()) {
				applied.setCount(toTicks);
			}
		} else {
			new EvilSpirit(participant, timeUnit, duration).start();
		}
	}

	private static final Random random = new Random();
	private static final SimpleSound[] sounds = {
			SoundLib.BLOCK_STONE_BREAK, SoundLib.BLOCK_GRASS_BREAK, SoundLib.BLOCK_GRAVEL_BREAK, SoundLib.BLOCK_STONE_PLACE, SoundLib.BLOCK_GRASS_PLACE, SoundLib.BLOCK_GRAVEL_PLACE
	};

	private final Participant participant;

	private final Predicate<Player> predicate = new Predicate<Player>() {
		@Override
		public boolean test(Player entity) {
			return getGame().isParticipating(entity.getUniqueId())
					&& (!(getGame() instanceof DeathManager.Handler) || !((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()));
		}
	};

	private EvilSpirit(final Participant participant, final TimeUnit timeUnit, final int duration) {
		participant.getGame().super(participant, "§c악령", TaskType.REVERSE, timeUnit.toTicks(duration) / 4);
		this.participant = participant;
		setPeriod(TimeUnit.TICKS, 4);
	}

	@Override
	protected void onStart() {
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		evilSpirits.put(participant, this);
	}

	@EventHandler
	private void onPlayerDeath(PlayerDeathEvent e) {
		if (participant.getPlayer().getUniqueId().equals(e.getEntity().getUniqueId())) {
			stop(false);
		}
	}

	@EventHandler
	private void onPlayerDeath(final EntityDamageByEntityEvent e) {
		if (participant.getPlayer().equals(e.getEntity())) {
			final Entity damager = getDamager(e.getDamager());
			if (damager instanceof Player && !damager.equals(e.getEntity()) && predicate.test((Player) damager)) {
				apply(getGame().getParticipant(damager.getUniqueId()), TimeUnit.TICKS, getCount() * 4);
			}
		}
	}

	@Nullable
	private Entity getDamager(final Entity damager) {
		if (damager instanceof Projectile) {
			final ProjectileSource shooter = ((Projectile) damager).getShooter();
			return shooter instanceof Entity ? (Entity) shooter : null;
		} else return damager;
	}

	@Override
	protected void run(int count) {
		super.run(count);
		final double randNum = random.nextDouble();
		if (count % 15 == 0 && randNum <= 0.9) {
			PotionEffects.BLINDNESS.addPotionEffect(participant.getPlayer(), 60, 0, true);
			SoundLib.AMBIENT_CAVE.playSound(participant.getPlayer());
			if (randNum <= 0.3) {
				final SimpleSound sound = sounds[random.nextInt(sounds.length)];
				participant.getGame().new GameTimer(TaskType.NORMAL, random.nextInt(4) + 4) {
					@Override
					protected void run(int count) {
						sound.playSound(participant.getPlayer());
					}
				}.setPeriod(TimeUnit.TICKS, random.nextInt(2) + 3).start();
			}
		}
	}

	@Override
	protected void onEnd() {
		evilSpirits.remove(participant);
		HandlerList.unregisterAll(this);
		super.onEnd();
	}

	@Override
	protected void onSilentEnd() {
		evilSpirits.remove(participant);
		HandlerList.unregisterAll(this);
		super.onSilentEnd();
	}

}