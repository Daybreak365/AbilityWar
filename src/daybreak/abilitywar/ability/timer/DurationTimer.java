package daybreak.abilitywar.ability.timer;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.library.tItle.Actionbar;
import daybreak.abilitywar.utils.math.NumberUtil;
import daybreak.abilitywar.utils.thread.TimerBase;

/**
 * Duration TimerBase (지속시간 타이머)
 * 능력의 지속시간을 관리하고 능력을 발동시키기 위해 만들어진 타이머입니다.
 * @author DayBreak 새벽
 */
abstract public class DurationTimer extends TimerBase {

    /**
     * 지속시간 초기화
     */
    public static void ResetDuration() {
        TimerBase.StopTasks(DurationTimer.class);
    }

    private final AbilityBase ability;
    private final int duration;
    private final CooldownTimer cooldownTimer;

    public DurationTimer(AbilityBase ability, int duration, CooldownTimer cooldownTimer) {
        super(duration);
        this.ability = ability;
        this.duration = duration;
        this.cooldownTimer = cooldownTimer;
    }

    public DurationTimer(AbilityBase Ability, int Duration) {
        this(Ability, Duration, null);
    }

    protected abstract void onDurationStart();

    protected abstract void onDurationProcess(int seconds);

    protected abstract void onDurationEnd();

    public boolean isDuration() {
        if (isRunning()) {
            ability.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6지속 시간 &f" + NumberUtil.parseTimeString(this.getFixedCount())));
        }

        return isRunning();
    }

    @Override
    public DurationTimer setPeriod(int Period) {
        super.setPeriod(Period);
        return this;
    }

    @Override
    public DurationTimer setSilentNotice(boolean forcedStopNotice) {
        super.setSilentNotice(forcedStopNotice);
        return this;
    }

    @Override
    protected void onStart() {
        this.onDurationStart();

        Counted = new ArrayList<>();
    }

    private ArrayList<Integer> Counted;

    @Override
    protected void onProcess(int Seconds) {
        Player target = ability.getPlayer();
        if (target != null) {
            this.onDurationProcess(Seconds);

            Actionbar actionbar = new Actionbar(ChatColor.translateAlternateColorCodes('&', "&6지속 시간 &f: &e" + NumberUtil.parseTimeString(this.getFixedCount())), 0, 25, 0);
            actionbar.sendTo(target);

            if (this.getFixedCount() == (duration / 2) && !Counted.contains(this.getFixedCount())) {
                Counted.add(this.getFixedCount());
                target.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6지속 시간 &f" + NumberUtil.parseTimeString(this.getFixedCount())));
                SoundLib.BLOCK_NOTE_BLOCK_HAT.playSound(target);
            } else if (this.getFixedCount() <= 5 && this.getFixedCount() >= 1 && !Counted.contains(this.getFixedCount())) {
                Counted.add(this.getFixedCount());
                target.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6지속 시간 &f" + NumberUtil.parseTimeString(this.getFixedCount())));
                SoundLib.BLOCK_NOTE_BLOCK_HAT.playSound(target);
            }
        }
    }

    @Override
    protected void onEnd() {
        Player target = ability.getPlayer();
        if (target != null) {
            onDurationEnd();

            if (cooldownTimer != null) {
                cooldownTimer.StartTimer();
            }

            target.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6지속 시간&f이 종료되었습니다."));
        }
    }

}
