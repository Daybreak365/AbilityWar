package daybreak.abilitywar.utils.base;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

public class BracketReplacer {

	public static final char INVALID = '\uFFFF', TOKEN = '$';
	private final char open, close;

	public BracketReplacer(final char open, final char close) {
		this.open = open;
		this.close = close;
	}

	@NotNull
	public String replaceAll(@NotNull final String string, @NotNull final Function<String, String> function) {
		final StringBuilder builder = new StringBuilder();
		final CharacterIterator iterator = new StringCharacterIterator(string);
		for (char character = iterator.current(); character != CharacterIterator.DONE; character = iterator.next()) {
			if (character == TOKEN) {
				final String parse = parseBracket(iterator);
				if (parse.length() > 0) {
					if (parse.charAt(0) != INVALID) {
						builder.append(function.apply(parse));
					} else {
						builder.append(parse.substring(1));
					}
				}
			} else {
				builder.append(character);
			}
		}
		return builder.toString();
	}

	private String parseBracket(final CharacterIterator iterator) {
		final char open = iterator.next();
		if (open != this.open) return new String(new char[] {INVALID, TOKEN, open});
		final StringBuilder builder = new StringBuilder();
		for (char character = iterator.next(); character != CharacterIterator.DONE; character = iterator.next()) {
			if (character == close) return builder.toString();
			builder.append(character);
		}
		return INVALID + TOKEN + open + builder.toString();
	}

}
