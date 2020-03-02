package daybreak.abilitywar.utils.base.language.korean.detectors;

import daybreak.abilitywar.utils.base.language.CharUtil;
import daybreak.abilitywar.utils.base.language.korean.JongSungDetector;

public class EnglishCapitalDetector implements JongSungDetector {

	public static JongSungDetector instance = new EnglishCapitalDetector();

	private EnglishCapitalDetector() {
	}

	@Override
	public boolean canHandle(String str) {
		return CharUtil.isAlphaUpperCase(CharUtil.lastChar(str));
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