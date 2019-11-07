package daybreak.abilitywar.game.script.objects;

import daybreak.abilitywar.game.games.standard.Game;
import daybreak.abilitywar.utils.thread.TimerBase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public abstract class AbstractScript {

	private final String scriptType;
	private final String name;
	private final int period;
	private final int loopCount;
	private final String preMessage;
	private final String runMessage;
	private transient TimerBase Timer;

	public AbstractScript(String name, int period, int loopCount, String preMessage, String runMessage) {
		this.scriptType = this.getClass().getName();
		this.name = name;
		this.period = period;
		this.loopCount = loopCount;
		this.preMessage = preMessage;
		this.runMessage = runMessage;
		this.Timer = newTimer();
	}

	private transient Game game;

	public void Start(Game game) {
		this.game = game;

		if (Timer != null) {
			Timer.startTimer();
		} else {
			Timer = newTimer();
			Timer.startTimer();
		}
	}

	private TimerBase newTimer() {
		return new TimerBase(period) {

			// count가 0이 되면 루프 종료
			// count가 0보다 작을 경우 무한루프
			int count = loopCount;

			@Override
			public void onStart() {
				if(count > 0) count--;
			}

			@Override
			public void onProcess(int count) {
				String msg = getPreRunMessage(count);

				if (!msg.equalsIgnoreCase("none")) {
					if (count == (this.getMaxCount() / 2)) {
						Bukkit.broadcastMessage(msg);
					} else if (count <= 5 && count >= 1) {
						Bukkit.broadcastMessage(msg);
					}
				}
			}

			@Override
			public void onEnd() {
				Execute(game);

				String msg = getRunMessage();
				if (!msg.equalsIgnoreCase("none")) {
					Bukkit.broadcastMessage(msg);
				}

				if (isLoop()) {
					if(count > -1) {
						if(count > 0) {
							this.startTimer();
						}
					} else {
						this.startTimer();
					}
				}
			}

		};
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

	protected TimerBase getTimer() {
		return Timer;
	}

	private String getPreRunMessage(Integer Time) {
		return ChatColor.translateAlternateColorCodes('&',
				preMessage.replaceAll("%Time%", Time.toString()).replaceAll("%ScriptName%", name));
	}

	private String getRunMessage() {
		return ChatColor.translateAlternateColorCodes('&', runMessage.replaceAll("%ScriptName%", name));
	}

	protected abstract void Execute(Game game);

}
