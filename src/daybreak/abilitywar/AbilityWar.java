package daybreak.abilitywar;

import daybreak.abilitywar.ability.AbilityFactory;
import daybreak.abilitywar.addon.AddonLoader;
import daybreak.abilitywar.config.AbilitySettings;
import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.game.MainCommand;
import daybreak.abilitywar.game.script.ScriptManager;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.installer.Installer;
import daybreak.abilitywar.utils.thread.AbilityWarThread;
import daybreak.abilitywar.utils.versioncompat.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Ability War 능력자 전쟁 플러그인
 *
 * @author Daybreak 새벽
 */
public class AbilityWar extends JavaPlugin {

	private static final Logger logger = Logger.getLogger(AbilityWar.class.getName());
	private static final Messager messager = new Messager();
	private static AbilityWar plugin;

	public static AbilityWar getPlugin() {
		if (plugin != null) {
			return plugin;
		}
		throw new IllegalStateException("플러그인이 아직 초기화되지 않았습니다.");
	}

	private Installer installer = null;

	public AbilityWar() {
		plugin = this;

	}

	public Installer getInstaller() throws IllegalStateException {
		if (installer != null) {
			return installer;
		}
		throw new IllegalStateException("버전 목록이 아직 불러와지지 않았습니다.");
	}

	@Override
	public void onEnable() {
		CompletableFuture.runAsync(new Runnable() {
			@Override
			public void run() {
				Installer installer = null;
				try {
					installer = new Installer("DayBreak365", "AbilityWar", AbilityWar.this);
					messager.sendConsoleMessage("버전 목록을 모두 불러왔습니다.");
				} catch (IOException | InterruptedException | ExecutionException ignore) {
				}
				AbilityWar.this.installer = installer;
				try {
					Class.forName("daybreak.abilitywar.utils.math.FastMath");
				} catch (ClassNotFoundException ignored) {
				}
			}
		});
		ServerVersion.compatVersion(this);
		messager.sendConsoleMessage("Server Version: " + Bukkit.getServer().getBukkitVersion());
		Bukkit.getPluginCommand("AbilityWar").setExecutor(new MainCommand(this));

		AddonLoader.loadAll();
		AddonLoader.enableAll();

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				AbilityFactory.nameValues();
				try {
					Configuration.load();
					AbilitySettings.load();
				} catch (IOException | InvalidConfigurationException e) {
					logger.log(Level.SEVERE, "콘피그를 불러오는 도중 오류가 발생하였습니다.");
					Bukkit.getPluginManager().disablePlugin(plugin);
				}
				ScriptManager.LoadAll();
			}
		});

		messager.sendConsoleMessage("플러그인이 활성화되었습니다.");
	}

	@Override
	public void onDisable() {
		AbilityWarThread.StopGame();
		try {
			Configuration.update();
		} catch (IOException | InvalidConfigurationException e1) {
			logger.log(Level.SEVERE, "콘피그를 업데이트하는 도중 오류가 발생하였습니다.");
		}
		AbilitySettings.Update();
		AddonLoader.disableAll();
		messager.sendConsoleMessage("플러그인이 비활성화되었습니다.");
	}

}
