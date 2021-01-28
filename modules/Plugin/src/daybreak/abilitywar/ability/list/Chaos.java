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
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.base.color.RGB;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

@AbilityManifest(name = "카오스", rank = Rank.L, species = Species.GOD, explain = {
		"태초의 신 카오스.",
		"철괴를 우클릭하면 $[DURATION_CONFIG]초간 짙은 암흑 속으로 주변",
		"$[DURATION_CONFIG]칸 이내의 모든 물체와 생명체들을 끌어당기며",
		"대미지를 줍니다. $[COOLDOWN_CONFIG]"
})
@Tips(tip = {
		"어떤 적이던 상관 없이 카오스의 능력은 유용할 것입니다.",
		"단, 상대가 강력한 이동기를 가지고 있지만 않다면 말이죠."
}, strong = {
		@Description(subject = "강력한 군중 제어", explain = {
				"상대가 강력한 돌진기나 텔레포트 스킬이 있는 것이 아니라면,",
				"카오스로부터 벗어나는 것은 불가능에 가깝습니다. 주변의 모든",
				"생명체를 한 곳으로 몰아넣어 대미지를 주세요!"
		})
}, weak = {
		@Description(subject = "강력한 돌진기", explain = {
				"강력한 돌진기가 있는 능력자는 카오스를 벗어날 수도 있습니다."
		}),
		@Description(subject = "순간 이동 스킬", explain = {
				"순간 이동 스킬이 있다면 더욱 쉽게 카오스를 벗어날 수 있을 것입니다."
		}),
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
				if (entity instanceof Damageable) ((Damageable) entity).damage(1);
				entity.setVelocity(center.toVector().subtract(entity.getLocation().toVector()).multiply(0.7));
			}
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
