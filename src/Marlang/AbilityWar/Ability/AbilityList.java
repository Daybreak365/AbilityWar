package Marlang.AbilityWar.Ability;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;

import Marlang.AbilityWar.Ability.List.Ares;
import Marlang.AbilityWar.Ability.List.Assassin;
import Marlang.AbilityWar.Ability.List.Berserker;
import Marlang.AbilityWar.Ability.List.BlackCandle;
import Marlang.AbilityWar.Ability.List.Chaos;
import Marlang.AbilityWar.Ability.List.Chaser;
import Marlang.AbilityWar.Ability.List.Clown;
import Marlang.AbilityWar.Ability.List.DarkVision;
import Marlang.AbilityWar.Ability.List.Demigod;
import Marlang.AbilityWar.Ability.List.DiceGod;
import Marlang.AbilityWar.Ability.List.EnergyBlocker;
import Marlang.AbilityWar.Ability.List.FastRegeneration;
import Marlang.AbilityWar.Ability.List.Feather;
import Marlang.AbilityWar.Ability.List.FireFightWithFire;
import Marlang.AbilityWar.Ability.List.Flora;
import Marlang.AbilityWar.Ability.List.Gladiator;
import Marlang.AbilityWar.Ability.List.Hacker;
import Marlang.AbilityWar.Ability.List.HigherBeing;
import Marlang.AbilityWar.Ability.List.Ira;
import Marlang.AbilityWar.Ability.List.Muse;
import Marlang.AbilityWar.Ability.List.Nex;
import Marlang.AbilityWar.Ability.List.OnlyOddNumber;
import Marlang.AbilityWar.Ability.List.Pumpkin;
import Marlang.AbilityWar.Ability.List.ShowmanShip;
import Marlang.AbilityWar.Ability.List.Terrorist;
import Marlang.AbilityWar.Ability.List.TheEmperor;
import Marlang.AbilityWar.Ability.List.TheEmpress;
import Marlang.AbilityWar.Ability.List.TheHighPriestess;
import Marlang.AbilityWar.Ability.List.TheMagician;
import Marlang.AbilityWar.Ability.List.Virtus;
import Marlang.AbilityWar.Ability.List.Virus;
import Marlang.AbilityWar.Ability.List.Void;
import Marlang.AbilityWar.Ability.List.Yeti;
import Marlang.AbilityWar.Ability.List.Zeus;
import Marlang.AbilityWar.Ability.List.Zombie;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.Utils.Messager;

public class AbilityList {
	
	private static HashMap<String, Class<? extends AbilityBase>> Abilities = new HashMap<String, Class<? extends AbilityBase>>();
	
	/**
	 * 능력 등록
	 */
	public static void registerAbility(String name, Class<? extends AbilityBase> Ability) {
		if(!Abilities.containsKey(name)) {
			Abilities.put(name, Ability);
		}
		
		try {
			for(Field field : Ability.getFields()) {
				if(Modifier.isStatic(field.getModifiers()) && field.getType().equals(SettingObject.class)) {
					field.get(new Object());
				}
			}
		} catch (IllegalAccessException | IllegalArgumentException e) {
			Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e" + Ability.getName() + " &f능력 등록중 오류가 발생하였습니다."));
		}
	}
	
	/**
	 * 플러그인 기본 능력 등록
	 */
	static {
		registerAbility("암살자", Assassin.class);
		registerAbility("깃털", Feather.class);
		registerAbility("데미갓", Demigod.class);
		registerAbility("빠른 회복", FastRegeneration.class);
		registerAbility("에너지 블로커", EnergyBlocker.class);
		registerAbility("다이스 갓", DiceGod.class);
		registerAbility("아레스", Ares.class);
		registerAbility("제우스", Zeus.class);
		registerAbility("버서커", Berserker.class);
		registerAbility("좀비", Zombie.class);
		registerAbility("테러리스트", Terrorist.class);
		registerAbility("설인", Yeti.class);
		registerAbility("글래디에이터", Gladiator.class);
		registerAbility("카오스", Chaos.class);
		registerAbility("보이드", Void.class);
		registerAbility("심안", DarkVision.class);
		registerAbility("상위존재", HigherBeing.class);
		registerAbility("검은 양초", BlackCandle.class);
		registerAbility("이열치열", FireFightWithFire.class);
		registerAbility("해커", Hacker.class);
		registerAbility("뮤즈", Muse.class);
		registerAbility("추적자", Chaser.class);
		registerAbility("플로라", Flora.class);
		registerAbility("쇼맨쉽", ShowmanShip.class);
		registerAbility("베르투스", Virtus.class);
		registerAbility("넥스", Nex.class);
		registerAbility("이라", Ira.class);
		registerAbility("홀수강박증", OnlyOddNumber.class);
		registerAbility("광대", Clown.class);
		registerAbility("마술사", TheMagician.class);
		registerAbility("교황", TheHighPriestess.class);
		registerAbility("여제", TheEmpress.class);
		registerAbility("황제", TheEmperor.class);
		registerAbility("호박", Pumpkin.class);
		registerAbility("바이러스", Virus.class);
	}
	
	public static ArrayList<String> values() {
		ArrayList<String> Values = new ArrayList<String>();
		
		for(String s : Abilities.keySet()) {
			Values.add(s);
		}
		
		return Values;
	}

	public static Class<? extends AbilityBase> getByString(String name) {
		return Abilities.get(name);
	}
	
}
