package daybreak.abilitywar.utils.math.geometry;

import daybreak.abilitywar.utils.math.LocationUtil;
import daybreak.abilitywar.utils.math.VectorUtil.Vectors;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import static daybreak.abilitywar.utils.base.Precondition.checkNotNull;

public class Line {

	private Vector vector;
	private int locationAmount = 10;

	public Line(Location startLocation, Location targetLocation) {
		this.vector = checkNotNull(targetLocation).toVector().subtract(checkNotNull(startLocation).toVector());
	}

	public Line(Vector vector) {
		this.vector = checkNotNull(vector);
	}

	public Line setVector(Location startLocation, Location targetLocation) {
		this.vector = checkNotNull(targetLocation).toVector().subtract(checkNotNull(startLocation).toVector());
		return this;
	}

	public Line setVector(Vector vector) {
		this.vector = checkNotNull(vector);
		return this;
	}

	public Line setLocationAmount(int locationAmount) {
		if (locationAmount < 1) {
			throw new IllegalArgumentException("locationAmount는 자연수로 설정되어야 합니다.");
		}
		this.locationAmount = locationAmount;
		return this;
	}

	/**
	 * @param index index가 0이면 StartLocation과 동일한 위치, index가 LocationAmount면 TargetLocation과 동일한 위치를 반환합니다.
	 * @throws IndexOutOfBoundsException index에 범위 외의 수가 입력되었을 경우
	 * @return index 번째 위치
	 */
	public Location getLocation(Location startLocation, int index) throws IndexOutOfBoundsException {
		if (index <= locationAmount) {
			return startLocation.toVector().clone().add(vector.clone().multiply(Math.min((1.0 / locationAmount) * index, 1.0))).toLocation(startLocation.getWorld());
		} else {
			throw new IndexOutOfBoundsException("index는 0과 " + locationAmount + " 사이의 수가 입력되어야 합니다.");
		}
	}

	public LocationUtil.Locations getLocations(Location startLocation) {
		LocationUtil.Locations locations = new LocationUtil.Locations();
		final double increasement = 1.0 / locationAmount;
		for (int i = 0; i <= locationAmount; i++) {
			locations.add(startLocation.toVector().clone().add(vector.clone().multiply(Math.min(increasement * i, 1.0))).toLocation(startLocation.getWorld()));
		}
		return locations;
	}

	public Vectors getVectors() {
		Vectors vectors = new Vectors();
		final double increasement = 1.0 / locationAmount;
		for (int i = 0; i <= locationAmount; i++) {
			vectors.add(vector.clone().multiply(Math.min(increasement * i, 1.0)));
		}
		return vectors;
	}

}
