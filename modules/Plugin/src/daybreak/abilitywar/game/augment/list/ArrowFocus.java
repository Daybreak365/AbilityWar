package daybreak.abilitywar.game.augment.list;

import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.augment.AbstractAugment;
import daybreak.abilitywar.game.augment.AugmentRarity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

public class ArrowFocus extends AbstractAugment {
	public ArrowFocus() { super("arrow_focus", "저격수의 집중", AugmentRarity.SILVER, "10블록 이상 떨어진 화살 피해가 35% 증가합니다."); }
	@EventHandler(ignoreCancelled = true) public void onDamage(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Projectile) {
			final ProjectileSource shooter = ((Projectile) event.getDamager()).getShooter();
			if (shooter instanceof Player) {
				final Participant damager = getParticipant((Player) shooter);
				if (hasAugment(damager)) {
					final double distanceSquared = damager.getPlayer().getLocation().distanceSquared(event.getEntity().getLocation());
					if (distanceSquared >= 100) event.setDamage(event.getDamage() * 1.35);
				}
			}
		}
	}
}
