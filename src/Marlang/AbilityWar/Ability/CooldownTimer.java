package Marlang.AbilityWar.Ability;

import org.bukkit.ChatColor;
import org.bukkit.Sound;

import Marlang.AbilityWar.Utils.EffectUtil;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.NumberUtil;
import Marlang.AbilityWar.Utils.TimerBase;

public class CooldownTimer extends TimerBase {
	
	AbilityBase Ability;
	Integer Cool;
	
	public CooldownTimer(AbilityBase Ability, Integer Cool) {
		super(Cool);
		this.Ability = Ability;
		this.Cool = Cool;
	}
	
	public boolean isCooldown() {
		if(isTimerRunning()) {
			Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&c쿨타임 &f" + NumberUtil.parseTimeString(this.getTempCount())));
		}
		
		return isTimerRunning();
	}
	
	@Override
	public void TimerStart() {
		
	}
	
	@Override
	public void TimerProcess(Integer Seconds) {

		if(Seconds == (Cool / 2)) {
			Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&c쿨타임 &f" + NumberUtil.parseTimeString(Seconds)));
			EffectUtil.sendSound(Ability.getPlayer(), Sound.BLOCK_NOTE_HAT);
		}
		
		if(Seconds <= 5 && Seconds >= 1) {
			Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&c쿨타임 &f" + NumberUtil.parseTimeString(Seconds)));
			EffectUtil.sendSound(Ability.getPlayer(), Sound.BLOCK_NOTE_HAT);
		}
	}
	
	@Override
	public void TimerEnd() {
		Ability.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&a능력을 다시 사용할 수 있습니다."));
	}
	
}
