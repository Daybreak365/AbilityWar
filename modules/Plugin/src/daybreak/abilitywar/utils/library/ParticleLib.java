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

import com.google.common.base.Enums;
import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

/**
 * 파티클 라이브러리
 *
 * @author Daybreak 새벽
 * @version 2.0
 * @since 2019-02-19
 */
public class ParticleLib {

	public static final SimpleParticle BARRIER = new SimpleParticle("BARRIER");
	public static final BlockParticle BLOCK_CRACK = new BlockParticle("BLOCK_CRACK");
	public static final SimpleParticle BLOCK_DUST = new SimpleParticle("BLOCK_DUST");
	public static final SimpleParticle CLOUD = new SimpleParticle("CLOUD");
	public static final SimpleParticle CRIT = new SimpleParticle("CRIT");
	public static final SimpleParticle CRIT_MAGIC = new SimpleParticle("CRIT_MAGIC");
	public static final SimpleParticle DAMAGE_INDICATOR = new SimpleParticle("DAMAGE_INDICATOR");
	public static final SimpleParticle DRAGON_BREATH = new SimpleParticle("DRAGON_BREATH");
	public static final SimpleParticle DRIP_LAVA = new SimpleParticle("DRIP_LAVA");
	public static final SimpleParticle DRIP_WATER = new SimpleParticle("DRIP_WATER");
	public static final SimpleParticle ENCHANTMENT_TABLE = new SimpleParticle("ENCHANTMENT_TABLE");
	public static final SimpleParticle END_ROD = new SimpleParticle("END_ROD");
	public static final SimpleParticle EXPLOSION_HUGE = new SimpleParticle("EXPLOSION_HUGE");
	public static final SimpleParticle EXPLOSION_LARGE = new SimpleParticle("EXPLOSION_LARGE");
	public static final SimpleParticle EXPLOSION_NORMAL = new SimpleParticle("EXPLOSION_NORMAL");
	public static final SimpleParticle FALLING_DUST = new SimpleParticle("FALLING_DUST");
	public static final SimpleParticle FIREWORKS_SPARK = new SimpleParticle("FIREWORKS_SPARK");
	public static final SimpleParticle FLAME = new SimpleParticle("FLAME");
	public static final SimpleParticle HEART = new SimpleParticle("HEART");
	public static final SimpleParticle ITEM_CRACK = new SimpleParticle("ITEM_CRACK");
	public static final SimpleParticle LAVA = new SimpleParticle("LAVA");
	public static final SimpleParticle MOB_APPEARANCE = new SimpleParticle("MOB_APPEARANCE");
	public static final SimpleParticle NOTE = new SimpleParticle("NOTE");
	public static final SimpleParticle PORTAL = new SimpleParticle("PORTAL");
	public static final ColouredParticle REDSTONE = new ColouredParticle("REDSTONE");
	public static final SimpleParticle SLIME = new SimpleParticle("SLIME");
	public static final SimpleParticle SMOKE_LARGE = new SimpleParticle("SMOKE_LARGE");
	public static final SimpleParticle SMOKE_NORMAL = new SimpleParticle("SMOKE_NORMAL");
	public static final SimpleParticle SNOWBALL = new SimpleParticle("SNOWBALL");
	public static final SimpleParticle SNOW_SHOVEL = new SimpleParticle("SNOW_SHOVEL");
	public static final SimpleParticle SPELL = new SimpleParticle("SPELL");
	public static final SimpleParticle SPELL_INSTANT = new SimpleParticle("SPELL_INSTANT");
	public static final ColouredParticle SPELL_MOB = new ColouredParticle("SPELL_MOB");
	public static final ColouredParticle SPELL_MOB_AMBIENT = new ColouredParticle("SPELL_MOB_AMBIENT");
	public static final SimpleParticle SPELL_WITCH = new SimpleParticle("SPELL_WITCH");
	public static final SimpleParticle SPIT = new SimpleParticle("SPIT");
	public static final SimpleParticle SUSPENDED = new SimpleParticle("SUSPENDED");
	public static final SimpleParticle SWEEP_ATTACK = new SimpleParticle("SWEEP_ATTACK");
	public static final SimpleParticle TOTEM = new SimpleParticle("TOTEM");
	public static final SimpleParticle TOWN_AURA = new SimpleParticle("TOWN_AURA");
	public static final SimpleParticle VILLAGER_ANGRY = new SimpleParticle("VILLAGER_ANGRY");
	public static final SimpleParticle VILLAGER_HAPPY = new SimpleParticle("VILLAGER_HAPPY");
	public static final SimpleParticle WATER_BUBBLE = new SimpleParticle("WATER_BUBBLE");
	public static final SimpleParticle WATER_DROP = new SimpleParticle("WATER_DROP");
	public static final SimpleParticle WATER_SPLASH = new SimpleParticle("WATER_SPLASH");
	public static final SimpleParticle WATER_WAKE = new SimpleParticle("WATER_WAKE");

