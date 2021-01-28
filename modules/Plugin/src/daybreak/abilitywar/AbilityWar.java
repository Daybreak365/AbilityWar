package daybreak.abilitywar;

import daybreak.abilitywar.ability.AbilityFactory;
import daybreak.abilitywar.addon.AddonLoader;
import daybreak.abilitywar.addon.installer.info.Addons;
import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.config.ability.AbilitySettings;
import daybreak.abilitywar.config.game.GameSettings;
import daybreak.abilitywar.config.kitpreset.KitConfiguration;
import daybreak.abilitywar.config.serializable.AbilityKit;
import daybreak.abilitywar.config.serializable.team.PresetContainer;
import daybreak.abilitywar.config.serializable.team.TeamPreset;
import daybreak.abilitywar.config.serializable.team.TeamPreset.TeamScheme;
import daybreak.abilitywar.game.GameManager;
import daybreak.abilitywar.game.list.mix.synergy.SynergyFactory;
import daybreak.abilitywar.game.manager.GameFactory;
import daybreak.abilitywar.game.script.manager.ScriptManager;
import daybreak.abilitywar.game.specialthanks.SpecialThanks;
import daybreak.abilitywar.music.christmas.ChristmasMusic;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.logging.Logger;
import daybreak.abilitywar.utils.base.math.FastMath;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.installer.Installer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

/**
 * @author Daybreak 새벽
 */
public class AbilityWar extends JavaPlugin implements Provider {

	static {
		ConfigurationSerialization.registerClass(AbilityKit.class);
		ConfigurationSerialization.registerClass(PresetContainer.class);
		ConfigurationSerialization.registerClass(TeamPreset.class);
		ConfigurationSerialization.registerClass(TeamScheme.class);
	}

	private static final Logger logger = Logger.getLogger(AbilityWar.class);
	private static AbilityWar plugin;

	public static AbilityWar getPlugin() {
		if (plugin != null) {
			return plugin;
		}
		throw new IllegalStateException("플러그인이 아직 초기화되지 않았습니다.");
	}

	private Installer installer = null;
	private final Commands commands = new Commands(this);

	public AbilityWar() {
		plugin = this;
		CompletableFuture.runAsync(new Runnable() {
			@Override
			public void run() {
				Installer installer = null;
				try {
					installer = new Installer("DayBreak365", "AbilityWar", AbilityWar.this);
					Messager.sendConsoleMessage("버전 목록을 모두 불러왔습니다.");
				} catch (IOException | InterruptedException | ExecutionException ignore) {}
				AbilityWar.this.installer = installer;
				Addons.load();
				Messager.sendConsoleMessage("추천 애드온 목록을 모두 불러왔습니다.");
			}
		});
	}

	public Installer getInstaller() throws IllegalStateException {
		if (installer != null) {
			return installer;
		}
		throw new IllegalStateException("버전 목록이 아직 불러와지지 않았습니다.");
	}

	public Commands getCommands() {
		return commands;
	}

	@Override
	public void onEnable() {
		if (!ServerVersion.compatVersion(this)) return;
		Messager.sendConsoleMessage("Server Version: " + Bukkit.getBukkitVersion());
		Bukkit.getPluginCommand("AbilityWar").setExecutor(commands);

		AddonLoader.loadAll();
		AddonLoader.enableAll();

		try {
			Class.forName(FastMath.class.getName());
			Class.forName(SpecialThanks.class.getName());
			Class.forName(ChristmasMusic.class.getName());
		} catch (Exception ignored) {
		}

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				AbilityFactory.nameValues();
				SynergyFactory.getSynergies();
				try {
					Configuration.update();
					KitConfiguration.getInstance().update();
				} catch (IOException | InvalidConfigurationException e) {
					logger.log(Level.SEVERE, "콘피그를 불러오는 도중 오류가 발생하였습니다.");
					Bukkit.getPluginManager().disablePlugin(plugin);
				}
				for (AbilitySettings abilitySetting : AbilitySettings.getAbilitySettings()) {
					abilitySetting.update();
				}
				GameFactory.nameValues();
				for (GameSettings gameSetting : GameSettings.getGameSettings()) {
					gameSetting.update();
				}
				ScriptManager.loadAll();
			}
		});
		Messager.sendConsoleMessage("플러그인이 활성화되었습니다. §8(§7" + ServerVersion.getName() + "§8)");
	}

	@Override
	public void onDisable() {
		GameManager.stopGame();
		try {
			Configuration.update();
			KitConfiguration.getInstance().update();
		} catch (IOException | InvalidConfigurationException e) {
			logger.log(Level.SEVERE, "콘피그를 업데이트하는 도중 오류가 발생하였습니다.");
		}
		for (AbilitySettings abilitySetting : AbilitySettings.getAbilitySettings()) {
			abilitySetting.update();
		}
		for (GameSettings gameSetting : GameSettings.getGameSettings()) {
			gameSetting.update();
		}
		AddonLoader.disableAll();
		Messager.sendConsoleMessage("플러그인이 비활성화되었습니다.");
	}

	@Override
	public AbilityWar getInstance() {
		return this;
	}
}
