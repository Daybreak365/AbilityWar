package daybreak.abilitywar.utils.base.minecraft.version

import daybreak.abilitywar.utils.base.minecraft.version.NMSVersion.UNSUPPORTED
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginDescriptionFile
import java.util.logging.Level
import java.util.logging.Logger

interface IVersion {
	val version: Int
	val release: Int
	fun isAboveOrEqual(other: IVersion): Boolean {
		return version >= other.version && (version != other.version || release >= other.release)
	}

	fun isBelowOrEqual(other: IVersion): Boolean {
		return version <= other.version && (version != other.version || release <= other.release)
	}
}

enum class NMSVersion(override val version: Int, override val release: Int,
                      val versionedPackage: Boolean, vararg val minecraftVersions: String) : IVersion {
    UNSUPPORTED(-1, -1, false),
    v1_12_R1(12, 1, true, "1.12", "1.12.1", "1.12.2"),
    v1_13_R1(13, 1, true, "1.13"),
    v1_13_R2(13, 2, true, "1.13.1", "1.13.2"),
    v1_14_R1(14, 1, true, "1.14", "1.14.1", "1.14.2", "1.14.3", "1.14.4"),
    v1_15_R1(15, 1, true, "1.15", "1.15.1", "1.15.2"),
    v1_16_R1(16, 1, true, "1.16", "1.16.1"),
    v1_16_R2(16, 2, true, "1.16.2", "1.16.3"),
    v1_16_R3(16, 3, true, "1.16.4", "1.16.5"),
    v1_17_R1(17, 1, true, "1.17", "1.17.1"),
    v1_18_R1(18, 1, true, "1.18", "1.18.1"),
    v1_18_R2(18, 2, true, "1.18.2"),
    v1_19_R1(19, 1, true, "1.19", "1.19.1", "1.19.2"),
    v1_19_R2(19, 2, true, "1.19.3"),
    v1_19_R3(19, 3, true, "1.19.4"),
    v1_20_R1(20, 1, true, "1.20", "1.20.1"),
    v1_20_R2(20, 2, true, "1.20.2"),
    v1_20_R3(20, 3, true, "1.20.3", "1.20.4"),
    v1_20_R4(20, 4, true, "1.20.5", "1.20.6"),
    v1_21_R1(21, 1, true, "1.21", "1.21.1"),
    v1_21_R2(21, 2, true, "1.21.2", "1.21.3"),
    v1_21_R3(21, 3, true, "1.21.4"),
    v1_21_R4(21, 4, true, "1.21.5"),
    v1_21_R5(21, 5, true, "1.21.6", "1.21.7", "1.21.8"),
    v1_21_R6(21, 6, true, "1.21.9", "1.21.10"),
    v1_21_R7(21, 7, true, "1.21.11"),
    v26_1(26, 1, false, "26.1", "26.1.1", "26.1.2"),
    v26_2(26, 2, false, "26.2"),
    v26_3(26, 3, false, "26.3");

    val unversionedPackage: Boolean
        get() = version >= 21 || (version == 20 && release >= 4)

    companion object {
        private val RELEASE = Regex("""(\d+\.\d+(?:\.\d+)?)(?:-R\d+\.\d+-SNAPSHOT|\.build\.\d+-[a-z]+|\.local-SNAPSHOT)?""")
        private val UNVERSIONED = values().filter { it.unversionedPackage }
            .flatMap { adapter -> adapter.minecraftVersions.map { it to adapter } }.toMap()
        private val LEGACY = values().filter { it.versionedPackage }.associateBy { it.name }

        /** Spigot uses revision packages; modern Paper resolves the same adapter by its exact release. */
        @JvmStatic
        fun resolve(serverClassName: String, bukkitVersion: String): NMSVersion {
            if (!serverClassName.startsWith("org.bukkit.craftbukkit.")) return UNSUPPORTED
            val packageVersion = serverClassName.split('.').getOrNull(3)
            val minecraftVersion = RELEASE.matchEntire(bukkitVersion)?.groupValues?.get(1) ?: return UNSUPPORTED
            LEGACY[packageVersion]?.let {
                return if (minecraftVersion in it.minecraftVersions) it else UNSUPPORTED
            }
            if (serverClassName != "org.bukkit.craftbukkit.CraftServer") return UNSUPPORTED
            return UNVERSIONED[minecraftVersion] ?: UNSUPPORTED
        }
    }
}

class ServerVersion private constructor() {
	companion object INSTANCE : IVersion {
		private val logger = Logger.getLogger(ServerVersion::class.java.name)

		private val INSTANCE = NMSVersion.resolve(Bukkit.getServer().javaClass.name, Bukkit.getBukkitVersion())

		@JvmStatic
		override val version: Int
			get() = INSTANCE.version

		@JvmStatic
		override val release: Int
			get() = INSTANCE.release

		@JvmStatic
		val name: String
			get() = INSTANCE.name

		@JvmStatic
		override fun isAboveOrEqual(other: IVersion): Boolean {
			return super.isAboveOrEqual(other)
		}

		@JvmStatic
		override fun isBelowOrEqual(other: IVersion): Boolean {
			return super.isBelowOrEqual(other)
		}

		@JvmStatic
		fun compatVersion(plugin: Plugin): Boolean {
			return if (INSTANCE != UNSUPPORTED) {
				if (version >= 13) {
					try {
						PluginDescriptionFile::class.java.getDeclaredField("apiVersion").apply {
							isAccessible = true
							this[plugin.description] = if (version >= 26) "$version.$release" else "1.$version"
							isAccessible = false
						}
					} catch (ignored: ReflectiveOperationException) {
					}
				}
				true
			} else {
				logger.log(Level.SEVERE, "플러그인이 지원하지 않는 버전을 이용하고 있습니다. Bukkit=${Bukkit.getBukkitVersion()}, Server=${Bukkit.getVersion()}, Class=${Bukkit.getServer().javaClass.name}")
				Bukkit.getPluginManager().disablePlugin(plugin)
				false
			}
		}
	}
}
