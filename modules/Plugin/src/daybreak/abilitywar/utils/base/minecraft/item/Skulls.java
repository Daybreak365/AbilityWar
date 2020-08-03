package daybreak.abilitywar.utils.base.minecraft.item;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.base.reflect.ReflectionUtil.FieldUtil;
import daybreak.abilitywar.utils.library.MaterialX;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.codec.binary.Base64;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Skulls {

	private Skulls() {
	}

	private static final String LINK_HEAD = "http://textures.minecraft.net/texture/";

	public static ItemStack createCustomSkull(String url) {
		final ItemStack stack = MaterialX.PLAYER_HEAD.parseItem();
		if (url == null || url.isEmpty()) return stack;
		if (!url.startsWith(LINK_HEAD)) url = LINK_HEAD + url;
		final SkullMeta meta = (SkullMeta) stack.getItemMeta();
		final GameProfile profile = new GameProfile(UUID.randomUUID(), null);
		profile.getProperties().put("textures", new Property("textures", new String(Base64.encodeBase64(("{textures:{SKIN:{url:\"" + url + "\"}}}").getBytes()))));
		try {
			FieldUtil.setValue(meta.getClass(), meta, "profile", profile);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException ignored) {}
		stack.setItemMeta(meta);
		return stack;
	}

	public static ItemStack createSkull(final String name) {
		final ItemStack stack = MaterialX.PLAYER_HEAD.parseItem();
		stack.setItemMeta(setOwningPlayer((SkullMeta) stack.getItemMeta(), name));
		return stack;
	}

	public static ItemStack createSkull(final Player owner) {
		final ItemStack stack = MaterialX.PLAYER_HEAD.parseItem();
		stack.setItemMeta(setOwningPlayer((SkullMeta) stack.getItemMeta(), owner));
		return stack;
	}

	@SuppressWarnings("deprecation")
	public static SkullMeta setOwningPlayer(final SkullMeta skullMeta, final String name) {
		if (ServerVersion.getVersion() >= 13) {
			skullMeta.setOwningPlayer(new NamedPlayer(name));
		} else {
			skullMeta.setOwner(name);
		}
		return skullMeta;
	}

	@SuppressWarnings("deprecation")
	public static SkullMeta setOwningPlayer(final SkullMeta skullMeta, final Player player) {
		if (ServerVersion.getVersion() >= 13) {
			skullMeta.setOwningPlayer(player);
		} else {
			skullMeta.setOwner(player.getName());
		}
		return skullMeta;
	}

	private static class NamedPlayer implements OfflinePlayer {

		private final UUID randomUUID = UUID.randomUUID();
		private final String name;

		private NamedPlayer(final String name) {
			this.name = name;
		}

		@Override
		public boolean isOnline() {
			return false;
		}

		@Nullable
		@Override
		public String getName() {
			return name;
		}

		@NotNull
		@Override
		public UUID getUniqueId() {
			return randomUUID;
		}

		@Override
		public boolean isBanned() {
			return false;
		}

		@Override
		public boolean isWhitelisted() {
			return false;
		}

		@Override
		public void setWhitelisted(boolean b) {
		}

		@Nullable
		@Override
		public Player getPlayer() {
			return null;
		}

		@Override
		public long getFirstPlayed() {
			return 0;
		}

		@Override
		public long getLastPlayed() {
			return 0;
		}

		@Override
		public boolean hasPlayedBefore() {
			return false;
		}

		@Nullable
		@Override
		public Location getBedSpawnLocation() {
			return null;
		}

		@Override
		public void incrementStatistic(@NotNull Statistic statistic) throws IllegalArgumentException {
		}

		@Override
		public void decrementStatistic(@NotNull Statistic statistic) throws IllegalArgumentException {
		}

		@Override
		public void incrementStatistic(@NotNull Statistic statistic, int i) throws IllegalArgumentException {
		}

		@Override
		public void decrementStatistic(@NotNull Statistic statistic, int i) throws IllegalArgumentException {
		}

		@Override
		public void setStatistic(@NotNull Statistic statistic, int i) throws IllegalArgumentException {
		}

		@Override
		public int getStatistic(@NotNull Statistic statistic) throws IllegalArgumentException {
			return 0;
		}

		@Override
		public void incrementStatistic(@NotNull Statistic statistic, @NotNull Material material) throws IllegalArgumentException {
		}

		@Override
		public void decrementStatistic(@NotNull Statistic statistic, @NotNull Material material) throws IllegalArgumentException {
		}

		@Override
		public int getStatistic(@NotNull Statistic statistic, @NotNull Material material) throws IllegalArgumentException {
			return 0;
		}

		@Override
		public void incrementStatistic(@NotNull Statistic statistic, @NotNull Material material, int i) throws IllegalArgumentException {
		}

		@Override
		public void decrementStatistic(@NotNull Statistic statistic, @NotNull Material material, int i) throws IllegalArgumentException {
		}

		@Override
		public void setStatistic(@NotNull Statistic statistic, @NotNull Material material, int i) throws IllegalArgumentException {
		}

		@Override
		public void incrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType) throws IllegalArgumentException {
		}

		@Override
		public void decrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType) throws IllegalArgumentException {
		}

		@Override
		public int getStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType) throws IllegalArgumentException {
			return 0;
		}

		@Override
		public void incrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType, int i) throws IllegalArgumentException {
		}

		@Override
		public void decrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType, int i) {
		}

		@Override
		public void setStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType, int i) {
		}

		@NotNull
		@Override
		public Map<String, Object> serialize() {
			return Collections.emptyMap();
		}

		@Override
		public boolean isOp() {
			return false;
		}

		@Override
		public void setOp(boolean b) {
		}
	}

}
