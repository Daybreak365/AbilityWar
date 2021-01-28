package daybreak.abilitywar.utils.base.math.geometry;

import com.google.common.collect.AbstractIterator;
import daybreak.abilitywar.utils.base.math.geometry.ImageVector.Point2D;
import daybreak.abilitywar.utils.base.color.RGB;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ImageVector implements Iterable<Point2D> {

	@NotNull
	public static ImageVector parse(final InputStream inputStream) {
		try {
			return new ImageVector(inputStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private final BufferedImage image;
	private double offset = .1;
	private final Set<Integer> transparent = new HashSet<>();

	private ImageVector(final InputStream inputStream) throws IOException {
		this.image = ImageIO.read(inputStream);
	}

	@NotNull
	public ImageVector setOffset(final double offset) {
		this.offset = offset;
		return this;
	}

	@NotNull
	public ImageVector addTransparent(final int transparent) {
		this.transparent.add(transparent);
		return this;
	}

	@NotNull
	@Override
	public Iterator<Point2D> iterator() {
		return new ImageIterator(offset, transparent);
	}

	private class ImageIterator extends AbstractIterator<Point2D> {

		private final double offset;
		private final Set<Integer> transparent;
		private final int maxCursor = image.getWidth() * image.getHeight() - 1;
		private int cursor = -1;

		private ImageIterator(final double offset, final Set<Integer> transparent) {
			this.offset = offset;
			this.transparent = transparent;
		}

		@Override
		protected Point2D computeNext() {
			if (cursor + 1 <= maxCursor) {
				final int current = ++cursor;
				final int x = current % image.getWidth(), y = current / image.getWidth();
				final int rgb = image.getRGB(x, y);
				if (transparent.contains(rgb)) {
					return computeNext();
				}
				return new Point2D((offset * x) - ((image.getWidth() * offset) / 2.0), offset * (image.getHeight() - y) - (offset * (image.getWidth() / 2.0)), 0, RGB.fromRGB(rgb));
			} else {
				endOfData();
				return null;
			}
		}

	}

	public static class Point2D extends Vector {

		private final RGB color;

		private Point2D(double x, double y, double z, RGB color) {
			super(x, y, z);
			this.color = color;
		}

		public RGB getColor() {
			return color;
		}

	}

}
