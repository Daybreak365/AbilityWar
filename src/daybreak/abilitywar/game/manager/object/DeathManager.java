package daybreak.abilitywar.game.manager.object;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.config.Configuration.Settings.DeathSettings;
import daybreak.abilitywar.game.events.participant.ParticipantDeathEvent;
import daybreak.abilitywar.game.games.mode.AbstractGame;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.game.games.standard.Game;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.message.KoreanUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Death Manager
 *
 * @author Daybreak 새벽
 */
public class DeathManager implements Listener, AbstractGame.Observer {

	private final Game game;

	public DeathManager(Game game) {
		this.game = game;
		game.attachObserver(this);
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
	}

	@EventHandler
	public final void onPlayerDeath(PlayerDeathEvent e) {
		Player victimPlayer = e.getEntity();
		Player killerPlayer = victimPlayer.getKiller();
		if (victimPlayer.getLastDamageCause() != null) {
			if (killerPlayer != null) {
				e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&a" + killerPlayer.getName() + "&f님이 &c" + victimPlayer.getName() + "&f님을 죽였습니다."));
			} else {
				switch (victimPlayer.getLastDamageCause().getCause()) {
					case CONTACT:
						e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 찔려 죽었습니다."));
						break;
					case FALL:
						e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 떨어져 죽었습니다."));
						break;
					case FALLING_BLOCK:
						e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 떨어지는 블록에 맞아 죽었습니다."));
						break;
					case SUFFOCATION:
						e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 끼여 죽었습니다."));
						break;
					case DROWNING:
						e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 익사했습니다."));
						break;
					case ENTITY_EXPLOSION:
						e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 폭발했습니다."));
						break;
					case LAVA:
						e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 용암에 빠져 죽었습니다."));
						break;
					case FIRE:
					case FIRE_TICK:
						e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 노릇노릇하게 구워졌습니다."));
						break;
					default:
						e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 죽었습니다."));
						break;
				}
			}
		} else {
			e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 죽었습니다."));
		}

		if (game.isParticipating(victimPlayer)) {
			Participant victim = game.getParticipant(victimPlayer);

			e.setKeepInventory(!DeathSettings.getItemDrop());

			Bukkit.getPluginManager().callEvent(new ParticipantDeathEvent(victim));

			if (DeathSettings.getAbilityReveal()) {
				Bukkit.broadcastMessage(AbilityReveal(victim));
			}
			if (DeathSettings.getAbilityRemoval()) {
				victim.removeAbility();
			}

			Operation(victim);
		}
	}

	protected String AbilityReveal(Participant victim) {
		if (victim.hasAbility()) {
			String name = victim.getAbility().getName();
			return ChatColor.translateAlternateColorCodes('&',
					"&f[&c능력&f] &c" + victim.getPlayer().getName() + "&f님의 능력은 &e" + name + "&f" + KoreanUtil.getNeededJosa(name, KoreanUtil.Josa.이었였) + "습니다.");
		} else {
			return ChatColor.translateAlternateColorCodes('&',
					"&f[&c능력&f] &c" + victim.getPlayer().getName() + "&f님은 능력이 없습니다.");
		}
	}

	protected void Operation(Participant victim) {
		switch (DeathSettings.getOperation()) {
			case 탈락:
				Eliminate(victim);
				deadPlayers.add(victim.getPlayer().getUniqueId());
				break;
			case 관전모드:
				victim.getPlayer().setGameMode(GameMode.SPECTATOR);
				deadPlayers.add(victim.getPlayer().getUniqueId());
				break;
			case 없음:
				break;
		}
	}

	/**
	 * 사망한 유저 UUID 목록
	 */
	protected final ArrayList<UUID> deadPlayers = new ArrayList<>();

	/**
	 * Operation 콘피그에 따라 탈락, 관전모드 설정 또는 아무 행동도 하지 않을 수 있습니다.
	 *
	 * @param participant 작업을 처리할 참가자
	 */
	public final void Eliminate(Participant participant) {
		Player player = participant.getPlayer();
		player.kickPlayer(Messager.defaultPrefix + "\n" + ChatColor.translateAlternateColorCodes('&', "&f탈락하셨습니다."));
		Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c" + player.getName() + "&f님이 탈락하셨습니다."));
	}

	/**
	 * 플레이어의 사망 여부를 확인합니다.
	 */
	public final boolean isDead(Player player) {
		return deadPlayers.contains(player.getUniqueId());
	}

	/**
	 * 플레이어의 사망 여부를 확인합니다.
	 */
	public final boolean isDead(UUID uuid) {
		return deadPlayers.contains(uuid);
	}

	@Override
	public void update(AbstractGame.GAME_UPDATE update) {
		if (update.equals(AbstractGame.GAME_UPDATE.END)) {
			HandlerList.unregisterAll(this);
		}
	}

	public interface Handler {
		DeathManager getDeathManager();
	}

}
