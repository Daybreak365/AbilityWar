package daybreak.abilitywar.game.augment.list;

import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.augment.AbstractAugment;
import daybreak.abilitywar.game.augment.AugmentManifest;
import daybreak.abilitywar.game.augment.AugmentRarity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@AugmentManifest(key = "vampiric_blade", name = "흡혈 칼날", rarity = AugmentRarity.GOLD, description = "플레이어에게 준 피해의 20%만큼 체력을 회복합니다.")
public class VampiricBlade extends AbstractAugment {
	@EventHandler(ignoreCancelled = true) public void onDamage(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
			final Participant damager = getParticipant((Player) event.getDamager());
			if (hasAugment(damager)) heal(damager.getPlayer(), event.getFinalDamage() * 0.20);
		}
	}
}
