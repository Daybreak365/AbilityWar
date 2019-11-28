package daybreak.abilitywar.utils.library;

import com.google.common.base.Enums;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * 효과 라이브러리
 *
 * @author Daybreak 새벽
 * @version 1.3 (Minecraft 1.14)
 * @since 2019-02-19
 */
public enum PotionEffects {

	ABSORPTION(Type.POSITIVE),
	BAD_OMEN(Type.NEGATIVE),
	BLINDNESS(Type.NEGATIVE),
	CONFUSION(Type.NEGATIVE),
	CONDUIT_POWER(Type.POSITIVE),
	DOLPHINS_GRACE(Type.POSITIVE),
	DAMAGE_RESISTANCE(Type.POSITIVE),
	FAST_DIGGING(Type.POSITIVE),
	FIRE_RESISTANCE(Type.POSITIVE),
	GLOWING(Type.NEGATIVE),
	HARM(Type.NEGATIVE),
	HEAL(Type.POSITIVE),
	HEALTH_BOOST(Type.POSITIVE),
	HUNGER(Type.NEGATIVE),
	HERO_OF_THE_VILLAGE(Type.POSITIVE),
	INCREASE_DAMAGE(Type.POSITIVE),
	INVISIBILITY(Type.POSITIVE),
	JUMP(Type.POSITIVE),
	LEVITATION(Type.NEGATIVE),
	LUCK(Type.POSITIVE),
	NIGHT_VISION(Type.POSITIVE),
	POISON(Type.NEGATIVE),
	REGENERATION(Type.POSITIVE),
	SATURATION(Type.POSITIVE),
	SLOW(Type.NEGATIVE),
	SLOW_DIGGING(Type.NEGATIVE),
	SLOW_FALLING(Type.POSITIVE),
	SPEED(Type.POSITIVE),
	UNLUCK(Type.NEGATIVE),
	WATER_BREATHING(Type.POSITIVE),
	WEAKNESS(Type.NEGATIVE),
	WITHER(Type.NEGATIVE);

	final PotionEffectType effect;
	final Type type;

	PotionEffects(Type type) {
		this.effect = PotionEffectType.getByName(name());
		this.type = type;
	}

	public void addPotionEffect(LivingEntity entity, int duration, int amplifier, boolean force) {
		if (effect != null) {
			entity.addPotionEffect(new PotionEffect(effect, duration, amplifier), force);
		}
	}

	public void removePotionEffect(LivingEntity entity) {
		if (effect != null) {
			entity.removePotionEffect(effect);
		}
	}

	public boolean isPositive() {
		return type.equals(Type.POSITIVE);
	}
	public boolean isNegative() {
		return type.equals(Type.NEGATIVE);
	}

	public static PotionEffects valueOf(PotionEffectType potionEffectType) {
		return Enums.getIfPresent(PotionEffects.class, potionEffectType.getName()).get();
	}

    private enum Type { POSITIVE, NEGATIVE }

}
