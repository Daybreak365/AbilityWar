package daybreak.abilitywar.utils.base.minecraft.version;

public class UnsupportedVersionException extends RuntimeException {

	public UnsupportedVersionException(int version) {
		super("지원되지 않는 버전: v1.".concat(String.valueOf(version)));
	}

}
