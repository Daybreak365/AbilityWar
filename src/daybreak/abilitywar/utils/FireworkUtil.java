package daybreak.abilitywar.utils;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Random;

public class FireworkUtil {

	private FireworkUtil() {
	}

	private static final Random random = new Random();

	public static void spawnRandomFirework(final Location location) {
		Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
		FireworkMeta fireworkMeta = firework.getFireworkMeta();
		fireworkMeta.addEffect(FireworkEffect.builder()
				.flicker(random.nextBoolean())
				.withColor(getColor(random.nextInt(17)))
				.withFade(getColor(random.nextInt(17)))
				.with(Type.values()[random.nextInt(Type.values().length)])
				.trail(random.nextBoolean())
				.build());
		fireworkMeta.setPower(1 + random.nextInt(2));
		firework.setFireworkMeta(fireworkMeta);
	}

	public static void spawnWinnerFirework(final Location loc) {
		Firework firework = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
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
	}

	private static Color getColor(int color) {
		switch (color) {
			case 0:
				return Color.AQUA;
			case 1:
				return Color.BLACK;
			case 2:
				return Color.BLUE;
			case 3:
				return Color.FUCHSIA;
			case 4:
				return Color.GRAY;
			case 5:
				return Color.GREEN;
			case 6:
				return Color.LIME;
			case 7:
				return Color.MAROON;
			case 8:
				return Color.NAVY;
			case 9:
				return Color.OLIVE;
			case 10:
				return Color.ORANGE;
			case 11:
				return Color.PURPLE;
			case 12:
				return Color.RED;
			case 13:
				return Color.SILVER;
			case 14:
				return Color.TEAL;
			case 15:
				return Color.WHITE;
			case 16:
				return Color.YELLOW;
			default:
				return null;
		}
	}
}