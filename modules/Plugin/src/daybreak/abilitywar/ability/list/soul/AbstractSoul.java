package daybreak.abilitywar.ability.list.soul;

import com.google.common.collect.ImmutableSet;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.ability.event.AbilityCooldownResetEvent;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.manager.effect.Fear;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.module.Wreck;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.VectorUtil;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.base.minecraft.damage.Damages;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.library.BlockX;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import kotlin.ranges.RangesKt;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@AbilityManifest(name = "소울", rank = Rank.S, species = Species.HUMAN, explain = {
		"§7패시브 §8- §b어둑서니§f: 적의 두려움을 먹고 힘을 키웁니다. 두려움은 최대 6까지",
		" 모을 수 있으며, 서서히 사라집니다. 두려움을 먹은 후 15초간은 두려움이",
		" 사라지지 않습니다.",
		"§7검 우클릭 §8- §b오싹한 힘§f: 15칸 이내의 바라보고 있는 대상에게 사용합니다.",
		" 최대 체력의 20%에 해당하는 피해를 입히며, 대상의 §b두려움§f을 먹습니다.",
		" 이후 1초간, 대상이 나를 바라보고 있다면 대상을 §5밀어§f내고, 등지고 있다면",
		" §5당겨§f옵니다. $[GRASP_COOLDOWN]",
		"§7철괴 우클릭 §8- §b공포 그 자체 §8(§7두려움 4 소모§8)§f: 주변 10칸 이내의 모든 적을 3초간",
		" §5공포§f에 빠뜨리고, 가장 가까운 적을 따라가는 유령 여섯을 소환합니다.",
		" 유령은 4초 뒤 사라지면서 주변에 각각 1.5의 고정 피해를 입힙니다.",
		"§7더블 점프 §8- §b유령화 §8(§7두려움 1 소모§8)§f: 3초간 타게팅 불가능한 유령 상태로 변하여",
		" 앞으로 돌진합니다. 지속시간이 끝날 때, 주변 6칸 이내의 적들을 1.5초간 §5공포§f에",
		" 빠뜨립니다.",
		"§7패시브 §8- §b유령의 몸§f: 낙하 피해를 입지 않습니다."
})
public abstract class AbstractSoul extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> GRASP_COOLDOWN = abilitySettings.new SettingObject<Integer>(AbstractSoul.class, "grasp-cooldown", 8,
			"# 오싹한 힘 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};

	public static final SettingObject<Integer> GHOST_FORM_COOLDOWN = abilitySettings.new SettingObject<Integer>(AbstractSoul.class, "ghost-form-cooldown", 2,
			"# 유령화 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};

	private static final Note[][] NOTES = {
			{
					Note.natural(0, Tone.A),
					Note.natural(0, Tone.C),
					Note.natural(0, Tone.E)
			},
			{
					Note.flat(0, Tone.B),
					Note.natural(0, Tone.D),
					Note.natural(0, Tone.F)
			}
	};
	private static final RGB SPIRIT = RGB.of(80, 196, 217);
	private static final Set<Material> swords;
	private static final Circle circle = Circle.of(0.5, 20);

	static {
		if (MaterialX.NETHERITE_SWORD.isSupported()) {
			swords = ImmutableSet.of(MaterialX.WOODEN_SWORD.getMaterial(), Material.STONE_SWORD, Material.IRON_SWORD, MaterialX.GOLDEN_SWORD.getMaterial(), Material.DIAMOND_SWORD, MaterialX.NETHERITE_SWORD.getMaterial());
		} else {
			swords = ImmutableSet.of(MaterialX.WOODEN_SWORD.getMaterial(), Material.STONE_SWORD, Material.IRON_SWORD, MaterialX.GOLDEN_SWORD.getMaterial(), Material.DIAMOND_SWORD);
		}
	}

	private final Predicate<Entity> predicate = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			if (entity.equals(getPlayer())) return false;
			if (!(entity instanceof Player)) return false;
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

	public class Souls extends AbilityTimer implements Listener {

		private final int maxSoul;
		private final BossBar bossBar;
		private int noDecrease = 0;
		private int souls = 0;
		private int sound = 0;

		private Souls(final int maxSoul) {
			super();
			this.maxSoul = maxSoul;
			this.bossBar = Bukkit.createBossBar("두려움", BarColor.BLUE, BarStyle.SEGMENTED_6);
			setPeriod(TimeUnit.TICKS, 1);
			setBehavior(RestrictionBehavior.PAUSE_RESUME);
		}

		private boolean consume(int amount) {
			final int current = this.souls;
			if (current >= amount) {
				this.souls = current - amount;
				return true;
			} else return false;
		}

		private void collect(int amount) {
			this.souls = Math.min(maxSoul, this.souls + amount);
			this.noDecrease = 400;
			updateBossbar();

			final int sound = this.sound;
			new AbilityTimer(5) {
				@Override
				protected void run(int count) {
					for (Note note : NOTES[sound % 2]) {
						SoundLib.FLUTE.playInstrument(getPlayer(), note);
					}
				}
			}.setPeriod(TimeUnit.TICKS, 1).start();
			this.sound++;

			new AbilityTimer(TaskType.NORMAL, 30) {
				@Override
				protected void run(int count) {
					for (int i = 0; i < 2; i++) {
						ParticleLib.REDSTONE.spawnParticle(getPlayer().getLocation().clone().add(circle.get((count - 1) % 20)).add(0, ((double) count / getMaximumCount()) * 2, 0), SPIRIT);
					}
				}
			}.setPeriod(TimeUnit.TICKS, 1).start();
		}

		private void updateBossbar() {
			bossBar.setProgress(RangesKt.coerceIn(souls / (double) maxSoul, 0, 1));
		}

		public int getSouls() {
			return souls;
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
			if (souls > 0) {
				if (noDecrease <= 0) {
					souls--;
				} else {
					noDecrease--;
				}
			}
			updateBossbar();
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
	private class Vindictive extends AbilityTimer {

		private final Ghost ghost;
		private final float damage;

		private Vindictive(final int maxCount, final Location location, final Color color, final double damage) {
			super(maxCount);
			setPeriod(TimeUnit.TICKS, 2);
			this.ghost = createGhost(location, color, true);
			this.damage = (float) damage;
		}

		@Override
		protected void onStart() {
			final Location loc = ghost.getLocation().add(0, 1, 0);
			for (float pitch = 1f; pitch <= 2f; pitch += .25f) {
				SoundLib.ENTITY_VEX_AMBIENT.playSound(loc, .45f, pitch);
			}
			new AbilityTimer(5) {
				@Override
				protected void run(int count) {
					for (Note note : NOTES[0]) {
						SoundLib.FLUTE.playInstrument(loc, note);
					}
				}
			}.setPeriod(TimeUnit.TICKS, 1).start();
			for (int i = 0; i < 30; i++) {
				ParticleLib.SMOKE_LARGE.spawnParticle(getPlayer().getLocation(), .5, .5, .5, 10, .15);
			}
			ParticleLib.CLOUD.spawnParticle(loc, .3f, .3f, .3f, 30, 0);
		}

		@Override
		protected void run(int count) {
			ParticleLib.CLOUD.spawnParticle(ghost.getLocation().add(0, 1, 0), .3f, .3f, .3f, 5, 0);
		}

		@Override
		protected void onEnd() {
			final Location loc = ghost.getLocation().add(0, 1, 0);
			for (float pitch = 1f; pitch <= 2f; pitch += .25f) {
				SoundLib.ENTITY_VEX_AMBIENT.playSound(loc, .45f, pitch);
			}
			for (int i = 0; i < 3; i++) {
				SoundLib.ENTITY_GHAST_HURT.playSound(loc, .45f, 1.4f);
			}
			ParticleLib.CLOUD.spawnParticle(loc, .3f, .3f, .3f, 30, 0);
			for (Player nearby : LocationUtil.getNearbyEntities(Player.class, loc, 3, 3, predicate)) {
				nearby.setNoDamageTicks(0);
				Damages.damageFixed(nearby, getPlayer(), damage);
			}
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			ghost.remove();
		}
	}

	private final Cooldown cooldown = new Cooldown(GRASP_COOLDOWN.getValue(), "오싹한 힘", 20);
	private final Charge charge = new Charge();
	private final Souls souls = new Souls(360);
	private GhostForm ghostForm = null;

	public AbstractSoul(Participant participant) {
		super(participant);
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onToggleFlight(PlayerToggleFlightEvent e) {
		if (this.ghostForm == null && charge.canUse() && souls.getSouls() >= 60) {
			Block lastEmpty = null;
			try {
				for (BlockIterator iterator = new BlockIterator(getPlayer().getWorld(), getPlayer().getLocation().toVector(), getPlayer().getLocation().getDirection(), 1, 13); iterator.hasNext(); ) {
					final Block block = iterator.next();
					if (BlockX.isIndestructible(block.getType())) break;
					if (!block.getType().isSolid()) {
						lastEmpty = block;
					}
				}
			} catch (IllegalStateException ignored) {}
			if (lastEmpty != null) {
				charge.use();
				souls.consume(60);
				e.setCancelled(true);
				new GhostForm(lastEmpty.getLocation()).start();
			} else {
				getPlayer().sendMessage("§7바라보는 방향§f에 §b이동할 수 있는 곳§f이 없습니다.");
			}
		}
	}

	protected abstract Ghost createGhost(Location location, boolean follow);
	protected abstract Ghost createGhost(Location location, Color color, boolean follow);

	@Override
	public boolean usesMaterial(Material material) {
		return super.usesMaterial(material) || swords.contains(material);
	}

	@Override
	public boolean ActiveSkill(Material material, ClickType clickType) {
		if (clickType == ClickType.RIGHT_CLICK) {
			if (swords.contains(material)) {
				if (cooldown.isCooldown()) return false;
				final Player target = LocationUtil.getEntityLookingAt(Player.class, getPlayer(), 15, predicate);
				if (target == null) {
					getPlayer().sendMessage("§7바라보는 방향§f에 §b대상§f이 없습니다.");
					return false;
				}
				souls.collect(60);
				if (LocationUtil.isBehind(target, getPlayer())) {
					new Grasp(target, target, getPlayer()).start();
				} else {
					new Grasp(target, getPlayer(), target).start();
				}
				cooldown.start();
			} else if (material == Material.IRON_INGOT) {
				if (souls.getSouls() >= 240) {
					final List<Player> nearbyEntities = LocationUtil.getNearbyEntities(Player.class, getPlayer().getLocation(), 6, 6, predicate);
					if (!nearbyEntities.isEmpty()) {
						souls.consume(240);
						for (Player nearby : nearbyEntities) {
							final Participant participant = getGame().getParticipant(nearby);
							if (participant != null) {
								Fear.apply(participant, TimeUnit.SECONDS, 3, getPlayer());
							}
						}
						SoundLib.ENTITY_WITHER_SPAWN.playSound(getPlayer().getLocation());
						for (int i = 0; i < 6; i++) {
							new Vindictive(40, LocationUtil.floorY(LocationUtil.getRandomLocation(getPlayer().getLocation(), 5)), Color.RED, 1.5f).start();
						}
					} else getPlayer().sendMessage("§7주위§f에 §b대상§f이 없습니다.");
				} else getPlayer().sendMessage("§b두려움§f이 부족합니다.");
			}
		}
		return false;
	}

	@SubscribeEvent(onlyRelevant = true)
	protected void onAbilityCooldownReset(AbilityCooldownResetEvent e) {
		charge.setCount(charge.cooldown);
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onEntityDamage(final EntityDamageEvent e) {
		if (e.getCause() == DamageCause.FALL) {
			e.setCancelled(true);
		}
	}

	private class GhostForm extends AbilityTimer implements Listener {

		private final GameMode gameMode;
		private final float flySpeed;
		private final Location targetLocation;

		private GhostForm(Location targetLocation) {
			super(TaskType.NORMAL, 60);
			if (AbstractSoul.this.ghostForm != null) throw new IllegalStateException();
			this.targetLocation = targetLocation;
			setPeriod(TimeUnit.TICKS, 1);
			GameMode mode = getPlayer().getGameMode();
			if (mode == GameMode.SPECTATOR) mode = GameMode.SURVIVAL;
			this.gameMode = mode;
			this.flySpeed = getPlayer().getFlySpeed();
			AbstractSoul.this.ghostForm = this;
		}

		@Override
		protected void onStart() {
			AbstractSoul.this.charge.pause();
			Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
			getParticipant().attributes().TARGETABLE.setValue(false);
			getPlayer().setGameMode(GameMode.SPECTATOR);
			final Location loc = getPlayer().getLocation();
			for (float pitch = 1f; pitch <= 2f; pitch += .25f) {
				SoundLib.ENTITY_VEX_AMBIENT.playSound(loc, .45f, pitch);
			}
			ParticleLib.CLOUD.spawnParticle(loc.add(0, 1, 0), .3, .3, .3, 30, 0);
		}

		@EventHandler
		private void onTeleport(PlayerTeleportEvent e) {
			if (getPlayer().equals(e.getPlayer())) {
				if (e.getCause() == TeleportCause.SPECTATE) e.setCancelled(true);
			}
		}

		@Override
		protected void run(int count) {
			ParticleLib.CLOUD.spawnParticle(getPlayer().getLocation().add(0, 1, 0), .3, .3, .3, 3, 0);
			getPlayer().setFlySpeed(0f);
			getPlayer().setGameMode(GameMode.SPECTATOR);
			getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 2, 100, false, false));
			final Location playerLocation = getPlayer().getLocation();
			getPlayer().setVelocity(VectorUtil.validateVector(targetLocation.toVector().subtract(playerLocation.toVector()).multiply(0.25)));
			if (playerLocation.distanceSquared(targetLocation) < 1) {
				stop(false);
			}
		}

		@Override
		protected void onEnd() {
			getPlayer().teleport(targetLocation.setDirection(getPlayer().getLocation().getDirection()));
			final Location loc = getPlayer().getLocation();
			for (float pitch = 1f; pitch <= 2f; pitch += .25f) {
				SoundLib.ENTITY_VEX_AMBIENT.playSound(loc, .45f, pitch);
			}
			ParticleLib.CLOUD.spawnParticle(loc.add(0, 1, 0), .3, .3, .3, 30, 0);
			AbstractSoul.this.charge.resume();
			onSilentEnd();
			final Block block = getPlayer().getLocation().getBlock();
			if (block.getType().isSolid() && block.getRelative(BlockFace.UP).getType().isSolid()) {
				getPlayer().teleport(LocationUtil.floorY(getPlayer().getLocation()));
			}
			SoundLib.ENTITY_WITHER_SPAWN.playSound(getPlayer().getLocation());
			for (Player nearby : LocationUtil.getNearbyEntities(Player.class, getPlayer().getLocation(), 7, 7, predicate)) {
				final Participant participant = getGame().getParticipant(nearby);
				if (participant != null) {
					Fear.apply(participant, TimeUnit.TICKS, 30, getPlayer());
				}
			}
		}

		@Override
		protected void onSilentEnd() {
			HandlerList.unregisterAll(this);
			getPlayer().setGameMode(gameMode);
			getPlayer().setAllowFlight(gameMode != GameMode.SURVIVAL && gameMode != GameMode.ADVENTURE);
			getPlayer().setVelocity(new Vector());
			getPlayer().setFlySpeed(flySpeed);
			getPlayer().setFlying(false);
			getParticipant().attributes().TARGETABLE.setValue(true);
			NMS.setInvisible(getPlayer(), false);
			AbstractSoul.this.ghostForm = null;
		}

	}

	private class Charge extends AbilityTimer {

		private final int cooldown = Math.max((int) (GHOST_FORM_COOLDOWN.getValue() * Wreck.calculateDecreasedAmount(0)), 1);
		private final ActionbarChannel actionbarChannel = newActionbarChannel();
		private boolean ready = true;

		private Charge() {
			super();
			setBehavior(RestrictionBehavior.PAUSE_RESUME);
		}

		@Override
		protected void onStart() {
			setCount(cooldown + 1);
		}

		private boolean canUse() {
			return ready;
		}

		private void use() {
			if (ready) {
				this.ready = false;
				final GameMode mode = getPlayer().getGameMode();
				getPlayer().setAllowFlight(mode != GameMode.SURVIVAL && mode != GameMode.ADVENTURE);
				setCount(0);
				updateActionbar();
			}
		}

		@Override
		protected void onCountSet() {
			if (getCount() < cooldown) {
				actionbarChannel.update("§c유령화 쿨타임 §f: §6" + (cooldown - getCount()) + "초");
			} else if (getCount() == cooldown) {
				actionbarChannel.update(null);
				if (souls.getSouls() >= 60) {
					final GameMode mode = getPlayer().getGameMode();
					getPlayer().setAllowFlight((mode != GameMode.SURVIVAL && mode != GameMode.ADVENTURE) || souls.getSouls() >= 60);
					getPlayer().setFlying(false);
				}
				this.ready = true;
			}
		}

		@Override
		protected void run(int count) {
			if (count < cooldown) {
				actionbarChannel.update("§c유령화 쿨타임 §f: §6" + (cooldown - count) + "초");
				return;
			} else if (count == cooldown) {
				actionbarChannel.update(null);
				if (souls.getSouls() >= 60) {
					final GameMode mode = getPlayer().getGameMode();
					getPlayer().setAllowFlight((mode != GameMode.SURVIVAL && mode != GameMode.ADVENTURE) || souls.getSouls() >= 60);
					getPlayer().setFlying(false);
				}
				this.ready = true;
				return;
			}
			final GameMode mode = getPlayer().getGameMode();
			getPlayer().setAllowFlight((mode != GameMode.SURVIVAL && mode != GameMode.ADVENTURE) || souls.getSouls() >= 60);
		}

		@Override
		protected void onSilentEnd() {
			actionbarChannel.update(null);
		}

		private void updateActionbar() {
			if (isPaused()) {
				actionbarChannel.update(null);
				return;
			}
			final int count = getCount();
			if (count < cooldown) {
				actionbarChannel.update("§c유령화 쿨타임 §f: §6" + (cooldown - count) + "초");
			} else {
				actionbarChannel.update(null);
			}
		}
	}

	private class Grasp extends AbilityTimer {

		private final Player target, from, to;
		private final Ghost ghost;

		private Grasp(final Player target, final Player from, final Player to) {
			super(TaskType.NORMAL, 5);
			setPeriod(TimeUnit.TICKS, 4);
			this.target = target;
			this.ghost = createGhost(from.getLocation(), Color.AQUA, false);
			this.from = from;
			this.to = to;
		}

		@Override
		protected void onStart() {
			final Location loc = ghost.getLocation();
			for (float pitch = 1f; pitch <= 2f; pitch += .25f) {
				SoundLib.ENTITY_VEX_AMBIENT.playSound(loc, .45f, pitch);
			}
			ParticleLib.CLOUD.spawnParticle(ghost.getLocation().add(0, 1, 0), .3, .3, .3, 30, 0);
			Damages.damageMagic(target, getPlayer(), false, (float) (target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * .2));
		}

		@Override
		protected void run(int count) {
			target.setVelocity(VectorUtil.validateVector(to.getLocation().toVector().subtract(from.getLocation().toVector()).setY(0).normalize()).multiply(.6));
			ghost.move(to.getLocation(), 1f);
		}

		@Override
		protected void onEnd() {
			final Location loc = ghost.getLocation();
			for (float pitch = 1f; pitch <= 2f; pitch += .25f) {
				SoundLib.ENTITY_VEX_AMBIENT.playSound(loc, .45f, pitch);
			}
			ParticleLib.CLOUD.spawnParticle(ghost.getLocation().add(0, 1, 0), .3, .3, .3, 30, 0);
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			ghost.remove();
		}
	}

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			souls.start();
			if (!charge.isRunning()) {
				charge.start();
			}
			charge.updateActionbar();
		}
	}

}
