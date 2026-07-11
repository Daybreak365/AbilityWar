package daybreak.abilitywar.game.augment.list;

import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.augment.AbstractAugment;
import daybreak.abilitywar.game.augment.AugmentManifest;
import daybreak.abilitywar.game.augment.AugmentRarity;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@AugmentManifest(key = "executioner", name = "처형자", rarity = AugmentRarity.GOLD, description = "체력이 30% 이하인 적에게 주는 피해가 30% 증가합니다.")
public class Executioner extends AbstractAugment {
	@EventHandler(ignoreCancelled = true) public void onDamage(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player && event.getEntity() instanceof LivingEntity) {
			final Participant damager = getParticipant((Player) event.getDamager());
			final LivingEntity target = (LivingEntity) event.getEntity();
			final AttributeInstance maxHealth = target.getAttribute(Attribute.GENERIC_MAX_HEALTH);
			if (hasAugment(damager) && maxHealth != null && target.getHealth() <= maxHealth.getValue() * 0.30) event.setDamage(event.getDamage() * 1.30);
		}
	}
}
