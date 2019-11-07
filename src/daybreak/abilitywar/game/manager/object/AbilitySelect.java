package daybreak.abilitywar.game.manager.object;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.config.AbilityWarSettings;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.thread.TimerBase;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;


import static daybreak.abilitywar.utils.Validate.notNull;

public abstract class AbilitySelect extends TimerBase {

	private final Map<Participant, Integer> selectors;

	public AbilitySelect(int changeCount) {
		Map<Participant, Integer> selectors = new HashMap<>();
		for (Participant p : notNull(initSelectors()))
			selectors.put(p, changeCount);
		this.selectors = selectors;
		drawAbility(getSelectors());
		startTimer();
	}

	/**
	 * 능력을 선택할 {@link Participant} 목록을 설정합니다.
	 *
	 * null을 반환하지 않습니다.
	 */
	protected abstract Collection<Participant> initSelectors();

	/**
	 * 능력을 선택할 {@link Participant} 목록을 반환합니다.
	 */
	public final Collection<Participant> getSelectors() {
		return selectors.keySet();
	}

	/**
	 * {@link Participant}에게 남은 능력 변경 횟수를 설정합니다.
	 */
	private void setRemainingChangeCount(Participant participant, int count) {
		selectors.put(participant, count);

		if (count == 0) {
			Player p = participant.getPlayer();

			p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6능력이 확정되셨습니다. 다른 플레이어를 기다려주세요."));

			for (String m : Messager.asList(
					ChatColor.translateAlternateColorCodes('&', "&e" + p.getName() + "&f님이 능력을 확정하셨습니다."),
					ChatColor.translateAlternateColorCodes('&', "&a남은 인원 &7: &f" + getLeftPlayersCount() + "명"))) {
				Bukkit.broadcastMessage(m);
			}
		}
	}

	/**
	 * 능력을 아직 결정하지 않은 참가자의 수를 반환합니다.
	 */
	private int getLeftPlayersCount() {
		int count = 0;
		for (Participant p : getSelectors())
			if (!hasDecided(p))
				count++;
		return count;
	}

	/**
	 * {@link Participant}의 능력 선택 여부를 반환합니다. 능력을 선택중인 {@link Participant}가 아닐 경우
	 * false를 반환합니다.
	 */
	public final boolean hasDecided(Participant participant) {
		if (selectors.containsKey(participant)) {
			return selectors.get(participant) <= 0;
		} else {
			return false;
		}
	}

	/**
	 * 능력 선택 중 {@link Participant}의 능력을 변경합니다.
	 */
	public final void alterAbility(Participant participant) {
		if (isSelector(participant) && !hasDecided(participant)) {
			setRemainingChangeCount(participant, selectors.get(participant) - 1);
			if (changeAbility(participant)) {
				Player p = participant.getPlayer();

				if (!hasDecided(participant)) {
					p.sendMessage(new String[] {
							ChatColor.translateAlternateColorCodes('&',
									"&a당신에게 능력이 할당되었습니다. &e/ability check&f로 확인 할 수 있습니다."),
							ChatColor.translateAlternateColorCodes('&', "&e/ability yes &f명령어를 사용하면 능력을 확정합니다."),
							ChatColor.translateAlternateColorCodes('&', "&e/ability no &f명령어를 사용하면 능력을 변경할 수 있습니다.") });
				} else {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&',
							"&a당신의 능력이 변경되었습니다. &e/ability check&f로 확인 할 수 있습니다."));
				}
			}
		}
	}

	/**
	 * 참가자들의 초기 능력을 설정합니다.
	 */
	protected abstract void drawAbility(Collection<Participant> selectors);

	/**
	 * 능력 선택 중 {@link Participant}의 능력을 변경합니다.
	 */
	protected abstract boolean changeAbility(Participant participant);

	/**
	 * 능력 선택 중 {@link Participant}의 능력을 결정합니다. 능력을 결정하면 더 이상 능력을 변경할 수 없습니다.
	 */
	public final void decideAbility(Participant participant) {
		if (isSelector(participant))
			setRemainingChangeCount(participant, 0);
	}

	/**
	 * {@link Participant}가 능력 선택에 참여한 참가자인지의 여부를 반환합니다.
	 */
	public final boolean isSelector(Participant participant) {
		return selectors.containsKey(participant);
	}

	/**
	 * 모든 참가자의 능력을 강제로 결정합니다.
	 * 
	 * @param admin 출력할 관리자의 이름
	 */
	public final void Skip(String admin) {
		for (Participant p : getSelectors())
			if (!hasDecided(p))
				decideAbility(p);

		Bukkit.broadcastMessage(
				ChatColor.translateAlternateColorCodes('&', "&f관리자 &e" + admin + "&f님이 모든 플레이어의 능력을 강제로 확정시켰습니다."));
		this.stopTimer(false);
	}

	@Override
	public void onStart() {
	}

	@Override
	public void onProcess(int count) {
		if (!isEveryoneSelected()) {
			if (count % 20 == 0) {
				for (String m : Messager.asList(
						ChatColor.translateAlternateColorCodes('&', "&c아직 모든 유저가 능력을 확정하지 않았습니다."),
						ChatColor.translateAlternateColorCodes('&', "&c/ability yes나 /ability no 명령어로 능력을 확정해주세요."))) {
					Bukkit.broadcastMessage(m);
				}
			}
		} else {
			this.stopTimer(false);
		}
	}

	/**
	 * 능력을 선택중인 모든 참가자가 능력을 결정했는지의 여부를 반환합니다.
	 */
	private boolean isEveryoneSelected() {
		for (Participant Key : getSelectors())
			if (!hasDecided(Key))
				return false;
		return true;
	}

	@Override
	public void onEnd() {
		Ended = true;
		onSelectEnd();
	}

	protected abstract void onSelectEnd();

	private boolean Ended = false;

	public boolean isEnded() {
		return Ended;
	}

	public interface Handler {
		AbilitySelect getAbilitySelect();
	}

	public interface AbilitySelectStrategy {
		AbilitySelectStrategy EVERY_ABILITY_EXCLUDING_BLACKLISTED = new AbilitySelectStrategy() {
			@Override
			public ArrayList<Class<? extends AbilityBase>> getAbilities() {
				ArrayList<Class<? extends AbilityBase>> abilities = new ArrayList<>();
				for(String name : AbilityList.nameValues()) {
					if(!AbilityWarSettings.Settings.isBlackListed(name)) {
						abilities.add(AbilityList.getByString(name));
					}
				}
				return abilities;
			}
		};

		ArrayList<Class<? extends AbilityBase>> getAbilities();
	}

}
