package daybreak.abilitywar.utils.base.minecraft.compat;

import org.bukkit.Material;
import org.bukkit.block.Block;

public interface Blocks {

	BlockSnapshot createSnapshot(Block block);

	boolean isIndestructible(Material type);

}
