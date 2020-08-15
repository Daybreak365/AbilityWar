package daybreak.abilitywar.patch;

public interface Patchable {

	boolean isValid();
	boolean isApplied();
	void apply();
	String getName();

}
