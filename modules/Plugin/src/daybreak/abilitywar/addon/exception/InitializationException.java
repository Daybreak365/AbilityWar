package daybreak.abilitywar.addon.exception;

public class InitializationException extends RuntimeException {

	public InitializationException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public InitializationException(String msg) {
		super(msg);
	}

	public InitializationException(Throwable cause) {
		super(cause);
	}

}
