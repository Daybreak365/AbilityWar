package daybreak.abilitywar.utils.thread;

import daybreak.abilitywar.AbilityWar;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.bukkit.Bukkit;

/**
 * 게임 진행 중 실행되는 타이머
 * @author DayBreak 새벽
 */
public abstract class TimerBase {

    private static final ArrayList<TimerBase> tasks = new ArrayList<>();

    /**
     * 현재 실행중인 모든 {@link TimerBase}를 반환합니다.
     */
    public static Collection<TimerBase> getTasks() {
        return Collections.unmodifiableList(new ArrayList<>(tasks));
    }

    /**
     * 해당 타입의 {@link TimerBase}를 모두 종료합니다.
     *
     * @param timerClass 종료할 타입
     */
    public static void stopTasks(Class<? extends TimerBase> timerClass) {
        for (TimerBase timer : getTasks()) {
            if (timerClass.isAssignableFrom(timer.getClass())) {
                timer.stopTimer(false);
            }
        }
    }

    /**
     * 현재 실행중인 {@link TimerBase}를 모두 종료합니다.
     */
    public static void resetTasks() {
        for (TimerBase timer : getTasks()) timer.stopTimer(true);
        tasks.clear();
    }

    private int task = -1;

    private boolean isInfinite;

    private int maxCount;
    private int count;
    private int period = 20;

    /**
     * 타이머를 Silent 모드로 종료시키더도 {@link #onEnd()}를 호출할지의 여부입니다.<p>
     * 일부 능력들에서 {@link #onEnd()}가 호출되지 않게 되면 상태 초기화가 이루어지지 않아
     * 능력 발동 중의 상태가 계속 유지되는 문제가 있어 추가된 설정입니다.
     * <p>
     * 능력이 강제로 변경된 이후에 초기화가 필요한 능력에서만 사용할 것을 권장합니다.
     */
    private boolean isSilentNotice = false;

    /**
     * {@link TimerBase}가 실행될 때 호출됩니다.
     */
    protected void onStart() {}

    /**
     * {@link TimerBase} 실행 이후 {@link #period}틱마다 호출됩니다.
     * <pre>
     * 일반 타이머
     * <pre>
     * 카운트 값이 {@link #maxCount}에서 시작하여 1까지 감소합니다.</pre>
     * 무한 타이머
     * <pre>
     * 카운트 값이 1에서 시작하여 {@link Integer#MAX_VALUE}까지 증가합니다.</pre>
     * </pre>
     */
    protected abstract void onProcess(int count);

    /**
     * {@link TimerBase}가 종료될 때 호출됩니다.
     */
    protected void onEnd() {}

    /**
     * {@link TimerBase}의 실행 여부를 반환합니다.
     */
    public final boolean isRunning() {
        return task != -1;
    }

    /**
     * {@link TimerBase}를 실행합니다.
     */
    public final void startTimer() {
        if (!isRunning()) {
            count = maxCount;
            this.task = Bukkit.getScheduler().scheduleSyncRepeatingTask(AbilityWar.getPlugin(), new TimerTask(), 0, period);
            tasks.add(this);
            onStart();
        }
    }

    /**
     * {@link TimerBase}를 종료합니다.<p>
     *
     * @param silent true인 경우에 타이머를 silent 모드로 종료합니다.
     *               silent 모드에서는 {@link #onEnd()}가 호출되지 않습니다.
     */
    public final void stopTimer(boolean silent) {
        if (isRunning()) {
            Bukkit.getScheduler().cancelTask(task);
            tasks.remove(this);
            count = maxCount;
            this.task = -1;
            if (!silent || isSilentNotice) {
                onEnd();
            }
        }
    }

    /**
     * @return 타이머를 초기화할 때 설정된 Max Count를 반환합니다.
     */
    public final int getMaxCount() {
        return maxCount;
    }

    /**
     * @return 남은 Count를 반환합니다.
     */
    public final int getCount() {
        return count;
    }

    /**
     * @return 타이머가 무한 타이머인지의 여부를 반환합니다.
     */
    public final boolean isInfinite() {
        return isInfinite;
    }

    /**
     * @return Period에 따라 변할 수 있는 실행 주기를 계산하여 Count를 반환합니다.
     * 1틱마다 실행되는 타이머가 count 20만큼 남았을 때 1을 반환합니다.
     */
    public final int getFixedCount() {
        return count / (20 / period);
    }

    public TimerBase setPeriod(int period) {
        this.period = period;
        return this;
    }

    public TimerBase setSilentNotice(boolean silentNotice) {
        isSilentNotice = silentNotice;
        return this;
    }

    /**
     * maxCount 이후 종료되는 일반 {@link TimerBase}를 만듭니다.
     */
    public TimerBase(int maxCount) {
        isInfinite = false;
        this.maxCount = maxCount;
    }

    /**
     * 종료되지 않는 무한 {@link TimerBase}를 만듭니다.
     */
    public TimerBase() {
        isInfinite = true;
        this.maxCount = 1;
    }

    private final class TimerTask extends Thread {
        @Override
        public void run() {
            if (isInfinite) {
                onProcess(count);
                count++;
            } else {
                if (count > 0) {
                    onProcess(count);
                    count--;
                } else {
                    stopTimer(false);
                }
            }
        }
    }

}