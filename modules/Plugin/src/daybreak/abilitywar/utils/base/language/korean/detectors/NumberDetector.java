package daybreak.abilitywar.utils.base.language.korean.detectors;

import daybreak.abilitywar.utils.base.language.CharUtil;
import daybreak.abilitywar.utils.base.language.korean.JongSungDetector;

public class NumberDetector implements JongSungDetector {

	public static JongSungDetector instance = new NumberDetector();

	private NumberDetector() {
	}

	@Override
	public boolean canHandle(String str) {
		EnglishNumberDetector.ParseResult parseResult = EnglishNumberDetector.parse(str);

		return parseResult.isNumberFound;

	}

	@Override
	public int getJongSungType(String str) {
		EnglishNumberDetector.ParseResult parseResult = EnglishNumberDetector.parse(str);

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