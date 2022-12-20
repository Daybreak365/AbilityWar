package daybreak.abilitywar.utils.annotations;

import daybreak.abilitywar.utils.base.minecraft.server.ServerType;
import daybreak.abilitywar.utils.base.minecraft.version.NMSVersion;
import kotlin.annotation.MustBeDocumented;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class Support {

	private Support() {}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD, ElementType.TYPE})
	@Documented
	public @interface Version {
		NMSVersion min();

		NMSVersion max() default NMSVersion.v1_19_R2;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD, ElementType.TYPE})
	@MustBeDocumented
	public @interface Server {
		ServerType[] value();
	}

}
