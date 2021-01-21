package daybreak.abilitywar.utils.base.minecraft.ability.list.grapplinghook;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface IHooks {

	HookEntity createHook(final Player player, final Location targetLoc);
	HookEntity createHook(final Player player, final Player target);

}
