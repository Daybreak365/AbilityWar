package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.Tips;
import daybreak.abilitywar.ability.Tips.Description;
import daybreak.abilitywar.ability.Tips.Difficulty;
import daybreak.abilitywar.ability.Tips.Level;
import daybreak.abilitywar.ability.Tips.Stats;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.base.random.RouletteWheel;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.library.PotionEffects;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Random;

@AbilityManifest(name = "데미갓", rank = Rank.S, species = Species.DEMIGOD, explain = {
		"공격을 받으면 $[CHANCE_CONFIG]% 확률로 5초간 §e흡수§f/§c재생§f/§3저항§f 중 하나의 효과가 적용됩니다."
})
@Tips(tip = {
		"모든 것은 사용자의 운에 달려있습니다."
}, strong = {
		@Description(subject = "운", explain = {
				"사용자의 운이 곧 강점입니다.",
				"오늘 느낌이 좋으신가요? 이 능력은 당신에게 딱입니다."
		})
}, weak = {
		@Description(subject = "운", explain = {
				"사용자의 운이 곧 약점입니다.",
				"오늘 느낌이 좋지 않으신가요? 이 능력은 피하세요."
		})
}, stats = @Stats(offense = Level.ZERO, survival = Level.THREE, crowdControl = Level.ZERO, mobility = Level.ZERO, utility = Level.THREE), difficulty = Difficulty.VERY_EASY)
public class Demigod extends AbilityBase {

	public static final SettingObject<Integer> CHANCE_CONFIG = abilitySettings.new SettingObject<Integer>(Demigod.class, "chance", 30,
			"# 피격시 랜덤 버프를 받을 확률",
			"# 30 = 30%") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1 && value <= 100;
		}

	};

	private static final Random random = new Random();

	private static final RGB ABSORPTION = RGB.of(255, 215, 54), REGENERATION = RGB.of(255, 92, 92), DAMAGE_RESISTANCE = RGB.of(173, 173, 173);

	private static final int particleCount = 20;
	private static final double yDiff = 0.6 / particleCount;
	private static final Circle circle = Circle.of(0.5, particleCount);

	public Demigod(Participant participant) {
		super(participant);
	}

	private final RouletteWheel rouletteWheel = new RouletteWheel();
	private final RouletteWheel.Slice positive = rouletteWheel.newSlice(CHANCE_CONFIG.getValue() * 10), negative = rouletteWheel.newSlice(1000 - positive.getWeight());

	@SubscribeEvent(onlyRelevant = true, ignoreCancelled = true)
	private void onEntityDamageByEntity(final EntityDamageByEntityEvent e) {
		final RouletteWheel.Slice select = rouletteWheel.select();
		if (select == positive) {
			switch (random.nextInt(5)) {
				case 0:
					showHelix(ABSORPTION);
					PotionEffects.ABSORPTION.addPotionEffect(getPlayer(), 100, 1, true);
					break;
				case 1: case 2:
					showHelix(REGENERATION);
					PotionEffects.REGENERATION.addPotionEffect(getPlayer(), 100, 0, true);
					break;
				case 3: case 4:
					showHelix(DAMAGE_RESISTANCE);
					PotionEffects.DAMAGE_RESISTANCE.addPotionEffect(getPlayer(), 100, 1, true);
					break;
			}
			negative.increaseWeight(5);
			positive.resetWeight();
		} else {
			positive.increaseWeight(5);
			negative.resetWeight();
		}
	}

	private void showHelix(final RGB color) {
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
