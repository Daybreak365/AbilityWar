package daybreak.abilitywar.ability.list.virus;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.GameManager;
import daybreak.abilitywar.game.event.participant.ParticipantDeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@AbilityManifest(name = "바이러스", rank = Rank.C, species = Species.OTHERS, explain = {
		"이 능력은 당신을 죽인 사람에게 감염됩니다."
})
public class Virus extends AbilityBase {

	public Virus(Participant participant) {
		super(participant);
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onPlayerDeath(ParticipantDeathEvent e) {
		final Player killer = getPlayer().getKiller();
		final Participant killerParticipant = GameManager.getGame().getParticipant(killer);
		if (killer != null && killerParticipant != null) {
			final VirusInfectionEvent event = new VirusInfectionEvent(this, killerParticipant);
			Bukkit.getPluginManager().callEvent(event);
			if (!event.isCancelled()) {
				try {
					getGame().getParticipant(killer).setAbility(Virus.class);
				} catch (ReflectiveOperationException ignored) {}
			}
		}
	}

}
