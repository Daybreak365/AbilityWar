package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.Tips;
import daybreak.abilitywar.ability.Tips.Difficulty;
import daybreak.abilitywar.ability.Tips.Level;
import daybreak.abilitywar.ability.Tips.Stats;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

@AbilityManifest(name = "아레스", rank = Rank.A, species = Species.GOD, explain = {
		"전쟁의 신 아레스.",
		"철괴를 우클릭하면 앞으로 도약합니다. 도약 중 주위의 생명체들을 끌고 가며",
		"대미지를 주고, 도약이 끝나 땅에 착지하면 주변의 생명체들을",
		"모두 밀쳐냅니다. $[COOLDOWN_CONFIG]"
})
@Tips(tip = {
		"낙하 대미지를 한 번 무시할 수 있기 때문에, 매우 높은 곳에서 착지할 때",
		"사용할 수도 있습니다. 상대를 밀쳐내는 효과도 있기 때문에 기절을 사용하는",
		"능력을 카운팅할 때도 유용하고, 멀리 도망가는 적을 잡을 때도 좋습니다."
}, stats = @Stats(offense = Level.SIX, survival = Level.ZERO, crowdControl = Level.FIVE, mobility = Level.FOUR, utility = Level.ZERO), difficulty = Difficulty.EASY)
public class Ares extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> DamageConfig = abilitySettings.new SettingObject<Integer>(Ares.class, "damage-percent", 40,
			"# 스킬 대미지 (단위: 백분율)",
			"# 10으로 설정한 경우 대상의 최대 체력 10% 만큼의 대미지를 줍니다.") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

	};

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Ares.class, "cooldown", 90,
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

	public Ares(Participant participant) {
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

		private Set<LivingEntity> attacked;

		@Override
		protected void onDurationStart() {
			attacked = new HashSet<>();
			noFallDamage = true;
			SoundLib.ENTITY_PLAYER_ATTACK_SWEEP.playSound(LocationUtil.getNearbyEntities(Player.class, getPlayer().getLocation(), 10, 10, null));
		}

		@Override
		public void onDurationProcess(int count) {
			ParticleLib.LAVA.spawnParticle(getPlayer().getLocation(), 5, 5, 5, 30);

			getPlayer().setVelocity(getPlayer().getVelocity().add(getPlayer().getLocation().getDirection().multiply(0.25).setY(((count / 20.0) - .5) * .45)));
			for (LivingEntity livingEntity : LocationUtil.getNearbyEntities(LivingEntity.class, getPlayer().getLocation(), 6, 6, predicate)) {
				if (!attacked.contains(livingEntity)) {
					livingEntity.damage((livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() / 100) * DamageConfig.getValue(), getPlayer());
					attacked.add(livingEntity);
					SoundLib.BLOCK_ANVIL_LAND.playSound(getPlayer(), 0.5f, 1);
					ParticleLib.SWEEP_ATTACK.spawnParticle(livingEntity.getEyeLocation(), 0, 0, 0, 1);
				} else if (count % 2 == 0) {
					livingEntity.setNoDamageTicks(0);
					livingEntity.damage(((livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() / 100) * DamageConfig.getValue()) / 9, getPlayer());
					ParticleLib.SWEEP_ATTACK.spawnParticle(livingEntity.getEyeLocation(), 0, 0, 0, 1);
				}
				livingEntity.setVelocity(getPlayer().getLocation().toVector().subtract(livingEntity.getLocation().toVector()).multiply((getPlayer().getLocation().distanceSquared(livingEntity.getLocation()) / 36) * 0.75));
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
