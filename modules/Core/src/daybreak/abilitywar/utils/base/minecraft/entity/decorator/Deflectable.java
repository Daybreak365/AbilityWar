package daybreak.abilitywar.utils.base.minecraft.entity.decorator;

import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

public interface Deflectable {

	void onDeflect(Participant deflector, Vector newDirection);

	ProjectileSource getShooter();

}
