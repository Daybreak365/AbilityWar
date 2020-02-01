package daybreak.abilitywar.game.manager;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityFactory;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.list.Void;
import daybreak.abilitywar.ability.list.*;
import daybreak.abilitywar.game.games.changeability.ChangeAbilityWar;
import daybreak.abilitywar.game.games.standard.DefaultGame;
import daybreak.abilitywar.utils.Messager;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * {@link DefaultGame}, {@link ChangeAbilityWar} 등에서 사용하는 능력자 플러그인의 기본적인 능력 목록을 관리하는 클래스입니다.
 */
public class AbilityList {

	private AbilityList() {
	}

	private static final Messager messager = new Messager();
	private static final Map<String, Class<? extends AbilityBase>> abilities = new TreeMap<>();

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
		if (!abilities.containsValue(abilityClass)) {
			AbilityFactory.AbilityRegistration registration = AbilityFactory.getRegisteration(abilityClass);
			if (registration != null) {
				String name = registration.getManifest().Name();
				if (!abilities.containsKey(name)) {
					abilities.put(name, abilityClass);
				} else {
					messager.sendConsoleMessage(ChatColor.translateAlternateColorCodes('&', "&e" + abilityClass.getName() + " &f능력은 겹치는 이름이 있어 등록되지 않았습니다."));
				}
			} else {
				messager.sendConsoleMessage(ChatColor.translateAlternateColorCodes('&', "&e" + abilityClass.getName() + " &f능력은 AbilityFactory에 등록되지 않은 능력입니다."));
			}
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
		registerAbility(BlackCandle.class);
		registerAbility(FireFightWithFire.class);
		registerAbility(Hacker.class);
		registerAbility(Muse.class);
		registerAbility(Stalker.class);
		registerAbility(Flora.class);
		registerAbility(ShowmanShip.class);
		registerAbility(Virtus.class);
		registerAbility(Nex.class);
		registerAbility(Ira.class);
		registerAbility(OnlyOddNumber.class);
		registerAbility(Clown.class);
		registerAbility(TheMagician.class);
		registerAbility(TheEmperor.class);
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
		registerAbility(Reaper.class);
		registerAbility(Hedgehog.class);
		registerAbility(ReligiousLeader.class);
		registerAbility(Kidnap.class);
	}

	public static List<String> nameValues() {
		return new ArrayList<>(abilities.keySet());
	}

	public static List<String> nameValues(Rank rank) {
		List<String> values = new ArrayList<>();
		for (Class<? extends AbilityBase> abilityClass : abilities.values()) {
			AbilityFactory.AbilityRegistration registration = AbilityFactory.getRegisteration(abilityClass);
			if (registration != null) {
				AbilityManifest manifest = registration.getManifest();
				if (manifest.Rank().equals(rank)) {
					values.add(manifest.Name());
				}
			}
		}
		return values;
	}

	public static List<String> nameValues(Species species) {
		List<String> values = new ArrayList<>();
		for (Class<? extends AbilityBase> abilityClass : abilities.values()) {
			AbilityFactory.AbilityRegistration registration = AbilityFactory.getRegisteration(abilityClass);
			if (registration != null) {
				AbilityManifest manifest = registration.getManifest();
				if (manifest.Species().equals(species)) {
					values.add(manifest.Name());
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
		return abilities.get(name);
	}

}