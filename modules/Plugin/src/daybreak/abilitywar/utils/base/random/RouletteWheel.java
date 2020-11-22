package daybreak.abilitywar.utils.base.random;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

public class RouletteWheel {

	private final Random random = new Random();
	private final LinkedList<Slice> slices = new LinkedList<>();
	private final List<Slice> slicesView = Collections.unmodifiableList(slices);
	private int sum = 0;

	public RouletteWheel() {}

	public RouletteWheel(final int... initialWeights) {
		for (final int initialWeight : initialWeights) {
			new Slice(initialWeight);
		}
	}

	public Slice newSlice(final int initialWeight) {
		return new Slice(initialWeight);
	}

	public Slice newSlice() {
		return newSlice(1);
	}

	public Slice select() {
		if (slices.isEmpty()) throw new NoSuchElementException("There must be at least one slice.");
		final int r = random.nextInt(sum) + 1;
		int current = 0;
		for (Slice slice : slices) {
			current += slice.weight;
			if (r <= current) {
				return slice;
			}
		}
		return slices.getLast();
	}

	public List<Slice> getSlices() {
		return slicesView;
	}

	public int getSum() {
		return sum;
	}

	public class Slice {

		private final int initialWeight;
		private int weight = 0;

		private Slice(final int initialWeight) {
			if (initialWeight < 0) throw new IllegalArgumentException("initialWeight must be at least 0.");
			this.initialWeight = initialWeight;
			slices.add(this);
			increaseWeight(initialWeight);
		}

		public void increaseWeight(final int amount) {
			RouletteWheel.this.sum += amount;
			this.weight += amount;
		}

		public void increaseWeight() {
			this.increaseWeight(1);
		}

		public void decreaseWeight(final int amount) {
			if (this.weight - amount < 1) {
				RouletteWheel.this.sum -= weight - 1;
				this.weight = 1;
			} else {
				RouletteWheel.this.sum -= amount;
				this.weight -= amount;
			}
		}

		public void decreaseWeight() {
			this.decreaseWeight(1);
		}

		public void resetWeight() {
			RouletteWheel.this.sum += initialWeight - weight;
			this.weight = initialWeight;
		}

		public int getWeight() {
			return weight;
		}

		public double getProportion() {
			return this.weight / (double) RouletteWheel.this.sum;
		}

	}

}
