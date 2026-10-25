package daybreak.abilitywar.utils.base.minecraft.server;

import org.bukkit.Bukkit;
import java.util.Locale;

public enum ServerType {

	UNKNOWN,
	CRAFTBUKKIT,
	SPIGOT,
	PAPER;

	private static class Companion {
		public static final ServerType INSTANCE;

		static {
			INSTANCE = resolve(Bukkit.getName(), Bukkit.getVersion());
		}
	}

	public static ServerType resolve(String serverName, String serverVersion) {
		final ServerType named = parse(serverName);
		if (named != UNKNOWN) return named;
		// Older servers identify their implementation in git-Paper-<build> etc.
		final String[] split = serverVersion.split("-");
		return split.length > 1 ? parse(split[1]) : UNKNOWN;
	}

	private static ServerType parse(String name) {
		final String normalized = name.toUpperCase(Locale.ROOT);
		if (normalized.equals("BUKKIT")) return CRAFTBUKKIT;
		try {
			return valueOf(normalized);
		} catch (IllegalArgumentException e) {
			return UNKNOWN;
		}
	}

	public static ServerType getServerType() {
		return Companion.INSTANCE;
	}

	public static boolean is(final ServerType other) {
		return Companion.INSTANCE == other;
	}

}
