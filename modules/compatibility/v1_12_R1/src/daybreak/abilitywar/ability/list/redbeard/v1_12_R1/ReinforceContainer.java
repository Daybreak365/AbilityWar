package daybreak.abilitywar.ability.list.redbeard.v1_12_R1;

import net.minecraft.server.v1_12_R1.ChatComponentText;
import net.minecraft.server.v1_12_R1.ContainerEnchantTable;
import net.minecraft.server.v1_12_R1.Enchantment;
import net.minecraft.server.v1_12_R1.Enchantments;
import net.minecraft.server.v1_12_R1.EntityHuman;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.IInventory;
import net.minecraft.server.v1_12_R1.ItemStack;
import net.minecraft.server.v1_12_R1.PacketPlayOutOpenWindow;
import net.minecraft.server.v1_12_R1.PlayerInventory;
import net.minecraft.server.v1_12_R1.World;

public class ReinforceContainer extends ContainerEnchantTable {

	static void openInventory(final EntityPlayer player) {
		final ReinforceContainer container = new ReinforceContainer(player.inventory, player.world);
		container.windowId = player.nextContainerCounter();
		player.activeContainer = container;
		player.playerConnection.sendPacket(new PacketPlayOutOpenWindow(container.windowId, "minecraft:enchanting_table", new ChatComponentText("아이템 강화"), 0));
		container.addSlotListener(player);
	}

	private ReinforceContainer(PlayerInventory playerinventory, World world) {
		super(playerinventory, world, null);
	}

	@Override
	public void a(IInventory iinventory) {
		if (iinventory == this.enchantSlots) {
			final ItemStack itemstack = iinventory.getItem(0);
			if (!itemstack.isEmpty()) {
				for(int j = 0; j < 3; ++j) {
					this.costs[j] = 10;
					this.h[j] = Enchantment.getId(Enchantments.DAMAGE_ALL);
					this.i[j] = 10;
				}
				this.b();
			} else {
				for(int i = 0; i < 3; ++i) {
					this.costs[i] = 0;
					this.h[i] = -1;
					this.i[i] = -1;
				}
			}
		}

	}

	@Override
	public boolean a(EntityHuman entityhuman, int i) {
		final ItemStack itemstack = this.enchantSlots.getItem(0);
		final ItemStack itemstack1 = this.enchantSlots.getItem(1);
		return true;
	}

	@Override
	public boolean canUse(EntityHuman entityhuman) {
		return true;
	}

}
