package daybreak.abilitywar.game.games.mixability;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.game.games.mode.AbstractGame;
import daybreak.abilitywar.utils.Messager;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

@AbilityManifest(Name = "믹스", Rank = AbilityManifest.Rank.SPECIAL, Species = AbilityManifest.Species.OTHERS)
public class Mix extends AbilityBase {

	private static String[] formatAbilityInfo(AbilityBase ability) {
		ArrayList<String> list = Messager.asList(
				ChatColor.translateAlternateColorCodes('&', "&b" + ability.getName() + " " + ability.getRank().getRankName() + " " + ability.getSpecies().getSpeciesName()));
		list.addAll(ability.getExplain());
		return list.toArray(new String[0]);
	}

	private AbilityBase first;
	private AbilityBase second;

	public void setAbility(Class<? extends AbilityBase> first, Class<? extends AbilityBase> second)
			throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		if (first != null && second != null) {
			if (hasAbility()) removeAbility();

			this.first = first.getConstructor(AbstractGame.Participant.class).newInstance(getParticipant());
			this.first.setRestricted(isRestricted() || !getGame().isGameStarted());
			getDescriptionLine(2).setStrings(formatAbilityInfo(this.first));
			this.second = second.getConstructor(AbstractGame.Participant.class).newInstance(getParticipant());
			this.second.setRestricted(isRestricted() || !getGame().isGameStarted());
			getDescriptionLine(4).setStrings(formatAbilityInfo(this.second));
		}
	}

	public boolean hasAbility() {
		return first != null && second != null;
	}

	public void removeAbility() {
		if (hasAbility()) {
			first.destroy();
			first = null;
			second.destroy();
			second = null;
		}
	}

	public AbilityBase getFirst() {
		return first;
	}

	public AbilityBase getSecond() {
		return second;
	}

	public Mix(AbstractGame.Participant participant) throws IllegalStateException {
		super(participant,
				new DescriptionLine("믹스"),
				new DescriptionLine(ChatColor.translateAlternateColorCodes('&', "&a--------------------------------")),
				new DescriptionLine("능력이 없습니다."),
				new DescriptionLine(ChatColor.translateAlternateColorCodes('&', "&a--------------------------------")),
				new DescriptionLine("능력이 없습니다."));
	}

	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		if (hasAbility()) {
			boolean bool = false;
			if (first.ActiveSkill(mt, ct)) {
				bool = true;
			}
			if (second.ActiveSkill(mt, ct)) {
				bool = true;
			}
			return bool;
		} else {
			return false;
		}
	}

	@Override
	public void TargetSkill(MaterialType mt, LivingEntity entity) {
		if (hasAbility()) {
			first.TargetSkill(mt, entity);
			second.TargetSkill(mt, entity);
		}
	}

	@Override
	public void onRestrictClear() {
		if (hasAbility()) {
			first.onRestrictClear();
			second.onRestrictClear();
		}
	}

}