	public static class SimpleParticle {

		protected final Particle particle;

		private SimpleParticle(String name) {
			this.particle = Enums.getIfPresent(Particle.class, name).orNull();
		}

		public <T> void spawnParticle(Player player, Location location, double offsetX, double offsetY, double offsetZ, int count, double extra, T customData) {
			if (Configuration.Settings.getVisualEffect() && this.particle != null) {
				player.spawnParticle(this.particle, location, count, offsetX, offsetY, offsetZ, extra, customData);
			}
		}

		public <T> void spawnParticle(Player player, Location location, double offsetX, double offsetY, double offsetZ, int count, T customData) {
			if (Configuration.Settings.getVisualEffect() && this.particle != null) {
				player.spawnParticle(this.particle, location, count, offsetX, offsetY, offsetZ, customData);
			}
		}

		public void spawnParticle(Player player, Location location, double offsetX, double offsetY, double offsetZ, int count, double extra) {
			this.spawnParticle(player, location, offsetX, offsetY, offsetZ, count, extra, null);
		}

		public void spawnParticle(Player player, Location location, double offsetX, double offsetY, double offsetZ, int count) {
			this.spawnParticle(player, location, offsetX, offsetY, offsetZ, count, null);
		}

		public void spawnParticle(Player player, Location location) {
			this.spawnParticle(player, location, 0, 0, 0, 1);
		}

		public <T> void spawnParticle(Location location, double offsetX, double offsetY, double offsetZ, int count, double extra, T customData) {
			if (Configuration.Settings.getVisualEffect() && this.particle != null) {
				location.getWorld().spawnParticle(this.particle, location, count, offsetX, offsetY, offsetZ, extra, customData);
			}
		}

		public <T> void spawnParticle(Location location, double offsetX, double offsetY, double offsetZ, int count, T customData) {
			if (Configuration.Settings.getVisualEffect() && this.particle != null) {
				location.getWorld().spawnParticle(this.particle, location, count, offsetX, offsetY, offsetZ, customData);
			}
		}

		public void spawnParticle(Location location, double offsetX, double offsetY, double offsetZ, int count, double extra) {
			this.spawnParticle(location, offsetX, offsetY, offsetZ, count, extra, null);
		}

		public void spawnParticle(Location location, double offsetX, double offsetY, double offsetZ, int count) {
			this.spawnParticle(location, offsetX, offsetY, offsetZ, count, null);
		}

		public void spawnParticle(Location location) {
			this.spawnParticle(location, 0, 0, 0, 1);
		}
	}

	public static class ColouredParticle extends ParticleLib.SimpleParticle {

		private ColouredParticle(String name) {
			super(name);
		}

		public void spawnParticle(Player player, Location location, ParticleLib.RGB rgb) {
			if (ServerVersion.getVersion() >= 13) {
				if (this.particle.getDataType().equals(Particle.DustOptions.class)) {
					this.spawnParticle(player, location, 0, 0, 0, 0, new Particle.DustOptions(rgb.getColor(), 1));
				} else {
					this.spawnParticle(player, location, rgb.getRed(), rgb.getGreen(), rgb.getBlue(), 0);
				}
			} else {
				this.spawnParticle(player, location, rgb.getRed(), rgb.getGreen(), rgb.getBlue(), 0);
			}
		}

		public void spawnParticle(Location location, ParticleLib.RGB rgb) {
			if (ServerVersion.getVersion() >= 13) {
				if (this.particle.getDataType().equals(Particle.DustOptions.class)) {
					this.spawnParticle(location, 0, 0, 0, 0, new Particle.DustOptions(rgb.getColor(), 1));
				} else {
					this.spawnParticle(location, rgb.getRed(), rgb.getGreen(), rgb.getBlue(), 0);
				}
			} else {
				this.spawnParticle(location, rgb.getRed(), rgb.getGreen(), rgb.getBlue(), 0);
			}
		}
	}

	public static class RGB {

		public static RGB fromRGB(int rgb) {
			return new RGB(rgb >> 16 & 255, rgb >> 8 & 255, rgb & 255);
		}

