package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.events.participant.ParticipantDeathEvent;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.thread.AbilityWarThread;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;

@AbilityManifest(Name = "바이러스", Rank = Rank.D, Species = Species.OTHERS)
public class Virus extends AbilityBase {

	public Virus(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f이 능력은 당신을 죽인 사람에게 감염됩니다."));
	}

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		return false;
	}

	@SubscribeEvent
	public void onPlayerDeath(ParticipantDeathEvent e) {
		Participant participant = e.getParticipant();
		if (participant.equals(getParticipant())) {
			Player killer = getPlayer().getKiller();
			if (killer != null && AbilityWarThread.getGame().isParticipating(killer)) {
				try {
					getGame().getParticipant(killer).setAbility(Virus.class);
				} catch (InstantiationException | IllegalAccessException | InvocationTargetException ignored) {
				}
			}
		}
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
	}

}
