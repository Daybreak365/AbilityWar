package daybreak.abilitywar.game;

import daybreak.abilitywar.game.team.interfaces.Teamable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TeamSupport {
	Class<? extends Teamable> value();
}
