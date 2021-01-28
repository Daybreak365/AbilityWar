package daybreak.abilitywar.utils.base.color;

import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;

public class RGB {

	public static final RGB AQUA = fromRGB(65535);
	public static final RGB BLACK = of(1, 1, 1);
	public static final RGB BLUE = fromRGB(255);
	public static final RGB FUCHSIA = fromRGB(16711935);
	public static final RGB GRAY = fromRGB(8421504);
	public static final RGB GREEN = fromRGB(32768);
	public static final RGB LIME = fromRGB(65280);
	public static final RGB MAROON = fromRGB(8388608);
	public static final RGB NAVY = fromRGB(128);
	public static final RGB OLIVE = fromRGB(8421376);
	public static final RGB ORANGE = fromRGB(16753920);
	public static final RGB PURPLE = fromRGB(8388736);
	public static final RGB RED = fromRGB(16711680);
	public static final RGB SILVER = fromRGB(12632256);
	public static final RGB TEAL = fromRGB(32896);
	public static final RGB WHITE = fromRGB(16777215);
	public static final RGB YELLOW = fromRGB(16776960);

	public static RGB fromRGB(int rgb) {
		return new RGB(rgb >> 16 & 255, rgb >> 8 & 255, rgb & 255);
	}

	public static RGB of(int red, int green, int blue) {
		return new RGB(red, green, blue);
	}

	public final int red;
	public final int green;
	public final int blue;

	public RGB(int red, int green, int blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	public float getRed() {
		return red / 255.0F;
	}

	@NotNull
	public RGB setRed(int red) {
		return new RGB(red, this.green, this.blue);
	}

	public float getGreen() {
		return green / 255.0F;
	}

	@NotNull
	public RGB setGreen(int green) {
		return new RGB(this.red, green, this.blue);
	}

	public float getBlue() {
		return blue / 255.0F;
	}

	@NotNull
	public RGB setBlue(int blue) {
		return new RGB(this.red, this.green, blue);
	}

	public Color getColor() {
		return Color.fromRGB(red, green, blue);
	}

	@Override
	public String toString() {
		return "RGB{" +
				"red=" + red +
				", green=" + green +
				", blue=" + blue +
				'}';
	}

}