package DayBreak.AbilityWar.Game.Manager;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.plugin.EventExecutor;

import DayBreak.AbilityWar.AbilityWar;
import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Config.AbilityWarSettings;
import DayBreak.AbilityWar.Game.Events.EventCaller;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame.Participant;

public class GameListener implements Listener, EventExecutor {
	
	private AbstractGame game;
	private EventCaller eventCaller = new EventCaller();
	
	public GameListener(AbstractGame abstractGame) {
		this.game = abstractGame;
		
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		
		Bukkit.getPluginManager().registerEvent(EntityDamageEvent.class, this, EventPriority.HIGHEST, eventCaller, AbilityWar.getPlugin());
		
		for(Class<? extends Event> e : PassiveEvents) {
			Bukkit.getPluginManager().registerEvent(e, this, EventPriority.HIGH, this, AbilityWar.getPlugin());
		}
	}
	
	/**
	 * ³¯¾¾ Listener
	 */
	@EventHandler
	public void onWeatherChange(WeatherChangeEvent e) {
		if(game.isGameStarted()) {
			if(AbilityWarSettings.getClearWeather()) {
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent e) {
		if(AbilityWarSettings.getNoHunger()) {
			e.setCancelled(true);
			
			Player p = (Player) e.getEntity();
			p.setFoodLevel(19);
		}
	}
	
	private static ArrayList<Class<? extends Event>> PassiveEvents = new ArrayList<Class<? extends Event>>();
	
	static {
		registerPassive(EntityDamageEvent.class);
		registerPassive(ProjectileLaunchEvent.class);
		registerPassive(ProjectileHitEvent.class);
		registerPassive(BlockBreakEvent.class);
		registerPassive(PlayerMoveEvent.class);
		registerPassive(PlayerDeathEvent.class);
	}
	
	public static void registerPassive(Class<? extends Event> e) {
		if(!PassiveEvents.contains(e)) {
			PassiveEvents.add(e);
		}
	}
	
	@Override
	public void execute(Listener listener, Event e) throws EventException {
		for(Participant participant : game.getParticipants()) {
			if(participant.hasAbility()) {
				AbilityBase ability = participant.getAbility();
				if(!ability.isRestricted()) {
					ability.PassiveSkill(e);
				}
			}
		}
	}
	
}
