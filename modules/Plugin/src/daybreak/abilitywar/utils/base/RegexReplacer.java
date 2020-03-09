package daybreak.abilitywar.utils.base;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexReplacer {

	private final Pattern pattern;

	public RegexReplacer(String regex) {
		this.pattern = Pattern.compile(regex);
	}

	public String replaceAll(CharSequence sequence, Function<String, String> function) {
		Matcher matcher = pattern.matcher(sequence);
		StringBuffer buffer = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(buffer, function.apply(matcher.group(1)));
		}
		matcher.appendTail(buffer);
		return buffer.toString();
	}

}