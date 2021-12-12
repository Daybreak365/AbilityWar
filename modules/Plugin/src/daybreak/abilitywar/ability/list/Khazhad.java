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
import daybreak.abilitywar.game.manager.effect.Frost;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.minecraft.FallingBlocks;
import daybreak.abilitywar.utils.base.minecraft.FallingBlocks.Behavior;
import daybreak.abilitywar.utils.base.minecraft.boundary.BoundingBox;
import daybreak.abilitywar.utils.base.minecraft.boundary.EntityBoundingBox;
import daybreak.abilitywar.utils.base.minecraft.damage.Damages;
import daybreak.abilitywar.utils.base.minecraft.entity.decorator.Deflectable;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

@AbilityManifest(name = "카쟈드", rank = Rank.A, species = Species.GOD, explain = {
		"§7철괴 좌클릭 §8- §b얼음§f: 자신이 보고 있는 방향으로 §b얼음§f을 날립니다. $[COOLDOWN_CONFIG]",
		" §b얼음§f에 맞은 생명체는 2초간 §5빙결§f되며, 무적 상태가 됩니다. 얼어붙은 얼음이",
		" 바닥에 닿으면 고정됩니다. 웅크린채로 철괴를 좌클릭하면 날아가고 있는 모든",
		" 얼음을 그 자리에서 고정합니다. 고정된 얼음은 4초 뒤 깨지며 대미지를 줍니다.",
		"§7패시브 §8- §b냉기§f: 주변을 지나가는 발사체들이 모두 얼어붙습니다.",
})
public class Khazhad extends AbilityBase implements ActiveHandler {

	private class BlockHandler extends BukkitRunnable {

		private final FallingBlock fallingBlock;

		protected BlockHandler(final FallingBlock fallingBlock) {
			this.fallingBlock = fallingBlock;
		}

		@Override
		public void run() {
			if (!fallingBlock.isValid()) {
				cancel();
				blockhandlers.remove(fallingBlock.getUniqueId());
			}
		}

		protected void ice() {
			cancel();
			final Block block = fallingBlock.getLocation().getBlock();
			for (int x = -1; x < 1; x++) {
				for (int y = -1; y < 1; y++) {
					for (int z = -1; z < 1; z++) {
						final Block relative = block.getRelative(x, y, z);
						relative.setType(Material.PACKED_ICE);
					}
				}
			}
			new AbilityTimer(4) {
				@Override
				protected void onEnd() {
					for (int x = -1; x < 1; x++) {
						for (int y = -1; y < 1; y++) {
							for (int z = -1; z < 1; z++) {
								final Block relative = block.getRelative(x, y, z);
								if (relative.getType() == Material.PACKED_ICE) {
									SoundLib.BLOCK_GLASS_BREAK.playSound(relative.getLocation());
									ParticleLib.BLOCK_CRACK.spawnParticle(relative.getLocation(), 0, 0, 0, 1, 0.001, relative);
									for (Player nearby : LocationUtil.getNearbyEntities(Player.class, relative.getLocation(), 4, 4, predicate)) {
										Damages.damageMagic(nearby, getPlayer(), false, 7.5f);
									}
								}
								relative.setType(Material.AIR);
							}
						}
					}
				}
			}.start();
		}

	}

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

	private final Map<UUID, BlockHandler> blockhandlers = new HashMap<UUID, BlockHandler>();
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

	private final Set<Projectile> projectiles = new HashSet<Projectile>() {
		@Override
		public boolean add(Projectile projectile) {
			if (size() >= 15) clear();
			return super.add(projectile);
		}
	};

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.LEFT_CLICK) {
			if (getPlayer().isSneaking()) {
				if (!blockhandlers.isEmpty()) {
					for (Iterator<Entry<UUID, BlockHandler>> it = blockhandlers.entrySet().iterator(); it.hasNext(); ) {
						Map.Entry<UUID, BlockHandler> entry = it.next();
						it.remove();
						entry.getValue().ice();
						entry.getValue().fallingBlock.remove();
					}
					getPlayer().sendMessage("§b날아가고 있는 얼음§f이 모두 얼어붙었습니다.");
					return false;
				}
			}
			if (cooldownTimer.isCooldown()) return false;
			final Location location = getPlayer().getEyeLocation();
			final World world = location.getWorld();
			if (world == null) return false;
			final FallingBlock fallingBlock;
			if (ServerVersion.getVersion() >= 13)
				fallingBlock = location.getWorld().spawnFallingBlock(location, Material.PACKED_ICE.createBlockData());
			else fallingBlock = location.getWorld().spawnFallingBlock(location, Material.PACKED_ICE, (byte) 0);
			final BlockHandler handler = new BlockHandler(fallingBlock);
			blockhandlers.put(fallingBlock.getUniqueId(), handler);
			handler.runTaskTimer(AbilityWar.getPlugin(), 0L, 200L);

			fallingBlock.setVelocity(getPlayer().getLocation().getDirection().multiply(1.7));

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
									Frost.apply(getGame(), livingEntity, TimeUnit.SECONDS, 2);
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
							Frost.apply(getGame(), livingEntity, TimeUnit.SECONDS, 2);
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
	private void onEntityChangeBlock(final EntityChangeBlockEvent e) {
		if (!(e.getEntity() instanceof FallingBlock)) return;
		final FallingBlock fallingBlock = (FallingBlock) e.getEntity();
		final BlockHandler handler = blockhandlers.remove(fallingBlock.getUniqueId());
		if (handler != null) {
			handler.ice();
		}
	}

	@SubscribeEvent
	private void onProjectileHit(ProjectileHitEvent e) {
		projectiles.remove(e.getEntity());
	}

}
