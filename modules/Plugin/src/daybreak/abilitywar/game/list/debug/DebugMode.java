package daybreak.abilitywar.game.list.debug;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.Category;
import daybreak.abilitywar.game.Category.GameCategory;
import daybreak.abilitywar.game.Game;
import daybreak.abilitywar.game.GameManifest;
import daybreak.abilitywar.game.manager.object.DefaultKitHandler;
import daybreak.abilitywar.game.manager.object.InfiniteDurability;
import daybreak.abilitywar.utils.base.minecraft.PlayerCollector;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

@GameManifest(name = "Debug Mode", description = {})
@Category(GameCategory.DEBUG)
public class DebugMode extends Game implements DefaultKitHandler, AbstractGame.Observer {

	public DebugMode() {
		super(PlayerCollector.EVERY_PLAYER_EXCLUDING_SPECTATORS());
		attachObserver(this);
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
	}

	@Override
	protected void progressGame(int seconds) {
		if (seconds == 1) {
			setRestricted(false);
			if (Settings.getNoHunger()) {
				Bukkit.broadcastMessage("§2배고픔 무제한§a이 적용됩니다.");
			} else {
				Bukkit.broadcastMessage("§4배고픔 무제한§c이 적용되지 않습니다.");
			}
			if (Settings.getInfiniteDurability()) {
				attachObserver(new InfiniteDurability());
			} else {
				Bukkit.broadcastMessage("§4내구도 무제한§c이 적용되지 않습니다.");
			}
			startGame();
		}
	}

	@Override
	public void update(GameUpdate update) {
		if (update == GameUpdate.END) {
			HandlerList.unregisterAll(this);
		}
	}

}
