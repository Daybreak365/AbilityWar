package Marlang.AbilityWar.GameManager.Script.Objects.Setter;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.EventExecutor;

import Marlang.AbilityWar.AbilityWar;
import Marlang.AbilityWar.GameManager.Script.ScriptWizard;

abstract public class Setter<T> implements EventExecutor {
	
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
	
	protected void registerEvent(Class<? extends Event> event) {
		Bukkit.getPluginManager().registerEvent(event, Wizard, EventPriority.HIGH, this, AbilityWar.getPlugin());
	}
	
	protected void updateGUI() {
		Wizard.openScriptWizard(Wizard.getPlayerPage());
	}
	
	abstract public void onClick(ClickType click);
	
	abstract public ItemStack getItem();
	
	/*
	 * Setter 직접 하나씩 등록해줘야함
	 */
	public static Setter<?> newInstance(Class<?> clazz, String Key, Object Default, ScriptWizard Wizard) throws IllegalArgumentException {
		if(clazz.equals(Location.class)) {
			return new LocationSetter(Key, (Location) Default, Wizard);
		}
		
		throw new IllegalArgumentException();
	}
	
}
