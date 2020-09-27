package daybreak.abilitywar.utils.base.minecraft.server;

import com.google.common.base.Enums;
import org.bukkit.Bukkit;

public enum ServerType {

	UNKNOWN,
	CRAFTBUKKIT,
	SPIGOT,
	PAPER;

	private static class Companion {
		public static final ServerType INSTANCE;

		static {
			final String[] split = Bukkit.getVersion().split("-");
			final String name = split[1].toUpperCase();
			INSTANCE = Enums.getIfPresent(ServerType.class, name).or(name.equals("BUKKIT") ? CRAFTBUKKIT : UNKNOWN);
		}
	}

	public static ServerType getServerType() {
		return Companion.INSTANCE;
	}

	public static boolean is(final ServerType other) {
		return Companion.INSTANCE == other;
	}

}
