package daybreak.abilitywar.addon.exception;

public class InvalidAddonException extends RuntimeException {

	public InvalidAddonException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public InvalidAddonException(String msg) {
		super(msg);
	}

}
