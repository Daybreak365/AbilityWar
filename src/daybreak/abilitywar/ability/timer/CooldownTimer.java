package daybreak.abilitywar.ability.timer;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.game.manager.object.WRECK;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.library.tItle.Actionbar;
import daybreak.abilitywar.utils.math.NumberUtil;
import daybreak.abilitywar.utils.thread.AbilityWarThread;
import daybreak.abilitywar.utils.thread.TimerBase;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Cooldown Timer (쿨타임 타이머)
 * 능력의 쿨타임을 관리하기 위해 만들어진 타이머입니다.
 * @author DayBreak 새벽
 */
public class CooldownTimer extends TimerBase {

    /**
     * 쿨타임 초기화
     */
    public static void ResetCool() {
        TimerBase.stopTasks(CooldownTimer.class);
    }

    private final AbilityBase ability;
    private final String abilityName;
    private boolean actionbarNotice = true;

    public CooldownTimer(AbilityBase ability, int Cool, String abilityName) {
        super((AbilityWarThread.isGameTaskRunning() && (AbilityWarThread.getGame() instanceof WRECK.Handler && ((WRECK.Handler) AbilityWarThread.getGame()).isWRECKEnabled())) ? (Cool / 10) : Cool);
        this.ability = ability;
        this.abilityName = abilityName;
    }

    public CooldownTimer(AbilityBase ability, int cool) {
        this(ability, cool, "");
    }

    public CooldownTimer setActionbarNotice(boolean bool) {
        this.actionbarNotice = bool;
        return this;
    }

    public boolean isCooldown() {
        if (isRunning()) {
            Player target = ability.getPlayer();
            if (target != null) {
                if (!abilityName.isEmpty()) {
                    target.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + abilityName + " 쿨타임 &f" + NumberUtil.parseTimeString(this.getCount())));
                } else {
                    target.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c쿨타임 &f" + NumberUtil.parseTimeString(this.getCount())));
                }
            }
        }

        return isRunning();
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onProcess(int count) {
        Player target = ability.getPlayer();
        if (target != null) {
            Actionbar actionbar;
            if (!abilityName.isEmpty()) {
                actionbar = new Actionbar(ChatColor.translateAlternateColorCodes('&', "&c" + abilityName + " 쿨타임 &f: &6" + NumberUtil.parseTimeString(this.getCount())), 0, 25, 0);

                if (count == (getMaxCount() / 2)) {
                    SoundLib.BLOCK_NOTE_BLOCK_HAT.playSound(target);
                    target.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + abilityName + " 쿨타임 &f" + NumberUtil.parseTimeString(this.getCount())));
                } else if (count <= 5 && count >= 1) {
                    SoundLib.BLOCK_NOTE_BLOCK_HAT.playSound(target);
                    target.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + abilityName + " 쿨타임 &f" + NumberUtil.parseTimeString(this.getCount())));
                }
            } else {
                actionbar = new Actionbar(ChatColor.translateAlternateColorCodes('&', "&c쿨타임 &f: &6" + NumberUtil.parseTimeString(this.getCount())), 0, 25, 0);

                if (count == (getMaxCount() / 2)) {
                    SoundLib.BLOCK_NOTE_BLOCK_HAT.playSound(target);
                    target.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c쿨타임 &f" + NumberUtil.parseTimeString(this.getCount())));
                } else if (count <= 5 && count >= 1) {
                    SoundLib.BLOCK_NOTE_BLOCK_HAT.playSound(target);
                    target.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c쿨타임 &f" + NumberUtil.parseTimeString(this.getCount())));
                }
            }

            if (actionbarNotice) actionbar.sendTo(target);
        }
    }

    @Override
    public void onEnd() {
        Player target = ability.getPlayer();
        if (target != null) {
            Actionbar actionbar = new Actionbar(ChatColor.translateAlternateColorCodes('&', "&a능력을 다시 사용할 수 있습니다."), 0, 50, 0);
            if (actionbarNotice) actionbar.sendTo(target);
            target.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a능력을 다시 사용할 수 있습니다."));
        }
    }

}
