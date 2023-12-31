package daybreak.abilitywar.ability.list.grapplinghook.v1_20_R3;

import daybreak.abilitywar.utils.base.minecraft.ability.list.grapplinghook.HookEntity;
import daybreak.abilitywar.utils.base.minecraft.ability.list.grapplinghook.IHooks;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class HooksImpl implements IHooks {
	@Override
	public HookEntity createHook(Player player, Location targetLoc) {
		return new EntityHook(((CraftWorld) player.getWorld()).getHandle(), ((CraftPlayer) player).getHandle(), targetLoc);
	}

	@Override
	public HookEntity createHook(Player player, Player target) {
		return new EntityHook(((CraftWorld) player.getWorld()).getHandle(), ((CraftPlayer) player).getHandle(), target);
	}
}
