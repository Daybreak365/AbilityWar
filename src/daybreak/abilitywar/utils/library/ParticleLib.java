/**
 * MIT 라이선스
 * <p>
 * Copyright ⓒ 2019 DayBreak
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 **/

package daybreak.abilitywar.utils.library;

import daybreak.abilitywar.config.AbilityWarSettings.Settings;
import daybreak.abilitywar.utils.versioncompat.ServerVersion;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;

/**
 * 파티클 라이브러리
 *
 * @author Daybreak 새벽
 * @version 1.3 (Minecraft 1.14)
 * @since 2019-02-19
 */
public class ParticleLib {

	private ParticleLib() {
	}

	public static final Particles BARRIER = new Particles("BARRIER", "BARRIER", "BARRIER", "BARRIER", "BARRIER", "BARRIER", "BARRIER", "");
	public static final Particles BLOCK_CRACK = new Particles("BLOCK_CRACK", "BLOCK_CRACK", "BLOCK_CRACK", "BLOCK_CRACK", "BLOCK_CRACK", "BLOCK_CRACK", "BLOCK_CRACK", "");
	public static final Particles BLOCK_DUST = new Particles("BLOCK_DUST", "BLOCK_DUST", "BLOCK_DUST", "BLOCK_DUST", "BLOCK_DUST", "BLOCK_DUST", "BLOCK_DUST", "");
	public static final Particles BUBBLE_COLUMN_UP = new Particles("BUBBLE_COLUMN_UP", "BUBBLE_COLUMN_UP", "", "", "", "", "", "");
	public static final Particles BUBBLE_POP = new Particles("BUBBLE_POP", "BUBBLE_POP", "", "", "", "", "", "");
	public static final Particles CAMPFIRE_COSY_SMOKE = new Particles("CAMPFIRE_COSY_SMOKE", "", "", "", "", "", "", "");
	public static final Particles CAMPFIRE_SIGNAL_SMOKE = new Particles("CAMPFIRE_SIGNAL_SMOKE", "", "", "", "", "", "", "");
	public static final Particles CLOUD = new Particles("CLOUD", "CLOUD", "CLOUD", "CLOUD", "CLOUD", "CLOUD", "CLOUD", "");
	public static final Particles COMPOSTER = new Particles("COMPOSTER", "", "", "", "", "", "", "");
	public static final Particles CRIT = new Particles("CRIT", "CRIT", "CRIT", "CRIT", "CRIT", "CRIT", "CRIT", "");
	public static final Particles CRIT_MAGIC = new Particles("CRIT_MAGIC", "CRIT_MAGIC", "CRIT_MAGIC", "CRIT_MAGIC", "CRIT_MAGIC", "CRIT_MAGIC", "CRIT_MAGIC", "");
	public static final Particles CURRENT_DOWN = new Particles("CURRENT_DOWN", "CURRENT_DOWN", "", "", "", "", "", "");
	public static final Particles DAMAGE_INDICATOR = new Particles("DAMAGE_INDICATOR", "DAMAGE_INDICATOR", "DAMAGE_INDICATOR", "DAMAGE_INDICATOR", "DAMAGE_INDICATOR", "DAMAGE_INDICATOR", "", "");
	public static final Particles DOLPHIN = new Particles("DOLPHIN", "DOLPHIN", "", "", "", "", "", "");
	public static final Particles DRAGON_BREATH = new Particles("DRAGON_BREATH", "DRAGON_BREATH", "DRAGON_BREATH", "DRAGON_BREATH", "DRAGON_BREATH", "DRAGON_BREATH", "", "");
	public static final Particles DRIP_LAVA = new Particles("DRIP_LAVA", "DRIP_LAVA", "DRIP_LAVA", "DRIP_LAVA", "DRIP_LAVA", "DRIP_LAVA", "DRIP_LAVA", "");
	public static final Particles DRIP_WATER = new Particles("DRIP_WATER", "DRIP_WATER", "DRIP_WATER", "DRIP_WATER", "DRIP_WATER", "DRIP_WATER", "DRIP_WATER", "");
	public static final Particles ENCHANTMENT_TABLE = new Particles("ENCHANTMENT_TABLE", "ENCHANTMENT_TABLE", "ENCHANTMENT_TABLE", "ENCHANTMENT_TABLE", "ENCHANTMENT_TABLE", "ENCHANTMENT_TABLE", "ENCHANTMENT_TABLE", "");
	public static final Particles END_ROD = new Particles("END_ROD", "END_ROD", "END_ROD", "END_ROD", "END_ROD", "END_ROD", "", "");
	public static final Particles EXPLOSION_HUGE = new Particles("EXPLOSION_HUGE", "EXPLOSION_HUGE", "EXPLOSION_HUGE", "EXPLOSION_HUGE", "EXPLOSION_HUGE", "EXPLOSION_HUGE", "EXPLOSION_HUGE", "");
	public static final Particles EXPLOSION_LARGE = new Particles("EXPLOSION_LARGE", "EXPLOSION_LARGE", "EXPLOSION_LARGE", "EXPLOSION_LARGE", "EXPLOSION_LARGE", "EXPLOSION_LARGE", "EXPLOSION_LARGE", "");
	public static final Particles EXPLOSION_NORMAL = new Particles("EXPLOSION_NORMAL", "EXPLOSION_NORMAL", "EXPLOSION_NORMAL", "EXPLOSION_NORMAL", "EXPLOSION_NORMAL", "EXPLOSION_NORMAL", "EXPLOSION_NORMAL", "");
	public static final Particles FALLING_DUST = new Particles("FALLING_DUST", "FALLING_DUST", "FALLING_DUST", "FALLING_DUST", "FALLING_DUST", "", "", "");
	public static final Particles FALLING_LAVA = new Particles("FALLING_LAVA", "", "", "", "", "", "", "");
	public static final Particles FALLING_WATER = new Particles("FALLING_WATER", "", "", "", "", "", "", "");
	public static final Particles FIREWORKS_SPARK = new Particles("FIREWORKS_SPARK", "FIREWORKS_SPARK", "FIREWORKS_SPARK", "FIREWORKS_SPARK", "FIREWORKS_SPARK", "FIREWORKS_SPARK", "FIREWORKS_SPARK", "");
	public static final Particles FLAME = new Particles("FLAME", "FLAME", "FLAME", "FLAME", "FLAME", "FLAME", "FLAME", "MOBSPAWNER_FLAMES");
	public static final Particles FLASH = new Particles("FLASH", "", "", "", "", "", "", "");
	public static final Particles FOOTSTEP = new Particles("", "", "FOOTSTEP", "FOOTSTEP", "FOOTSTEP", "FOOTSTEP", "FOOTSTEP", "");
	public static final Particles HEART = new Particles("HEART", "HEART", "HEART", "HEART", "HEART", "HEART", "HEART", "");
	public static final Particles ITEM_CRACK = new Particles("ITEM_CRACK", "ITEM_CRACK", "ITEM_CRACK", "ITEM_CRACK", "ITEM_CRACK", "ITEM_CRACK", "ITEM_CRACK", "");
	public static final Particles LANDING_LAVA = new Particles("LANDING_LAVA", "", "", "", "", "", "", "");
	public static final Particles LAVA = new Particles("LAVA", "LAVA", "LAVA", "ITEM_TAKE", "ITEM_TAKE", "ITEM_TAKE", "ITEM_TAKE", "");
	public static final Particles MOB_APPEARANCE = new Particles("MOB_APPEARANCE", "MOB_APPEARANCE", "LAVA", "LAVA", "LAVA", "LAVA", "LAVA", "");
	public static final Particles NAUTILUS = new Particles("NAUTILUS", "NAUTILUS", "MOB_APPEARANCE", "MOB_APPEARANCE", "MOB_APPEARANCE", "MOB_APPEARANCE", "MOB_APPEARANCE", "");
	public static final Particles NOTE = new Particles("NOTE", "NOTE", "NOTE", "NOTE", "NOTE", "NOTE", "NOTE", "");
	public static final Particles PORTAL = new Particles("PORTAL", "PORTAL", "PORTAL", "PORTAL", "PORTAL", "PORTAL", "PORTAL", "");
	public static final Particles REDSTONE = new Particles("REDSTONE", "REDSTONE", "REDSTONE", "REDSTONE", "REDSTONE", "REDSTONE", "REDSTONE", "");
	public static final Particles SLIME = new Particles("SLIME", "SLIME", "SLIME", "SLIME", "SLIME", "SLIME", "SLIME", "");
	public static final Particles SMOKE_LARGE = new Particles("SMOKE_LARGE", "SMOKE_LARGE", "SMOKE_LARGE", "SMOKE_LARGE", "SMOKE_LARGE", "SMOKE_LARGE", "SMOKE_LARGE", "");
	public static final Particles SMOKE_NORMAL = new Particles("SMOKE_NORMAL", "SMOKE_NORMAL", "SMOKE_NORMAL", "SMOKE_NORMAL", "SMOKE_NORMAL", "SMOKE_NORMAL", "SMOKE_NORMAL", "SMOKE");
	public static final Particles SNEEZE = new Particles("SNEEZE", "", "", "", "", "", "", "");
	public static final Particles SNOWBALL = new Particles("SNOWBALL", "SNOWBALL", "SNOWBALL", "SNOWBALL", "SNOWBALL", "SNOWBALL", "SNOWBALL", "");
	public static final Particles SNOW_SHOVEL = new Particles("SNOW_SHOVEL", "SNOW_SHOVEL", "SNOW_SHOVEL", "SNOW_SHOVEL", "SNOW_SHOVEL", "SNOW_SHOVEL", "SNOW_SHOVEL", "");
	public static final Particles SPELL = new Particles("SPELL", "SPELL", "SPELL", "SPELL", "SPELL", "SPELL", "SPELL", "");
	public static final Particles SPELL_INSTANT = new Particles("SPELL_INSTANT", "SPELL_INSTANT", "SPELL_INSTANT", "SPELL_INSTANT", "SPELL_INSTANT", "SPELL_INSTANT", "SPELL_INSTANT", "");
	public static final Particles SPELL_MOB = new Particles("SPELL_MOB", "SPELL_MOB", "SPELL_MOB", "SPELL_MOB", "SPELL_MOB", "SPELL_MOB", "SPELL_MOB", "");
	public static final Particles SPELL_MOB_AMBIENT = new Particles("SPELL_MOB_AMBIENT", "SPELL_MOB_AMBIENT", "SPELL_MOB_AMBIENT", "SPELL_MOB_AMBIENT", "SPELL_MOB_AMBIENT", "SPELL_MOB_AMBIENT", "SPELL_MOB_AMBIENT", "");
	public static final Particles SPELL_WITCH = new Particles("SPELL_WITCH", "SPELL_WITCH", "SPELL_WITCH", "SPELL_WITCH", "SPELL_WITCH", "SPELL_WITCH", "SPELL_WITCH", "");
	public static final Particles SPIT = new Particles("SPIT", "SPIT", "SPIT", "SPIT", "", "", "", "");
	public static final Particles SQUID_INK = new Particles("SQUID_INK", "SQUID_INK", "", "", "", "", "", "");
	public static final Particles SUSPENDED = new Particles("SUSPENDED", "SUSPENDED", "SUSPENDED", "SUSPENDED", "SUSPENDED", "SUSPENDED", "SUSPENDED", "");
	public static final Particles SUSPENDED_DEPTH = new Particles("SUSPENDED_DEPTH", "SUSPENDED_DEPTH", "SUSPENDED_DEPTH", "SUSPENDED_DEPTH", "SUSPENDED_DEPTH", "SUSPENDED_DEPTH", "SUSPENDED_DEPTH", "");
	public static final Particles SWEEP_ATTACK = new Particles("SWEEP_ATTACK", "SWEEP_ATTACK", "SWEEP_ATTACK", "SWEEP_ATTACK", "SWEEP_ATTACK", "SWEEP_ATTACK", "", "");
	public static final Particles TOTEM = new Particles("TOTEM", "TOTEM", "TOTEM", "TOTEM", "", "", "", "");
	public static final Particles TOWN_AURA = new Particles("TOWN_AURA", "TOWN_AURA", "TOWN_AURA", "TOWN_AURA", "TOWN_AURA", "TOWN_AURA", "TOWN_AURA", "");
	public static final Particles VILLAGER_ANGRY = new Particles("VILLAGER_ANGRY", "VILLAGER_ANGRY", "VILLAGER_ANGRY", "VILLAGER_ANGRY", "VILLAGER_ANGRY", "VILLAGER_ANGRY", "VILLAGER_ANGRY", "");
	public static final Particles VILLAGER_HAPPY = new Particles("VILLAGER_HAPPY", "VILLAGER_HAPPY", "VILLAGER_HAPPY", "VILLAGER_HAPPY", "VILLAGER_HAPPY", "VILLAGER_HAPPY", "VILLAGER_HAPPY", "");
	public static final Particles WATER_BUBBLE = new Particles("WATER_BUBBLE", "WATER_BUBBLE", "WATER_BUBBLE", "WATER_BUBBLE", "WATER_BUBBLE", "WATER_BUBBLE", "WATER_BUBBLE", "");
	public static final Particles WATER_DROP = new Particles("WATER_DROP", "WATER_DROP", "WATER_DROP", "WATER_DROP", "WATER_DROP", "WATER_DROP", "WATER_DROP", "");
	public static final Particles WATER_SPLASH = new Particles("WATER_SPLASH", "WATER_SPLASH", "WATER_SPLASH", "WATER_SPLASH", "WATER_SPLASH", "WATER_SPLASH", "WATER_SPLASH", "");
	public static final Particles WATER_WAKE = new Particles("WATER_WAKE", "WATER_WAKE", "WATER_WAKE", "WATER_WAKE", "WATER_WAKE", "WATER_WAKE", "WATER_WAKE", "");

