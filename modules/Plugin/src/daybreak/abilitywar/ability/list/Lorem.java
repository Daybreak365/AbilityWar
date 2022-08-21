package daybreak.abilitywar.ability.list;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.Tips;
import daybreak.abilitywar.ability.Tips.Description;
import daybreak.abilitywar.ability.Tips.Difficulty;
import daybreak.abilitywar.ability.Tips.Level;
import daybreak.abilitywar.ability.Tips.Stats;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.CustomEntity;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.VectorUtil;
import daybreak.abilitywar.utils.base.minecraft.damage.Damages;
import daybreak.abilitywar.utils.base.minecraft.entity.decorator.Deflectable;
import daybreak.abilitywar.utils.base.minecraft.nms.IHologram;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.library.item.EnchantLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

@AbilityManifest(name = "로렘", rank = Rank.S, species = Species.HUMAN, explain = {
		"기본적으로 근접 공격을 할 수 없습니다. 검을 휘두르면 바라보는 방향으로",
		"검기를 발사합니다. 검기를 생명체에 적중시킨 경우 대미지를 주고 움직이던",
		"방향으로 도약하며, 한 생명체에 검기를 네 번 적중시킬 때마다 강력한 대미지를",
		"줍니다. 대상이 5칸 이내에 있는 경우, 검기의 대미지가 약해집니다. 검기를",
		"빗맞춘 경우, 2초간 탈진 상태에 빠지며 탈진 중에는 움직임이 둔해지고 공격이",
		"불가능해집니다."
}, summarize = {
		"§c근접 공격 불가§f. 그 대신 §3검을 휘두르면§f 검기를 발사합니다. 검기 적중 시",
		"최근 이동 방향으로 돌진합니다. 대상당 §c4회 적중§f마다 §4강력한 피해§f를 입힙니다.",
		"검기는 적중 대상과 가까울 경우 피해량이 감소합니다.",
		"검기를 빗맞히면 잠시간 이동력 대폭 감소 및 검기 스킬이 사라집니다."
})
@Tips(tip = {
		"카이팅이 이 능력의 핵심입니다. 검기는 가까운 곳에서는 대미지가 약해지기",
		"때문에 상대방과의 거리를 잘 유지해야 하는데, 이를 돕는 것이 검기 적중 시",
		"도약입니다. 이를 잘 이용하면 다가오는 적을 피해 공격을 하거나, 도망가는 적을",
		"따라가며 공격을 하는 등 전투에서 우위를 점할 수 있습니다. 하지만 검기를",
		"빗맞출 경우 엄청난 리스크가 뒤따르기 때문에, 상대방의 다음 움직임을 예측하고",
		"검기를 정확하게 맞추는 것이 굉장히 중요합니다."
}, strong = {
		@Description(subject = "상황 벗어나기", explain = {
				"도약 패시브를 이용해 위험한 상황으로부터 쉽게 벗어날 수 있습니다."
		}),
		@Description(subject = "쫓아가기", explain = {
				"도약은 도망가는 상대를 쫓아가기에도 좋습니다. 상대의 이동 속도가 빠르거나,",
				"주위에 검기를 막을 수 있는 장애물이 많지만 않다면 말이죠."
		}),
		@Description(subject = "강력한 공격", explain = {
				"네 번째 공격마다 일반 공격의 3배에 해당하는 대미지를 넣을 수 있습니다.",
				"스택을 잘 활용하여 효율적인 전투를 펼치세요."
		})
}, weak = {
		@Description(subject = "근접전", explain = {
				"기본적으로 근접 공격을 할 수 없고, 근접에서는 능력이 약해지기 때문에",
				"근접전에 매우 취약합니다. 상대방과 가까운 거리에 있다면, 능력을 이용해",
				"거리를 벌리세요."
		}),
		@Description(subject = "빠른 상대", explain = {
				"공격을 하기 위해서는 상대의 다음 이동을 예측해야 합니다. 하지만 이동이",
				"빠른 상대는 예측이 쉽지 않을 뿐더러, 검기를 발사하더라도 상대가 피할 수",
				"있습니다. 이동이 빠른 상대와의 전투는 최대한 피하세요."
		}),
		@Description(subject = "좁은 공간", explain = {
				"좁은 공간에서는 이동이 어렵고, §b근접전§f이 일어나기 쉽기 때문에 불리해집니다.",
				"최대한 넓은 공간을 찾으세요. 바닥에 평평하면 더 좋습니다."
		})
}, stats = @Stats(offense = Level.SIX, survival = Level.FIVE, crowdControl = Level.ZERO, mobility = Level.SIX, utility = Level.ZERO), difficulty = Difficulty.HARD)
public class Lorem extends AbilityBase {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Lorem.class, "cooldown", 10,
			"# 스킬 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}
	};

	private static final RGB COLOUR = RGB.of(50, 129, 168);
	private static final Set<Material> swords;

	static {
		if (MaterialX.NETHERITE_SWORD.isSupported()) {
			swords = ImmutableSet.of(MaterialX.WOODEN_SWORD.getMaterial(), Material.STONE_SWORD, Material.IRON_SWORD, MaterialX.GOLDEN_SWORD.getMaterial(), Material.DIAMOND_SWORD, MaterialX.NETHERITE_SWORD.getMaterial());
		} else {
			swords = ImmutableSet.of(MaterialX.WOODEN_SWORD.getMaterial(), Material.STONE_SWORD, Material.IRON_SWORD, MaterialX.GOLDEN_SWORD.getMaterial(), Material.DIAMOND_SWORD);
		}
	}

	private final Map<UUID, Stack> stackMap = new HashMap<>();
	private final Cooldown cooldown = new Cooldown(COOLDOWN_CONFIG.getValue(), 35);
	private Bullet bullet = null;
	private Exhaustion exhaustion = null;
	private double dx, dz;

	public Lorem(Participant participant) {
		super(participant);
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onPlayerMove(final PlayerMoveEvent e) {
		final Location from = e.getFrom(), to = e.getTo();
		this.dx = to.getX() - from.getX();
		this.dz = to.getZ() - from.getZ();
	}

	@SubscribeEvent
	private void onEntityDamageByEntity(final EntityDamageByEntityEvent e) {
		if (getPlayer().equals(e.getDamager()) && (e.getCause() == DamageCause.ENTITY_ATTACK || e.getCause() == DamageCause.ENTITY_SWEEP_ATTACK)) {
			e.setCancelled(true);
			if (swords.contains(getPlayer().getInventory().getItemInMainHand().getType())) {
				ability();
			}
		}
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onPlayerInteract(final PlayerInteractEvent e) {
		if (e.getItem() != null && swords.contains(e.getItem().getType())) {
			if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
				ability();
			}
		}
	}

	@SubscribeEvent
	private void onDeath(EntityDeathEvent e) {
		if (stackMap.containsKey(e.getEntity().getUniqueId())) stackMap.get(e.getEntity().getUniqueId()).stop(true);
	}

	@SubscribeEvent
	private void onDeath(PlayerDeathEvent e) {
		if (stackMap.containsKey(e.getEntity().getUniqueId())) stackMap.get(e.getEntity().getUniqueId()).stop(true);
	}

	private void ability() {
		if (cooldown.isCooldown() || exhaustion != null || bullet != null) return;
		final ItemStack mainHand = getPlayer().getInventory().getItemInMainHand();
		if (swords.contains(mainHand.getType())) {
			new Bullet(getPlayer(), getPlayer().getLocation().clone().add(0, 1.5, 0), getPlayer().getLocation().getDirection().multiply(.4), mainHand.getEnchantmentLevel(Enchantment.DAMAGE_ALL), getPlayer().getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue(), COLOUR).start();
		}
	}

	private boolean addStack(final LivingEntity entity) {
		if (stackMap.containsKey(entity.getUniqueId())) {
			if (stackMap.get(entity.getUniqueId()).addStack()) {
				return true;
			}
		} else new Stack(entity).start();
		return false;
	}

	private void startCooldown() {
		cooldown.start();
		new Exhaustion(TimeUnit.TICKS, 40).start();
	}

	public class Bullet extends AbilityTimer {

		private final LivingEntity shooter;
		private final CustomEntity entity;
		private final Vector forward;
		private final int sharpnessEnchant;
		private final double damage;
		private final Predicate<Entity> predicate;

		private final RGB color;
		private Location lastLocation;

		private Bullet(LivingEntity shooter, Location startLocation, Vector arrowVelocity, int sharpnessEnchant, double damage, RGB color) {
			super(4);
			Lorem.this.bullet = this;
			setPeriod(TimeUnit.TICKS, 1);
			this.shooter = shooter;
			this.entity = new Bullet.ArrowEntity(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ()).resizeBoundingBox(-.75, -.75, -.75, .75, .75, .75);
			this.forward = arrowVelocity.multiply(10);
			this.sharpnessEnchant = sharpnessEnchant;
			this.damage = damage;
			this.color = color;
			this.lastLocation = startLocation;
			this.predicate = new Predicate<Entity>() {
				@Override
				public boolean test(Entity entity) {
					if (entity instanceof ArmorStand) return false;
					if (entity.equals(shooter)) return false;
					if (entity instanceof Player) {
						if (!getGame().isParticipating(entity.getUniqueId())
								|| (getGame() instanceof DeathManager.Handler && ((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
								|| !getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue()) {
							return false;
						}
						if (getGame() instanceof Teamable) {
							final Teamable teamGame = (Teamable) getGame();
							final Participant entityParticipant = teamGame.getParticipant(entity.getUniqueId()), participant = teamGame.getParticipant(shooter.getUniqueId());
							if (participant != null) {
								return !teamGame.hasTeam(entityParticipant) || !teamGame.hasTeam(participant) || (!teamGame.getTeam(entityParticipant).equals(teamGame.getTeam(participant)));
							}
						}
					}
					return true;
				}
			};
		}

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
				entity.setLocation(location);
				if (!isRunning()) {
					return;
				}
				final Block block = location.getBlock();
				final Material type = block.getType();
				if (type.isSolid()) {
					startCooldown();
					stop(true);
					return;
				}
				for (LivingEntity livingEntity : LocationUtil.getConflictingEntities(LivingEntity.class, entity.getWorld(), entity.getBoundingBox(), predicate)) {
					if (!shooter.equals(livingEntity)) {
						if (addStack(livingEntity)) {
							Damages.damageArrow(livingEntity, shooter, (float) (EnchantLib.getDamageWithSharpnessEnchantment(damage, sharpnessEnchant) * Math.max(0.25, Math.min(30, livingEntity.getLocation().distanceSquared(shooter.getLocation())) / 30) * 2));
							ParticleLib.BLOCK_CRACK.spawnParticle(livingEntity.getEyeLocation(), .3f, .3f, .3f, 15, MaterialX.REDSTONE_BLOCK);
							SoundLib.ENTITY_PLAYER_ATTACK_SWEEP.playSound(getPlayer());
						} else {
							Damages.damageArrow(livingEntity, shooter, (float) (EnchantLib.getDamageWithSharpnessEnchantment(damage, sharpnessEnchant) * Math.max(0.25, Math.min(30, livingEntity.getLocation().distanceSquared(shooter.getLocation())) / 30) * 0.6));
						}
						getPlayer().setVelocity(VectorUtil.validateVector(new Vector(dx, 0, dz).normalize().multiply(0.6)));
						stop(true);
						return;
					}
				}
				ParticleLib.REDSTONE.spawnParticle(location, color);
			}
			lastLocation = newLocation;
		}

		@Override
		protected void onEnd() {
			startCooldown();
			entity.remove();
			Lorem.this.bullet = null;
		}

		@Override
		protected void onSilentEnd() {
			entity.remove();
			Lorem.this.bullet = null;
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
				final Player deflectedPlayer = deflector.getPlayer();
				new Bullet(deflectedPlayer, lastLocation, newDirection, sharpnessEnchant, damage, color).start();
			}

			@Override
			public ProjectileSource getShooter() {
				return shooter;
			}

			@Override
			protected void onRemove() {
				Bullet.this.stop(false);
			}

		}

	}

	private class Stack extends AbilityTimer {

		private final LivingEntity entity;
		private final IHologram hologram;
		private int stack = 0;

		private Stack(LivingEntity entity) {
			super(30);
			setPeriod(TimeUnit.TICKS, 4);
			this.entity = entity;
			this.hologram = NMS.newHologram(entity.getWorld(), entity.getLocation().getX(), entity.getLocation().getY() + entity.getEyeHeight() + 0.6, entity.getLocation().getZ(), Strings.repeat("§3◆", stack).concat(Strings.repeat("§3◇", 4 - stack)));
			hologram.display(getPlayer());
			stackMap.put(entity.getUniqueId(), this);
			addStack();
		}

		@Override
		protected void run(int count) {
			hologram.teleport(entity.getWorld(), entity.getLocation().getX(), entity.getLocation().getY() + entity.getEyeHeight() + 0.6, entity.getLocation().getZ(), entity.getLocation().getYaw(), 0);
		}

		private boolean addStack() {
			setCount(30);
			stack++;
			hologram.setText(Strings.repeat("§3◆", stack).concat(Strings.repeat("§3◇", 4 - stack)));
			if (stack >= 4) {
				stop(false);
				return true;
			}
			return false;
		}

		@Override
		protected void onEnd() {
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			hologram.unregister();
			stackMap.remove(entity.getUniqueId());
		}
	}

	public class Exhaustion extends AbilityTimer implements Listener {

		private final ActionbarChannel channel;

		private Exhaustion(TimeUnit timeUnit, int duration) {
			super(TaskType.REVERSE, timeUnit.toTicks(duration));
			this.channel = getParticipant().actionbar().newChannel();
			setPeriod(TimeUnit.TICKS, 1);
		}

		@Override
		protected void onStart() {
			Lorem.this.exhaustion = this;
			Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		}

		@Override
		protected void run(int count) {
			channel.update("§3탈진§7: §f" + (count / (20.0 / getPeriod())) + "초");
			PotionEffects.SLOW.addPotionEffect(getPlayer(), 3, 2, true);
		}

		@EventHandler
		private void onPlayerMove(PlayerMoveEvent e) {
			if (!e.getPlayer().equals(getPlayer())) return;
			final double fromY = e.getFrom().getY(), toY = e.getTo().getY();
			if (toY > fromY) {
				e.getTo().setY(fromY);
			}
		}

		@EventHandler
		private void onShootBow(final EntityShootBowEvent e) {
			if (e.getEntity().equals(getPlayer())) e.setCancelled(true);
		}

		@Override
		protected void onEnd() {
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			channel.unregister();
			HandlerList.unregisterAll(this);
			Lorem.this.exhaustion = null;
		}

	}

}
