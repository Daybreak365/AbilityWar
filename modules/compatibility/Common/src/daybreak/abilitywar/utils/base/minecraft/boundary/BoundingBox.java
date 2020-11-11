package daybreak.abilitywar.utils.base.minecraft.boundary;

import com.google.common.base.Preconditions;
import daybreak.abilitywar.utils.base.minecraft.boundary.Boundary.BoundaryData;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BoundingBox {

	protected double minX, minY, minZ, maxX, maxY, maxZ;

	public BoundingBox(double x1, double y1, double z1, double x2, double y2, double z2) {
		resize(x1, y1, z1, x2, y2, z2);
	}

	@NotNull
	public BoundingBox resize(double x1, double y1, double z1, double x2, double y2, double z2) {
		NumberConversions.checkFinite(x1, "x1 not finite");
		NumberConversions.checkFinite(y1, "y1 not finite");
		NumberConversions.checkFinite(z1, "z1 not finite");
		NumberConversions.checkFinite(x2, "x2 not finite");
		NumberConversions.checkFinite(y2, "y2 not finite");
		NumberConversions.checkFinite(z2, "z2 not finite");
		this.minX = Math.min(x1, x2);
		this.minY = Math.min(y1, y2);
		this.minZ = Math.min(z1, z2);
		this.maxX = Math.max(x1, x2);
		this.maxY = Math.max(y1, y2);
		this.maxZ = Math.max(z1, z2);
		return this;
	}

	public abstract @NotNull BoundingBox copy();

	public @NotNull Vector getMin() {
		final Vector center = getCenter();
		return new Vector(center.getX() + minX, center.getY() + minY, center.getZ() + minZ);
	}

	public @NotNull Vector getMax() {
		final Vector center = getCenter();
		return new Vector(center.getX() + maxX, center.getY() + maxY, center.getZ() + maxZ);
	}

	public double getMinX() {
		return getCenter().getX() + this.minX;
	}

	public double getMinY() {
		return getCenter().getY() + this.minY;
	}

	public double getMinZ() {
		return getCenter().getZ() + this.minZ;
	}

	public double getMaxX() {
		return getCenter().getX() + this.maxX;
	}

	public double getMaxY() {
		return getCenter().getY() + this.maxY;
	}

	public double getMaxZ() {
		return getCenter().getZ() + this.maxZ;
	}

	public double getHeight() {
		return this.maxY - this.minY;
	}

	public double getWidthX() {
		return this.maxX - this.minX;
	}

	public double getWidthZ() {
		return this.maxZ - this.minZ;
	}

	public double getVolume() {
		return this.getHeight() * this.getWidthX() * this.getWidthZ();
	}

	public abstract @NotNull Vector getCenter();

	public boolean conflicts(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		final Vector min = getMin(), max = getMax();
		return min.getX() < maxX && minX < max.getX() && min.getY() < maxY && minY < max.getY() && min.getZ() < maxZ && minZ < max.getZ();
	}

	public boolean conflicts(@NotNull BoundingBox other) {
		final Vector aMin = getMin(), aMax = getMax(), bMin = other.getMin(), bMax = other.getMax();
		return aMin.getX() < bMax.getX() && bMin.getX() < aMax.getX() && aMin.getY() < bMax.getY() && bMin.getY() < aMax.getY() && aMin.getZ() < bMax.getZ() && bMin.getZ() < aMax.getZ();
	}

	public boolean conflicts(@NotNull final Entity entity) {
		final BoundaryData data = BoundaryData.of(entity.getType());
		final Location loc = entity.getLocation();
		final Vector center = getCenter();
		return loc.getX() + data.getMinX() < center.getX() + this.maxX && center.getX() + this.minX < loc.getX() + data.getMaxX() && loc.getY() + data.getMinY() < center.getY() + this.maxY &&
				center.getY() + this.minY < loc.getY() + data.getMaxY() && loc.getZ() + data.getMinZ() < center.getZ() + this.maxZ && center.getZ() + this.minZ < loc.getZ() + data.getMaxZ();
	}

	public boolean contains(double x, double y, double z) {
		final Vector min = getMin(), max = getMax();
		return x >= min.getX() && x < max.getX() && y >= min.getY() && y < max.getY() && z >= min.getZ() && z < max.getZ();
	}

	public boolean contains(@NotNull Vector position) {
		Preconditions.checkNotNull(position, "Position is null");
		return this.contains(position.getX(), position.getY(), position.getZ());
	}

	private boolean contains(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		final Vector min = getMin(), max = getMax();
		return min.getX() <= minX && max.getX() >= maxX && min.getY() <= minY && max.getY() >= maxY && min.getZ() <= minZ && max.getZ() >= maxZ;
	}

	public boolean contains(@NotNull BoundingBox other) {
		Preconditions.checkNotNull(other, "Other bounding box is null");
		return this.contains(other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ);
	}

	/**
	 * 이 BoundingBox와 선분의 교차점을 계산합니다.
	 * @param start 시작 위치
	 * @param direction 방향
	 * @param maxDistance 최대 거리
	 * @return BoundingBox와 선분의 교차점, 교차하지 않을 경우 null
	 */
	@Nullable
	public Vector rayTrace(@NotNull Vector start, @NotNull Vector direction, double maxDistance) {
		start.checkFinite();
		direction.checkFinite();
		if (maxDistance < 0.0D) return null;

		final Vector center = getCenter();
		final double startX = start.getX(), startY = start.getY(), startZ = start.getZ();
		final double minX = center.getX() + this.minX, minY = center.getY() + this.minY, minZ = center.getZ() + this.minZ;
		final double maxX = center.getX() + this.maxX, maxY = center.getY() + this.maxY, maxZ = center.getZ() + this.maxZ;
		direction = direction.clone().normalize();
		final double dirX = direction.getX(), dirY = direction.getY(), dirZ = direction.getZ(), divX = 1.0D / dirX, divY = 1.0D / dirY, divZ = 1.0D / dirZ;
		double tMin = ((dirX >= 0 ? minX : maxX) - startX) * divX, tMax = ((dirX >= 0 ? maxX : minX) - startX) * divX;
		final double tyMin = ((dirY >= 0 ? minY : maxY) - startY) * divY, tyMax = ((dirY >= 0 ? maxY : minY) - startY) * divY;

		if (tMin <= tyMax && tMax >= tyMin) {
			if (tyMin > tMin) tMin = tyMin;
			if (tyMax < tMax) tMax = tyMax;
			final double tzMin = ((divZ >= 0 ? minZ : maxZ) - startZ) * divZ, tzMax = ((divZ >= 0 ? maxZ : minZ) - startZ) * divZ;
			if (tMin <= tzMax && tMax >= tzMin) {
				if (tzMin > tMin) tMin = tzMin;
				if (tzMax < tMax) tMax = tzMax;

				if (tMax < 0.0D) return null;
				else if (tMin > maxDistance) return null;

				double t;
				if (tMin < 0.0D) t = tMax;
				else t = tMin;
				return direction.multiply(t).add(start);
			} else return null;
		} else return null;
	}

	/**
	 * 이 BoundingBox와 선분의 교차점을 계산합니다.
	 * @param start 시작 위치
	 * @param direction 방향
	 * @return BoundingBox와 선분의 교차점, 교차하지 않을 경우 null
	 */
	@Nullable
	public Vector rayTrace(@NotNull Vector start, @NotNull Vector direction) {
		start.checkFinite();
		direction.checkFinite();

		final Vector center = getCenter();
		final double startX = start.getX(), startY = start.getY(), startZ = start.getZ();
		final double minX = center.getX() + this.minX, minY = center.getY() + this.minY, minZ = center.getZ() + this.minZ;
		final double maxX = center.getX() + this.maxX, maxY = center.getY() + this.maxY, maxZ = center.getZ() + this.maxZ;
		direction = direction.clone().normalize();
		final double dirX = direction.getX(), dirY = direction.getY(), dirZ = direction.getZ(), divX = 1.0D / dirX, divY = 1.0D / dirY, divZ = 1.0D / dirZ;
		double tMin = ((dirX >= 0 ? minX : maxX) - startX) * divX, tMax = ((dirX >= 0 ? maxX : minX) - startX) * divX;
		final double tyMin = ((dirY >= 0 ? minY : maxY) - startY) * divY, tyMax = ((dirY >= 0 ? maxY : minY) - startY) * divY;

		if (tMin <= tyMax && tMax >= tyMin) {
			if (tyMin > tMin) tMin = tyMin;
			if (tyMax < tMax) tMax = tyMax;
			final double tzMin = ((divZ >= 0 ? minZ : maxZ) - startZ) * divZ, tzMax = ((divZ >= 0 ? maxZ : minZ) - startZ) * divZ;
			if (tMin <= tzMax && tMax >= tzMin) {
				if (tzMin > tMin) tMin = tzMin;
				if (tzMax < tMax) tMax = tzMax;

				if (tMax < 0.0D) return null;

				double t;
				if (tMin < 0.0D) t = tMax;
				else t = tMin;
				return direction.multiply(t).add(start);
			} else return null;
		} else return null;
	}

	@NotNull
	public BoundingBox expand(double negativeX, double negativeY, double negativeZ, double positiveX, double positiveY, double positiveZ) {
		if (negativeX == 0.0D && negativeY == 0.0D && negativeZ == 0.0D && positiveX == 0.0D && positiveY == 0.0D && positiveZ == 0.0D) {
			return this;
		}
		double newMinX = minX - negativeX, newMinY = minY - negativeY, newMinZ = minZ - negativeZ, newMaxX = maxX + positiveX, newMaxY = maxY + positiveY, newMaxZ = maxZ + positiveZ;
		final Vector center = getCenter();

		if (newMinX > newMaxX) {
			double centerX = center.getX();
			if (newMaxX >= centerX) {
				newMinX = newMaxX;
			} else if (newMinX <= centerX) {
				newMaxX = newMinX;
			} else {
				newMinX = centerX;
				newMaxX = centerX;
			}
		}
		if (newMinY > newMaxY) {
			double centerY = center.getY();
			if (newMaxY >= centerY) {
				newMinY = newMaxY;
			} else if (newMinY <= centerY) {
				newMaxY = newMinY;
			} else {
				newMinY = centerY;
				newMaxY = centerY;
			}
		}
		if (newMinZ > newMaxZ) {
			double centerZ = center.getZ();
			if (newMaxZ >= centerZ) {
				newMinZ = newMaxZ;
			} else if (newMinZ <= centerZ) {
				newMaxZ = newMinZ;
			} else {
				newMinZ = centerZ;
				newMaxZ = centerZ;
			}
		}

		this.minX = newMinX;
		this.minY = newMinY;
		this.minZ = newMinZ;
		this.maxX = newMaxX;
		this.maxY = newMaxY;
		this.maxZ = newMaxZ;
		return this;
	}

	@NotNull
	public BoundingBox expand(double x, double y, double z) {
		return this.expand(x, y, z, x, y, z);
	}

	@NotNull
	public BoundingBox expand(double expansion) {
		return this.expand(expansion, expansion, expansion, expansion, expansion, expansion);
	}

	@NotNull
	public BoundingBox expand(double dirX, double dirY, double dirZ, double expansion) {
		if (expansion == 0.0D) {
			return this;
		} else if (dirX == 0.0D && dirY == 0.0D && dirZ == 0.0D) {
			return this;
		} else {
			double negativeX = dirX < 0.0D ? -dirX * expansion : 0.0D;
			double negativeY = dirY < 0.0D ? -dirY * expansion : 0.0D;
			double negativeZ = dirZ < 0.0D ? -dirZ * expansion : 0.0D;
			double positiveX = dirX > 0.0D ? dirX * expansion : 0.0D;
			double positiveY = dirY > 0.0D ? dirY * expansion : 0.0D;
			double positiveZ = dirZ > 0.0D ? dirZ * expansion : 0.0D;
			return this.expand(negativeX, negativeY, negativeZ, positiveX, positiveY, positiveZ);
		}
	}

	@NotNull
	public BoundingBox expand(@NotNull Vector direction, double expansion) {
		Preconditions.checkNotNull(direction, "Direction is null");
		return this.expand(direction.getX(), direction.getY(), direction.getZ(), expansion);
	}

	@NotNull
	public BoundingBox expand(@NotNull BlockFace blockFace, double expansion) {
		Preconditions.checkNotNull(blockFace, "Block face is null");
		return blockFace == BlockFace.SELF ? this : this.expand(blockFace.getDirection(), expansion);
	}

	@NotNull
	public BoundingBox expandDirectional(double dirX, double dirY, double dirZ) {
		return this.expand(dirX, dirY, dirZ, 1.0D);
	}
	
	@NotNull
	public BoundingBox expandDirectional(@NotNull Vector direction) {
		Preconditions.checkNotNull(direction, "Direction is null");
		return this.expand(direction.getX(), direction.getY(), direction.getZ(), 1.0D);
	}

	@NotNull
	public BoundingBox shift(double shiftX, double shiftY, double shiftZ) {
		return shiftX == 0.0D && shiftY == 0.0D && shiftZ == 0.0D ? this : this.resize(this.minX + shiftX, this.minY + shiftY, this.minZ + shiftZ, this.maxX + shiftX, this.maxY + shiftY, this.maxZ + shiftZ);
	}

	@NotNull
	public BoundingBox shift(@NotNull Vector shift) {
		Preconditions.checkNotNull(shift, "Shift is null");
		return this.shift(shift.getX(), shift.getY(), shift.getZ());
	}

	@Override
	public String toString() {
		return "BoundingBox{" + "minX=" + minX + ", minY=" + minY + ", minZ=" + minZ + ", maxX=" + maxX + ", maxY=" + maxY + ", maxZ=" + maxZ + ", center=(" + getCenter().toString() + ")}";
	}

}