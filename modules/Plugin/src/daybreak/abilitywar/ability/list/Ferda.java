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
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.minecraft.block.Blocks;
import daybreak.abilitywar.utils.base.minecraft.block.IBlockSnapshot;
import daybreak.abilitywar.utils.library.BlockX;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import kotlin.ranges.RangesKt;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

@AbilityManifest(name = "페르다", rank = Rank.S, species = Species.GOD, explain = {
		"§7철괴 우클릭 §8- §2생명의 요람§f: 주위에 생명으로 가득 찬 지역을 만들어냅니다. 뿌리가",
		"자라나 주변에 있던 모든 적 플레이어를 옭아매, 뿌리 안에 가두고 무적 상태로",
		"만듭니다. 구속된 플레이어들의 체력 회복량은 모두 흡수되어 지역 내의 구속되지",
		"않은 모든 생명체에 나눠집니다. 지역이 생성된 이후에도 지역 내에서 나를 공격하는",
		"모든 플레이어는 뿌리에 의해 구속됩니다. 지역은 $[CRADLE_OF_LIFE_DURATION]초간 지속됩니다. $[COOLDOWN_CONFIG]",
		"§7철괴 좌클릭 §8- §2자정작용§f: $[SELF_PURIFICATION_DURATION]초간 지속되는 생명의 나무가 내 위치에 자라나 나를",
		"보호합니다. 지속 중에는 어떠한 대미지도 받지 않지만, 이동할 수 없습니다.",
		"나무가 모두 자라나면 나를 포함한 주위 $[RANGE_CONFIG]칸 이내의 모든 생명체는 체력을 3 만큼",
		"회복합니다. $[COOLDOWN_CONFIG]"
})
public class Ferda extends AbilityBase implements ActiveHandler {

	private static final Object CRADLE_OF_LIFE_DURATION = new Object() {
		@Override
		public String toString() {
			return String.valueOf((RANGE_CONFIG.getValue() + 60) / 10);
		}
	};

	private static final Object SELF_PURIFICATION_DURATION = new Object() {
		@Override
		public String toString() {
			return String.valueOf((RANGE_CONFIG.getValue() + 30) / 10);
		}
	};

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Ferda.class, "cooldown", 80, "# 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};

