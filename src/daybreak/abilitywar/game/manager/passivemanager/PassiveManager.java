package daybreak.abilitywar.game.manager.passivemanager;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
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
import java.util.HashSet;
import java.util.Set;

public class PassiveManager implements Listener, EventExecutor, AbstractGame.Observer {

	public PassiveManager(AbstractGame game) {
		game.attachObserver(this);
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
	}

	private final Multimap<Class<? extends Event>, PassiveExecutor> passiveExecutors = MultimapBuilder.hashKeys().hashSetValues().build();
	private final EventPriority priority = EventPriority.HIGHEST;
	private final Set<Class<? extends Event>> registeredEvents = new HashSet<>();

	@SuppressWarnings("unchecked")
	private Class<? extends Event> getHandlerListDeclaringClass(Class<? extends Event> eventClass) {
		try {
			for (Field field : ReflectionUtil.FieldUtil.getExistingFields(eventClass, HandlerList.class)) {
				return (Class<? extends Event>) field.getDeclaringClass();
			}
		} catch (Exception ignored) {
		}
		return null;
	}

	public void register(Class<? extends Event> eventClass, PassiveExecutor executor) {
		Class<? extends Event> handlerDeclaringClass = getHandlerListDeclaringClass(eventClass);
		if (handlerDeclaringClass != null && registeredEvents.add(handlerDeclaringClass)) {
			Bukkit.getPluginManager().registerEvent(handlerDeclaringClass, this, priority, this, AbilityWar.getPlugin());
		}

		passiveExecutors.get(eventClass).add(executor);
	}

	public void unregisterAll(PassiveExecutor executor) {
		for (Class<? extends Event> eventClass : new HashSet<>(passiveExecutors.keySet())) {
			passiveExecutors.remove(eventClass, executor);
		}
	}

	@Override
	public void execute(Listener listener, Event event) {
		Class<? extends Event> eventClass = event.getClass();
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
