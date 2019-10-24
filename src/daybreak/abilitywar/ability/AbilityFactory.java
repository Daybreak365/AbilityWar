package daybreak.abilitywar.ability;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.event.Event;

import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.list.Ares;
import daybreak.abilitywar.ability.list.Assassin;
import daybreak.abilitywar.ability.list.Berserker;
import daybreak.abilitywar.ability.list.BlackCandle;
import daybreak.abilitywar.ability.list.BombArrow;
import daybreak.abilitywar.ability.list.Brewer;
import daybreak.abilitywar.ability.list.Celebrity;
import daybreak.abilitywar.ability.list.Chaos;
import daybreak.abilitywar.ability.list.Chaser;
import daybreak.abilitywar.ability.list.Clown;
import daybreak.abilitywar.ability.list.Curse;
import daybreak.abilitywar.ability.list.DarkVision;
import daybreak.abilitywar.ability.list.Demigod;
import daybreak.abilitywar.ability.list.DevilBoots;
import daybreak.abilitywar.ability.list.DiceGod;
import daybreak.abilitywar.ability.list.EnergyBlocker;
import daybreak.abilitywar.ability.list.ExpertOfFall;
import daybreak.abilitywar.ability.list.FastRegeneration;
import daybreak.abilitywar.ability.list.Feather;
import daybreak.abilitywar.ability.list.FireFightWithFire;
import daybreak.abilitywar.ability.list.Flora;
import daybreak.abilitywar.ability.list.Gladiator;
import daybreak.abilitywar.ability.list.Hacker;
import daybreak.abilitywar.ability.list.Hermit;
import daybreak.abilitywar.ability.list.HigherBeing;
import daybreak.abilitywar.ability.list.Imprison;
import daybreak.abilitywar.ability.list.Ira;
import daybreak.abilitywar.ability.list.JellyFish;
import daybreak.abilitywar.ability.list.Khazhad;
import daybreak.abilitywar.ability.list.Muse;
import daybreak.abilitywar.ability.list.Nex;
import daybreak.abilitywar.ability.list.OnlyOddNumber;
import daybreak.abilitywar.ability.list.Pumpkin;
import daybreak.abilitywar.ability.list.ShowmanShip;
import daybreak.abilitywar.ability.list.Sniper;
import daybreak.abilitywar.ability.list.SuperNova;
import daybreak.abilitywar.ability.list.Terrorist;
import daybreak.abilitywar.ability.list.TheEmperor;
import daybreak.abilitywar.ability.list.TheEmpress;
import daybreak.abilitywar.ability.list.TheHighPriestess;
import daybreak.abilitywar.ability.list.TheMagician;
import daybreak.abilitywar.ability.list.TimeRewind;
import daybreak.abilitywar.ability.list.Virtus;
import daybreak.abilitywar.ability.list.Virus;
import daybreak.abilitywar.ability.list.Void;
import daybreak.abilitywar.ability.list.Yeti;
import daybreak.abilitywar.ability.list.Zeus;
import daybreak.abilitywar.ability.list.Zombie;
import daybreak.abilitywar.config.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.game.games.squirtgunfight.SquirtGun;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.thread.TimerBase;

/**
 * {@link AbilityBase}를 기반으로 하는 모든 능력을 관리하는 클래스입니다.
 */
public class AbilityFactory {

	private static final Map<Class<? extends AbilityBase>, AbilityRegisteration<? extends AbilityBase>> registeredAbilities = new HashMap<>();

	/**
	 * 능력을 등록합니다.
	 * 
	 * 능력을 등록하기 전, AbilityManifest 어노테이션이 클래스에 존재하는지, 겹치는 이름은 없는지, 생성자는 올바른지 확인해주시길
	 * 바랍니다.
	 * 
	 * 이미 등록된 능력일 경우 다시 등록이 되지 않습니다.
	 * 
	 * @param abilityClass 능력 클래스
	 */
	public static void registerAbility(Class<? extends AbilityBase> abilityClass) {
		if (!registeredAbilities.containsKey(abilityClass)) {
			try {
				AbilityRegisteration<?> registeration = new AbilityRegisteration<>(abilityClass);
				if (!containsName(registeration.getManifest().Name())) {
					registeredAbilities.put(abilityClass, registeration);

					for (Field field : abilityClass.getFields()) {
						if (field.getType().equals(SettingObject.class) && Modifier.isStatic(field.getModifiers())) {
							field.get(null);
						}
					}
				} else {
					Messager.sendConsoleErrorMessage(ChatColor.translateAlternateColorCodes('&',
							"&e" + abilityClass.getName() + " &f능력은 겹치는 이름이 있어 등록되지 않았습니다."));
				}
			} catch (Exception ex) {
				if (ex.getMessage() != null && !ex.getMessage().isEmpty()) {
					Messager.sendConsoleErrorMessage(ex.getMessage());
				} else {
					Messager.sendConsoleErrorMessage(ChatColor.translateAlternateColorCodes('&',
							"&e" + abilityClass.getName() + " &f능력 등록중 오류가 발생하였습니다."));
				}
			}
		}
	}

