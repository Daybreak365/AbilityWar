package daybreak.abilitywar.game.script.types;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import daybreak.abilitywar.game.games.mode.AbstractGame;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.game.script.objects.AbstractScript;
import daybreak.abilitywar.utils.Messager;

public class LocationNoticeScript extends AbstractScript {

	public LocationNoticeScript(String ScriptName, int Time, int LoopCount, String PreRunMessage, String RunMessage) {
		super(ScriptName, Time, LoopCount, PreRunMessage, RunMessage);
	}

	@Override
	protected void Execute(AbstractGame game) {
		List<String> msg = new ArrayList<String>();
		
		msg.add(Messager.formatTitle(ChatColor.DARK_AQUA, ChatColor.AQUA, "플레이어 위치"));
		
		for (Participant participant : game.getParticipants()) {
			Player player = participant.getPlayer();
			if (!game.getDeathManager().isEliminated(player)) {
				Location l = player.getLocation();
				int X = (int) l.getX();
				int Y = (int) l.getY();
				int Z = (int) l.getZ();
				
				msg.add(ChatColor.translateAlternateColorCodes('&', "&9" + player.getName() + " &f: &bX&f" + X + ", &bY&f" + Y + ", &bZ&f" + Z));
			}
		}
		
		msg.add(ChatColor.translateAlternateColorCodes('&', "&3-------------------------------------------------------------"));
		
		for (String m : msg) {
			Bukkit.broadcastMessage(m);
		}
	}

}
