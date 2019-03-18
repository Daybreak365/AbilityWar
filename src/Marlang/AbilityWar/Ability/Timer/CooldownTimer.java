package Marlang.AbilityWar.Ability.Timer;

import java.util.ArrayList;

import org.bukkit.ChatColor;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Library.SoundLib;
import Marlang.AbilityWar.Utils.Library.Packet.ActionbarPacket;
import Marlang.AbilityWar.Utils.Math.NumberUtil;
import Marlang.AbilityWar.Utils.Thread.TimerBase;

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
	
	private final AbilityBase Ability;
	private final Integer Cool;
	private String AbilityName = "";

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
				Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&c" + AbilityName + " 쿨타임 &f" + NumberUtil.parseTimeString(this.getCount())));
			} else {
				Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&c쿨타임 &f" + NumberUtil.parseTimeString(this.getCount())));
			}
		}
		
		return isTimerRunning();
	}
	
	@Override
	public CooldownTimer setPeriod(Integer Period) {
		super.setPeriod(Period);
		return this;
	}
	
	@Override
	public CooldownTimer setProcessDuringGame(boolean bool) {
		super.setProcessDuringGame(bool);
		return this;
	}
	
	@Override
	public void onStart() {}
	
	@Override
	public void TimerProcess(Integer Seconds) {
		ActionbarPacket actionbar;
		if(!AbilityName.isEmpty()) {
			actionbar = new ActionbarPacket(ChatColor.translateAlternateColorCodes('&', "&c" + AbilityName + " 쿨타임 &f: &6" + NumberUtil.parseTimeString(this.getCount())), 0, 25, 0);
			
			if(Seconds == (Cool / 2)) {
				SoundLib.BLOCK_NOTE_BLOCK_HAT.playSound(Ability.getPlayer());
				Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&c" + AbilityName + " 쿨타임 &f" + NumberUtil.parseTimeString(this.getCount())));
			} else if(Seconds <= 5 && Seconds >= 1) {
				SoundLib.BLOCK_NOTE_BLOCK_HAT.playSound(Ability.getPlayer());
				Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&c" + AbilityName + " 쿨타임 &f" + NumberUtil.parseTimeString(this.getCount())));
			}
		} else {
			actionbar = new ActionbarPacket(ChatColor.translateAlternateColorCodes('&', "&c쿨타임 &f: &6" + NumberUtil.parseTimeString(this.getCount())), 0, 25, 0);
			
			if(Seconds == (Cool / 2)) {
				SoundLib.BLOCK_NOTE_BLOCK_HAT.playSound(Ability.getPlayer());
				Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&c쿨타임 &f" + NumberUtil.parseTimeString(this.getCount())));
			} else if(Seconds <= 5 && Seconds >= 1) {
				SoundLib.BLOCK_NOTE_BLOCK_HAT.playSound(Ability.getPlayer());
				Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&c쿨타임 &f" + NumberUtil.parseTimeString(this.getCount())));
			}
		}
		
		actionbar.Send(Ability.getPlayer());
	}
	
	@Override
	public void onEnd() {
		ActionbarPacket actionbar = new ActionbarPacket(ChatColor.translateAlternateColorCodes('&', "&a능력을 다시 사용할 수 있습니다."), 0, 50, 0);
		actionbar.Send(Ability.getPlayer());
		Ability.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&a능력을 다시 사용할 수 있습니다."));
	}
	
}
