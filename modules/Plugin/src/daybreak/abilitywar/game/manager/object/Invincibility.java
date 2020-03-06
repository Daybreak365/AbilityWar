package daybreak.abilitywar.game.manager.object;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.game.AbstractGame.GameTimer;
import daybreak.abilitywar.game.Game;
import daybreak.abilitywar.game.event.InvincibleEndEvent;
import daybreak.abilitywar.utils.base.TimeUtil;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.EventExecutor;

/**
 * 무적
 *
 * @author Daybreak 새벽
 */
public class Invincibility implements EventExecutor {

	private final int duration = Settings.InvincibilitySettings.getDuration();

	private final Game game;

	public Invincibility(Game game) {
		this.game = game;
		Bukkit.getPluginManager().registerEvent(EntityDamageEvent.class, game, EventPriority.HIGH, this, AbilityWar.getPlugin());
	}

	private GameTimer timer;

	public boolean Start(boolean isInfinite) {
		if (timer == null || !timer.isRunning()) {
			if (!isInfinite) {
				this.timer = new InvincibilityTimer(duration);
			} else {
				this.timer = new InvincibilityTimer();
			}
			timer.start();
			return true;
		}
		return false;
	}

	public boolean Start(final int duration) {
		if (timer == null || !timer.isRunning()) {
			this.timer = new InvincibilityTimer(duration);
			timer.start();
			return true;
		}
		return false;
	}

	public boolean Stop() {
		if (timer != null && timer.isRunning()) {
			timer.stop(false);
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

	private class InvincibilityTimer extends GameTimer {

		private final String startMessage;

		private InvincibilityTimer(int duration) {
			game.super(TaskType.REVERSE, duration);
			this.startMessage = ChatColor.GREEN + "무적이 " + ChatColor.WHITE + TimeUtil.parseTimeAsString(duration) + ChatColor.GREEN + "동안 적용됩니다.";
		}

		private InvincibilityTimer() {
			game.super(TaskType.INFINITE, -1);
			this.startMessage = ChatColor.GREEN + "무적이 적용되었습니다. 지금부터 무적이 해제될 때까지 대미지를 입지 않습니다.";
		}

		@Override
		protected void onStart() {
			game.setRestricted(true);
			Bukkit.broadcastMessage(startMessage);
		}

		@Override
		protected void run(int count) {
			if (getTaskType() != TaskType.INFINITE) {
				if (count == (getMaximumCount()) / 2 || (count <= 5 && count >= 1)) {
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
							"&a무적이 &f" + TimeUtil.parseTimeAsString(count) + " &a후에 해제됩니다."));
					SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				}
			}
		}

		@Override
		protected void onEnd() {
			game.setRestricted(false);
			Bukkit.broadcastMessage(ChatColor.GREEN + "무적이 해제되었습니다. 지금부터 대미지를 입습니다.");
			SoundLib.ENTITY_ENDER_DRAGON_GROWL.broadcastSound();
			Bukkit.getPluginManager().callEvent(new InvincibleEndEvent(game));
		}

	}

}
