package daybreak.abilitywar.game.augment.list;

import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.augment.AbstractAugment;
import daybreak.abilitywar.game.augment.AugmentContext;
import daybreak.abilitywar.game.augment.AugmentRarity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class BattleTrance extends AbstractAugment {
	public BattleTrance() { super("battle_trance", "전투 몰입", AugmentRarity.GOLD, "상시 힘 I 효과를 얻습니다."); }
	@Override protected void onApply(@NotNull AugmentContext context, @NotNull Participant participant) {
		participant.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 0, true, false));
	}
}
