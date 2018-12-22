package Marlang.AbilityWar.GameManager;

import org.bukkit.ChatColor;
import org.bukkit.Sound;

import Marlang.AbilityWar.AbilityWar;
import Marlang.AbilityWar.Utils.EffectUtil;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.NumberUtil;
import Marlang.AbilityWar.Utils.TimerBase;

/**
 * 초반 무적
 * @author _Marlang 말랑
 */
public class Invincibility extends TimerBase {
	
	static Integer Duration = AbilityWar.getSetting().getInvincibilityDuration();
	
	public Invincibility() {
		super(Duration * 60);
		Messager.sendMessage(Duration + "분");
	}
	
	public void setInvincibility() {
		this.StartTimer();
	}
	
	@Override
	public void TimerStart() {
		Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&a초반 무적이 &f" + Duration + "분 &a동안 적용됩니다."));
	}
	
	@Override
	public void TimerProcess(Integer Seconds) {
		if(Seconds == (Duration * 60) / 2) {
			Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&a초반 무적이 &f" + NumberUtil.parseTimeString(Seconds) + " &a후에 해제됩니다."));
		}
		

		if(Seconds <= 5 && Seconds >= 1) {
			Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&a초반 무적이 &f" + NumberUtil.parseTimeString(Seconds) + " &a후에 해제됩니다."));
			EffectUtil.broadcastSound(Sound.BLOCK_NOTE_HARP);
		}
	}
	
	@Override
	public void TimerEnd() {
		Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&a초반 무적이 해제되었습니다."));
		EffectUtil.broadcastTitle(
				ChatColor.translateAlternateColorCodes('&', "&c&lWarning"),
				ChatColor.translateAlternateColorCodes('&', "&f초반 무적이 해제되었습니다."));
		EffectUtil.broadcastSound(Sound.ENTITY_ENDERDRAGON_AMBIENT);
	}
	
}
