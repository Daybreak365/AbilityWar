package daybreak.abilitywar.game.augment.list;

import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.augment.AbstractAugment;
import daybreak.abilitywar.game.augment.AugmentRarity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LastStand extends AbstractAugment {
	private final Set<UUID> used = new HashSet<>();
	public LastStand() { super("last_stand", "최후의 저항", AugmentRarity.PRISMATIC, "죽음에 이르는 피해를 한 번 버티고 5초간 강해집니다."); }
	@EventHandler(ignoreCancelled = true) public void onDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)) return;
		final Player player = (Player) event.getEntity();
		final Participant participant = getParticipant(player);
		if (hasAugment(participant) && !used.contains(player.getUniqueId()) && player.getHealth() - event.getFinalDamage() <= 0) {
			event.setCancelled(true);
			used.add(player.getUniqueId());
			player.setHealth(1.0);
			player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 1, true, false));
			player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 100, 1, true, false));
			player.sendMessage("§d[증강] §f최후의 저항이 발동했습니다!");
		}
	}
}
