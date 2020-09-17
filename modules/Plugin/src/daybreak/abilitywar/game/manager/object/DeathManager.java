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
import daybreak.abilitywar.utils.base.minecraft.entity.death.Deaths;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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

	@EventHandler(priority = EventPriority.HIGHEST)
	public final void onPlayerDeath(final PlayerDeathEvent e) {
		final Player dead = e.getEntity();
		e.setDeathMessage(getDeathMessage(dead));

		if (autoRespawn) {
			new BukkitRunnable() {
				@Override
				public void run() {
					NMS.respawn(dead);
				}
			}.runTaskLater(AbilityWar.getPlugin(), 2L);
		}
		if (!game.isGameStarted()) return;
		if (game.isParticipating(dead)) {
			final Participant victim = game.getParticipant(dead);

			final ParticipantDeathEvent event = new ParticipantDeathEvent(victim);
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

	protected String getRevealMessage(final Participant victim) {
		if (victim.hasAbility()) {
			final String name = victim.getAbility().getDisplayName();
			return "§f[§c능력§f] §c" + victim.getPlayer().getName() + "§f님의 능력은 §e" + name + "§f" + KoreanUtil.getJosa(name, KoreanUtil.Josa.이었였) + "습니다.";
		} else {
			return "§f[§c능력§f] §c" + victim.getPlayer().getName() + "§f님은 능력이 없습니다.";
		}
	}

	protected String getDeathMessage(final Player dead) {
		return Deaths.getMessage(dead);
	}

	public void Operation(final Participant victim) {
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
							NMS.respawn(victim.getPlayer());
						}
					}.runTaskLater(AbilityWar.getPlugin(), 2L);
				}
				break;
			case 없음:
				if (autoRespawn) {
					new BukkitRunnable() {
						@Override
						public void run() {
							NMS.respawn(victim.getPlayer());
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
