package daybreak.abilitywar.game.manager.effect;

import daybreak.abilitywar.game.AbstractGame.GameTimer;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;

public abstract class Effect extends GameTimer {

	private final ActionbarChannel channel;
	private final String displayName;

	protected Effect(Participant participant, String displayName, TaskType taskType, int maximumCount) {
		participant.getGame().super(taskType, maximumCount);
		this.channel = participant.actionbar().newChannel();
		this.displayName = displayName;
	}

	@Override
	public boolean start() {
		if (!channel.isValid()) return false;
		return super.start();
	}

	@Override
	protected void run(int count) {
		channel.update(displayName + "§7: §f" + (count / (20.0 / getPeriod())) + "초");
	}

	@Override
	protected void onEnd() {
		channel.unregister();
	}

	@Override
	protected void onSilentEnd() {
		channel.unregister();
	}

}
