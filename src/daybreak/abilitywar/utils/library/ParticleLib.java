/** MIT 라이선스
* 
* Copyright ⓒ 2019 DayBreak
* 
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"),
* to deal in the Software without restriction, including without limitation 
* the rights to use, copy, modify, merge, publish, distribute, sublicense, 
* and/or sell copies of the Software, and to permit persons to whom the 
* Software is furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included 
* in all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES 
* OF * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
* IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
* ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
* DEALINGS IN THE SOFTWARE.
**/

package daybreak.abilitywar.utils.library;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;

import daybreak.abilitywar.config.AbilityWarSettings;
import daybreak.abilitywar.utils.versioncompat.ServerVersion;

/**
 * 파티클 라이브러리
 * @author DayBreak 새벽
 * @since 2019-02-19
 * @version 1.2 (Minecraft 1.14)
 */
public class ParticleLib {

	public static Particles BARRIER = new Particles("BARRIER", "BARRIER", "BARRIER", "BARRIER", "BARRIER", "BARRIER", "BARRIER", "");
	public static Particles BLOCK_CRACK = new Particles("BLOCK_CRACK", "BLOCK_CRACK", "BLOCK_CRACK", "BLOCK_CRACK", "BLOCK_CRACK", "BLOCK_CRACK", "BLOCK_CRACK", "");
	public static Particles BLOCK_DUST = new Particles("BLOCK_DUST", "BLOCK_DUST", "BLOCK_DUST", "BLOCK_DUST", "BLOCK_DUST", "BLOCK_DUST", "BLOCK_DUST", "");
	public static Particles BUBBLE_COLUMN_UP = new Particles("BUBBLE_COLUMN_UP", "BUBBLE_COLUMN_UP", "", "", "", "", "", "");
	public static Particles BUBBLE_POP = new Particles("BUBBLE_POP", "BUBBLE_POP", "", "", "", "", "", "");
	public static Particles CAMPFIRE_COSY_SMOKE = new Particles("CAMPFIRE_COSY_SMOKE", "", "", "", "", "", "", "");
	public static Particles CAMPFIRE_SIGNAL_SMOKE = new Particles("CAMPFIRE_SIGNAL_SMOKE", "", "", "", "", "", "", "");
	public static Particles CLOUD = new Particles("CLOUD", "CLOUD", "CLOUD", "CLOUD", "CLOUD", "CLOUD", "CLOUD", "");
	public static Particles COMPOSTER = new Particles("COMPOSTER", "", "", "", "", "", "", "");
	public static Particles CRIT = new Particles("CRIT", "CRIT", "CRIT", "CRIT", "CRIT", "CRIT", "CRIT", "");
	public static Particles CRIT_MAGIC = new Particles("CRIT_MAGIC", "CRIT_MAGIC", "CRIT_MAGIC", "CRIT_MAGIC", "CRIT_MAGIC", "CRIT_MAGIC", "CRIT_MAGIC", "");
	public static Particles CURRENT_DOWN = new Particles("CURRENT_DOWN", "CURRENT_DOWN", "", "", "", "", "", "");
	public static Particles DAMAGE_INDICATOR = new Particles("DAMAGE_INDICATOR", "DAMAGE_INDICATOR", "DAMAGE_INDICATOR", "DAMAGE_INDICATOR", "DAMAGE_INDICATOR", "DAMAGE_INDICATOR", "", "");
	public static Particles DOLPHIN = new Particles("DOLPHIN", "DOLPHIN", "", "", "", "", "", "");
	public static Particles DRAGON_BREATH = new Particles("DRAGON_BREATH", "DRAGON_BREATH", "DRAGON_BREATH", "DRAGON_BREATH", "DRAGON_BREATH", "DRAGON_BREATH", "", "");
	public static Particles DRIP_LAVA = new Particles("DRIP_LAVA", "DRIP_LAVA", "DRIP_LAVA", "DRIP_LAVA", "DRIP_LAVA", "DRIP_LAVA", "DRIP_LAVA", "");
	public static Particles DRIP_WATER = new Particles("DRIP_WATER", "DRIP_WATER", "DRIP_WATER", "DRIP_WATER", "DRIP_WATER", "DRIP_WATER", "DRIP_WATER", "");
	public static Particles ENCHANTMENT_TABLE = new Particles("ENCHANTMENT_TABLE", "ENCHANTMENT_TABLE", "ENCHANTMENT_TABLE", "ENCHANTMENT_TABLE", "ENCHANTMENT_TABLE", "ENCHANTMENT_TABLE", "ENCHANTMENT_TABLE", "");
	public static Particles END_ROD = new Particles("END_ROD", "END_ROD", "END_ROD", "END_ROD", "END_ROD", "END_ROD", "", "");
	public static Particles EXPLOSION_HUGE = new Particles("EXPLOSION_HUGE", "EXPLOSION_HUGE", "EXPLOSION_HUGE", "EXPLOSION_HUGE", "EXPLOSION_HUGE", "EXPLOSION_HUGE", "EXPLOSION_HUGE", "");
	public static Particles EXPLOSION_LARGE = new Particles("EXPLOSION_LARGE", "EXPLOSION_LARGE", "EXPLOSION_LARGE", "EXPLOSION_LARGE", "EXPLOSION_LARGE", "EXPLOSION_LARGE", "EXPLOSION_LARGE", "");
	public static Particles EXPLOSION_NORMAL = new Particles("EXPLOSION_NORMAL", "EXPLOSION_NORMAL", "EXPLOSION_NORMAL", "EXPLOSION_NORMAL", "EXPLOSION_NORMAL", "EXPLOSION_NORMAL", "EXPLOSION_NORMAL", "");
	public static Particles FALLING_DUST = new Particles("FALLING_DUST", "FALLING_DUST", "FALLING_DUST", "FALLING_DUST", "FALLING_DUST", "", "", "");
	public static Particles FALLING_LAVA = new Particles("FALLING_LAVA", "", "", "", "", "", "", "");
	public static Particles FALLING_WATER = new Particles("FALLING_WATER", "", "", "", "", "", "", "");
	public static Particles FIREWORKS_SPARK = new Particles("FIREWORKS_SPARK", "FIREWORKS_SPARK", "FIREWORKS_SPARK", "FIREWORKS_SPARK", "FIREWORKS_SPARK", "FIREWORKS_SPARK", "FIREWORKS_SPARK", "");
	public static Particles FLAME = new Particles("FLAME", "FLAME", "FLAME", "FLAME", "FLAME", "FLAME", "FLAME", "MOBSPAWNER_FLAMES");
	public static Particles FLASH = new Particles("FLASH", "", "", "", "", "", "", "");
	public static Particles FOOTSTEP = new Particles("", "", "FOOTSTEP", "FOOTSTEP", "FOOTSTEP", "FOOTSTEP", "FOOTSTEP", "");
	public static Particles HEART = new Particles("HEART", "HEART", "HEART", "HEART", "HEART", "HEART", "HEART", "");
	public static Particles ITEM_CRACK = new Particles("ITEM_CRACK", "ITEM_CRACK", "ITEM_CRACK", "ITEM_CRACK", "ITEM_CRACK", "ITEM_CRACK", "ITEM_CRACK", "");
	public static Particles LANDING_LAVA = new Particles("LANDING_LAVA", "", "", "", "", "", "", "");
	public static Particles LAVA = new Particles("LAVA", "LAVA", "LAVA", "ITEM_TAKE", "ITEM_TAKE", "ITEM_TAKE", "ITEM_TAKE", "");
	public static Particles MOB_APPEARANCE = new Particles("MOB_APPEARANCE", "MOB_APPEARANCE", "LAVA", "LAVA", "LAVA", "LAVA", "LAVA", "");
	public static Particles NAUTILUS = new Particles("NAUTILUS", "NAUTILUS", "MOB_APPEARANCE", "MOB_APPEARANCE", "MOB_APPEARANCE", "MOB_APPEARANCE", "MOB_APPEARANCE", "");
	public static Particles NOTE = new Particles("NOTE", "NOTE", "NOTE", "NOTE", "NOTE", "NOTE", "NOTE", "");
	public static Particles PORTAL = new Particles("PORTAL", "PORTAL", "PORTAL", "PORTAL", "PORTAL", "PORTAL", "PORTAL", "");
	public static Particles REDSTONE = new Particles("REDSTONE", "REDSTONE", "REDSTONE", "REDSTONE", "REDSTONE", "REDSTONE", "REDSTONE", "");
	public static Particles SLIME = new Particles("SLIME", "SLIME", "SLIME", "SLIME", "SLIME", "SLIME", "SLIME", "");
	public static Particles SMOKE_LARGE = new Particles("SMOKE_LARGE", "SMOKE_LARGE", "SMOKE_LARGE", "SMOKE_LARGE", "SMOKE_LARGE", "SMOKE_LARGE", "SMOKE_LARGE", "");
	public static Particles SMOKE_NORMAL = new Particles("SMOKE_NORMAL", "SMOKE_NORMAL", "SMOKE_NORMAL", "SMOKE_NORMAL", "SMOKE_NORMAL", "SMOKE_NORMAL", "SMOKE_NORMAL", "SMOKE");
	public static Particles SNEEZE = new Particles("SNEEZE", "", "", "", "", "", "", "");
	public static Particles SNOWBALL = new Particles("SNOWBALL", "SNOWBALL", "SNOWBALL", "SNOWBALL", "SNOWBALL", "SNOWBALL", "SNOWBALL", "");
	public static Particles SNOW_SHOVEL = new Particles("SNOW_SHOVEL", "SNOW_SHOVEL", "SNOW_SHOVEL", "SNOW_SHOVEL", "SNOW_SHOVEL", "SNOW_SHOVEL", "SNOW_SHOVEL", "");
	public static Particles SPELL = new Particles("SPELL", "SPELL", "SPELL", "SPELL", "SPELL", "SPELL", "SPELL", "");
	public static Particles SPELL_INSTANT = new Particles("SPELL_INSTANT", "SPELL_INSTANT", "SPELL_INSTANT", "SPELL_INSTANT", "SPELL_INSTANT", "SPELL_INSTANT", "SPELL_INSTANT", "");
	public static Particles SPELL_MOB = new Particles("SPELL_MOB", "SPELL_MOB", "SPELL_MOB", "SPELL_MOB", "SPELL_MOB", "SPELL_MOB", "SPELL_MOB", "");
	public static Particles SPELL_MOB_AMBIENT = new Particles("SPELL_MOB_AMBIENT", "SPELL_MOB_AMBIENT", "SPELL_MOB_AMBIENT", "SPELL_MOB_AMBIENT", "SPELL_MOB_AMBIENT", "SPELL_MOB_AMBIENT", "SPELL_MOB_AMBIENT", "");
	public static Particles SPELL_WITCH = new Particles("SPELL_WITCH", "SPELL_WITCH", "SPELL_WITCH", "SPELL_WITCH", "SPELL_WITCH", "SPELL_WITCH", "SPELL_WITCH", "");
	public static Particles SPIT = new Particles("SPIT", "SPIT", "SPIT", "SPIT", "", "", "", "");
	public static Particles SQUID_INK = new Particles("SQUID_INK", "SQUID_INK", "", "", "", "", "", "");
	public static Particles SUSPENDED = new Particles("SUSPENDED", "SUSPENDED", "SUSPENDED", "SUSPENDED", "SUSPENDED", "SUSPENDED", "SUSPENDED", "");
	public static Particles SUSPENDED_DEPTH = new Particles("SUSPENDED_DEPTH", "SUSPENDED_DEPTH", "SUSPENDED_DEPTH", "SUSPENDED_DEPTH", "SUSPENDED_DEPTH", "SUSPENDED_DEPTH", "SUSPENDED_DEPTH", "");
	public static Particles SWEEP_ATTACK = new Particles("SWEEP_ATTACK", "SWEEP_ATTACK", "SWEEP_ATTACK", "SWEEP_ATTACK", "SWEEP_ATTACK", "SWEEP_ATTACK", "", "");
	public static Particles TOTEM = new Particles("TOTEM", "TOTEM", "TOTEM", "TOTEM", "", "", "", "");
	public static Particles TOWN_AURA = new Particles("TOWN_AURA", "TOWN_AURA", "TOWN_AURA", "TOWN_AURA", "TOWN_AURA", "TOWN_AURA", "TOWN_AURA", "");
	public static Particles VILLAGER_ANGRY = new Particles("VILLAGER_ANGRY", "VILLAGER_ANGRY", "VILLAGER_ANGRY", "VILLAGER_ANGRY", "VILLAGER_ANGRY", "VILLAGER_ANGRY", "VILLAGER_ANGRY", "");
	public static Particles VILLAGER_HAPPY = new Particles("VILLAGER_HAPPY", "VILLAGER_HAPPY", "VILLAGER_HAPPY", "VILLAGER_HAPPY", "VILLAGER_HAPPY", "VILLAGER_HAPPY", "VILLAGER_HAPPY", "");
	public static Particles WATER_BUBBLE = new Particles("WATER_BUBBLE", "WATER_BUBBLE", "WATER_BUBBLE", "WATER_BUBBLE", "WATER_BUBBLE", "WATER_BUBBLE", "WATER_BUBBLE", "");
	public static Particles WATER_DROP = new Particles("WATER_DROP", "WATER_DROP", "WATER_DROP", "WATER_DROP", "WATER_DROP", "WATER_DROP", "WATER_DROP", "");
	public static Particles WATER_SPLASH = new Particles("WATER_SPLASH", "WATER_SPLASH", "WATER_SPLASH", "WATER_SPLASH", "WATER_SPLASH", "WATER_SPLASH", "WATER_SPLASH", "");
	public static Particles WATER_WAKE = new Particles("WATER_WAKE", "WATER_WAKE", "WATER_WAKE", "WATER_WAKE", "WATER_WAKE", "WATER_WAKE", "WATER_WAKE", "");

