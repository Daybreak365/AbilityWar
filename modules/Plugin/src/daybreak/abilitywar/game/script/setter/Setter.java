package daybreak.abilitywar.game.script.setter;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.game.script.ScriptWizard;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.EventExecutor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Setter
 *
 * @author Daybreak 새벽
 */
public abstract class Setter<T> implements EventExecutor {

	private final String key;
	private T value;
	private final ScriptWizard wizard;

	public Setter(String key, T defaultValue, ScriptWizard wizard) {
		this.key = key;
		this.value = defaultValue;
		this.wizard = wizard;
	}

	public String getKey() {
		return key;
	}

	public T getValue() {
		return value;
	}

	protected void setValue(T value) {
		this.value = value;
		updateGUI();
	}

	protected ScriptWizard getWizard() {
		return wizard;
	}

	public Class<?> getClazz() {
		if (getValue().getClass().getSuperclass().isEnum()) {
			return getValue().getClass().getSuperclass();
		} else {
			return getValue().getClass();
		}
	}

	protected void registerEvent(Class<? extends Event> event) {
		Bukkit.getPluginManager().registerEvent(event, wizard, EventPriority.HIGH, this, AbilityWar.getPlugin());
	}

	protected void updateGUI() {
		wizard.openScriptWizard(wizard.getPlayerPage());
	}

	public abstract void onClick(ClickType click);

	public abstract ItemStack getItem();

	private static final Map<Class<?>, Class<? extends Setter<?>>> setterMap = new HashMap<>();

	public static void registerSetter(Class<?> clazz, Class<? extends Setter<?>> setterClass) {
		setterMap.putIfAbsent(clazz, setterClass);
	}

	static {
		registerSetter(Location.class, LocationSetter.class);
	}

	public static Setter<?> newInstance(Class<?> clazz, String key, Object defaultValue, ScriptWizard wizard) throws IllegalArgumentException {
		try {
			if (!clazz.isEnum()) {
				if (setterMap.containsKey(clazz)) {
					Class<? extends Setter<?>> setterClass = setterMap.get(clazz);
					Constructor<? extends Setter<?>> constructor = setterClass.getConstructor(String.class, clazz, ScriptWizard.class);
					return constructor.newInstance(key, defaultValue, wizard);
				}
			} else {
				Object[] enumConstants = clazz.getEnumConstants();
				if (enumConstants.length < 1) {
					throw new IllegalStateException(clazz.getName() + "에 최소 1개의 상수가 있어야 합니다.");
				}
				return new EnumSetter(key, clazz.getEnumConstants()[0], wizard);
			}
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		throw new IllegalArgumentException();
	}

}
