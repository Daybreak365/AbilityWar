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
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.object.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.LocationUtil.Locations;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import java.util.function.Predicate;
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
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

@AbilityManifest(name = "진검승부", rank = Rank.A, species = Species.HUMAN, explain = {
		"다른 플레이어를 철괴로 우클릭하면 대상과의 진검승부를 시작합니다. $[COOLDOWN_CONFIG]",
		"진검승부가 시작되면 밖으로 나갈 수 없는 지름 5칸의 링이 생성",
		"되고, 일시적으로 체력이 모두 회복되며 인벤토리 내의 모든 아이템이",
		"제거됩니다. 진검승부 중에는 대상의 능력이 비활성화 되며,",
		"상호 간의 공격 대미지 이외의 대미지는 받지 않습니다.",
		"능력 사용 후 $[DurationConfig]초가 지나거나 둘 중 한명이 죽은 경우",
		"진검승부가 종료되며, 체력이 능력 사용 전의 상태로 회복되고",
		"모든 아이템이 되돌아옵니다."
})
public class VictoryBySword extends AbilityBase implements TargetHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(VictoryBySword.class, "Cooldown", 110,
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

	public static final SettingObject<Integer> DurationConfig = abilitySettings.new SettingObject<Integer>(VictoryBySword.class, "Duration", 60,
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
	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());
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
	private final int duration = DurationConfig.getValue();

	@Override
	public void TargetSkill(Material material, LivingEntity entity) {
		if (entity instanceof Player && predicate.test(entity) && !cooldownTimer.isCooldown() && ring == null) {
			this.ring = new Ring(cooldownTimer, 5, (Player) entity);
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
			getPlayer().getInventory().clear();
			target.getInventory().clear();
			for (PotionEffects effect : PotionEffects.values()) {
				effect.removePotionEffect(getPlayer());
				effect.removePotionEffect(target);
			}
			for (Location loc : locations) {
				ParticleLib.REDSTONE.spawnParticle(loc, COLOR);
			}
		}

		@EventHandler
		protected void onDeath(PlayerDeathEvent e) {
			if (target.equals(e.getEntity())) {
				new AbilityTimer(3) {
					@Override
					protected void run(int count) {
						SoundLib.PIANO.playInstrument(getPlayer(), C);
						SoundLib.PIANO.playInstrument(getPlayer(), EFlat);
						SoundLib.PIANO.playInstrument(getPlayer(), G);
					}
				}.setPeriod(TimeUnit.TICKS, 7).start();
				stop(false);
			} else if (getPlayer().equals(e.getEntity())) {
				new AbilityTimer(3) {
					@Override
					protected void run(int count) {
						SoundLib.PIANO.playInstrument(target, C);
						SoundLib.PIANO.playInstrument(target, EFlat);
						SoundLib.PIANO.playInstrument(target, G);
					}
				}.setPeriod(TimeUnit.TICKS, 7).start();
				stop(false);
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
				if (restrictCondition.getAbility().equals(targetParticipant.getAbility())) {
					restrictCondition.getAbility().setRestricted(false);
				}
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
