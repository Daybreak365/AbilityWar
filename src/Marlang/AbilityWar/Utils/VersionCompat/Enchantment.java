package Marlang.AbilityWar.Utils.VersionCompat;

public enum Enchantment {
	
	PROTECTION_ENVIRONMENTAL(5),
	PROTECTION_FIRE(5),
	PROTECTION_FALL(5),
	PROTECTION_EXPLOSIONS(5),
	PROTECTION_PROJECTILE(5),
	OXYGEN(5),
	WATER_WORKER(5),
	MENDING(9),
	THORNS(5),
	VANISHING_CURSE(11),
	DEPTH_STRIDER(8),
	FROST_WALKER(9),
	BINDING_CURSE(11),
	DAMAGE_ALL(5),
	DAMAGE_UNDEAD(5),
	DAMAGE_ARTHROPODS(5),
	KNOCKBACK(5),
	FIRE_ASPECT(5),
	LOOT_BONUS_MOBS(5),
	SWEEPING_EDGE(12),
	DIG_SPEED(5),
	SILK_TOUCH(5),
	DURABILITY(5),
	LOOT_BONUS_BLOCKS(5),
	ARROW_DAMAGE(5),
	ARROW_KNOCKBACK(5),
	ARROW_FIRE(5),
	ARROW_INFINITE(5),
	LUCK(7),
	LURE(7);

	private Integer Version;

	private Enchantment(Integer Version) {
		this.Version = Version;
	}
	
	public Integer getVersion() {
		return Version;
	}
	
}
