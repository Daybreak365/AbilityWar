package daybreak.abilitywar.game.list.augment;

import daybreak.abilitywar.game.GameAliases;
import daybreak.abilitywar.game.GameManifest;
import daybreak.abilitywar.game.augment.Augment;
import daybreak.abilitywar.game.augment.AugmentRegistry;
import daybreak.abilitywar.game.augment.AugmentSelection;
import daybreak.abilitywar.game.augment.DefaultAugments;
import daybreak.abilitywar.game.augment.ParticipantAugments;
import daybreak.abilitywar.game.list.standard.WarGame;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.Random;

@GameManifest(name = "증강 능력자 전쟁", description = {
		"§f일정 시간마다 참가자가 증강을 획득하는 능력자 전쟁입니다.",
		"",
		"§a● §f첫 증강은 게임 시작 직후 지급됩니다.",
		"§a● §f이후 2분마다 생존 참가자에게 새로운 증강이 지급됩니다."
})
@GameAliases({"증강", "augment", "augments"})
public class AugmentWarGame extends WarGame {

	private static final int FIRST_AUGMENT_SECONDS = 18;
	private static final int AUGMENT_INTERVAL_SECONDS = 120;

	private final ParticipantAugments participantAugments = new ParticipantAugments(this);
	private final AugmentRegistry augmentRegistry = DefaultAugments.createRegistry();
	private final AugmentSelection augmentSelection = new AugmentSelection(augmentRegistry, new Random());

	@Override
	protected void progressGame(int seconds) {
		super.progressGame(seconds);
		if (seconds >= FIRST_AUGMENT_SECONDS && (seconds - FIRST_AUGMENT_SECONDS) % AUGMENT_INTERVAL_SECONDS == 0) {
			grantAugments(seconds);
		}
	}

	private void grantAugments(int seconds) {
		Bukkit.broadcastMessage("§d[증강] §f새로운 증강 라운드가 시작되었습니다. §7(" + seconds + "초)");
		for (Participant participant : getParticipants()) {
			if (!getDeathManager().isExcluded(participant.getPlayer())) {
				final List<Augment> candidates = augmentSelection.roll(participantAugments, participant, 3);
				if (!candidates.isEmpty()) {
					final Augment selected = candidates.get(0);
					selected.apply(participantAugments, participant);
					participant.getPlayer().sendMessage("§d[증강] §f획득: " + DefaultAugments.format(selected));
				}
			}
		}
	}
}
