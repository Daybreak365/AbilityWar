package daybreak.abilitywar.utils.base.minecraft;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Bar implements Listener {

	private final BossBar bossBar;

	public Bar(String title, BarColor barColor, BarStyle barStyle, BarFlag... barFlags) {
		this.bossBar = Bukkit.createBossBar(title, barColor, barStyle, barFlags);
		for (Player player : Bukkit.getOnlinePlayers()) {
			bossBar.addPlayer(player);
		}
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		if (ServerVersion.getVersionNumber() >= 10) bossBar.setVisible(true);
	}

	@EventHandler
	private void onJoin(PlayerJoinEvent e) {
		bossBar.addPlayer(e.getPlayer());
	}

	@EventHandler
	private void onQuit(PlayerQuitEvent e) {
		bossBar.removePlayer(e.getPlayer());
	}

	public Bar setTitle(String title) {
		bossBar.setTitle(title);
		return this;
	}

	public Bar setColor(BarColor barColor) {
		bossBar.setColor(barColor);
		return this;
	}

	public Bar setStyle(BarStyle barStyle) {
		bossBar.setStyle(barStyle);
		return this;
	}

	public Bar setProgress(double progress) {
		bossBar.setProgress(progress);
		return this;
	}

	public Bar addFlag(BarFlag barFlag) {
		bossBar.addFlag(barFlag);
		return this;
	}

	public Bar removeFlag(BarFlag barFlag) {
		bossBar.removeFlag(barFlag);
		return this;
	}

	public void remove() {
		HandlerList.unregisterAll(this);
		bossBar.removeAll();
	}

}
