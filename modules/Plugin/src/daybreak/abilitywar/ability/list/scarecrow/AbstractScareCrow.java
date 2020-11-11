package daybreak.abilitywar.ability.list.scarecrow;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer.TaskType;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.UUID;

@AbilityManifest(name = "허수아비", rank = Rank.A, species = Species.OTHERS)
@Beta
public abstract class AbstractScareCrow extends AbilityBase implements ActiveHandler {

	@Nullable
	private static Entity getDamager(final Entity damager) {
		if (damager instanceof Projectile) {
			final ProjectileSource shooter = ((Projectile) damager).getShooter();
			return shooter instanceof Entity ? (Entity) shooter : null;
		} else return damager;
	}

	private final BiMap<UUID, ScareCrowEntity> scareCrows = HashBiMap.create();
	private final BiMap<Integer, ScareCrowEntity> entitiesById = HashBiMap.create();
	private final Disguise disguise = new Disguise();

	private class Disguise extends AbilityTimer {

		private final ActionbarChannel actionbarChannel = newActionbarChannel();

		private Disguise() {
			super(TaskType.REVERSE, 5);
			setBehavior(RestrictionBehavior.PAUSE_RESUME);
			start();
		}

		protected void reset() {
			start();
			setCount(getMaximumCount());
		}

		@Override
		protected void onStart() {
			actionbarChannel.update("§c허수아비§7: §f" + getCount() + "초");
			getParticipant().attributes().TARGETABLE.setValue(true);
		}

		@Override
		protected void run(int count) {
			actionbarChannel.update("§c허수아비§7: §f" + count + "초");
		}

		@Override
		protected void onCountSet() {
			actionbarChannel.update("§c허수아비§7: §f" + getCount() + "초");
		}

		@Override
		protected void onEnd() {
			getParticipant().attributes().TARGETABLE.setValue(false);
			actionbarChannel.update("§c허수아비");
		}

		@Override
		protected void onSilentEnd() {
			getParticipant().attributes().TARGETABLE.setValue(true);
			actionbarChannel.update(null);
		}
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onPlayerMove(final PlayerMoveEvent e) {
		disguise.reset();
	}

	protected @Nullable ScareCrowEntity getEntityById(int id) {
		return entitiesById.get(id);
	}

	public AbstractScareCrow(Participant participant) {
		super(participant);
	}

	protected abstract ScareCrowEntity createScareCrow(final Location location);
	protected abstract FakeScareCrowEntity createFakeScareCrow(final Location location);

	@Override
	public boolean ActiveSkill(Material material, ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK) {
			final ScareCrowEntity entity = createScareCrow(getPlayer().getLocation());
			scareCrows.put(entity.getUniqueID(), entity);
			entitiesById.put(entity.getId(), entity);
			return true;
		}
		return false;
	}

	@SubscribeEvent
	private void onPlayerJoin(final PlayerJoinEvent e) {
		final Player player = e.getPlayer();
		new BukkitRunnable() {
			@Override
			public void run() {
				for (ScareCrowEntity entity : scareCrows.values()) {
					entity.display(player);
				}
			}
		}.runTaskLater(AbilityWar.getPlugin(), 10L);
	}

	@SubscribeEvent
	private void onEntityDamage(final EntityDamageEvent e) {
		if (getPlayer().equals(e.getEntity())) {
			disguise.reset();
			return;
		}
		final UUID entity = e.getEntity().getUniqueId();
		if (scareCrows.containsKey(entity)) {
			e.setCancelled(true);
		}
	}

	@SubscribeEvent
	private void onEntityDamageByBlock(final EntityDamageByBlockEvent e) {
		this.onEntityDamage(e);
	}

	@SubscribeEvent
	private void onEntityDamageByEntity(final EntityDamageByEntityEvent e) {
		if (getPlayer().equals(e.getEntity())) {
			if (!disguise.isRunning()) {
				e.setCancelled(true);
				final Entity damagerEntity = getDamager(e.getDamager());
				if (damagerEntity instanceof Player && !getPlayer().equals(damagerEntity)) {
					final Player damager = (Player) damagerEntity;
					SoundLib.ENTITY_WITHER_SPAWN.playSound(damager, 2f, 0);
					PotionEffects.BLINDNESS.addPotionEffect(damager, 80, 0, true);
					final Location damagerLoc = damager.getLocation();
					final Location loc = damagerLoc.clone().add(damagerLoc.getDirection().setY(0).multiply(-1));
					loc.setDirection(damagerLoc.toVector().subtract(loc.toVector()));
					getPlayer().teleport(loc);
				}
			}
			disguise.reset();
			return;
		}
		final UUID entity = e.getEntity().getUniqueId();
		if (scareCrows.containsKey(entity)) {
			e.setCancelled(true);
			final Entity damagerEntity = getDamager(e.getDamager());
			if (damagerEntity instanceof Player && !getPlayer().equals(damagerEntity)) {
				final Player damager = (Player) damagerEntity;
				final ScareCrowEntity scareCrow = scareCrows.remove(entity);
				entitiesById.inverse().remove(scareCrow);
				scareCrow.remove();
				SoundLib.ENTITY_WITHER_SPAWN.playSound(damager, 2f, 0);
				PotionEffects.BLINDNESS.addPotionEffect(damager, 80, 0, true);
				final Location damagerLoc = damager.getLocation();
				final Location loc = damagerLoc.clone().add(damagerLoc.getDirection().setY(0));
				final Vector direction = damagerLoc.toVector().subtract(loc.toVector());
				loc.setDirection(direction);
				final FakeScareCrowEntity fakeEntity = createFakeScareCrow(loc);
				fakeEntity.display(damager);
				new AbilityTimer(TaskType.NORMAL, 20) {
					@Override
					protected void run(int count) {
						final Vector look = fakeEntity.getBukkitEntity().getLocation().toVector().subtract(damager.getLocation().toVector());
						NMS.rotateHead(damager, damager, LocationUtil.getYaw(look), LocationUtil.getPitch(look));
						fakeEntity.lookAt(damager, damager.getLocation());
					}
					@Override
					protected void onEnd() {
						new AbilityTimer(TaskType.NORMAL, 0) {
							@Override
							protected void onEnd() {
								onSilentEnd();
							}
							@Override
							protected void onSilentEnd() {
								fakeEntity.hide(damager);
							}
						}.setInitialDelay(TimeUnit.TICKS, 40).start();
					}

					@Override
					protected void onSilentEnd() {
						fakeEntity.hide(damager);
					}
				}.setPeriod(TimeUnit.TICKS, 1).start();
			}
		}
	}

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.ABILITY_DESTROY) {
			for (final Iterator<ScareCrowEntity> iterator = scareCrows.values().iterator(); iterator.hasNext();) {
				iterator.next().remove();
				iterator.remove();
			}
			entitiesById.clear();
		}
	}
}
