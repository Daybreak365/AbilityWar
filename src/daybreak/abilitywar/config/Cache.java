package daybreak.abilitywar.config;

public class Cache {

	private final boolean isModifiedValue;
	private final Object value;

	public Cache(boolean isModifiedValue, Object value) {
		this.isModifiedValue = isModifiedValue;
		this.value = value;
	}

	public boolean isModifiedValue() {
		return isModifiedValue;
	}

	public Object getValue() {
		return value;
	}

}