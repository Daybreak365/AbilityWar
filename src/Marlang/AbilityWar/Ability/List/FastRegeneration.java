package Marlang.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.Utils.TimerBase;

public class FastRegeneration extends AbilityBase {
	
	public static SettingObject<Integer> RegenSpeedConfig = new SettingObject<Integer>("빠른회복", "RegenSpeed", 20,
			"# 회복 속도를 설정합니다.",
			"# 숫자가 낮을수록 회복이 더욱 빨라집니다.") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}
		
	};
	
	public FastRegeneration() {
		super("빠른 회복", Rank.A, 
				ChatColor.translateAlternateColorCodes('&', "&f다른 능력들에 비해서 더 빠른 속도로 체력을 회복합니다.")
				);
		Passive.setPeriod(RegenSpeedConfig.getValue());
		registerTimer(Passive);
	}
	
	TimerBase Passive = new TimerBase() {
		
		@Override
		public void TimerStart() {}
		
		@Override
		public void TimerProcess(Integer Seconds) {
			if(!isRestricted()) {
				Player p = getPlayer();
				if(!p.isDead()) {
					double MaxHealth = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
					
					if(p.getHealth() < MaxHealth) {
						p.setHealth((int) p.getHealth() + 1);
					}
				}
			}
		}
		
		@Override
		public void TimerEnd() {}
		
	};
	
	@Override
	public boolean ActiveSkill(ActiveMaterialType mt, ActiveClickType ct) {
		return false;
	}
	
	@Override
	public void PassiveSkill(Event event) {}

	@Override
	public void AbilityEvent(EventType type) {
		if(type.equals(EventType.RestrictClear)) {
			Passive.StartTimer();
		}
	}
	
}
