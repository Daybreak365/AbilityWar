package daybreak.abilitywar.game.list.mix.triplemix;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.ability.decorator.TargetHandler;
import daybreak.abilitywar.game.AbstractGame.GameTimer;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.collect.SetUnion;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Set;
import java.util.StringJoiner;

@AbilityManifest(name = "트리플 믹스", rank = Rank.SPECIAL, species = Species.OTHERS, explain = "$(EXPLAIN)")
public class TripleMix extends AbilityBase implements ActiveHandler, TargetHandler {

	private AbilityBase first = null;
	private AbilityBase second = null;
	private AbilityBase third = null;

	private final Object EXPLAIN = new Object() {
		@Override
		public String toString() {
			final StringJoiner joiner = new StringJoiner("\n");
			joiner.add("§a---------------------------------");
			formatInfo(joiner, first);
			joiner.add("§a---------------------------------");
			formatInfo(joiner, second);
			joiner.add("§a---------------------------------");
			formatInfo(joiner, third);
			return joiner.toString();
		}

		private void formatInfo(final StringJoiner joiner, final AbilityBase ability) {
			if (ability != null) {
				joiner.add("§b" + ability.getName() + " §f[" + (ability.isRestricted() ? "§7능력 비활성화됨" : "§a능력 활성화됨") + "§f] " + ability.getRank().getRankName() + " " + ability.getSpecies().getSpeciesName());
				for (final Iterator<String> iterator = ability.getExplanation(); iterator.hasNext(); ) {
					joiner.add(ChatColor.RESET + iterator.next());
				}
			} else {
				joiner.add("§f능력이 없습니다.");
			}
		}
	};

	public TripleMix(Participant participant) {
		super(participant);
	}

	public AbilityBase getFirst() {
		return first;
	}

	public AbilityBase getSecond() {
		return second;
	}

	public AbilityBase getThird() {
		return third;
	}

	public void setAbility(final Class<? extends AbilityBase> first, final Class<? extends AbilityBase> second, final Class<? extends AbilityBase> third) throws SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		setAbility0(create(first, getParticipant()), create(second, getParticipant()), create(third, getParticipant()));
	}

	public void setAbility(final AbilityRegistration first, final AbilityRegistration second, final AbilityRegistration third) throws SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		setAbility0(create(first, getParticipant()), create(second, getParticipant()), create(third, getParticipant()));
	}

	private void setAbility0(AbilityBase abilityBase, AbilityBase abilityBase2, AbilityBase abilityBase3) {
		removeAbility();
		this.first = abilityBase;
		abilityBase.setRestricted(false);
		this.second = abilityBase2;
		abilityBase2.setRestricted(false);
		this.third = abilityBase3;
		abilityBase3.setRestricted(false);
	}

	@Override
	public boolean usesMaterial(Material material) {
		return (first != null && first.usesMaterial(material)) || (second != null && second.usesMaterial(material)) || (third != null && third.usesMaterial(material));
	}

	@Override
	public Set<GameTimer> getTimers() {
		if (hasAbility()) {
			return SetUnion.union(first.getTimers(), second.getTimers(), third.getTimers());
		}
		return super.getTimers();
	}

	@Override
	public Set<GameTimer> getRunningTimers() {
		if (hasAbility()) {
			return SetUnion.union(first.getRunningTimers(), second.getRunningTimers(), third.getRunningTimers());
		}
		return super.getRunningTimers();
	}

	public boolean hasAbility() {
		return first != null && second != null && third != null;
	}

	public void removeAbility() {
		if (hasAbility()) {
			if (first != null) {
				first.destroy();
				this.first = null;
			}
			if (second != null) {
				second.destroy();
				this.second = null;
			}
			if (third != null) {
				third.destroy();
				this.third = null;
			}
		}
	}

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull AbilityBase.ClickType clickType) {
		if (hasAbility()) {
			boolean abilityUsed = false;
			if (first instanceof ActiveHandler && ((ActiveHandler) first).ActiveSkill(material, clickType)) abilityUsed = true;
			if (second instanceof ActiveHandler && ((ActiveHandler) second).ActiveSkill(material, clickType)) abilityUsed = true;
			if (third instanceof ActiveHandler && ((ActiveHandler) third).ActiveSkill(material, clickType)) abilityUsed = true;
			return abilityUsed;
		}
		return false;
	}

	@Override
	public void TargetSkill(@NotNull Material material, @NotNull LivingEntity entity) {
		if (hasAbility()) {
			if (first instanceof TargetHandler) ((TargetHandler) first).TargetSkill(material, entity);
			if (second instanceof TargetHandler) ((TargetHandler) second).TargetSkill(material, entity);
			if (third instanceof TargetHandler) ((TargetHandler) third).TargetSkill(material, entity);
		}
	}

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			if (first != null) {
				first.setRestricted(false);
			}
			if (second != null) {
				second.setRestricted(false);
			}
			if (third != null) {
				third.setRestricted(false);
			}
		} else if (update == Update.RESTRICTION_SET) {
			if (first != null) {
				first.setRestricted(true);
			}
			if (second != null) {
				second.setRestricted(true);
			}
			if (third != null) {
				third.setRestricted(true);
			}
		} else if (update == Update.ABILITY_DESTROY) {
			if (first != null) {
				first.destroy();
				this.first = null;
			}
			if (second != null) {
				second.destroy();
				this.second = null;
			}
			if (third != null) {
				third.destroy();
				this.third = null;
			}
		}
	}
}
