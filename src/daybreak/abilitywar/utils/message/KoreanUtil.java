package daybreak.abilitywar.utils.message;

/**
 * 종성에 따른 조사 처리 라이브러리
 *
 * @author Developed by Bae Yong Ju, and modified by Daybreak 새벽
 */
public class KoreanUtil {

	private static final JongSungDetector[] jongSungDetectors = {
			new HangulJongSungDetector(),
			new EnglishCapitalJongSungDetector(),
			new EnglishJongSungDetector(),
			new EnglishNumberKorStyleJongSungDetector(),
			new NumberJongSungDetector()
	};

	public static String getJosaModifiedString(String target, Josa josa) {
		if (target != null && target.length() > 0) {
			String readText = getReadText(target);

			for (JongSungDetector jongSungDetector : jongSungDetectors) {
				if (jongSungDetector.canHandle(readText))
					return target + (jongSungDetector.getJongSungType(readText) > 0 ? josa.withJongsung
							: josa.withoutJongsung);
			}
		}
		return target + josa.withJongsung + "(" + josa.withoutJongsung + ")";
	}

	public static String getNeededJosa(String target, Josa josa) {
		if (target != null && target.length() > 0) {
			String readText = getReadText(target);

			for (JongSungDetector jongSungDetector : jongSungDetectors) {
				if (jongSungDetector.canHandle(readText))
					return (jongSungDetector.getJongSungType(readText) > 0 ? josa.withJongsung : josa.withoutJongsung);
			}
		}
		return josa.withJongsung + "(" + josa.withoutJongsung + ")";
	}

	private static boolean isEndSkipText(char ch) {
		String skipChars = "\"')]}>";
		return skipChars.indexOf(ch) >= 0;
	}

	private static String getReadText(String str) {
		for (Josa readingRule : Josa.values()) {
			str = str.replace(readingRule.withJongsung, readingRule.withoutJongsung);
		}

		int i;
		for (i = str.length() - 1; i >= 0; --i) {
			char ch = str.charAt(i);

			if (!isEndSkipText(ch)) {
				break;
			}
		}

		return str.substring(0, i + 1);
	}

	private static interface JongSungDetector {

		boolean canHandle(String str);

		int getJongSungType(String str);

	}

	private static class HangulJongSungDetector implements JongSungDetector {

		@Override
		public boolean canHandle(String str) {
			return CharUtil.isHangulSyllables(CharUtil.lastChar(str));
		}

		@Override
		public int getJongSungType(String str) {
			return CharUtil.getHangulJongSungType(CharUtil.lastChar(str));
		}
	}

	private static class EnglishCapitalJongSungDetector implements JongSungDetector {

		@Override
		public boolean canHandle(String str) {
			char ch = CharUtil.lastChar(str);
			if (CharUtil.isAlphaUpperCase(ch)) {
				return true;
			}

			return false;
		}

		@Override
		public int getJongSungType(String str) {
			char lastChar = CharUtil.lastChar(str);
			switch (lastChar) {
				case 'M':
				case 'N':
					return 1;
				case 'L':
				case 'R':
					return 2;
				default:
					return 0;
			}

		}
	}

	private static class EnglishJongSungDetector implements JongSungDetector {

		@Override
		public boolean canHandle(String str) {
			char lastChar = CharUtil.lastChar(str);

			String unknownWordSuffixs = "qj";

			if (unknownWordSuffixs.indexOf(lastChar) >= 0) {
				return false;
			}

			return CharUtil.isAlpha(lastChar);
		}

		@Override
		public int getJongSungType(String str) {
			str = str.toLowerCase();

			int length = str.length();
			char lastChar1 = str.charAt(length - 1);

			String suffix = null;
			char lastChar2 = '\0';
			char lastChar3 = '\0';
			if (str.length() >= 3) {
				lastChar2 = str.charAt(length - 2);
				lastChar3 = str.charAt(length - 3);

				if (CharUtil.isAlpha(lastChar2) && CharUtil.isAlpha(lastChar3)) {
					suffix = String.valueOf(lastChar2) + String.valueOf(lastChar1);
				}
			}

			if (suffix != null) {
				String rieuljongSungChars = "l";
				String jongSungChars = "mn";
				String notJongSungChars = "afhiorsuvwxyz";
				String jongSungCandidateChars = "bckpt";
				String notJongSungCandidateChars = "deg";

				if (rieuljongSungChars.indexOf(lastChar1) >= 0) {
					return 2;
				} else if (jongSungChars.indexOf(lastChar1) >= 0) {
					return 1;
				} else if (notJongSungChars.indexOf(lastChar1) >= 0) {
					return 0;
				}

				if (jongSungCandidateChars.indexOf(lastChar1) >= 0) {
					switch (suffix) {
						case "ck":
						case "mb":
							return 1;
					}

					String vowelChars = "aeiou";
					return vowelChars.indexOf(lastChar2) >= 0 ? 1 : 0;
				} else if (notJongSungCandidateChars.indexOf(lastChar1) >= 0) {
					switch (suffix) {
						case "le": // ㄹ
							return 2;
						case "me": // ㅁ
						case "ne": // ㄴ
						case "ng": // ㅇ
							return 1;
						default:
							return 0;
					}
				}
			} else {
				if ("lr".indexOf(lastChar1) >= 0) {
					return 2;
				}
				if ("mn".indexOf(lastChar1) >= 0) {
					return 2;
				}
			}
			return 0;
		}
	}

