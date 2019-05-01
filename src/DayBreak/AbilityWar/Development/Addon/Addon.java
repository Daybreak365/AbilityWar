package DayBreak.AbilityWar.Development.Addon;

import org.bukkit.plugin.Plugin;

import DayBreak.AbilityWar.AbilityWar;
import DayBreak.AbilityWar.Development.Addon.AddonLoader.DescriptionFile;

/**
 * 애드온
 * @author DayBreak 새벽
 */
abstract public class Addon {

	private DescriptionFile description;

	private ClassLoader classLoader;
	
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

	/**
	 * 이 애드온을 불러올 때 사용된 ClassLoader를 받아옵니다.
	 */
	public ClassLoader getClassLoader() {
		return classLoader;
	}

}
