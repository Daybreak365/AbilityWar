package daybreak.abilitywar.utils.language;

public class KoreanUtil {

	private KoreanUtil() {}

	/**
	 * 받침 포함 여부에 따라 문자열을 변형하여 반환합니다.
	 * @param str			확인할 문자열
	 * @param firstValue	받침이 있을 때 사용할 값
	 * @param secondValue	받침이 없을 때 사용할 값
	 */
	public static String getCompleteWord(String str, String firstValue, String secondValue) {
		char lastChar = str.charAt(str.length() - 1);

		if (lastChar < 0xAC00 || lastChar > 0xD7A3) return str;
		return str + ((lastChar - 0xAC00) % 28 > 0 ? firstValue : secondValue);
	}

}
