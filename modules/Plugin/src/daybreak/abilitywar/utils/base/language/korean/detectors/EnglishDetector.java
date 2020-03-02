package daybreak.abilitywar.utils.base.language.korean.detectors;

import daybreak.abilitywar.utils.base.language.CharUtil;
import daybreak.abilitywar.utils.base.language.korean.JongSungDetector;

public class EnglishDetector implements JongSungDetector {

	public static JongSungDetector instance = new EnglishDetector();

	private EnglishDetector() {
	}

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