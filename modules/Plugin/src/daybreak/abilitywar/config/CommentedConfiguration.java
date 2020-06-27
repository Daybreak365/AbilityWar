package daybreak.abilitywar.config;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import daybreak.abilitywar.utils.base.logging.Logger;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.logging.Level;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConstructor;
import org.bukkit.configuration.file.YamlRepresenter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

/**
 * @author Developed by dumptruckman, LlmDL & Articdive, and modified by
 * Daybreak 새벽
 */
public class CommentedConfiguration extends YamlConfiguration {

	private static final Logger logger = Logger.getLogger(CommentedConfiguration.class.getName());

	private final DumperOptions yamlOptions = new DumperOptions();
	private final Representer yamlRepresenter = new YamlRepresenter();
	private final Yaml yaml = new Yaml(new YamlConstructor(), yamlRepresenter, yamlOptions);
	private final HashMap<String, String> comments = new HashMap<>();
	private final File file;

	public CommentedConfiguration(File file) throws IOException, InvalidConfigurationException {
		super.load(file);
		this.file = file;
	}

	public void load() throws IOException, InvalidConfigurationException {
		super.load(file);
	}

	public void save() throws IOException {
		this.save(file);
		if (!comments.isEmpty()) {
			String[] yamlContents = convertFileToString(file).split("[" + System.lineSeparator() + "]");

			StringBuilder newContents = new StringBuilder();
			String currentPath = "";
			boolean node;
			int depth = 0;

			for (String line : yamlContents) {
				if (line.contains(": ") || (line.length() > 1 && line.charAt(line.length() - 1) == ':')) {
					node = true;

					int index;
					index = line.indexOf(": ");
					if (index < 0) {
						index = line.length() - 1;
					}
					if (currentPath.isEmpty()) {
						currentPath = line.substring(0, index);
					} else {
						int whiteSpace = 0;
						for (int n = 0; n < line.length(); n++) {
							if (line.charAt(n) == ' ') {
								whiteSpace++;
							} else {
								break;
							}
						}
						if (whiteSpace / 2 > depth) {
							currentPath += "." + line.substring(whiteSpace, index);
							depth++;
						} else if (whiteSpace / 2 < depth) {
							int newDepth = whiteSpace / 2;
							for (int i = 0; i < depth - newDepth; i++) {
								currentPath = currentPath.replace(currentPath.substring(currentPath.lastIndexOf(".")),
										"");
							}
							int lastIndex = currentPath.lastIndexOf(".");
							if (lastIndex < 0) {
								currentPath = "";
							} else {
								currentPath = currentPath.replace(currentPath.substring(currentPath.lastIndexOf(".")),
										"");
								currentPath += ".";
							}
							currentPath += line.substring(whiteSpace, index);
							depth = newDepth;
						} else {
							int lastIndex = currentPath.lastIndexOf(".");
							if (lastIndex < 0) {
								currentPath = "";
							} else {
								currentPath = currentPath.replace(currentPath.substring(currentPath.lastIndexOf(".")),
										"");
								currentPath += ".";
							}
							currentPath += line.substring(whiteSpace, index);

						}

					}

				} else
					node = false;

				if (node) {
					String comment;
					comment = comments.get(currentPath);
					if (comment != null) {
						line = comment + System.getProperty("line.separator") + line
								+ System.getProperty("line.separator");
					} else {
						line += System.getProperty("line.separator");
					}
				}
				newContents.append(line).append((!node) ? System.getProperty("line.separator") : "");

			}
			while (newContents.toString().startsWith(System.getProperty("line.separator")))
				newContents = new StringBuilder(
						newContents.toString().replaceFirst(System.getProperty("line.separator"), ""));

			stringToFile(newContents.toString(), file);
		}
	}

	/**
	 * Adds a comment just before the specified path. The comment can be multiple
	 * lines. An empty string will indicate a blank line.
	 *
	 * @param path         Configuration path to add comment.
	 * @param commentLines Comments to add. One String per line.
	 */
	public void addComment(String path, String... commentLines) {
		StringBuilder commentstring = new StringBuilder();
		StringBuilder leadingSpaces = new StringBuilder();
		for (int n = 0; n < path.length(); n++) {
			if (path.charAt(n) == '.') {
				leadingSpaces.append("  ");
			}
		}
		for (String line : commentLines) {
			if (!line.isEmpty()) {
				line = leadingSpaces + line;
			} else {
				line = "";
			}
			if (commentstring.length() > 0) {
				commentstring.append(System.getProperty("line.separator"));
			}
			commentstring.append(line);
		}
		comments.put(path, commentstring.toString());
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

	/**
	 * Pass a file and it will return it's contents as a string.
	 *
	 * @param file File to read.
	 * @return Contents of file. String will be empty in case of any errors.
	 */
	public static String convertFileToString(File file) {
		if (file != null && file.exists() && file.canRead() && !file.isDirectory()) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try (InputStream is = new FileInputStream(file)) {
				Reader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
				reader.close();
			} catch (IOException e) {
				logger.log(Level.SEVERE, "An error has occurred while converting the File into String.");
			}
			return writer.toString();
		} else {
			return "";
		}
	}

	/**
	 * Writes the contents of a string to a file.
	 *
	 * @param source String to write.
	 * @param file   File to write to.
	 */
	public static void stringToFile(String source, File file) {
		try {
			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);

			out.write(source);
			out.close();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "An error has occurred while writing the String to the File.");
		}
	}
}