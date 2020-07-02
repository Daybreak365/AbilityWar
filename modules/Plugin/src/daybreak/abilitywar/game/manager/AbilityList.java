package daybreak.abilitywar.game.manager;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityFactory;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration.Flag;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.list.Void;
import daybreak.abilitywar.ability.list.*;
import daybreak.abilitywar.config.Configuration.Settings.DeveloperSettings;
import daybreak.abilitywar.game.list.changeability.ChangeAbilityWar;
import daybreak.abilitywar.game.list.standard.DefaultGame;
import daybreak.abilitywar.utils.base.logging.Logger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * {@link DefaultGame}, {@link ChangeAbilityWar} 등에서 사용하는 능력자 플러그인의 기본적인 능력 목록을 관리하는 클래스입니다.
 */
public class AbilityList {

	private AbilityList() {
	}

	private static final Logger logger = Logger.getLogger(AbilityList.class);

	private static final Map<String, AbilityRegistration> abilities = new TreeMap<>();

	public static boolean isRegistered(String name) {
		return abilities.containsKey(name);
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
	public static void registerAbility(Class<? extends AbilityBase> abilityClass) {
		AbilityRegistration registration = AbilityFactory.getRegistration(abilityClass);
		if (registration != null) {
			String name = registration.getManifest().name();
			if (!abilities.containsKey(name)) {
				if (registration.hasFlag(Flag.BETA) && !DeveloperSettings.isEnabled()) return;
				abilities.put(name, registration);
			} else {
				logger.debug("§e" + abilityClass.getName() + " §f능력은 겹치는 이름이 있어 등록되지 않았습니다.");
			}
		} else {
			logger.debug("§e" + abilityClass.getName() + " §f능력은 AbilityFactory에 등록되지 않은 능력입니다.");
		}
	}

	static {
		registerAbility(Assassin.class);
		registerAbility(Feather.class);
		registerAbility(Demigod.class);
		registerAbility(FastRegeneration.class);
		registerAbility(EnergyBlocker.class);
		registerAbility(DiceGod.class);
		registerAbility(Ares.class);
		registerAbility(Zeus.class);
		registerAbility(Berserker.class);
		registerAbility(Zombie.class);
		registerAbility(Terrorist.class);
		registerAbility(Yeti.class);
		registerAbility(Gladiator.class);
		registerAbility(Chaos.class);
		registerAbility(Void.class);
		registerAbility(DarkVision.class);
		registerAbility(HigherBeing.class);
		registerAbility(VictoryBySword.class);
		registerAbility(FireFightWithFire.class);
		registerAbility(Hacker.class);
		registerAbility(Muse.class);
		registerAbility(Stalker.class);
		registerAbility(Flora.class);
		registerAbility(ShowmanShip.class);
		registerAbility(Virtus.class);
		registerAbility(Nex.class);
		registerAbility(Ira.class);
		registerAbility(Clown.class);
		registerAbility(Magician.class);
		registerAbility(Emperor.class);
		registerAbility(Pumpkin.class);
		registerAbility(Virus.class);
		registerAbility(Hermit.class);
		registerAbility(DevilBoots.class);
		registerAbility(BombArrow.class);
		registerAbility(Brewer.class);
		registerAbility(Imprison.class);
		registerAbility(SuperNova.class);
		registerAbility(Celebrity.class);
		registerAbility(ExpertOfFall.class);
		registerAbility(Curse.class);
		registerAbility(TimeRewind.class);
		registerAbility(Khazhad.class);
		registerAbility(Sniper.class);
		registerAbility(JellyFish.class);
		registerAbility(Lazyness.class);
		registerAbility(Vampire.class);
		registerAbility(PenetrationArrow.class);
		registerAbility(SoulEncroach.class);
		registerAbility(Hedgehog.class);
		registerAbility(ReligiousLeader.class);
		registerAbility(Kidnap.class);
		registerAbility(Flector.class);
		registerAbility(Ghost.class);
		registerAbility(Lunar.class);
		registerAbility(Apology.class);
	}

	public static Collection<AbilityRegistration> values() {
		return Collections.unmodifiableCollection(abilities.values());
	}

	public static List<String> nameValues() {
		return new ArrayList<>(abilities.keySet());
	}

	public static List<String> nameValues(Rank rank) {
		List<String> values = new ArrayList<>();
		for (AbilityRegistration registration : abilities.values()) {
			if (registration != null) {
				AbilityManifest manifest = registration.getManifest();
				if (manifest.rank().equals(rank)) {
					values.add(manifest.name());
				}
			}
		}
		return values;
	}

	public static List<String> nameValues(Species species) {
		List<String> values = new ArrayList<>();
		for (AbilityRegistration registration : abilities.values()) {
			if (registration != null) {
				AbilityManifest manifest = registration.getManifest();
				if (manifest.species().equals(species)) {
					values.add(manifest.name());
				}
			}
		}
		return values;
	}

	/**
	 * 등록된 능력 중 해당 이름의 능력을 반환합니다.
	 * AbilityManifest가 존재하지 않는 능력이거나 존재하지 않는 능력일 경우 null을 반환합니다.
	 *
	 * @param name 능력의 이름
	 * @return 능력 Class
	 */
	public static Class<? extends AbilityBase> getByString(String name) {
		return abilities.get(name).getAbilityClass();
	}

}