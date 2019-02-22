package Marlang.AbilityWar.Addon;

import org.bukkit.plugin.Plugin;

import Marlang.AbilityWar.AbilityWar;
import Marlang.AbilityWar.Addon.AddonLoader.DescriptionFile;

/**
 * 애드온
 */
abstract public class Addon {

	private DescriptionFile description;

	abstract public void onEnable();

	abstract public void onDisable();

	/**
	 * AbilityWar 플러그인을 받아옵니다.
	 */
	protected Plugin getPlugin() {
		return AbilityWar.getPlugin();
	}

	/**
	 * addon.yml에 작성한 애드온의 설명을 받아옵니다.
	 */
	public DescriptionFile getDescription() {
		return description;
	}

}
