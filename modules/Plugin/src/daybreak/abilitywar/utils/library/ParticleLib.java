package daybreak.abilitywar.utils.library;

import com.google.common.base.Enums;
import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

/**
 * 파티클 라이브러리
 * @author Daybreak 새벽
 * @version 3.0
 * @since 2019-02-19
 */
public class ParticleLib {

	public static final SimpleParticle ASH = new SimpleParticle("ASH");
	public static final SimpleParticle BARRIER = new SimpleParticle("BARRIER");
	public static final BlockParticle BLOCK_CRACK = new BlockParticle("BLOCK_CRACK");
	public static final BlockParticle BLOCK_DUST = new BlockParticle("BLOCK_DUST");
	public static final SimpleParticle BUBBLE_COLUMN_UP = new SimpleParticle("BUBBLE_COLUMN_UP");
	public static final SimpleParticle BUBBLE_POP = new SimpleParticle("BUBBLE_POP");
	public static final SimpleParticle CAMPFIRE_COSY_SMOKE = new SimpleParticle("CAMPFIRE_COSY_SMOKE");
	public static final SimpleParticle CAMPFIRE_SIGNAL_SMOKE = new SimpleParticle("CAMPFIRE_SIGNAL_SMOKE");
	public static final SimpleParticle CLOUD = new SimpleParticle("CLOUD");
	public static final SimpleParticle COMPOSTER = new SimpleParticle("COMPOSTER");
	public static final SimpleParticle CRIMSON_SPORE = new SimpleParticle("CRIMSON_SPORE");
	public static final SimpleParticle CRIT = new SimpleParticle("CRIT");
	public static final SimpleParticle CRIT_MAGIC = new SimpleParticle("CRIT_MAGIC");
	public static final SimpleParticle CURRENT_DOWN = new SimpleParticle("CURRENT_DOWN");
	public static final SimpleParticle DAMAGE_INDICATOR = new SimpleParticle("DAMAGE_INDICATOR");
	public static final SimpleParticle DOLPHIN = new SimpleParticle("DOLPHIN");
	public static final SimpleParticle DRAGON_BREATH = new SimpleParticle("DRAGON_BREATH");
	public static final SimpleParticle DRIPPING_HONEY = new SimpleParticle("DRIPPING_HONEY");
	public static final SimpleParticle DRIPPING_OBSIDIAN_TEAR = new SimpleParticle("DRIPPING_OBSIDIAN_TEAR");
	public static final SimpleParticle DRIP_LAVA = new SimpleParticle("DRIP_LAVA");
	public static final SimpleParticle DRIP_WATER = new SimpleParticle("DRIP_WATER");
	public static final SimpleParticle ENCHANTMENT_TABLE = new SimpleParticle("ENCHANTMENT_TABLE");
	public static final SimpleParticle END_ROD = new SimpleParticle("END_ROD");
	public static final SimpleParticle EXPLOSION_HUGE = new SimpleParticle("EXPLOSION_HUGE");
	public static final SimpleParticle EXPLOSION_LARGE = new SimpleParticle("EXPLOSION_LARGE");
	public static final SimpleParticle EXPLOSION_NORMAL = new SimpleParticle("EXPLOSION_NORMAL");
	public static final SimpleParticle FALLING_DUST = new SimpleParticle("FALLING_DUST");
	public static final SimpleParticle FALLING_HONEY = new SimpleParticle("FALLING_HONEY");
	public static final SimpleParticle FALLING_LAVA = new SimpleParticle("FALLING_LAVA");
	public static final SimpleParticle FALLING_NECTAR = new SimpleParticle("FALLING_NECTAR");
	public static final SimpleParticle FALLING_OBSIDIAN_TEAR = new SimpleParticle("FALLING_OBSIDIAN_TEAR");
	public static final SimpleParticle FALLING_WATER = new SimpleParticle("FALLING_WATER");
	public static final SimpleParticle FIREWORKS_SPARK = new SimpleParticle("FIREWORKS_SPARK");
	public static final SimpleParticle FLAME = new SimpleParticle("FLAME");
	public static final SimpleParticle FLASH = new SimpleParticle("FLASH");
	public static final SimpleParticle HEART = new SimpleParticle("HEART");
	public static final ItemParticle ITEM_CRACK = new ItemParticle("ITEM_CRACK");
	public static final SimpleParticle LANDING_HONEY = new SimpleParticle("LANDING_HONEY");
	public static final SimpleParticle LANDING_LAVA = new SimpleParticle("LANDING_LAVA");
	public static final SimpleParticle LANDING_OBSIDIAN_TEAR = new SimpleParticle("LANDING_OBSIDIAN_TEAR");
	public static final SimpleParticle LAVA = new SimpleParticle("LAVA");
	public static final SimpleParticle MOB_APPEARANCE = new SimpleParticle("MOB_APPEARANCE");
	public static final SimpleParticle NAUTILUS = new SimpleParticle("NAUTILUS");
	public static final SimpleParticle NOTE = new SimpleParticle("NOTE");
	public static final SimpleParticle PORTAL = new SimpleParticle("PORTAL");
	public static final ColouredParticle REDSTONE = new ColouredParticle("REDSTONE");
	public static final SimpleParticle REVERSE_PORTAL = new SimpleParticle("REVERSE_PORTAL");
	public static final SimpleParticle SLIME = new SimpleParticle("SLIME");
	public static final SimpleParticle SMOKE_LARGE = new SimpleParticle("SMOKE_LARGE");
	public static final SimpleParticle SMOKE_NORMAL = new SimpleParticle("SMOKE_NORMAL");
	public static final SimpleParticle SNEEZE = new SimpleParticle("SNEEZE");
	public static final SimpleParticle SNOWBALL = new SimpleParticle("SNOWBALL");
	public static final SimpleParticle SNOW_SHOVEL = new SimpleParticle("SNOW_SHOVEL");
	public static final SimpleParticle SOUL = new SimpleParticle("SOUL");
	public static final SimpleParticle SOUL_FIRE_FLAME = new SimpleParticle("SOUL_FIRE_FLAME");
	public static final SimpleParticle SPELL = new SimpleParticle("SPELL");
	public static final SimpleParticle SPELL_INSTANT = new SimpleParticle("SPELL_INSTANT");
	public static final ColouredParticle SPELL_MOB = new ColouredParticle("SPELL_MOB");
	public static final ColouredParticle SPELL_MOB_AMBIENT = new ColouredParticle("SPELL_MOB_AMBIENT");
	public static final SimpleParticle SPELL_WITCH = new SimpleParticle("SPELL_WITCH");
	public static final SimpleParticle SPIT = new SimpleParticle("SPIT");
	public static final SimpleParticle SQUID_INK = new SimpleParticle("SQUID_INK");
	public static final SimpleParticle SUSPENDED = new SimpleParticle("SUSPENDED");
	public static final SimpleParticle SUSPENDED_DEPTH = new SimpleParticle("SUSPENDED_DEPTH");
	public static final SimpleParticle SWEEP_ATTACK = new SimpleParticle("SWEEP_ATTACK");
	public static final SimpleParticle TOTEM = new SimpleParticle("TOTEM");
	public static final SimpleParticle TOWN_AURA = new SimpleParticle("TOWN_AURA");
	public static final SimpleParticle VILLAGER_ANGRY = new SimpleParticle("VILLAGER_ANGRY");
	public static final SimpleParticle VILLAGER_HAPPY = new SimpleParticle("VILLAGER_HAPPY");
	public static final SimpleParticle WARPED_SPORE = new SimpleParticle("WARPED_SPORE");
	public static final SimpleParticle WATER_BUBBLE = new SimpleParticle("WATER_BUBBLE");
	public static final SimpleParticle WATER_DROP = new SimpleParticle("WATER_DROP");
	public static final SimpleParticle WATER_SPLASH = new SimpleParticle("WATER_SPLASH");
	public static final SimpleParticle WATER_WAKE = new SimpleParticle("WATER_WAKE");
	public static final SimpleParticle WHITE_ASH = new SimpleParticle("WHITE_ASH");

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

