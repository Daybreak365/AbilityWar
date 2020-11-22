package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.TargetHandler;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.annotations.Beta;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;

@AbilityManifest(name = "솔라", rank = Rank.A, species = Species.OTHERS, explain = {
	"베타"
})
@Beta
public class Solar extends AbilityBase implements TargetHandler {

	public Solar(Participant participant) {
		super(participant);
	}

	@Override
	public void TargetSkill(Material material, LivingEntity entity) {
	}
}
