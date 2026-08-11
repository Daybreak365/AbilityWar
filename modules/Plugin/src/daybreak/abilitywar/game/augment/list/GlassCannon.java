package daybreak.abilitywar.game.augment.list;

import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.augment.AbstractAugment;
import daybreak.abilitywar.game.augment.AugmentManifest;
import daybreak.abilitywar.game.augment.AugmentRarity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@AugmentManifest(key = "glass_cannon", name = "양날의 검", rarity = AugmentRarity.GOLD, description = "주는 피해가 25% 증가하지만 받는 피해도 15% 증가합니다.")
public class GlassCannon extends AbstractAugment {
	@EventHandler(ignoreCancelled = true) public void onDamage(EntityDamageByEntityEvent event) {
		final Participant damager = event.getDamager() instanceof Player ? getParticipant((Player) event.getDamager()) : null;
		final Participant victim = event.getEntity() instanceof Player ? getParticipant((Player) event.getEntity()) : null;
		if (hasAugment(damager)) event.setDamage(event.getDamage() * 1.25);
		if (hasAugment(victim)) event.setDamage(event.getDamage() * 1.15);
	}
}
