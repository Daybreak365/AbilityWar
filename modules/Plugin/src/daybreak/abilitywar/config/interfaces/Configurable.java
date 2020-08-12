package daybreak.abilitywar.config.interfaces;

public interface Configurable<T> {

	String getKey();
	String getPath();
	String[] getComments();
	boolean condition(T value);
	T getDefaultValue();
	T getValue();
	boolean setValue(T value);
	void reset();

}
