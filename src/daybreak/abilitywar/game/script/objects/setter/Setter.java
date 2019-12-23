package daybreak.abilitywar.game.script.objects.setter;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.game.script.ScriptWizard;
import daybreak.abilitywar.utils.Validate;
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

/**
 * Setter
 * @author Daybreak 새벽
 */
public abstract class Setter<T> implements EventExecutor {
	
	private final String Key;
	private T Value;
	private final ScriptWizard Wizard;
	
	public Setter(String Key, T Default, ScriptWizard Wizard) {
		this.Key = Key;
		this.Value = Default;
		this.Wizard = Wizard;
	}
	
	public String getKey() {
		return Key;
	}
	
	public T getValue() {
		return Value;
	}
	
	protected void setValue(T value) {
		Value = value;
		updateGUI();
	}
	
	protected ScriptWizard getWizard() {
		return Wizard;
	}
	
	public Class<?> getClazz() {
		if(getValue().getClass().getSuperclass().isEnum()) {
			return getValue().getClass().getSuperclass();
		} else {
			return getValue().getClass();
		}
	}
	
	protected void registerEvent(Class<? extends Event> event) {
		Bukkit.getPluginManager().registerEvent(event, Wizard, EventPriority.HIGH, this, AbilityWar.getPlugin());
	}
	
	protected void updateGUI() {
		Wizard.openScriptWizard(Wizard.getPlayerPage());
	}
	
	public abstract void onClick(ClickType click);
	
	public abstract ItemStack getItem();
	
	private static HashMap<Class<?>, Class<? extends Setter<?>>> SetterMap = new HashMap<Class<?>, Class<? extends Setter<?>>>();
	
	public static void registerSetter(Class<?> clazz, Class<? extends Setter<?>> setterClass) {
		if(!SetterMap.containsKey(clazz)) {
			SetterMap.put(clazz, setterClass);
		}
	}
	
	static {
		registerSetter(Location.class, LocationSetter.class);
	}

	public static Setter<?> newInstance(Class<?> clazz, String Key, Object Default, ScriptWizard Wizard) throws IllegalArgumentException {
		try {
			if(!clazz.isEnum()) {
				if(SetterMap.containsKey(clazz)) {
					Class<? extends Setter<?>> setterClass = SetterMap.get(clazz);
					Constructor<? extends Setter<?>> constructor = setterClass.getConstructor(String.class, clazz, ScriptWizard.class);
					return constructor.newInstance(Key, Default, Wizard);
				}
			} else {
				Validate.minimumConstant(clazz, 1);
				return new EnumSetter(Key, clazz.getEnumConstants()[0], Wizard);
			}
		} catch (NoSuchMethodException | SecurityException | InstantiationException
				| IllegalAccessException | InvocationTargetException e) {}
		
		throw new IllegalArgumentException();
	}

}