	public static class ColouredParticle extends SimpleParticle {

		private ColouredParticle(String name) {
			super(name);
		}

		public void spawnParticle(Player player, Location location, RGB rgb) {
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

		public void spawnParticle(Location location, RGB rgb) {
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

	public static class ItemParticle extends SimpleParticle {

		private ItemParticle(String name) {
			super(name);
		}

		public void spawnParticle(Location location, double offsetX, double offsetY, double offsetZ, int count, double extra, MaterialX material) {
			this.spawnParticle(location, offsetX, offsetY, offsetZ, count, extra, material.createItem());
		}

		public void spawnParticle(Location location, double offsetX, double offsetY, double offsetZ, int count, MaterialX material) {
			this.spawnParticle(location, offsetX, offsetY, offsetZ, count, material.createItem());
		}

		public void spawnParticle(Player player, Location location, double offsetX, double offsetY, double offsetZ, int count, double extra, MaterialX material) {
			this.spawnParticle(player, location, offsetX, offsetY, offsetZ, count, extra, material.createItem());
		}

		public void spawnParticle(Player player, Location location, double offsetX, double offsetY, double offsetZ, int count, MaterialX material) {
			this.spawnParticle(player, location, offsetX, offsetY, offsetZ, count, material.createItem());
		}

	}

	public static class BlockParticle extends SimpleParticle {

		private BlockParticle(String name) {
			super(name);
		}

		public void spawnParticle(Location location, double offsetX, double offsetY, double offsetZ, int count, double extra, Block block) {
			if (ServerVersion.getVersion() >= 13) {
				this.spawnParticle(location, offsetX, offsetY, offsetZ, count, extra, block.getBlockData());
			} else {
				this.spawnParticle(location, offsetX, offsetY, offsetZ, count, extra, block.getState().getData());
			}
		}

		public void spawnParticle(Location location, double offsetX, double offsetY, double offsetZ, int count, Block block) {
			if (ServerVersion.getVersion() >= 13) {
				this.spawnParticle(location, offsetX, offsetY, offsetZ, count, block.getBlockData());
			} else {
				this.spawnParticle(location, offsetX, offsetY, offsetZ, count, block.getState().getData());
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
				this.spawnParticle(player, location, offsetX, offsetY, offsetZ, count, extra, block.getState().getData());
			}
		}

		public void spawnParticle(Player player, Location location, double offsetX, double offsetY, double offsetZ, int count, Block block) {
			if (ServerVersion.getVersion() >= 13) {
				this.spawnParticle(player, location, offsetX, offsetY, offsetZ, count, block.getBlockData());
			} else {
				this.spawnParticle(player, location, offsetX, offsetY, offsetZ, count, block.getState().getData());
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
