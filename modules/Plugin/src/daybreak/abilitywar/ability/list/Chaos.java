package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.Tips;
import daybreak.abilitywar.ability.Tips.Description;
import daybreak.abilitywar.ability.Tips.Difficulty;
import daybreak.abilitywar.ability.Tips.Level;
import daybreak.abilitywar.ability.Tips.Stats;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.effect.Oppress;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.PotionEffects;
import kotlin.ranges.RangesKt;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

@AbilityManifest(name = "카오스", rank = Rank.L, species = Species.GOD, explain = {
		"§7철괴 우클릭 §f- §8흡수§f: 카오스가 $[DURATION_CONFIG]초간 주변 $[DURATION_CONFIG]칸 이내의 모든 생명체를 §5실명시키고§f,",
		"중앙으로 §5끌어당기며§f, 주기적으로 §c고정 피해§f를 입힙니다. $[COOLDOWN_CONFIG]",
		"§8[§3질량§f-§e에너지 §f동등성§8] §f입힌 피해의 §a50%§f를 자신의 체력으로 전환하여 §a회복§f합니다.",
		"§8[§e빛§f조차 빠져나갈 수 없는§8] §f범위 내 모든 플레이어의 §5능력을 비활성화§f합니다.",
		"§7상태 이상 §f- §8제압§f: 능력이 비활성화됩니다."
})
@Tips(tip = {
		"어떤 적이던 상관 없이 카오스의 능력은 유용할 것입니다."
}, strong = {
		@Description(subject = "강력한 군중 제어", explain = {
				"상대가 강력한 돌진기나 텔레포트 스킬이 있는 것이 아니라면,",
				"카오스로부터 벗어나는 것은 불가능에 가깝습니다. 주변의 모든",
				"생명체를 한 곳으로 몰아넣어 대미지를 주세요!"
		})
}, weak = {
		@Description(subject = "상대의 공격", explain = {
				"끌어 당겨서 움직이기 힘들게 하는 것이지, 시야를 돌리지 못하게 막는 것이",
				"아닙니다. 항상 상대의 공격을 조심하십시오."
		})
}, stats = @Stats(offense = Level.SIX, survival = Level.ZERO, crowdControl = Level.SEVEN, mobility = Level.ZERO, utility = Level.ZERO), difficulty = Difficulty.EASY)
public class Chaos extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Chaos.class, "cooldown", 100,
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

	public static final SettingObject<Integer> DURATION_CONFIG = abilitySettings.new SettingObject<Integer>(Chaos.class, "duration", 5,
			"# 능력 지속 시간") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	public static final SettingObject<Integer> DISTANCE_CONFIG = abilitySettings.new SettingObject<Integer>(Chaos.class, "distance", 5,
			"# 거리 설정") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	public Chaos(Participant participant) {
		super(participant);
	}

	private static final RGB BLACK = RGB.of(1, 1, 1);
	private final int distance = DISTANCE_CONFIG.getValue();
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
	private final Circle CIRCLE = Circle.of(distance, distance * 4);
	private final Duration skill = new Duration(DURATION_CONFIG.getValue() * 20, cooldownTimer) {

		private Location center;
		private Circle pCircle, sCircle;

		@Override
		public void onDurationStart() {
			this.center = getPlayer().getLocation();
			this.pCircle = CIRCLE.clone();
			this.sCircle = CIRCLE.clone();
		}

		@Override
		public void onDurationProcess(int count) {
			if (count % 2 == 0) {
				ParticleLib.SMOKE_LARGE.spawnParticle(center, 1.4, 1.4, 1.4, 35, 0.05);
			}
			for (Location loc : pCircle.rotateAroundAxisX(-5).rotateAroundAxisZ(5).rotateAroundAxisY(3).toLocations(center)) {
				ParticleLib.REDSTONE.spawnParticle(loc, BLACK);
			}
			for (Location loc : sCircle.rotateAroundAxisX(5).rotateAroundAxisZ(-5).rotateAroundAxisY(-6).toLocations(center)) {
				ParticleLib.REDSTONE.spawnParticle(loc, BLACK);
			}
			for (Entity entity : LocationUtil.getNearbyEntities(Entity.class, center, distance, distance, predicate)) {
				if (entity instanceof Player) {
					final Participant participant = getGame().getParticipant(entity.getUniqueId());
					Oppress.apply(participant, TimeUnit.TICKS, 5);
				}
				if (count % 3 == 0) {
					if (entity instanceof LivingEntity) {
						final LivingEntity livingEntity = (LivingEntity) entity;
						PotionEffects.BLINDNESS.addPotionEffect(livingEntity, 30, 1, true);
						livingEntity.setNoDamageTicks(0);
						{
							final EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(getPlayer(), livingEntity, DamageCause.ENTITY_ATTACK, 0.3f);
							Bukkit.getPluginManager().callEvent(event);
							if (!event.isCancelled()) {
								livingEntity.setNoDamageTicks(livingEntity.getMaximumNoDamageTicks());
								livingEntity.setHealth(Math.max(0, livingEntity.getHealth() - 0.3f));
								NMS.broadcastEntityEffect(livingEntity, (byte) 2);
								livingEntity.setLastDamageCause(event);
							}
						}
						{
							final EntityRegainHealthEvent event = new EntityRegainHealthEvent(getPlayer(), 0.15f, RegainReason.CUSTOM);
							Bukkit.getPluginManager().callEvent(event);
							if (!event.isCancelled()) {
								getPlayer().setHealth(RangesKt.coerceIn(getPlayer().getHealth() + event.getAmount(), 0, getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
							}
						}
					}
				}
				entity.setVelocity(center.toVector().subtract(entity.getLocation().toVector()).multiply(0.7));
			}
		}

		@Override
		protected void onDurationEnd() {
			super.onDurationEnd();
		}

	}.setPeriod(TimeUnit.TICKS, 1);

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !skill.isDuration() && !cooldownTimer.isCooldown()) {
			skill.start();
			return true;
		}
		return false;
	}

}
