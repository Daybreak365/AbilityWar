package daybreak.abilitywar.game.list.mixability;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.ability.decorator.TargetHandler;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Messager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@AbilityManifest(name = "믹스", rank = AbilityManifest.Rank.SPECIAL, Species = AbilityManifest.Species.OTHERS)
public class Mix extends AbilityBase implements ActiveHandler, TargetHandler {

	private static String[] formatAbilityInfo(AbilityBase ability) {
		List<String> list = Messager.asList(
				ChatColor.translateAlternateColorCodes('&', "&b" + ability.getName() + " " + ability.getRank().getRankName() + " " + ability.getSpecies().getSpeciesName()));
		list.addAll(ability.getExplanation());
		return list.toArray(new String[0]);
	}

	private AbilityBase first;
	private AbilityBase second;

	public void setAbility(Class<? extends AbilityBase> first, Class<? extends AbilityBase> second) throws SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		removeAbility();
		this.first = AbilityBase.create(first, getParticipant());
		this.first.setRestricted(isRestricted() || !getGame().isGameStarted());
		getExplanation(2).setLines(formatAbilityInfo(this.first));
		this.second = AbilityBase.create(second, getParticipant());
		this.second.setRestricted(isRestricted() || !getGame().isGameStarted());
		getExplanation(4).setLines(formatAbilityInfo(this.second));
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

	public Mix(Participant participant) {
		super(participant,
				new Explanation("믹스"),
				new Explanation(ChatColor.translateAlternateColorCodes('&', "&a--------------------------------")),
				new Explanation("능력이 없습니다."),
				new Explanation(ChatColor.translateAlternateColorCodes('&', "&a--------------------------------")),
				new Explanation("능력이 없습니다."));
	}

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (hasAbility()) {
			boolean abilityUsed = false;
			if (first instanceof ActiveHandler && ((ActiveHandler) first).ActiveSkill(materialType, clickType))
				abilityUsed = true;
			if (second instanceof ActiveHandler && ((ActiveHandler) second).ActiveSkill(materialType, clickType))
				abilityUsed = true;
			return abilityUsed;
		} else {
			return false;
		}
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
		if (hasAbility()) {
			if (first instanceof TargetHandler) ((TargetHandler) first).TargetSkill(materialType, entity);
			if (second instanceof TargetHandler) ((TargetHandler) second).TargetSkill(materialType, entity);
		}
	}

	@Override
	protected void onUpdate(AbilityBase.Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			if (hasAbility()) {
				first.setRestricted(false);
				second.setRestricted(false);
			}
		} else if (update == Update.RESTRICTION_SET) {
			if (hasAbility()) {
				first.setRestricted(true);
				second.setRestricted(true);
			}
		}
	}

	private enum MODE {
		;
	}

}
