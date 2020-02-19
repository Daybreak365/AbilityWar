package daybreak.abilitywar.game.games.mixability;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.event.AbilityRestrictionClearEvent;
import daybreak.abilitywar.ability.event.AbilityRestrictionSetEvent;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.Messager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@AbilityManifest(Name = "믹스", Rank = AbilityManifest.Rank.SPECIAL, Species = AbilityManifest.Species.OTHERS)
public class Mix extends AbilityBase {

	private static String[] formatAbilityInfo(AbilityBase ability) {
		List<String> list = Messager.asList(
				ChatColor.translateAlternateColorCodes('&', "&b" + ability.getName() + " " + ability.getRank().getRankName() + " " + ability.getSpecies().getSpeciesName()));
		list.addAll(ability.getDescription());
		return list.toArray(new String[0]);
	}

	private AbilityBase first;
	private AbilityBase second;

	public void setAbility(Class<? extends AbilityBase> first, Class<? extends AbilityBase> second) throws SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		removeAbility();
		this.first = AbilityBase.create(first, getParticipant());
		this.first.setRestricted(isRestricted() || !getGame().isGameStarted());
		getDescriptionLine(2).setStrings(formatAbilityInfo(this.first));
		this.second = AbilityBase.create(second, getParticipant());
		this.second.setRestricted(isRestricted() || !getGame().isGameStarted());
		getDescriptionLine(4).setStrings(formatAbilityInfo(this.second));
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
				new DescriptionLine("믹스"),
				new DescriptionLine(ChatColor.translateAlternateColorCodes('&', "&a--------------------------------")),
				new DescriptionLine("능력이 없습니다."),
				new DescriptionLine(ChatColor.translateAlternateColorCodes('&', "&a--------------------------------")),
				new DescriptionLine("능력이 없습니다."));
	}

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (hasAbility()) {
			boolean abilityUsed = false;
			if (first.ActiveSkill(materialType, clickType)) abilityUsed = true;
			if (second.ActiveSkill(materialType, clickType)) abilityUsed = true;
			return abilityUsed;
		} else {
			return false;
		}
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
		if (hasAbility()) {
			first.TargetSkill(materialType, entity);
			second.TargetSkill(materialType, entity);
		}
	}

	@SubscribeEvent(onlyRelevant = true)
	public void onRestrictionClear(AbilityRestrictionClearEvent e) {
		if (hasAbility()) {
			first.setRestricted(false);
			second.setRestricted(false);
		}
	}

	@SubscribeEvent(onlyRelevant = true)
	public void onRestrictionSet(AbilityRestrictionSetEvent e) {
		if (hasAbility()) {
			first.setRestricted(true);
			second.setRestricted(true);
		}
	}

	private enum MODE {
		;
	}

}
