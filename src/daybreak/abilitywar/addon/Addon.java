package daybreak.abilitywar.addon;

import org.bukkit.plugin.Plugin;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.addon.AddonLoader.DescriptionFile;

/**
 * 애드온
 * @author DayBreak 새벽
 */
public abstract class Addon {

	private DescriptionFile description;

	private ClassLoader classLoader;

	public abstract void onEnable();

	public abstract void onDisable();

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
