package daybreak.abilitywar.utils.base.minecraft.entity.health;

import daybreak.abilitywar.utils.base.minecraft.entity.health.event.PlayerSetHealthEvent;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

public class Healths {

	private Healths() {}

	public static double setHealth(final Player player, final double health) {
		final PlayerSetHealthEvent event = new PlayerSetHealthEvent(player, health);
		Bukkit.getPluginManager().callEvent(event);
		if (!event.isCancelled()) {
			player.setHealth(Math.min(event.getHealth(), player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
		}
		return player.getHealth();
	}

}
