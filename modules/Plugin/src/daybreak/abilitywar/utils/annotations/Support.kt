package daybreak.abilitywar.utils.annotations

import daybreak.abilitywar.utils.base.minecraft.server.ServerType
import daybreak.abilitywar.utils.base.minecraft.version.NMSVersion
import daybreak.abilitywar.utils.base.minecraft.version.NMSVersion.v1_16_R1
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.ANNOTATION_CLASS
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.PROPERTY_GETTER
import kotlin.annotation.AnnotationTarget.PROPERTY_SETTER

class Support private constructor() {
	@Retention(RUNTIME)
	@Target(FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER, ANNOTATION_CLASS, CLASS)
	@MustBeDocumented
	annotation class Version(val min: NMSVersion, val max: NMSVersion = v1_16_R1)

	@Retention(RUNTIME)
	@Target(FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER, ANNOTATION_CLASS, CLASS)
	@MustBeDocumented
	annotation class Server(vararg val value: ServerType)
}