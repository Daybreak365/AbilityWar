package daybreak.abilitywar.utils.library;
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Crypto Morin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import com.google.common.collect.ImmutableMap;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.Rotatable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.Cake;
import org.bukkit.material.Colorable;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Openable;
import org.bukkit.material.Wood;
import org.bukkit.material.Wool;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("deprecation")
public final class BlockX {

	public static final int CAKE_SLICES = 6;

	public static boolean isLit(Block block) {
		if (MaterialX.isNewVersion()) {
			if (!(block.getBlockData() instanceof Lightable)) return false;
			Lightable lightable = (Lightable) block.getBlockData();
			return lightable.isLit();
		}

		return isMaterial(block, "REDSTONE_LAMP_ON", "REDSTONE_TORCH_ON", "BURNING_FURNACE");
	}

	/**
	 * Checks if the block is a container.
	 * Containers are chests, hoppers, enderchests and everything that
	 * has an inventory.
	 *
	 * @param block the block to check.
	 * @return true if the block is a container, otherwise false.
	 */
	public static boolean isContainer(Block block) {
		return block.getState() instanceof InventoryHolder;
	}

	/**
	 * Can be furnaces or redstone lamps.
	 */
	public static void setLit(Block block, boolean lit) {
		if (MaterialX.isNewVersion()) {
			if (!(block.getBlockData() instanceof Lightable)) return;
			Lightable lightable = (Lightable) block.getBlockData();
			lightable.setLit(lit);
			return;
		}

		String name = block.getType().name();
		if (name.endsWith("FURNACE")) block.setType(Material.getMaterial("BURNING_FURNACE"));
		else if (name.startsWith("REDSTONE_LAMP")) block.setType(Material.getMaterial("REDSTONE_LAMP_ON"));
		else block.setType(Material.getMaterial("REDSTONE_TORCH_ON"));
	}

	/**
	 * Wool and Dye. But Dye is not a block itself.
	 */
	public static DyeColor getColor(Block block) {
		if (MaterialX.isNewVersion()) {
			if (!(block.getBlockData() instanceof Colorable)) return null;
			Colorable colorable = (Colorable) block.getBlockData();
			return colorable.getColor();
		}

		BlockState state = block.getState();
		MaterialData data = state.getData();
		if (data instanceof Wool) {
			Wool wool = (Wool) data;
			return wool.getColor();
		}
		return null;
	}

	public static boolean isCake(Material material) {
		return MaterialX.isNewVersion() ? material == Material.CAKE : material.name().equals("CAKE_BLOCK");
	}

	public static boolean isWheat(Material material) {
		return MaterialX.isNewVersion() ? material == Material.WHEAT : material.name().equals("CROPS");
	}

	public static boolean isSugarCane(Material material) {
		return MaterialX.isNewVersion() ? material == Material.SUGAR_CANE : material.name().equals("SUGAR_CANE_BLOCK");
	}

	public static boolean isBeetroot(Material material) {
		return MaterialX.isNewVersion() ? material == Material.SUGAR_CANE : material.name().equals("BEETROOT_BLOCK");
	}

	public static boolean isNetherWart(Material material) {
		return MaterialX.isNewVersion() ? material == Material.NETHER_WART : material.name().equals("NETHER_WARTS");
	}

	public static boolean isCarrot(Material material) {
		return MaterialX.isNewVersion() ? material.name().equals("CARROTS") : material == Material.CARROT;
	}

	public static boolean isPotato(Material material) {
		return MaterialX.isNewVersion() ? material.name().equals("POTATOES") : material == Material.POTATO;
	}

	public static BlockFace getDirection(Block block) {
		if (MaterialX.isNewVersion()) {
			if (!(block.getBlockData() instanceof Directional)) return BlockFace.SELF;
			Directional direction = (Directional) block.getBlockData();
			return direction.getFacing();
		}

		BlockState state = block.getState();
		MaterialData data = state.getData();
		if (data instanceof org.bukkit.material.Directional) {
			return ((org.bukkit.material.Directional) data).getFacing();
		}
		return null;
	}

