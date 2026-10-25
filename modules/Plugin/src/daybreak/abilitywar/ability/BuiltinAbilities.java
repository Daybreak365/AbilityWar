package daybreak.abilitywar.ability;

import daybreak.abilitywar.ability.list.Ares;
import daybreak.abilitywar.ability.list.Assassin;
import daybreak.abilitywar.ability.list.Bellum;
import daybreak.abilitywar.ability.list.Berserker;
import daybreak.abilitywar.ability.list.Canis;
import daybreak.abilitywar.ability.list.Celebrity;
import daybreak.abilitywar.ability.list.Chaos;
import daybreak.abilitywar.ability.list.Curse;
import daybreak.abilitywar.ability.list.DarkVision;
import daybreak.abilitywar.ability.list.Demigod;
import daybreak.abilitywar.ability.list.Developer;
import daybreak.abilitywar.ability.list.DevilBoots;
import daybreak.abilitywar.ability.list.DiceGod;
import daybreak.abilitywar.ability.list.Emperor;
import daybreak.abilitywar.ability.list.EnergyBlocker;
import daybreak.abilitywar.ability.list.Eos;
import daybreak.abilitywar.ability.list.ExpertOfFall;
import daybreak.abilitywar.ability.list.Explosion;
import daybreak.abilitywar.ability.list.FastRegeneration;
import daybreak.abilitywar.ability.list.Feather;
import daybreak.abilitywar.ability.list.Ferda;
import daybreak.abilitywar.ability.list.FireFightWithFire;
import daybreak.abilitywar.ability.list.Flector;
import daybreak.abilitywar.ability.list.Flora;
import daybreak.abilitywar.ability.list.Ghost;
import daybreak.abilitywar.ability.list.Ghoul;
import daybreak.abilitywar.ability.list.Glacier;
import daybreak.abilitywar.ability.list.Gladiator;
import daybreak.abilitywar.ability.list.Hacker;
import daybreak.abilitywar.ability.list.Hedgehog;
import daybreak.abilitywar.ability.list.HigherBeing;
import daybreak.abilitywar.ability.list.Imprison;
import daybreak.abilitywar.ability.list.Ira;
import daybreak.abilitywar.ability.list.JellyFish;
import daybreak.abilitywar.ability.list.Khazhad;
import daybreak.abilitywar.ability.list.Kidnap;
import daybreak.abilitywar.ability.list.Lazyness;
import daybreak.abilitywar.ability.list.Liberator;
import daybreak.abilitywar.ability.list.Loki;
import daybreak.abilitywar.ability.list.Lorem;
import daybreak.abilitywar.ability.list.Lunar;
import daybreak.abilitywar.ability.list.Lux;
import daybreak.abilitywar.ability.list.Magician;
import daybreak.abilitywar.ability.list.Morpheus;
import daybreak.abilitywar.ability.list.Muse;
import daybreak.abilitywar.ability.list.Nex;
import daybreak.abilitywar.ability.list.PenetrationArrow;
import daybreak.abilitywar.ability.list.Pumpkin;
import daybreak.abilitywar.ability.list.Reverse;
import daybreak.abilitywar.ability.list.Ruber;
import daybreak.abilitywar.ability.list.ShowmanShip;
import daybreak.abilitywar.ability.list.Sniper;
import daybreak.abilitywar.ability.list.Solar;
import daybreak.abilitywar.ability.list.SoulEncroach;
import daybreak.abilitywar.ability.list.Stalker;
import daybreak.abilitywar.ability.list.SuperNova;
import daybreak.abilitywar.ability.list.SurvivalInstinct;
import daybreak.abilitywar.ability.list.Swap;
import daybreak.abilitywar.ability.list.SwordMaster;
import daybreak.abilitywar.ability.list.Synchronize;
import daybreak.abilitywar.ability.list.Terrorist;
import daybreak.abilitywar.ability.list.Themis;
import daybreak.abilitywar.ability.list.TimeRewind;
import daybreak.abilitywar.ability.list.Vampire;
import daybreak.abilitywar.ability.list.VictoryBySword;
import daybreak.abilitywar.ability.list.Virtus;
import daybreak.abilitywar.ability.list.Void;
import daybreak.abilitywar.ability.list.Xenon;
import daybreak.abilitywar.ability.list.Yeti;
import daybreak.abilitywar.ability.list.Zeus;
import daybreak.abilitywar.ability.list.Zombie;
import daybreak.abilitywar.ability.list.grapplinghook.GrapplingHook;
import daybreak.abilitywar.ability.list.magnet.Magnet;
import daybreak.abilitywar.ability.list.prophet.Prophet;
import daybreak.abilitywar.ability.list.virus.Virus;
import daybreak.abilitywar.game.list.mix.Mix;
import daybreak.abilitywar.game.list.mix.triplemix.TripleMix;
import daybreak.abilitywar.game.list.murdermystery.ability.Detective;
import daybreak.abilitywar.game.list.murdermystery.ability.Innocent;
import daybreak.abilitywar.game.list.murdermystery.ability.Murderer;
import daybreak.abilitywar.game.list.murdermystery.ability.jobs.innocent.Doctor;
import daybreak.abilitywar.game.list.murdermystery.ability.jobs.innocent.Police;
import daybreak.abilitywar.game.list.murdermystery.ability.jobs.murderer.AssassinMurderer;
import daybreak.abilitywar.game.list.murdermystery.ability.jobs.murderer.BlackMurderer;
import daybreak.abilitywar.game.list.murdermystery.ability.jobs.murderer.SniperMurderer;
import daybreak.abilitywar.game.list.summervacation.SquirtGun;
import daybreak.abilitywar.game.list.tnt.ability.TNT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Built-in registrations shared by the factory and the selectable ability list.
 * Version-specific classes are resolved by the caller at their original position;
 * declaring the catalogue does not initialize abilities or either registry.
 */
