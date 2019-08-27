package DayBreak.AbilityWar.Utils;

/**
 * Validate
 * @author DayBreak 새벽
 */
public class Validate {

	private Validate() {}

	/**
	 * 객체가 null인지 아닌지 확인합니다.
	 * @param object						null 여부를 확인할 객체
	 * @throws NullPointerException			객체가 null일 경우
	 * @return								객체가 null이 아닐 경우 그대로 반환합니다.
	 */
	public static <T> T notNull(T object) throws NullPointerException {
		if(object == null) throw new NullPointerException();
		return object;
	}

	public static void MinimumConstant(Class<?> enumClass, int count) throws IllegalArgumentException {
		if(notNull(enumClass).isEnum()) {
			if(enumClass.getEnumConstants().length < count) {
				throw new IllegalArgumentException(enumClass.getName() + "에 최소 " + count + "개의 상수가 있어야 합니다.");
			}
		} else {
			throw new IllegalArgumentException();
		}
	}
	
}
