package daybreak.abilitywar.game.list.mix.synergy.list;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.Scheduled;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.LocationUtil.Predicates;
import daybreak.abilitywar.utils.base.math.geometry.Boundary.BoundingBox;
import daybreak.abilitywar.utils.base.math.geometry.Boundary.EntityBoundingBox;
import daybreak.abilitywar.utils.base.minecraft.FallingBlocks;
import daybreak.abilitywar.utils.base.minecraft.FallingBlocks.Behavior;
import daybreak.abilitywar.utils.base.minecraft.compat.block.BlockHandler;
import daybreak.abilitywar.utils.base.minecraft.compat.block.BlockSnapshot;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.library.BlockX;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.PotionEffects;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

@AbilityManifest(name = "절대 영도", rank = Rank.S, species = Species.OTHERS, explain = {
		"철괴를 좌클릭하면 자신이 보고 있는 방향으로 §b얼음§f을 날립니다. $[LeftCooldownConfig]",
		"§b얼음§f에 맞은 생명체는 2초간 얼어붙으며, 대미지를 입지 않습니다.",
		"주변을 지나가는 발사체들이 모두 얼어붙어 바닥으로 떨어집니다.",
		"눈과 얼음 위에서 §6힘§f, §b신속 §f버프를 받습니다.",
		"철괴를 우클릭하면 주변을 눈 지형으로 바꿉니다. $[CooldownConfig]",
		"우클릭 능력 사용시 주위에 있었던 모든 플레이어를 6초간 얼립니다."
})
public class AbsoluteZero extends Synergy implements ActiveHandler {

