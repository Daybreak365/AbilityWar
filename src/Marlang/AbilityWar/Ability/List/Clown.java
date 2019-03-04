package Marlang.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.Timer.CooldownTimer;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.Config.AbilityWarSettings;
import Marlang.AbilityWar.GameManager.Object.Participant;
import Marlang.AbilityWar.Utils.Messager;

public class Clown extends AbilityBase {

	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>("±¤´ë", "Cooldown", 3, 
			"# ÄðÅ¸ÀÓ") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};

	public Clown(Participant participant) {
		super(participant, "±¤´ë", Rank.D,
				ChatColor.translateAlternateColorCodes('&', "&fÃ¶±«¸¦ ¿ìÅ¬¸¯ÇÏ¸é ½ºÆùÀ¸·Î ÀÌµ¿ÇÕ´Ï´Ù. " + Messager.formatCooldown(CooldownConfig.getValue())));
	}

	CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		if(mt.equals(MaterialType.Iron_Ingot)) {
			if(ct.equals(ClickType.RightClick)) {
				if(!Cool.isCooldown()) {
					Location Spawn = AbilityWarSettings.getSpawnLocation();
					
					getPlayer().teleport(Spawn);
					
					Cool.StartTimer();
					
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public void PassiveSkill(Event event) {}

	@Override
	public void onRestrictClear() {}

	@Override
	public void TargetSkill(MaterialType mt, Entity entity) {}
	
}
