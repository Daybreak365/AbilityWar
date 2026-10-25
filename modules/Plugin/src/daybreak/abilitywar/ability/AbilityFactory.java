package daybreak.abilitywar.ability;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.Provider;
import daybreak.abilitywar.ability.Tips.Difficulty;
import daybreak.abilitywar.ability.Tips.Stats;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.ability.decorator.TargetHandler;
import daybreak.abilitywar.addon.AddonClassLoader;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.annotations.Support;
import daybreak.abilitywar.utils.base.collect.Pair;
import daybreak.abilitywar.utils.base.logging.Logger;
import daybreak.abilitywar.utils.base.minecraft.server.ServerNotSupportedException;
import daybreak.abilitywar.utils.base.minecraft.server.ServerType;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.base.minecraft.version.VersionNotSupportedException;
import daybreak.abilitywar.utils.base.reflect.ReflectionUtil;
import daybreak.abilitywar.utils.library.MaterialX;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link AbilityBase}를 기반으로 하는 모든 능력을 관리하는 클래스입니다.
 */
public class AbilityFactory {

	private static final Logger logger = Logger.getLogger(AbilityFactory.class);
	private static final Map<String, AbilityRegistration> usedNames = new HashMap<>();
	private static final Map<Class<? extends AbilityBase>, AbilityRegistration> registeredAbilities = new HashMap<>();

	static {
		BuiltinAbilities.registerAll(AbilityFactory::registerAbility,
				(packagePrefix, simpleName) -> registerAbility(packagePrefix + ServerVersion.getName() + "." + simpleName));
	}

	private AbilityFactory() {
	}

	/**
	 * 능력을 등록합니다.
	 * <p>
	 * 능력을 등록하기 전, AbilityManifest 어노테이션이 클래스에 존재하는지, 겹치는 이름은 없는지, 생성자는 올바른지 확인해주시길
	 * 바랍니다.
	 * <p>
	 * 이미 등록된 능력일 경우 다시 등록이 되지 않습니다.
	 *
	 * @param abilityClass 능력 클래스
	 */
	public static void registerAbility(Class<? extends AbilityBase> abilityClass) {
		if (!registeredAbilities.containsKey(abilityClass)) {
			try {
				final AbilityRegistration registration = new AbilityRegistration(abilityClass);
				final String name = registration.getManifest().name();
				if (!usedNames.containsKey(name)) {
					registeredAbilities.put(registration.getAbilityClass(), registration);
					usedNames.put(name, registration);
				} else {
					logger.debug("§e" + abilityClass.getName() + " §f능력은 겹치는 이름이 있어 등록되지 않았습니다.");
				}
			} catch (IllegalAccessException e) {
				logger.error(abilityClass.getName() + " 능력 클래스에 public 생성자가 존재하지 않습니다.");
			} catch (VersionNotSupportedException e) {
				logger.debug("§e" + abilityClass.getName() + " §f능력은 이 버전에서 지원되지 않습니다.");
			} catch (ServerNotSupportedException e) {
				logger.debug("§e" + abilityClass.getName() + " §f능력은 이 서버에서 지원되지 않습니다. (이 서버: " + ServerType.getServerType().name() + ") (지원되는 서버: " + Arrays.toString(e.getSupported()) + ")");
			} catch (Exception | ExceptionInInitializerError e) {
				logger.error("§e" + abilityClass.getName() + " §f능력 등록 중 오류가 발생하였습니다.");
				e.printStackTrace();
			}
		}
	}

	/**
	 * 능력을 등록합니다.
	 * <p>
	 * 능력을 등록하기 전, AbilityManifest 어노테이션이 클래스에 존재하는지, 겹치는 이름은 없는지, 생성자는 올바른지 확인해주시길
	 * 바랍니다.
	 * <p>
	 * 이미 등록된 능력일 경우 다시 등록이 되지 않습니다.
	 *
	 * @param className 능력 클래스 이름
	 */
	public static void registerAbility(String className) {
		try {
			registerAbility(ReflectionUtil.ClassUtil.forName(className).asSubclass(AbilityBase.class));
		} catch (ClassNotFoundException e) {
			logger.debug("§e" + className + " §f클래스는 존재하지 않습니다.");
		} catch (ClassCastException e) {
			logger.error("§e" + className + " §f클래스는 AbilityBase를 확장하지 않습니다.");
		} catch (NoClassDefFoundError e) {
			logger.debug("§e" + className + " §f클래스를 찾을 수 없습니다.");
		}
	}

	public static AbilityRegistration getRegistration(Class<? extends AbilityBase> clazz) {
		return registeredAbilities.get(clazz);
	}

