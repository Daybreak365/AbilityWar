package DayBreak.AbilityWar.Game.Manager;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;

import DayBreak.AbilityWar.Config.AbilityWarSettings.DeathSettings;
import DayBreak.AbilityWar.Game.Events.ParticipantDeathEvent;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame.Participant;
import DayBreak.AbilityWar.Utils.KoreanUtil;
import DayBreak.AbilityWar.Utils.Messager;

/**
 * Death Manager
 * @author DayBreak 새벽
 */
public class DeathManager implements Listener {
	
	private final AbstractGame game;

	public DeathManager(AbstractGame game) {
		this.game = game;
		game.registerListener(this);
	}

	@EventHandler
	private void onDeath(PlayerDeathEvent e) {
		Player victimPlayer = e.getEntity();
		Player killerPlayer = victimPlayer.getKiller();
		if(victimPlayer.getLastDamageCause() != null) {
			DamageCause Cause = victimPlayer.getLastDamageCause().getCause();

			if(killerPlayer != null) {
				e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&a" + killerPlayer.getName() + "&f님이 &c" + victimPlayer.getName() + "&f님을 죽였습니다."));
			} else {
				if(Cause.equals(DamageCause.CONTACT)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 찔려 죽었습니다."));
				} else if(Cause.equals(DamageCause.FALL)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 떨어져 죽었습니다."));
				} else if(Cause.equals(DamageCause.FALLING_BLOCK)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 떨어지는 블록에 맞아 죽었습니다."));
				} else if(Cause.equals(DamageCause.SUFFOCATION)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 끼여 죽었습니다."));
				} else if(Cause.equals(DamageCause.DROWNING)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 익사했습니다."));
				} else if(Cause.equals(DamageCause.ENTITY_EXPLOSION)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 폭발했습니다."));
				} else if(Cause.equals(DamageCause.LAVA)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 용암에 빠져 죽었습니다."));
				} else if(Cause.equals(DamageCause.FIRE) || Cause.equals(DamageCause.FIRE_TICK)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 노릇노릇하게 구워졌습니다."));
				} else {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 죽었습니다."));
				}
			}
		} else {
			e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 죽었습니다."));
		}

		if(DeathSettings.getItemDrop()) {
			e.setKeepInventory(false);
			victimPlayer.getInventory().clear();
		} else {
			e.setKeepInventory(true);
		}

		if(game.isParticipating(victimPlayer)) {
			Participant victim = game.getParticipant(victimPlayer);
			
			Bukkit.getPluginManager().callEvent(new ParticipantDeathEvent(victim));
			
			if(DeathSettings.getAbilityReveal()) {
				if(victim.hasAbility()) {
					String name = victim.getAbility().getName();
					if(name != null) {
						Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f[&c능력&f] &c" + victimPlayer.getName() + "&f님의 능력은 " + KoreanUtil.getCompleteWord("&e" + name, "&f이었", "&f였") + "습니다."));
					}
				} else {
					Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f[&c능력&f] &c" + victimPlayer.getName() + "&f님은 능력이 없습니다."));
				}
			}
		}

		if(game.isGameStarted() && game.isParticipating(victimPlayer)) {
			switch(DeathSettings.getOperation()) {
			case 탈락:
				this.Eliminate(victimPlayer);
				break;
			case 관전모드:
				victimPlayer.setGameMode(GameMode.SPECTATOR);
				break;
			default:
				break;
			}
			if(DeathSettings.getAbilityRemoval()) game.getParticipant(victimPlayer).removeAbility();
		}
	}
	
	/**
	 * 탈락된 유저 UUID 목록
	 */
	private final ArrayList<UUID> Eliminated = new ArrayList<UUID>();
	
	/**
	 * 플레이어를 탈락시킵니다.
	 * @param p   탈락시킬 플레이어입니다.
	 */
	public void Eliminate(Player p) {
		Eliminated.add(p.getUniqueId());
		p.kickPlayer(
				Messager.getPrefix()
				+ "\n"
				+ ChatColor.translateAlternateColorCodes('&', "&f탈락하셨습니다."));
		Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c" + p.getName() + "&f님이 탈락하셨습니다."));
	}
	
	/**
	 * 플레이어의 탈락 여부를 확인합니다.
	 */
	public boolean isEliminated(Player p) {
		return Eliminated.contains(p.getUniqueId());
	}
	
}
