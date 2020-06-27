package daybreak.abilitywar.utils.base.minecraft;

import org.bukkit.event.Listener;

public class ResourcepackManager implements Listener {
/*
	private static final String RESOURCEPACK_LINK = "https://drive.google.com/uc?export=download§confirm=no_antivirus§id=" + "1RQB5AQ1KwrxPC21DASf3BTgKB7eDL3tt";
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
			player.sendMessage("§bAbilityWar §f리소스팩을 성공적으로 불러왔습니다.");
		}
	}
*/
}