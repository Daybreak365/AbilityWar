package daybreak.abilitywar.game.manager.object;

import com.google.common.base.Preconditions;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration.Flag;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration.Tip;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.GameTimer;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.list.lite.LiteAbilities;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil.Josa;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.naming.OperationNotSupportedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbilitySelect extends GameTimer {

	private final AbstractGame game;

	public AbilitySelect(AbstractGame game, Collection<? extends Participant> selectors, int changeCount) {
		game.super(TaskType.INFINITE, -1);
		this.game = game;
		this.selectorData = new SelectorData(Preconditions.checkNotNull(filterSelectors(selectors)), changeCount);
	}

	private final SelectorData selectorData;
	private Map<Participant, Integer> selectorMap = null;

	protected Collection<? extends Participant> filterSelectors(Collection<? extends Participant> selectors) {
		return selectors;
	}

	/**
	 * 능력을 선택할 {@link Participant} 목록을 반환합니다.
	 */
	public final Collection<? extends Participant> getSelectors() {
		return Collections.unmodifiableCollection(selectorData.selectors);
	}

	protected void onChange(final Participant participant) {
		final Player player = participant.getPlayer();
		if (!hasDecided(participant)) {
			player.sendMessage("§a능력이 변경되었습니다. §e/aw check§f로 확인하세요.");
			player.sendMessage("§e/aw yes §f명령어로 능력을 확정하거나, §e/aw no §f명령어로 능력을 변경하세요.");
		} else {
			player.sendMessage("§a능력이 변경되었습니다. §e/aw check§f로 확인하세요.");
		}
		if (participant.hasAbility()) {
			final Tip tip = participant.getAbility().getRegistration().getTip();
			if (tip != null) {
				player.sendMessage("§e/aw abtip§f으로 능력 팁을 확인하세요.");
			}
			if (participant.getAbility().hasSummarize()) {
				player.sendMessage("§e/aw check s§8(§7um§8)§f으로 능력 요약을 확인하세요.");
			}
		}
	}

	protected void onDecision(final Participant participant) {
		final Player player = participant.getPlayer();
		player.sendMessage("§6능력이 확정되셨습니다. 다른 플레이어를 기다려주세요.");
		Bukkit.broadcastMessage("§e" + player.getName() + "§f님이 능력을 확정하셨습니다.");
		Bukkit.broadcastMessage("§a남은 인원 §7: §f" + getLeftPlayerCount() + "명");
	}

	protected void onSkip(final String admin) {
		Bukkit.broadcastMessage(
				admin != null ? (
						"§f관리자 §e" + admin + "§f" + KoreanUtil.getJosa(admin.replaceAll("_", ""), Josa.이가) + " 모든 참가자의 능력을 강제로 확정했습니다."
				) : "§e모든 참가자§f의 능력이 강제로 확정되었습니다."
		);
	}

	@Override
	public void onStart() {
		started = true;
		selectorMap = selectorData.newMap();
		drawAbility(selectorMap.keySet());
		if (Settings.isAutoSkipEnabled()) {
			new AutoSkip();
		}
	}

	/**
	 * 능력 선택 중 {@link Participant}의 능력을 변경합니다.
	 */
	public final void alterAbility(Participant participant) {
		if (isSelector(participant) && !hasDecided(participant)) {
			updateChangeCount(participant, selectorMap.get(participant) - 1);
			if (changeAbility(participant)) {
				onChange(participant);
			}
		}
	}

	/**
	 * {@link Participant}의 능력 선택 여부를 반환합니다. 능력을 선택중인 {@link Participant}가 아닐 경우
	 * false를 반환합니다.
	 */
	public final boolean hasDecided(Participant participant) {
		return selectorMap != null && selectorMap.containsKey(participant) && selectorMap.get(participant) <= 0;
	}

	public final void reset() {
		setEnded(false);
		stop(true);
		start();
	}

	/**
	 * {@link Participant}에게 남은 능력 변경 횟수를 설정합니다.
	 */
	private void updateChangeCount(final Participant participant, int count) {
		count = Math.max(0, count);
		selectorMap.put(participant, count);
		if (count == 0) {
			onDecision(participant);
		}
	}

	protected int getLeftPlayerCount() {
		int count = 0;
		for (final Participant participant : getSelectors()) if (!hasDecided(participant)) count++;
		return count;
	}

	/**
	 * 참가자들의 초기 능력을 설정합니다.
	 */
	protected abstract void drawAbility(Collection<? extends Participant> selectors);

	/**
	 * 모든 참가자의 능력을 강제로 결정합니다.
	 *
	 * @param admin 출력할 관리자의 이름
	 */
	public final void skip(final String admin) {
		if (this.stop(false)) {
			for (Participant participant : getSelectors()) {
				if (!hasDecided(participant)) {
					decideAbility(participant);
				}
			}

			onSkip(admin);
		}
	}

	public final void skip() {
		this.skip(null);
	}

	/**
	 * 능력 선택 중 {@link Participant}의 능력을 변경합니다.
	 */
	protected abstract boolean changeAbility(Participant participant);

	/**
	 * 능력 선택 중 {@link Participant}의 능력을 결정합니다. 능력을 결정하면 더 이상 능력을 변경할 수 없습니다.
	 */
	public final void decideAbility(Participant participant) {
		if (isSelector(participant))
			updateChangeCount(participant, 0);
	}

	/**
	 * {@link Participant}가 능력 선택에 참여한 참가자인지의 여부를 반환합니다.
	 */
	public final boolean isSelector(Participant participant) {
		return selectorMap != null && selectorMap.containsKey(participant);
	}

	@Override
	public void run(int count) {
		if (!hasEveryoneSelected()) {
			if (count % 20 == 0) {
				Bukkit.broadcastMessage("§c아직 능력을 확정하지 않은 참가자가 있습니다.");
				Bukkit.broadcastMessage("§4/aw yes §c또는 §4/aw no §c명령어로 능력을 확정해주세요.");
			}
		} else {
			this.stop(false);
		}
	}

	private static final class SelectorData {

		private final Collection<? extends Participant> selectors;
		private final int changeCount;

		private SelectorData(Collection<? extends Participant> selectors, int changeCount) {
			this.selectors = selectors;
			this.changeCount = changeCount;
		}

		private Map<Participant, Integer> newMap() {
			Map<Participant, Integer> map = new HashMap<>();
			for (Participant participant : selectors) {
				map.put(participant, changeCount);
			}
			return map;
		}

	}

	@Override
	public void onEnd() {
		ended = true;
	}

	/**
	 * 능력을 선택중인 모든 참가자가 능력을 결정했는지의 여부를 반환합니다.
	 */
	private boolean hasEveryoneSelected() {
		for (Participant key : selectorMap.keySet())
			if (!hasDecided(key))
				return false;
		return true;
	}

	private boolean started = false;
	private boolean ended = false;

	public boolean isStarted() {
		return started;
	}

	public void setEnded(boolean ended) {
		this.ended = ended;
	}

	public boolean isEnded() {
		return ended;
	}

	public interface Handler {
		AbilitySelect getAbilitySelect();

		AbilitySelect newAbilitySelect();

		void startAbilitySelect() throws OperationNotSupportedException;
	}

	public interface AbilityCollector {
		AbilityCollector EVERY_ABILITY_EXCLUDING_BLACKLISTED = new AbilityCollector() {
			@Override
			public List<Class<? extends AbilityBase>> collect(Class<? extends AbstractGame> game) {
				final List<Class<? extends AbilityBase>> abilities = new ArrayList<>();
				for (AbilityRegistration registration : (Settings.isLiteModeEnabled() ? LiteAbilities.values() : AbilityList.values())) {
					if (!Settings.isBlacklisted(registration.getManifest().name()) && registration.isAvailable(game) && (Settings.isUsingBetaAbility() || !registration.hasFlag(Flag.BETA))) {
						abilities.add(registration.getAbilityClass());
					}
				}
				return abilities;
			}
		};

		List<Class<? extends AbilityBase>> collect(final Class<? extends AbstractGame> game);
	}

	public class AutoSkip extends GameTimer implements Listener {

		private final BossBar bossBar;
		private final SimpleTimer.Observer observer = new Observer() {
			@Override
			public void onSilentEnd() {
				stop(true);
			}

			@Override
			public void onEnd() {
				stop(true);
			}
		};

		public AutoSkip() {
			game.super(TaskType.REVERSE, Settings.getAutoSkipTime());
			this.bossBar = Bukkit.createBossBar("§f자동 스킵까지§7: §3" + getMaximumCount() + "초", BarColor.BLUE, BarStyle.SOLID);
			Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
			AbilitySelect.this.attachObserver(observer);
			start();
		}

		@Override
		protected void onStart() {
			Bukkit.getOnlinePlayers().forEach(bossBar::addPlayer);
		}

		@Override
		protected void run(final int count) {
			bossBar.setProgress((double) count / getMaximumCount());
			bossBar.setTitle("§f자동 스킵까지§7: §3" + count + "초");
		}

		@Override
		protected void onEnd() {
			AbilitySelect.this.skip();
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			HandlerList.unregisterAll(this);
			bossBar.removeAll();
			AbilitySelect.this.detachObserver(observer);
		}

		@EventHandler
		private void onPlayerJoin(final PlayerJoinEvent e) {
			bossBar.addPlayer(e.getPlayer());
		}

		@EventHandler
		private void onPlayerQuit(final PlayerQuitEvent e) {
			bossBar.removePlayer(e.getPlayer());
		}

	}

}
