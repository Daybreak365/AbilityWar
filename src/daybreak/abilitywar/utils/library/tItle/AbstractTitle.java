package daybreak.abilitywar.utils.library.tItle;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * 추상 타이틀
 *
 * @author Daybreak 새벽
 */
public abstract class AbstractTitle {

	public abstract void sendTo(Player p);

	public void Broadcast() {
		for (Player p : Bukkit.getOnlinePlayers()) sendTo(p);
	}

}
