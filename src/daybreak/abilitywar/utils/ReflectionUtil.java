package daybreak.abilitywar.utils;

import daybreak.abilitywar.addon.AddonLoader;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Reflection Util
 * @author Daybreak 새벽
 */
public class ReflectionUtil {

	private ReflectionUtil() {}

	/**
	 * Class Util
	 */
	public static class ClassUtil {

		private ClassUtil() {}
		
		public static Class<?> forName(String name) throws ClassNotFoundException {
			try {
				return Class.forName(name);
			} catch (ClassNotFoundException first) {
				for(ClassLoader classLoader : AddonLoader.getClassLoaders()) {
					try {
						return Class.forName(name, true, classLoader);
					} catch (ClassNotFoundException ignore) {}
				}
			}
			
			throw new ClassNotFoundException(name + " 클래스를 찾지 못하였습니다.");
		}
		
	}

	/**
	 * Field Util
	 */
	public static class FieldUtil {

		private FieldUtil() {}

		/**
		 * 존재하는 모든 {@link Field}를 가져옵니다.
		 * {@link Class}의 부모 클래스, 부모 클래스의 부모 클래스, ...에 있는 {@link Field} 또한 접근 제한자 상관 없이 가져옵니다.
		 * @param clazz	{@link Field} 목록을 검색할 {@link Class}
		 * @param fieldType 검색할 {@link Field}의 타입, 모든 {@link Field}를 검색하고 싶다면 {@link Object}.class를 사용하면 됨
		 * @return 검색한 {@link Field} 목록
		 */
		public static ArrayList<Field> getExistingFields(Class<?> clazz, Class<?> fieldType) {
			final ArrayList<Field> fields = new ArrayList<>();
		    Class<?> finding = clazz;
		    while (finding != null && finding != Object.class) {
		        for (Field field : finding.getDeclaredFields()) {
		        	if (!field.isSynthetic() && fieldType.isAssignableFrom(field.getType())) {
		        		fields.add(field);
					}
				}
		        finding = finding.getSuperclass();
		    }
		    return fields;
		}
		
	}
	
}
