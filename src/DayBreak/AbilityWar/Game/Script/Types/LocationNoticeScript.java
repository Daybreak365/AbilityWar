package DayBreak.AbilityWar.Game.Script.Types;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame.Participant;
import DayBreak.AbilityWar.Game.Script.Objects.AbstractScript;
import DayBreak.AbilityWar.Utils.Messager;

public class LocationNoticeScript extends AbstractScript {

	private static final long serialVersionUID = 7506633181977083329L;

	public LocationNoticeScript(String ScriptName, int Time, boolean Loop, int LoopCount, String PreRunMessage, String RunMessage) {
		super(ScriptName, Time, Loop, LoopCount, PreRunMessage, RunMessage);
	}

	@Override
	protected void Execute(AbstractGame game) {
		List<String> msg = new ArrayList<String>();
		
		msg.add(Messager.formatTitle(ChatColor.DARK_AQUA, ChatColor.AQUA, "플레이어 위치"));
		
		for(Participant participant : game.getParticipants()) {
			Player player = participant.getPlayer();
			if(!game.getDeathManager().isEliminated(player)) {
				Location l = player.getLocation();
				int X = (int) l.getX();
				int Y = (int) l.getY();
				int Z = (int) l.getZ();
				
				msg.add(ChatColor.translateAlternateColorCodes('&', "&9" + player.getName() + " &f: &bX&f" + X + ", &bY&f" + Y + ", &bZ&f" + Z));
			}
		}
		
		msg.add(ChatColor.translateAlternateColorCodes('&', "&3------------------------------------------------------------------------------"));
		
		Messager.broadcastMessage(msg);
	}

}
