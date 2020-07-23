package daybreak.abilitywar.utils.base;

import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexReplacer {

	protected final Pattern pattern;

	public RegexReplacer(String regex) {
		this.pattern = Pattern.compile(regex);
	}

	public String replaceAll(String string, Function<MatchResult, String> function) {
		final Matcher matcher = pattern.matcher(string);
		final StringBuffer buffer = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(buffer, Matcher.quoteReplacement(function.apply(matcher)));
		}
		matcher.appendTail(buffer);
		return buffer.toString();
	}

}