	private ParticleLib() {}

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
			if (AbilityWarSettings.getVisualEffect()) {
				if (particle != null) {
					if(p != null) {
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

		public void spawnParticle(Location l, float offsetX, float offsetY, float offsetZ, int Count) {
			this.spawnParticle(l, offsetX, offsetY, offsetZ, Count, null);
		}

		public void spawnParticle(Player p, Location l, float offsetX, float offsetY, float offsetZ, int Count) {
			this.spawnParticle(p, l, offsetX, offsetY, offsetZ, Count, null);
		}

		public void spawnParticle(Player p, Location l, RGB rgb, int Count) {
			if(ServerVersion.getVersion() >= 13) {
				if(particle.getDataType() != null && particle.getDataType().equals(DustOptions.class)) {
					this.spawnParticle(p, l, 0, 0, 0, Count, new DustOptions(Color.fromRGB(rgb.getRedInt(), rgb.getGreenInt(), rgb.getBlueInt()), 1));
				} else {
					this.spawnParticle(p, l, 0, 0, 0, Count, null);
				}
			} else {
				this.spawnParticle(p, l, rgb.getRed(), rgb.getGreen(), rgb.getBlue(), Count);
			}
		}

		public void spawnParticle(Location l, RGB rgb, int Count) {
			if(ServerVersion.getVersion() >= 13) {
				if(particle.getDataType() != null && particle.getDataType().equals(DustOptions.class)) {
					this.spawnParticle(l, 0, 0, 0, Count, new DustOptions(Color.fromRGB(rgb.getRedInt(), rgb.getGreenInt(), rgb.getBlueInt()), 1));
				} else {
					this.spawnParticle(l, 0, 0, 0, Count, null);
				}
			} else {
				this.spawnParticle(l, rgb.getRed(), rgb.getGreen(), rgb.getBlue(), Count);
			}
		}

	}
	
	public static class RGB {
		
		private final int Red;
		private final int Green;
		private final int Blue;
		
		public RGB(int Red, int Green, int Blue) {
			this.Red = Red;
			this.Green = Green;
			this.Blue = Blue;
		}

		public float getRed() {
			return (float) Red / 255;
		}

		public float getGreen() {
			return (float) Green / 255;
		}

		public float getBlue() {
			return (float) Blue / 255;
		}

		public int getRedInt() {
			return Red;
		}

		public int getGreenInt() {
			return Green;
		}

		public int getBlueInt() {
			return Blue;
		}
		
	}

}
