package daybreak.abilitywar.game.augment.list;

import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.augment.AbstractAugment;
import daybreak.abilitywar.game.augment.AugmentManifest;
import daybreak.abilitywar.game.augment.AugmentRarity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@AugmentManifest(key = "thornmail", name = "가시 갑옷", rarity = AugmentRarity.GOLD, description = "근접 피해를 받으면 공격자에게 피해의 20%를 반사합니다.")
public class Thornmail extends AbstractAugment {
	@EventHandler(ignoreCancelled = true) public void onDamage(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player && event.getEntity() instanceof Player && !(event.getDamager() instanceof Projectile)) {
			final Participant victim = getParticipant((Player) event.getEntity());
			if (hasAugment(victim)) ((Player) event.getDamager()).damage(event.getFinalDamage() * 0.20);
		}
	}
}
