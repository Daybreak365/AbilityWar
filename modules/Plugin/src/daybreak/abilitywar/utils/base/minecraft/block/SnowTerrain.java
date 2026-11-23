package daybreak.abilitywar.utils.base.minecraft.block;

import daybreak.abilitywar.utils.library.BlockX;
import daybreak.abilitywar.utils.library.MaterialX;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Temporary terrain made by Yeti and Absolute Zero. */
public final class SnowTerrain {
	private final Map<Block, Change> changes = new LinkedHashMap<>();

	private static final class Change {
		final IBlockSnapshot original;
		MaterialX replacement;

		Change(Block block) {
			original = Blocks.createSnapshot(block);
		}

		boolean stillPresent(Block block) {
			if (replacement == MaterialX.GREEN_WOOL) return replacement.compare(block);
			return replacement.getMaterial() == block.getType();
		}
	}

	public void cover(Block surface) {
		Block ground = surface.getRelative(BlockFace.DOWN);
		Change groundChange = original(ground);
		Change surfaceChange = original(surface);

		MaterialX replacement;
		if (ground.getType() == Material.WATER) {
			replacement = MaterialX.PACKED_ICE;
		} else if (ground.getType() == Material.LAVA) {
			replacement = MaterialX.OBSIDIAN;
		} else if (MaterialX.ACACIA_LEAVES.compare(ground) || MaterialX.BIRCH_LEAVES.compare(ground)
				|| MaterialX.DARK_OAK_LEAVES.compare(ground) || MaterialX.JUNGLE_LEAVES.compare(ground)
				|| MaterialX.OAK_LEAVES.compare(ground) || MaterialX.SPRUCE_LEAVES.compare(ground)) {
			replacement = MaterialX.GREEN_WOOL;
		} else {
			replacement = MaterialX.SNOW_BLOCK;
		}
		groundChange.replacement = replacement;
		changes.put(ground, groundChange);
		BlockX.setType(ground, replacement);
		surfaceChange.replacement = MaterialX.SNOW;
		changes.put(surface, surfaceChange);
		surface.setType(Material.SNOW);
	}

	private Change original(Block block) {
		Change change = changes.get(block);
		return change != null ? change : new Change(block);
	}

	public void restore() {
		// Check ownership before restoring supporting blocks can dislodge the snow above them.
		List<IBlockSnapshot> originals = new ArrayList<>();
		changes.forEach((block, change) -> {
			if (change.stillPresent(block)) originals.add(change.original);
		});
		originals.forEach(IBlockSnapshot::apply);
		changes.clear();
	}
}
