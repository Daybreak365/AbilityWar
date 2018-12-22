package Marlang.AbilityWar.Ability;

import Marlang.AbilityWar.Ability.List.Assassin;
import Marlang.AbilityWar.Ability.List.Feather;

public enum AbilityList {
	
	Assassin(new Assassin()),
	Feather(new Feather());
	
	AbilityBase Ability;
	
	private AbilityList(AbilityBase Ability) {
		this.Ability = Ability;
	}
	
	public AbilityBase getAbility() {
		return Ability;
	}
	
}
