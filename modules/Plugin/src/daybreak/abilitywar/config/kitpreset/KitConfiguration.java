package daybreak.abilitywar.config.kitpreset;

import daybreak.abilitywar.config.CachedConfig;
import daybreak.abilitywar.config.CommentedConfiguration;
import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.config.serializable.AbilityKit;
import daybreak.abilitywar.config.serializable.KitPreset;
import java.io.IOException;
import java.util.List;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.ItemStack;

public class KitConfiguration extends CachedConfig<KitNodes> {

	private static final KitConfiguration INSTANCE;

	public static KitConfiguration getInstance() {
		return INSTANCE;
	}

	static {
		try {
			INSTANCE = new KitConfiguration();
		} catch (IOException | InvalidConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private KitConfiguration() throws IOException, InvalidConfigurationException {
		super(KitNodes.class, "kits.yml");
		final CommentedConfiguration configuration = Configuration.getConfig();
		if (configuration.isSet("게임.기본템")) {
			final List<KitPreset> list = getList(KitNodes.KIT_PRESET, KitPreset.class);
			final KitPreset kitPreset = new KitPreset((List<ItemStack>) configuration.getList("게임.기본템"));
			list.add(kitPreset);
			modifyProperty(KitNodes.KIT_PRESET, list);
			modifyProperty(KitNodes.KIT, new KitPreset(kitPreset));
			configuration.set("게임.기본템", null);
			configuration.save();
		}
	}

	public static class KitSettings {

		private KitSettings() {}

		public static KitPreset getKit() {
			return INSTANCE.get(KitNodes.KIT);
		}

		public static List<KitPreset> getKitPresets() {
			return INSTANCE.getList(KitNodes.KIT_PRESET, KitPreset.class);
		}

		public static AbilityKit getAbilityKit() {
			return INSTANCE.get(KitNodes.ABILITY_KIT);
		}

	}

}
