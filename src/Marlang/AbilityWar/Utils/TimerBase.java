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
		for (TimerBase Task : Tasks) {
			Task.StopTimer(true);
		}
	}

	int Task = -1;

	boolean InfiniteTimer;
	boolean ProcessDuringGame = true;
	boolean SilentNotice = false;
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

	/**
	 * 타이머를 시작합니다.
	 */
	public void StartTimer() {
		TempCount = Count;
		this.Task = Bukkit.getScheduler().scheduleSyncRepeatingTask(AbilityWar.getPlugin(), new TimerTask(), 0, Period);
		if(ProcessDuringGame) {
			Tasks.add(this);
		}
		TimerStart();
	}

	/**
	 * 타이머를 종료합니다.
	 */
	public void StopTimer(boolean Silent) {
		Bukkit.getScheduler().cancelTask(Task);
		TempCount = Count;
		this.Task = -1;
		if(!Silent || getSilentNotice()) {
			TimerEnd();
		}
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

	public int getPeriod() {
		return Period;
	}
	
	public int getTempCount() {
		return TempCount;
	}

	public boolean getSilentNotice() {
		return SilentNotice;
	}

	public void setSilentNotice(boolean silentNotice) {
		SilentNotice = silentNotice;
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
			if (ProcessDuringGame) {
				if (AbilityWarThread.isGameTaskRunning()) {
					if (InfiniteTimer) {
						TimerProcess(-1);
					} else {
						if (TempCount > 0) {
							TimerProcess(TempCount);

							if (TempCount <= 0) {
								StopTimer(false);
							}

							TempCount--;
						} else {
							StopTimer(false);
						}
					}
				} else {
					StopTimer(true);
				}
			} else {
				if (InfiniteTimer) {
					TimerProcess(-1);
				} else {
					TimerProcess(TempCount);

					if (TempCount <= 0) {
						StopTimer(false);
					}

					TempCount--;
				}
			}
		}

	}

}