package daybreak.abilitywar.game.script.list;

import com.google.common.base.Preconditions;
import daybreak.abilitywar.game.Game;
import daybreak.abilitywar.game.script.AbstractScript;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TeleportScript extends AbstractScript {

	private final String worldName;
	private final double x;
	private final double y;
	private final double z;
	private final float yaw;
	private final float pitch;

	public TeleportScript(String scriptName, int time, int loopCount, String preRunMessage, String runMessage, Location location) {
		super(scriptName, time, loopCount, preRunMessage, runMessage);
		this.worldName = location.getWorld().getName();
		this.x = location.getX();
		this.y = location.getY();
		this.z = location.getZ();
		this.yaw = location.getYaw();
		this.pitch = location.getPitch();
	}

	@Override
	public void execute(Game game) {
		try {
			Location l = new Location(Preconditions.checkNotNull(Bukkit.getWorld(worldName)), x, y, z, yaw, pitch);
			for (Player p : Bukkit.getOnlinePlayers()) p.teleport(l);
		} catch (NullPointerException ignore) {
		}
	}

}
