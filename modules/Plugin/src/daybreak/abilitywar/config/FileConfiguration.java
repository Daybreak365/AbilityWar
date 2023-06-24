package daybreak.abilitywar.config;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import daybreak.abilitywar.utils.base.logging.Logger;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConstructor;
import org.bukkit.configuration.file.YamlRepresenter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;

/**
 * @author Developed by dumptruckman, LlmDL & Articdive, and modified by
 * Daybreak 새벽
 */
public class FileConfiguration extends YamlConfiguration {

	private static final Logger logger = Logger.getLogger(FileConfiguration.class.getName());

	private final DumperOptions yamlOptions = new DumperOptions();
	private final Representer yamlRepresenter = new YamlRepresenter();
	private final Yaml yaml = new Yaml(new YamlConstructor(), yamlRepresenter, yamlOptions);
	private final HashMap<String, String> comments = new HashMap<>();
	private final File file;

	public FileConfiguration(File file) throws IOException, InvalidConfigurationException {
		super.load(file);
		this.file = file;
	}

	public void load() throws IOException, InvalidConfigurationException {
		super.load(file);
	}

	public void save() throws IOException {
		this.save(file);
	}

	public void save(File file) throws IOException {
		Preconditions.checkNotNull(file, "File cannot be null");

		file.mkdirs();
		String data = this.saveToString();

		try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8)) {
			writer.write(data);
		}
	}

	@Override
	public String saveToString() {
		yamlOptions.setIndent(options().indent());
		yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		yamlOptions.setWidth(10000);
		yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

		String dump = yaml.dump(getValues(false));

		if (dump.equals(BLANK_CONFIG)) {
			dump = "";
		}

		return dump;
	}

}