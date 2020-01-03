package daybreak.abilitywar.utils.math.geometry;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.ArrayList;

import static daybreak.abilitywar.utils.Validate.notNull;

public class Line {

	private Location startLocation;
	private Location targetLocation;
	private int locationAmount = 10;
	private boolean highestLocation = false;

	public Line(Location startLocation, Location targetLocation) {
		if (!notNull(startLocation).getWorld().equals(notNull(targetLocation).getWorld())) {
			throw new IllegalArgumentException("StartLocation과 TargetLocation이 같은 세계에 있어야 합니다.");
		}
		this.startLocation = startLocation.clone();
		this.targetLocation = targetLocation.clone();
	}

	public Line setStartLocation(Location startLocation) {
		if (!targetLocation.getWorld().equals(notNull(startLocation).getWorld())) {
			throw new IllegalArgumentException("StartLocation과 TargetLocation이 같은 세계에 있어야 합니다.");
		}
		this.startLocation = notNull(startLocation).clone();
		return this;
	}

	public Line setTargetLocation(Location targetLocation) {
		if (!startLocation.getWorld().equals(notNull(targetLocation).getWorld())) {
			throw new IllegalArgumentException("StartLocation과 TargetLocation이 같은 세계에 있어야 합니다.");
		}
		this.targetLocation = notNull(targetLocation).clone();
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
	public Location getLocation(int index) throws IndexOutOfBoundsException {
		if (index <= locationAmount) {
			return startLocation.toVector().clone().add(targetLocation.toVector().subtract(startLocation.toVector()).clone().multiply(Math.min((1.0 / locationAmount) * index, 1.0))).toLocation(startLocation.getWorld());
		} else {
			throw new IndexOutOfBoundsException("index는 0과 " + locationAmount + " 사이의 수가 입력되어야 합니다.");
		}
	}

	public ArrayList<Location> getLocations() {
		ArrayList<Location> locations = new ArrayList<>();
		World world = startLocation.getWorld();
		Vector vector = targetLocation.toVector().subtract(startLocation.toVector());
		final double increasement = 1.0 / locationAmount;
		for (int i = 0; i <= locationAmount; i++) {
			locations.add(startLocation.toVector().clone().add(vector.clone().multiply(Math.min(increasement * i, 1.0))).toLocation(world));
		}
		return locations;
	}

	public ArrayList<Vector> getVectors() {
		ArrayList<Vector> vectors = new ArrayList<>();
		Vector vector = targetLocation.toVector().subtract(startLocation.toVector());
		final double increasement = 1.0 / locationAmount;
		for (int i = 0; i <= locationAmount; i++) {
			vectors.add(vector.clone().multiply(Math.min(increasement * i, 1.0)));
		}
		return vectors;
	}

}
