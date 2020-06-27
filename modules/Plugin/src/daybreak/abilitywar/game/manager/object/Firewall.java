package daybreak.abilitywar.game.manager.object;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.Configuration.Settings.DeathSettings;
import daybreak.abilitywar.config.enums.OnDeath;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.GameUpdate;
import daybreak.abilitywar.game.AbstractGame.Observer;
import daybreak.abilitywar.game.manager.SpectatorManager;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

/**
 * 게임 진행 중 접속을 제한하는 방화벽
 *
 * @author Daybreak 새벽
 */
public class Firewall implements Listener, Observer {

	private final AbstractGame game;
	private final DeathManager.Handler handler;
	private final Set<Predicate<Player>> predicates = new HashSet<>();

	{
		registerPredicate(new Predicate<Player>() {
			@Override
			public boolean test(Player player) {
				return !player.isOp() && !game.isParticipating(player) && !SpectatorManager.isSpectator(player.getName());
			}
		});
	}

	/**
	 * {@link Predicate<Player>}를 등록합니다.
	 * {@link Predicate<Player>}의 {@link Predicate#test} 메소드를 사용했을 때
	 * false를 반환하는 대상은 방화벽 차단에서 제외됩니다.
	 * <p>
	 * 기본적으로 등록되어있는 {@link Predicate<Player>}은 OP 권한이 없고, 게임에 참여중이지 않으며, 관전 중이 아닌 경우에 false를 반환합니다.
	 */
	public void registerPredicate(Predicate<Player> predicate) {
		predicates.add(predicate);
	}

	public Firewall(AbstractGame game, DeathManager.Handler handler) {
		this.game = game;
		this.handler = handler;
		game.attachObserver(this);
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
	}

	@Override
	public void update(GameUpdate update) {
		if (update.equals(GameUpdate.END)) {
			HandlerList.unregisterAll(this);
		}
	}

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent e) {
		Player p = e.getPlayer();
		if (Settings.getFirewall()) {
			for (Predicate<Player> predicate : predicates) {
				if (!predicate.test(p)) return;
			}
			e.disallow(Result.KICK_OTHER, "§2《§aAbilityWar§2》"
					+ "\n" + "§f게임 진행중이므로 접속할 수 없습니다.");
		}
		if (DeathSettings.getOperation().equals(OnDeath.탈락)) {
			if (handler.getDeathManager().isExcluded(p) && !p.isOp()) {
				e.disallow(Result.KICK_OTHER, "§2《§aAbilityWar§2》"
						+ "\n" + "§f탈락하셨습니다.");
			}
		}
	}

	public interface Handler {
		Firewall getFirewall();
	}

}
