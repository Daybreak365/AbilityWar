package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.config.enums.CooldownDecrease;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.minecraft.boundary.BoundingBox;
import daybreak.abilitywar.utils.base.minecraft.boundary.EntityBoundingBox;
import daybreak.abilitywar.utils.base.minecraft.FallingBlocks;
import daybreak.abilitywar.utils.base.minecraft.FallingBlocks.Behavior;
import daybreak.abilitywar.utils.base.minecraft.block.Blocks;
import daybreak.abilitywar.utils.base.minecraft.block.IBlockSnapshot;
import daybreak.abilitywar.utils.base.minecraft.entity.decorator.Deflectable;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

@AbilityManifest(name = "카쟈드", rank = Rank.A, species = Species.GOD, explain = {
		"철괴를 좌클릭하면 자신이 보고 있는 방향으로 §b얼음§f을 날립니다. $[COOLDOWN_CONFIG]",
		"§b얼음§f에 맞은 생명체는 2초간 얼어붙으며, 대미지를 입지 않습니다.",
		"주변을 지나가는 발사체들이 모두 얼어붙어 바닥으로 떨어집니다."
})
public class Khazhad extends AbilityBase implements ActiveHandler {

	private static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Khazhad.class, "cooldown", 10, "# 좌클릭 쿨타임") {

		@Override
		public boolean condition(Integer arg0) {
			return arg0 >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};

	public Khazhad(Participant participant) {
		super(participant);
	}

	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue(), CooldownDecrease._25);
	private final Predicate<Entity> predicate = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
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

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			passive.start();
		}
	}

	private static final Set<LivingEntity> frozenEntities = new HashSet<>();

	public class Frost extends AbilityTimer implements Listener {

		private final LivingEntity target;
		private final Block[] blocks = new Block[2];
		private final IBlockSnapshot[] snapshots = new IBlockSnapshot[2];
		private final Location teleport;
		private ActionbarChannel actionbarChannel;

		private Frost(LivingEntity target) {
			super(TaskType.REVERSE, 40);
			setPeriod(TimeUnit.TICKS, 1);
			this.target = target;
			blocks[0] = target.getEyeLocation().getBlock();
			blocks[1] = blocks[0].getRelative(BlockFace.DOWN);
			if (ServerVersion.getVersion() >= 10) target.setInvulnerable(true);
			for (int i = 0; i < 2; i++) {
				snapshots[i] = Blocks.createSnapshot(blocks[i]);
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
			if (ServerVersion.getVersion() >= 10) target.setInvulnerable(false);
			for (int i = 0; i < 2; i++) {
				snapshots[i].apply();
			}
			if (actionbarChannel != null) actionbarChannel.unregister();
			frozenEntities.remove(target);
		}

		@Override
		protected void onSilentEnd() {
			HandlerList.unregisterAll(this);
			if (ServerVersion.getVersion() >= 10) target.setInvulnerable(false);
			for (int i = 0; i < 2; i++) {
				snapshots[i].apply();
			}
			if (actionbarChannel != null) actionbarChannel.unregister();
			frozenEntities.remove(target);
		}

	}

	private final Set<Projectile> projectiles = new HashSet<Projectile>() {
		@Override
		public boolean add(Projectile projectile) {
			if (size() >= 15) clear();
			return super.add(projectile);
		}
	};

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.LEFT_CLICK && !cooldownTimer.isCooldown()) {
			final FallingBlock fallingBlock = FallingBlocks.spawnFallingBlock(getPlayer().getEyeLocation(), Material.PACKED_ICE, true, getPlayer().getLocation().getDirection().multiply(1.7), new Behavior() {
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
			fallingBlock.setMetadata("deflectable", new FixedMetadataValue(AbilityWar.getPlugin(), new Deflectable() {
				@Override
				public Vector getDirection() {
					return fallingBlock.getVelocity();
				}

				@Override
				public Location getLocation() {
					return fallingBlock.getLocation();
				}

				@Override
				public void onDeflect(Participant deflector, Vector newDirection) {
					fallingBlock.remove();
					final Player player = deflector.getPlayer();
					final FallingBlock newBlock = FallingBlocks.spawnFallingBlock(fallingBlock.getLocation(), Material.PACKED_ICE, true, newDirection, new Behavior() {
						@Override
						public boolean onEntityChangeBlock(FallingBlock fallingBlock, EntityChangeBlockEvent event) {
							final Block block = fallingBlock.getLocation().getBlock();
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
					final BoundingBox boundingBox = EntityBoundingBox.of(newBlock).expand(.5, .5, .5, .5, .5, .5);
					new AbilityTimer() {
						@Override
						protected void run(int count) {
							if (newBlock.isValid() && !newBlock.isDead()) {
								for (LivingEntity livingEntity : LocationUtil.getConflictingEntities(LivingEntity.class, newBlock.getWorld(), boundingBox, predicate)) {
									if (livingEntity.equals(player)) continue;
									if (frozenEntities.add(livingEntity)) {
										new Frost(livingEntity).start();
									}
								}
							} else {
								stop(false);
							}
						}
					}.setPeriod(TimeUnit.TICKS, 1).start();
				}

				@Override
				public ProjectileSource getShooter() {
					return getPlayer();
				}
			}));
			final BoundingBox boundingBox = EntityBoundingBox.of(fallingBlock).expand(.5, .5, .5, .5, .5, .5);
			new AbilityTimer() {
				@Override
				protected void run(int count) {
					if (fallingBlock.isValid() && !fallingBlock.isDead()) {
						for (LivingEntity livingEntity : LocationUtil.getConflictingEntities(LivingEntity.class, fallingBlock.getWorld(), boundingBox, predicate)) {
							if (livingEntity.equals(getPlayer())) continue;
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
		return false;
	}

	@SubscribeEvent
	private void onProjectileHit(ProjectileHitEvent e) {
		projectiles.remove(e.getEntity());
	}

}
