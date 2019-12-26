package daybreak.abilitywar.utils;

/**
 * Validate
 *
 * @author Daybreak 새벽
 */
public class Validate {

	private Validate() {
	}

	/**
	 * 객체가 null인지 아닌지 확인합니다.
	 *
	 * @param object null 여부를 확인할 객체
	 * @return 객체가 null이 아닐 경우 그대로 반환합니다.
	 * @throws IllegalStateException 객체가 null일 경우 예외를 발생시킵니다.
	 */
	public static <T> T notNull(T object) throws IllegalStateException {
		if (object == null) throw new IllegalStateException("This object cannot be null.");
		return object;
	}

	/**
	 * enum 클래스에 최소 count 개의 상수가 있는지 확인합니다.
	 *
	 * @param enumClass 상수의 개수를 확인할 enum 클래스
	 * @param count     상수의 최소 개수
	 * @throws IllegalStateException    상수의 최소 개수를 만족하지 못할 경우 예외를 발생시킵니다.
	 * @throws IllegalArgumentException enumClass 클래스가 enum 타입이 아닐 경우 예외를 발생시킵니다.
	 */
	public static void minimumConstant(Class<?> enumClass, int count) throws IllegalStateException {
		if (notNull(enumClass).isEnum()) {
			if (enumClass.getEnumConstants().length < count) {
				throw new IllegalStateException(enumClass.getName() + "에 최소 " + count + "개의 상수가 있어야 합니다.");
			}
		} else {
			throw new IllegalArgumentException(enumClass.getName() + " 클래스는 enum 타입이 아닙니다.");
		}
	}

}
