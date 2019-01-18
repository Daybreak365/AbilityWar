package Marlang.AbilityWar.Ability.Timer;

import org.bukkit.ChatColor;
import org.bukkit.Sound;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Utils.EffectUtil;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.NumberUtil;
import Marlang.AbilityWar.Utils.TimerBase;

public class CooldownTimer extends TimerBase {
	
	AbilityBase Ability;
	Integer Cool;
	String AbilityName = "";

	public CooldownTimer(AbilityBase Ability, Integer Cool) {
		super(Cool);
		this.Ability = Ability;
		this.Cool = Cool;
	}

	public CooldownTimer(AbilityBase Ability, Integer Cool, String AbilityName) {
		super(Cool);
		this.Ability = Ability;
		this.Cool = Cool;
		this.AbilityName = AbilityName;
	}
	
	public boolean isCooldown() {
		if(isTimerRunning()) {
			if(!AbilityName.isEmpty()) {
				Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&c" + AbilityName + " 쿨타임 &f" + NumberUtil.parseTimeString(this.getTempCount())));
			} else {
				Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&c쿨타임 &f" + NumberUtil.parseTimeString(this.getTempCount())));
			}
		}
		
		return isTimerRunning();
	}
	
	@Override
	public void TimerStart() {
		
	}
	
	@Override
	public void TimerProcess(Integer Seconds) {
		
		boolean showed = false;
		
		if(Seconds == (Cool / 2)) {
			showed = true;
			if(!AbilityName.isEmpty()) {
				Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&c" + AbilityName + " 쿨타임 &f" + NumberUtil.parseTimeString(this.getTempCount())));
			} else {
				Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&c쿨타임 &f" + NumberUtil.parseTimeString(this.getTempCount())));
			}
			EffectUtil.sendSound(Ability.getPlayer(), Sound.BLOCK_NOTE_HAT);
		}
		
		if(Seconds <= 5 && Seconds >= 1) {
			if(!showed) {
				if(!AbilityName.isEmpty()) {
					Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&c" + AbilityName + " 쿨타임 &f" + NumberUtil.parseTimeString(this.getTempCount())));
				} else {
					Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&c쿨타임 &f" + NumberUtil.parseTimeString(this.getTempCount())));
				}
				EffectUtil.sendSound(Ability.getPlayer(), Sound.BLOCK_NOTE_HAT);
			}
		}
	}
	
	@Override
	public void TimerEnd() {
		Ability.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&a능력을 다시 사용할 수 있습니다."));
	}
	
}
