package Marlang.AbilityWar.GameManager.Script.Objects;

import java.io.Serializable;

import org.bukkit.ChatColor;

import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Thread.TimerBase;

abstract public class AbstractScript implements Serializable {
	
	private static final long serialVersionUID = 7230527266220521425L;
	
	private final String ScriptName;
	private final int Time;
	private final boolean Loop;
	private final int LoopCount;
	private final String PreRunMessage;
	private final String RunMessage;
	private transient TimerBase Timer;
	
	protected AbstractScript(String ScriptName, int Time, boolean Loop, int LoopCount, String PreRunMessage, String RunMessage) {
		this.ScriptName = ScriptName;
		this.Time = Time;
		this.Loop = Loop;
		this.LoopCount = LoopCount;
		this.PreRunMessage = PreRunMessage;
		this.RunMessage = RunMessage;
		this.Timer = newTimer();
	}
	
	public void Start() {
		if(Timer != null) {
			Timer.StartTimer();
		} else {
			Timer = newTimer();
			Timer.StartTimer();
		}
	}
	
	private TimerBase newTimer() {
		return new TimerBase(Time) {
			
			//loopCount가 0이 되면 루프 종료
			//loopCount가 0보다 작을 경우 무한루프
			//Loop가 true일 경우에만 작동
			int loopCount = LoopCount;
			
			@Override
			public void onStart() {}
			
			@Override
			public void TimerProcess(Integer Seconds) {
				if(Seconds == (this.getCount() / 2)) {
					Messager.broadcastMessage(getPreRunMessage(Seconds));
				} else if(Seconds <= 5 && Seconds >= 1) {
					Messager.broadcastMessage(getPreRunMessage(Seconds));
				}
			}
			
			@Override
			public void onEnd() {
				Execute();
				Messager.broadcastMessage(getRunMessage());
				if(Loop) {
					if(loopCount > -1) {
						if(loopCount > 1) {
		 					this.StartTimer();
		 					loopCount--;
						}
					} else {
	 					this.StartTimer();
					}
				}
			}
			
		};
	}
	
	public String getScriptName() {
		return ScriptName;
	}
	
	protected boolean isLoop() {
		return Loop;
	}
	
	protected TimerBase getTimer() {
		return Timer;
	}
	
	private String getPreRunMessage(Integer Time) {
		return ChatColor.translateAlternateColorCodes('&', PreRunMessage.replaceAll("%Time%", Time.toString()).replaceAll("%ScriptName%", this.getScriptName()));
	}

	private String getRunMessage() {
		return ChatColor.translateAlternateColorCodes('&', RunMessage.replaceAll("%ScriptName%", this.getScriptName()));
	}

	abstract public void Execute();
	
}
