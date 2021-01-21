package daybreak.abilitywar.ability.list.grapplinghook

import daybreak.abilitywar.utils.base.minecraft.ability.list.grapplinghook.HookEntity
import daybreak.abilitywar.utils.base.minecraft.ability.list.grapplinghook.IHooks
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion
import daybreak.abilitywar.utils.base.minecraft.version.VersionNotSupportedException
import org.bukkit.Location
import org.bukkit.entity.Player

class Hooks private constructor() {
    companion object INSTANCE : IHooks {
        private val INSTANCE: IHooks = try {
            Class.forName("daybreak.abilitywar.ability.list.grapplinghook." + ServerVersion.name + ".HooksImpl").asSubclass(IHooks::class.java).getConstructor().newInstance()
        } catch (e: Exception) {
            throw VersionNotSupportedException()
        }

        @JvmStatic
        override fun createHook(player: Player, targetLoc: Location): HookEntity {
            return INSTANCE.createHook(player, targetLoc)
        }

        @JvmStatic
        override fun createHook(player: Player, target: Player): HookEntity {
            return INSTANCE.createHook(player, target)
        }
    }
}