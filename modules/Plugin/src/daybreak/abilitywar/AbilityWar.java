package daybreak.abilitywar;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityFactory;
import daybreak.abilitywar.addon.AddonLoader;
import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.config.serializable.AbilityKit;
import daybreak.abilitywar.config.serializable.team.PresetContainer;
import daybreak.abilitywar.config.serializable.team.TeamPreset;
import daybreak.abilitywar.config.serializable.team.TeamPreset.TeamScheme;
import daybreak.abilitywar.game.GameManager;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;
import daybreak.abilitywar.game.manager.gui.SpecialThanksGUI;
import daybreak.abilitywar.game.script.manager.ScriptManager;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.logging.Logger;
import daybreak.abilitywar.utils.base.math.FastMath;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.installer.Installer;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Ability War 능력자 전쟁 플러그인
 *
 * @author Daybreak 새벽
 */
public class AbilityWar extends JavaPlugin {

	static {
		ConfigurationSerialization.registerClass(AbilityKit.class);
		ConfigurationSerialization.registerClass(PresetContainer.class);
		ConfigurationSerialization.registerClass(TeamPreset.class);
		ConfigurationSerialization.registerClass(TeamScheme.class);
	}

	private static final Logger logger = Logger.getLogger(AbilityWar.class);
	private static final long mainThreadId = Thread.currentThread().getId();
	private static AbilityWar plugin;

	public static AbilityWar getPlugin() {
		if (plugin != null) {
			return plugin;
		}
		throw new IllegalStateException("플러그인이 아직 초기화되지 않았습니다.");
	}

	public static boolean isMainThread() {
		return Thread.currentThread().getId() == mainThreadId;
	}

	private Installer installer = null;

	public AbilityWar() {
		plugin = this;
		CompletableFuture.runAsync(new Runnable() {
			@Override
			public void run() {
				Installer installer = null;
				try {
					installer = new Installer("DayBreak365", "AbilityWar", AbilityWar.this);
					Messager.sendConsoleMessage("버전 목록을 모두 불러왔습니다.");
				} catch (IOException | InterruptedException | ExecutionException ignore) {
				}
				AbilityWar.this.installer = installer;
			}
		});
	}

	public Installer getInstaller() throws IllegalStateException {
		if (installer != null) {
			return installer;
		}
		throw new IllegalStateException("버전 목록이 아직 불러와지지 않았습니다.");
	}

	@Override
	public void onEnable() {
		if (!ServerVersion.compatVersion(this)) return;
		Messager.sendConsoleMessage("Server Version: " + Bukkit.getServer().getBukkitVersion());
		Bukkit.getPluginCommand("AbilityWar").setExecutor(new Commands(this));

		AddonLoader.loadAll();
		AddonLoader.enableAll();

		try {
			Class.forName(FastMath.class.getName());
			Class.forName(SpecialThanksGUI.class.getName());
		} catch (ClassNotFoundException ignored) {
		}

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				AbilityFactory.nameValues();
				try {
					Configuration.update();
				} catch (IOException | InvalidConfigurationException e) {
					logger.log(Level.SEVERE, "콘피그를 불러오는 도중 오류가 발생하였습니다.");
					Bukkit.getPluginManager().disablePlugin(plugin);
				}
				ScriptManager.loadAll();
			}
		});

		Messager.sendConsoleMessage("플러그인이 활성화되었습니다.");
	}

	@Override
	public void onDisable() {
		GameManager.stopGame();
		try {
			Configuration.update();
			AbilityBase.abilitySettings.update();
			Synergy.synergySettings.update();
		} catch (IOException | InvalidConfigurationException e) {
			logger.log(Level.SEVERE, "콘피그를 업데이트하는 도중 오류가 발생하였습니다.");
		}
		AddonLoader.disableAll();
		Messager.sendConsoleMessage("플러그인이 비활성화되었습니다.");
	}

}
