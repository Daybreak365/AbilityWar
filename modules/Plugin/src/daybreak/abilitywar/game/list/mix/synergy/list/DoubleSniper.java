package daybreak.abilitywar.game.list.mix.synergy.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.Scheduled;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.AbstractGame.CustomEntity;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;
import daybreak.abilitywar.utils.base.ProgressBar;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.geometry.Line;
import daybreak.abilitywar.utils.base.minecraft.entity.decorator.Deflectable;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.library.item.EnchantLib;
import daybreak.abilitywar.utils.library.item.ItemLib;
import java.util.Iterator;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

@AbilityManifest(name = "더블 스나이퍼", rank = Rank.S, species = Species.HUMAN, explain = {
		"활을 쏘면 매우 빠른 속도로 나아가는 특수한 투사체를 두 번 연속으로 쏩니다.",
		"투사체는 하나의 대상만 공격할 수 있고, 블록에 닿으면 폭발 후 소멸합니다.",
		"단, 유리나 유리 판과 같은 블록은 뚫고 지나갑니다.",
		"투사체를 쏘고 난 후 일정 시간동안 재장전을 하며, 재장전 중에는",
		"활을 쏠 수 없습니다. 활을 들고 있을 경우 빠르게 이동할 수 없으며,",
		"이동이 제한됩니다."
})
public class DoubleSniper extends Synergy {

	private static final Material GLASS_PANE = ServerVersion.getVersionNumber() > 12 ? Material.valueOf("GLASS_PANE") : Material.valueOf("THIN_GLASS");
	private static final RGB BULLET_COLOR = new RGB(43, 209, 224);
	@Scheduled
	private final AbilityBase.Timer snipeMode = new Timer() {
		@Override
		protected void run(int count) {
			Material main = getPlayer().getInventory().getItemInMainHand().getType();
			Material off = getPlayer().getInventory().getItemInOffHand().getType();
			if (main.equals(Material.BOW) || off.equals(Material.BOW) || (ServerVersion.getVersionNumber() >= 14 && (main.equals(Material.CROSSBOW) || off.equals(Material.CROSSBOW)))) {
				PotionEffects.SLOW.addPotionEffect(getPlayer(), 2, 3, true);
				getPlayer().setVelocity(getPlayer().getVelocity().setX(0).setY(Math.min(0, getPlayer().getVelocity().getY())).setZ(0));
			}
		}
	}.setPeriod(TimeUnit.TICKS, 1);
	private final ActionbarChannel actionbarChannel = newActionbarChannel();
	private Timer reload = null;

	public DoubleSniper(Participant participant) {
		super(participant);
	}

	@SubscribeEvent
	public void onProjectileLaunch(EntityShootBowEvent e) {
		if (getPlayer().equals(e.getEntity()) && e.getProjectile() instanceof Arrow) {
			e.setCancelled(true);
			if (reload == null) {
				if (!getPlayer().getGameMode().equals(GameMode.CREATIVE) && (!e.getBow().hasItemMeta() || !e.getBow().getItemMeta().hasEnchant(Enchantment.ARROW_INFINITE))) {
					ItemLib.removeItem(getPlayer().getInventory(), Material.ARROW, 1);
				}
				Arrow arrow = (Arrow) e.getProjectile();
				new Timer(2) {
					@Override
					protected void run(int count) {
						new Bullet<>(getPlayer(), arrow.getLocation(), arrow.getVelocity(), e.getBow().getEnchantmentLevel(Enchantment.ARROW_DAMAGE), BULLET_COLOR).start();
						SoundLib.ENTITY_GENERIC_EXPLODE.playSound(getPlayer().getLocation(), 7, 1.75f);
					}
				}.setPeriod(TimeUnit.TICKS, 15).start();
				this.reload = new Timer(15) {
					private final ProgressBar progressBar = new ProgressBar(15, 15);

					@Override
					protected void run(int count) {
						progressBar.step();
						actionbarChannel.update("재장전: " + progressBar.toString());
					}

					@Override
					protected void onEnd() {
						DoubleSniper.this.reload = null;
						actionbarChannel.update(null);
						SoundLib.BLOCK_STONE_PRESSURE_PLATE_CLICK_ON.playSound(getPlayer());
					}
				}.setPeriod(TimeUnit.TICKS, 4);
				reload.start();
			} else {
				getPlayer().sendMessage("§b재장전 §f중입니다.");
			}
		}
	}

	public class Bullet<Shooter extends Entity & ProjectileSource> extends Timer {

		private final Shooter shooter;
		private final CustomEntity entity;
		private final Vector forward;
		private final int powerEnchant;

		private final RGB color;
		private Location lastLocation;

		private Bullet(Shooter shooter, Location startLocation, Vector arrowVelocity, int powerEnchant, RGB color) {
			super(160);
			setPeriod(TimeUnit.TICKS, 1);
			this.shooter = shooter;
			this.entity = new Bullet.ArrowEntity(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ()).setBoundingBox(-.75, -.75, -.75, .75, .75, .75);
			this.forward = arrowVelocity.multiply(10);
			this.powerEnchant = powerEnchant;
			this.color = color;
			this.lastLocation = startLocation;
		}

		@Override
		protected void run(int i) {
			Location newLocation = lastLocation.clone().add(forward);
			for (Iterator<Location> iterator = Line.iteratorBetween(lastLocation, newLocation, 40); iterator.hasNext(); ) {
				Location location = iterator.next();
				entity.setLocation(location);
				Block block = location.getBlock();
				Material type = block.getType();
				if (type.isSolid()) {
					if (ItemLib.STAINED_GLASS.compareType(type) || Material.GLASS == type || ItemLib.STAINED_GLASS_PANE.compareType(type) || type == GLASS_PANE) {
						block.breakNaturally();
						SoundLib.BLOCK_GLASS_BREAK.playSound(block.getLocation(), 3, 1);
					} else {
						location.getWorld().createExplosion(location, 2);
						stop(false);
						return;
					}
				}
				for (Damageable damageable : LocationUtil.getConflictingDamageables(entity.getBoundingBox())) {
					if (!shooter.equals(damageable)) {
						damageable.damage(EnchantLib.getDamageWithPowerEnchantment(Math.min((forward.getX() * forward.getX()) + (forward.getY() * forward.getY()) + (forward.getZ() * forward.getZ()) / 10.0, 10), powerEnchant), shooter);
						stop(false);
						return;
					}
				}
				ParticleLib.REDSTONE.spawnParticle(location, color);
			}
			lastLocation = newLocation;
		}

		@Override
		protected void onEnd() {
			entity.remove();
		}

		@Override
		protected void onSilentEnd() {
			entity.remove();
		}

		public class ArrowEntity extends CustomEntity implements Deflectable {

			public ArrowEntity(World world, double x, double y, double z) {
				getGame().super(world, x, y, z);
			}

			@Override
			public Vector getDirection() {
				return forward.clone();
			}

			@Override
			public void onDeflect(Participant deflector, Vector newDirection) {
				stop(false);
				Player deflectedPlayer = deflector.getPlayer();
				new Bullet<>(deflectedPlayer, lastLocation, newDirection, powerEnchant, color).start();
			}

			@Override
			public ProjectileSource getShooter() {
				return shooter;
			}

		}

	}

}
