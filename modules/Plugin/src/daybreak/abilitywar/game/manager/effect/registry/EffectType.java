package daybreak.abilitywar.game.manager.effect.registry;

public enum EffectType {

	MOVEMENT_RESTRICTION("이동 제한"),
	MOVEMENT_INTERRUPT("이동 방해"),
	SIGHT_RESTRICTION("시야 제한"),
	SIGHT_CONTROL("시야 조종"),
	ABILITY_RESTRICTION("능력 제한"),
	COMBAT_RESTRICTION("전투 제한"),
	HEALING_REDUCTION("회복 감소"),
	HEALING_BAN("회복 금지"),
	INVINCIBILITY("무적");

	private final String displayName;

	EffectType(final String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
