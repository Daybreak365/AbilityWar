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
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.library.PotionEffects;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.function.Predicate;

@AbilityManifest(name = "심안", rank = Rank.B, species = Species.HUMAN, explain = {
		"앞이 보이지 않는 대신, 주위 $[DISTANCE_CONFIG]칸 안에 있는 모든 생명체는",
		"발광 효과가 적용됩니다. 또한, 빠른 속도로 이동할 수 있습니다."
})
@Tips(tip = {
		"실명 효과로 인해 앞이 보이지 않지만, 적에게 걸리는 발광 효과 덕분에",
		"주변 생명체들의 위치를 더욱 잘 파악할 수 있습니다."
}, strong = {
		@Description(subject = "빠른 이동", explain = {
				"신속 효과 덕분에 빠르게 이동할 수 있습니다."
		}),
		@Description(subject = "위치 파악", explain = {
				"상대가 벽 뒤에 있더라도 발광 효과로 위치를 쉽게 파악할 수 있습니다."
		})
}, weak = {
		@Description(subject = "실명", explain = {
				"앞이 잘 보이지 않기 때문에, 주변 환경을 파악하기가 어렵습니다."
		}),
		@Description(subject = "크리티컬 공격", explain = {
				"실명으로 인해서 크리티컬 공격을 넣을 수 없습니다."
		})
}, stats = @Stats(offense = Level.ZERO, survival = Level.ZERO, crowdControl = Level.ZERO, mobility = Level.SIX, utility = Level.FIVE), difficulty = Difficulty.NORMAL)
public class DarkVision extends AbilityBase {

	public static final SettingObject<Integer> DISTANCE_CONFIG = abilitySettings.new SettingObject<Integer>(DarkVision.class, "Distance", 30,
			"# 거리 설정") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	public DarkVision(Participant participant) {
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

	private final int distance = DISTANCE_CONFIG.getValue();

	private final AbilityTimer darkVision = new AbilityTimer() {
		@Override
		public void run(int count) {
			PotionEffects.BLINDNESS.addPotionEffect(getPlayer(), 40, 0, true);
			PotionEffects.SPEED.addPotionEffect(getPlayer(), 5, 5, true);
			for (LivingEntity entity : LocationUtil.getNearbyEntities(LivingEntity.class, getPlayer().getLocation(), distance, distance, predicate)) {
				PotionEffects.GLOWING.addPotionEffect(entity, 10, 0, true);
			}
		}
	}.setPeriod(TimeUnit.TICKS, 1).register();

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			darkVision.start();
		}
	}

}
