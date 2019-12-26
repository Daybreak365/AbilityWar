package daybreak.abilitywar.game.manager.object;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.config.AbilityWarSettings.Settings;
import daybreak.abilitywar.config.AbilityWarSettings.Settings.DeathSettings;
import daybreak.abilitywar.config.enums.OnDeath;
import daybreak.abilitywar.game.games.mode.AbstractGame;
import daybreak.abilitywar.game.manager.SpectatorManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.plugin.EventExecutor;

/**
 * 방화벽
 *
 * @author Daybreak 새벽
 */
public class Firewall implements EventExecutor {

	private final AbstractGame game;
	private final DeathManager.Handler handler;

	public Firewall(AbstractGame game, DeathManager.Handler handler) {
		this.game = game;
		this.handler = handler;
		Bukkit.getPluginManager().registerEvent(PlayerLoginEvent.class, game, EventPriority.HIGHEST, this,
				AbilityWar.getPlugin());
	}

	@Override
	public void execute(Listener listener, Event event) {
		if (event instanceof PlayerLoginEvent) {
			PlayerLoginEvent e = (PlayerLoginEvent) event;
			Player p = e.getPlayer();
			if (Settings.getFirewall()) {
				if (!(p.isOp() || game.isParticipating(p.getUniqueId())
						|| SpectatorManager.getSpectators().contains(p.getName()))) {
					e.disallow(Result.KICK_OTHER, ChatColor.translateAlternateColorCodes('&', "&2《&aAbilityWar&2》")
							+ "\n" + ChatColor.translateAlternateColorCodes('&', "&f게임 진행중이므로 접속할 수 없습니다."));
				}
			}
			if (DeathSettings.getOperation().equals(OnDeath.탈락)) {
				if (handler.getDeathManager().isDead(p) && !p.isOp()) {
					e.disallow(Result.KICK_OTHER, ChatColor.translateAlternateColorCodes('&', "&2《&aAbilityWar&2》")
							+ "\n" + ChatColor.translateAlternateColorCodes('&', "&f탈락하셨습니다."));
				}
			}
		}
	}

}
