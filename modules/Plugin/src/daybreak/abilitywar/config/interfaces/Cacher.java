package daybreak.abilitywar.config.interfaces;

public interface Cacher {

	Object toCache(Object object);

	Object revertCache(Object object);

}