	public static final SettingObject<Integer> RANGE_CONFIG = abilitySettings.new SettingObject<Integer>(Ferda.class, "range", 10,
			"# 스킬 범위 (스킬 범위에 따라 두 능력의 지속 시간도 늘어나니 너무 크게 설정하지 말 것)") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1 && value <= 50;
		}

	};
	private static final Relative[] leaves = {
			new RelativeRange(-2, -1, -1, 2, 0, 1, MaterialX.OAK_LEAVES, (x, y, z) -> !(x == 0 && z == 0 && y == -1)),
			new RelativeRange(-1, 1, -1, 1, 1, 1, MaterialX.OAK_LEAVES, null),
			new RelativeFixed(0, 2, 0, MaterialX.OAK_LEAVES),
			new RelativeRange(-1, -1, 2, 1, 0, 2, MaterialX.OAK_LEAVES, null),
			new RelativeRange(-1, -1, -2, 1, 0, -2, MaterialX.OAK_LEAVES, null)
	};
	private static final Relative[] relatives = {
			new RelativeRange(-1, 0, -1, 1, 1, 1, MaterialX.OAK_LOG, (x, y, z) -> !((x == -1 || x == 1) && (z == -1 || z == 1)) && !(x == 0 && z == 0 && (y == 0 || y == 1))),
			new RelativeFixed(0, -1, 0, MaterialX.OAK_LOG),
			new RelativeFixed(0, 2, 0, MaterialX.OAK_LOG),
			new RelativeFixed(1, 2, 0, MaterialX.OAK_LEAVES),
			new RelativeFixed(-1, 2, 0, MaterialX.OAK_LEAVES),
			new RelativeFixed(0, 2, 1, MaterialX.OAK_LEAVES),
			new RelativeFixed(0, 2, -1, MaterialX.OAK_LEAVES),
			new RelativeFixed(0, 3, 0, MaterialX.OAK_LEAVES)
	};
	private final Predicate<Entity> ONLY_PARTICIPANTS = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			return getGame().isParticipating(entity.getUniqueId())
					&& (!(getGame() instanceof DeathManager.Handler) || !((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()));
		}
	};
	private final int range = RANGE_CONFIG.getValue();
	private final double particleRange = (range / 3.0) * 2;
	private final Cooldown cooldown = new Cooldown(COOLDOWN_CONFIG.getValue(), 60), leftCool = new Cooldown(COOLDOWN_CONFIG.getValue(), 60);
	private CradleOfLife cradleOfLife = null;
	private SelfPurification selfPurification = null;

	public Ferda(Participant participant) {
		super(participant);
	}

	@Nullable
	private static Entity getDamager(final Entity damager) {
		if (damager instanceof Projectile) {
			final ProjectileSource shooter = ((Projectile) damager).getShooter();
			return shooter instanceof Entity ? (Entity) shooter : null;
		} else return damager;
	}

	@Override
	public boolean ActiveSkill(Material material, ClickType clickType) {
		if (material == Material.IRON_INGOT) {
			if (clickType == ClickType.RIGHT_CLICK) {
				if (cradleOfLife == null && !cooldown.isCooldown()) {
					new CradleOfLife().start();
				}
			} else if (clickType == ClickType.LEFT_CLICK) {
				if (selfPurification == null && !leftCool.isCooldown()) {
					new SelfPurification().start();
				}
			}
		}
		return false;
	}

	@SubscribeEvent
	private void onSnapshot(final SnapshotEvent e) {
		if (e.snapshot != null) return;
		if (cradleOfLife != null) {
			final IBlockSnapshot snapshot = cradleOfLife.snapshots.get(e.block);
			if (snapshot != null) {
				e.snapshot = snapshot;
				return;
			}
		}
		if (selfPurification != null) {
			final IBlockSnapshot snapshot = selfPurification.snapshots.get(e.block);
			if (snapshot != null) e.snapshot = snapshot;
		}
	}

	@SubscribeEvent
	private void onBlockBreak(final BlockBreakEvent e) {
		if (cradleOfLife != null && cradleOfLife.snapshots.containsKey(e.getBlock())) {
			e.setCancelled(true);
			return;
		}
		if (selfPurification != null && selfPurification.snapshots.containsKey(e.getBlock())) {
			e.setCancelled(true);
		}
	}

	@SubscribeEvent
	private void onLeavesDecay(final LeavesDecayEvent e) {
		if (cradleOfLife != null && cradleOfLife.snapshots.containsKey(e.getBlock())) {
			e.setCancelled(true);
			return;
		}
		if (selfPurification != null && selfPurification.snapshots.containsKey(e.getBlock())) {
			e.setCancelled(true);
		}
	}

	private void regainHealth(final LivingEntity livingEntity, final double amount) {
		livingEntity.setHealth(RangesKt.coerceIn(livingEntity.getHealth() + amount, 0, livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
	}

	private boolean regainHealth(final Player player, final double amount) {
		if (getPlayer().equals(player)) {
			getPlayer().setHealth(RangesKt.coerceIn(getPlayer().getHealth() + amount, 0, getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
			return true;
		} else {
			final EntityRegainHealthEvent event = new EntityRegainHealthEvent(player, amount, RegainReason.CUSTOM);
			Bukkit.getPluginManager().callEvent(event);
			if (!event.isCancelled()) {
				player.setHealth(RangesKt.coerceIn(player.getHealth() + event.getAmount(), 0, player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
				return true;
			}
		}
		return false;
	}

	@FunctionalInterface
	private interface CoordConsumer {

		void accept(final int x, final int y, final int z, final MaterialX type);

	}

	private interface Relative {
		void forEach(final CoordConsumer consumer);
	}

	private static class RelativeFixed implements Relative {

		private final int x, y, z;
		private final MaterialX material;

		private RelativeFixed(final int x, final int y, final int z, final MaterialX material) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.material = material;
		}

		@Override
		public void forEach(CoordConsumer consumer) {
			consumer.accept(x, y, z, material);
		}
	}

	private static class RelativeRange implements Relative {

		private final int minX, minY, minZ, maxX, maxY, maxZ;
		private final MaterialX material;
		private final CoordPredicate predicate;

		private RelativeRange(final int minX, final int minY, final int minZ, final int maxX, final int maxY, final int maxZ, final MaterialX material, final CoordPredicate predicate) {
			this.minX = minX;
			this.minY = minY;
			this.minZ = minZ;
			this.maxX = maxX;
			this.maxY = maxY;
			this.maxZ = maxZ;
			this.material = material;
			this.predicate = predicate;
		}

		@Override
		public void forEach(final CoordConsumer consumer) {
			for (int x = minX; x <= maxX; x++) {
				for (int y = minY; y <= maxY; y++) {
					for (int z = minZ; z <= maxZ; z++) {
						if (predicate != null && !predicate.test(x, y, z)) continue;
						consumer.accept(x, y, z, material);
					}
				}
			}
		}
	}

	private static class SnapshotEvent extends Event {

		private static final HandlerList handlers = new HandlerList();
		private final Block block;
		private IBlockSnapshot snapshot = null;

		private SnapshotEvent(final Block block) {
			this.block = block;
		}

		public static HandlerList getHandlerList() {
			return handlers;
		}

		@Override
		public @NotNull HandlerList getHandlers() {
			return handlers;
		}

	}

	private static class RootEvent extends Event implements Cancellable {

		private static final HandlerList handlers = new HandlerList();
		private final Player player;
		private boolean cancelled = false;

		private RootEvent(final Player player) {
			this.player = player;
		}

		public static HandlerList getHandlerList() {
			return handlers;
		}

		@Override
		public @NotNull HandlerList getHandlers() {
			return handlers;
		}

		@Override
		public boolean isCancelled() {
			return cancelled;
		}

		@Override
		public void setCancelled(boolean cancelled) {
			this.cancelled = cancelled;
		}
	}

	private class SelfPurification extends AbilityTimer implements Listener {

		private final Map<Block, IBlockSnapshot> snapshots = new HashMap<>();
		private final ActionbarChannel actionbarChannel;
		private final Location center = getPlayer().getLocation().clone();
		private Block lastBlock;
		private boolean leaves = false;

		private SelfPurification() {
			super(TaskType.REVERSE, range + 30);
			setPeriod(TimeUnit.TICKS, 2);
			this.actionbarChannel = getParticipant().actionbar().newChannel();
			Ferda.this.selfPurification = this;
		}

		private void addSnapshot(final Block block) {
			if (!snapshots.containsKey(block)) {
				final SnapshotEvent event = new SnapshotEvent(block);
				Bukkit.getPluginManager().callEvent(event);
				final IBlockSnapshot snapshot = event.snapshot;
				if (snapshot == null) {
					snapshots.put(block, Blocks.createSnapshot(block));
				} else {
					snapshots.put(block, snapshot);
				}
			}
		}

		private void restoreAll() {
			for (final Iterator<IBlockSnapshot> iterator = snapshots.values().iterator(); iterator.hasNext(); ) {
				iterator.next().apply();
				iterator.remove();
			}
		}

		@Override
		protected void run(int count) {
			actionbarChannel.update(toString());
			if (count >= (getMaximumCount() - range)) {
				final int radius = getMaximumCount() - count + 1;
				for (Block block : LocationUtil.getBlocks2D(center, radius, true, true, true)) {
					Block below = block.getRelative(BlockFace.DOWN);
					if (snapshots.containsKey(below)) {
						below = below.getRelative(BlockFace.DOWN);
					}
					addSnapshot(below);
					BlockX.setType(below, MaterialX.OAK_LEAVES);
				}
			}
			if (count >= 36) {
				if (lastBlock == null) {
					this.lastBlock = getPlayer().getLocation().getBlock();
				} else {
					this.lastBlock = lastBlock.getRelative(BlockFace.UP);
				}
				addSnapshot(lastBlock);
				if (!checkBlocks(lastBlock)) {
					setCount(35);
				}
				SoundLib.BLOCK_WOOD_PLACE.playSound(lastBlock.getLocation(), .5f, 1f);
				BlockX.setType(lastBlock, MaterialX.OAK_LOG);
			} else {
				if (!leaves) {
					this.leaves = true;
					if (lastBlock != null) {
						final CoordConsumer consumer = new CoordConsumer() {
							@Override
							public void accept(int x, int y, int z, MaterialX material) {
								final Block blockRel = lastBlock.getRelative(x, y, z);
								addSnapshot(blockRel);
								SoundLib.BLOCK_WOOD_PLACE.playSound(blockRel.getLocation(), .5f, 1f);
								BlockX.setType(blockRel, material);
							}
						};
						for (Relative relative : Ferda.leaves) {
							relative.forEach(consumer);
						}
					}

					regainHealth(getPlayer(), 3);
					SoundLib.ENTITY_PLAYER_LEVELUP.playSound(getPlayer());
					for (LivingEntity livingEntity : LocationUtil.getNearbyEntities(LivingEntity.class, getPlayer().getLocation(), range, range, ONLY_PARTICIPANTS)) {
						if (getPlayer().equals(livingEntity)) continue;
						if (livingEntity instanceof Player) {
							final Player player = (Player) livingEntity;
							if (regainHealth(player, 3)) {
								SoundLib.ENTITY_PLAYER_LEVELUP.playSound(player);
							}
						} else {
							regainHealth(livingEntity, 3);
						}
					}
				}
			}
		}

		private boolean checkBlocks(final Block criterion) {
			if (!criterion.isEmpty()) return false;
			final Block up = criterion.getRelative(BlockFace.UP);
			return up.isEmpty() && up.getRelative(BlockFace.UP).isEmpty();
		}

		@EventHandler
		private void onPlayerMove(final PlayerMoveEvent e) {
			if (getPlayer().getUniqueId().equals(e.getPlayer().getUniqueId())) {
				final Location to = e.getTo(), from = e.getFrom();
				if (to != null) {
					to.setX(from.getX());
					to.setY(from.getY());
					to.setZ(from.getZ());
				}
			}
		}

		@EventHandler
		private void onEntityDamage(final EntityDamageEvent e) {
			if (getPlayer().equals(e.getEntity())) {
				e.setCancelled(true);
			}
		}

		@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
		private void onEntityDamageByEntity(final EntityDamageByEntityEvent e) {
			if (getPlayer().equals(getDamager(e.getDamager()))) {
				e.setCancelled(true);
			}
		}

		@Override
		protected void onStart() {
			Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
			final Location lastBlockLoc = getPlayer().getLocation().getBlock().getLocation(), loc = getPlayer().getLocation().clone();
			loc.setX(lastBlockLoc.getX() + .5);
			loc.setY(lastBlockLoc.getY());
			loc.setZ(lastBlockLoc.getZ() + .5);
			getPlayer().teleport(loc);
		}

		@Override
		protected void onEnd() {
			leftCool.start();
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			actionbarChannel.unregister();
			restoreAll();
			HandlerList.unregisterAll(this);
			Ferda.this.selfPurification = null;
		}

		@Override
		public final String toString() {
			return "§2자정 작용§f" + ": §a" + (getCount() / 10.0) + "초";
		}

	}

	public class CradleOfLife extends AbilityTimer implements Listener {

		private final Map<Block, IBlockSnapshot> snapshots = new HashMap<>();
		private final ActionbarChannel actionbarChannel = getParticipant().actionbar().newChannel();
		private final Location center = getPlayer().getLocation().clone();
		private final Map<UUID, Root> rooted = new HashMap<>();
		private final Predicate<Entity> notRooted = ONLY_PARTICIPANTS.and(new Predicate<Entity>() {
			@Override
			public boolean test(Entity entity) {
				return !rooted.containsKey(entity.getUniqueId());
			}
		});
		private final Predicate<Entity> strictPredicate = new Predicate<Entity>() {
			@Override
			public boolean test(Entity entity) {
				if (entity.equals(getPlayer())) return false;
				if (rooted.containsKey(entity.getUniqueId())) return false;
				{
					final Participant participant = getGame().getParticipant(entity.getUniqueId());
					if (participant == null
							|| (getGame() instanceof DeathManager.Handler && ((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
							|| !participant.attributes().TARGETABLE.getValue()
							|| participant.getAbility() instanceof Ferda) {
						return false;
					}
				}
				if (getGame() instanceof Teamable) {
					final Teamable teamGame = (Teamable) getGame();
					final Participant entityParticipant = teamGame.getParticipant(entity.getUniqueId()), participant = getParticipant();
					return !teamGame.hasTeam(entityParticipant) || !teamGame.hasTeam(participant) || (!teamGame.getTeam(entityParticipant).equals(teamGame.getTeam(participant)));
				}
				return true;
			}
		};
		private CradleOfLife() {
			super(TaskType.REVERSE, range + 60);
			setPeriod(TimeUnit.TICKS, 2);
			setBehavior(RestrictionBehavior.PAUSE_RESUME);
		}

		private void addSnapshot(final Block block) {
			if (!snapshots.containsKey(block)) {
				final SnapshotEvent event = new SnapshotEvent(block);
				Bukkit.getPluginManager().callEvent(event);
				final IBlockSnapshot snapshot = event.snapshot;
				if (snapshot == null) {
					snapshots.put(block, Blocks.createSnapshot(block));
				} else {
					snapshots.put(block, snapshot);
				}
			}
		}

		private void restoreAll() {
			for (final Iterator<IBlockSnapshot> iterator = snapshots.values().iterator(); iterator.hasNext(); ) {
				iterator.next().apply();
				iterator.remove();
			}
		}

		@EventHandler
		private void onRoot(final RootEvent e) {
			if (rooted.containsKey(e.player.getUniqueId())) {
				e.setCancelled(true);
			}
		}

		@EventHandler(ignoreCancelled = true)
		private void onEntityDamageByEntity(final EntityDamageByEntityEvent e) {
			if (getPlayer().equals(e.getEntity())) {
				final Entity damager = getDamager(e.getDamager());
				if (damager != null && strictPredicate.test(damager)) {
					new Root((Player) damager);
				}
			}
		}

		@Override
		protected void onStart() {
			Ferda.this.cradleOfLife = this;
			Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		}

		@Override
		protected void run(int count) {
			if (count % 5 == 0) {
				ParticleLib.VILLAGER_HAPPY.spawnParticle(center, particleRange, particleRange, particleRange, 30, 0);
				ParticleLib.END_ROD.spawnParticle(center, particleRange, particleRange, particleRange, 30, 0);
			}
			actionbarChannel.update(toString());
			if (count >= (getMaximumCount() - range)) {
				final int radius = getMaximumCount() - count + 1;
				for (Block block : LocationUtil.getBlocks2D(center, radius, true, true, true)) {
					Block below = block.getRelative(BlockFace.DOWN);
					if (snapshots.containsKey(below)) {
						below = below.getRelative(BlockFace.DOWN);
					}
					addSnapshot(below);
					BlockX.setType(below, MaterialX.OAK_LEAVES);
				}
				for (Player player : LocationUtil.getEntitiesInCircle(Player.class, center, radius, strictPredicate)) {
					final RootEvent event = new RootEvent(player);
					Bukkit.getPluginManager().callEvent(event);
					if (!event.isCancelled()) {
						new Root(player);
					}
				}
			}
		}

		@Override
		protected void onEnd() {
			cooldown.start();
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			HandlerList.unregisterAll(this);
			actionbarChannel.unregister();
			restoreAll();
			Ferda.this.cradleOfLife = null;
		}

		@Override
		public final String toString() {
			return "§2생명의 요람§f" + ": §a" + (getCount() / 10.0) + "초";
		}

		private class Root extends AbilityTimer implements Listener {

			private final Player player;
			private final ActionbarChannel actionbarChannel;
			private Block lastBlock;
			private boolean placed = false;

			private Root(final Player player) {
				super();
				setPeriod(TimeUnit.TICKS, 2);
				this.player = player;
				final Participant participant = getGame().getParticipant(player);
				this.actionbarChannel = participant.actionbar().newChannel();
				rooted.put(player.getUniqueId(), this);
				CradleOfLife.this.attachObserver(new Observer() {
					@Override
					public void onEnd() {
						Root.this.stop(false);
					}

					@Override
					public void onSilentEnd() {
						Root.this.stop(true);
					}
				});
				start();
			}

			@Override
			protected void run(int count) {
				actionbarChannel.update(toString());
				if (count <= 4) {
					if (lastBlock == null) {
						this.lastBlock = player.getLocation().getBlock();
					} else {
						final IBlockSnapshot snapshot = snapshots.remove(lastBlock);
						if (snapshot != null) snapshot.apply();
						this.lastBlock = lastBlock.getRelative(BlockFace.UP);
					}
					addSnapshot(lastBlock);
					if (!checkBlocks(lastBlock)) {
						setCount(5);
					}
					SoundLib.BLOCK_WOOD_PLACE.playSound(lastBlock.getLocation(), .5f, 1f);
					BlockX.setType(lastBlock, MaterialX.OAK_LOG);
					final Location lastBlockLoc = lastBlock.getLocation(), loc = player.getLocation().clone();
					loc.setX(lastBlockLoc.getX() + .5);
					loc.setY(lastBlockLoc.getY() + 2);
					loc.setZ(lastBlockLoc.getZ() + .5);
					player.teleport(loc);
				} else {
					if (!placed) {
						this.placed = true;
						final IBlockSnapshot snapshot = snapshots.remove(lastBlock);
						if (snapshot != null) snapshot.apply();
						final Block block = player.getLocation().getBlock();
						final CoordConsumer consumer = new CoordConsumer() {
							@Override
							public void accept(int x, int y, int z, MaterialX material) {
								final Block blockRel = block.getRelative(x, y, z);
								addSnapshot(blockRel);
								SoundLib.BLOCK_WOOD_PLACE.playSound(blockRel.getLocation(), .5f, 1f);
								BlockX.setType(blockRel, material);
							}
						};
						for (Relative relative : relatives) {
							relative.forEach(consumer);
						}
						final Location blockLoc = block.getLocation(), loc = player.getLocation().clone();
						loc.setX(blockLoc.getX() + .5);
						loc.setY(blockLoc.getY());
						loc.setZ(blockLoc.getZ() + .5);
						player.teleport(loc);
					}
				}
			}

			private boolean checkBlocks(final Block criterion) {
				if (!criterion.isEmpty()) return false;
				final Block up = criterion.getRelative(BlockFace.UP);
				return up.isEmpty() && up.getRelative(BlockFace.UP).isEmpty();
			}

			@EventHandler
			private void onPlayerMove(final PlayerMoveEvent e) {
				if (player.getUniqueId().equals(e.getPlayer().getUniqueId())) {
					final Location to = e.getTo(), from = e.getFrom();
					if (to != null) {
						to.setX(from.getX());
						to.setY(from.getY());
						to.setZ(from.getZ());
					}
				}
			}

			@EventHandler
			private void onEntityDamage(final EntityDamageEvent e) {
				if (player.getUniqueId().equals(e.getEntity().getUniqueId())) {
					e.setCancelled(true);
				}
			}

			@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
			private void onEntityDamageByEntity(final EntityDamageByEntityEvent e) {
				if (getPlayer().equals(e.getEntity())) {
					final Entity damager = getDamager(e.getDamager());
					if (damager != null && strictPredicate.test(damager)) {
						new Root((Player) damager);
					}
				}
			}

			@EventHandler
			private void onEntityRegainHealth(final EntityRegainHealthEvent e) {
				if (player.equals(e.getEntity())) {
					e.setCancelled(true);
					for (LivingEntity livingEntity : LocationUtil.getNearbyEntities(LivingEntity.class, center, range, range, notRooted)) {
						if (livingEntity instanceof Player) {
							final Player player = (Player) livingEntity;
							if (regainHealth(player, e.getAmount())) {
								SoundLib.ENTITY_PLAYER_LEVELUP.playSound(player);
							}
						} else {
							regainHealth(livingEntity, e.getAmount() / 2);
						}
					}
				}
			}

			@Override
			protected void onStart() {
				Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
			}

			@Override
			protected void onEnd() {
				onSilentEnd();
			}

			@Override
			protected void onSilentEnd() {
				actionbarChannel.unregister();
				HandlerList.unregisterAll(this);
				rooted.remove(player.getUniqueId());
			}

			@Override
			public final String toString() {
				return CradleOfLife.this.toString();
			}

		}

	}

	@FunctionalInterface
	public interface CoordPredicate {
		boolean test(int x, int y, int z);
	}

}
