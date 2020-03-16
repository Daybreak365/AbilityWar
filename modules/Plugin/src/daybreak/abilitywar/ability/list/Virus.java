package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.GameManager;
import daybreak.abilitywar.game.event.participant.ParticipantDeathEvent;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;

@AbilityManifest(name = "바이러스", rank = Rank.C, species = Species.OTHERS, explain = {
		"이 능력은 당신을 죽인 사람에게 감염됩니다."
})
public class Virus extends AbilityBase {

	public Virus(Participant participant) {
		super(participant);
	}

	@SubscribeEvent
	public void onPlayerDeath(ParticipantDeathEvent e) {
		Participant participant = e.getParticipant();
		if (participant.equals(getParticipant())) {
			Player killer = getPlayer().getKiller();
			if (killer != null && GameManager.getGame().isParticipating(killer)) {
				try {
					getGame().getParticipant(killer).setAbility(Virus.class);
				} catch (InstantiationException | IllegalAccessException | InvocationTargetException ignored) {
				}
			}
		}
	}

}
