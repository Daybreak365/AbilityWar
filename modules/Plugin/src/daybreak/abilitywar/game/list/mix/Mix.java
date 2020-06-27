package daybreak.abilitywar.game.list.mix;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.ability.decorator.TargetHandler;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;
import daybreak.abilitywar.game.list.mix.synergy.SynergyFactory;
import daybreak.abilitywar.utils.base.collect.Pair;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.StringJoiner;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;

@AbilityManifest(name = "믹스", rank = AbilityManifest.Rank.SPECIAL, species = AbilityManifest.Species.OTHERS, explain = {
		"$(explain)"
})
public class Mix extends AbilityBase implements ActiveHandler, TargetHandler {

	private static String formatAbilityInfo(AbilityBase ability) {
		StringJoiner joiner = new StringJoiner("\n");
		joiner.add("§b" + ability.getName() + " " + ability.getRank().getRankName() + " " + ability.getSpecies().getSpeciesName());
		for (Iterator<String> iterator = ability.getExplanation(); iterator.hasNext(); ) {
			joiner.add(ChatColor.RESET + iterator.next());
		}
		return joiner.toString();
	}

	private Synergy synergy;

	private AbilityBase first;
	private AbilityBase second;
	private final Object explain = new Object() {
		@Override
		public String toString() {
			StringJoiner joiner = new StringJoiner("\n");
			joiner.add("§a--------------------------------");
			if (synergy != null) {
				Pair<AbilityRegistration, AbilityRegistration> base = SynergyFactory.getSynergyBase(synergy.getRegistration());
				joiner.add("§f시너지: §a" + base.getLeft().getManifest().name() + " §f+ §a" + base.getRight().getManifest().name());
				joiner.add("§a--------------------------------");
				joiner.add(formatAbilityInfo(synergy));
				return joiner.toString();
			} else {
				joiner.add(first != null ? formatAbilityInfo(first) : "§f능력이 없습니다.");
				joiner.add("§a--------------------------------");
				joiner.add(second != null ? formatAbilityInfo(second) : "§f능력이 없습니다.");
				return joiner.toString();
			}
		}
	};

	public void setAbility(Class<? extends AbilityBase> first, Class<? extends AbilityBase> second) throws SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		removeAbility();
		AbilityRegistration synergyReg = SynergyFactory.getSynergy(first, second);
		if (synergyReg != null) {
			this.synergy = (Synergy) AbilityBase.create(synergyReg.getAbilityClass(), getParticipant());
			this.synergy.setRestricted(isRestricted() || !getGame().isGameStarted());
		} else {
			this.first = AbilityBase.create(first, getParticipant());
			this.first.setRestricted(isRestricted() || !getGame().isGameStarted());
			this.second = AbilityBase.create(second, getParticipant());
			this.second.setRestricted(isRestricted() || !getGame().isGameStarted());
		}
	}

	public boolean hasSynergy() {
		return synergy != null;
	}

	public boolean hasAbility() {
		return synergy != null || (first != null && second != null);
	}

	public void removeAbility() {
		if (hasAbility()) {
			if (synergy != null) {
				synergy.destroy();
				synergy = null;
			}
			if (first != null) {
				first.destroy();
				first = null;
			}
			if (second != null) {
				second.destroy();
				second = null;
			}
		}
	}

	public Synergy getSynergy() {
		return synergy;
	}

	public AbilityBase getFirst() {
		return first;
	}

	public AbilityBase getSecond() {
		return second;
	}

	public Mix(Participant participant) {
		super(participant);
	}

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (hasAbility()) {
			if (synergy != null) {
				return synergy instanceof ActiveHandler && ((ActiveHandler) synergy).ActiveSkill(materialType, clickType);
			} else {
				boolean abilityUsed = false;
				if (first instanceof ActiveHandler && ((ActiveHandler) first).ActiveSkill(materialType, clickType))
					abilityUsed = true;
				if (second instanceof ActiveHandler && ((ActiveHandler) second).ActiveSkill(materialType, clickType))
					abilityUsed = true;
				return abilityUsed;
			}
		} else {
			return false;
		}
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
		if (hasAbility()) {
			if (synergy != null) {
				if (synergy instanceof TargetHandler) ((TargetHandler) synergy).TargetSkill(materialType, entity);
			} else {
				if (first instanceof TargetHandler) ((TargetHandler) first).TargetSkill(materialType, entity);
				if (second instanceof TargetHandler) ((TargetHandler) second).TargetSkill(materialType, entity);
			}
		}
	}

	@Override
	protected void onUpdate(AbilityBase.Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			if (hasAbility()) {
				if (synergy != null) {
					synergy.setRestricted(false);
				} else {
					first.setRestricted(false);
					second.setRestricted(false);
				}
			}
		} else if (update == Update.RESTRICTION_SET) {
			if (hasAbility()) {
				if (synergy != null) {
					synergy.setRestricted(true);
				} else {
					first.setRestricted(true);
					second.setRestricted(true);
				}
			}
		}
	}

}
