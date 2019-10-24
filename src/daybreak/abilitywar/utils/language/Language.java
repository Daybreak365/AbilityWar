package daybreak.abilitywar.utils.language;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class Language {

	private static Language instance = null;

	public static Language getInstance(String fileName) throws IOException {
		if (instance == null) {
			instance = new Language(fileName);
		}
		return instance;
	}

	private final Properties language = new Properties();

	Language(String fileName) throws IOException {
		language.load(new BufferedReader(new InputStreamReader(Language.class.getResourceAsStream("/" + fileName), StandardCharsets.UTF_8)));
	}

	public String get(LanguageNode node) {
		return language.getProperty(node.key, "");
	}

	public enum LanguageNode {

		;

		private final String key;

		LanguageNode(String key) {
			this.key = key;
		}

	}

}