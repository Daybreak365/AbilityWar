package daybreak.abilitywar.utils.base.color;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Gradient implements Iterable<RGB> {

	public static RGB getGradientColor(final RGB a, final RGB b, final double percentage) {
		final double inverse = 1.0 - percentage;
		return new RGB(
				(int) (a.red * inverse + b.red * percentage),
				(int) (a.green * inverse + b.green * percentage),
				(int) (a.blue * inverse + b.blue * percentage)
		);
	}

	public static List<RGB> createGradient(final int colors, RGB... rgbs) {
		Preconditions.checkArgument(rgbs.length >= 2, "rgbs must have at least 2 RGBs");
		final List<RGB> gradient = new ArrayList<>(colors * rgbs.length);
		final double minusOne = colors - 1;
		RGB last = null;
		for (RGB color : rgbs) {
			if (last == null) {
				last = color;
				continue;
			}
			for (int i = 0; i < colors; i++) {
				gradient.add(getGradientColor(last, color, i / minusOne));
			}
			last = color;
		}
		return gradient;
	}

	private @NotNull RGB start, end;
	private int colors;

	public Gradient(final @NotNull RGB start, final @NotNull RGB end, final int colors) {
		this.start = start;
		this.end = end;
		setColors(colors);
	}

	public Gradient(final @NotNull RGB start, final @NotNull RGB end) {
		this(start, end, 5);
	}

	public Gradient setStart(@NotNull RGB start) {
		this.start = start;
		return this;
	}

	public Gradient setEnd(@NotNull RGB end) {
		this.end = end;
		return this;
	}

	public Gradient setColors(int colors) {
		Preconditions.checkArgument(colors >= 2, "colors must be 2 or greater");
		this.colors = colors;
		return this;
	}

	@NotNull
	@Override
	public Iterator<RGB> iterator() {
		return new GradientIterator();
	}

	private class GradientIterator implements Iterator<RGB> {

		private final RGB start, end;
		private final int colors;
		private final double division;
		private int cursor = -1;

		private GradientIterator() {
			this.start = Gradient.this.start;
			this.end = Gradient.this.end;
			this.colors = Gradient.this.colors;
			this.division = 1.0 / (colors - 1);
		}

		@Override
		public boolean hasNext() {
			return cursor + 1 <= colors - 1;
		}

		@Override
		public RGB next() {
			return getGradientColor(start, end, division * ++cursor);
		}
	}

}
