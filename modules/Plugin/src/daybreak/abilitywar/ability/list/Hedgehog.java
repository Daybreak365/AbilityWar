package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.Scheduled;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.math.LocationUtil;
import org.bukkit.entity.Damageable;

@AbilityManifest(name = "고슴도치", rank = Rank.C, species = Species.ANIMAL, explain = {
		"가시로 가까이에 있는 모든 생명체를 찌릅니다.",
		"아야! 그것 참 아프겠네요."
})
public class Hedgehog extends AbilityBase {

	public static final SettingObject<Double> DamageConfig = new SettingObject<Double>(Hedgehog.class, "Damage", 2.0,
			"# 대미지") {

		@Override
		public boolean Condition(Double value) {
			return value >= 0;
		}

	};

	public Hedgehog(Participant participant) {
		super(participant);
	}

	@Scheduled
	private final Timer passive = new Timer() {
		@Override
		protected void run(int count) {
			if (!getPlayer().isDead()) {
				double damage = DamageConfig.getValue();
				for (Damageable damageable : LocationUtil.getNearbyDamageableEntities(getPlayer(), 1.5, 1.5)) {
					damageable.damage(damage, getPlayer());
				}
			}
		}
	}.setPeriod(TimeUnit.TICKS, 10);

}
