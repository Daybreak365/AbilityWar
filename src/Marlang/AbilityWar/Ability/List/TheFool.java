package Marlang.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.Event;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.Timer.CooldownTimer;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.Config.AbilityWarSettings;
import Marlang.AbilityWar.Utils.Messager;

public class TheFool extends AbilityBase {

	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>("TheFool", "Cooldown", 3, 
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};

	public TheFool() {
		super("The Fool", Rank.D,
				ChatColor.translateAlternateColorCodes('&', "철괴를 우클릭하면 스폰으로 이동합니다. " + Messager.formatCooldown(CooldownConfig.getValue())));
	
		registerTimer(Cool);
	}

	CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());
	
	@Override
	public void ActiveSkill(ActiveMaterialType mt, ActiveClickType ct) {
		if(mt.equals(ActiveMaterialType.Iron_Ingot)) {
			if(ct.equals(ActiveClickType.RightClick)) {
				if(!Cool.isCooldown()) {
					Location Spawn = AbilityWarSettings.getSpawnLocation();
					
					getPlayer().teleport(Spawn);
					
					Cool.StartTimer();
				}
			}
		}
	}

	@Override
	public void PassiveSkill(Event event) {}

	@Override
	public void AbilityEvent(EventType type) {}

}
