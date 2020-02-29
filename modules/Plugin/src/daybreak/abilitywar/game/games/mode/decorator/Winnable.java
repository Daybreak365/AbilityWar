package daybreak.abilitywar.game.games.mode.decorator;

import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer.TaskType;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.minecraft.FireworkUtil;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.thread.AbilityWarThread;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.StringJoiner;

public interface Winnable {

	default void Win(Participant... winners) {
		Messager.clearChat();
		StringBuilder builder = new StringBuilder(ChatColor.translateAlternateColorCodes('&', "&5&l우승자&f: "));
		StringJoiner joiner = new StringJoiner(ChatColor.WHITE + ", " + ChatColor.LIGHT_PURPLE, ChatColor.LIGHT_PURPLE.toString(), ChatColor.WHITE + ".");
		for (Participant participant : winners) {
			Player p = participant.getPlayer();
			joiner.add(p.getName());
			SoundLib.UI_TOAST_CHALLENGE_COMPLETE.playSound(p);
			new SimpleTimer(TaskType.REVERSE, 8) {
				@Override
				protected void run(int seconds) {
					FireworkUtil.spawnWinnerFirework(p.getEyeLocation());
				}
			}.setPeriod(TimeUnit.TICKS, 8).start();
		}
		builder.append(joiner.toString());
		Bukkit.broadcastMessage(builder.toString());
		AbilityWarThread.StopGame();
	}

}
