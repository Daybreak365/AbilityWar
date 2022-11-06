package daybreak.abilitywar.game.list.murdermystery.ability.jobs.murderer;

import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.list.murdermystery.Items;
import daybreak.abilitywar.game.list.murdermystery.MurderMystery;
import daybreak.abilitywar.game.list.murdermystery.ability.AbstractMurderer;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.minecraft.boundary.CenteredBoundingBox;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.base.minecraft.raytrace.RayTrace;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.library.item.ItemLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

@AbilityManifest(name = "머더: 스나이퍼", rank = Rank.SPECIAL, species = Species.HUMAN, explain = {
		"모든 시민을 죽이세요!",
		"살인자의 검으로 상대를 죽일 경우 5초간 투명 효과를 받습니다.",
		"금 우클릭으로 금 20개를 소모해 활과 화살을 얻을 수 있습니다.",
		"활을 쏠 때 매우 빠른 속도로 나아가는 특수한 투사체를 쏩니다.",
		"투사체는 하나의 대상만 공격할 수 있고, 블록에 닿으면 소멸합니다.",
		"단, 유리나 유리 판과 같은 블록은 뚫고 지나갑니다."
})
public class SniperMurderer extends AbstractMurderer {

	private static final Material GLASS_PANE = ServerVersion.getVersion() > 12 ? Material.valueOf("GLASS_PANE") : Material.valueOf("THIN_GLASS");
	private static final RGB BULLET_COLOR = new RGB(43, 209, 224);

	public SniperMurderer(Participant participant) {
		super(participant);
	}

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			final PlayerInventory inventory = getPlayer().getInventory();
			final boolean hadSword = Items.isMurdererSword(inventory.getItem(1));
			final int arrowCount = getArrowCount();
			inventory.clear();
			final MurderMystery murderMystery = (MurderMystery) getGame();
			murderMystery.addGold(getParticipant(), arrowCount * 8);
			getPlayer().getInventory().setHeldItemSlot(0);
			murderMystery.updateGold(getParticipant());
			addArrow();
			if (!hasBow()) {
				getPlayer().getInventory().setItem(2, Items.NORMAL_BOW.getStack());
			}
			if (!hadSword) {
				getPlayer().sendMessage("§e15초 §f뒤에 §4살인자§c의 검§f을 얻습니다.");
				new AbilityTimer(1) {
					@Override
					protected void run(int count) {
						getPlayer().getInventory().setHeldItemSlot(0);
						inventory.setItem(1, Items.MURDERER_SWORD.getStack());
						getPlayer().sendMessage("§4살인자§c의 검§f을 들고 있을 때 더 빠르게 움직일 수 있습니다.");
						for (Player player : Bukkit.getOnlinePlayers()) {
							NMS.sendTitle(player, "§4머더§c가 검을 얻었습니다.", "", 10, 80, 10);
							new AbilityTimer(1) {
								@Override
								protected void onEnd() {
									NMS.clearTitle(player);
								}
							}.setInitialDelay(TimeUnit.SECONDS, 5).start();
						}
					}
				}.setInitialDelay(TimeUnit.SECONDS, 15).start();
			} else {
				getPlayer().getInventory().setHeldItemSlot(0);
				inventory.setItem(1, Items.MURDERER_SWORD.getStack());
			}
			PASSIVE.start();

			NMS.sendTitle(getPlayer(), "§e직업§f: §5스나이퍼", "§7철컥, 탕!", 10, 80, 10);
			new AbilityTimer(1) {
				@Override
				protected void onEnd() {
					NMS.clearTitle(getPlayer());
				}
			}.setInitialDelay(TimeUnit.SECONDS, 5).start();
		}
	}

	@SubscribeEvent(ignoreCancelled = true)
	public void onProjectileLaunch(EntityShootBowEvent e) {
		if (getPlayer().equals(e.getEntity()) && e.getProjectile() instanceof Arrow) {
			e.setCancelled(true);
			ItemLib.removeItem(getPlayer().getInventory(), Material.ARROW, 1);
			final Arrow arrow = (Arrow) e.getProjectile();
			new Bullet(getPlayer(), arrow.getLocation(), arrow.getVelocity(), BULLET_COLOR).start();
			SoundLib.ENTITY_GENERIC_EXPLODE.playSound(getPlayer().getLocation(), 7, 1.75f);
		}
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onInteract(PlayerInteractEvent e) {
		if (Items.isGold(e.getItem())) {
			if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				final MurderMystery murderMystery = (MurderMystery) getGame();
				if (murderMystery.consumeGold(getParticipant(), 20)) {
					if (!addArrow()) {
						murderMystery.addGold(getParticipant());
					} else {
						if (!hasBow()) {
							getPlayer().getInventory().setItem(2, Items.NORMAL_BOW.getStack());
						}
					}
				}
			}
		}
	}

	public class Bullet extends AbilityTimer {

		private final LivingEntity shooter;
		private final CenteredBoundingBox boundingBox;
		private final Vector forward;
		private final Predicate<Entity> predicate;

		private final RGB color;

		private Bullet(LivingEntity shooter, Location startLocation, Vector arrowVelocity, RGB color) {
			super(160);
			setPeriod(TimeUnit.TICKS, 1);
			this.shooter = shooter;
			this.boundingBox = CenteredBoundingBox.of(startLocation, -1, -1, -1, 1, 1, 1);
			this.forward = arrowVelocity.multiply(10);
			this.color = color;
			this.lastLocation = startLocation;
			this.predicate = new Predicate<Entity>() {
				@Override
				public boolean test(Entity entity) {
					if (entity.equals(getPlayer())) return false;
					return (entity instanceof Player) && getGame().isParticipating(entity.getUniqueId())
							&& !((MurderMystery) getGame()).isDead(entity.getUniqueId())
							&& getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue();
				}
			};
		}

		private Location lastLocation;

		@Override
		protected void run(int i) {
			final Location newLocation = lastLocation.clone().add(forward);
			for (Iterator<Location> iterator = new Iterator<Location>() {
				private final Vector vectorBetween = newLocation.toVector().subtract(lastLocation.toVector()), unit = vectorBetween.clone().normalize().multiply(.1);
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
					return lastLocation.clone().add(unit.clone().multiply(cursor));
				}
			}; iterator.hasNext(); ) {
				final Location location = iterator.next();
				boundingBox.setCenter(location);
				final Block block = location.getBlock();
				final Material type = block.getType();
				if (type.isSolid()) {
					if (ItemLib.STAINED_GLASS.compareType(type) || Material.GLASS == type || ItemLib.STAINED_GLASS_PANE.compareType(type) || type == GLASS_PANE) {
						block.breakNaturally();
						SoundLib.BLOCK_GLASS_BREAK.playSound(block.getLocation(), 3, 1);
					} else if (RayTrace.hitsBlock(location.getWorld(), lastLocation.getX(), lastLocation.getY(), lastLocation.getZ(), location.getX(), location.getY(), location.getZ())) {
						stop(false);
						return;
					}
				}
				for (Player player : LocationUtil.getConflictingEntities(Player.class, shooter.getWorld(), boundingBox, predicate)) {
					if (!shooter.equals(player)) {
						if (((MurderMystery) getGame()).fireArrowHitEvent(getPlayer(), player)) {
							PotionEffects.INVISIBILITY.addPotionEffect(getPlayer(), 100, 1, true);
						}
						stop(false);
						return;
					}
				}
				ParticleLib.REDSTONE.spawnParticle(location, color);
			}
			lastLocation = newLocation;
		}

	}

}
