package Marlang.AbilityWar.Ability.Timer;

import org.bukkit.ChatColor;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.NumberUtil;
import Marlang.AbilityWar.Utils.TimerBase;
import Marlang.AbilityWar.Utils.Library.SoundLib;
import Marlang.AbilityWar.Utils.PacketUtil.ActionbarObject;

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
				Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&c" + AbilityName + " ÄðÅ¸ÀÓ &f" + NumberUtil.parseTimeString(this.getTempCount())));
			} else {
				Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&cÄðÅ¸ÀÓ &f" + NumberUtil.parseTimeString(this.getTempCount())));
			}
		}
		
		return isTimerRunning();
	}
	
	@Override
	public void TimerStart() {
		
	}
	
	@Override
	public void TimerProcess(Integer Seconds) {
		ActionbarObject actionbar;
		if(!AbilityName.isEmpty()) {
			actionbar = new ActionbarObject(ChatColor.translateAlternateColorCodes('&', "&c" + AbilityName + " ÄðÅ¸ÀÓ &f: &6" + NumberUtil.parseTimeString(this.getTempCount())));
			
			if(Seconds == (Cool / 2)) {
				SoundLib.BLOCK_NOTE_HAT.playSound(Ability.getPlayer());
				Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&c" + AbilityName + " ÄðÅ¸ÀÓ &f" + NumberUtil.parseTimeString(this.getTempCount())));
			} else if(Seconds <= 5 && Seconds >= 1) {
				SoundLib.BLOCK_NOTE_HAT.playSound(Ability.getPlayer());
				Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&c" + AbilityName + " ÄðÅ¸ÀÓ &f" + NumberUtil.parseTimeString(this.getTempCount())));
			}
		} else {
			actionbar = new ActionbarObject(ChatColor.translateAlternateColorCodes('&', "&cÄðÅ¸ÀÓ &f: &6" + NumberUtil.parseTimeString(this.getTempCount())));
			
			if(Seconds == (Cool / 2)) {
				SoundLib.BLOCK_NOTE_HAT.playSound(Ability.getPlayer());
				Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&cÄðÅ¸ÀÓ &f" + NumberUtil.parseTimeString(this.getTempCount())));
			} else if(Seconds <= 5 && Seconds >= 1) {
				SoundLib.BLOCK_NOTE_HAT.playSound(Ability.getPlayer());
				Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&cÄðÅ¸ÀÓ &f" + NumberUtil.parseTimeString(this.getTempCount())));
			}
		}
		
		actionbar.Send(Ability.getPlayer());
	}
	
	@Override
	public void TimerEnd() {
		Ability.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&a´É·ÂÀ» ´Ù½Ã »ç¿ëÇÒ ¼ö ÀÖ½À´Ï´Ù."));
	}
	
}