	public static boolean isRegistered(Class<? extends AbilityBase> clazz) {
		return registeredAbilities.containsKey(clazz);
	}

	public static boolean isRegistered(String name) {
		return usedNames.containsKey(name);
	}

	/**
	 * 등록된 능력들의 이름을 String List로 반환합니다. AbilityManifest가 존재하지 않는 능력은 포함되지 않습니다.
	 */
	public static List<String> nameValues() {
		return new ArrayList<>(usedNames.keySet());
	}

	public static List<AbilityRegistration> getRegistrations() {
		return new ArrayList<>(registeredAbilities.values());
	}

	/**
	 * 등록된 능력 중 해당 이름의 능력을 반환합니다. AbilityManifest가 존재하지 않는 능력이거나 존재하지 않는 능력일 경우
	 * null을 반환할 수 있습니다.
	 *
	 * @param name 능력의 이름
	 * @return 능력 Class
	 */
	public static AbilityRegistration getByName(String name) {
		return usedNames.get(name);
	}

	public static class AbilityRegistration {

		private static final ImmutableSet<Material> DEFAULT_MATERIALS = ImmutableSet.of(Material.IRON_INGOT);

		private final Class<? extends AbilityBase> clazz;
		private final Provider provider;
		private final Constructor<? extends AbilityBase> constructor;
		private final AbilityManifest manifest;
		private final Tip tip;
		private final Multimap<Class<? extends Event>, Pair<Method, SubscribeEvent>> eventhandlers;
		private final Map<String, SettingObject<?>> settingObjects;
		private final ImmutableSet<Material> materials;
		private final ImmutableSet<Class<? extends AbstractGame>> notAvailable;
		private final Explanation explain;
		private final int flag;


		private AbilityRegistration(Class<? extends AbilityBase> clazz) throws NullPointerException, NoSuchMethodException, SecurityException, IllegalAccessException, VersionNotSupportedException, ServerNotSupportedException {
			if (clazz.isAnnotationPresent(Support.Version.class)) {
				final Support.Version supportedVersion = clazz.getAnnotation(Support.Version.class);
				if (!(ServerVersion.isAboveOrEqual(supportedVersion.min()) && ServerVersion.isBelowOrEqual(supportedVersion.max()))) {
					throw new VersionNotSupportedException();
				}
			}
			if (clazz.isAnnotationPresent(Support.Server.class)) {
				final ServerType[] supportedServers = clazz.getAnnotation(Support.Server.class).value();
				if (!Arrays.asList(supportedServers).contains(ServerType.getServerType())) {
					throw new ServerNotSupportedException(supportedServers);
				}
			}
			this.clazz = clazz;
			this.constructor = clazz.getConstructor(Participant.class);

			if (!clazz.isAnnotationPresent(AbilityManifest.class))
				throw new IllegalArgumentException("AbilityManfiest가 없는 능력입니다.");
			this.manifest = clazz.getAnnotation(AbilityManifest.class);
			Preconditions.checkNotNull(manifest.name());
			Preconditions.checkNotNull(manifest.rank());
			Preconditions.checkNotNull(manifest.species());

			final Tips tips = clazz.getAnnotation(Tips.class);
			this.tip = tips != null ? new Tip(tips) : null;

			if (clazz.getClassLoader() instanceof AddonClassLoader) {
				this.provider = ((AddonClassLoader) clazz.getClassLoader()).getAddon();
			} else {
				Provider provider;
				try {
					final JavaPlugin javaPlugin = JavaPlugin.getProvidingPlugin(clazz);
					if (javaPlugin instanceof AbilityWar) {
						provider = AbilityWar.getPlugin();
					} else {
						provider = new Provider() {
							@Override
							public JavaPlugin getInstance() {
								return javaPlugin;
							}

							@Override
							public String getName() {
								return javaPlugin.getName();
							}
						};
					}
				} catch (IllegalArgumentException e) {
					provider = AbilityWar.getPlugin();
				}
				this.provider = provider;
			}

			{
				final Multimap<Class<? extends Event>, Pair<Method, SubscribeEvent>> eventhandlers = HashMultimap.create();
				Class<?> current = clazz;
				while (current != null && AbilityBase.class.isAssignableFrom(current) && current != AbilityBase.class) {
					for (Method method : current.getDeclaredMethods()) {
						final SubscribeEvent subscribeEvent = method.getAnnotation(SubscribeEvent.class);
						if (subscribeEvent != null) {
							final Class<?>[] parameters = method.getParameterTypes();
							if (parameters.length == 1 && Event.class.isAssignableFrom(parameters[0])) {
								eventhandlers.put(parameters[0].asSubclass(Event.class), Pair.of(method, subscribeEvent));
							}
						}
					}
					current = current.getSuperclass();
				}
				this.eventhandlers = Multimaps.unmodifiableMultimap(eventhandlers);
			}

			{
				final Map<String, SettingObject<?>> settingObjects = new HashMap<>();
				Class<?> current = clazz;
				while (current != null && AbilityBase.class.isAssignableFrom(current) && current != AbilityBase.class) {
					for (Field field : current.getDeclaredFields()) {
						if (Modifier.isStatic(field.getModifiers())) {
							if (SettingObject.class.isAssignableFrom(field.getType())) {
								SettingObject<?> settingObject = (SettingObject<?>) ReflectionUtil.setAccessible(field).get(null);
								settingObjects.put(settingObject.getKey(), settingObject);
							}
						}
					}
					current = current.getSuperclass();
				}
				this.settingObjects = Collections.unmodifiableMap(settingObjects);
			}

			final Materials materials = clazz.getAnnotation(Materials.class);
			this.materials = materials != null ? ImmutableSet.<Material>builder().add(materials.materials()).build() : DEFAULT_MATERIALS;

			final NotAvailable notAvailable = clazz.getAnnotation(NotAvailable.class);
			this.notAvailable = notAvailable != null ? ImmutableSet.<Class<? extends AbstractGame>>builder().add(notAvailable.value()).build() : ImmutableSet.of();

			this.explain = new Explanation();

			int flag = 0x0;
			if (ActiveHandler.class.isAssignableFrom(clazz)) flag |= Flag.ACTIVE_SKILL;
			if (TargetHandler.class.isAssignableFrom(clazz)) flag |= Flag.TARGET_SKILL;
			if (clazz.isAnnotationPresent(Beta.class)) flag |= Flag.BETA;
			this.flag = flag;
		}

