package daybreak.abilitywar.game

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.ANNOTATION_CLASS
import kotlin.annotation.AnnotationTarget.CLASS

@Retention(RUNTIME)
@Target(ANNOTATION_CLASS, CLASS)
annotation class GameManifest(val name: String, val description: Array<String>)