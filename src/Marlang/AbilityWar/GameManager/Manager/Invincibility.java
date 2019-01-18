package Marlang.AbilityWar.GameManager.Manager;

import org.bukkit.ChatColor;
import org.bukkit.Sound;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Config.AbilityWarSettings;
import Marlang.AbilityWar.Utils.AbilityWarThread;
import Marlang.AbilityWar.Utils.EffectUtil;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.NumberUtil;
import Marlang.AbilityWar.Utils.TimerBase;

/**
 * 초반 무적
 * @author _Marlang 말랑
 */
public class Invincibility extends TimerBase {
	
	static Integer Duration;
	
	public Invincibility() {
		super(AbilityWarSettings.getInvincibilityDuration() * 60);
		Duration = AbilityWarSettings.getInvincibilityDuration();
	}
	
	@Override
	public void TimerStart() {
		Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&a초반 무적이 &f" + NumberUtil.parseTimeString(Duration * 60) + "&a동안 적용됩니다."));
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
		
		for(AbilityBase Ability : AbilityWarThread.getGame().getAbilities().values()) {
			Ability.setRestricted(false);
		}
	}
	
}
