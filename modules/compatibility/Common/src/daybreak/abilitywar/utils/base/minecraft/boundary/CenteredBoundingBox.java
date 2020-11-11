package daybreak.abilitywar.utils.base.minecraft.boundary;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class CenteredBoundingBox extends BoundingBox {

	public static @NotNull CenteredBoundingBox of(@NotNull Location a, @NotNull Location b) {
		final Vector delta = b.toVector().subtract(a.toVector());
		return new CenteredBoundingBox(a.toVector(), 0, 0, 0, delta.getX(), delta.getY(), delta.getZ());
	}

	public static @NotNull CenteredBoundingBox of(@NotNull Location center, double x1, double y1, double z1, double x2, double y2, double z2) {
		return new CenteredBoundingBox(center.toVector(), x1, y1, z1, x2, y2, z2);
	}

	public static @NotNull CenteredBoundingBox of(@NotNull Vector center, double x1, double y1, double z1, double x2, double y2, double z2) {
		return new CenteredBoundingBox(center, x1, y1, z1, x2, y2, z2);
	}

	private @NotNull Vector center;

	public CenteredBoundingBox(@NotNull Vector center, double x1, double y1, double z1, double x2, double y2, double z2) {
		super(x1, y1, z1, x2, y2, z2);
		this.center = center;
	}

	@Override
	public @NotNull CenteredBoundingBox resize(double x1, double y1, double z1, double x2, double y2, double z2) {
		super.resize(x1, y1, z1, x2, y2, z2);
		return this;
	}

	@Override
	public @NotNull CenteredBoundingBox copy() {
		return new CenteredBoundingBox(center, this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
	}

	@Override
	public @NotNull Vector getCenter() {
		return center;
	}

	public void setCenter(@NotNull Vector center) {
		center.checkFinite();
		this.center = center;
	}

	public void setCenter(@NotNull Location location) {
		this.setCenter(location.toVector());
	}

	@Override
	public @NotNull CenteredBoundingBox expand(double negativeX, double negativeY, double negativeZ, double positiveX, double positiveY, double positiveZ) {
		super.expand(negativeX, negativeY, negativeZ, positiveX, positiveY, positiveZ);
		return this;
	}

	@Override
	public @NotNull CenteredBoundingBox expand(double x, double y, double z) {
		super.expand(x, y, z);
		return this;
	}

	@Override
	public @NotNull CenteredBoundingBox expand(double expansion) {
		super.expand(expansion);
		return this;
	}

	@Override
	public @NotNull CenteredBoundingBox expand(double dirX, double dirY, double dirZ, double expansion) {
		super.expand(dirX, dirY, dirZ, expansion);
		return this;
	}

	@Override
	public @NotNull CenteredBoundingBox expand(@NotNull Vector direction, double expansion) {
		super.expand(direction, expansion);
		return this;
	}

	@Override
	public @NotNull CenteredBoundingBox expand(@NotNull BlockFace blockFace, double expansion) {
		super.expand(blockFace, expansion);
		return this;
	}

	@Override
	public @NotNull CenteredBoundingBox expandDirectional(double dirX, double dirY, double dirZ) {
		super.expandDirectional(dirX, dirY, dirZ);
		return this;
	}

	@Override
	public @NotNull CenteredBoundingBox expandDirectional(@NotNull Vector direction) {
		super.expandDirectional(direction);
		return this;
	}

	@Override
	public @NotNull CenteredBoundingBox shift(double shiftX, double shiftY, double shiftZ) {
		super.shift(shiftX, shiftY, shiftZ);
		return this;
	}

	@Override
	public @NotNull CenteredBoundingBox shift(@NotNull Vector shift) {
		super.shift(shift);
		return this;
	}
}
