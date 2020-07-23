package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
import daybreak.abilitywar.utils.library.PotionEffects;
import java.util.Random;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@AbilityManifest(name = "데미갓", rank = Rank.S, species = Species.DEMIGOD, explain = {
		"공격을 받으면 $[ChanceConfig]% 확률로 5초간 임의의 버프가 적용됩니다."
})
public class Demigod extends AbilityBase {

	public static final SettingObject<Integer> ChanceConfig = abilitySettings.new SettingObject<Integer>(Demigod.class, "Chance", 30,
			"# 공격을 받았을 시 몇 퍼센트 확률로 랜덤 버프를 받을지 설정합니다.",
			"# 30은 30%를 의미합니다.") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1 && value <= 100;
		}

	};

	public Demigod(Participant participant) {
		super(participant);
	}

	private final int chance = ChanceConfig.getValue();

	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if (p.equals(getPlayer())) {
				if (!e.isCancelled()) {
					Random r = new Random();

					if ((r.nextInt(100) + 1) <= chance) {
						int buff = r.nextInt(3);
						if (buff == 0) {
							showHelix(ABSORPTION);
							PotionEffects.ABSORPTION.addPotionEffect(p, 100, 1, true);
						} else if (buff == 1) {
							showHelix(REGENERATION);
							PotionEffects.REGENERATION.addPotionEffect(p, 100, 0, true);
						} else if (buff == 2) {
							showHelix(DAMAGE_RESISTANCE);
							PotionEffects.DAMAGE_RESISTANCE.addPotionEffect(p, 100, 1, true);
						}
					}
				}
			}
		}
	}

	private static final RGB ABSORPTION = RGB.of(255, 215, 54);
	private static final RGB REGENERATION = RGB.of(255, 92, 92);
	private static final RGB DAMAGE_RESISTANCE = RGB.of(173, 173, 173);

	private static final int particleCount = 20;
	private static final double yDiff = 0.6 / particleCount;
	private static final Circle circle = Circle.of(0.5, particleCount);

	private void showHelix(RGB color) {
		new AbilityTimer((particleCount * 3) / 2) {
			int count = 0;

			@Override
			protected void run(int a) {
				for (int i = 0; i < 2; i++) {
					ParticleLib.REDSTONE.spawnParticle(getPlayer().getLocation().clone().add(circle.get(count % 20)).add(0, count * yDiff, 0), color);
					count++;
				}
			}
		}.setPeriod(TimeUnit.TICKS, 1).start();
	}

}