	public static AbilityRegisteration<?> getRegisteration(Class<? extends AbilityBase> clazz) {
		return registeredAbilities.get(clazz);
	}

	public static boolean isRegistered(Class<? extends AbilityBase> clazz) {
		return registeredAbilities.containsKey(clazz);
	}

	private static boolean containsName(String name) {
		for (AbilityRegisteration<?> r : registeredAbilities.values()) {
			AbilityManifest manifest = r.getManifest();
			if (manifest.Name().equalsIgnoreCase(name)) {
				return true;
			}
		}

		return false;
	}

	static {
		// 초창기 능력자
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
		registerAbility(Chaser.class);
		registerAbility(Flora.class);
		registerAbility(ShowmanShip.class);
		registerAbility(Virtus.class);
		registerAbility(Nex.class);
		registerAbility(Ira.class);
		registerAbility(OnlyOddNumber.class);
		registerAbility(Clown.class);
		registerAbility(TheMagician.class);
		registerAbility(TheHighPriestess.class);
		registerAbility(TheEmpress.class);
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

		// 즐거운 여름휴가 게임모드
		registerAbility(SquirtGun.class);
	}

	/**
	 * 등록된 능력들의 이름을 String List로 반환합니다. AbilityManifest가 존재하지 않는 능력은 포함되지 않습니다.
	 */
	public static List<String> nameValues() {
		ArrayList<String> Values = new ArrayList<>();

		for (AbilityRegisteration<?> r : registeredAbilities.values()) {
			AbilityManifest manifest = r.getManifest();
			Values.add(manifest.Name());
		}

		return Values;
	}

	/**
	 * 등록된 능력 중 해당 이름의 능력을 반환합니다. AbilityManifest가 존재하지 않는 능력이거나 존재하지 않는 능력일 경우
	 * null을 반환할 수 있습니다.
	 * 
	 * @param name 능력의 이름
	 * @return 능력 Class
	 */
	public static Class<? extends AbilityBase> getByString(String name) {
		for (AbilityRegisteration<?> r : registeredAbilities.values()) {
			AbilityManifest manifest = r.getManifest();
			if (manifest.Name().equalsIgnoreCase(name)) {
				return r.getAbilityClass();
			}
		}

		return null;
	}

	public static List<String> getAbilityNames(Rank r) {
		List<String> list = new ArrayList<>();

		for (String name : AbilityList.nameValues()) {
			Class<? extends AbilityBase> clazz = AbilityList.getByString(name);
			AbilityManifest manifest = clazz.getAnnotation(AbilityManifest.class);
			if (manifest != null) {
				if (manifest.Rank().equals(r)) {
					list.add(name);
				}
			}
		}

		return list;
	}

	public static List<String> getAbilityNames(Species s) {
		List<String> list = new ArrayList<>();

		for (String name : AbilityList.nameValues()) {
			Class<? extends AbilityBase> clazz = AbilityList.getByString(name);
			AbilityManifest manifest = clazz.getAnnotation(AbilityManifest.class);
			if (manifest != null) {
				if (manifest.Species().equals(s)) {
					list.add(name);
				}
			}
		}

		return list;
	}

	public static class AbilityRegisteration<T extends AbilityBase> {

		private final Class<T> clazz;
		private final Constructor<T> constructor;
		private final AbilityManifest manifest;
		private final List<Field> timers;
		private final Map<Class<? extends Event>, Method> eventhandlers;

		@SuppressWarnings("unchecked")
		private AbilityRegisteration(Class<T> clazz) throws NoSuchMethodException, SecurityException {
			this.clazz = clazz;

			this.constructor = clazz.getConstructor(Participant.class);

			if (!clazz.isAnnotationPresent(AbilityManifest.class))
				throw new IllegalArgumentException("AbilityManfiest가 없는 능력입니다.");
			this.manifest = clazz.getAnnotation(AbilityManifest.class);

			List<Field> timers = new ArrayList<>();
			for (Field field : clazz.getDeclaredFields()) {
				Class<?> type = field.getType();
				Class<?> superClass = type.getSuperclass();
				if (type.equals(TimerBase.class) || (superClass != null && superClass.equals(TimerBase.class))) {
					timers.add(field);
				}
			}
			this.timers = Collections.unmodifiableList(timers);

			Map<Class<? extends Event>, Method> eventhandlers = new HashMap<>();
			for (Method method : clazz.getDeclaredMethods()) {
				if (method.isAnnotationPresent(SubscribeEvent.class)) {
					Class<?>[] parameters = method.getParameterTypes();
					if (parameters.length == 1 && Event.class.isAssignableFrom(parameters[0])) {
						eventhandlers.put((Class<? extends Event>) parameters[0], method);
					}
				}
			}
			this.eventhandlers = Collections.unmodifiableMap(eventhandlers);
		}

		public Class<T> getAbilityClass() {
			return clazz;
		}

		public Constructor<T> getConstructor() {
			return constructor;
		}

		public AbilityManifest getManifest() {
			return manifest;
		}

		public List<Field> getTimers() {
			return timers;
		}

		public Map<Class<? extends Event>, Method> getEventhandlers() {
			return eventhandlers;
		}

	}

}
