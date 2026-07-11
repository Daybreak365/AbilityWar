package daybreak.abilitywar.game.augment.list;

import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.augment.AbstractAugment;
import daybreak.abilitywar.game.augment.AugmentRarity;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Comeback extends AbstractAugment {
	public Comeback() { super("comeback", "역전의 기회", AugmentRarity.SILVER, "피격 후 체력이 40% 이하라면 잠시 재생과 저항을 얻습니다."); }
	@EventHandler(ignoreCancelled = true) public void onDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)) return;
		final Participant participant = getParticipant((Player) event.getEntity());
		if (!hasAugment(participant)) return;
		final AttributeInstance maxHealth = participant.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH);
		if (maxHealth != null && participant.getPlayer().getHealth() - event.getFinalDamage() <= maxHealth.getValue() * 0.40) {
			participant.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 80, 0, true, false));
			participant.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 80, 0, true, false));
		}
	}
}
