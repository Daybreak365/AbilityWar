package Marlang.AbilityWar.GameManager;

import java.time.Instant;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import Marlang.AbilityWar.AbilityWar;
import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.AbilityBase.ActiveClickType;
import Marlang.AbilityWar.Ability.AbilityBase.ActiveMaterialType;
import Marlang.AbilityWar.GameManager.Module.Module;
import Marlang.AbilityWar.Utils.AbilityWarThread;

public class GameListener extends Module implements Listener {
	
	public GameListener() {
		RegisterListener(this);
	}
	
	HashMap<String, Instant> InstantMap = new HashMap<String, Instant>();
	
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
			if(AbilityWar.getSetting().getNoHunger()) {
				e.setCancelled(true);
				
				Player p = (Player) e.getEntity();
				p.setFoodLevel(19);
			}
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		if(AbilityWarThread.isGameTaskRunning()) {
			Game game = AbilityWarThread.getGame();
			Player joined = e.getPlayer();

			for(Player p : game.getPlayers()) {
				if(p.getName().equals(joined.getName())) {
					game.getPlayers().remove(p);
					game.getPlayers().add(joined);
				}
			}
			
			for(Player p : game.getSpectators()) {
				if(p.getName().equals(joined.getName())) {
					game.getSpectators().remove(p);
					game.getSpectators().add(joined);
				}
			}
			
			for(Player p : game.getAbilities().keySet()) {
				if(p.getName().equals(joined.getName())) {
					AbilityBase Ability = game.getAbilities().get(p);
					Ability.setPlayer(joined);
					game.getAbilities().remove(p);
					game.getAbilities().put(joined, Ability);
				}
			}
			
			AbilitySelect select = AbilityWarThread.getAbilitySelect();
			if(select != null) {
				for(Player p : select.AbilitySelect.keySet()) {
					if(p.getName().equals(joined.getName())) {
						select.AbilitySelect.put(joined, select.AbilitySelect.get(p));
						select.AbilitySelect.remove(p);
					}
				}
			}
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
	
	public ActiveMaterialType getMaterialType(Material m) {
		for(ActiveMaterialType Type : ActiveMaterialType.values()) {
			if(Type.getMaterial().equals(m)) {
				return Type;
			}
		}
		
		return null;
	}
	
}
