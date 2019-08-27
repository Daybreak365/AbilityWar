package DayBreak.AbilityWar.Ability;

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

import DayBreak.AbilityWar.Ability.AbilityManifest.Rank;
import DayBreak.AbilityWar.Ability.AbilityManifest.Species;
import DayBreak.AbilityWar.Ability.List.Ares;
import DayBreak.AbilityWar.Ability.List.Assassin;
import DayBreak.AbilityWar.Ability.List.Berserker;
import DayBreak.AbilityWar.Ability.List.BlackCandle;
import DayBreak.AbilityWar.Ability.List.BombArrow;
import DayBreak.AbilityWar.Ability.List.Brewer;
import DayBreak.AbilityWar.Ability.List.Celebrity;
import DayBreak.AbilityWar.Ability.List.Chaos;
import DayBreak.AbilityWar.Ability.List.Chaser;
import DayBreak.AbilityWar.Ability.List.Clown;
import DayBreak.AbilityWar.Ability.List.Curse;
import DayBreak.AbilityWar.Ability.List.DarkVision;
import DayBreak.AbilityWar.Ability.List.Demigod;
import DayBreak.AbilityWar.Ability.List.DevilBoots;
import DayBreak.AbilityWar.Ability.List.DiceGod;
import DayBreak.AbilityWar.Ability.List.EnergyBlocker;
import DayBreak.AbilityWar.Ability.List.ExpertOfFall;
import DayBreak.AbilityWar.Ability.List.FastRegeneration;
import DayBreak.AbilityWar.Ability.List.Feather;
import DayBreak.AbilityWar.Ability.List.FireFightWithFire;
import DayBreak.AbilityWar.Ability.List.Flora;
import DayBreak.AbilityWar.Ability.List.Gladiator;
import DayBreak.AbilityWar.Ability.List.Hacker;
import DayBreak.AbilityWar.Ability.List.Hermit;
import DayBreak.AbilityWar.Ability.List.HigherBeing;
import DayBreak.AbilityWar.Ability.List.Imprison;
import DayBreak.AbilityWar.Ability.List.Ira;
import DayBreak.AbilityWar.Ability.List.JellyFish;
import DayBreak.AbilityWar.Ability.List.Khazhad;
import DayBreak.AbilityWar.Ability.List.Muse;
import DayBreak.AbilityWar.Ability.List.Nex;
import DayBreak.AbilityWar.Ability.List.OnlyOddNumber;
import DayBreak.AbilityWar.Ability.List.Pumpkin;
import DayBreak.AbilityWar.Ability.List.ShowmanShip;
import DayBreak.AbilityWar.Ability.List.Sniper;
import DayBreak.AbilityWar.Ability.List.SuperNova;
import DayBreak.AbilityWar.Ability.List.Terrorist;
import DayBreak.AbilityWar.Ability.List.TheEmperor;
import DayBreak.AbilityWar.Ability.List.TheEmpress;
import DayBreak.AbilityWar.Ability.List.TheHighPriestess;
import DayBreak.AbilityWar.Ability.List.TheMagician;
import DayBreak.AbilityWar.Ability.List.TimeRewind;
import DayBreak.AbilityWar.Ability.List.Virtus;
import DayBreak.AbilityWar.Ability.List.Virus;
import DayBreak.AbilityWar.Ability.List.Void;
import DayBreak.AbilityWar.Ability.List.Yeti;
import DayBreak.AbilityWar.Ability.List.Zeus;
import DayBreak.AbilityWar.Ability.List.Zombie;
import DayBreak.AbilityWar.Config.AbilitySettings.SettingObject;
import DayBreak.AbilityWar.Game.Games.SquirtGunFight.SquirtGun;
import DayBreak.AbilityWar.Game.Manager.AbilityList;
import DayBreak.AbilityWar.Utils.Messager;
import DayBreak.AbilityWar.Utils.Thread.TimerBase;

/**
 * {@link AbilityBase}를 기반으로 하는 모든 능력을 관리하는 클래스입니다.
 */
public class AbilityFactory {

	private static Map<Class<? extends AbilityBase>, AbilityRegisteration<? extends AbilityBase>> RegisteredAbilities = new HashMap<>();
	
	/**
	 * 능력을 등록합니다.
	 * 
	 * 능력을 등록하기 전, AbilityManifest 어노테이션이 클래스에 존재하는지,
	 * 겹치는 이름은 없는지, 생성자는 올바른지 확인해주시길 바랍니다.
	 * 
	 * 이미 등록된 능력일 경우 다시 등록이 되지 않습니다.
	 * @param Ability		능력 클래스
	 */
	public static void registerAbility(Class<? extends AbilityBase> abilityClass) {
		if(!RegisteredAbilities.containsKey(abilityClass)) {
			try {
				AbilityRegisteration<?> registeration = new AbilityRegisteration<>(abilityClass);
				if(!containsName(registeration.getManifest().Name())) {
					RegisteredAbilities.put(abilityClass, registeration);

					for(Field field : abilityClass.getFields()) {
						if(field.getType().equals(SettingObject.class) && Modifier.isStatic(field.getModifiers())) {
							field.get(null);
						}
					}
				} else {
					Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e" + abilityClass.getName() + " &f능력은 겹치는 이름이 있어 등록되지 않았습니다."));
				}
			} catch(Exception ex) {
				if(ex.getMessage() != null && !ex.getMessage().isEmpty()) {
					Messager.sendErrorMessage(ex.getMessage());
				} else {
					Messager.sendErrorMessage(ChatColor.translateAlternateColorCodes('&', "&e" + abilityClass.getName() + " &f능력 등록중 오류가 발생하였습니다."));
				}
			}
		}
	}
	
