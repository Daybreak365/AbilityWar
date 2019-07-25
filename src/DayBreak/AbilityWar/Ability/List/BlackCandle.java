package DayBreak.AbilityWar.Ability.List;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;

import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Ability.AbilityManifest;
import DayBreak.AbilityWar.Ability.AbilityManifest.Rank;
import DayBreak.AbilityWar.Ability.AbilityManifest.Species;
import DayBreak.AbilityWar.Config.AbilitySettings.SettingObject;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame.Participant;
import DayBreak.AbilityWar.Utils.Library.EffectLib;
import DayBreak.AbilityWar.Utils.Thread.TimerBase;

@AbilityManifest(Name = "검은 양초", Rank = Rank.A, Species = Species.OTHERS)
public class BlackCandle extends AbilityBase {

	public static SettingObject<Integer> ChanceConfig = new SettingObject<Integer>(BlackCandle.class, "Chance", 35,
			"# 데미지를 받았을 시 체력을 회복할 확률") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1 && value <= 100;
		}
		
	};

	public BlackCandle(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f디버프를 받지 않으며, 데미지를 받으면 " + ChanceConfig.getValue() + "% 확률로 체력 1.5칸을 회복합니다."));
	}

	TimerBase NoDebuff = new TimerBase() {
		
		@Override
		public void onStart() {}
		
		@Override
		public void TimerProcess(Integer Seconds) {
			EffectLib.BLINDNESS.removePotionEffect(getPlayer());
			EffectLib.CONFUSION.removePotionEffect(getPlayer());
			EffectLib.GLOWING.removePotionEffect(getPlayer());
			EffectLib.HARM.removePotionEffect(getPlayer());
			EffectLib.HUNGER.removePotionEffect(getPlayer());
			EffectLib.POISON.removePotionEffect(getPlayer());
			EffectLib.SLOW.removePotionEffect(getPlayer());
			EffectLib.SLOW_DIGGING.removePotionEffect(getPlayer());
			EffectLib.UNLUCK.removePotionEffect(getPlayer());
			EffectLib.WEAKNESS.removePotionEffect(getPlayer());
			EffectLib.WITHER.removePotionEffect(getPlayer());
		}
		
		@Override
		public void onEnd() {}
		
	}.setPeriod(5);
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
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

	@Override
	public void TargetSkill(MaterialType mt, Entity entity) {}
	
}
