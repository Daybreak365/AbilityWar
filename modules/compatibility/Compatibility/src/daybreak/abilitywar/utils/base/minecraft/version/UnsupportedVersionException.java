package daybreak.abilitywar.utils.base.minecraft.version;

import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion.Version;

public class UnsupportedVersionException extends RuntimeException {

	public UnsupportedVersionException() {
		super();
	}

	public UnsupportedVersionException(Version version) {
		super("지원되지 않는 버전: ".concat(version.name()));
	}

}
