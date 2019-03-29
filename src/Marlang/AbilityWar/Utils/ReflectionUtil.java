package Marlang.AbilityWar.Utils;

import Marlang.AbilityWar.Addon.Addon;
import Marlang.AbilityWar.Addon.AddonLoader;

/**
 * Reflection Util
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
			} catch (ClassNotFoundException exceptionOne) {
				for(Addon addon : AddonLoader.getAddons()) {
					try {
						return Class.forName(name, true, addon.getClassLoader());
					} catch (ClassNotFoundException exceptionTwo) {}
				}
			}
			
			throw new ClassNotFoundException(name + " 클래스를 찾지 못하였습니다.");
		}
		
	}
	
}
