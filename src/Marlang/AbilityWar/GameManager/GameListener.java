package Marlang.AbilityWar.GameManager;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import Marlang.AbilityWar.API.Events.AbilityWarJoinEvent;
import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.AbilityBase.ActiveClickType;
import Marlang.AbilityWar.Ability.AbilityBase.ActiveMaterialType;
import Marlang.AbilityWar.Config.AbilityWarSettings;
import Marlang.AbilityWar.GameManager.Module.Module;
import Marlang.AbilityWar.Utils.AbilityWarThread;

public class GameListener extends Module implements Listener {
	
	public GameListener() {
		RegisterListener(this);
	}
	
	HashMap<String, Instant> InstantMap = new HashMap<String, Instant>();
	
	/**
	 * 액티브 Listener
	 */
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if(AbilityWarThread.isGameTaskRunning()) {
			Player p = e.getPlayer();
			ActiveMaterialType mt = getMaterialType(p.getInventory().getItemInMainHand().getType());
			ActiveClickType ct = (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) ? ActiveClickType.RightClick : ActiveClickType.LeftClick;
			if(mt != null) {
				if(AbilityWarThread.getGame().getAbilities().containsKey(p)) {
					AbilityBase Ability = AbilityWarThread.getGame().getAbilities().get(p);
					if(!Ability.isRestricted()) {
						if(InstantMap.containsKey(p.getName())) {
							Instant Before = InstantMap.get(p.getName());
							Instant Now = Instant.now();
							long Duration = java.time.Duration.between(Before, Now).toMillis();
							if(Duration >= 250) {
								InstantMap.put(p.getName(), Instant.now());
								Ability.ActiveSkill(mt, ct);
							}
						} else {
							InstantMap.put(p.getName(), Instant.now());
							Ability.ActiveSkill(mt, ct);
						}
					}
				}
			}
		}
	}
	
	/**
	 * 내구도 Listener
	 */
	@EventHandler
	public void onArmorDurabilityChange(PlayerItemDamageEvent e) {
		if(AbilityWarThread.isGameTaskRunning()) {
			if(AbilityWarSettings.getInfiniteDurability()) {
				e.setCancelled(true);
				
				e.getItem().setDurability((short) 0);
			}
		}
	}
	
	/**
	 * 날씨 Listener
	 */
	@EventHandler
	public void onWeatherChange(WeatherChangeEvent e) {
		if(AbilityWarThread.isGameTaskRunning()) {
			if(AbilityWarThread.getGame().isGameStarted()) {
				if(AbilityWarSettings.getClearWeather()) {
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerDamage(EntityDamageEvent e) {
		if(AbilityWarThread.isGameTaskRunning()) {
			if(e.getEntity() instanceof Player) {
				if(AbilityWarThread.getGame().getInvincibility().isTimerRunning()) {
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent e) {
		if(AbilityWarThread.isGameTaskRunning()) {
			if(AbilityWarSettings.getNoHunger()) {
				e.setCancelled(true);
				
				Player p = (Player) e.getEntity();
				p.setFoodLevel(19);
			}
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player joined = e.getPlayer();
		if(AbilityWarThread.isGameTaskRunning()) {
			Game game = AbilityWarThread.getGame();
			
			ArrayList<Player> PlayersToRemove = new ArrayList<Player>();
			ArrayList<Player> PlayersToAdd = new ArrayList<Player>();
			
			for(Player p : game.getPlayers()) {
				if(p.getName().equals(joined.getName())) {
					PlayersToRemove.add(p);
					PlayersToAdd.add(joined);
				}
			}
			
			game.getPlayers().removeAll(PlayersToRemove);
			game.getPlayers().addAll(PlayersToAdd);
			
			ArrayList<Player> AbilitiesToRemove = new ArrayList<Player>();
			HashMap<Player, AbilityBase> AbilitiesToAdd = new HashMap<Player, AbilityBase>();
			
			for(Player p : game.getAbilities().keySet()) {
				if(p.getName().equals(joined.getName())) {
					AbilityBase Ability = game.getAbilities().get(p);
					Ability.setPlayer(joined);
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
				
				for(Player p : select.AbilitySelect.keySet()) {
					if(p.getName().equals(joined.getName())) {
						SelectToRemove.add(p);
						SelectToAdd.put(joined, select.AbilitySelect.get(p));
					}
				}
				
				select.AbilitySelect.keySet().removeAll(SelectToRemove);
				select.AbilitySelect.putAll(SelectToAdd);
			}
			
			AbilityWarJoinEvent event = new AbilityWarJoinEvent(joined, game.getGameAPI());
			Bukkit.getPluginManager().callEvent(event);
			
		}
	}
	
	/**
	 * 패시브 Listener
	 */
	@EventHandler
	public void EntityDamagePassive(EntityDamageEvent e) {
		if(AbilityWarThread.isGameTaskRunning()) {
			for(AbilityBase Ability : AbilityWarThread.getGame().getAbilities().values()) {
				if(!Ability.isRestricted()) {
					Ability.PassiveSkill(e);
				}
			}
		}
	}
	
	/**
	 * 패시브 Listener
	 */
	@EventHandler
	public void ProjectileLaunchPassive(ProjectileLaunchEvent e) {
		if(AbilityWarThread.isGameTaskRunning()) {
			for(AbilityBase Ability : AbilityWarThread.getGame().getAbilities().values()) {
				if(!Ability.isRestricted()) {
					Ability.PassiveSkill(e);
				}
			}
		}
	}

	/**
	 * 패시브 Listener
	 */
	@EventHandler
	public void ProjectileHitPassive(ProjectileHitEvent e) {
		if(AbilityWarThread.isGameTaskRunning()) {
			for(AbilityBase Ability : AbilityWarThread.getGame().getAbilities().values()) {
				if(!Ability.isRestricted()) {
					Ability.PassiveSkill(e);
				}
			}
		}
	}

	/**
	 * 패시브 Listener
	 */
	@EventHandler
	public void BlockBreakPassive(BlockBreakEvent e) {
		if(AbilityWarThread.isGameTaskRunning()) {
			for(AbilityBase Ability : AbilityWarThread.getGame().getAbilities().values()) {
				if(!Ability.isRestricted()) {
					Ability.PassiveSkill(e);
				}
			}
		}
	}

	/**
	 * 패시브 Listener
	 */
	@EventHandler
	public void BlockExplodePassive(BlockExplodeEvent e) {
		if(AbilityWarThread.isGameTaskRunning()) {
			for(AbilityBase Ability : AbilityWarThread.getGame().getAbilities().values()) {
				if(!Ability.isRestricted()) {
					Ability.PassiveSkill(e);
				}
			}
		}
	}

	/**
	 * 패시브 Listener
	 */
	@EventHandler
	public void BlockExplodePassive(PlayerMoveEvent e) {
		if(AbilityWarThread.isGameTaskRunning()) {
			for(AbilityBase Ability : AbilityWarThread.getGame().getAbilities().values()) {
				if(!Ability.isRestricted()) {
					Ability.PassiveSkill(e);
				}
			}
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
	
}