	public static boolean setDirection(Block block, BlockFace facing) {
		if (MaterialX.isNewVersion()) {
			if (!(block.getBlockData() instanceof Directional)) return false;
			Directional direction = (Directional) block.getBlockData();
			direction.setFacing(facing);
			return true;
		}

		BlockState state = block.getState();
		MaterialData data = state.getData();
		if (data instanceof org.bukkit.material.Directional) {
			((org.bukkit.material.Directional) data).setFacingDirection(facing);
			state.update(true);
			return true;
		}
		return false;
	}

	public static int getAge(Block block) {
		if (MaterialX.isNewVersion()) {
			if (!(block.getBlockData() instanceof Ageable)) return 0;
			Ageable ageable = (Ageable) block.getBlockData();
			return ageable.getAge();
		}

		BlockState state = block.getState();
		MaterialData data = state.getData();
		return data.getData();
	}

	public static void setAge(Block block, int age) {
		if (MaterialX.isNewVersion()) {
			if (!(block.getBlockData() instanceof Ageable)) return;
			Ageable ageable = (Ageable) block.getBlockData();
			ageable.setAge(age);
		}

		BlockState state = block.getState();
		MaterialData data = state.getData();
		data.setData((byte) age);
		state.update(true);
	}

	/**
	 * Sets the type of any block that can be colored.
	 *
	 * @param block the block to color.
	 * @param color the color to use.
	 * @return true if the block can be colored, otherwise false.
	 */
	public static boolean setColor(Block block, DyeColor color) {
		if (MaterialX.isNewVersion()) {
			String type = block.getType().name();
			if (type.endsWith("WOOL")) block.setType(Material.getMaterial(color.name() + "_WOOL"));
			else if (type.endsWith("BED")) block.setType(Material.getMaterial(color.name() + "_BED"));
			else if (type.endsWith("STAINED_GLASS"))
				block.setType(Material.getMaterial(color.name() + "_STAINED_GLASS"));
			else if (type.endsWith("STAINED_GLASS_PANE"))
				block.setType(Material.getMaterial(color.name() + "_STAINED_GLASS_PANE"));
			else if (type.endsWith("TERRACOTTA")) block.setType(Material.getMaterial(color.name() + "_TERRACOTTA"));
			else if (type.endsWith("GLAZED_TERRACOTTA"))
				block.setType(Material.getMaterial(color.name() + "_GLAZED_TERRACOTTA"));
			else if (type.endsWith("BANNER")) block.setType(Material.getMaterial(color.name() + "_BANNER"));
			else if (type.endsWith("WALL_BANNER")) block.setType(Material.getMaterial(color.name() + "_WALL_BANNER"));
			else if (type.endsWith("CARPET")) block.setType(Material.getMaterial(color.name() + "_CARPET"));
			else if (type.endsWith("SHULKER_BOX")) block.setType(Material.getMaterial(color.name() + "_SHULKERBOX"));
			else if (type.endsWith("CONCRETE")) block.setType(Material.getMaterial(color.name() + "_CONCRETE"));
			else if (type.endsWith("CONCRETE_POWDER"))
				block.setType(Material.getMaterial(color.name() + "_CONCRETE_POWDER"));
			else return false;
			return true;
		}

		BlockState state = block.getState();
		state.setRawData(color.getWoolData());
		state.update(true);
		return false;
	}

	/**
	 * Can be used on cauldron.
	 */
	public static boolean setFluidLevel(Block block, int level) {
		if (MaterialX.isNewVersion()) {
			if (!(block.getBlockData() instanceof Levelled)) return false;
			Levelled levelled = (Levelled) block.getBlockData();
			levelled.setLevel(level);
			return true;
		}

		BlockState state = block.getState();
		MaterialData data = state.getData();
		data.setData((byte) level);
		state.update(true);
		return false;
	}

	public static int getFluidLevel(Block block) {
		if (MaterialX.isNewVersion()) {
			if (!(block.getBlockData() instanceof Levelled)) return -1;
			Levelled levelled = (Levelled) block.getBlockData();
			return levelled.getLevel();
		}

		BlockState state = block.getState();
		MaterialData data = state.getData();
		return data.getData();
	}

	public static boolean isWaterStationary(Block block) {
		return MaterialX.isNewVersion() ? getFluidLevel(block) < 7 : block.getType().name().equals("STATIONARY_WATER");
	}