		public static RGB of(int red, int green, int blue) {
			return new RGB(red, green, blue);
		}

		private int red;
		private int green;
		private int blue;

		public RGB(int red, int green, int blue) {
			this.red = red;
			this.green = green;
			this.blue = blue;
		}

		public float getRed() {
			return red / 255.0F;
		}

		public float getGreen() {
			return green / 255.0F;
		}

		public float getBlue() {
			return blue / 255.0F;
		}

		public Color getColor() {
			return Color.fromRGB(red, green, blue);
		}

		public void setRed(int red) {
			this.red = red;
		}

		public void setGreen(int green) {
			this.green = green;
		}

		public void setBlue(int blue) {
			this.blue = blue;
		}

	}

	public static class BlockParticle extends ParticleLib.SimpleParticle {

		private BlockParticle(String name) {
			super(name);
		}

		public void spawnParticle(Location location, double offsetX, double offsetY, double offsetZ, int count, double extra, Block block) {
			if (ServerVersion.getVersion() >= 13) {
				this.spawnParticle(location, offsetX, offsetY, offsetZ, count, extra, block.getBlockData());
			} else {
				this.spawnParticle(location, offsetX, offsetY, offsetZ, count, extra, new MaterialData(block.getType(), block.getData()));
			}
		}

		public void spawnParticle(Location location, double offsetX, double offsetY, double offsetZ, int count, Block block) {
			if (ServerVersion.getVersion() >= 13) {
				this.spawnParticle(location, offsetX, offsetY, offsetZ, count, block.getBlockData());
			} else {
				this.spawnParticle(location, offsetX, offsetY, offsetZ, count, new MaterialData(block.getType(), block.getData()));
			}
		}

		public void spawnParticle(Location location, double offsetX, double offsetY, double offsetZ, int count, double extra, MaterialX material) {
			if (ServerVersion.getVersion() >= 13) {
				this.spawnParticle(location, offsetX, offsetY, offsetZ, count, extra, material.getMaterial().createBlockData());
			} else {
				this.spawnParticle(location, offsetX, offsetY, offsetZ, count, extra, new MaterialData(material.getMaterial(), material.getData()));
			}
		}

		public void spawnParticle(Location location, double offsetX, double offsetY, double offsetZ, int count, MaterialX material) {
			if (ServerVersion.getVersion() >= 13) {
				this.spawnParticle(location, offsetX, offsetY, offsetZ, count, material.getMaterial().createBlockData());
			} else {
				this.spawnParticle(location, offsetX, offsetY, offsetZ, count, new MaterialData(material.getMaterial(), material.getData()));
			}
		}

		public void spawnParticle(Player player, Location location, double offsetX, double offsetY, double offsetZ, int count, double extra, Block block) {
			if (ServerVersion.getVersion() >= 13) {
				this.spawnParticle(player, location, offsetX, offsetY, offsetZ, count, extra, block.getBlockData());
			} else {
				this.spawnParticle(player, location, offsetX, offsetY, offsetZ, count, extra, new MaterialData(block.getType(), block.getData()));
			}
		}

		public void spawnParticle(Player player, Location location, double offsetX, double offsetY, double offsetZ, int count, Block block) {
			if (ServerVersion.getVersion() >= 13) {
				this.spawnParticle(player, location, offsetX, offsetY, offsetZ, count, block.getBlockData());
			} else {
				this.spawnParticle(player, location, offsetX, offsetY, offsetZ, count, new MaterialData(block.getType(), block.getData()));
			}
		}

		public void spawnParticle(Player player, Location location, double offsetX, double offsetY, double offsetZ, int count, double extra, MaterialX material) {
			if (ServerVersion.getVersion() >= 13) {
				this.spawnParticle(player, location, offsetX, offsetY, offsetZ, count, extra, material.getMaterial().createBlockData());
			} else {
				this.spawnParticle(player, location, offsetX, offsetY, offsetZ, count, extra, new MaterialData(material.getMaterial(), material.getData()));
			}
		}

		public void spawnParticle(Player player, Location location, double offsetX, double offsetY, double offsetZ, int count, MaterialX material) {
			if (ServerVersion.getVersion() >= 13) {
				this.spawnParticle(player, location, offsetX, offsetY, offsetZ, count, material.getMaterial().createBlockData());
			} else {
				this.spawnParticle(player, location, offsetX, offsetY, offsetZ, count, new MaterialData(material.getMaterial(), material.getData()));
			}
		}

	}

}
