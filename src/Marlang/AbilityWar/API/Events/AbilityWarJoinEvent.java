package Marlang.AbilityWar.API.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import Marlang.AbilityWar.API.GameAPI;

public class AbilityWarJoinEvent extends Event {
	
	Player player;
	GameAPI api;
	
	public AbilityWarJoinEvent(Player player, GameAPI api) {
		this.player = player;
		this.api = api;
	}
	
	public Player getPlayer() {
		return player;
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
	
	public static enum JoinType {
		
		Rejoin, Join;
		
	}
	
}
