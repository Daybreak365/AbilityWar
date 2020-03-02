package daybreak.abilitywar.utils.base.language.korean.detectors;

import daybreak.abilitywar.utils.base.language.CharUtil;
import daybreak.abilitywar.utils.base.language.korean.JongSungDetector;

public class EnglishNumberDetector implements JongSungDetector {

	public static JongSungDetector instance = new EnglishNumberDetector();

	private EnglishNumberDetector() {
	}

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
		EnglishNumberDetector.ParseResult parseResult = EnglishNumberDetector.parse(str);

		return parseResult.isNumberFound && parseResult.isEnglishFound;

	}

	@Override
	public int getJongSungType(String str) {
		EnglishNumberDetector.ParseResult parseResult = EnglishNumberDetector.parse(str);

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