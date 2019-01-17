package Marlang.AbilityWar.Utils;

import java.util.ArrayList;

import org.bukkit.Bukkit;

import Marlang.AbilityWar.AbilityWar;

/**
 * Timer Base
 * @author _Marlang 말랑
 */
abstract public class TimerBase {
	
	static ArrayList<TimerBase> Tasks = new ArrayList<TimerBase>();
	
	public static void StopAllTasks() {
		for(TimerBase Task : Tasks) {
			Task.ForceStopTimer();
		}
	}
	
	int Task = -1;
	
	boolean InfiniteTimer;
	boolean ProcessDuringGame = true;
	int Count;
	int MaxCount;
	
	int TempCount;
	
	int Period = 20;
	
	abstract public void TimerStart();
	
	abstract public void TimerProcess(Integer Seconds);
	
	abstract public void TimerEnd();
	
	public void ForceTimerEnd() {}
	
	public boolean isTimerRunning() {
		return Task != -1;
	}
	
	public void StartTimer() {
		TempCount = Count;
		this.Task = Bukkit.getScheduler().scheduleSyncRepeatingTask(AbilityWar.getPlugin(), new TimerTask(), 0, Period);
		Tasks.add(this);
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
		ForceTimerEnd();
	}
	
	public void setPeriod(Integer Period) {
		this.Period = Period;
	}
	
	public void setProcessDuringGame(boolean bool) {
		this.ProcessDuringGame = bool;
	}
	
	public int getCount() {
		return Count;
	}
	
	public int getTempCount() {
		return TempCount;
	}
	
	/**
	 * 일반 타이머
	 */
	public TimerBase(int Count) {
		this.Count = Count;
		InfiniteTimer = false;
	}
	
	/**
	 * 무한 타이머
	 */
	public TimerBase() {
		InfiniteTimer = true;
	}
	
	public final class TimerTask extends Thread {
		
		@Override
		public void run() {
			if(ProcessDuringGame) {
				if(AbilityWarThread.isGameTaskRunning()) {
					if(InfiniteTimer) {
						TimerProcess(-1);
					} else {
						if(TempCount > 0) {
							TimerProcess(TempCount);
							
							if (TempCount <= 0) {
								StopTimer();
							}
							
							TempCount--;
						} else {
							StopTimer();
						}
					}
				} else {
					ForceStopTimer();
				}
			} else {
				if(InfiniteTimer) {
					TimerProcess(-1);
				} else {
					TimerProcess(TempCount);
					
					if (TempCount <= 0) {
						StopTimer();
					}
					
					TempCount--;
				}
			}
		}
		
	}
	
}
