package daybreak.abilitywar.game.games.mode.decorator;

import daybreak.abilitywar.game.games.mode.AbstractGame;
import daybreak.abilitywar.utils.FireworkUtil;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.thread.AbilityWarThread;
import daybreak.abilitywar.utils.thread.OverallTimer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.StringJoiner;

public interface Winnable {

	default void Win(AbstractGame.Participant... winners) {
		Messager.clearChat();
		StringBuilder builder = new StringBuilder(ChatColor.translateAlternateColorCodes('&', "&5&l우승자&f: "));
		StringJoiner joiner = new StringJoiner(ChatColor.WHITE + ", " + ChatColor.LIGHT_PURPLE, ChatColor.LIGHT_PURPLE.toString(), ChatColor.WHITE + ".");
		for (AbstractGame.Participant participant : winners) {
			Player p = participant.getPlayer();
			joiner.add(p.getName());
			SoundLib.UI_TOAST_CHALLENGE_COMPLETE.playSound(p);
			new OverallTimer(8) {
				@Override
				protected void onProcess(int seconds) {
					FireworkUtil.spawnWinnerFirework(p.getEyeLocation());
				}
			}.setPeriod(8).startTimer();
		}
		builder.append(joiner.toString());
		Bukkit.broadcastMessage(builder.toString());
		AbilityWarThread.StopGame();
	}

}