	public static AbilityRegisteration<?> getRegisteration(Class<? extends AbilityBase> clazz) {
		return RegisteredAbilities.get(clazz);
	}
	
	public static boolean isRegistered(Class<? extends AbilityBase> clazz) {
		return RegisteredAbilities.containsKey(clazz);
	}
	
	private static boolean containsName(String name) {
		for(AbilityRegisteration<?> r : RegisteredAbilities.values()) {
			AbilityManifest manifest = r.getManifest();
			if(manifest.Name().equalsIgnoreCase(name)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 플러그인 기본 능력 등록
	 */
	static {
		//초창기 능력자
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
		
		//2019 여름 업데이트
		registerAbility(Khazhad.class);
		registerAbility(Sniper.class);
		registerAbility(JellyFish.class);
		
		//즐거운 여름휴가 게임모드
		registerAbility(SquirtGun.class);
	}

	/**
	 * 등록된 능력들의 이름을 String List로 반환합니다.
	 * AbilityManifest가 존재하지 않는 능력은 포함되지 않습니다.
	 */
	public static List<String> nameValues() {
		ArrayList<String> Values = new ArrayList<String>();

		for(AbilityRegisteration<?> r : RegisteredAbilities.values()) {
			AbilityManifest manifest = r.getManifest();
			Values.add(manifest.Name());
		}
		
		return Values;
	}

	/**
	 * 등록된 능력 중 해당 이름의 능력을 반환합니다.
	 * AbilityManifest가 존재하지 않는 능력이거나 존재하지 않는 능력일 경우 null을 반환합니다.
	 * @param name	능력의 이름
	 * @return		능력 Class
	 */
	public static Class<? extends AbilityBase> getByString(String name) {
		for(AbilityRegisteration<?> r : RegisteredAbilities.values()) {
			AbilityManifest manifest = r.getManifest();
			if(manifest.Name().equalsIgnoreCase(name)) {
				return r.getClazz();
			}
		}
		
		return null;
	}

	public static List<String> getAbilityNames(Rank r) {
		List<String> list = new ArrayList<String>();
		
		for(String name : AbilityList.nameValues()) {
			Class<? extends AbilityBase> clazz = AbilityList.getByString(name);
			AbilityManifest manifest = clazz.getAnnotation(AbilityManifest.class);
			if(manifest != null) {
				if(manifest.Rank().equals(r)) {
					list.add(name);
				}
			}
		}
		
		return list;
	}

	public static List<String> getAbilityNames(Species s) {
		List<String> list = new ArrayList<String>();
		
		for(String name : AbilityList.nameValues()) {
			Class<? extends AbilityBase> clazz = AbilityList.getByString(name);
			AbilityManifest manifest = clazz.getAnnotation(AbilityManifest.class);
			if(manifest != null) {
				if(manifest.Species().equals(s)) {
					list.add(name);
				}
			}
		}
		
		return list;
	}
	
	public static class AbilityRegisteration<T extends AbilityBase> {
		
		private final Class<T> clazz;
		private final AbilityManifest manifest;
		private final List<Field> timers;
		private final Map<Class<? extends Event>, Method> eventhandlers;
		
		@SuppressWarnings("unchecked")
		private AbilityRegisteration(Class<T> clazz) {
			this.clazz = clazz;

			if(!clazz.isAnnotationPresent(AbilityManifest.class)) throw new IllegalArgumentException();
			this.manifest = clazz.getAnnotation(AbilityManifest.class);
			
			List<Field> timers = new ArrayList<>();
			for(Field field : clazz.getDeclaredFields()) {
				Class<?> type = field.getType();
				Class<?> superClass = type.getSuperclass();
				if(type.equals(TimerBase.class) ||(superClass != null && superClass.equals(TimerBase.class))) {
					timers.add(field);
				}
			}
			this.timers = Collections.unmodifiableList(timers);
			
			Map<Class<? extends Event>, Method> eventhandlers = new HashMap<>();
			for(Method method : clazz.getDeclaredMethods()) {
				if(method.isAnnotationPresent(SubscribeEvent.class)) {
					Class<?>[] parameters = method.getParameterTypes();
					if(parameters.length == 1 && Event.class.isAssignableFrom(parameters[0])) {
						Class<? extends Event> eventClass = (Class<? extends Event>) parameters[0];
						eventhandlers.put(eventClass, method);
					}
				}
			}
			this.eventhandlers = Collections.unmodifiableMap(eventhandlers);
		}

		public Class<T> getClazz() {
			return clazz;
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
