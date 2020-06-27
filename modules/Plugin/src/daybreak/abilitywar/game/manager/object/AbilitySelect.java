package daybreak.abilitywar.game.manager.object;

import com.google.common.base.Preconditions;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.GameTimer;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.AbilityList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.OperationNotSupportedException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public abstract class AbilitySelect extends GameTimer {

	public AbilitySelect(AbstractGame game, Collection<? extends Participant> selectors, int changeCount) {
		game.super(TaskType.INFINITE, -1);
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

	@Override
	public void onStart() {
		started = true;
		selectorMap = selectorData.newMap();
		drawAbility(selectorMap.keySet());
	}

	/**
	 * 능력 선택 중 {@link Participant}의 능력을 변경합니다.
	 */
	public final void alterAbility(Participant participant) {
		if (isSelector(participant) && !hasDecided(participant)) {
			updateRemainingChangeCount(participant, selectorMap.get(participant) - 1);
			if (changeAbility(participant)) {
				Player p = participant.getPlayer();

				if (!hasDecided(participant)) {
					p.sendMessage(new String[]{
							"§a능력이 할당되었습니다. §e/aw check§f로 확인 할 수 있습니다.",
							"§e/aw yes §f명령어를 사용하여 능력을 확정합니다.",
							"§e/aw no §f명령어를 사용하여 능력을 변경합니다."
					});
				} else {
					p.sendMessage("§a당신의 능력이 변경되었습니다. §e/aw check§f로 확인 할 수 있습니다.");
				}
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
	private void updateRemainingChangeCount(Participant participant, int count) {
		selectorMap.put(participant, count);

		if (count == 0) {
			Player p = participant.getPlayer();

			p.sendMessage("§6능력이 확정되셨습니다. 다른 플레이어를 기다려주세요.");

			Bukkit.broadcastMessage("§e" + p.getName() + "§f님이 능력을 확정하셨습니다.");
			Bukkit.broadcastMessage("§a남은 인원 §7: §f" + getLeftPlayers() + "명");
		}
	}

	private int getLeftPlayers() {
		int count = 0;
		for (Participant participant : getSelectors()) if (!hasDecided(participant)) count++;
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
	public final void Skip(String admin) {
		for (Participant p : getSelectors())
			if (!hasDecided(p))
				decideAbility(p);

		Bukkit.broadcastMessage("§f관리자 §e" + admin + "§f님이 모든 플레이어의 능력을 강제로 확정시켰습니다.");
		this.stop(false);
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
			updateRemainingChangeCount(participant, 0);
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
				Bukkit.broadcastMessage("§c아직 모든 유저가 능력을 확정하지 않았습니다.");
				Bukkit.broadcastMessage("§c/aw yes 또는 /aw no 명령어로 능력을 확정해주세요.");
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

	public interface AbilitySelectStrategy {
		AbilitySelectStrategy EVERY_ABILITY_EXCLUDING_BLACKLISTED = new AbilitySelectStrategy() {
			@Override
			public List<Class<? extends AbilityBase>> getAbilities() {
				ArrayList<Class<? extends AbilityBase>> abilities = new ArrayList<>();
				for (String name : AbilityList.nameValues()) {
					if (!Configuration.Settings.isBlacklisted(name)) {
						abilities.add(AbilityList.getByString(name));
					}
				}
				return abilities;
			}
		};

		List<Class<? extends AbilityBase>> getAbilities();
	}

}
