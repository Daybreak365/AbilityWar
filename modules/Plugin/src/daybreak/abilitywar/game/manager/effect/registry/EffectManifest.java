package daybreak.abilitywar.game.manager.effect.registry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EffectManifest {

	String name();
	String displayName();
	ApplicationMethod method() default ApplicationMethod.MULTIPLE;
	EffectType[] type() default {};

}