public final class BuiltinAbilities {

	private static final List<Entry> ALL;
	private static final List<Entry> SELECTABLE;

	static {
		final List<Entry> all = new ArrayList<>();

		add(all, Assassin.class);
		add(all, Feather.class);
		add(all, Demigod.class);
		add(all, FastRegeneration.class);
		add(all, EnergyBlocker.class);
		add(all, DiceGod.class);
		add(all, Ares.class);
		add(all, Zeus.class);
		add(all, Berserker.class);
		add(all, Zombie.class);
		add(all, Terrorist.class);
		add(all, Yeti.class);
		add(all, Gladiator.class);
		add(all, Chaos.class);
		add(all, Void.class);
		add(all, DarkVision.class);
		final Entry higherBeing = add(all, HigherBeing.class);
		add(all, FireFightWithFire.class);
		add(all, Hacker.class);
		add(all, Muse.class);
		add(all, Stalker.class);
		add(all, Flora.class);
		add(all, ShowmanShip.class);
		add(all, Virtus.class);
		add(all, Nex.class);
		add(all, Ira.class);
		add(all, Magician.class);
		add(all, Emperor.class);
		add(all, Pumpkin.class);
		add(all, Virus.class);
		add(all, DevilBoots.class);
		add(all, Explosion.class);
		add(all, Imprison.class);
		add(all, SuperNova.class);
		add(all, Celebrity.class);
		add(all, ExpertOfFall.class);
		add(all, Curse.class);
		add(all, TimeRewind.class);
		// 2019 여름 업데이트
		add(all, Khazhad.class);
		add(all, Sniper.class);
		add(all, JellyFish.class);

		add(all, Lazyness.class);
		// v2.0.7.7
		add(all, Vampire.class);
		add(all, PenetrationArrow.class);
		// v2.0.8.8
		add(all, SoulEncroach.class);
		add(all, Hedgehog.class);
		// v2.0.9.2
		add(all, Prophet.class);
		// v2.1.3
		add(all, Kidnap.class);
		// v2.1.4.8
		final Entry victoryBySword = add(all, VictoryBySword.class);
		add(all, Flector.class);
		// v2.1.5.8
		add(all, Ghost.class);
		// v2.1.8.2
		add(all, Lunar.class);
		// v2.1.8.8
		addVersioned(all, "hermit", "Hermit");
		// v2.1.9.3
		add(all, SwordMaster.class);
		// v2.1.9.6
		add(all, SurvivalInstinct.class);
		// v2.2.1
		add(all, Synchronize.class);
		// v2.2.2
		add(all, Ghoul.class);
		// v2.2.3
		add(all, Swap.class);
		// v2.2.4
		add(all, Lorem.class);
		add(all, Reverse.class);
		add(all, Themis.class);
		// v2.3.0
		add(all, Ferda.class);
		add(all, Lux.class);
		add(all, Loki.class);
		add(all, GrapplingHook.class);
		addVersioned(all, "scarecrow", "ScareCrow");
		// v2.3.3
		add(all, Solar.class);
		add(all, Canis.class);
		add(all, Ruber.class);
		// v2.4.2
		add(all, Liberator.class);
		// v2.4.4
		add(all, Magnet.class);
		// v2.4.5
		add(all, Bellum.class);
		add(all, Eos.class);
		// v2.6.0
		add(all, Morpheus.class);
		addVersioned(all, "clown", "Clown");

		// v3.0.2
		addVersioned(all, "soul", "Soul");

		// v3.2.1
		add(all, Developer.class);

		// v3.2.2
		add(all, Xenon.class);

		// v3.3.2
		add(all, Glacier.class);


		final List<Entry> selectable = new ArrayList<>(all);
		// Preserve the original AbilityList order used by command completion.
		selectable.remove(victoryBySword);
		selectable.add(selectable.indexOf(higherBeing) + 1, victoryBySword);
		SELECTABLE = Collections.unmodifiableList(selectable);

		// 게임모드 전용
		// 즐거운 여름휴가 게임모드
		add(all, SquirtGun.class);
		// 믹스 능력자 게임모드
		add(all, Mix.class);
		// 트리플 믹스 게임모드
		add(all, TripleMix.class);

		// 머더 미스터리 게임모드
		add(all, Murderer.class);
		add(all, Detective.class);
		add(all, Innocent.class);
		add(all, Police.class);
		add(all, Doctor.class);
		add(all, AssassinMurderer.class);
		add(all, BlackMurderer.class);
		add(all, SniperMurderer.class);

		// 폭탄 돌리기 게임모드
		add(all, TNT.class);
		ALL = Collections.unmodifiableList(all);
	}

