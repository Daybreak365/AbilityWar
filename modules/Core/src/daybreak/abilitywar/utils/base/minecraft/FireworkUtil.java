package daybreak.abilitywar.utils.base.minecraft;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Random;

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
				.withColor(colors[random.nextInt(colors.length)])
				.withFade(colors[random.nextInt(colors.length)])
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

	public static Firework spawnWinnerFirework(final Location location) {
		Firework firework = location.getWorld().spawn(location, Firework.class);
		FireworkMeta fireworkMeta = firework.getFireworkMeta();
		fireworkMeta.addEffect(FireworkEffect.builder()
				.flicker(true)
				.withColor(new Color[]{Color.YELLOW, Color.RED}[random.nextInt(2)])
				.withFade(new Color[]{Color.WHITE, Color.YELLOW, Color.RED, Color.ORANGE}[random.nextInt(4)])
				.with(new Type[]{Type.STAR, Type.BALL_LARGE, Type.BURST}[random.nextInt(3)])
				.trail(true)
				.build());
		fireworkMeta.setPower(random.nextInt(3) + 1);
		firework.setFireworkMeta(fireworkMeta);
		return firework;
	}

}