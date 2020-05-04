package daybreak.abilitywar.game.list.mixability;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.ability.decorator.TargetHandler;
import daybreak.abilitywar.game.AbstractGame.Participant;
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
		joiner.add(ChatColor.translateAlternateColorCodes('&', "&b" + ability.getName() + " " + ability.getRank().getRankName() + " " + ability.getSpecies().getSpeciesName()));
		for (Iterator<String> iterator = ability.getExplanation(); iterator.hasNext(); ) {
			joiner.add(ChatColor.RESET + iterator.next());
		}
		return joiner.toString();
	}

	private AbilityBase first;
	private AbilityBase second;
	private final Object explain = new Object() {
		@Override
		public String toString() {
			StringJoiner joiner = new StringJoiner("\n");
			joiner.add("§a--------------------------------");
			joiner.add(first != null ? formatAbilityInfo(first) : "§f능력이 없습니다.");
			joiner.add("§a--------------------------------");
			joiner.add(second != null ? formatAbilityInfo(second) : "§f능력이 없습니다.");
			return joiner.toString();
		}
	};

	public void setAbility(Class<? extends AbilityBase> first, Class<? extends AbilityBase> second) throws SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		removeAbility();
		this.first = AbilityBase.create(first, getParticipant());
		this.first.setRestricted(isRestricted() || !getGame().isGameStarted());
		this.second = AbilityBase.create(second, getParticipant());
		this.second.setRestricted(isRestricted() || !getGame().isGameStarted());
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
		super(participant);
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

}
