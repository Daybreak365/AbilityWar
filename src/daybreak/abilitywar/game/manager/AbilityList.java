package daybreak.abilitywar.game.manager;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import daybreak.abilitywar.game.games.defaultgame.DefaultGame;
import org.bukkit.ChatColor;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
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
import daybreak.abilitywar.game.games.changeability.ChangeAbilityWar;
import daybreak.abilitywar.utils.Messager;

/**
 * {@link DefaultGame}, {@link ChangeAbilityWar} 등에서 사용하는 능력자 플러그인의 기본적인 능력 목록을 관리하는 클래스입니다.
 */
public class AbilityList {

	private static final Messager messager = new Messager();
	private static final ArrayList<Class<? extends AbilityBase>> abilities = new ArrayList<>();
	
	/**
	 * 능력을 등록합니다.
	 * 
	 * 능력을 등록하기 전, AbilityManifest 어노테이션이 클래스에 존재하는지,
	 * 겹치는 이름은 없는지, 생성자는 올바른지 확인해주시길 바랍니다.
	 * 
	 * 이미 등록된 능력일 경우 다시 등록이 되지 않습니다.
	 * @param abilityClass		능력 클래스
	 */
	public static void registerAbility(Class<? extends AbilityBase> abilityClass) {
		if(!abilities.contains(abilityClass)) {
			AbilityManifest manifest = abilityClass.getAnnotation(AbilityManifest.class);
			
			if(manifest != null) {
				if(!containsName(manifest.Name())) {
					abilities.add(abilityClass);
					
					try {
						for(Field field : abilityClass.getFields()) {
							if(field.getType().equals(SettingObject.class) && Modifier.isStatic(field.getModifiers())) {
								field.get(null);
							}
						}
					} catch (Exception ex) {
						if(ex.getMessage() != null && !ex.getMessage().isEmpty()) {
							Messager.sendConsoleErrorMessage(ex.getMessage());
						} else {
							Messager.sendConsoleErrorMessage(ChatColor.translateAlternateColorCodes('&', "&e" + abilityClass.getName() + " &f능력 등록중 오류가 발생하였습니다."));
						}
					}
				} else {
					messager.sendConsoleMessage(ChatColor.translateAlternateColorCodes('&', "&e" + abilityClass.getName() + " &f능력은 겹치는 이름이 있어 등록되지 않았습니다."));
				}
			} else {
				messager.sendConsoleMessage(ChatColor.translateAlternateColorCodes('&', "&e" + abilityClass.getName() + " &f능력은 AbilityManifest 어노테이션이 존재하지 않아 등록되지 않았습니다."));
			}
		}
	}
	
	private static boolean containsName(String name) {
		for(Class<? extends AbilityBase> abilityClass : abilities) {
			AbilityManifest manifest = abilityClass.getAnnotation(AbilityManifest.class);
			if(manifest != null) {
				if(manifest.Name().equalsIgnoreCase(name)) {
					return true;
				}
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
	}

	/**
	 * 등록된 능력들의 이름을 String List로 반환합니다.
	 * AbilityManifest가 존재하지 않는 능력은 포함되지 않습니다.
	 */
	public static List<String> nameValues() {
		ArrayList<String> Values = new ArrayList<String>();
		
		for(Class<? extends AbilityBase> abilityClass : abilities) {
			AbilityManifest manifest = abilityClass.getAnnotation(AbilityManifest.class);
			if(manifest != null) {
				Values.add(manifest.Name());
			}
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
		for(Class<? extends AbilityBase> abilityClass : abilities) {
			AbilityManifest manifest = abilityClass.getAnnotation(AbilityManifest.class);
			if(manifest != null) {
				if(manifest.Name().equalsIgnoreCase(name)) {
					return abilityClass;
				}
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
	
}