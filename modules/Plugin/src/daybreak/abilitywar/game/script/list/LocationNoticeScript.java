package daybreak.abilitywar.game.script.list;

import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.Game;
import daybreak.abilitywar.game.script.AbstractScript;
import daybreak.abilitywar.utils.base.Formatter;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class LocationNoticeScript extends AbstractScript {

	public LocationNoticeScript(String scriptName, int time, int loopCount, String preRunMessage, String runMessage) {
		super(scriptName, time, loopCount, preRunMessage, runMessage);
	}

	@Override
	protected void execute(Game game) {
		List<String> msg = new ArrayList<>();

		msg.add(Formatter.formatTitle(ChatColor.DARK_AQUA, ChatColor.AQUA, "플레이어 위치"));

		for (Participant participant : game.getParticipants()) {
			Player player = participant.getPlayer();
			if (!game.getDeathManager().isExcluded(player)) {
				Location l = player.getLocation();
				int X = (int) l.getX();
				int Y = (int) l.getY();
				int Z = (int) l.getZ();

				msg.add("§9" + player.getName() + " §f: §bX§f" + X + ", §bY§f" + Y + ", §bZ§f" + Z);
			}
		}

		msg.add("§3-------------------------------------------------------------");

		for (String m : msg) {
			Bukkit.broadcastMessage(m);
		}
	}

}
