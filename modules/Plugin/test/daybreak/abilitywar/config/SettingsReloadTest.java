package daybreak.abilitywar.config;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.config.ability.AbilitySettings;
import daybreak.abilitywar.config.game.GameSettings;
import daybreak.abilitywar.config.interfaces.Configurable;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.GameManifest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class SettingsReloadTest {

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> types() {
		return Arrays.asList(new Object[][] {{"ability"}, {"game"}});
	}

	@Rule
	public final TemporaryFolder temporary = new TemporaryFolder();

	private final String type;
	private File file;
	private Runnable update;
	private Configurable<Integer> value, other;
	private AbilitySettings abilitySettings;
	private GameSettings gameSettings;

	public SettingsReloadTest(String type) {
		this.type = type;
	}

	@Before
	public void createSettings() throws Exception {
		file = temporary.newFile(type + ".yml");
		if ("ability".equals(type)) {
			abilitySettings = new AbilitySettings(file);
			value = abilitySettings.new SettingObject<Integer>(TestAbility.class, "value", 10);
			other = abilitySettings.new SettingObject<Integer>(TestAbility.class, "other", 20);
			update = abilitySettings::update;
		} else {
			gameSettings = new GameSettings(file);
			value = gameSetting("value", 10);
			other = gameSetting("other", 20);
			update = gameSettings::update;
		}
		// Ensure that a save has a different timestamp from the source file,
		// regardless of how quickly construction and update run.
		assertTrue(file.setLastModified(1000));
		update.run();
	}

	private Configurable<Integer> gameSetting(String key, int defaultValue) {
		return gameSettings.new Setting<Integer>(TestGame.class, key, defaultValue) {
			@Override
			public boolean condition(Integer value) {
				return true;
			}
		};
	}

	@After
	public void unregisterFixtures() {
		if (abilitySettings != null) AbilitySettings.getAbilitySettings().remove(abilitySettings);
		if (gameSettings != null) GameSettings.getGameSettings().remove(gameSettings);
	}

	@Test
	public void readingSavedSettingsDoesNotRewriteFile() throws Exception {
		final long modified = file.lastModified();
		final byte[] contents = Files.readAllBytes(file.toPath());
		Thread.sleep(30);
		for (int i = 0; i < 5; i++) {
			assertEquals(Integer.valueOf(10), value.getValue());
			assertEquals(Integer.valueOf(20), other.getValue());
		}
		assertEquals("Reading cached values must not save the YAML again", modified, file.lastModified());
		assertArrayEquals(contents, Files.readAllBytes(file.toPath()));
	}

	@Test
	public void externalEditsStillReloadOnTheNextRead() throws Exception {
		final YamlConfiguration edited = YamlConfiguration.loadConfiguration(file);
		edited.set(value.getPath(), 37);
		edited.save(file);
		assertTrue(file.setLastModified(2000));

		assertEquals(Integer.valueOf(37), value.getValue());
		assertEquals(Integer.valueOf(20), other.getValue());
		assertEquals(37, YamlConfiguration.loadConfiguration(file).getInt(value.getPath()));
	}

	@Test
	public void reloadKeepsPendingLocalEditsAndRestoresMissingDefaults() throws Exception {
		assertTrue(value.setValue(42));
		final YamlConfiguration edited = YamlConfiguration.loadConfiguration(file);
		edited.set(value.getPath(), 99);
		edited.set(other.getPath(), null);
		edited.save(file);
		assertTrue(file.setLastModified(2000));

		assertEquals(Integer.valueOf(42), value.getValue());
		assertEquals(Integer.valueOf(20), other.getValue());
		final YamlConfiguration saved = YamlConfiguration.loadConfiguration(file);
		assertEquals(42, saved.getInt(value.getPath()));
		assertEquals(20, saved.getInt(other.getPath()));
	}

	@AbilityManifest(name = "SettingsFixture", rank = AbilityManifest.Rank.C, species = AbilityManifest.Species.HUMAN)
	public abstract static class TestAbility extends AbilityBase {
		protected TestAbility(AbstractGame.Participant participant) {
			super(participant);
		}
	}

	@GameManifest(name = "SettingsFixture", description = {})
	public abstract static class TestGame extends AbstractGame {
		protected TestGame() {
			super(Collections.emptyList());
		}
	}
}
