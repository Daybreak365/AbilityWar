package daybreak.abilitywar.game.list.murdermystery;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityFactory;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration.Flag;
import daybreak.abilitywar.config.Configuration.Settings.AprilSettings;
import daybreak.abilitywar.game.list.murdermystery.ability.Detective;
import daybreak.abilitywar.game.list.murdermystery.ability.Innocent;
import daybreak.abilitywar.game.list.murdermystery.ability.Murderer;
import daybreak.abilitywar.game.list.murdermystery.ability.jobs.innocent.Doctor;
import daybreak.abilitywar.game.list.murdermystery.ability.jobs.innocent.Police;
import daybreak.abilitywar.game.list.murdermystery.ability.jobs.murderer.AssassinMurderer;
import daybreak.abilitywar.game.list.murdermystery.ability.jobs.murderer.BlackMurderer;
import daybreak.abilitywar.game.list.murdermystery.ability.jobs.murderer.SniperMurderer;
import daybreak.abilitywar.utils.base.logging.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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

	private static void registerJob(final Class<? extends AbilityBase> abilityClass, final String name) {
		final AbilityRegistration registration = AbilityFactory.getRegistration(abilityClass);
		if (registration != null) {
			if (!jobs.containsKey(name)) {
				if (registration.hasFlag(Flag.BETA) && !AprilSettings.isEnabled()) return;
				jobs.put(name, registration);
				registered.add(registration);
			} else {
				logger.debug("§e" + abilityClass.getName() + " §f능력은 겹치는 이름이 있어 등록되지 않았습니다.");
			}
		} else {
			logger.debug("§e" + abilityClass.getName() + " §f능력은 AbilityFactory에 등록되지 않은 능력입니다.");
		}
	}

	public static void registerJob(final Class<? extends AbilityBase> abilityClass, final String name, CharacterType type) {
		final AbilityRegistration registration = AbilityFactory.getRegistration(abilityClass);
		if (registration != null) {
			if (!jobs.containsKey(name)) {
				if (registration.hasFlag(Flag.BETA) && !AprilSettings.isEnabled()) return;
				jobs.put(name, registration);
				registered.add(registration);
				(type == CharacterType.INNOCENT ? MurderMystery.JOB_ABILITIES : MurderMystery.MURDER_JOB_ABILITIES).add(registration);
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
		registerJob(Police.class, "경찰", CharacterType.INNOCENT);
		registerJob(Doctor.class, "의사", CharacterType.INNOCENT);
		registerJob(AssassinMurderer.class, "암살자", CharacterType.MURDER);
		registerJob(BlackMurderer.class, "블랙", CharacterType.MURDER);
		registerJob(SniperMurderer.class, "스나이퍼", CharacterType.MURDER);
	}

	public static Collection<AbilityRegistration> values() {
		return Collections.unmodifiableCollection(jobs.values());
	}

	public static Class<? extends AbilityBase> getByString(String name) {
		return jobs.get(name).getAbilityClass();
	}

}