package daybreak.abilitywar.config.interfaces;

public interface Node {

	String getPath();
	Object getDefault();
	boolean hasCacher();
	Cacher getCacher();
	String[] getComments();

}
