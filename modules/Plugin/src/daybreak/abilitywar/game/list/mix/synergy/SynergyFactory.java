package daybreak.abilitywar.game.list.mix.synergy;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityFactory;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.ability.list.Ares;
import daybreak.abilitywar.ability.list.Assassin;
import daybreak.abilitywar.ability.list.Chaos;
import daybreak.abilitywar.ability.list.DevilBoots;
import daybreak.abilitywar.ability.list.Imprison;
import daybreak.abilitywar.ability.list.Khazhad;
import daybreak.abilitywar.ability.list.Muse;
import daybreak.abilitywar.ability.list.Nex;
import daybreak.abilitywar.ability.list.PenetrationArrow;
import daybreak.abilitywar.ability.list.Sniper;
import daybreak.abilitywar.ability.list.Stalker;
import daybreak.abilitywar.ability.list.SuperNova;
import daybreak.abilitywar.ability.list.Terrorist;
import daybreak.abilitywar.ability.list.TimeRewind;
import daybreak.abilitywar.ability.list.Virus;
import daybreak.abilitywar.ability.list.Yeti;
import daybreak.abilitywar.game.list.mix.synergy.list.AbsoluteZero;
import daybreak.abilitywar.game.list.mix.synergy.list.Bind;
import daybreak.abilitywar.game.list.mix.synergy.list.Bless;
import daybreak.abilitywar.game.list.mix.synergy.list.DeathGrasp;
import daybreak.abilitywar.game.list.mix.synergy.list.DoubleSniper;
import daybreak.abilitywar.game.list.mix.synergy.list.EventHorizon;
import daybreak.abilitywar.game.list.mix.synergy.list.FlameMan;
import daybreak.abilitywar.game.list.mix.synergy.list.Meteor;
import daybreak.abilitywar.game.list.mix.synergy.list.NexAssassin;
import daybreak.abilitywar.game.list.mix.synergy.list.Pandemic;
import daybreak.abilitywar.game.list.mix.synergy.list.PenetrationSniper;
import daybreak.abilitywar.game.list.mix.synergy.list.RocketLauncher;
import daybreak.abilitywar.game.list.mix.synergy.list.TimeLoop;
import daybreak.abilitywar.utils.base.collect.Pair;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * {@link Synergy}를 기반으로 하는 모든 능력을 관리하는 클래스입니다.
 */
public class SynergyFactory {

	private static final Table<AbilityRegistration, AbilityRegistration, AbilityRegistration> synergies = HashBasedTable.create();
	private static final Map<AbilityRegistration, Pair<AbilityRegistration, AbilityRegistration>> synergyBases = new HashMap<>();
	private static final Map<String, AbilityRegistration> usedNames = new HashMap<>();

	static {
		registerSynergy(SuperNova.class, Virus.class, Pandemic.class);
		registerSynergy(Yeti.class, Khazhad.class, AbsoluteZero.class);
		registerSynergy(Nex.class, Terrorist.class, Meteor.class);
		registerSynergy(Imprison.class, Imprison.class, Bind.class);
		registerSynergy(Sniper.class, Sniper.class, DoubleSniper.class);
		registerSynergy(Nex.class, Assassin.class, NexAssassin.class);
		registerSynergy(Chaos.class, Chaos.class, EventHorizon.class);
		registerSynergy(Nex.class, Stalker.class, DeathGrasp.class);
		registerSynergy(TimeRewind.class, TimeRewind.class, TimeLoop.class);
		registerSynergy(Ares.class, Terrorist.class, RocketLauncher.class);
		registerSynergy(DevilBoots.class, Yeti.class, FlameMan.class);
		registerSynergy(Sniper.class, PenetrationArrow.class, PenetrationSniper.class);
		registerSynergy(Muse.class, Muse.class, Bless.class);
	}

	private SynergyFactory() {
	}

	public static Set<AbilityRegistration> getSynergies() {
		return synergyBases.keySet();
	}

	public static boolean isSynergy(AbilityRegistration registration) {
		return synergyBases.containsKey(registration);
	}

	public static AbilityRegistration getByName(String name) {
		return usedNames.get(name);
	}

	public static boolean isRegistered(String name) {
		return usedNames.containsKey(name);
	}

	public static void registerSynergy(Class<? extends AbilityBase> first, Class<? extends AbilityBase> second, Class<? extends Synergy> synergy) {
		AbilityFactory.registerAbility(synergy);
		if (AbilityFactory.isRegistered(first) && AbilityFactory.isRegistered(second) && AbilityFactory.isRegistered(synergy)) {
			AbilityRegistration firstReg = AbilityFactory.getRegistration(first);
			AbilityRegistration secondReg = AbilityFactory.getRegistration(second);
			AbilityRegistration synergyReg = AbilityFactory.getRegistration(synergy);
			if (!synergies.contains(firstReg, secondReg) && !synergies.contains(secondReg, firstReg)) {
				synergies.put(firstReg, secondReg, synergyReg);
				synergies.put(secondReg, firstReg, synergyReg);
				synergyBases.put(synergyReg, Pair.of(firstReg, secondReg));
				usedNames.put(synergyReg.getManifest().name(), synergyReg);
			}
		}
	}

	public static AbilityRegistration getSynergy(Class<? extends AbilityBase> first, Class<? extends AbilityBase> second) {
		if (AbilityFactory.isRegistered(first) && AbilityFactory.isRegistered(second)) {
			return synergies.get(AbilityFactory.getRegistration(first), AbilityFactory.getRegistration(second));
		}
		return null;
	}

	public static Pair<AbilityRegistration, AbilityRegistration> getSynergyBase(AbilityRegistration synergyReg) {
		return synergyBases.get(synergyReg);
	}

}
