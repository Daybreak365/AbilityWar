package Marlang.AbilityWar.GameManager.Manager;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Config.AbilityWarSettings;
import Marlang.AbilityWar.Utils.AbilityWarThread;
import Marlang.AbilityWar.Utils.Messager;

/**
 * Death Manager
 * @author _Marlang 말랑
 */
public class DeathManager implements Listener {
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDeath(PlayerDeathEvent e) {
		Player Victim = e.getEntity();
		Player Killer = Victim.getKiller();
		if(Victim.getLastDamageCause() != null) {
			DamageCause Cause = Victim.getLastDamageCause().getCause();

			if(Killer != null) {
				e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&a" + Killer.getName() + "&f님이 &c" + Victim.getName() + "&f님을 죽였습니다."));
			} else {
				if(Cause.equals(DamageCause.CONTACT)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f님이 찔려 죽었습니다."));
				} else if(Cause.equals(DamageCause.FALL)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f님이 떨어져 죽었습니다."));
				} else if(Cause.equals(DamageCause.FALLING_BLOCK)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f님이 떨어지는 블록에 맞아 죽었습니다."));
				} else if(Cause.equals(DamageCause.SUFFOCATION)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f님이 끼여 죽었습니다."));
				} else if(Cause.equals(DamageCause.DROWNING)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f님이 물에 빠져 죽었습니다."));
				} else if(Cause.equals(DamageCause.ENTITY_EXPLOSION)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f님이 폭발하였습니다."));
				} else if(Cause.equals(DamageCause.LAVA)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f님이 용암에 빠져 죽었습니다."));
				} else if(Cause.equals(DamageCause.FIRE) || Cause.equals(DamageCause.FIRE_TICK)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f님이 노릇노릇하게 구워졌습니다."));
				} else {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f님이 죽었습니다."));
				}
			}
		} else {
			e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f님이 죽었습니다."));
		}
		
		if(AbilityWarThread.isGameTaskRunning()) {
			if(AbilityWarThread.getGame().isGameStarted()) {
				if(AbilityWarSettings.getItemDrop()) {
					e.setKeepInventory(false);
				} else {
					e.setKeepInventory(true);
				}
				if(AbilityWarThread.getGame().getPlayers().contains(Victim)) {
					if(AbilityWarSettings.getAbilityReveal()) {
						if(AbilityWarThread.getGame().getAbilities().containsKey(Victim)) {
							AbilityBase Ability = AbilityWarThread.getGame().getAbilities().get(Victim);
							Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f[&c능력&f] &c" + Victim.getName() + "&f님은 &e" + Ability.getAbilityName() + " &f능력이었습니다!"));
						} else {
							Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f[&c능력&f] &c" + Victim.getName() + "&f님은 능력이 없습니다!"));
						}
					}
					
					if(AbilityWarSettings.getEliminate()) {
						Eliminate(Victim);
					}
				}
			}
		}
	}
	
	/**
	 * 탈락된 유저 닉네임 목록
	 */
	private ArrayList<String> Eliminated = new ArrayList<String>();
	
	/**
	 * 플레이어를 탈락시킵니다.
	 * @param p   탈락시킬 플레이어입니다.
	 */
	public void Eliminate(Player p) {
		Eliminated.add(p.getName());
		p.kickPlayer(
				ChatColor.translateAlternateColorCodes('&', "&2《&aAbilityWar&2》")
				+ "\n"
				+ ChatColor.translateAlternateColorCodes('&', "&f탈락하셨습니다."));
		Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c" + p.getName() + "&f님이 탈락하셨습니다."));
	}
	
	/**
	 * 플레이어의 탈락 여부를 확인합니다.
	 */
	public boolean isEliminated(Player p) {
		return Eliminated.contains(p.getName());
	}
	
}
