package Marlang.AbilityWar.Ability;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;

import Marlang.AbilityWar.Ability.List.Ares;
import Marlang.AbilityWar.Ability.List.Assassin;
import Marlang.AbilityWar.Ability.List.Berserker;
import Marlang.AbilityWar.Ability.List.BlackCandle;
import Marlang.AbilityWar.Ability.List.BombArrow;
import Marlang.AbilityWar.Ability.List.Brewer;
import Marlang.AbilityWar.Ability.List.Celebrity;
import Marlang.AbilityWar.Ability.List.Chaos;
import Marlang.AbilityWar.Ability.List.Chaser;
import Marlang.AbilityWar.Ability.List.Clown;
import Marlang.AbilityWar.Ability.List.Curse;
import Marlang.AbilityWar.Ability.List.DarkVision;
import Marlang.AbilityWar.Ability.List.Demigod;
import Marlang.AbilityWar.Ability.List.DevilBoots;
import Marlang.AbilityWar.Ability.List.DiceGod;
import Marlang.AbilityWar.Ability.List.EnergyBlocker;
import Marlang.AbilityWar.Ability.List.ExpertOfFall;
import Marlang.AbilityWar.Ability.List.FastRegeneration;
import Marlang.AbilityWar.Ability.List.Feather;
import Marlang.AbilityWar.Ability.List.FireFightWithFire;
import Marlang.AbilityWar.Ability.List.Flora;
import Marlang.AbilityWar.Ability.List.Gladiator;
import Marlang.AbilityWar.Ability.List.Hacker;
import Marlang.AbilityWar.Ability.List.Hermit;
import Marlang.AbilityWar.Ability.List.HigherBeing;
import Marlang.AbilityWar.Ability.List.Imprison;
import Marlang.AbilityWar.Ability.List.Ira;
import Marlang.AbilityWar.Ability.List.Muse;
import Marlang.AbilityWar.Ability.List.Nex;
import Marlang.AbilityWar.Ability.List.OnlyOddNumber;
import Marlang.AbilityWar.Ability.List.Pumpkin;
import Marlang.AbilityWar.Ability.List.ShowmanShip;
import Marlang.AbilityWar.Ability.List.SuperNova;
import Marlang.AbilityWar.Ability.List.Terrorist;
import Marlang.AbilityWar.Ability.List.TheEmperor;
import Marlang.AbilityWar.Ability.List.TheEmpress;
import Marlang.AbilityWar.Ability.List.TheHighPriestess;
import Marlang.AbilityWar.Ability.List.TheMagician;
import Marlang.AbilityWar.Ability.List.TimeRewind;
import Marlang.AbilityWar.Ability.List.Virtus;
import Marlang.AbilityWar.Ability.List.Virus;
import Marlang.AbilityWar.Ability.List.Void;
import Marlang.AbilityWar.Ability.List.Yeti;
import Marlang.AbilityWar.Ability.List.Zeus;
import Marlang.AbilityWar.Ability.List.Zombie;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.Utils.Messager;

/**
 * 능력자 전쟁 플러그인의 능력 목록을 관리하는 클래스입니다.
 */
public class AbilityList {
	
	private static ArrayList<Class<? extends AbilityBase>> Abilities = new ArrayList<>();
	
	/**
	 * 능력을 등록합니다.
	 * 
	 * 능력을 등록하기 전, AbilityManifest 어노테이션이 클래스에 존재하는지,
	 * 겹치는 이름은 없는지, 생성자는 올바른지 확인해주시길 바랍니다.
	 * 
	 * 이미 등록된 능력일 경우 다시 등록이 되지 않습니다.
	 * @param Ability		능력 클래스
	 */
	public static void registerAbility(Class<? extends AbilityBase> Ability) {
		if(!Abilities.contains(Ability)) {
			AbilityManifest manifest = Ability.getAnnotation(AbilityManifest.class);
			
			if(manifest != null) {
				if(!containsName(manifest.Name())) {
					Abilities.add(Ability);
					
					try {
						for(Field field : Ability.getFields()) {
							if(field.getType().equals(SettingObject.class) && Modifier.isStatic(field.getModifiers())) {
								field.get(null);
							}
						}
					} catch (IllegalAccessException | IllegalArgumentException e) {
						Messager.sendErrorMessage(ChatColor.translateAlternateColorCodes('&', "&e" + Ability.getName() + " &f능력 등록중 오류가 발생하였습니다."));
					} catch (Exception ex) {
						if(ex.getMessage() != null && !ex.getMessage().isEmpty()) {
							Messager.sendErrorMessage(ex.getMessage());
						} else {
							Messager.sendErrorMessage(ChatColor.translateAlternateColorCodes('&', "&e" + Ability.getName() + " &f능력 등록중 오류가 발생하였습니다."));
						}
					}
				} else {
					Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e" + Ability.getName() + " &f능력은 겹치는 이름이 있어 등록되지 않았습니다."));
				}
			} else {
				Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e" + Ability.getName() + " &f능력은 AbilityManifest 어노테이션이 존재하지 않아 등록되지 않았습니다."));
			}
		}
	}
	
	private static boolean containsName(String name) {
		for(Class<? extends AbilityBase> abilityClass : Abilities) {
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
	}
	
	/**
	 * 등록된 능력들의 이름을 String List로 반환합니다.
	 * AbilityManifest가 존재하지 않는 능력은 포함되지 않습니다.
	 */
	public static List<String> nameValues() {
		ArrayList<String> Values = new ArrayList<String>();
		
		for(Class<? extends AbilityBase> abilityClass : Abilities) {
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
		for(Class<? extends AbilityBase> abilityClass : Abilities) {
			AbilityManifest manifest = abilityClass.getAnnotation(AbilityManifest.class);
			if(manifest != null) {
				if(manifest.Name().equalsIgnoreCase(name)) {
					return abilityClass;
				}
			}
		}
		
		return null;
	}
	
}