	public static boolean isWater(Material material) {
		String name = material.name();
		return name.equals("WATER") || name.equals("STATIONARY_WATER");
	}

	public static void setCakeSlices(Block block, int amount) {
		if (!isCake(block.getType())) throw new IllegalArgumentException("Block is not a cake: " + block.getType());
		if (MaterialX.isNewVersion()) {
			BlockData bd = block.getBlockData();
			if (bd instanceof org.bukkit.block.data.type.Cake) {
				org.bukkit.block.data.type.Cake cake = (org.bukkit.block.data.type.Cake) bd;

				if (amount <= cake.getMaximumBites()) {
					cake.setBites(cake.getBites() + 1);
				} else {
					block.breakNaturally();
					return;
				}

				block.setBlockData(bd);
			}
			return;
		}

		BlockState state = block.getState();
		if (state instanceof Cake) {
			Cake cake = (Cake) state.getData();

			if (amount <= 1) {
				cake.setSlicesRemaining(amount);
			} else {
				block.breakNaturally();
				return;
			}

			state.update();
		}
	}

	public static int addCakeSlices(Block block, int slices) {
		if (!isCake(block.getType())) throw new IllegalArgumentException("Block is not a cake: " + block.getType());
		if (MaterialX.isNewVersion()) {
			BlockData bd = block.getBlockData();
			org.bukkit.block.data.type.Cake cake = (org.bukkit.block.data.type.Cake) bd;

			if (cake.getBites() + slices <= cake.getMaximumBites()) {
				cake.setBites(cake.getBites() + slices);
			} else {
				block.breakNaturally();
				return cake.getMaximumBites() - cake.getBites();
			}

			block.setBlockData(bd);
			return cake.getMaximumBites() - cake.getBites();
		}

		BlockState state = block.getState();
		Cake cake = (Cake) state.getData();

		if (cake.getSlicesEaten() + slices < CAKE_SLICES) {
			cake.setSlicesEaten(cake.getSlicesEaten() + slices);
		} else {
			block.breakNaturally();
			return cake.getSlicesRemaining();
		}
		state.update();
		return cake.getSlicesRemaining();
	}

	public static boolean setWooden(Block block, MaterialX species) {
		block.setType(species.parseMaterial());
		if (MaterialX.isNewVersion()) return true;

		TreeSpecies type = species == MaterialX.SPRUCE_LOG ? TreeSpecies.REDWOOD :
				TreeSpecies.valueOf(species.name().substring(0, species.name().indexOf('_')));

		BlockState state = block.getState();
		MaterialData data = state.getData();
		((Wood) data).setSpecies(type);
		state.update(true);
		return true;
	}

	/**
	 * <b>Universal Method</b>
	 * <p>
	 * Check if the block type matches the specified MaterialX
	 *
	 * @param block    the block to check.
	 * @param material the MaterialX similar to this block type.
	 * @return true if the block type is similar to the material.
	 */
	public static boolean isType(Block block, MaterialX material) {
		Material blockType = block.getType();

		switch (material) {
			case CAKE:
				return isCake(blockType);
			case NETHER_WART:
				return isNetherWart(blockType);
			case CARROT:
			case CARROTS:
				return isCarrot(blockType);
			case POTATO:
			case POTATOES:
				return isPotato(blockType);
			case WHEAT:
			case WHEAT_SEEDS:
				return isWheat(blockType);
			case BEETROOT:
			case BEETROOT_SEEDS:
			case BEETROOTS:
				return isBeetroot(blockType);
			case SUGAR_CANE:
				return isSugarCane(blockType);
			case WATER:
				return isWater(blockType);
			case AIR:
				return isAir(blockType);
			default:
				return MaterialX.isNewVersion() ? material.parseMaterial() == blockType : material.parseMaterial() == blockType && block.getData() == material.getData();
		}
	}

	public static boolean isAir(Material material) {
		// Only air material names end with "IR"
		return material.name().endsWith("IR");
	}

	public static boolean isPowered(Block block) {
		if (MaterialX.isNewVersion()) {
			if (!(block.getBlockData() instanceof Powerable)) return false;
			Powerable powerable = (Powerable) block.getBlockData();
			return powerable.isPowered();
		}

		String name = block.getType().name();
		if (name.startsWith("REDSTONE_COMPARATOR"))
			return isMaterial(block, "REDSTONE_COMPARATOR_ON");

		return false;
	}