		public Class<? extends AbilityBase> getAbilityClass() {
			return clazz;
		}

		public Provider getProvider() {
			return provider;
		}

		public Constructor<? extends AbilityBase> getConstructor() {
			return constructor;
		}

		public AbilityManifest getManifest() {
			return manifest;
		}

		@Nullable
		public Tip getTip() {
			return tip;
		}

		public Multimap<Class<? extends Event>, Pair<Method, SubscribeEvent>> getEventhandlers() {
			return eventhandlers;
		}

		public Map<String, SettingObject<?>> getSettingObjects() {
			return settingObjects;
		}

		public ImmutableSet<Material> getMaterials() {
			return materials;
		}

		public boolean isAvailable(final Class<? extends AbstractGame> game) {
			for (final Class<? extends AbstractGame> clazz : notAvailable) {
				if (clazz.isAssignableFrom(game)) {
					return false;
				}
			}
			return true;
		}

		public Explanation getExplain() {
			return explain;
		}

		public boolean hasFlag(int flag) {
			return (this.flag & flag) == flag;
		}

		public static class Flag {
			public static final int ACTIVE_SKILL = 0x1;
			public static final int BETA = 0x4;
			public static final int TARGET_SKILL = 0x2;
		}

		public class Explanation {

			private final boolean[] needsReplace;

			private Explanation() {
				this.needsReplace = new boolean[manifest.explain().length];
				final String[] explain = manifest.explain();
				for (int i = 0; i < explain.length; i++) {
					needsReplace[i] = explain[i].contains("$");
				}
			}

			public boolean needsReplace(final int index) {
				return needsReplace[index];
			}

		}

		public static class Tip {

			public final ImmutableList<String> tips;
			public final ImmutableSet<Description> strong, weak;
			public final Stats stats;
			public final Difficulty difficulty;

			private Tip(final Tips tips) {
				this.tips = ImmutableList.copyOf(tips.tip());
				{
					final ImmutableSet.Builder<Description> strong = ImmutableSet.builder();
					for (final Tips.Description description : tips.strong()) {
						strong.add(new Description(description));
					}
					this.strong = strong.build();
				}
				{
					final ImmutableSet.Builder<Description> weak = ImmutableSet.builder();
					for (final Tips.Description description : tips.weak()) {
						weak.add(new Description(description));
					}
					this.weak = weak.build();
				}
				this.stats = tips.stats();
				this.difficulty = tips.difficulty();
			}

			public static class Description {

				public final String subject;
				public final ImmutableList<String> explain;
				public final MaterialX icon;

				private Description(final Tips.Description description) {
					this.subject = description.subject();
					this.explain = ImmutableList.copyOf(description.explain());
					this.icon = description.icon();
				}

			}

		}

	}

}
