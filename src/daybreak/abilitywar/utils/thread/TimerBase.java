package daybreak.abilitywar.utils.thread;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.list.Feather;
import daybreak.abilitywar.ability.list.Gladiator;
import daybreak.abilitywar.ability.list.Pumpkin;

/**
 * 게임 진행 중 실행되는 타이머
 * @author DayBreak 새벽
 */
public abstract class TimerBase {

    private static List<TimerBase> Tasks = new ArrayList<>();

    /**
     * 현재 실행중인 모든 {@link TimerBase}를 반환합니다.
     */
    public static Collection<TimerBase> getTasks() {
        return Collections.unmodifiableList(new ArrayList<>(Tasks));
    }

    /**
     * 해당 타입의 {@link TimerBase}를 모두 종료합니다.
     * @param timerClass 종료할 타입
     */
    public static void StopTasks(Class<? extends TimerBase> timerClass) {
        for (TimerBase timer : getTasks()) {
            if (timerClass.isAssignableFrom(timer.getClass())) {
                timer.StopTimer(false);
            }
        }
    }

    /**
     * 현재 실행중인 {@link TimerBase}를 모두 종료합니다.
     */
    public static void ResetTasks() {
        for (TimerBase timer : getTasks()) timer.StopTimer(true);
        Tasks = new ArrayList<>();
    }

    private int Task = -1;

    private boolean InfiniteTimer;

    private int MaxCount;
    private int Count;
    private int Period = 20;

    /**
     * 타이머를 Silent 모드로 종료시키더도 {@link #onEnd()}를 호출할지의 여부입니다.<p>
     * {@link Pumpkin}, {@link Feather}, {@link Gladiator}와 같은 능력들에서
     * {@link TimerBase}의 {@link #onEnd()}가 호출되지 않게 되면 상태 초기화가 이루어지지 않아
     * 능력 발동 중의 상태가 계속 유지되는 문제가 있어 추가된 설정입니다.
     * <p>
     * 능력이 강제로 변경된 이후에 초기화가 필요한 능력에서만 사용할 것을 권장합니다.
     */
    private boolean SilentNotice = false;

    /**
     * {@link TimerBase}가 실행될 때 호출됩니다.
     */
    protected abstract void onStart();

    /**
     * {@link TimerBase} 실행 이후 {@link #Period}틱마다 호출됩니다.
     * <pre>
     * 일반 타이머
     * <pre>
     * 카운트 값이 {@link #MaxCount}에서 시작하여 1까지 감소합니다.</pre>
     * 무한 타이머
     * <pre>
     * 카운트 값이 1에서 시작하여 {@link Integer#MAX_VALUE}까지 증가합니다.</pre>
     * </pre>
     */
    protected abstract void onProcess(int Count);

    /**
     * {@link TimerBase}가 종료될 때 호출됩니다.
     */
    protected abstract void onEnd();

    /**
     * {@link TimerBase}의 실행 여부를 반환합니다.
     */
    public final boolean isRunning() {
        return Task != -1;
    }

    /**
     * {@link TimerBase}를 실행합니다.
     */
    public final void StartTimer() {
        if (!isRunning()) {
            Count = MaxCount;
            this.Task = Bukkit.getScheduler().scheduleSyncRepeatingTask(AbilityWar.getPlugin(), new TimerTask(), 0, Period);
            Tasks.add(this);
            onStart();
        }
    }

    /**
     * {@link TimerBase}를 종료합니다.<p>
     *
     * @param Silent true인 경우에 타이머를 Silent 모드로 종료합니다.
     *               Silent 모드에서는 {@link #onEnd()}가 호출되지 않습니다.
     */
    public final void StopTimer(boolean Silent) {
        if (isRunning()) {
            Bukkit.getScheduler().cancelTask(Task);
            Tasks.remove(this);
            Count = MaxCount;
            this.Task = -1;
            if (!Silent || SilentNotice) {
                onEnd();
            }
        }
    }

    public final int getMaxCount() {
        return MaxCount;
    }

    public final int getCount() {
        return Count;
    }

    public final int getFixedCount() {
        return Count / (20 / Period);
    }

    public TimerBase setPeriod(int Period) {
        this.Period = Period;
        return this;
    }

    public TimerBase setSilentNotice(boolean silentNotice) {
        SilentNotice = silentNotice;
        return this;
    }

    /**
     * 일반 {@link TimerBase}
     */
    public TimerBase(int Count) {
        InfiniteTimer = false;
        this.MaxCount = Count;
    }

    /**
     * 무한 {@link TimerBase}
     */
    public TimerBase() {
        InfiniteTimer = true;
        this.MaxCount = 1;
    }

    private final class TimerTask extends Thread {

        @Override
        public void run() {
            if (AbilityWarThread.isGameTaskRunning()) {
                if (InfiniteTimer) {
                    onProcess(Count);
                    Count++;
                } else {
                    if (Count > 0) {
                        onProcess(Count);
                        Count--;
                    } else {
                        StopTimer(false);
                    }
                }
            } else {
                StopTimer(true);
            }
        }

    }

}