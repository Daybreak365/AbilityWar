package daybreak.abilitywar.utils.base.reflect;

import com.google.common.base.Preconditions;
import daybreak.abilitywar.addon.AddonClassLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Daybreak 새벽
 */
public class ReflectionUtil {

	private ReflectionUtil() {
	}

	public static <T extends AccessibleObject> T setAccessible(final T accessibleObject) {
		accessibleObject.setAccessible(true);
		return accessibleObject;
	}

	public static class ClassUtil {

		private ClassUtil() {
		}

		public static Class<?> forName(final String name) throws ClassNotFoundException {
			try {
				return Class.forName(name);
			} catch (final ClassNotFoundException exception) {
				for (final ClassLoader classLoader : AddonClassLoader.getLoaders()) {
					try {
						return Class.forName(name, true, classLoader);
					} catch (ClassNotFoundException ignored) {}
				}
			}
			throw new ClassNotFoundException(name + " 클래스를 찾지 못하였습니다.");
		}

	}

	public static class FieldUtil {

		private FieldUtil() {
		}

		/**
		 * 존재하는 모든 {@link Field}를 가져옵니다.
		 * {@link Class}의 부모 클래스, 부모 클래스의 부모 클래스, ...에 있는 {@link Field} 또한 접근 제한자 상관 없이 가져옵니다.
		 *
		 * @param clazz     {@link Field} 목록을 검색할 {@link Class}
		 * @param fieldType 검색할 {@link Field}의 타입, 모든 {@link Field}를 검색하고 싶다면 {@link Object}.class를 사용하면 됨
		 * @return 검색한 {@link Field} 목록
		 */
		public static List<Field> getAllFields(Class<?> clazz, Class<?> fieldType) {
			final List<Field> fields = new ArrayList<>();
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

		public static <T> T getStaticValue(@NotNull Class<?> clazz, @NotNull String name) throws ClassCastException, NoSuchFieldException, IllegalAccessException {
			return getValue(clazz, null, name);
		}

		@SuppressWarnings("unchecked")
		public static <T> T getValue(@NotNull Class<?> clazz, @Nullable Object obj, @NotNull String name) throws ClassCastException, NoSuchFieldException, IllegalAccessException {
			Preconditions.checkNotNull(clazz);
			Preconditions.checkNotNull(name);
			return (T) setAccessible(clazz.getDeclaredField(name)).get(obj);
		}

		public static <T> T getValue(@NotNull Object obj, @NotNull String name) throws ClassCastException, NoSuchFieldException, IllegalAccessException {
			return getValue(obj.getClass(), obj, name);
		}

		public static void setValue(@NotNull Class<?> clazz, @Nullable Object obj, @NotNull String name, @Nullable Object value) throws NoSuchFieldException, IllegalAccessException {
			Preconditions.checkNotNull(clazz);
			Preconditions.checkNotNull(name);
			setAccessible(clazz.getDeclaredField(name)).set(obj, value);
		}

		public static void setValue(@NotNull Object obj, @NotNull String name, @Nullable Object value) throws NoSuchFieldException, IllegalAccessException {
			setValue(obj.getClass(), obj, name, value);
		}

		public static Field addFlag(@NotNull Field field, int modifiers) throws NoSuchFieldException, IllegalAccessException {
			setAccessible(Field.class.getDeclaredField("modifiers")).setInt(field, field.getModifiers() | modifiers);
			return field;
		}

		public static Field removeFlag(@NotNull Field field, int modifiers) throws NoSuchFieldException, IllegalAccessException {
			setAccessible(Field.class.getDeclaredField("modifiers")).setInt(field, field.getModifiers() & ~modifiers);
			return field;
		}

	}

}
