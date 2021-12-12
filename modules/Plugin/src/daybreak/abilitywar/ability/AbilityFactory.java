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
import daybreak.abilitywar.ability.list.Void;
import daybreak.abilitywar.ability.list.*;
import daybreak.abilitywar.ability.list.grapplinghook.GrapplingHook;
import daybreak.abilitywar.ability.list.magnet.Magnet;
import daybreak.abilitywar.ability.list.prophet.Prophet;
import daybreak.abilitywar.addon.AddonClassLoader;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.list.mix.Mix;
import daybreak.abilitywar.game.list.mix.triplemix.TripleMix;
import daybreak.abilitywar.game.list.murdermystery.ability.Detective;
import daybreak.abilitywar.game.list.murdermystery.ability.Innocent;
import daybreak.abilitywar.game.list.murdermystery.ability.Murderer;
import daybreak.abilitywar.game.list.murdermystery.ability.jobs.innocent.Doctor;
import daybreak.abilitywar.game.list.murdermystery.ability.jobs.innocent.Police;
import daybreak.abilitywar.game.list.murdermystery.ability.jobs.murderer.AssassinMurderer;
import daybreak.abilitywar.game.list.murdermystery.ability.jobs.murderer.BlackMurderer;
import daybreak.abilitywar.game.list.murdermystery.ability.jobs.murderer.SniperMurderer;
import daybreak.abilitywar.game.list.summervacation.SquirtGun;
import daybreak.abilitywar.game.list.tnt.ability.TNT;
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
		registerAbility(Assassin.class);
		registerAbility(Feather.class);
		registerAbility(Demigod.class);
		registerAbility(FastRegeneration.class);
		registerAbility(EnergyBlocker.class);
		registerAbility(DiceGod.class);
		registerAbility(Ares.class);
		registerAbility(Zeus.class);
		registerAbility(Berserker.class);
		registerAbility(Zombie.class);
		registerAbility(Terrorist.class);
		registerAbility(Yeti.class);
		registerAbility(Gladiator.class);
		registerAbility(Chaos.class);
		registerAbility(Void.class);
		registerAbility(DarkVision.class);
		registerAbility(HigherBeing.class);
		registerAbility(FireFightWithFire.class);
		registerAbility(Hacker.class);
		registerAbility(Muse.class);
		registerAbility(Stalker.class);
		registerAbility(Flora.class);
		registerAbility(ShowmanShip.class);
		registerAbility(Virtus.class);
		registerAbility(Nex.class);
		registerAbility(Ira.class);
		registerAbility(Magician.class);
		registerAbility(Emperor.class);
		registerAbility(Pumpkin.class);
		registerAbility(Virus.class);
		registerAbility(DevilBoots.class);
		registerAbility(Explosion.class);
		registerAbility(Imprison.class);
		registerAbility(SuperNova.class);
		registerAbility(Celebrity.class);
		registerAbility(ExpertOfFall.class);
		registerAbility(Curse.class);
		registerAbility(TimeRewind.class);
		// 2019 여름 업데이트
		registerAbility(Khazhad.class);
		registerAbility(Sniper.class);
		registerAbility(JellyFish.class);

		registerAbility(Lazyness.class);
		// v2.0.7.7
		registerAbility(Vampire.class);
		registerAbility(PenetrationArrow.class);
		// v2.0.8.8
		registerAbility(SoulEncroach.class);
		registerAbility(Hedgehog.class);
		// v2.0.9.2
		registerAbility(Prophet.class);
		// v2.1.3
		registerAbility(Kidnap.class);
		// v2.1.4.8
		registerAbility(VictoryBySword.class);
		registerAbility(Flector.class);
		// v2.1.5.8
		registerAbility(Ghost.class);
		// v2.1.8.2
		registerAbility(Lunar.class);
		// v2.1.8.8
		registerAbility("daybreak.abilitywar.ability.list.hermit." + ServerVersion.getName() + ".Hermit");
		// v2.1.9.3
		registerAbility(SwordMaster.class);
		// v2.1.9.6
		registerAbility(SurvivalInstinct.class);
		// v2.2.1
		registerAbility(Synchronize.class);
		// v2.2.2
		registerAbility(Ghoul.class);
		// v2.2.3
		registerAbility(Swap.class);
		// v2.2.4
		registerAbility(Lorem.class);
		registerAbility(Reverse.class);
		registerAbility(Themis.class);
		// v2.3.0
		registerAbility(Ferda.class);
		registerAbility(Lux.class);
		registerAbility(Loki.class);
		registerAbility(GrapplingHook.class);
		registerAbility("daybreak.abilitywar.ability.list.scarecrow." + ServerVersion.getName() + ".ScareCrow");
		// v2.3.3
		registerAbility(Solar.class);
		registerAbility(Canis.class);
		registerAbility(Ruber.class);
		// v2.4.2
		//registerAbility("daybreak.abilitywar.ability.list.redbeard." + ServerVersion.getName() + ".RedBeard");
		registerAbility(Liberator.class);
		// v2.4.4
		registerAbility(Magnet.class);
		// v2.4.5
		registerAbility(Bellum.class);
		registerAbility(Eos.class);
		// v2.6.0
		registerAbility(Morpheus.class);
		registerAbility("daybreak.abilitywar.ability.list.clown." + ServerVersion.getName() + ".Clown");

		// v3.0.2
		registerAbility("daybreak.abilitywar.ability.list.soul." + ServerVersion.getName() + ".Soul");

		// 게임모드 전용
		// 즐거운 여름휴가 게임모드
		registerAbility(SquirtGun.class);
		// 믹스 능력자 게임모드
		registerAbility(Mix.class);
		// 트리플 믹스 게임모드
		registerAbility(TripleMix.class);

		// 머더 미스터리 게임모드
		registerAbility(Murderer.class);
		registerAbility(Detective.class);
		registerAbility(Innocent.class);
		registerAbility(Police.class);
		registerAbility(Doctor.class);
		registerAbility(AssassinMurderer.class);
		registerAbility(BlackMurderer.class);
		registerAbility(SniperMurderer.class);

		// 폭탄 돌리기 게임모드
		registerAbility(TNT.class);
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
			registerAbility(Class.forName(className).asSubclass(AbilityBase.class));
		} catch (ClassNotFoundException e) {
			logger.debug("§e" + className + " §f클래스는 존재하지 않습니다.");
		} catch (ClassCastException e) {
			logger.error("§e" + className + " §f클래스는 AbilityBase를 확장하지 않습니다.");
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
