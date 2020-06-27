package daybreak.abilitywar.game.script;

import daybreak.abilitywar.game.AbstractGame.GameTimer;
import daybreak.abilitywar.game.Game;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer.TaskType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public abstract class AbstractScript {

	private final String scriptType, name, preMessage, runMessage;
	private final int period, loopCount;
	private transient GameTimer timer = null;

	public AbstractScript(String name, int period, int loopCount, String preMessage, String runMessage) {
		this.scriptType = this.getClass().getName();
		this.name = name;
		this.period = period;
		this.loopCount = loopCount;
		this.preMessage = preMessage;
		this.runMessage = runMessage;
	}

	public void start(Game game) {
		if (timer == null || !timer.isRunning()) {
			timer = game.new GameTimer(TaskType.REVERSE, period) {
				int count = loopCount;

				@Override
				public void onStart() {
					if (count > 0) count--;
				}

				@Override
				protected void run(int count) {
					String msg = getPreRunMessage(count);

					if (!msg.equalsIgnoreCase("none")) {
						if (count == (getMaximumCount() / 2)) {
							Bukkit.broadcastMessage(msg);
						} else if (count <= 5 && count >= 1) {
							Bukkit.broadcastMessage(msg);
						}
					}
				}

				@Override
				public void onEnd() {
					execute(game);

					String msg = getRunMessage();
					if (!msg.equalsIgnoreCase("none")) {
						Bukkit.broadcastMessage(msg);
					}

					if (isLoop()) {
						if (count > -1) {
							if (count > 0) {
								this.start();
							}
						} else {
							this.start();
						}
					}
				}
			};
			timer.start();
		}
	}

	public String getType() {
		return scriptType;
	}

	public String getName() {
		return name;
	}

	protected boolean isLoop() {
		return loopCount != 0;
	}

	protected GameTimer getTimer() {
		return timer;
	}

	private String getPreRunMessage(int time) {
		return ChatColor.translateAlternateColorCodes('&', preMessage.replaceAll("%Time%", String.valueOf(time)).replaceAll("%ScriptName%", name));
	}

	private String getRunMessage() {
		return ChatColor.translateAlternateColorCodes('&', runMessage.replaceAll("%ScriptName%", name));
	}

	protected abstract void execute(Game game);

}
