package daybreak.abilitywar.utils.base.minecraft;

public class SkinInfo {

	private final String name, value, signature;

	public SkinInfo(final String name, final String value, final String signature) {
		this.name = name;
		this.value = value;
		this.signature = signature;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public String getSignature() {
		return signature;
	}

}
