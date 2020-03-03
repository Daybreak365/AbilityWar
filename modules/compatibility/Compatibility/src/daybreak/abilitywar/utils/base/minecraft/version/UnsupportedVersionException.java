package daybreak.abilitywar.utils.base.minecraft.version;

public class UnsupportedVersionException extends RuntimeException {

	public UnsupportedVersionException() {
		super();
	}

	public UnsupportedVersionException(ServerVersion.Version version) {
		super("지원되지 않는 버전: ".concat(version.name()));
	}

}
