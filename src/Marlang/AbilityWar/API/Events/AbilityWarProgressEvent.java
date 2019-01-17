package Marlang.AbilityWar.API.Events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import Marlang.AbilityWar.API.GameAPI;

public class AbilityWarProgressEvent extends Event {
	
	Progress progress;
	GameAPI api;
	
	public AbilityWarProgressEvent(Progress progress, GameAPI api) {
		this.progress = progress;
	}
	
	public Progress getProgress() {
		return progress;
	}
	
	public GameAPI getAPI() {
		return api;
	}
	
	private static final HandlerList handlers = new HandlerList();
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	public static enum Progress {
		
		AbilitySelect_STARTED, AbilitySelect_FINISHED,
		Game_STARTED;
		
	}
	
}
