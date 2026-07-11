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

		registry.register(new AbstractAugment("glass_cannon", "양날의 검", AugmentRarity.GOLD, "주는 피해가 25% 증가하지만 받는 피해도 15% 증가합니다.") {
			@Override
			public void apply(@NotNull AugmentContext context, @NotNull Participant participant) {
				context.addAugment(participant, this);
			}
		});
		registry.register(new AbstractAugment("vampiric_blade", "흡혈 칼날", AugmentRarity.GOLD, "플레이어에게 준 피해의 20%만큼 체력을 회복합니다.") {
			@Override
			public void apply(@NotNull AugmentContext context, @NotNull Participant participant) {
				context.addAugment(participant, this);
			}
		});
		registry.register(new AbstractAugment("executioner", "처형자", AugmentRarity.GOLD, "체력이 30% 이하인 적에게 주는 피해가 30% 증가합니다.") {
			@Override
			public void apply(@NotNull AugmentContext context, @NotNull Participant participant) {
				context.addAugment(participant, this);
			}
		});
		registry.register(new AbstractAugment("comeback", "역전의 기회", AugmentRarity.SILVER, "피격 후 체력이 40% 이하라면 잠시 재생과 저항을 얻습니다.") {
			@Override
			public void apply(@NotNull AugmentContext context, @NotNull Participant participant) {
				context.addAugment(participant, this);
			}
		});
		registry.register(new AbstractAugment("arrow_focus", "저격수의 집중", AugmentRarity.SILVER, "10블록 이상 떨어진 화살 피해가 35% 증가합니다.") {
			@Override
			public void apply(@NotNull AugmentContext context, @NotNull Participant participant) {
				context.addAugment(participant, this);
			}
		});
		registry.register(new AbstractAugment("thornmail", "가시 갑옷", AugmentRarity.GOLD, "근접 피해를 받으면 공격자에게 피해의 20%를 반사합니다.") {
			@Override
			public void apply(@NotNull AugmentContext context, @NotNull Participant participant) {
				context.addAugment(participant, this);
			}
		});
		registry.register(new AbstractAugment("last_stand", "최후의 저항", AugmentRarity.PRISMATIC, "죽음에 이르는 피해를 한 번 버티고 5초간 강해집니다.") {
			@Override
			public void apply(@NotNull AugmentContext context, @NotNull Participant participant) {
				context.addAugment(participant, this);
			}
		});
		registry.register(new AbstractAugment("raid_boss", "레이드 보스", AugmentRarity.PRISMATIC, "최대 체력이 8 증가하지만 이동 속도가 감소합니다.") {
			@Override
			public void apply(@NotNull AugmentContext context, @NotNull Participant participant) {
				context.addMaxHealth(participant, 8.0);
				final AttributeInstance attribute = participant.getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
				if (attribute != null) attribute.setBaseValue(attribute.getBaseValue() * 0.92);
				context.addAugment(participant, this);
			}
		});
		registry.register(new AbstractAugment("ability_echo", "능력 메아리", AugmentRarity.GOLD, "능력 쿨타임이 끝날 때 5초간 신속과 힘을 얻습니다.") {
			@Override
			public boolean isAvailable(@NotNull AugmentContext context, @NotNull Participant participant) {
				return super.isAvailable(context, participant) && participant.hasAbility();
			}

			@Override
			public void apply(@NotNull AugmentContext context, @NotNull Participant participant) {
				context.addAugment(participant, this);
			}
		});
		registry.register(new AbstractAugment("ability_overdrive", "능력 과부하", AugmentRarity.PRISMATIC, "능력이 있다면 최대 체력과 이동 속도가 증가합니다.") {
			@Override
			public boolean isAvailable(@NotNull AugmentContext context, @NotNull Participant participant) {
				return super.isAvailable(context, participant) && participant.hasAbility();
			}

			@Override
			public void apply(@NotNull AugmentContext context, @NotNull Participant participant) {
				context.addMaxHealth(participant, 4.0);
				final AttributeInstance attribute = participant.getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
				if (attribute != null) attribute.setBaseValue(attribute.getBaseValue() * 1.10);
				context.addAugment(participant, this);
			}
		});
		return registry;
	}

	public static String format(@NotNull Augment augment) {
		return augment.getRarity().format("[" + augment.getRarity().getDisplayName() + "] ") + ChatColor.WHITE + augment.getDisplayName() + ChatColor.GRAY + " - " + augment.getDescription();
	}
}
