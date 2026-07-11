package daybreak.abilitywar.game.augment;

import daybreak.abilitywar.game.AbstractGame.Participant;
import org.jetbrains.annotations.NotNull;

/**
 * 참가자가 선택할 수 있는 하나의 증강입니다.
 */
public interface Augment {

	@NotNull
	String getKey();

	@NotNull
	String getDisplayName();

	@NotNull
	AugmentRarity getRarity();

	@NotNull
	String getDescription();

	/**
	 * 특정 참가자에게 이미 적용되어 있거나 조건을 만족하지 못하면 false를 반환합니다.
	 */
	boolean isAvailable(@NotNull AugmentContext context, @NotNull Participant participant);

	/**
	 * 선택된 증강 효과를 참가자에게 적용합니다.
	 */
	void apply(@NotNull AugmentContext context, @NotNull Participant participant);
}
