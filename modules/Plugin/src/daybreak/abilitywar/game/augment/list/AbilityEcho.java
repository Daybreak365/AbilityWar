package daybreak.abilitywar.game.augment.list;

import daybreak.abilitywar.ability.event.AbilityCooldownEndEvent;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.augment.AbstractAugment;
import daybreak.abilitywar.game.augment.AugmentManifest;
import daybreak.abilitywar.game.augment.AugmentContext;
import daybreak.abilitywar.game.augment.AugmentRarity;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

@AugmentManifest(key = "ability_echo", name = "능력 메아리", rarity = AugmentRarity.GOLD, description = "능력 쿨타임이 끝날 때 5초간 신속과 힘을 얻습니다.")
public class AbilityEcho extends AbstractAugment {
	@Override public boolean isAvailable(@NotNull AugmentContext context, @NotNull Participant participant) { return super.isAvailable(context, participant) && participant.hasAbility(); }
	@EventHandler public void onAbilityCooldownEnd(AbilityCooldownEndEvent event) {
		if (hasAugment(event.getParticipant())) {
			event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 0, true, false));
			event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 100, 0, true, false));
		}
	}
}
