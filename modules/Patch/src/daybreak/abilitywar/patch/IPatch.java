package daybreak.abilitywar.patch;

public interface IPatch {

	boolean condition();

	boolean isApplied();

	void apply();

	String getName();

	String getMessage();

}
