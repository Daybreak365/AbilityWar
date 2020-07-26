package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.game.manager.object.DeathManager;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.library.PotionEffects;
import java.util.function.Predicate;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

@AbilityManifest(name = "심안", rank = Rank.B, species = Species.HUMAN, explain = {
		"앞이 보이지 않는 대신, 플레이어의 $[DistanceConfig]칸 안에 있는 모든 생명체는",
		"발광 효과가 적용됩니다. 또한, 빠르게 달리고 높게 점프할 수 있습니다."
})
public class DarkVision extends AbilityBase {

	public static final SettingObject<Integer> DistanceConfig = abilitySettings.new SettingObject<Integer>(DarkVision.class, "Distance", 30,
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

	private final int distance = DistanceConfig.getValue();

	private final AbilityTimer darkVision = new AbilityTimer() {
		@Override
		public void run(int count) {
			PotionEffects.BLINDNESS.addPotionEffect(getPlayer(), 40, 0, true);
			PotionEffects.SPEED.addPotionEffect(getPlayer(), 5, 5, true);
			PotionEffects.JUMP.addPotionEffect(getPlayer(), 5, 1, true);
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
