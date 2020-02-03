package daybreak.abilitywar.ability;

import daybreak.abilitywar.ability.list.Void;
import daybreak.abilitywar.ability.list.*;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mixability.Mix;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.game.games.mode.AbstractGame.TimerBase;
import daybreak.abilitywar.game.games.squirtgunfight.SquirtGun;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.ReflectionUtil;
import daybreak.abilitywar.utils.base.collect.Pair;
import org.bukkit.ChatColor;
import org.bukkit.event.Event;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@link AbilityBase}를 기반으로 하는 모든 능력을 관리하는 클래스입니다.
 */
public class AbilityFactory {

	private AbilityFactory() {
	}

	private static final HashMap<String, Class<? extends AbilityBase>> usedNames = new HashMap<>();
	private static final HashMap<Class<? extends AbilityBase>, AbilityRegistration> registeredAbilities = new HashMap<>();

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
				AbilityRegistration registeration = new AbilityRegistration(abilityClass);
				String name = registeration.getManifest().Name();
				if (!usedNames.containsKey(name)) {
					registeredAbilities.put(abilityClass, registeration);
					usedNames.put(name, abilityClass);
				} else {
					Messager.sendConsoleErrorMessage(ChatColor.translateAlternateColorCodes('&', "&e" + abilityClass.getName() + " &f능력은 겹치는 이름이 있어 등록되지 않았습니다."));
				}
			} catch (NoSuchMethodException | IllegalAccessException e) {
				if (e.getMessage() != null && !e.getMessage().isEmpty()) {
					Messager.sendConsoleErrorMessage(e.getMessage());
				} else {
					Messager.sendConsoleErrorMessage(ChatColor.translateAlternateColorCodes('&', "&e" + abilityClass.getName() + " &f능력 등록중 오류가 발생하였습니다."));
				}
			}
		}
	}

	public static AbilityRegistration getRegistration(Class<? extends AbilityBase> clazz) {
		return registeredAbilities.get(clazz);
	}

	public static boolean isRegistered(Class<? extends AbilityBase> clazz) {
		return registeredAbilities.containsKey(clazz);
	}

	private static boolean containsName(String name) {
		return usedNames.containsKey(name);
	}

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
		registerAbility(BlackCandle.class);
		registerAbility(FireFightWithFire.class);
		registerAbility(Hacker.class);
		registerAbility(Muse.class);
		registerAbility(Stalker.class);
		registerAbility(Flora.class);
		registerAbility(ShowmanShip.class);
		registerAbility(Virtus.class);
		registerAbility(Nex.class);
		registerAbility(Ira.class);
		registerAbility(OnlyOddNumber.class);
		registerAbility(Clown.class);
		registerAbility(TheMagician.class);
		registerAbility(TheEmperor.class);
		registerAbility(Pumpkin.class);
		registerAbility(Virus.class);
		registerAbility(Hermit.class);
		registerAbility(DevilBoots.class);
		registerAbility(BombArrow.class);
		registerAbility(Brewer.class);
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
		registerAbility(Reaper.class);
		registerAbility(Hedgehog.class);
		// v2.0.9.2
		registerAbility(ReligiousLeader.class);
		// v2.1.3
		registerAbility(Kidnap.class);

		// 게임모드 전용
		// 즐거운 여름휴가 게임모드
		registerAbility(SquirtGun.class);
		// 믹스 능력자 게임모드
		registerAbility(Mix.class);
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
	public static Class<? extends AbilityBase> getByName(String name) {
		return usedNames.get(name);
	}

	public static class AbilityRegistration {

		private final Class<? extends AbilityBase> clazz;
		private final Constructor<? extends AbilityBase> constructor;
		private final AbilityManifest manifest;
		private final Map<Class<? extends Event>, Pair<Method, SubscribeEvent>> eventhandlers;
		private final Map<String, SettingObject<?>> settingObjects;
		private final Set<Field> scheduledTimers;

		@SuppressWarnings("unchecked")
		private AbilityRegistration(Class<? extends AbilityBase> clazz) throws NoSuchMethodException, SecurityException, IllegalAccessException {
			this.clazz = clazz;

			this.constructor = clazz.getConstructor(Participant.class);

			if (!clazz.isAnnotationPresent(AbilityManifest.class))
				throw new IllegalArgumentException("AbilityManfiest가 없는 능력입니다.");
			this.manifest = clazz.getAnnotation(AbilityManifest.class);

			Map<Class<? extends Event>, Pair<Method, SubscribeEvent>> eventhandlers = new HashMap<>();
			for (Method method : clazz.getDeclaredMethods()) {
				SubscribeEvent subscribeEvent = method.getAnnotation(SubscribeEvent.class);
				if (subscribeEvent != null) {
					Class<?>[] parameters = method.getParameterTypes();
					if (parameters.length == 1 && Event.class.isAssignableFrom(parameters[0])) {
						eventhandlers.put((Class<? extends Event>) parameters[0], Pair.of(method, subscribeEvent));
					}
				}
			}
			this.eventhandlers = Collections.unmodifiableMap(eventhandlers);

			Map<String, SettingObject<?>> settingObjects = new HashMap<>();
			Set<Field> scheduledTimers = new HashSet<>();
			for (Field field : clazz.getDeclaredFields()) {
				Class<?> type = field.getType();
				if (type.equals(SettingObject.class)) {
					if (Modifier.isStatic(field.getModifiers())) {
						SettingObject<?> settingObject = (SettingObject<?>) ReflectionUtil.setAccessible(field).get(null);
						settingObjects.put(settingObject.getKey(), settingObject);
					}
				} else if (TimerBase.class.isAssignableFrom(type)) {
					if (!Modifier.isStatic(field.getModifiers()) && field.isAnnotationPresent(Scheduled.class)) {
						scheduledTimers.add(field);
					}
				}
			}
			this.settingObjects = Collections.unmodifiableMap(settingObjects);
			this.scheduledTimers = Collections.unmodifiableSet(scheduledTimers);
		}

		public Class<? extends AbilityBase> getAbilityClass() {
			return clazz;
		}

		public Constructor<? extends AbilityBase> getConstructor() {
			return constructor;
		}

		public AbilityManifest getManifest() {
			return manifest;
		}

		public Map<Class<? extends Event>, Pair<Method, SubscribeEvent>> getEventhandlers() {
			return eventhandlers;
		}

		public Map<String, SettingObject<?>> getSettingObjects() {
			return settingObjects;
		}

		public Set<Field> getScheduledTimers() {
			return scheduledTimers;
		}

	}

}
