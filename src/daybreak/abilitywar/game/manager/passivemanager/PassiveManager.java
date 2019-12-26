package daybreak.abilitywar.game.manager.passivemanager;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.game.games.mode.AbstractGame;
import daybreak.abilitywar.utils.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PassiveManager implements Listener, EventExecutor, AbstractGame.Observer {

	public PassiveManager(AbstractGame game) {
		game.attachObserver(this);
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
	}

	private final HashMap<Class<? extends Event>, CopyOnWriteArrayList<PassiveExecutor>> passiveExecutors = new HashMap<>();
	private final EventPriority priority = EventPriority.HIGHEST;
	private final ArrayList<Class<? extends Event>> registeredEvents = new ArrayList<>();

	@SuppressWarnings("unchecked")
	private Class<? extends Event> getHandlerListDeclaringClass(Class<? extends Event> eventClass) {
		Class<? extends Event> handlerClass = null;
		try {
			for (Field field : ReflectionUtil.FieldUtil.getExistingFields(eventClass, HandlerList.class)) {
				handlerClass = (Class<? extends Event>) field.getDeclaringClass();
				break;
			}
		} catch (Exception ignored) {}
		return handlerClass;
	}

	public void register(Class<? extends Event> eventClass, PassiveExecutor executor) {
		if (!passiveExecutors.containsKey(eventClass)) {
			passiveExecutors.put(eventClass, new CopyOnWriteArrayList<>());
		}

		Class<? extends Event> handlerDeclaringClass = getHandlerListDeclaringClass(eventClass);
		if (handlerDeclaringClass != null && !registeredEvents.contains(handlerDeclaringClass)) {
			Bukkit.getPluginManager().registerEvent(handlerDeclaringClass, this, priority, this, AbilityWar.getPlugin());
			registeredEvents.add(handlerDeclaringClass);
		}

		List<PassiveExecutor> list = passiveExecutors.get(eventClass);
		if (!list.contains(executor)) {
			list.add(executor);
		}
	}

	public void unregisterAll(PassiveExecutor executor) {
		for (Class<? extends Event> eventClass : passiveExecutors.keySet()) {
			CopyOnWriteArrayList<PassiveExecutor> list = passiveExecutors.get(eventClass);
			while (list.contains(executor))
				list.remove(executor);
		}
	}

	@Override
	public void execute(Listener listener, Event event) {
		Class<?> eventClass = event.getClass();
		if (passiveExecutors.containsKey(eventClass)) {
			for (PassiveExecutor executor : passiveExecutors.get(eventClass)) {
				executor.execute(event);
			}
		}
	}

	@Override
	public void update(AbstractGame.GAME_UPDATE update) {
		if (update.equals(AbstractGame.GAME_UPDATE.END)) {
			HandlerList.unregisterAll(this);
		}
	}
}
