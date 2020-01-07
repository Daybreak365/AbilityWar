package daybreak.abilitywar.utils;

import daybreak.abilitywar.AbilityWar;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;

public class ResourcepackManager implements Listener {

	private static final String RESOURCEPACK_LINK = "https://drive.google.com/uc?export=download&confirm=no_antivirus&id=" + "1W223kUlcU4o11CEpeQXQPgpgh3LUqKCY";
	private static final HashSet<Player> usingPlayers = new HashSet<>();
	public static boolean isUsing(Player player) {
		return usingPlayers.contains(player);
	}

	static {
		new ResourcepackManager();
	}

	private ResourcepackManager() {
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
	}

	@EventHandler
	private void onPlayerJoin(PlayerJoinEvent e) {
		new BukkitRunnable() {
			@Override
			public void run() {
				e.getPlayer().setResourcePack(RESOURCEPACK_LINK);
			}
		}.runTaskLater(AbilityWar.getPlugin(), 10);
	}

	@EventHandler
	private void onPlayerResourcePackStatus(PlayerResourcePackStatusEvent e) {
		if (e.getStatus().equals(PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED)) {
			Player player = e.getPlayer();
			usingPlayers.add(player);
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bAbilityWar &f리소스팩을 성공적으로 불러왔습니다."));
		}
	}

}