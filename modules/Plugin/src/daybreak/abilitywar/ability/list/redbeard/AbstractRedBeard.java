package daybreak.abilitywar.ability.list.redbeard;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.game.AbstractGame.Participant;
import org.bukkit.Material;

@AbilityManifest(name = "레드비어드", rank = Rank.A, species = Species.OTHERS)
public abstract class AbstractRedBeard extends AbilityBase implements ActiveHandler {

	protected AbstractRedBeard(Participant participant) {
		super(participant);
	}

	protected abstract void test();

	@Override
	public boolean ActiveSkill(Material material, ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK) {
			test();
		}
		return false;
	}

}
