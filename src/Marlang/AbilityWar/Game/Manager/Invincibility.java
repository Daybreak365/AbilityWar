package Marlang.AbilityWar.Game.Manager;

import org.bukkit.ChatColor;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Config.AbilityWarSettings;
import Marlang.AbilityWar.Game.Games.AbstractGame;
import Marlang.AbilityWar.Game.Games.AbstractGame.Participant;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Library.SoundLib;
import Marlang.AbilityWar.Utils.Library.Packet.TitlePacket;
import Marlang.AbilityWar.Utils.Math.NumberUtil;
import Marlang.AbilityWar.Utils.Thread.TimerBase;

/**
 * 무적
 * @author _Marlang 말랑
 */
public class Invincibility {
	
	private final Integer Duration = AbilityWarSettings.getInvincibilityDuration();
	private final AbstractGame game;
	
	public Invincibility(AbstractGame game) {
		this.game = game;
	}
	
	private TimerBase InvincibilityTimer;
	
	public boolean Start(boolean Infinite) {
		if(this.InvincibilityTimer == null || !this.InvincibilityTimer.isTimerRunning()) {
			if(!Infinite) {
				this.InvincibilityTimer = new TimerBase(Duration * 60) {

					@Override
					protected void onStart() {
						for(Participant participant : game.getParticipants()) {
							if(participant.hasAbility()) {
								AbilityBase ability = participant.getAbility();
								ability.setRestricted(true);
							}
						}
						
						Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&a무적이 &f" + NumberUtil.parseTimeString(Duration * 60) + "&a동안 적용됩니다."));
					}
					
					@Override
					protected void TimerProcess(Integer Seconds) {
						if(Seconds == (Duration * 60) / 2) {
							Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&a무적이 &f" + NumberUtil.parseTimeString(Seconds) + " &a후에 해제됩니다."));
						}
						

						if(Seconds <= 5 && Seconds >= 1) {
							Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&a무적이 &f" + NumberUtil.parseTimeString(Seconds) + " &a후에 해제됩니다."));
							SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
						}
					}
					
					@Override
					protected void onEnd() {
						game.setRestricted(false);
						TitlePacket titlePacket = new TitlePacket(ChatColor.translateAlternateColorCodes('&', "&c&lWarning"),
								ChatColor.translateAlternateColorCodes('&', "&f무적이 해제되었습니다."), 20, 60, 20);
						titlePacket.Broadcast();
						SoundLib.ENTITY_ENDER_DRAGON_AMBIENT.broadcastSound();
						
						for(Participant participant : game.getParticipants()) {
							if(participant.hasAbility()) {
								participant.getAbility().setRestricted(false);
							}
						}
					}
					
				};
			} else {
				this.InvincibilityTimer = new TimerBase() {

					@Override
					protected void onStart() {
						for(Participant participant : game.getParticipants()) {
							if(participant.hasAbility()) {
								AbilityBase ability = participant.getAbility();
								ability.setRestricted(true);
							}
						}
						Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&a무적이 적용되었습니다. 지금부터 무적이 해제될 때까지 데미지를 입지 않습니다."));
					}
					
					@Override
					protected void TimerProcess(Integer Seconds) {}
					
					@Override
					protected void onEnd() {
						game.setRestricted(false);
						TitlePacket titlePacket = new TitlePacket(ChatColor.translateAlternateColorCodes('&', "&c&lWarning"),
								ChatColor.translateAlternateColorCodes('&', "&f무적이 해제되었습니다."), 20, 60, 20);
						titlePacket.Broadcast();
						SoundLib.ENTITY_ENDER_DRAGON_AMBIENT.broadcastSound();
						
						for(Participant participant : game.getParticipants()) {
							if(participant.hasAbility()) {
								participant.getAbility().setRestricted(false);
							}
						}
					}
					
				};
			}
			
			this.InvincibilityTimer.StartTimer();
			return true;
		}
		
		return false;
	}
	
	public boolean Stop() {
		if(this.InvincibilityTimer != null && this.InvincibilityTimer.isTimerRunning()) {
			this.InvincibilityTimer.StopTimer(false);
			return true;
		}
		
		return false;
	}
	
	public boolean isInvincible() {
		return this.InvincibilityTimer != null && this.InvincibilityTimer.isTimerRunning();
	}
	
}