	public static class Particles {

		private String particleName = "";
		private Particle particle = null;

		private Particles(String Name14, String Name13, String Name12, String Name11, String Name10, String Name9,
						  String Name8, String Name7) {
			switch (ServerVersion.getVersion()) {
				case 14:
					particleName = Name14;
					particle = getParticle();
					break;
				case 13:
					particleName = Name13;
					particle = getParticle();
					break;
				case 12:
					particleName = Name12;
					particle = getParticle();
					break;
			}
		}

		/**
		 * 1.9버전 이상
		 */
		private Particle getParticle() {
			Particle particle = null;

			for (Particle p : Particle.values()) {
				if (p.toString().equalsIgnoreCase(particleName)) {
					particle = p;
				}
			}

			return particle;
		}

		public <T> void spawnParticle(Player p, Location l, float offsetX, float offsetY, float offsetZ, int Count, T t) {
			if (Settings.getVisualEffect()) {
				if (particle != null) {
					if (p != null) {
						p.spawnParticle(particle, l, Count, offsetX, offsetY, offsetZ, t);
					} else {
						l.getWorld().spawnParticle(particle, l, Count, offsetX, offsetY, offsetZ, t);
					}
				}
			}
		}

		public <T> void spawnParticle(Location l, float offsetX, float offsetY, float offsetZ, int Count, T t) {
			this.spawnParticle(null, l, offsetX, offsetY, offsetZ, Count, t);
		}

