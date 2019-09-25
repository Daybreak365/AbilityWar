package daybreak.abilitywar.utils;
import java.util.Random;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

public class FireworkUtil {

	public static void spawnRandomFirework(final Location loc) {
		final Firework firework = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
		final FireworkMeta fireworkMeta = firework.getFireworkMeta();
		final Random random = new Random();
		final FireworkEffect effect = FireworkEffect.builder().flicker(random.nextBoolean()).withColor(getColor(random.nextInt(17) + 1)).withFade(getColor(random.nextInt(17) + 1)).with(Type.values()[random.nextInt(Type.values().length)]).trail(random.nextBoolean()).build();
		fireworkMeta.addEffect(effect);
		fireworkMeta.setPower(random.nextInt(2) + 1);
		firework.setFireworkMeta(fireworkMeta);
	}

	public static void spawnWinnerFirework(final Location loc) {
		final Firework firework = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
		final FireworkMeta fireworkMeta = firework.getFireworkMeta();
		final Random random = new Random();
		final FireworkEffect effect = FireworkEffect.builder().flicker(true).withColor(new Color[]{Color.YELLOW, Color.RED}[random.nextInt(2)]).withFade(new Color[]{Color.WHITE, Color.YELLOW, Color.RED, Color.ORANGE}[random.nextInt(4)]).with(new Type[]{Type.STAR, Type.BALL_LARGE, Type.BURST}[random.nextInt(3)]).trail(true).build();
		fireworkMeta.addEffect(effect);
		fireworkMeta.setPower(random.nextInt(3) + 1);
		firework.setFireworkMeta(fireworkMeta);
	}

	private static Color getColor(final int i) {
		switch (i) {
		case 1:
			return Color.AQUA;
		case 2:
			return Color.BLACK;
		case 3:
			return Color.BLUE;
		case 4:
			return Color.FUCHSIA;
		case 5:
			return Color.GRAY;
		case 6:
			return Color.GREEN;
		case 7:
			return Color.LIME;
		case 8:
			return Color.MAROON;
		case 9:
			return Color.NAVY;
		case 10:
			return Color.OLIVE;
		case 11:
			return Color.ORANGE;
		case 12:
			return Color.PURPLE;
		case 13:
			return Color.RED;
		case 14:
			return Color.SILVER;
		case 15:
			return Color.TEAL;
		case 16:
			return Color.WHITE;
		case 17:
			return Color.YELLOW;
		}
		return null;
	}
}