package daybreak.abilitywar.game.augment;

import daybreak.abilitywar.game.AbstractGame.Participant;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public final class DefaultAugments {

	private DefaultAugments() {}

	public static AugmentRegistry createRegistry() {
		final AugmentRegistry registry = new AugmentRegistry();
		registry.register(new AbstractAugment("extra_heart", "튼튼한 심장", AugmentRarity.SILVER, "최대 체력이 2 증가합니다.") {
			@Override
			public void apply(@NotNull AugmentContext context, @NotNull Participant participant) {
				context.addMaxHealth(participant, 2.0);
				context.addAugment(participant, this);
			}
		});
		registry.register(new AbstractAugment("swift_start", "민첩한 출발", AugmentRarity.SILVER, "이동 속도가 소폭 증가합니다.") {
			@Override
			public void apply(@NotNull AugmentContext context, @NotNull Participant participant) {
				final AttributeInstance attribute = participant.getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
				if (attribute != null) attribute.setBaseValue(attribute.getBaseValue() * 1.08);
				context.addAugment(participant, this);
			}
		});
		registry.register(new AbstractAugment("battle_trance", "전투 몰입", AugmentRarity.GOLD, "상시 힘 I 효과를 얻습니다.") {
			@Override
			public void apply(@NotNull AugmentContext context, @NotNull Participant participant) {
				participant.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 0, true, false));
				context.addAugment(participant, this);
			}
		});
		registry.register(new AbstractAugment("prismatic_body", "프리즘 육체", AugmentRarity.PRISMATIC, "최대 체력이 6 증가하고 재생 I 효과를 얻습니다.") {
			@Override
			public void apply(@NotNull AugmentContext context, @NotNull Participant participant) {
				context.addMaxHealth(participant, 6.0);
				participant.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0, true, false));
				context.addAugment(participant, this);
			}
		});
		return registry;
	}

	public static String format(@NotNull Augment augment) {
		return augment.getRarity().format("[" + augment.getRarity().getDisplayName() + "] ") + ChatColor.WHITE + augment.getDisplayName() + ChatColor.GRAY + " - " + augment.getDescription();
	}
}
