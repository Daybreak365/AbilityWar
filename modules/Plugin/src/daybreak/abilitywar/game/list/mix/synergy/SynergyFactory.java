package daybreak.abilitywar.game.list.mix.synergy;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityFactory;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.ability.list.Ares;
import daybreak.abilitywar.ability.list.Assassin;
import daybreak.abilitywar.ability.list.Canis;
import daybreak.abilitywar.ability.list.Celebrity;
import daybreak.abilitywar.ability.list.Chaos;
import daybreak.abilitywar.ability.list.Curse;
import daybreak.abilitywar.ability.list.DevilBoots;
import daybreak.abilitywar.ability.list.Ghost;
import daybreak.abilitywar.ability.list.Imprison;
import daybreak.abilitywar.ability.list.Khazhad;
import daybreak.abilitywar.ability.list.Kidnap;
import daybreak.abilitywar.ability.list.Lazyness;
import daybreak.abilitywar.ability.list.Loki;
import daybreak.abilitywar.ability.list.Muse;
import daybreak.abilitywar.ability.list.Nex;
import daybreak.abilitywar.ability.list.PenetrationArrow;
import daybreak.abilitywar.ability.list.ShowmanShip;
import daybreak.abilitywar.ability.list.Sniper;
import daybreak.abilitywar.ability.list.Stalker;
import daybreak.abilitywar.ability.list.SuperNova;
import daybreak.abilitywar.ability.list.Terrorist;
import daybreak.abilitywar.ability.list.TimeRewind;
import daybreak.abilitywar.ability.list.Virus;
import daybreak.abilitywar.ability.list.Yeti;
import daybreak.abilitywar.ability.list.grapplinghook.GrapplingHook;
import daybreak.abilitywar.game.list.mix.synergy.list.AbsoluteZero;
import daybreak.abilitywar.game.list.mix.synergy.list.Bind;
import daybreak.abilitywar.game.list.mix.synergy.list.BlackKnight;
import daybreak.abilitywar.game.list.mix.synergy.list.Bless;
import daybreak.abilitywar.game.list.mix.synergy.list.CrazyAssassin;
import daybreak.abilitywar.game.list.mix.synergy.list.DeathGrasp;
import daybreak.abilitywar.game.list.mix.synergy.list.DoubleSniper;
import daybreak.abilitywar.game.list.mix.synergy.list.EventHorizon;
import daybreak.abilitywar.game.list.mix.synergy.list.FlameMan;
import daybreak.abilitywar.game.list.mix.synergy.list.Grudge;
import daybreak.abilitywar.game.list.mix.synergy.list.ManeuverGear;
import daybreak.abilitywar.game.list.mix.synergy.list.Meteor;
import daybreak.abilitywar.game.list.mix.synergy.list.NexAssassin;
import daybreak.abilitywar.game.list.mix.synergy.list.Pandemic;
import daybreak.abilitywar.game.list.mix.synergy.list.PenetrationSniper;
import daybreak.abilitywar.game.list.mix.synergy.list.RocketLauncher;
import daybreak.abilitywar.game.list.mix.synergy.list.ShotPut;
import daybreak.abilitywar.game.list.mix.synergy.list.ShowTime;
import daybreak.abilitywar.game.list.mix.synergy.list.SuperLazy;
import daybreak.abilitywar.game.list.mix.synergy.list.TimeLoop;
import daybreak.abilitywar.utils.base.collect.Pair;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * {@link Synergy}를 기반으로 하는 모든 능력을 관리하는 클래스입니다.
 */
public class SynergyFactory {

	private static final Table<AbilityRegistration, AbilityRegistration, AbilityRegistration> synergies = HashBasedTable.create();
	private static final Map<AbilityRegistration, Pair<AbilityRegistration, AbilityRegistration>> synergyBases = new HashMap<>();
	private static final Map<String, AbilityRegistration> usedNames = new HashMap<>();
	private static final ListMultimap<String, String> complete = MultimapBuilder.hashKeys().arrayListValues().build();

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
		registerSynergy(ShowmanShip.class, Celebrity.class, ShowTime.class);
		registerSynergy(Kidnap.class, Kidnap.class, ShotPut.class);
		registerSynergy(Ghost.class, Curse.class, Grudge.class);
		registerSynergy(Lazyness.class, Lazyness.class, SuperLazy.class);
		registerSynergy(Canis.class, Canis.class, BlackKnight.class);
		registerSynergy(GrapplingHook.class, GrapplingHook.class, ManeuverGear.class);
		registerSynergy(Assassin.class, Loki.class, CrazyAssassin.class);
	}

	private SynergyFactory() {
	}

	public static Set<AbilityRegistration> getSynergies() {
		return Collections.unmodifiableSet(synergyBases.keySet());
	}

	public static Set<Cell<AbilityRegistration, AbilityRegistration, AbilityRegistration>> cellSet() {
		return Collections.unmodifiableSet(synergies.cellSet());
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
				final String name = synergyReg.getManifest().name();
				final char[] chars = name.toCharArray();
				if (chars.length >= 2) {
					final StringTokenizer tokenizer = new StringTokenizer(name, " ");
					final StringBuilder builder = new StringBuilder(chars.length - 1);
					String token = tokenizer.nextToken();
					for (int i = 0; i < chars.length - 1; i++) {
						final char c = chars[i];
						builder.append(c);
						if (c == ' ') {
							if (tokenizer.hasMoreTokens()) {
								token = tokenizer.nextToken();
							}
						}
						complete.put(builder.toString(), token);
					}
				}
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

	public static List<String> getComplete(final String part) {
		return complete.get(part);
	}

}
