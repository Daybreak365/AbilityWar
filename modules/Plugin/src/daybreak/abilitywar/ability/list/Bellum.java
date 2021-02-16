package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.config.enums.CooldownDecrease;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.effect.Stun;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.module.Wreck;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.VectorUtil;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.library.BlockX;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import kotlin.ranges.RangesKt;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

@AbilityManifest(name = "벨리움", rank = Rank.S, species = Species.HUMAN, explain = {
		"§7패시브 §8- §e기력§f: 모든 능력이 쿨타임이 없는 대신, 기력을 소모합니다. 기력은",
		" 서서히 차오르며, 최대 6만큼 모을 수 있습니다. 기력 소모 후 3초간은 기력이",
		" 회복되지 않습니다.",
		"§7철괴 우클릭 §8- §c정면 돌파 §6(§e기력 2 소모§6)§f: 짧은 시간 동안 타게팅이 되지 않는",
		" 상태로 변하여 짧게 돌진합니다. 정면 돌파 중 가로막고 있는 벽을 만나면,",
		" 벽을 파괴하고 주변의 플레이어들을 기절시키며 피해를 줍니다.",
		"§7패시브 §8- §c일방적 구타 §6(§e기력 0.5 소모§6)§f: 기절 상태의 적에게 피해를 줄 때 피해량이",
		" $[BEAT_DAMAGE_CONFIG]배가 됩니다."
})
public class Bellum extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> BEAT_DAMAGE_CONFIG = abilitySettings.new SettingObject<Integer>(Bellum.class, "beat-damage", 25,
			"# 일방적 구타 추가 피해율",
			"# 25로 설정하면 25%의 추가 피해를 줍니다.") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

		@Override
		public String toString() {
			return String.valueOf((100 + getValue()) / 100.0);
		}
	};
	private static final ItemStack EMPTY = new ItemStack(Material.AIR);
	private final Energy energy = new Energy(360);
	private final double beatDamageModifier = (100 + BEAT_DAMAGE_CONFIG.getValue()) / 100.0;
	private final Predicate<Entity> predicate = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			if (entity.equals(getPlayer())) return false;
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
			return true;
		}
	};
	private Dash dash = null;

	public Bellum(Participant participant) {
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
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK) {
			if (dash != null) {
				getPlayer().sendMessage("§c이미 돌진 사용 중입니다.");
				return false;
			}
			if (energy.consumeEnergy(120)) {
				new Dash().start();
			} else {
				getPlayer().sendMessage("§c기력이 부족합니다.");
				return false;
			}
		}
		return false;
	}

	@SubscribeEvent
	private void onEntityDamageByEntity(final EntityDamageByEntityEvent e) {
		if (getPlayer().equals(getDamager(e.getDamager())) && predicate.test(e.getEntity())) {
			final Participant participant = getGame().getParticipant(e.getEntity().getUniqueId());
			if (participant.hasEffect(Stun.registration) && energy.consumeEnergy(30)) {
				e.setDamage(beatDamageModifier * e.getDamage());
				SoundLib.ENTITY_PLAYER_ATTACK_STRONG.playSound(getPlayer());
				SoundLib.ENTITY_PLAYER_ATTACK_CRIT.playSound(getPlayer());
			}
		}
	}

	public class Dash extends AbilityTimer {

		private Dash() {
			super(TaskType.REVERSE, 15);
			setPeriod(TimeUnit.TICKS, 1);
			Bellum.this.dash = this;
		}

		@Override
		protected void onStart() {
			getPlayer().setVelocity(getPlayer().getLocation().getDirection().setY(0).normalize().multiply(1.5));
			getParticipant().attributes().TARGETABLE.setValue(false);
		}

		@Override
		protected void run(int count) {
			if (getPlayer().isOnGround()) {
				getPlayer().setVelocity(getPlayer().getLocation().getDirection().setY(0).normalize().multiply(1));
			}
			getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 2, 2, false, false));
			final Location playerLocation = getPlayer().getLocation();
			final Vector direction = playerLocation.getDirection().setY(0).normalize();
			if (isBlockObstructing()) {
				final Set<Block> broke = new HashSet<>();
				for (double h = -2.5; h <= 2.5; h += 0.5) {
					for (int v = 0; v <= 3; v++) {
						final Location base = playerLocation.clone().add(VectorUtil.rotateAroundAxisY(new Vector(h, v, 0), -playerLocation.getYaw()));
						for (int i = 0; i <= 4; i++) {
							final Block block = base.clone().add(direction.clone().multiply(i)).getBlock();
							if (!block.isEmpty() && !BlockX.isIndestructible(block.getType()) && !block.isLiquid() && block.getType().isSolid() && broke.add(block)) {
								ParticleLib.BLOCK_CRACK.spawnParticle(block.getLocation(), .5, .5, .5, 10, block);
								final BlockBreakEvent event = new BlockBreakEvent(block, getPlayer());
								Bukkit.getPluginManager().callEvent(event);
								if (!event.isCancelled()) {
									block.breakNaturally(EMPTY);
								}
								for (Player player : LocationUtil.getNearbyEntities(Player.class, block.getLocation(), 2, 2, predicate)) {
									player.damage(10, getPlayer());
									Stun.apply(getGame().getParticipant(player), TimeUnit.TICKS, 50);
								}
							}
						}
					}
				}
				SoundLib.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR.playSound(getPlayer().getLocation());
				SoundLib.ENTITY_ZOMBIE_ATTACK_IRON_DOOR.playSound(getPlayer().getLocation());
				SoundLib.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR.playSound(getPlayer().getLocation());
				stop(false);
			}
		}

		private boolean isBlockObstructing() {
			final Location playerLocation = getPlayer().getLocation().clone();
			final Vector direction = playerLocation.getDirection().setY(0).normalize().multiply(.75);
			return isBlockObstructing(playerLocation, direction)
					|| isBlockObstructing(playerLocation, VectorUtil.rotateAroundAxisY(direction.clone(), 45))
					|| isBlockObstructing(playerLocation, VectorUtil.rotateAroundAxisY(direction.clone(), -45));
		}

		private boolean isBlockObstructing(Location location, Vector direction) {
			final Location front = location.clone().add(direction);
			final WorldBorder worldBorder = getPlayer().getWorld().getWorldBorder();
			return checkBlock(front.getBlock()) || checkBlock(front.clone().add(0, 1, 0).getBlock()) || (worldBorder.isInside(location) && !worldBorder.isInside(front));
		}

		private boolean checkBlock(final Block block) {
			return !block.isEmpty() && block.getType().isSolid();
		}

		@Override
		protected void onEnd() {
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			Bellum.this.dash = null;
			getParticipant().attributes().TARGETABLE.setValue(true);
		}
	}

	public class Energy extends AbilityTimer implements Listener {

		private final int maxEnergy;
		private final BossBar bossBar;
		private int noRestore = 0;
		private int energy = 0;

		private Energy(final int maxEnergy) {
			super();
			this.maxEnergy = maxEnergy;
			this.bossBar = Bukkit.createBossBar("기력", BarColor.YELLOW, BarStyle.SEGMENTED_6);
			setPeriod(TimeUnit.TICKS, 1);
			setBehavior(RestrictionBehavior.PAUSE_RESUME);
			start();
		}

		private boolean consumeEnergy(int amount) {
			final int current = this.energy;
			if (current >= amount) {
				this.energy = current - amount;
				this.noRestore = 60;
				return true;
			} else return false;
		}

		@Override
		protected void onStart() {
			bossBar.setProgress(0);
			bossBar.addPlayer(getPlayer());
			if (ServerVersion.getVersion() >= 10) bossBar.setVisible(true);
			Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		}

		@EventHandler
		private void onPlayerJoin(final PlayerJoinEvent e) {
			if (getPlayer().getUniqueId().equals(e.getPlayer().getUniqueId()) && isRunning()) {
				bossBar.addPlayer(e.getPlayer());
			}
		}

		@EventHandler
		private void onPlayerQuit(final PlayerQuitEvent e) {
			if (getPlayer().getUniqueId().equals(e.getPlayer().getUniqueId())) {
				bossBar.removePlayer(e.getPlayer());
			}
		}

		@Override
		protected void run(int count) {
			if (energy < maxEnergy) {
				if (noRestore <= 0) {
					energy++;
				} else {
					noRestore--;
				}
				if (Wreck.isEnabled(getGame()) && Settings.getCooldownDecrease() == CooldownDecrease._100) {
					energy = maxEnergy;
				}
			}
			bossBar.setProgress(RangesKt.coerceIn(energy / (double) maxEnergy, 0, 1));
		}

		@Override
		protected void onPause() {
			bossBar.removeAll();
		}

		@Override
		protected void onResume() {
			bossBar.addPlayer(getPlayer());
		}

		@Override
		protected void onEnd() {
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			bossBar.removeAll();
			HandlerList.unregisterAll(this);
		}

	}

}
