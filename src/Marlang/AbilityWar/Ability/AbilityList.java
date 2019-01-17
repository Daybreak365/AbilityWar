package Marlang.AbilityWar.Ability;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;

import Marlang.AbilityWar.Ability.List.Ares;
import Marlang.AbilityWar.Ability.List.Assassin;
import Marlang.AbilityWar.Ability.List.Demigod;
import Marlang.AbilityWar.Ability.List.DiceGod;
import Marlang.AbilityWar.Ability.List.EnergyBlocker;
import Marlang.AbilityWar.Ability.List.FastRegeneration;
import Marlang.AbilityWar.Ability.List.Feather;
import Marlang.AbilityWar.Ability.List.Zeus;
import Marlang.AbilityWar.Utils.Messager;

public class AbilityList {
	
	private static HashMap<String, Class<? extends AbilityBase>> Abilities = new HashMap<String, Class<? extends AbilityBase>>();
	
	/**
	 * 능력 등록
	 */
	public static void registerAbility(String name, Class<? extends AbilityBase> Ability) {
		if(!Abilities.containsKey(name)) {
			Abilities.put(name, Ability);
			
			try {
				Class.forName(Ability.getName());
			} catch(Exception e) {
				Messager.sendErrorMessage(ChatColor.translateAlternateColorCodes('&', "&e" + name + " &f능력을 불러오지 못하였습니다."));
			}
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
