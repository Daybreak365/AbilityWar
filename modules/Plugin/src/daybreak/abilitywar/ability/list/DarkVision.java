package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.Scheduled;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.library.PotionEffects;
import org.bukkit.entity.LivingEntity;

@AbilityManifest(name = "심안", rank = Rank.B, species = Species.HUMAN, explain = {
		"앞이 보이지 않는 대신, 플레이어의 $[DistanceConfig]칸 안에 있는 모든 생명체는",
		"발광 효과가 적용됩니다. 또한, 빠르게 달리고 높게 점프할 수 있습니다."
})
public class DarkVision extends AbilityBase {

	public static final SettingObject<Integer> DistanceConfig = new SettingObject<Integer>(DarkVision.class, "Distance", 30,
			"# 거리 설정") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}

	};

	public DarkVision(Participant participant) {
		super(participant);
	}

	private final int distance = DistanceConfig.getValue();

	@Scheduled
	private final Timer darkVision = new Timer() {
		@Override
		public void run(int count) {
			PotionEffects.BLINDNESS.addPotionEffect(getPlayer(), 40, 0, true);
			PotionEffects.SPEED.addPotionEffect(getPlayer(), 5, 5, true);
			PotionEffects.JUMP.addPotionEffect(getPlayer(), 5, 1, true);
			for (LivingEntity entity : LocationUtil.getNearbyEntities(LivingEntity.class, getPlayer(), distance, distance)) {
				PotionEffects.GLOWING.addPotionEffect(entity, 10, 0, true);
			}
		}
	}.setPeriod(TimeUnit.TICKS, 1);

}
