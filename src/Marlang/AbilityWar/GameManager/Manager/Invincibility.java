package Marlang.AbilityWar.GameManager.Manager;

import org.bukkit.ChatColor;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Config.AbilityWarSettings;
import Marlang.AbilityWar.GameManager.Game.Game;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Library.SoundLib;
import Marlang.AbilityWar.Utils.Math.NumberUtil;
import Marlang.AbilityWar.Utils.PacketLib.TitlePacket;
import Marlang.AbilityWar.Utils.Thread.TimerBase;

/**
 * 초반 무적
 * @author _Marlang 말랑
 */
public class Invincibility extends TimerBase {
	
	private Integer Duration = AbilityWarSettings.getInvincibilityDuration();
	private Game game;
	
	public Invincibility(Game game) {
		super(AbilityWarSettings.getInvincibilityDuration() * 60);
		this.game = game;
	}
	
	@Override
	public void onStart() {
		Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&a초반 무적이 &f" + NumberUtil.parseTimeString(Duration * 60) + "&a동안 적용됩니다."));
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
	public void onEnd() {
		game.setRestricted(false);
		Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&a초반 무적이 해제되었습니다."));
		SoundLib.ENTITY_ENDERDRAGON_AMBIENT.broadcastSound();
		
		TitlePacket title = new TitlePacket(ChatColor.translateAlternateColorCodes('&', "&c&lWarning"),
				ChatColor.translateAlternateColorCodes('&', "&f초반 무적이 해제되었습니다."), 20, 60, 20);
		title.Broadcast();
		
		for(AbilityBase Ability : game.getAbilities().values()) {
			Ability.setRestricted(false);
		}
	}
	
}
