package daybreak.abilitywar.utils.base.minecraft.raytrace

import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion
import daybreak.abilitywar.utils.base.minecraft.version.VersionNotSupportedException
import org.bukkit.World
import org.bukkit.util.Vector

class RayTrace private constructor() {
    companion object INSTANCE: IRayTrace {
        private val INSTANCE: IRayTrace = try {
            Class.forName("daybreak.abilitywar.utils.base.minecraft.raytrace." + ServerVersion.name + ".RayTraceImpl").asSubclass(IRayTrace::class.java).getConstructor().newInstance()
        } catch (e: Exception) {
            throw VersionNotSupportedException()
        }

        @JvmStatic
        override fun hitsBlock(world: World, ax: Double, ay: Double, az: Double, bx: Double, by: Double, bz: Double): Boolean {
            return INSTANCE.hitsBlock(world, ax, ay, az, bx, by, bz)
        }

        @JvmStatic
        fun hitsBlock(world: World, a: Vector, b: Vector): Boolean {
            return INSTANCE.hitsBlock(world, a.x, a.y, a.z, b.x, b.y, b.z)
        }

    }

}