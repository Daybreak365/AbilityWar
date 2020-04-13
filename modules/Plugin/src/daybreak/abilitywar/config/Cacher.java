package daybreak.abilitywar.config;

public interface Cacher {

	Object toCache(Object object);

	Object revertCache(Object object);

}