	private static class EnglishNumberJongSungDetector implements JongSungDetector {
		public static ParseResult parse(String str) {
			ParseResult parseResult = new ParseResult();
			int i;
			boolean isSpaceFound = false;
			int numberPartBeiginIndex = 0;
			boolean isNumberCompleted = false;
			// 뒤에서부터 숫자, 영어 순서로 찾는다.
			for (i = str.length() - 1; i >= 0; --i) {
				char ch = str.charAt(i);

				if (!isNumberCompleted && !isSpaceFound && CharUtil.isNumber(ch)) {
					parseResult.isNumberFound = true;
					numberPartBeiginIndex = i;
					continue;
				}

				if (ch == ',') {
					continue;
				}

				if (!isNumberCompleted && parseResult.isNumberFound && !parseResult.isFloat && ch == '.') {
					parseResult.isFloat = true;
					continue;
				}

				if (!isNumberCompleted && parseResult.isNumberFound && !isSpaceFound && ch == ' ') {
					isSpaceFound = true;
					isNumberCompleted = true;
					continue;
				}

				if (!isNumberCompleted && parseResult.isNumberFound && !isSpaceFound && ch == '-') {
					isNumberCompleted = true;
					continue;
				}

				if (parseResult.isNumberFound && CharUtil.isAlpha(ch)) {
					parseResult.isEnglishFound = true;
					isNumberCompleted = true;
					break;
				}

				break;
			}

			if (parseResult.isNumberFound) {
				parseResult.numberPart = str.substring(numberPartBeiginIndex);

				try {
					parseResult.number = Double.parseDouble(parseResult.numberPart);
				} catch (NullPointerException | NumberFormatException ignore) {
					//Ignored
				}
			}

			return parseResult;
		}

		@Override
		public boolean canHandle(String str) {
			EnglishNumberJongSungDetector.ParseResult parseResult = EnglishNumberJongSungDetector.parse(str);

			return parseResult.isNumberFound && parseResult.isEnglishFound;

		}

		@Override
		public int getJongSungType(String str) {
			EnglishNumberJongSungDetector.ParseResult parseResult = EnglishNumberJongSungDetector.parse(str);

			if (!parseResult.isFloat) {
				long number = (long) (parseResult.number);

				if (number == 0) {
					return 0;
				}

				int twoDigit = (int) (number % 100);

				if (twoDigit != 12 && twoDigit >= 10 && twoDigit <= 19) {
					return 1;
				}

				if (number % 100000 == 0) {
					return 1;
				}
			}

			int oneDigit = CharUtil.lastChar(parseResult.numberPart) - '0';

			switch (oneDigit) {
				case 1:
				case 7:
				case 8:
				case 9:
					return 1;
			}

			return 0;
		}

		public static class ParseResult {
			public boolean isNumberFound;

			public boolean isEnglishFound;
			public double number;
			public boolean isFloat;
			public String numberPart;
		}
	}

	private static class EnglishNumberKorStyleJongSungDetector implements JongSungDetector {

		@Override
		public boolean canHandle(String str) {
			EnglishNumberJongSungDetector.ParseResult parseResult = EnglishNumberJongSungDetector.parse(str);

			return parseResult.isNumberFound && parseResult.isEnglishFound && !parseResult.isFloat
					&& parseResult.number <= 10;

		}

		@Override
		public int getJongSungType(String str) {
			EnglishNumberJongSungDetector.ParseResult parseResult = EnglishNumberJongSungDetector.parse(str);
			int number = (int) (parseResult.number);
			switch (number) {
				case 1:
				case 7:
				case 8:
				case 9:
				case 10:
					return 1;
			}

			return 0;
		}
	}

	private static class NumberJongSungDetector implements JongSungDetector {
		@Override
		public boolean canHandle(String str) {
			EnglishNumberJongSungDetector.ParseResult parseResult = EnglishNumberJongSungDetector.parse(str);

			return parseResult.isNumberFound;

		}

		@Override
		public int getJongSungType(String str) {
			EnglishNumberJongSungDetector.ParseResult parseResult = EnglishNumberJongSungDetector.parse(str);

			if (!parseResult.isFloat) {
				long number = (long) (parseResult.number);
				if (number % 1000000000000L == 0) {
					return 1;
				}
			}

			int oneDigit = CharUtil.lastChar(parseResult.numberPart) - '0';
			switch (oneDigit) {
				case 0:
				case 1:
				case 3:
				case 6:
					return 1;
				case 7:
				case 8:
					return 2;
			}

			return 0;
		}
	}

	public static enum Josa {

		은는("은", "는"),
		이가("이", "가"),
		을를("을", "를"),
		과와("과", "와"),
		으로로("으로", "로"),
		이었였("이었", "였");

		private final String withJongsung;
		private final String withoutJongsung;

		private Josa(String withJongsung, String withoutJongsung) {
			this.withJongsung = withJongsung;
			this.withoutJongsung = withoutJongsung;
		}

	}
}

class CharUtil {

	static boolean isAlpha(char ch) {
		return isAlphaLowerCase(ch) || isAlphaUpperCase(ch);
	}

	static boolean isAlphaLowerCase(char ch) {
		return ch >= 'a' && ch <= 'z';
	}

	static boolean isAlphaUpperCase(char ch) {
		return ch >= 'A' && ch <= 'Z';
	}

	static boolean isNumber(char ch) {
		return ch >= '0' && ch <= '9';
	}

	static boolean isHangulSyllables(char ch) {
		return ch >= 0xac00 && ch <= 0xd7af;
	}

	static int getHangulJongSungType(char ch) {
		int result = 0;
		if (isHangulSyllables(ch)) {
			int code = (ch - 0xAC00) % 28;
			if (code > 0)
				++result;
			if (code == 8)
				++result;
		}

		return result;
	}

	public static char lastChar(CharSequence charSequence) {
		if (charSequence == null) {
			return '\0';
		}
		int length = charSequence.length();
		if (length == 0) {
			return '\0';
		}

		return charSequence.charAt(length - 1);
	}

}
