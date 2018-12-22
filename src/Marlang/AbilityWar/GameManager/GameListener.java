package Marlang.AbilityWar.GameManager;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.AbilityBase.ActiveClickType;
import Marlang.AbilityWar.Ability.AbilityBase.ActiveMaterialType;
import Marlang.AbilityWar.Utils.AbilityWarThread;

public class GameListener implements Listener {
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if(AbilityWarThread.isGameTaskRunning()) {
			Player p = e.getPlayer();
			ActiveMaterialType mt = getMaterialType(p.getInventory().getItemInMainHand().getType());
			ActiveClickType ct = (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) ? ActiveClickType.RightClick : ActiveClickType.LeftClick;
			if(mt != null) {
				if(AbilityWarThread.getGame().getAbilities().containsKey(p)) {
					AbilityBase Ability = AbilityWarThread.getGame().getAbilities().get(p);
					if(Ability.isRestricted()) {
						if(!AbilityWarThread.getGame().isAbilityRestricted()) {
							Ability.ActiveSkill(mt, ct);
						}
					} else {
						Ability.ActiveSkill(mt, ct);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerDamage(EntityDamageEvent e) {
		if(AbilityWarThread.isGameTaskRunning()) {
			for(AbilityBase Ability : AbilityWarThread.getGame().getAbilities().values()) {
				if(Ability.isRestricted()) {
					if(!AbilityWarThread.getGame().isAbilityRestricted()) {
						Ability.PassiveSkill(e);
					}
				} else {
					Ability.PassiveSkill(e);
				}
			}
			
			if(e.getEntity() instanceof Player) {
				if(AbilityWarThread.getGame().getInvincibility().isTimerRunning()) {
					e.setCancelled(true);
				}
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
