package daybreak.abilitywar.utils.base.minecraft.server;

public class ServerNotSupportedException extends RuntimeException {

	private final ServerType[] supported;

	public ServerNotSupportedException(final ServerType[] supported) {
		this.supported = supported;
	}

	public ServerType[] getSupported() {
		return supported;
	}
}