	public static void setPowered(Block block, boolean powered) {
		if (MaterialX.isNewVersion()) {
			if (!(block.getBlockData() instanceof Powerable)) return;
			Powerable powerable = (Powerable) block.getBlockData();
			powerable.setPowered(powered);
			return;
		}

		String name = block.getType().name();
		if (name.startsWith("REDSTONE_COMPARATOR")) block.setType(Material.getMaterial("REDSTONE_COMPARATOR_ON"));
	}

	public static boolean isOpen(Block block) {
		if (MaterialX.isNewVersion()) {
			if (!(block.getBlockData() instanceof org.bukkit.block.data.Openable)) return false;
			org.bukkit.block.data.Openable openable = (org.bukkit.block.data.Openable) block.getBlockData();
			return openable.isOpen();
		}

		BlockState state = block.getState();
		if (!(state instanceof Openable)) return false;
		Openable openable = (Openable) state.getData();
		return openable.isOpen();
	}

	public static void setOpened(Block block, boolean opened) {
		if (MaterialX.isNewVersion()) {
			if (!(block.getBlockData() instanceof org.bukkit.block.data.Openable)) return;
			org.bukkit.block.data.Openable openable = (org.bukkit.block.data.Openable) block.getBlockData();
			openable.setOpen(opened);
			return;
		}

		BlockState state = block.getState();
		if (!(state instanceof Openable)) return;
		Openable openable = (Openable) state.getData();
		openable.setOpen(opened);
		state.setData((MaterialData) openable);
		state.update();
	}

	public static BlockFace getRotation(Block block) {
		if (MaterialX.isNewVersion()) {
			if (!(block.getBlockData() instanceof Rotatable)) return null;
			Rotatable rotatable = (Rotatable) block.getBlockData();
			return rotatable.getRotation();
		}

		return null;
	}

	public static void setRotation(Block block, BlockFace facing) {
		if (MaterialX.isNewVersion()) {
			if (!(block.getBlockData() instanceof Rotatable)) return;
			Rotatable rotatable = (Rotatable) block.getBlockData();
			rotatable.setRotation(facing);
		}
	}

	private static boolean isMaterial(Block block, String... materials) {
		String type = block.getType().name();
		for (String material : materials)
			if (type.equals(material)) return true;
		return false;
	}

	private static final ImmutableMap<String, String> BLOCK_MATERIALS = ImmutableMap.<String, String>builder()
			.put("BED", "BED_BLOCK").put("BANNER", "STANDING_BANNER")
			.put("CAULDRON_ITEM", "CAULDRON")
			.put("REDSTONE_COMPARATOR", "REDSTONE_COMPARATOR_OFF")
			.put("SKULL_ITEM", "SKULL").put("DIODE", "DIODE_BLOCK_OFF").build();

	private static Material checkMaterial(Material material) {
		if (material != null && !MaterialX.isNewVersion() && BLOCK_MATERIALS.containsKey(material.name())) {
			return Material.getMaterial(BLOCK_MATERIALS.get(material.name()));
		}
		return material;
	}

	private static Method SET_DATA = null;

	static {
		if (!MaterialX.isNewVersion()) {
			try {
				SET_DATA = Block.class.getDeclaredMethod("setData", byte.class);
			} catch (NoSuchMethodException ignored) {
			}
		}
	}

	public static void setType(Block block, MaterialX materialX) {
		Material material = checkMaterial(materialX.parseMaterial());
		if (material != null) {
			block.setType(material);
			if (!MaterialX.isNewVersion() && materialX.hasData()) {
				try {
					SET_DATA.invoke(block, materialX.getData());
				} catch (IllegalAccessException | InvocationTargetException ignored) {
				}
			}
		}
	}

	public static void sendBlockChange(Player player, Location location, MaterialX materialX) {
		Material material = checkMaterial(materialX.parseMaterial());
		if (material != null) {
			if (ServerVersion.getVersionNumber() >= 13) {
				player.sendBlockChange(location, material.createBlockData());
			} else {
				player.sendBlockChange(location, material, materialX.getData());
			}
		}
	}

}