	private BuiltinAbilities() {
	}

	public static void registerAll(final Consumer<Class<? extends AbilityBase>> registration,
			final BiConsumer<String, String> versionedRegistration) {
		register(ALL, registration, versionedRegistration);
	}

	public static void registerSelectable(final Consumer<Class<? extends AbilityBase>> registration,
			final BiConsumer<String, String> versionedRegistration) {
		register(SELECTABLE, registration, versionedRegistration);
	}

	private static void register(final List<Entry> entries, final Consumer<Class<? extends AbilityBase>> registration,
			final BiConsumer<String, String> versionedRegistration) {
		for (Entry entry : entries) {
			if (entry.abilityClass != null) {
				registration.accept(entry.abilityClass);
			} else {
				versionedRegistration.accept(entry.packagePrefix, entry.simpleName);
			}
		}
	}

	private static Entry add(final List<Entry> entries, final Class<? extends AbilityBase> abilityClass) {
		final Entry entry = new Entry(abilityClass, null, null);
		entries.add(entry);
		return entry;
	}

	private static void addVersioned(final List<Entry> entries, final String packageName, final String simpleName) {
		entries.add(new Entry(null, "daybreak.abilitywar.ability.list." + packageName + ".", simpleName));
	}

	private static final class Entry {
		private final Class<? extends AbilityBase> abilityClass;
		private final String packagePrefix;
		private final String simpleName;

		private Entry(final Class<? extends AbilityBase> abilityClass, final String packagePrefix, final String simpleName) {
			this.abilityClass = abilityClass;
			this.packagePrefix = packagePrefix;
			this.simpleName = simpleName;
		}
	}
}
