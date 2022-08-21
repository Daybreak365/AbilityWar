package daybreak.abilitywar.ability.list;

import com.google.common.collect.ImmutableMap;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.TargetHandler;
import daybreak.abilitywar.ability.event.AbilityPreRestrictionEvent;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.config.enums.CooldownDecrease;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer.TaskType;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.LocationUtil.Locations;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Predicate;

@AbilityManifest(name = "진검승부", rank = Rank.A, species = Species.HUMAN, explain = {
		"적을 철괴로 우클릭하면 대상과의 진검승부를 시작합니다. $[COOLDOWN_CONFIG]",
		"진검승부가 시작되면 밖으로 나갈 수 없는 지름 5칸의 링이 생성되고,",
		"체력이 모두 회복되고 인벤토리가 초기화된 후 나무 검이 주어집니다.",
		"진검승부 중에는 대상의 능력이 비활성화 되며, 상호 간의 공격 피해",
		"외의 피해는 입지 않습니다. $[DURATION_CONFIG]초가 지나거나 둘 중 하나가 죽은 경우",
		"체력, 인벤토리 등이 능력 사용 전의 상태로 되돌아갑니다."
}, summarize = {
		"§7적을 대고 철괴를 우클릭§f하여 대상과의 §c진검승부§f를 시작합니다.",
		"두 플레이어는 갑옷 없이 풀 체력에 §6나무 검§f으로만 싸우며, 상대의 능력이 비활성화됩니다.",
		"이때 §5링§f이 생성되어 서로 §5링§f을 벗어나지 못합니다.",
		"양측의 공격만이 인정되고, 승자는 원상태로 되돌아옵니다."
})
public class VictoryBySword extends AbilityBase implements TargetHandler {

	private static final ItemStack[] swords = new ItemStack[41];

	static {
		final ItemStack sword = MaterialX.WOODEN_SWORD.createItem();
		for (int i = 0; i < 9; i++) {
			swords[i] = sword;
		}
	}

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(VictoryBySword.class, "cooldown", 110,
			"# 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};

	public static final SettingObject<Integer> DURATION_CONFIG = abilitySettings.new SettingObject<Integer>(VictoryBySword.class, "duration", 60,
			"# 능력 지속시간") {

		@Override
		public boolean condition(Integer value) {
			return value >= 5;
		}

	};

	public VictoryBySword(Participant participant) {
		super(participant);
	}

	private static final ImmutableMap<Integer, Note> notes = ImmutableMap.<Integer, Note>builder()
			.put(9, Note.natural(0, Tone.C))
			.put(8, Note.natural(0, Tone.D))
			.put(7, Note.flat(0, Tone.E))
			.put(6, Note.natural(0, Tone.F))
			.put(5, Note.natural(1, Tone.G))
			.put(4, Note.flat(1, Tone.A))
			.put(3, Note.natural(1, Tone.B))
			.put(2, Note.natural(1, Tone.C))
			.put(1, Note.natural(1, Tone.G))
			.build();

	private Ring ring = null;
	private static final Note C = Note.natural(0, Tone.C);
	private static final Note EFlat = Note.flat(0, Tone.E);
	private static final Note G = Note.natural(0, Tone.G);

	private static final RGB COLOR = new RGB(138, 25, 115);
	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue(), CooldownDecrease._50);
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
	private final int duration = DURATION_CONFIG.getValue();

	@Override
	public void TargetSkill(Material material, LivingEntity entity) {
		if (entity instanceof Player && predicate.test(entity) && !cooldownTimer.isCooldown() && ring == null) {
			this.ring = new Ring(cooldownTimer, 4, (Player) entity);
			ring.start();
		}
	}

	public class Ring extends Duration implements Listener {

		private final double radius;
		private final Location center;
		private final Locations locations;
		private final Participant targetParticipant;
		private final Player target;
		private final ItemStack[] contents, targetContents;
		private final double health, targetHealth;
		private final Restriction.Condition restrictCondition;

