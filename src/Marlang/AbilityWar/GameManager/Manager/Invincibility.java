package Marlang.AbilityWar.GameManager.Manager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import Marlang.AbilityWar.API.Events.AbilityWarProgressEvent;
import Marlang.AbilityWar.API.Events.AbilityWarProgressEvent.Progress;
import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Config.AbilityWarSettings;
import Marlang.AbilityWar.GameManager.Game;
import Marlang.AbilityWar.Utils.AbilityWarThread;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.NumberUtil;
import Marlang.AbilityWar.Utils.TimerBase;
import Marlang.AbilityWar.Utils.Library.SoundLib;

/**
 * 초반 무적
 * @author _Marlang 말랑
 */
public class Invincibility extends TimerBase {
	
	Integer Duration = AbilityWarSettings.getInvincibilityDuration();
	Game game;
	
	public Invincibility(Game game) {
		super(AbilityWarSettings.getInvincibilityDuration() * 60);
		this.game = game;
	}
	
	@Override
	public void TimerStart() {
		Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&a초반 무적이 &f" + NumberUtil.parseTimeString(Duration * 60) + "&a동안 적용됩니다."));
	
		AbilityWarProgressEvent event = new AbilityWarProgressEvent(Progress.Invincibility_STARTED, game.getGameAPI());
		Bukkit.getPluginManager().callEvent(event);
	}
	
	@Override
	public void TimerProcess(Integer Seconds) {
		if(Seconds == (Duration * 60) / 2) {
			Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&a초반 무적이 &f" + NumberUtil.parseTimeString(Seconds) + " &a후에 해제됩니다."));
		}
		

		if(Seconds <= 5 && Seconds >= 1) {
			Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&a초반 무적이 &f" + NumberUtil.parseTimeString(Seconds) + " &a후에 해제됩니다."));
			SoundLib.BLOCK_NOTE_HARP.broadcastSound();
		}
	}
	
	@Override
	public void TimerEnd() {
		Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&a초반 무적이 해제되었습니다."));
		SoundLib.ENTITY_ENDERDRAGON_AMBIENT.broadcastSound();
		
		for(AbilityBase Ability : AbilityWarThread.getGame().getAbilities().values()) {
			Ability.setRestricted(false);
		}
		
		AbilityWarProgressEvent event = new AbilityWarProgressEvent(Progress.Invincibility_ENDED, game.getGameAPI());
		Bukkit.getPluginManager().callEvent(event);
	}
	
}
