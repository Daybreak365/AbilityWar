package daybreak.abilitywar.utils.base.minecraft.nms.v1_12_R1.packet;

import daybreak.abilitywar.utils.base.collect.Pair;
import io.netty.channel.ChannelHandler;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class PacketHandler {

	private final Map<UUID, Pair<CraftPlayer, ChannelHandler>> handlers = new HashMap<>();
	private final Function<CraftPlayer, ChannelHandler> creator;

	public PacketHandler(final Function<CraftPlayer, ChannelHandler> creator) {
		this.creator = creator;
	}

	public void attach(CraftPlayer player) {
		final Pair<CraftPlayer, ChannelHandler> removed = handlers.remove(player.getUniqueId());
		if (removed != null) {
			final CraftPlayer left = removed.getLeft();
			try {
				left.getHandle().playerConnection.networkManager.channel.pipeline().remove(removed.getRight());
			} catch (NoSuchElementException ignored) {}
		}
		final ChannelHandler handler = creator.apply(player);
		try {
			player.getHandle().playerConnection.networkManager.channel.pipeline().addBefore("packet_handler", hashCode() + ":" + player.getName(), handler);
			handlers.put(player.getUniqueId(), Pair.of(player, handler));
		} catch (NoSuchElementException ignored) {}
	}

	public void detach(UUID uniqueId) {
		final Pair<CraftPlayer, ChannelHandler> removed = handlers.remove(uniqueId);
		if (removed != null) {
			try {
				removed.getLeft().getHandle().playerConnection.networkManager.channel.pipeline().remove(removed.getRight());
			} catch (NoSuchElementException ignored) {}
		}
	}

	public void detachAll() {
		for (final Iterator<Entry<UUID, Pair<CraftPlayer, ChannelHandler>>> iterator = handlers.entrySet().iterator(); iterator.hasNext();) {
			detach(iterator);
		}
	}

	public void detachAll(final Consumer<CraftPlayer> consumer) {
		for (final Iterator<Entry<UUID, Pair<CraftPlayer, ChannelHandler>>> iterator = handlers.entrySet().iterator(); iterator.hasNext();) {
			consumer.accept(detach(iterator));
		}
	}

	private CraftPlayer detach(final Iterator<Entry<UUID, Pair<CraftPlayer, ChannelHandler>>> iterator) {
		final Pair<CraftPlayer, ChannelHandler> pair = iterator.next().getValue();
		final CraftPlayer player = pair.getLeft();
		try {
			player.getHandle().playerConnection.networkManager.channel.pipeline().remove(pair.getRight());
		} catch (NoSuchElementException ignored) {}
		iterator.remove();
		return player;
	}

}
