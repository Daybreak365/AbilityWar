package Marlang.AbilityWar.Ability.Timer;

import java.util.ArrayList;

import org.bukkit.ChatColor;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.NumberUtil;
import Marlang.AbilityWar.Utils.PacketUtil.ActionbarObject;
import Marlang.AbilityWar.Utils.TimerBase;
import Marlang.AbilityWar.Utils.Library.SoundLib;

public class CooldownTimer extends TimerBase {
	
	/**
	 * 쿨타임 초기화
	 */
	public static void CoolReset() {
		ArrayList<TimerBase> Reset = new ArrayList<TimerBase>();
		
		for(TimerBase timer : TimerBase.getTasks()) {
			if(timer instanceof CooldownTimer) {
				Reset.add(timer);
			}
		}
		
		for(TimerBase timer : Reset) {
			timer.StopTimer(false);
		}
		
	}
	
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
	public CooldownTimer setPeriod(Integer Period) {
		this.Period = Period;
		return this;
	}
	
	@Override
	public CooldownTimer setProcessDuringGame(boolean bool) {
		this.ProcessDuringGame = bool;
		return this;
	}
	
	@Override
	public void TimerStart(Data<?>... args) {}
	
	@Override
	public void TimerProcess(Integer Seconds) {
		ActionbarObject actionbar;
		if(!AbilityName.isEmpty()) {
			actionbar = new ActionbarObject(ChatColor.translateAlternateColorCodes('&', "&c" + AbilityName + " 쿨타임 &f: &6" + NumberUtil.parseTimeString(this.getTempCount())));
			
			if(Seconds == (Cool / 2)) {
				SoundLib.BLOCK_NOTE_HAT.playSound(Ability.getPlayer());
				Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&c" + AbilityName + " 쿨타임 &f" + NumberUtil.parseTimeString(this.getTempCount())));
			} else if(Seconds <= 5 && Seconds >= 1) {
				SoundLib.BLOCK_NOTE_HAT.playSound(Ability.getPlayer());
				Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&c" + AbilityName + " 쿨타임 &f" + NumberUtil.parseTimeString(this.getTempCount())));
			}
		} else {
			actionbar = new ActionbarObject(ChatColor.translateAlternateColorCodes('&', "&c쿨타임 &f: &6" + NumberUtil.parseTimeString(this.getTempCount())));
			
			if(Seconds == (Cool / 2)) {
				SoundLib.BLOCK_NOTE_HAT.playSound(Ability.getPlayer());
				Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&c쿨타임 &f" + NumberUtil.parseTimeString(this.getTempCount())));
			} else if(Seconds <= 5 && Seconds >= 1) {
				SoundLib.BLOCK_NOTE_HAT.playSound(Ability.getPlayer());
				Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&c쿨타임 &f" + NumberUtil.parseTimeString(this.getTempCount())));
			}
		}
		
		actionbar.Send(Ability.getPlayer());
	}
	
	@Override
	public void TimerEnd() {
		ActionbarObject actionbar = new ActionbarObject(ChatColor.translateAlternateColorCodes('&', "&a능력을 다시 사용할 수 있습니다."));
		actionbar.Send(Ability.getPlayer());
		Ability.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&a능력을 다시 사용할 수 있습니다."));
	}
	
}
