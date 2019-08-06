package DayBreak.AbilityWar.Game.Manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import DayBreak.AbilityWar.AbilityWar;
import DayBreak.AbilityWar.Config.AbilityWarSettings;
import DayBreak.AbilityWar.Game.Events.EventCaller;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame;

public class GameListener implements Listener {
	
	private AbstractGame game;
	private EventCaller eventCaller = new EventCaller();
	
	public GameListener(AbstractGame abstractGame) {
		this.game = abstractGame;
		
		abstractGame.registerListener(this);
		
		Bukkit.getPluginManager().registerEvent(EntityDamageEvent.class, this, EventPriority.HIGHEST, eventCaller, AbilityWar.getPlugin());
	}
	
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
	
}
