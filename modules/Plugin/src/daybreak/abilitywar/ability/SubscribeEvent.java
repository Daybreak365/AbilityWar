package daybreak.abilitywar.ability;

import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SubscribeEvent {
	class Priority {
		public static final int LOWEST = 1;
		public static final int LOW = 2;
		public static final int NORMAL = 3;
		public static final int HIGH = 4;
		public static final int HIGHEST = 5;
	}

	boolean onlyRelevant() default false;

	boolean ignoreCancelled() default false;

	EventPriority eventPriority() default EventPriority.HIGHEST;

	int priority() default Priority.NORMAL;

	Class<? extends Event>[] childs() default {};
}
