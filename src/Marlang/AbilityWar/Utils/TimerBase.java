package Marlang.AbilityWar.Utils;

import org.bukkit.Bukkit;

import Marlang.AbilityWar.AbilityWar;

/**
 * Timer Base
 * @author _Marlang ¸»¶û
 */
abstract public class TimerBase {
	
	private static AbilityWar Plugin;
	
	public static void Initialize(AbilityWar Plugin) {
		TimerBase.Plugin = Plugin;
	}
	
	int Task = -1;
	
	boolean ReverseTimer;
	int Count;
	int MaxCount;
	
	int TempCount;
	
	int Period = 20;
	
	abstract public void TimerStart();
	
	abstract public void TimerProcess(Integer Seconds);
	
	abstract public void TimerEnd();
	
	public boolean isTimerRunning() {
		return Task != -1;
	}
	
	public void StartTimer() {
		TempCount = Count;
		this.Task = Bukkit.getScheduler().scheduleSyncRepeatingTask(Plugin, new TimerTask(), 0, Period);
		TimerStart();
	}

	public void StopTimer() {
		Bukkit.getScheduler().cancelTask(Task);
		TempCount = Count;
		this.Task = -1;
		TimerEnd();
	}
	
	public void ForceStopTimer() {
		Bukkit.getScheduler().cancelTask(Task);
		TempCount = Count;
		this.Task = -1;
	}
	
	public void setPeriod(Integer Period) {
		this.Period = Period;
	}
	
	public int getCount() {
		return Count;
	}
	
	public int getTempCount() {
		return TempCount + 1;
	}

	/**
	 * Reverse Timer
	 */
	public TimerBase(int Count) {
		this.ReverseTimer = true;
		this.Count = Count;
	}
	
	/**
	 * Normal Timer
	 */
	public TimerBase(int Count, int MaxCount) {
		this.ReverseTimer = false;
		this.Count = Count;
		this.MaxCount = MaxCount;
	}
	
	public final class TimerTask extends Thread {
		
		@Override
		public void run() {
			if(AbilityWarThread.isGameTaskRunning()) {
				TimerProcess(TempCount);
				if(!ReverseTimer) {
					if (TempCount >= MaxCount) {
						StopTimer();
					}
					
					TempCount++;
				} else {
					if (TempCount <= 0) {
						StopTimer();
					}
					
					TempCount--;
				}
			} else {
				ForceStopTimer();
			}
		}
		
	}
	
}
