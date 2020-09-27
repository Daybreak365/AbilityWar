package daybreak.abilitywar.utils.base.minecraft;

import daybreak.abilitywar.utils.base.Random;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

public class FireworkUtil {

	private FireworkUtil() {
	}

	private static final Color[] colors = {Color.AQUA, Color.BLACK, Color.BLACK, Color.FUCHSIA, Color.GRAY, Color.GREEN, Color.LIME, Color.MAROON, Color.NAVY, Color.OLIVE, Color.ORANGE, Color.PURPLE, Color.RED, Color.SILVER, Color.TEAL, Color.WHITE, Color.YELLOW};
	private static final Random random = new Random();

	public static Firework spawnRandomFirework(final Location location, int power) {
		Firework firework = location.getWorld().spawn(location, Firework.class);
		FireworkMeta fireworkMeta = firework.getFireworkMeta();
		fireworkMeta.addEffect(FireworkEffect.builder()
				.flicker(random.nextBoolean())
				.withColor(random.pick(colors))
				.withFade(random.pick(colors))
				.with(Type.values()[random.nextInt(Type.values().length)])
				.trail(random.nextBoolean())
				.build());
		fireworkMeta.setPower(power);
		firework.setFireworkMeta(fireworkMeta);
		return firework;
	}

	public static Firework spawnRandomFirework(final Location location) {
		return spawnRandomFirework(location, 1 + random.nextInt(2));
	}

	public static Firework spawnRandomFirework(final Location location, Color[] colors, Color[] fadeColors, Type[] types, int power) {
		Firework firework = location.getWorld().spawn(location, Firework.class);
		FireworkMeta fireworkMeta = firework.getFireworkMeta();
		fireworkMeta.addEffect(FireworkEffect.builder()
				.flicker(random.nextBoolean())
				.withColor(colors[random.nextInt(colors.length)])
				.withFade(fadeColors[random.nextInt(fadeColors.length)])
				.with(types[random.nextInt(types.length)])
				.trail(random.nextBoolean())
				.build());
		fireworkMeta.setPower(power);
		firework.setFireworkMeta(fireworkMeta);
		return firework;
	}

	private static final FireworkEffect WINNER_EFFECT = FireworkEffect.builder()
			.flicker(true)
			.withColor(Color.WHITE, Color.BLUE, Color.RED)
			.withFade(Color.BLACK)
			.with(Type.BURST)
			.trail(true)
			.build();

	public static Firework spawnWinnerFirework(final Location location) {
		final Firework firework = location.getWorld().spawn(location, Firework.class);
		final FireworkMeta meta = firework.getFireworkMeta();
		meta.addEffect(WINNER_EFFECT);
		meta.setPower(random.nextInt(2) + 1);
		firework.setFireworkMeta(meta);
		return firework;
	}

}