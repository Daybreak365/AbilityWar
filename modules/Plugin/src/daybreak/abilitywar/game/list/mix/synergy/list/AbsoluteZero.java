package daybreak.abilitywar.game.list.mix.synergy.list;

import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;
import daybreak.abilitywar.game.manager.effect.Frost;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.minecraft.FallingBlocks;
import daybreak.abilitywar.utils.base.minecraft.FallingBlocks.Behavior;
import daybreak.abilitywar.utils.base.minecraft.block.Blocks;
import daybreak.abilitywar.utils.base.minecraft.block.IBlockSnapshot;
import daybreak.abilitywar.utils.base.minecraft.boundary.BoundingBox;
import daybreak.abilitywar.utils.base.minecraft.boundary.EntityBoundingBox;
import daybreak.abilitywar.utils.library.BlockX;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.PotionEffects;
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
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

@AbilityManifest(name = "절대 영도", rank = Rank.S, species = Species.OTHERS, explain = {
		"철괴를 좌클릭하면 자신이 보고 있는 방향으로 §b얼음§f을 날립니다. $[LEFT_COOLDOWN_CONFIG]",
		"§b얼음§f에 맞은 생명체는 2초간 얼어붙으며, 대미지를 입지 않습니다.",
		"주변을 지나가는 발사체들이 모두 얼어붙어 바닥으로 떨어집니다.",
		"눈과 얼음 위에서 §6힘§f, §b신속 §f버프를 받습니다.",
		"철괴를 우클릭하면 주변을 눈 지형으로 바꿉니다. $[COOLDOWN_CONFIG]",
		"우클릭 능력 사용시 주위에 있었던 모든 플레이어를 6초간 얼립니다."
})
public class AbsoluteZero extends Synergy implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = synergySettings.new SettingObject<Integer>(AbsoluteZero.class, "cooldown", 80, "# 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};

	public static final SettingObject<Integer> RangeConfig = synergySettings.new SettingObject<Integer>(AbsoluteZero.class, "range", 15,
			"# 스킬 사용 시 눈 지형으로 바꿀 범위") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1 && value <= 50;
		}

	};

	private static final SettingObject<Integer> LEFT_COOLDOWN_CONFIG = synergySettings.new SettingObject<Integer>(AbsoluteZero.class, "LeftCooldown", 10, "# 좌클릭 쿨타임") {

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

	private final AbilityTimer buff = new AbilityTimer() {

		@Override
		public void run(int count) {
			Material m = getPlayer().getLocation().getBlock().getType();
			Material bm = getPlayer().getLocation().subtract(0, 1, 0).getBlock().getType();
			if (m.equals(Material.SNOW) || bm.equals(Material.SNOW) || bm.equals(Material.SNOW_BLOCK) || bm.equals(Material.ICE) || bm.equals(Material.PACKED_ICE)) {
				PotionEffects.SPEED.addPotionEffect(getPlayer(), 5, 2, true);
				PotionEffects.INCREASE_DAMAGE.addPotionEffect(getPlayer(), 5, 1, true);
			}
		}

	}.setPeriod(TimeUnit.TICKS, 1).register();

	private final Map<Block, IBlockSnapshot> blockData = new HashMap<>();

	private final int range = RangeConfig.getValue();
	private final AbilityTimer iceMaker = new AbilityTimer(range) {

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
					blockData.putIfAbsent(belowBlock, Blocks.createSnapshot(belowBlock));
					belowBlock.setType(Material.PACKED_ICE);
				} else if (type == Material.LAVA) {
					blockData.putIfAbsent(belowBlock, Blocks.createSnapshot(belowBlock));
					belowBlock.setType(Material.OBSIDIAN);
				} else if (MaterialX.ACACIA_LEAVES.compare(belowBlock) || MaterialX.BIRCH_LEAVES.compare(belowBlock) || MaterialX.DARK_OAK_LEAVES.compare(belowBlock)
						|| MaterialX.JUNGLE_LEAVES.compare(belowBlock) || MaterialX.OAK_LEAVES.compare(belowBlock) || MaterialX.SPRUCE_LEAVES.compare(belowBlock)) {
					BlockX.setType(belowBlock, MaterialX.GREEN_WOOL);
				} else {
					blockData.putIfAbsent(belowBlock, Blocks.createSnapshot(belowBlock));
					belowBlock.setType(Material.SNOW_BLOCK);
				}

				blockData.putIfAbsent(block, Blocks.createSnapshot(block));
				block.setType(Material.SNOW);
			}
			count++;
		}

	}.setPeriod(TimeUnit.TICKS, 1).register();
	private final Cooldown yetiCooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());
	private final Cooldown cooldownTimer = new Cooldown(LEFT_COOLDOWN_CONFIG.getValue());
	private final Predicate<Entity> predicate = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			if (entity.equals(getPlayer())) return false;
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
	private final List<Projectile> projectiles = new ArrayList<Projectile>() {
		@Override
		public boolean add(Projectile projectile) {
			if (size() >= 15) clear();
			return super.add(projectile);
		}
	};

	private final AbilityTimer passive = new AbilityTimer() {
		@Override
		protected void run(int count) {
			Location center = getPlayer().getLocation();
			for (Projectile projectile : LocationUtil.getNearbyEntities(Projectile.class, center, 7, 7, null)) {
				if (!projectile.isOnGround() && !projectiles.contains(projectile) && LocationUtil.isInCircle(center, projectile.getLocation(), 7)) {
					projectiles.add(projectile);
					projectile.setVelocity(projectile.getVelocity().multiply(0.1));
					new AbilityTimer(3) {
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
	}.setPeriod(TimeUnit.TICKS, 1).register();

	public AbsoluteZero(Participant participant) {
		super(participant);
	}

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			buff.start();
			passive.start();
		} else if (update == Update.ABILITY_DESTROY) {
			for (Entry<Block, IBlockSnapshot> entry : blockData.entrySet()) {
				Block key = entry.getKey();
				if (key.getType() == Material.PACKED_ICE || key.getType() == Material.OBSIDIAN || key.getType() == Material.SNOW_BLOCK || key.getType() == Material.SNOW) {
					entry.getValue().apply();
				}
			}
		}
	}

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT) {
			if (clickType == ClickType.LEFT_CLICK) {
				if (!cooldownTimer.isCooldown()) {
					FallingBlock fallingBlock = FallingBlocks.spawnFallingBlock(getPlayer().getEyeLocation(), Material.PACKED_ICE, true, getPlayer().getLocation().getDirection().multiply(1.7), new Behavior() {
						@Override
						public boolean onEntityChangeBlock(FallingBlock fallingBlock, EntityChangeBlockEvent event) {
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
					new AbilityTimer() {
						@Override
						protected void run(int count) {
							if (fallingBlock.isValid() && !fallingBlock.isDead()) {
								for (LivingEntity livingEntity : LocationUtil.getConflictingEntities(LivingEntity.class, fallingBlock.getWorld(), boundingBox, predicate)) {
									if (frozenEntities.add(livingEntity)) {
										Frost.apply(getGame(), livingEntity, TimeUnit.SECONDS, 2);
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
			} else if (clickType == ClickType.RIGHT_CLICK) {
				if (!yetiCooldownTimer.isCooldown()) {
					iceMaker.start();
					yetiCooldownTimer.start();
					for (LivingEntity entity : LocationUtil.getNearbyEntities(LivingEntity.class, getPlayer().getLocation(), range, range, predicate)) {
						if (entity instanceof Player && !getGame().isParticipating((Player) entity)) continue;
						Frost.apply(getGame(), entity, TimeUnit.SECONDS, 6);
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

}
