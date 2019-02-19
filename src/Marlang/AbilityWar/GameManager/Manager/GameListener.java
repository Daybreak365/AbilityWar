package Marlang.AbilityWar.GameManager.Manager;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.plugin.EventExecutor;

import Marlang.AbilityWar.AbilityWar;
import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.AbilityBase.ActiveClickType;
import Marlang.AbilityWar.Ability.AbilityBase.ActiveMaterialType;
import Marlang.AbilityWar.Config.AbilityWarSettings;
import Marlang.AbilityWar.GameManager.Game.AbstractGame;
import Marlang.AbilityWar.Utils.VersionCompat.PlayerCompat;

public class GameListener implements Listener, EventExecutor {
	
	private AbstractGame game;
	
	public GameListener(AbstractGame abstractGame) {
		this.game = abstractGame;
		
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		
		for(Class<? extends Event> e : PassiveEvents) {
			Bukkit.getPluginManager().registerEvent(e, this, EventPriority.HIGH, this, AbilityWar.getPlugin());
		}
	}
	
	private HashMap<String, Instant> InstantMap = new HashMap<String, Instant>();
	
	/**
	 * 액티브 Listener
	 */
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		ActiveMaterialType mt = getMaterialType(PlayerCompat.getItemInHand(p).getType());
		ActiveClickType ct = (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) ? ActiveClickType.RightClick : ActiveClickType.LeftClick;
		if(mt != null) {
			if(game.hasAbility(p)) {
				AbilityBase Ability = game.getAbility(p);
				if(!Ability.isRestricted()) {
					if(InstantMap.containsKey(p.getName())) {
						Instant Before = InstantMap.get(p.getName());
						Instant Now = Instant.now();
						long Duration = java.time.Duration.between(Before, Now).toMillis();
						if(Duration >= 250) {
							InstantMap.put(p.getName(), Instant.now());
							ActiveSkill(Ability, mt, ct);
						}
					} else {
						InstantMap.put(p.getName(), Instant.now());
						ActiveSkill(Ability, mt, ct);
					}
				}
			}
		}
	}
	
	private static final String AbilityExecuted = ChatColor.translateAlternateColorCodes('&', "&d능력을 사용하였습니다.");
	
	private void ActiveSkill(AbilityBase Ability, ActiveMaterialType mt, ActiveClickType ct) {
		boolean Run = Ability.ActiveSkill(mt, ct);
		
		if(Run) {
			Ability.getPlayer().sendMessage(AbilityExecuted);
		}
	}
	
	/**
	 * 내구도 Listener
	 */
	@EventHandler
	public void onArmorDurabilityChange(PlayerItemDamageEvent e) {
		if(AbilityWarSettings.getInfiniteDurability()) {
			e.setCancelled(true);
			
			e.getItem().setDurability((short) 0);
		}
	}
	
	/**
	 * 날씨 Listener
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
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player joined = e.getPlayer();
		
		ArrayList<Player> PlayersToRemove = new ArrayList<Player>();
		ArrayList<Player> PlayersToAdd = new ArrayList<Player>();
		
		for(Player p : game.getParticipants()) {
			if(p.getName().equals(joined.getName())) {
				PlayersToRemove.add(p);
				PlayersToAdd.add(joined);
			}
		}
		
		game.getParticipants().removeAll(PlayersToRemove);
		game.getParticipants().addAll(PlayersToAdd);
		
		ArrayList<Player> AbilitiesToRemove = new ArrayList<Player>();
		HashMap<Player, AbilityBase> AbilitiesToAdd = new HashMap<Player, AbilityBase>();
		
		for(Player p : game.getAbilities().keySet()) {
			if(p.getName().equals(joined.getName())) {
				AbilityBase Ability = game.getAbilities().get(p);
				Ability.updatePlayer(joined);
				AbilitiesToRemove.add(p);
				AbilitiesToAdd.put(joined, Ability);
			}
		}
		
		game.getAbilities().keySet().removeAll(AbilitiesToRemove);
		game.getAbilities().putAll(AbilitiesToAdd);
		
		AbilitySelect select = game.getAbilitySelect();
		if(select != null) {
			ArrayList<Player> SelectToRemove = new ArrayList<Player>();
			HashMap<Player, Boolean> SelectToAdd = new HashMap<Player, Boolean>();
			
			for(Player p : select.getMap().keySet()) {
				if(p.getName().equals(joined.getName())) {
					SelectToRemove.add(p);
					SelectToAdd.put(joined, select.getMap().get(p));
				}
			}
			
			select.getMap().keySet().removeAll(SelectToRemove);
			select.getMap().putAll(SelectToAdd);
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
	
	public ActiveMaterialType getMaterialType(Material m) {
		for(ActiveMaterialType Type : ActiveMaterialType.values()) {
			if(Type.getMaterial().equals(m)) {
				return Type;
			}
		}
		
		return null;
	}

	@Override
	public void execute(Listener listener, Event e) throws EventException {
		for(AbilityBase Ability : new ArrayList<AbilityBase>(game.getAbilities().values())) {
			if(!Ability.isRestricted()) {
				Ability.PassiveSkill(e);
			}
		}
	}
	
}
