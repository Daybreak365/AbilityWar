package daybreak.abilitywar.game.manager.object;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.config.Configuration.Settings.DeathSettings;
import daybreak.abilitywar.config.enums.OnDeath;
import daybreak.abilitywar.game.AbstractGame.GameUpdate;
import daybreak.abilitywar.game.AbstractGame.Observer;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.Game;
import daybreak.abilitywar.game.event.participant.ParticipantDeathEvent;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil;
import daybreak.abilitywar.utils.base.minecraft.compat.nms.NMSHandler;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Death Manager
 *
 * @author Daybreak 새벽
 */
public class DeathManager implements Listener, Observer {

	private final Game game;
	private boolean abilityReveal = DeathSettings.getAbilityReveal(), autoRespawn = DeathSettings.getAutoRespawn();
	private OnDeath operation = DeathSettings.getOperation();

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
				e.setDeathMessage("§a" + killerPlayer.getName() + "§f님이 §c" + victimPlayer.getName() + "§f님을 죽였습니다.");
			} else {
				switch (victimPlayer.getLastDamageCause().getCause()) {
					case CONTACT:
						e.setDeathMessage("§c" + victimPlayer.getName() + "§f님이 찔려 죽었습니다.");
						break;
					case FALL:
						e.setDeathMessage("§c" + victimPlayer.getName() + "§f님이 떨어져 죽었습니다.");
						break;
					case FALLING_BLOCK:
						e.setDeathMessage("§c" + victimPlayer.getName() + "§f님이 떨어지는 블록에 맞아 죽었습니다.");
						break;
					case SUFFOCATION:
						e.setDeathMessage("§c" + victimPlayer.getName() + "§f님이 끼여 죽었습니다.");
						break;
					case DROWNING:
						e.setDeathMessage("§c" + victimPlayer.getName() + "§f님이 익사했습니다.");
						break;
					case ENTITY_EXPLOSION:
						e.setDeathMessage("§c" + victimPlayer.getName() + "§f님이 폭발했습니다.");
						break;
					case LAVA:
						e.setDeathMessage("§c" + victimPlayer.getName() + "§f님이 용암에 빠져 죽었습니다.");
						break;
					case FIRE:
					case FIRE_TICK:
						e.setDeathMessage("§c" + victimPlayer.getName() + "§f님이 노릇노릇하게 구워졌습니다.");
						break;
					default:
						e.setDeathMessage("§c" + victimPlayer.getName() + "§f님이 죽었습니다.");
						break;
				}
			}
		} else {
			e.setDeathMessage("§c" + victimPlayer.getName() + "§f님이 죽었습니다.");
		}

		if (game.isParticipating(victimPlayer)) {
			Participant victim = game.getParticipant(victimPlayer);

			ParticipantDeathEvent event = new ParticipantDeathEvent(victim);
			Bukkit.getPluginManager().callEvent(event);
			if (!event.isCancelled()) {
				if (abilityReveal) Bukkit.broadcastMessage(getRevealMessage(victim));
				if (operation.getAbilityRemoval()) victim.removeAbility();

				Operation(victim);
			} else {
				e.setDeathMessage(null);
			}
		}
	}

	protected String getRevealMessage(Participant victim) {
		if (victim.hasAbility()) {
			String name = victim.getAbility().getName();
			return "§f[§c능력§f] §c" + victim.getPlayer().getName() + "§f님의 능력은 §e" + name + "§f" + KoreanUtil.getJosa(name, KoreanUtil.Josa.이었였) + "습니다.";
		} else {
			return "§f[§c능력§f] §c" + victim.getPlayer().getName() + "§f님은 능력이 없습니다.";
		}
	}

	public void Operation(Participant victim) {
		switch (operation) {
			case 탈락:
				Eliminate(victim);
				excludedPlayers.add(victim.getPlayer().getUniqueId());
				break;
			case 관전모드:
				victim.getPlayer().setGameMode(GameMode.SPECTATOR);
				excludedPlayers.add(victim.getPlayer().getUniqueId());
				if (autoRespawn) {
					new BukkitRunnable() {
						@Override
						public void run() {
							NMSHandler.getNMS().respawn(victim.getPlayer());
						}
					}.runTaskLater(AbilityWar.getPlugin(), 2L);
				}
				break;
			case 없음:
				if (autoRespawn) {
					new BukkitRunnable() {
						@Override
						public void run() {
							NMSHandler.getNMS().respawn(victim.getPlayer());
						}
					}.runTaskLater(AbilityWar.getPlugin(), 2L);
				}
				break;
		}
	}

	public DeathManager setAbilityReveal(boolean abilityReveal) {
		this.abilityReveal = abilityReveal;
		return this;
	}

	public DeathManager setAutoRespawn(boolean autoRespawn) {
		this.autoRespawn = autoRespawn;
		return this;
	}

	public DeathManager setOperation(OnDeath operation) {
		this.operation = operation != null ? operation : OnDeath.없음;
		return this;
	}

	/**
	 * 게임에서 제외된 유저 UUID 목록
	 */
	protected final Set<UUID> excludedPlayers = new HashSet<>();

	/**
	 * Operation 콘피그에 따라 탈락, 관전모드 설정 또는 아무 행동도 하지 않을 수 있습니다.
	 *
	 * @param participant 작업을 처리할 참가자
	 */
	public final void Eliminate(Participant participant) {
		Player player = participant.getPlayer();
		player.kickPlayer(Messager.defaultPrefix + "\n" + "§f탈락하셨습니다.");
		Bukkit.broadcastMessage("§c" + player.getName() + "§f님이 탈락하셨습니다.");
	}

	/**
	 * 플레이어의 게임 제외 여부를 확인합니다.
	 */
	public final boolean isExcluded(Player player) {
		return excludedPlayers.contains(player.getUniqueId());
	}

	/**
	 * 플레이어의 게임 제외 여부를 확인합니다.
	 */
	public final boolean isExcluded(UUID uuid) {
		return excludedPlayers.contains(uuid);
	}

	@Override
	public void update(GameUpdate update) {
		if (update.equals(GameUpdate.END)) {
			HandlerList.unregisterAll(this);
		}
	}

	public interface Handler {
		DeathManager getDeathManager();
		DeathManager newDeathManager();
	}

}
