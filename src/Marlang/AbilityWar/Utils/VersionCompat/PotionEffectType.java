package Marlang.AbilityWar.Utils.VersionCompat;

public enum PotionEffectType {
	
	ABSORPTION(6),
	BLINDNESS(5),
	CONFUSION(5),
	DAMAGE_RESISTANCE(5),
	FAST_DIGGING(5),
	FIRE_RESISTANCE(5),
	GLOWING(9),
	HARM(5),
	HEAL(5),
	HEALTH_BOOST(6),
	HUNGER(5),
	INCREASE_DAMAGE(5),
	INVISIBILITY(5),
	JUMP(5),
	LEVITATION(9),
	LUCK(9),
	NIGHT_VISION(5),
	POISON(5),
	REGENERATION(5),
	SATURATION(6),
	SLOW(5),
	SLOW_DIGGING(5),
	SPEED(5),
	UNLUCK(9),
	WATER_BREATHING(5),
	WEAKNESS(5),
	WITHER(4);
	
	private Integer Version;
	
	private PotionEffectType(Integer Version) {
		this.Version = Version;
	}
	
	public Integer getVersion() {
		return Version;
	}
	
}
