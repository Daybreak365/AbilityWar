package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.interfaces.TeamGame;
import daybreak.abilitywar.game.manager.object.DeathManager;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

@AbilityManifest(name = "아레스", rank = Rank.A, species = Species.GOD, explain = {
		"전쟁의 신 아레스.",
		"철괴를 우클릭하면 앞으로 돌진하며 주위의 엔티티에게 대미지를 주며,",
		"대미지를 받은 엔티티들을 밀쳐냅니다. $[COOLDOWN_CONFIG]"
})
public class Ares extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> DamageConfig = abilitySettings.new SettingObject<Integer>(Ares.class, "DamagePercent", 50,
			"# 스킬 대미지 (단위: 백분율)",
			"# 10으로 설정한 경우 대상의 최대 체력 10% 만큼의 대미지를 줍니다.") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

	};

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Ares.class, "Cooldown", 60,
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

	public static final SettingObject<Boolean> DashConfig = abilitySettings.new SettingObject<Boolean>(Ares.class, "DashIntoTheAir", false,
			"# true로 설정하면 아레스 능력 사용 시 공중으로 돌진 할 수 있습니다.") {

		@Override
		public boolean condition(Boolean value) {
			return true;
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
				if (getGame() instanceof TeamGame) {
					final TeamGame teamGame = (TeamGame) getGame();
					final Participant entityParticipant = getGame().getParticipant(entity.getUniqueId());
					return !teamGame.hasTeam(entityParticipant) || !teamGame.hasTeam(getParticipant()) || (!teamGame.getTeam(entityParticipant).equals(teamGame.getTeam(getParticipant())));
				}
			}
			return true;
		}
	};
	private final CooldownTimer cooldownTimer = new CooldownTimer(COOLDOWN_CONFIG.getValue());
	private final DurationTimer skill = new DurationTimer(20, cooldownTimer) {

		private Set<Damageable> attacked;

		@Override
		protected void onDurationStart() {
			attacked = new HashSet<>();
			SoundLib.ENTITY_PLAYER_ATTACK_SWEEP.playSound(LocationUtil.getNearbyEntities(Player.class, getPlayer().getLocation(), 10, 10, null));
		}

		@Override
		public void onDurationProcess(int seconds) {
			final Player p = getPlayer();
			ParticleLib.LAVA.spawnParticle(p.getLocation(), 4, 4, 4, 40);

			if (DashConfig.getValue()) {
				p.setVelocity(p.getVelocity().add(p.getLocation().getDirection().multiply(0.7)));
			} else {
				p.setVelocity(p.getVelocity().add(p.getLocation().getDirection().multiply(0.7).setY(0)));
			}

			for (LivingEntity livingEntity : LocationUtil.getNearbyEntities(LivingEntity.class, p.getLocation(), 4, 4, predicate)) {
				if (!attacked.contains(livingEntity)) {
					livingEntity.damage((livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() / 100) * DamageConfig.getValue(), p);
					attacked.add(livingEntity);
					SoundLib.BLOCK_ANVIL_LAND.playSound(p, 0.5f, 1);
					ParticleLib.SWEEP_ATTACK.spawnParticle(livingEntity.getEyeLocation(), 0, 0, 0, 1);
				} else if (seconds % 2 == 0) {
					livingEntity.setNoDamageTicks(0);
					livingEntity.damage(((livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() / 100) * DamageConfig.getValue()) / 5, p);
					ParticleLib.SWEEP_ATTACK.spawnParticle(livingEntity.getEyeLocation(), 0, 0, 0, 1);
				}
				livingEntity.setVelocity(livingEntity.getLocation().toVector().subtract(p.getLocation().toVector()).multiply(0.5).setY(0.5));
			}
		}

	}.setPeriod(TimeUnit.TICKS, 1);

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT)) {
			if (clickType.equals(ClickType.RIGHT_CLICK)) {
				if (!skill.isDuration() && !cooldownTimer.isCooldown()) {
					skill.start();

					return true;
				}
			}
		}

		return false;
	}

	@SubscribeEvent
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity().equals(getPlayer()) && e.getCause().equals(DamageCause.FALL) && skill.isDuration()) {
			e.setCancelled(true);
		}
	}

}
