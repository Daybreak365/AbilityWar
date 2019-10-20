package daybreak.abilitywar.ability.timer;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.game.manager.WRECK;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.library.tItle.Actionbar;
import daybreak.abilitywar.utils.math.NumberUtil;
import daybreak.abilitywar.utils.thread.AbilityWarThread;
import daybreak.abilitywar.utils.thread.TimerBase;

/**
 * Cooldown Timer (쿨타임 타이머)
 * @author DayBreak 새벽
 */
public class CooldownTimer extends TimerBase {
	
	/**
	 * 쿨타임 초기화
	 */
	public static void ResetCool() {
		TimerBase.StopTasks(CooldownTimer.class);
	}
	
	private final AbilityBase Ability;
	private String AbilityName = "";
	private boolean actionbarNotice = true;

	public CooldownTimer(AbilityBase Ability, Integer Cool) {
		super((AbilityWarThread.isGameTaskRunning() && (AbilityWarThread.getGame() instanceof WRECK.Handler && ((WRECK.Handler) AbilityWarThread.getGame()).isWRECKEnabled())) ? (int)(Cool / 10) : Cool);
		this.Ability = Ability;
	}

	public CooldownTimer(AbilityBase Ability, Integer Cool, String AbilityName) {
		this(Ability, Cool);
		this.AbilityName = AbilityName;
	}

	public CooldownTimer setActionbarNotice(boolean bool) {
		this.actionbarNotice = bool;
		return this;
	}
	
	public boolean isCooldown() {
		if(isTimerRunning()) {
			Player target = Ability.getPlayer();
			if(target != null) {
				if(!AbilityName.isEmpty()) {
					target.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + AbilityName + " 쿨타임 &f" + NumberUtil.parseTimeString(this.getCount())));
				} else {
					target.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c쿨타임 &f" + NumberUtil.parseTimeString(this.getCount())));
				}
			}
		}
		
		return isTimerRunning();
	}

	@Override
	public void onStart() {}
	
	@Override
	public void TimerProcess(Integer Seconds) {
		Player target = Ability.getPlayer();
		if(target != null) {
			Actionbar actionbar;
			if(!AbilityName.isEmpty()) {
				actionbar = new Actionbar(ChatColor.translateAlternateColorCodes('&', "&c" + AbilityName + " 쿨타임 &f: &6" + NumberUtil.parseTimeString(this.getCount())), 0, 25, 0);
				
				if(Seconds == (getMaxCount() / 2)) {
					SoundLib.BLOCK_NOTE_BLOCK_HAT.playSound(target);
					target.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + AbilityName + " 쿨타임 &f" + NumberUtil.parseTimeString(this.getCount())));
				} else if(Seconds <= 5 && Seconds >= 1) {
					SoundLib.BLOCK_NOTE_BLOCK_HAT.playSound(target);
					target.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + AbilityName + " 쿨타임 &f" + NumberUtil.parseTimeString(this.getCount())));
				}
			} else {
				actionbar = new Actionbar(ChatColor.translateAlternateColorCodes('&', "&c쿨타임 &f: &6" + NumberUtil.parseTimeString(this.getCount())), 0, 25, 0);
				
				if(Seconds == (getMaxCount() / 2)) {
					SoundLib.BLOCK_NOTE_BLOCK_HAT.playSound(target);
					target.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c쿨타임 &f" + NumberUtil.parseTimeString(this.getCount())));
				} else if(Seconds <= 5 && Seconds >= 1) {
					SoundLib.BLOCK_NOTE_BLOCK_HAT.playSound(target);
					target.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c쿨타임 &f" + NumberUtil.parseTimeString(this.getCount())));
				}
			}

			if(actionbarNotice) actionbar.sendTo(target);
		}
	}
	
	@Override
	public void onEnd() {
		Player target = Ability.getPlayer();
		if(target != null) {
			Actionbar actionbar = new Actionbar(ChatColor.translateAlternateColorCodes('&', "&a능력을 다시 사용할 수 있습니다."), 0, 50, 0);
			if(actionbarNotice) actionbar.sendTo(target);
			target.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a능력을 다시 사용할 수 있습니다."));
		}
	}
	
}
