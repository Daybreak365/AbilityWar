package daybreak.abilitywar.utils.base.language.korean.detectors;

import daybreak.abilitywar.utils.base.language.korean.JongSungDetector;

public class EnglishNumberKorStyleDetector implements JongSungDetector {

	public static JongSungDetector instance = new EnglishNumberKorStyleDetector();

	private EnglishNumberKorStyleDetector() {
	}

	@Override
	public boolean canHandle(String str) {
		EnglishNumberDetector.ParseResult parseResult = EnglishNumberDetector.parse(str);

		return parseResult.isNumberFound && parseResult.isEnglishFound && !parseResult.isFloat
				&& parseResult.number <= 10;

	}

	@Override
	public int getJongSungType(String str) {
		EnglishNumberDetector.ParseResult parseResult = EnglishNumberDetector.parse(str);
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