		public void spawnParticle(Player p, Location l, float offsetX, float offsetY, float offsetZ, int Count) {
			this.spawnParticle(p, l, offsetX, offsetY, offsetZ, Count, null);
		}

		public void spawnParticle(Location l, float offsetX, float offsetY, float offsetZ, int Count) {
			this.spawnParticle(l, offsetX, offsetY, offsetZ, Count, null);
		}

		public void spawnParticle(Player p, Location l) {
			this.spawnParticle(p, l, 0, 0, 0, 1, null);
		}

		public void spawnParticle(Location l) {
			this.spawnParticle(l, 0, 0, 0, 1, null);
		}

		public void spawnParticle(Player p, Location l, RGB rgb) {
			if (ServerVersion.getVersion() >= 13) {
				if (particle.getDataType().equals(DustOptions.class)) {
					this.spawnParticle(p, l, 0, 0, 0, 0, new DustOptions(Color.fromRGB(rgb.getRedInt(), rgb.getGreenInt(), rgb.getBlueInt()), 1));
				} else {
					this.spawnParticle(p, l, 0, 0, 0, 0, null);
				}
			} else {
				this.spawnParticle(p, l, rgb.getRed(), rgb.getGreen(), rgb.getBlue(), 0);
			}
		}

		public void spawnParticle(Location l, RGB rgb) {
			if (ServerVersion.getVersion() >= 13) {
				if (particle.getDataType().equals(DustOptions.class)) {
					this.spawnParticle(l, 0, 0, 0, 0, new DustOptions(Color.fromRGB(rgb.getRedInt(), rgb.getGreenInt(), rgb.getBlueInt()), 1));
				} else {
					this.spawnParticle(l, 0, 0, 0, 0, null);
				}
			} else {
				this.spawnParticle(l, rgb.getRed(), rgb.getGreen(), rgb.getBlue(), 0);
			}
		}

	}

	public static class RGB {

		private final int red;
		private final int green;
		private final int blue;

		public RGB(int Red, int Green, int Blue) {
			this.red = Red;
			this.green = Green;
			this.blue = Blue;
		}

		public float getRed() {
			return (float) red / 255;
		}

		public float getGreen() {
			return (float) green / 255;
		}

		public float getBlue() {
			return (float) blue / 255;
		}

		public int getRedInt() {
			return red;
		}

		public int getGreenInt() {
			return green;
		}

		public int getBlueInt() {
			return blue;
		}

		public Color getColor() {
			return Color.fromRGB(red, green, blue);
		}

	}

}
