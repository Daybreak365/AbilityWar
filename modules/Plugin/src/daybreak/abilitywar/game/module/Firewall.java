package daybreak.abilitywar.game.module;

import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.Configuration.Settings.DeathSettings;
import daybreak.abilitywar.config.enums.OnDeath;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.manager.SpectatorManager;
import daybreak.abilitywar.utils.base.Messager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * 방화벽 모듈
 * @author Daybreak 새벽
 */
@ModuleBase(Firewall.class)
public class Firewall implements ListenerModule {

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
	}

	@EventHandler
	private void onPlayerLogin(PlayerLoginEvent e) {
		if (Settings.getFirewall()) {
			for (Predicate<Player> predicate : predicates) {
				if (!predicate.test(e.getPlayer())) return;
			}
			e.disallow(Result.KICK_OTHER, Messager.defaultPrefix + "\n" + "§f게임 진행 중이므로 접속할 수 없습니다.");
		}
		if (DeathSettings.getOperation().equals(OnDeath.탈락) && handler.getDeathManager().isExcluded(e.getPlayer()) && !e.getPlayer().isOp()) {
			e.disallow(Result.KICK_OTHER, Messager.defaultPrefix + "\n" + "§f탈락했습니다.");
		}
	}

	public interface Handler {
		Firewall getFirewall();
	}

}