		public Ring(Cooldown cooldownTimer, double radius, Player target) {
			super(duration * 20, cooldownTimer);
			this.radius = radius;
			this.center = getPlayer().getLocation().clone();
			this.locations = Circle.of(radius, (int) (radius * 30)).toLocations(center).floor(center.getY());
			this.target = target;
			this.targetParticipant = getGame().getParticipant(target);
			getParticipant().attributes().TARGETABLE.setValue(false);
			targetParticipant.attributes().TARGETABLE.setValue(false);
			this.contents = getPlayer().getInventory().getContents();
			this.targetContents = target.getInventory().getContents();
			getPlayer().getInventory().clear();
			target.getInventory().clear();
			this.restrictCondition = targetParticipant.hasAbility() ? targetParticipant.getAbility().getRestriction().new Condition() {
				@Override
				public boolean condition() {
					return true;
				}
			}.register() : null;
			if (targetParticipant.hasAbility()) {
				targetParticipant.getAbility().setRestricted(true);
			}
			this.health = getPlayer().getHealth();
			this.targetHealth = target.getHealth();
			getPlayer().setHealth(getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
			target.setHealth(target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
			setPeriod(TimeUnit.TICKS, 1);
		}

		@Override
		protected void onDurationStart() {
			Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
			getPlayer().getInventory().setContents(swords);
			target.getInventory().setContents(swords);
		}

		@EventHandler
		public void onPlayerMove(PlayerMoveEvent e) {
			Player player = e.getPlayer();
			if ((player.equals(getPlayer()) || player.equals(target)) && e.getTo() != null && !LocationUtil.isInCircle(center, e.getTo(), radius)) {
				if (LocationUtil.isInCircle(center, e.getFrom(), radius)) {
					e.setTo(e.getFrom().setDirection(e.getTo().getDirection()));
				} else {
					player.teleport(center);
				}
			}
		}

		@EventHandler
		public void onPlayerDropItem(PlayerDropItemEvent e) {
			if (getPlayer().equals(e.getPlayer()) || target.equals(e.getPlayer())) {
				e.setCancelled(true);
			}
		}

		@EventHandler
		public void onInventoryClick(InventoryClickEvent e) {
			if (getPlayer().equals(e.getWhoClicked()) || target.equals(e.getWhoClicked())) {
				e.setCancelled(true);
			}
		}

		@EventHandler
		public void onPlayerTeleport(PlayerTeleportEvent e) {
			Player player = e.getPlayer();
			if ((player.equals(getPlayer()) || player.equals(target)) && e.getTo() != null && !LocationUtil.isInCircle(center, e.getTo(), radius)) {
				if (LocationUtil.isInCircle(center, e.getFrom(), radius)) {
					e.setTo(e.getFrom());
				} else {
					e.setTo(center);
				}
			}
		}

		@EventHandler(ignoreCancelled = true)
		public void onBlockPlace(BlockPlaceEvent e) {
			if (LocationUtil.isInCircle(center, e.getBlock().getLocation(), radius)) {
				e.setCancelled(true);
				e.getPlayer().sendMessage("§c링 안에 블록을 설치할 수 없습니다.");
			}
		}

		@EventHandler(ignoreCancelled = true)
		public void onPlayerBucketEmpty(PlayerBucketEmptyEvent e) {
			if (LocationUtil.isInCircle(center, e.getBlockClicked().getLocation(), radius)) {
				e.setCancelled(true);
				e.getPlayer().sendMessage("§c링 안에 블록을 설치할 수 없습니다.");
			}
		}

		@EventHandler(ignoreCancelled = true)
		public void onBlockPhysics(BlockPhysicsEvent e) {
			if (LocationUtil.isInCircle(center, e.getBlock().getLocation(), radius)) {
				e.setCancelled(true);
			}
		}

		@EventHandler(priority = EventPriority.HIGHEST)
		public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
			Entity entity = e.getEntity();
			Entity damager = e.getDamager();
			if (damager instanceof Projectile) {
				Projectile projectile = (Projectile) damager;
				if (projectile.getShooter() instanceof Entity) {
					damager = (Entity) projectile.getShooter();
				}
			}
			if (entity.equals(getPlayer()) || entity.equals(target)) {
				if (damager.equals(getPlayer()) || damager.equals(target)) {
					Player entityPlayer = (Player) entity;
					e.setCancelled(false);
					e.setDamage(e.getDamage() / 2);
					SoundLib.PIANO.playInstrument(damager.equals(getPlayer()) ? getPlayer() : target, notes.get(Math.max(1, Math.min(9, (int) ((entityPlayer.getHealth() / entityPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) * 10)))));
				} else {
					e.setCancelled(true);
					if (damager instanceof Player) damager.sendMessage(ChatColor.RED + "진검승부 중인 상대를 공격할 수 없습니다!");
				}
			} else {
				if (damager.equals(getPlayer()) || damager.equals(target)) {
					e.setCancelled(true);
					if (damager instanceof Player) damager.sendMessage(ChatColor.RED + "진검승부 중에 다른 상대를 공격할 수 없습니다!");
				}
			}
		}

		@EventHandler
		public void onEntityDamageByBlock(EntityDamageByBlockEvent e) {
			onEntityDamage(e);
		}

		@EventHandler
		public void onEntityDamage(EntityDamageEvent e) {
			if (e instanceof EntityDamageByEntityEvent) return;
			if (e.getEntity().equals(getPlayer()) || e.getEntity().equals(target)) e.setCancelled(true);
		}

		@EventHandler
		private void onPreAbilityRestriction(AbilityPreRestrictionEvent e) {
			if ((e.getAbility().getParticipant().equals(targetParticipant) || e.getAbility().getParticipant().equals(getParticipant())) && !e.getNewState())
				e.setNewState(true);
		}

		@Override
		protected void onDurationProcess(int count) {
			for (PotionEffects effect : PotionEffects.values()) {
				effect.removePotionEffect(getPlayer());
				effect.removePotionEffect(target);
			}
			if (count % 4 == 0) {
				for (Location loc : locations) {
					ParticleLib.REDSTONE.spawnParticle(loc, COLOR);
				}
			}
		}

		@EventHandler
		protected void onDeath(PlayerDeathEvent e) {
			if (target.equals(e.getEntity()) || getPlayer().equals(e.getEntity())) {
				getGame().new GameTimer(TaskType.NORMAL, 3) {
					@Override
					protected void run(int count) {
						SoundLib.PIANO.playInstrument(getPlayer().getLocation(), 1.3f, C);
						SoundLib.PIANO.playInstrument(getPlayer().getLocation(), 1.3f, EFlat);
						SoundLib.PIANO.playInstrument(getPlayer().getLocation(), 1.3f, G);
					}
				}.setPeriod(TimeUnit.TICKS, 7).start();
				stop(false);
				getPlayer().getInventory().setContents(contents);
				target.getInventory().setContents(targetContents);
			}
		}

		@Override
		protected void onDurationEnd() {
			onDurationSilentEnd();
		}

		@Override
		protected void onDurationSilentEnd() {
			getParticipant().attributes().TARGETABLE.setValue(true);
			targetParticipant.attributes().TARGETABLE.setValue(true);
			HandlerList.unregisterAll(this);
			getPlayer().getInventory().setContents(contents);
			target.getInventory().setContents(targetContents);
			if (restrictCondition != null) {
				restrictCondition.unregister();
				restrictCondition.getAbility().setRestricted(false);
			}
			if (!target.isDead()) {
				target.setHealth(Math.min(targetHealth, target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
			}
			if (!getPlayer().isDead()) {
				getPlayer().setHealth(Math.min(health, getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
			}
			unregister();
			ring = null;
		}

	}

}
