package daybreak.abilitywar.utils.annotations;

import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Support {

	ServerVersion.Version value();

}
