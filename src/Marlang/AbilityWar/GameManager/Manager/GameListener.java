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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.EventExecutor;

import Marlang.AbilityWar.AbilityWar;
import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.AbilityBase.ClickType;
import Marlang.AbilityWar.Ability.AbilityBase.MaterialType;
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
	public void ActiveListener(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		MaterialType mt = getMaterialType(PlayerCompat.getItemInHand(p).getType());
		ClickType ct = (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) ? ClickType.RightClick : ClickType.LeftClick;
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
							
							if(ct.equals(ClickType.LeftClick)) {
								Ability.TargetSkill(mt, null);
							}
						}
					} else {
						InstantMap.put(p.getName(), Instant.now());
						ActiveSkill(Ability, mt, ct);

						if(ct.equals(ClickType.LeftClick)) {
							Ability.TargetSkill(mt, null);
						}
					}
				}
			}
		}
	}
	
	private static final String AbilityExecuted = ChatColor.translateAlternateColorCodes('&', "&d능력을 사용하였습니다.");
	
	private void ActiveSkill(AbilityBase Ability, MaterialType mt, ClickType ct) {
		boolean Run = Ability.ActiveSkill(mt, ct);
		
		if(Run) {
			Ability.getPlayer().sendMessage(AbilityExecuted);
		}
	}

	/**
	 * TargetSkill Listener
	 */
	@EventHandler
	public void TargetListener(EntityDamageByEntityEvent e) {
		if(e.getDamager() instanceof Player) {
			Player p = (Player) e.getDamager();
			MaterialType mt = getMaterialType(PlayerCompat.getItemInHand(p).getType());
			if(mt != null) {
				if(!e.isCancelled()) {
					if(game.hasAbility(p)) {
						AbilityBase Ability = game.getAbility(p);
						if(!Ability.isRestricted()) {
							if(InstantMap.containsKey(p.getName())) {
								Instant Before = InstantMap.get(p.getName());
								Instant Now = Instant.now();
								long Duration = java.time.Duration.between(Before, Now).toMillis();
								if(Duration >= 250) {
									InstantMap.put(p.getName(), Instant.now());
									Ability.TargetSkill(mt, e.getEntity());
								}
							} else {
								InstantMap.put(p.getName(), Instant.now());
								Ability.TargetSkill(mt, e.getEntity());
							}
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
	public void onItemDurability(PlayerInteractEvent e) {
		if(e.getItem() != null) {
			if(hasDurability(e.getItem().getType())) {
				e.getItem().setDurability((short) 0);
			}
		}
	}

	/**
	 * 내구도 Listener
	 */
	@EventHandler
	public void onArmorDurability(EntityDamageEvent e) {
		if(e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			
			ItemStack Boots = p.getInventory().getBoots();
			if(Boots != null && hasDurability(Boots.getType())) {
				Boots.setDurability((short) 0);
				p.getInventory().setBoots(Boots);
			}
			
			ItemStack Leggings = p.getInventory().getLeggings();
			if(Leggings != null && hasDurability(Leggings.getType())) {
				Leggings.setDurability((short) 0);
				p.getInventory().setLeggings(Leggings);
			}
			
			ItemStack Chestplate = p.getInventory().getChestplate();
			if(Chestplate != null && hasDurability(Chestplate.getType())) {
				Chestplate.setDurability((short) 0);
				p.getInventory().setChestplate(Chestplate);
			}
			
			ItemStack Helmet = p.getInventory().getHelmet();
			if(Helmet != null && hasDurability(Helmet.getType())) {
				Helmet.setDurability((short) 0);
				p.getInventory().setHelmet(Helmet);
			}
		}
	}
	
	private boolean hasDurability(Material m) {
		String materialName = m.toString();
		
		String[] split = materialName.split("_");
		if(split.length > 1) {
			String[] Item = {"AXE", "HOE", "PICKAXE", "SPADE", "SWORD", "BOOTS", "LEGGINGS", "CHESTPLATE", "HELMET"};
			for(String compare : Item) {
				if(split[1].equalsIgnoreCase(compare)) {
					return true;
				}
			}
		} else {
			String[] Item = {"BOW", "SHEARS", "FISHING_ROD", "FLINT_AND_STEEL"};
			for(String compare : Item) {
				if(materialName.equalsIgnoreCase(compare)) {
					return true;
				}
			}
		}
		
		return false;
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
	
	public MaterialType getMaterialType(Material m) {
		for(MaterialType Type : MaterialType.values()) {
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
