package daybreak.abilitywar.utils.base.minecraft.entity.decorator;

import daybreak.abilitywar.game.AbstractGame.Participant;
import org.bukkit.Location;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

public interface Deflectable {

	Vector getDirection();

	Location getLocation();

	void onDeflect(Participant deflector, Vector newDirection);

	ProjectileSource getShooter();

}
