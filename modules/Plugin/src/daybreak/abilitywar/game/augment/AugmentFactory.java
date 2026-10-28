package daybreak.abilitywar.game.augment;

import com.google.common.base.Preconditions;
import daybreak.abilitywar.game.augment.list.AbilityEcho;
import daybreak.abilitywar.game.augment.list.AbilityOverdrive;
import daybreak.abilitywar.game.augment.list.ArrowFocus;
import daybreak.abilitywar.game.augment.list.BattleTrance;
import daybreak.abilitywar.game.augment.list.Comeback;
import daybreak.abilitywar.game.augment.list.Executioner;
import daybreak.abilitywar.game.augment.list.ExtraHeart;
import daybreak.abilitywar.game.augment.list.GlassCannon;
import daybreak.abilitywar.game.augment.list.LastStand;
import daybreak.abilitywar.game.augment.list.PrismaticBody;
import daybreak.abilitywar.game.augment.list.RaidBoss;
import daybreak.abilitywar.game.augment.list.SwiftStart;
import daybreak.abilitywar.game.augment.list.Thornmail;
import daybreak.abilitywar.game.augment.list.VampiricBlade;
import daybreak.abilitywar.utils.base.logging.Logger;
import daybreak.abilitywar.utils.base.reflect.ReflectionUtil;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AugmentFactory {

	private static final Logger logger = Logger.getLogger(AugmentFactory.class);
	private static final Map<String, AugmentRegistration> usedKeys = new LinkedHashMap<>();
	private static final Map<Class<? extends Augment>, AugmentRegistration> registeredAugments = new LinkedHashMap<>();

	static {
		registerAugment(ExtraHeart.class);
		registerAugment(SwiftStart.class);
		registerAugment(BattleTrance.class);
		registerAugment(PrismaticBody.class);
		registerAugment(GlassCannon.class);
		registerAugment(VampiricBlade.class);
		registerAugment(Executioner.class);
		registerAugment(Comeback.class);
		registerAugment(ArrowFocus.class);
		registerAugment(Thornmail.class);
		registerAugment(LastStand.class);
		registerAugment(RaidBoss.class);
		registerAugment(AbilityEcho.class);
		registerAugment(AbilityOverdrive.class);
	}

	private AugmentFactory() {}

	public static void registerAugment(Class<? extends Augment> augmentClass) {
		if (!registeredAugments.containsKey(augmentClass)) {
			try {
				final AugmentRegistration registration = new AugmentRegistration(augmentClass);
				final String key = registration.getManifest().key();
				if (!usedKeys.containsKey(key)) {
					registeredAugments.put(registration.getAugmentClass(), registration);
					usedKeys.put(key, registration);
				} else {
					logger.debug("§e" + augmentClass.getName() + " §f증강은 겹치는 키가 있어 등록되지 않았습니다.");
				}
			} catch (Exception | ExceptionInInitializerError e) {
				logger.error("§e" + augmentClass.getName() + " §f증강 등록 중 오류가 발생하였습니다.");
				e.printStackTrace();
			}
		}
	}

	public static void registerAugment(String className) {
		try {
			registerAugment(ReflectionUtil.ClassUtil.forName(className).asSubclass(Augment.class));
		} catch (ClassNotFoundException e) {
			logger.debug("§e" + className + " §f클래스는 존재하지 않습니다.");
		} catch (ClassCastException e) {
			logger.error("§e" + className + " §f클래스는 Augment를 구현하지 않습니다.");
		} catch (NoClassDefFoundError e) {
			logger.debug("§e" + className + " §f클래스를 찾을 수 없습니다.");
		}
	}

	public static List<AugmentRegistration> getRegistrations() {
		return new ArrayList<>(registeredAugments.values());
	}

	public static AugmentRegistration getByKey(String key) {
		return usedKeys.get(key);
	}

	public static class AugmentRegistration {
		private final Class<? extends Augment> clazz;
		private final Constructor<? extends Augment> constructor;
		private final AugmentManifest manifest;

		private AugmentRegistration(Class<? extends Augment> clazz) throws NoSuchMethodException {
			this.clazz = clazz;
			this.constructor = clazz.getConstructor();
			if (!clazz.isAnnotationPresent(AugmentManifest.class)) throw new IllegalArgumentException("AugmentManifest가 없는 증강입니다.");
			this.manifest = clazz.getAnnotation(AugmentManifest.class);
			Preconditions.checkNotNull(manifest.key());
			Preconditions.checkNotNull(manifest.name());
			Preconditions.checkNotNull(manifest.rarity());
			Preconditions.checkNotNull(manifest.description());
		}

		public Class<? extends Augment> getAugmentClass() { return clazz; }
		public Constructor<? extends Augment> getConstructor() { return constructor; }
		public AugmentManifest getManifest() { return manifest; }

		public Augment newInstance() {
			try {
				return constructor.newInstance();
			} catch (Exception e) {
				throw new IllegalStateException("증강 인스턴스를 생성할 수 없습니다: " + clazz.getName(), e);
			}
		}
	}
}
