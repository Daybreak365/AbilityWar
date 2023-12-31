package daybreak.abilitywar.utils.base.minecraft.item;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.library.MaterialX;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class Skulls {

	private Skulls() {
	}

	public static final String LINK_HEAD = "http://textures.minecraft.net/texture/";
	public static final Cache<String, ItemStack> customSkulls = CacheBuilder.newBuilder()
			.expireAfterAccess(Duration.ofMinutes(10))
			.build();

	public static ItemStack createCustomSkull(@NotNull String url) {
		return NMS.createCustomSkull(url);
	}

	public static ItemStack createSkull(final String name) {
		final ItemStack stack = MaterialX.PLAYER_HEAD.createItem();
		stack.setItemMeta(setOwningPlayer((SkullMeta) stack.getItemMeta(), name));
		return stack;
	}

	public static ItemStack createSkull(final Player owner) {
		final ItemStack stack = MaterialX.PLAYER_HEAD.createItem();
		stack.setItemMeta(setOwningPlayer((SkullMeta) stack.getItemMeta(), owner));
		return stack;
	}

	public static SkullMeta setOwningPlayer(final SkullMeta skullMeta, final String name) {
		return NMS.setOwningPlayer(skullMeta, name);
	}

	public static SkullMeta setOwningPlayer(final SkullMeta skullMeta, final Player player) {
		skullMeta.setOwningPlayer(player);
		return skullMeta;
	}

}
