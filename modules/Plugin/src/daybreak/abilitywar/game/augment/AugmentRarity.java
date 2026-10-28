package daybreak.abilitywar.game.augment;

import org.bukkit.ChatColor;

/**
 * 롤 아레나의 증강 등급처럼 선택지의 희귀도와 기본 가중치를 표현합니다.
 */
public enum AugmentRarity {

	SILVER("실버", ChatColor.WHITE, 60),
	GOLD("골드", ChatColor.GOLD, 30),
	PRISMATIC("프리즘", ChatColor.LIGHT_PURPLE, 10);

	private final String displayName;
	private final ChatColor color;
	private final int defaultWeight;

	AugmentRarity(String displayName, ChatColor color, int defaultWeight) {
		this.displayName = displayName;
		this.color = color;
		this.defaultWeight = defaultWeight;
	}

	public String getDisplayName() {
		return displayName;
	}

	public ChatColor getColor() {
		return color;
	}

	public int getDefaultWeight() {
		return defaultWeight;
	}

	public String format(String text) {
		return color + text + ChatColor.RESET;
	}
}