	public static final SettingObject<Integer> CooldownConfig = synergySettings.new SettingObject<Integer>(AbsoluteZero.class, "Cooldown", 80, "# 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};

	public static final SettingObject<Integer> RangeConfig = synergySettings.new SettingObject<Integer>(AbsoluteZero.class, "Range", 15,
			"# 스킬 사용 시 눈 지형으로 바꿀 범위") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1 && value <= 50;
		}

	};

	private static final SettingObject<Integer> LeftCooldownConfig = synergySettings.new SettingObject<Integer>(AbsoluteZero.class, "LeftCooldown", 10, "# 좌클릭 쿨타임") {

		@Override
		public boolean condition(Integer arg0) {
			return arg0 >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};
	private static final Set<LivingEntity> frozenEntities = new HashSet<>();
	@Scheduled
	private final Timer buff = new Timer() {

		@Override
		public void onStart() {
		}

		@Override
		public void run(int count) {
			Material m = getPlayer().getLocation().getBlock().getType();
			Material bm = getPlayer().getLocation().subtract(0, 1, 0).getBlock().getType();
			if (m.equals(Material.SNOW) || bm.equals(Material.SNOW) || bm.equals(Material.SNOW_BLOCK) || bm.equals(Material.ICE) || bm.equals(Material.PACKED_ICE)) {
				PotionEffects.SPEED.addPotionEffect(getPlayer(), 5, 2, true);
				PotionEffects.INCREASE_DAMAGE.addPotionEffect(getPlayer(), 5, 1, true);
			}
		}

		@Override
		public void onEnd() {
		}

	}.setPeriod(TimeUnit.TICKS, 1);

	private final Map<Block, BlockSnapshot> blockData = new HashMap<>();

	private final int range = RangeConfig.getValue();
	private final Timer iceMaker = new Timer(range) {

		private int count;
		private Location center;

		@Override
		public void onStart() {
			count = 1;
			center = getPlayer().getLocation();
		}

		@Override
		public void run(int sec) {
			Location playerLocation = getPlayer().getLocation();
			World world = getPlayer().getWorld();
			for (Block block : LocationUtil.getBlocks2D(center, count, true, false, true)) {
				block = world.getBlockAt(block.getX(), LocationUtil.getFloorYAt(world, playerLocation.getY(), block.getX(), block.getZ()), block.getZ());
				Block belowBlock = block.getRelative(BlockFace.DOWN);
				Material type = belowBlock.getType();
				if (type == Material.WATER) {
					blockData.putIfAbsent(belowBlock, BlockHandler.createSnapshot(belowBlock));
					belowBlock.setType(Material.PACKED_ICE);
				} else if (type == Material.LAVA) {
					blockData.putIfAbsent(belowBlock, BlockHandler.createSnapshot(belowBlock));
					belowBlock.setType(Material.OBSIDIAN);
				} else if (MaterialX.ACACIA_LEAVES.compareType(belowBlock) || MaterialX.BIRCH_LEAVES.compareType(belowBlock) || MaterialX.DARK_OAK_LEAVES.compareType(belowBlock)
						|| MaterialX.JUNGLE_LEAVES.compareType(belowBlock) || MaterialX.OAK_LEAVES.compareType(belowBlock) || MaterialX.SPRUCE_LEAVES.compareType(belowBlock)) {
					BlockX.setType(belowBlock, MaterialX.GREEN_WOOL);
				} else {
					blockData.putIfAbsent(belowBlock, BlockHandler.createSnapshot(belowBlock));
					belowBlock.setType(Material.SNOW_BLOCK);
				}

				blockData.putIfAbsent(block, BlockHandler.createSnapshot(block));
				block.setType(Material.SNOW);
			}
			count++;
		}

		@Override
		public void onEnd() {
		}

	}.setPeriod(TimeUnit.TICKS, 1);
	private final CooldownTimer yetiCooldownTimer = new CooldownTimer(CooldownConfig.getValue());
	private final CooldownTimer cooldownTimer = new CooldownTimer(LeftCooldownConfig.getValue());
	private final Predicate<Entity> strictPredicate = Predicates.STRICT(getPlayer());
	private final List<Projectile> projectiles = new ArrayList<Projectile>() {
		@Override
		public boolean add(Projectile projectile) {
			if (size() >= 15) clear();
			return super.add(projectile);
		}
	};
	@Scheduled
	private final Timer passive = new Timer() {
		@Override
		protected void run(int count) {
			Location center = getPlayer().getLocation();
			for (Projectile projectile : LocationUtil.getNearbyEntities(Projectile.class, center, 7, 7)) {
				if (!projectile.isOnGround() && !projectiles.contains(projectile) && LocationUtil.isInCircle(center, projectile.getLocation(), 7)) {
					projectiles.add(projectile);
					projectile.setVelocity(projectile.getVelocity().multiply(0.1));
					new Timer(3) {
						@Override
						protected void onStart() {
							projectile.getLocation().getBlock().setType(Material.ICE);
						}

						@Override
						protected void run(int count) {
						}

						@Override
						protected void onEnd() {
							projectile.getLocation().getBlock().setType(Material.AIR);
						}
					}.start();
				}
			}
		}
	}.setPeriod(TimeUnit.TICKS, 1);

	public AbsoluteZero(Participant participant) {
		super(participant);
	}

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.ABILITY_DESTROY) {
			for (Entry<Block, BlockSnapshot> entry : blockData.entrySet()) {
				Block key = entry.getKey();
				if (key.getType() == Material.PACKED_ICE || key.getType() == Material.OBSIDIAN || key.getType() == Material.SNOW_BLOCK || key.getType() == Material.SNOW) {
					entry.getValue().apply();
				}
			}
		}
	}

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT)) {
			if (clickType.equals(ClickType.LEFT_CLICK)) {
				if (!cooldownTimer.isCooldown()) {
					FallingBlock fallingBlock = FallingBlocks.spawnFallingBlock(getPlayer().getEyeLocation(), Material.PACKED_ICE, true, getPlayer().getLocation().getDirection().multiply(1.7), new Behavior() {
						@Override
						public boolean onEntityChangeBlock(FallingBlock fallingBlock) {
							Block block = fallingBlock.getLocation().getBlock();
							for (int x = -1; x < 1; x++) {
								for (int y = -1; y < 1; y++) {
									for (int z = -1; z < 1; z++) {
										block.getRelative(x, y, z).setType(Material.PACKED_ICE);
									}
								}
							}
							return true;
						}
					});
					BoundingBox boundingBox = EntityBoundingBox.of(fallingBlock);
					new Timer() {
						@Override
						protected void run(int count) {
							if (fallingBlock.isValid() && !fallingBlock.isDead()) {
								for (LivingEntity livingEntity : LocationUtil.getConflictingEntities(LivingEntity.class, boundingBox, strictPredicate)) {
									if (frozenEntities.add(livingEntity)) {
										new Frost(livingEntity).start();
									}
								}
							} else {
								stop(false);
							}
						}
					}.setPeriod(TimeUnit.TICKS, 1).start();

					cooldownTimer.start();
					return true;
				}
			} else if (clickType.equals(ClickType.RIGHT_CLICK)) {
				if (!yetiCooldownTimer.isCooldown()) {
					iceMaker.start();
					yetiCooldownTimer.start();
					for (LivingEntity entity : LocationUtil.getNearbyEntities(LivingEntity.class, getPlayer(), range, range)) {
						if (entity instanceof Player && !getGame().isParticipating((Player) entity)) continue;
						new Frost(entity, 6).start();
					}
					return true;
				}
			}
		}
		return false;
	}

	@SubscribeEvent
	private void onProjectileHit(ProjectileHitEvent e) {
		projectiles.remove(e.getEntity());
	}

	public class Frost extends Timer implements Listener {

		private final LivingEntity target;
		private final Block[] blocks = new Block[2];
		private final BlockSnapshot[] snapshots = new BlockSnapshot[2];
		private final Location teleport;
		private ActionbarChannel actionbarChannel;

		private Frost(LivingEntity target, int seconds) {
			super(TaskType.REVERSE, seconds * 20);
			setPeriod(TimeUnit.TICKS, 1);
			this.target = target;
			blocks[0] = target.getEyeLocation().getBlock();
			blocks[1] = blocks[0].getRelative(BlockFace.DOWN);
			if (ServerVersion.getVersionNumber() >= 10) target.setInvulnerable(true);
			for (int i = 0; i < 2; i++) {
				snapshots[i] = BlockHandler.createSnapshot(blocks[i]);
				blocks[i].setType(Material.ICE);
			}
			this.teleport = blocks[1].getLocation().clone().add(0.5, 0, 0.5).setDirection(target.getLocation().getDirection());
			if (target instanceof Player) {
				Player player = (Player) target;
				if (getGame().isParticipating(player)) {
					this.actionbarChannel = getGame().getParticipant(player).actionbar().newChannel();
				}
			}
		}

		private Frost(LivingEntity target) {
			this(target, 2);
		}

		@EventHandler
		public void onBlockBreak(BlockBreakEvent e) {
			if (e.getBlock().equals(blocks[0]) || e.getBlock().equals(blocks[1])) e.setCancelled(true);
		}

		@EventHandler
		public void onExplode(BlockExplodeEvent e) {
			e.blockList().removeIf(block -> block.equals(blocks[0]) || block.equals(blocks[1]));
		}

		@EventHandler
		public void onExplode(EntityExplodeEvent e) {
			e.blockList().removeIf(block -> block.equals(blocks[0]) || block.equals(blocks[1]));
		}

		@EventHandler
		private void onEntityDamage(EntityDamageEvent e) {
			if (e.getEntity().equals(target)) {
				e.setCancelled(true);
			}
		}

		@EventHandler
		private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
			onEntityDamage(e);
		}

		@EventHandler
		private void onEntityDamageByBlock(EntityDamageByBlockEvent e) {
			onEntityDamage(e);
		}

		@Override
		protected void onStart() {
			Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		}

		@Override
		protected void run(int count) {
			target.teleport(teleport);
			if (actionbarChannel != null)
				actionbarChannel.update("§b빙결§f: " + (getCount() / 20.0) + "초");
		}

		@Override
		protected void onEnd() {
			HandlerList.unregisterAll(this);
			if (ServerVersion.getVersionNumber() >= 10) target.setInvulnerable(false);
			for (int i = 0; i < 2; i++) {
				snapshots[i].apply();
			}
			if (actionbarChannel != null) actionbarChannel.unregister();
			frozenEntities.remove(target);
		}

		@Override
		protected void onSilentEnd() {
			HandlerList.unregisterAll(this);
			if (ServerVersion.getVersionNumber() >= 10) target.setInvulnerable(false);
			for (int i = 0; i < 2; i++) {
				snapshots[i].apply();
			}
			if (actionbarChannel != null) actionbarChannel.unregister();
			frozenEntities.remove(target);
		}

	}

}
