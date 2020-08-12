package daybreak.abilitywar.game.list.murdermystery;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityFactory;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration.Flag;
import daybreak.abilitywar.config.Configuration.Settings.DeveloperSettings;
import daybreak.abilitywar.game.list.changeability.ChangeAbilityWar;
import daybreak.abilitywar.game.list.murdermystery.ability.Detective;
import daybreak.abilitywar.game.list.murdermystery.ability.Innocent;
import daybreak.abilitywar.game.list.murdermystery.ability.Murderer;
import daybreak.abilitywar.game.list.murdermystery.ability.jobs.innocent.Doctor;
import daybreak.abilitywar.game.list.murdermystery.ability.jobs.innocent.Police;
import daybreak.abilitywar.game.list.murdermystery.ability.jobs.murderer.AssassinMurderer;
import daybreak.abilitywar.game.list.murdermystery.ability.jobs.murderer.BlackMurderer;
import daybreak.abilitywar.game.list.standard.StandardGame;
import daybreak.abilitywar.utils.base.logging.Logger;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * {@link StandardGame}, {@link ChangeAbilityWar} 등에서 사용하는 능력자 플러그인의 기본적인 능력 목록을 관리하는 클래스입니다.
 */
public class JobList {

	private JobList() {
	}

	private static final Logger logger = Logger.getLogger(JobList.class);

	private static final Map<String, AbilityRegistration> jobs = new TreeMap<>();
	private static final Set<AbilityRegistration> registered = new HashSet<>();

	public static boolean isRegistered(String name) {
		return jobs.containsKey(name);
	}

	public static boolean isRegistered(AbilityRegistration registration) {
		return registered.contains(registration);
	}

	/**
	 * 능력을 등록합니다.
	 * <p>
	 * 능력을 등록하기 전, AbilityManifest 어노테이션이 클래스에 존재하는지,
	 * 겹치는 이름은 없는지, 생성자는 올바른지 확인해주시길 바랍니다.
	 * <p>
	 * 이미 등록된 능력일 경우 다시 등록이 되지 않습니다.
	 *
	 * @param abilityClass 능력 클래스
	 */
	private static void registerJob(final Class<? extends AbilityBase> abilityClass, final String name) {
		final AbilityRegistration registration = AbilityFactory.getRegistration(abilityClass);
		if (registration != null) {
			if (!jobs.containsKey(name)) {
				if (registration.hasFlag(Flag.BETA) && !DeveloperSettings.isEnabled()) return;
				jobs.put(name, registration);
				registered.add(registration);
			} else {
				logger.debug("§e" + abilityClass.getName() + " §f능력은 겹치는 이름이 있어 등록되지 않았습니다.");
			}
		} else {
			logger.debug("§e" + abilityClass.getName() + " §f능력은 AbilityFactory에 등록되지 않은 능력입니다.");
		}
	}

	static {
		registerJob(Murderer.class, "머더");
		registerJob(Innocent.class, "시민");
		registerJob(Detective.class, "탐정");
		registerJob(Police.class, "경찰");
		registerJob(Doctor.class, "의사");
		registerJob(AssassinMurderer.class, "암살자");
		registerJob(BlackMurderer.class, "블랙");
	}

	public static Collection<AbilityRegistration> values() {
		return Collections.unmodifiableCollection(jobs.values());
	}

	/**
	 * 등록된 능력 중 해당 이름의 능력을 반환합니다.
	 * AbilityManifest가 존재하지 않는 능력이거나 존재하지 않는 능력일 경우 null을 반환합니다.
	 *
	 * @param name 능력의 이름
	 * @return 능력 Class
	 */
	public static Class<? extends AbilityBase> getByString(String name) {
		return jobs.get(name).getAbilityClass();
	}

}