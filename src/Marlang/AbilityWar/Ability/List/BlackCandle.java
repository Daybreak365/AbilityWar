package Marlang.AbilityWar.Ability.List;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.Utils.TimerBase;

public class BlackCandle extends AbilityBase {

	public static SettingObject<Integer> ChanceConfig = new SettingObject<Integer>("검은양초", "Chance", 10,
			"# 데미지를 받았을 시 체력을 회복할 확률") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1 && value <= 100;
		}
		
	};

	public BlackCandle(Player player) {
		super(player, "검은 양초", Rank.B,
				ChatColor.translateAlternateColorCodes('&', "&f디버프를 받지 않으며, 데미지를 받으면 " + ChanceConfig.getValue() + "% 확률로 체력 1.5칸을 회복합니다."));
	}

	TimerBase NoDebuff = new TimerBase() {
		
		@Override
		public void TimerStart(Data<?>... args) {}
		
		@Override
		public void TimerProcess(Integer Seconds) {
			getPlayer().removePotionEffect(PotionEffectType.BLINDNESS);
			getPlayer().removePotionEffect(PotionEffectType.CONFUSION);
			getPlayer().removePotionEffect(PotionEffectType.GLOWING);
			getPlayer().removePotionEffect(PotionEffectType.HARM);
			getPlayer().removePotionEffect(PotionEffectType.HUNGER);
			getPlayer().removePotionEffect(PotionEffectType.POISON);
			getPlayer().removePotionEffect(PotionEffectType.SLOW);
			getPlayer().removePotionEffect(PotionEffectType.SLOW_DIGGING);
			getPlayer().removePotionEffect(PotionEffectType.UNLUCK);
			getPlayer().removePotionEffect(PotionEffectType.WEAKNESS);
			getPlayer().removePotionEffect(PotionEffectType.WITHER);
		}
		
		@Override
		public void TimerEnd() {}
		
	}.setPeriod(5);
	
	@Override
	public boolean ActiveSkill(ActiveMaterialType mt, ActiveClickType ct) {
		return false;
	}

	@Override
	public void PassiveSkill(Event event) {
		if(event instanceof EntityDamageEvent) {
			EntityDamageEvent e = (EntityDamageEvent) event;
			if(e.getEntity().equals(getPlayer())) {
				Random r = new Random();
				if(r.nextInt(10) == 0) {
					Double Health = getPlayer().getHealth() + 1.5;
					if(Health > 20) Health = 20.0;
					
					if(!getPlayer().isDead()) {
						getPlayer().setHealth(Health);
					}
				}
			}
		}
	}

	@Override
	public void onRestrictClear() {
		NoDebuff.StartTimer();
	}

}
