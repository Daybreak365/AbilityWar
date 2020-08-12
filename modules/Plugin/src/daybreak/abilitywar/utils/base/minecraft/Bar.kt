package daybreak.abilitywar.utils.base.minecraft

import daybreak.abilitywar.AbilityWar
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion.INSTANCE.version
import org.bukkit.Bukkit
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarFlag
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class Bar(title: String, barColor: BarColor, barStyle: BarStyle, vararg barFlags: BarFlag) : Listener {
	private val bossBar: BossBar = Bukkit.createBossBar(title, barColor, barStyle, *barFlags)

	init {
		for (player: Player in Bukkit.getOnlinePlayers()) {
			bossBar.addPlayer(player)
		}
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin())
		if (version >= 10) bossBar.isVisible = true
	}

	@EventHandler
	private fun onJoin(e: PlayerJoinEvent) {
		bossBar.addPlayer(e.player)
	}

	@EventHandler
	private fun onQuit(e: PlayerQuitEvent) {
		bossBar.removePlayer(e.player)
	}

	fun setTitle(title: String): Bar {
		bossBar.setTitle(title)
		return this
	}

	fun setColor(barColor: BarColor): Bar {
		bossBar.color = barColor
		return this
	}

	fun setStyle(barStyle: BarStyle): Bar {
		bossBar.style = barStyle
		return this
	}

	fun setProgress(progress: Double): Bar {
		bossBar.progress = progress
		return this
	}

	fun addFlag(barFlag: BarFlag): Bar {
		bossBar.addFlag(barFlag)
		return this
	}

	fun removeFlag(barFlag: BarFlag): Bar {
		bossBar.removeFlag(barFlag)
		return this
	}

	fun remove() {
		HandlerList.unregisterAll(this)
		bossBar.removeAll()
	}

}