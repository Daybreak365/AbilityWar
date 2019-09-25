package daybreak.abilitywar.utils.library;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;

import daybreak.abilitywar.utils.versioncompat.ServerVersion;

/**
 * 효과 라이브러리
 * @author DayBreak 새벽
 * @since 2019-02-19
 * @version 1.2 (Minecraft 1.14)
 */
public class EffectLib {
	
	private EffectLib() {}
	
	public static Effects ABSORPTION = new Effects("ABSORPTION", 6);
	public static Effects BAD_OMEN = new Effects("BAD_OMEN", 14);
	public static Effects BLINDNESS = new Effects("BLINDNESS", 5);
	public static Effects CONFUSION = new Effects("CONFUSION", 5);
	public static Effects CONDUIT_POWER = new Effects("CONDUIT_POWER", 13);
	public static Effects DOLPHINS_GRACE = new Effects("DOLPHINS_GRACE", 13);
	public static Effects DAMAGE_RESISTANCE = new Effects("DAMAGE_RESISTANCE", 5);
	public static Effects FAST_DIGGING = new Effects("FAST_DIGGING", 5);
	public static Effects FIRE_RESISTANCE = new Effects("FIRE_RESISTANCE", 5);
	public static Effects GLOWING = new Effects("GLOWING", 9);
	public static Effects HARM = new Effects("HARM", 5);
	public static Effects HEAL = new Effects("HEAL", 5);
	public static Effects HEALTH_BOOST = new Effects("HEALTH_BOOST", 6);
	public static Effects HUNGER = new Effects("HUNGER", 5);
	public static Effects HERO_OF_THE_VILLAGE = new Effects("HERO_OF_THE_VILLAGE", 14);
	public static Effects INCREASE_DAMAGE = new Effects("INCREASE_DAMAGE", 5);
	public static Effects INVISIBILITY = new Effects("INVISIBILITY", 5);
	public static Effects JUMP = new Effects("JUMP", 5);
	public static Effects LEVITATION = new Effects("LEVITATION", 9);
	public static Effects LUCK = new Effects("LUCK", 9);
	public static Effects NIGHT_VISION = new Effects("NIGHT_VISION", 5);
	public static Effects POISON = new Effects("POISON", 5);
	public static Effects REGENERATION = new Effects("REGENERATION", 5);
	public static Effects SATURATION = new Effects("SATURATION", 6);
	public static Effects SLOW = new Effects("SLOW", 5);
	public static Effects SLOW_DIGGING = new Effects("SLOW_DIGGING", 5);
	public static Effects SLOW_FALLING = new Effects("SLOW_FALLING", 13);
	public static Effects SPEED = new Effects("SPEED", 5);
	public static Effects UNLUCK = new Effects("UNLUCK", 9);
	public static Effects WATER_BREATHING = new Effects("WATER_BREATHING", 5);
	public static Effects WEAKNESS = new Effects("WEAKNESS", 5);
	public static Effects WITHER = new Effects("WITHER", 4);
	
	public static class Effects {
		
		private final String EffectName;
		private final Integer Version;
		
		private Effects(String EffectName, Integer Version) {
			this.EffectName = EffectName;
			this.Version = Version;
		}

		public void addPotionEffect(LivingEntity entity, int duration, int amplifier, boolean force) {
			if(ServerVersion.getVersion() >= Version) {
				org.bukkit.potion.PotionEffectType effect = org.bukkit.potion.PotionEffectType.getByName(EffectName);
				if(effect != null) {
					entity.addPotionEffect(new PotionEffect(effect, duration, amplifier), force);
				}
			}
		}

		public void removePotionEffect(LivingEntity entity) {
			if(ServerVersion.getVersion() >= Version) {
				org.bukkit.potion.PotionEffectType effect = org.bukkit.potion.PotionEffectType.getByName(EffectName);
				if(effect != null) {
					entity.removePotionEffect(effect);
				}
			}
		}
		
	}
	
}
