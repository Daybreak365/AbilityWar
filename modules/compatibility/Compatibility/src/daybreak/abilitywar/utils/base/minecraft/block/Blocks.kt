package daybreak.abilitywar.utils.base.minecraft.block

import daybreak.abilitywar.utils.base.minecraft.version.NMSVersion
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion
import daybreak.abilitywar.utils.base.minecraft.version.UnsupportedVersionException
import org.bukkit.block.Block

class Blocks private constructor() {
	companion object INSTACE : IBlocks {
		private val INSTACE: IBlocks = try {
			Class.forName("daybreak.abilitywar.utils.base.minecraft.block." + if (ServerVersion.isAboveOrEqual(NMSVersion.v1_13_R1)) "flat" else "preflat" + ".BlocksImpl").asSubclass(IBlocks::class.java).getConstructor().newInstance()
		} catch (e: Exception) {
			throw UnsupportedVersionException()
		}

		@JvmStatic
		override fun createSnapshot(block: Block): IBlockSnapshot {
			return INSTACE.createSnapshot(block)
		}
	}
}