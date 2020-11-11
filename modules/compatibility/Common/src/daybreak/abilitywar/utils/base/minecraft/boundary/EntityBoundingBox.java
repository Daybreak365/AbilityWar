package daybreak.abilitywar.utils.base.minecraft.boundary;

import com.google.common.base.Preconditions;
import daybreak.abilitywar.utils.base.minecraft.boundary.Boundary.BoundaryData;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class EntityBoundingBox extends BoundingBox {

	public static @NotNull EntityBoundingBox of(@NotNull Entity entity) {
		final BoundaryData data = BoundaryData.of(Preconditions.checkNotNull(entity).getType());
		return new EntityBoundingBox(entity, data.minX, data.minY, data.minZ, data.maxX, data.maxY, data.maxZ);
	}

	private final Entity entity;

	public EntityBoundingBox(@NotNull Entity entity, double x1, double y1, double z1, double x2, double y2, double z2) {
		super(x1, y1, z1, x2, y2, z2);
		this.entity = entity;
	}

	@Override
	public @NotNull EntityBoundingBox resize(double x1, double y1, double z1, double x2, double y2, double z2) {
		super.resize(x1, y1, z1, x2, y2, z2);
		return this;
	}

	@Override
	public @NotNull EntityBoundingBox copy() {
		return new EntityBoundingBox(entity, this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
	}

	@Override
	public @NotNull Vector getCenter() {
		return entity.getLocation().toVector();
	}

	@Override
	public @NotNull EntityBoundingBox expand(double negativeX, double negativeY, double negativeZ, double positiveX, double positiveY, double positiveZ) {
		super.expand(negativeX, negativeY, negativeZ, positiveX, positiveY, positiveZ);
		return this;
	}

	@Override
	public @NotNull EntityBoundingBox expand(double x, double y, double z) {
		super.expand(x, y, z);
		return this;
	}

	@Override
	public @NotNull EntityBoundingBox expand(double expansion) {
		super.expand(expansion);
		return this;
	}

	@Override
	public @NotNull EntityBoundingBox expand(double dirX, double dirY, double dirZ, double expansion) {
		super.expand(dirX, dirY, dirZ, expansion);
		return this;
	}

	@Override
	public @NotNull EntityBoundingBox expand(@NotNull Vector direction, double expansion) {
		super.expand(direction, expansion);
		return this;
	}

	@Override
	public @NotNull EntityBoundingBox expand(@NotNull BlockFace blockFace, double expansion) {
		super.expand(blockFace, expansion);
		return this;
	}

	@Override
	public @NotNull EntityBoundingBox expandDirectional(double dirX, double dirY, double dirZ) {
		super.expandDirectional(dirX, dirY, dirZ);
		return this;
	}

	@Override
	public @NotNull EntityBoundingBox expandDirectional(@NotNull Vector direction) {
		super.expandDirectional(direction);
		return this;
	}

	@Override
	public @NotNull EntityBoundingBox shift(double shiftX, double shiftY, double shiftZ) {
		super.shift(shiftX, shiftY, shiftZ);
		return this;
	}

	@Override
	public @NotNull EntityBoundingBox shift(@NotNull Vector shift) {
		super.shift(shift);
		return this;
	}
}
