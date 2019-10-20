package daybreak.abilitywar.game.manager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import daybreak.abilitywar.config.AbilityWarSettings.Settings;
import daybreak.abilitywar.game.games.defaultgame.Game;

public class WRECK {

	@SuppressWarnings("unused")
	private final Game game;

	public WRECK(Game game) {
		this.game = game;
	}

	private final boolean enabled = Settings.isWRECKEnabled();

	public boolean isEnabled() {
		return enabled;
	}

	public void noticeIfEnabled() {
		if (enabled) {
			Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&cW&6R&eE&aC&bK &f모드가 활성화되었습니다!"));
			Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c모든 능력의 쿨타임이 90% 감소합니다."));
		}
	}

	public static interface Handler {
		WRECK getWRECK();
		boolean isWRECKEnabled();
	}

}
