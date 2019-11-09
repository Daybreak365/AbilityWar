package daybreak.abilitywar.game.manager.object;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.config.AbilityWarSettings.Settings;
import daybreak.abilitywar.game.events.InvincibleEndEvent;
import daybreak.abilitywar.game.games.standard.Game;
import daybreak.abilitywar.utils.Bar;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.math.NumberUtil;
import daybreak.abilitywar.utils.thread.TimerBase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.EventExecutor;

/**
 * 무적
 *
 * @author DayBreak 새벽
 */
public class Invincibility implements EventExecutor {

	private final int duration = Settings.InvincibilitySettings.getDuration() * 60;

	private final boolean isBossbarEnabled = Settings.InvincibilitySettings.isBossbarEnabled();
	private final String bossbarMessage = Settings.InvincibilitySettings.getBossbarMessage();
	private final String bossbarInfiniteMessage = Settings.InvincibilitySettings.getBossbarInfiniteMessage();
	private final Game game;

	public Invincibility(Game game) {
		this.game = game;
		Bukkit.getPluginManager().registerEvent(EntityDamageEvent.class, game, EventPriority.HIGH, this,
				AbilityWar.getPlugin());
	}

	private TimerBase timer;

	public boolean Start(boolean isInfinite) {
		if (timer == null || !timer.isRunning()) {
			if (!isInfinite) {
				this.timer = new InvincibilityTimer(duration);
			} else {
				this.timer = new InvincibilityTimer();
			}
			timer.startTimer();
			return true;
		}
		return false;
	}

	public boolean Start(final int duration) {
        if (timer == null || !timer.isRunning()) {
            this.timer = new InvincibilityTimer(duration);
            return true;
        }
        return false;
	}

	public boolean Stop() {
		if (timer != null && timer.isRunning()) {
			timer.stopTimer(false);
			timer = null;
			return true;
		}
		return false;
	}

	public boolean isInvincible() {
		return this.timer != null && this.timer.isRunning();
	}

	@Override
	public void execute(Listener listener, Event event) {
		if (isInvincible() && event instanceof EntityDamageEvent) {
			EntityDamageEvent e = (EntityDamageEvent) event;
			if (e.getEntity() instanceof Player && game.isParticipating((Player) e.getEntity())) {
				e.setCancelled(true);
			}
		}
	}

	public interface Handler {
		Invincibility getInvincibility();
	}

	private class InvincibilityTimer extends TimerBase {

		private Bar bar = null;
		private final String startMessage;

		private InvincibilityTimer(int duration) {
			super(duration);
			this.startMessage = ChatColor.GREEN + "무적이 " + ChatColor.WHITE + NumberUtil.parseTimeString(duration) + ChatColor.GREEN + "동안 적용됩니다.";
			if (isBossbarEnabled) {
				int[] time = NumberUtil.parseTime(duration);
				bar = new Bar(String.format(bossbarMessage, time[0], time[1]), BarColor.GREEN, BarStyle.SEGMENTED_10);
			}
		}

		private InvincibilityTimer() {
			super();
			this.startMessage = ChatColor.GREEN + "무적이 적용되었습니다. 지금부터 무적이 해제될 때까지 데미지를 입지 않습니다.";
			if (isBossbarEnabled) {
				bar = new Bar(bossbarInfiniteMessage, BarColor.GREEN, BarStyle.SEGMENTED_10);
			}
		}

		{
			setSilentNotice(true);
		}

		@Override
		protected void onStart() {
			game.setRestricted(true);
			Bukkit.broadcastMessage(startMessage);
		}

		@Override
		protected void onProcess(int count) {
		    if (!isInfinite()) {
                if (bar != null) {
                    int[] time = NumberUtil.parseTime(count);
                    bar.setTitle(String.format(bossbarMessage, time[0], time[1])).setProgress(Math.min(count / (double) getMaxCount(), 1.0));
                }
                if (count == (getMaxCount()) / 2) {
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                            "&a무적이 &f" + NumberUtil.parseTimeString(count) + " &a후에 해제됩니다."));
                }
                if (count <= 5 && count >= 1) {
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                            "&a무적이 &f" + NumberUtil.parseTimeString(count) + " &a후에 해제됩니다."));
                    SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
                }
            }
		}

		@Override
		protected void onEnd() {
			if (bar != null) {
				bar.remove();
			}
			game.setRestricted(false);
			Bukkit.broadcastMessage(ChatColor.GREEN + "무적이 해제되었습니다. 지금부터 데미지를 입습니다.");
			SoundLib.ENTITY_ENDER_DRAGON_GROWL.broadcastSound();
			Bukkit.getPluginManager().callEvent(new InvincibleEndEvent(game));
		}
	}

}
