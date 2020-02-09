package daybreak.abilitywar.game.manager.object;

import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.game.games.mode.AbstractGame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class WRECK {

	private final boolean enabled;

	public WRECK(boolean enabled) {
		this.enabled = enabled;
	}

	public WRECK() {
		this(Settings.isWRECKEnabled());
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void noticeIfEnabled(AbstractGame game) {
		if (enabled) {
			Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&cW&6R&eE&aC&bK &f모드가 활성화되었습니다!"));
			Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c모든 능력의 쿨타임이 90% 감소합니다."));
			// TODO: WRECK 시작 메시지
		}
	}

	public interface Handler {
		default WRECK newWRECK() {
			return new WRECK();
		}

		WRECK getWRECK();

		boolean isWRECKEnabled();
	}

	public static boolean isEnabled(AbstractGame game) {
		return game instanceof WRECK.Handler && ((WRECK.Handler) game).isWRECKEnabled();
	}

}
