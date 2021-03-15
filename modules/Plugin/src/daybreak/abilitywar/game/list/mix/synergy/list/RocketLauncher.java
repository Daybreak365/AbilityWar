package daybreak.abilitywar.game.list.mix.synergy.list;

import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.VectorUtil;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.base.math.geometry.vector.VectorIterator;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

@AbilityManifest(name = "로켓런쳐", rank = Rank.S, species = Species.OTHERS, explain = {
		"전쟁의 신 아레스.",
		"철괴를 우클릭하면 앞으로 돌진하며 주위의 엔티티에게 대미지를 주며,",
		"대미지를 받은 엔티티들을 밀쳐냅니다. $[COOLDOWN_CONFIG]",
		"또한 돌진 중 주위에 큰 폭발을 일으키며, 폭발 대미지를 받지 않습니다."
})
public class RocketLauncher extends Synergy implements ActiveHandler {

	public static final SettingObject<Integer> DAMAGE_CONFIG = synergySettings.new SettingObject<Integer>(RocketLauncher.class, "damage-percent", 40,
			"# 스킬 대미지 (단위: 백분율)",
			"# 10으로 설정한 경우 대상의 최대 체력 10% 만큼의 대미지를 줍니다.") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

	};

	public static final SettingObject<Integer> COOLDOWN_CONFIG = synergySettings.new SettingObject<Integer>(RocketLauncher.class, "cooldown", 90,
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

	public RocketLauncher(Participant participant) {
		super(participant);
	}

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
	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());
	private boolean noFallDamage = false;
	private boolean skillEnabled = false;
	private final Duration skill = new Duration(20, cooldownTimer) {

		private VectorIterator circle;
		private Set<LivingEntity> attacked;

		@Override
		protected void onDurationStart() {
			this.circle = Circle.infiniteIteratorOf(4, 10);
			this.attacked = new HashSet<>();
			noFallDamage = true;
			SoundLib.ENTITY_PLAYER_ATTACK_SWEEP.playSound(LocationUtil.getNearbyEntities(Player.class, getPlayer().getLocation(), 10, 10, null));
		}

		@Override
		public void onDurationProcess(int count) {
			ParticleLib.LAVA.spawnParticle(getPlayer().getLocation(), 5, 5, 5, 30);
			getPlayer().setVelocity(getPlayer().getVelocity().add(getPlayer().getLocation().getDirection().multiply(0.25).setY(((count / 20.0) - .5) * .45)));
			for (LivingEntity livingEntity : LocationUtil.getNearbyEntities(LivingEntity.class, getPlayer().getLocation(), 6, 6, predicate)) {
				if (!attacked.contains(livingEntity)) {
					livingEntity.damage((livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() / 100) * DAMAGE_CONFIG.getValue(), getPlayer());
					attacked.add(livingEntity);
					SoundLib.BLOCK_ANVIL_LAND.playSound(getPlayer(), 0.5f, 1);
					ParticleLib.SWEEP_ATTACK.spawnParticle(livingEntity.getEyeLocation(), 0, 0, 0, 1);
				} else if (count % 2 == 0) {
					livingEntity.setNoDamageTicks(0);
					livingEntity.damage(((livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() / 100) * DAMAGE_CONFIG.getValue()) / 9, getPlayer());
					ParticleLib.SWEEP_ATTACK.spawnParticle(livingEntity.getEyeLocation(), 0, 0, 0, 1);
				}
				livingEntity.setVelocity(getPlayer().getLocation().toVector().subtract(livingEntity.getLocation().toVector()).multiply((getPlayer().getLocation().distanceSquared(livingEntity.getLocation()) / 36) * 0.75));
			}
			for (int i = 0; i < 6; i++) {
				getPlayer().getWorld().createExplosion(getPlayer().getLocation().clone().add(VectorUtil.rotateAroundAxis(VectorUtil.rotateAroundAxisY(circle.next(), getPlayer().getLocation().getYaw()), VectorUtil.rotateAroundAxisY(getPlayer().getLocation().getDirection().clone().normalize().setY(0), 90), getPlayer().getLocation().getPitch() + 90)), 3);
			}
		}

		@Override
		protected void onDurationEnd() {
			for (LivingEntity livingEntity : LocationUtil.getNearbyEntities(LivingEntity.class, getPlayer().getLocation(), 6, 6, predicate)) {
				livingEntity.setVelocity(getPlayer().getLocation().toVector().subtract(livingEntity.getLocation().toVector()).multiply(0.45));
			}
			skillEnabled = true;
			new AbilityTimer() {
				@Override
				protected void run(int count) {
					if (skillEnabled) {
						final Block blockHere = getPlayer().getLocation().getBlock(), blockBelow = blockHere.getRelative(BlockFace.DOWN);
						if (blockHere.getType().isSolid() || blockBelow.getType().isSolid()) {
							skillEnabled = false;
							ability();
						}
					} else stop(false);
				}
			}.setPeriod(TimeUnit.TICKS, 5).start();
		}
	}.setPeriod(TimeUnit.TICKS, 1);

	@SubscribeEvent
	private void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity().equals(getPlayer()) && e.getCause().equals(DamageCause.FALL)) {
			if (noFallDamage) {
				e.setCancelled(true);
				noFallDamage = false;
			} else if (skill.isRunning()) {
				e.setCancelled(true);
			}
		}
	}

	@SubscribeEvent(onlyRelevant = true)
	public void onExplosion(EntityDamageEvent e) {
		if (e.getCause().equals(DamageCause.FALL) && skill.isDuration()) {
			e.setCancelled(true);
		}
		if (e.getCause().equals(DamageCause.BLOCK_EXPLOSION) || e.getCause().equals(DamageCause.ENTITY_EXPLOSION)) {
			e.setCancelled(true);
		}
	}

	@SubscribeEvent(onlyRelevant = true)
	public void onExplosion(EntityDamageByBlockEvent e) {
		if (e.getCause().equals(DamageCause.BLOCK_EXPLOSION) || e.getCause().equals(DamageCause.ENTITY_EXPLOSION)) {
			e.setCancelled(true);
		}
	}

	@SubscribeEvent(onlyRelevant = true)
	public void onExplosion(EntityDamageByEntityEvent e) {
		if (e.getCause().equals(DamageCause.BLOCK_EXPLOSION) || e.getCause().equals(DamageCause.ENTITY_EXPLOSION)) {
			e.setCancelled(true);
		}
	}

	@SubscribeEvent
	private void onPlayerMove(PlayerMoveEvent e) {
		if (e.getPlayer().equals(getPlayer())) {
			if (skillEnabled) {
				final Block blockHere = getPlayer().getLocation().getBlock(), blockBelow = blockHere.getRelative(BlockFace.DOWN);
				if (blockHere.getType().isSolid() || blockBelow.getType().isSolid()) {
					skillEnabled = false;
					ability();
				}
			}
		}
	}

	private void ability() {
		SoundLib.ENTITY_GENERIC_EXPLODE.playSound(getPlayer().getLocation(), 5, 1);
		for (LivingEntity livingEntity : LocationUtil.getNearbyEntities(LivingEntity.class, getPlayer().getLocation(), 6, 6, predicate)) {
			livingEntity.setVelocity(livingEntity.getLocation().toVector().subtract(getPlayer().getLocation().toVector()).normalize().multiply(2.5).setY(0.5));
		}
	}

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !skill.isDuration() && !cooldownTimer.isCooldown()) {
			skill.start();
			return true;
		}
		return false;
	}

}
