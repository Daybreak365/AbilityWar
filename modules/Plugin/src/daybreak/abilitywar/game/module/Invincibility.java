package daybreak.abilitywar.game.module;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.config.Configuration.Settings.InvincibilitySettings;
import daybreak.abilitywar.game.AbstractGame.GameTimer;
import daybreak.abilitywar.game.Game;
import daybreak.abilitywar.game.event.InvincibilityStatusChangeEvent;
import daybreak.abilitywar.utils.base.TimeUtil;
import daybreak.abilitywar.utils.base.TimeUtil.ParsedTime;
import daybreak.abilitywar.utils.base.minecraft.BroadBar;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * 무적 관리 모듈
 * @author Daybreak 새벽
 */
@ModuleBase(Invincibility.class)
public class Invincibility implements Module {

	private final int duration = InvincibilitySettings.getDuration();
	private final String message = InvincibilitySettings.getBossbarMessage(), infiniteMessage = InvincibilitySettings.getBossbarInfiniteMessage();
	private final Game game;
	private final Set<Observer> observers = new HashSet<>();
	private GameTimer timer;

	public Invincibility(Game game) {
		this.game = game;
	}

	public final void attachObserver(Observer observer) {
		observers.add(observer);
	}

	public boolean start(boolean isInfinite) {
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

	public boolean start(final int duration) {
		if (timer == null || !timer.isRunning()) {
			this.timer = new InvincibilityTimer(duration);
			timer.start();
			return true;
		}
		return false;
	}

	public boolean stop() {
		if (timer != null && timer.isRunning()) {
			timer.stop(false);
			timer = null;
			return true;
		}
		return false;
	}

	public boolean isEnabled() {
		return this.timer != null && this.timer.isRunning();
	}

	@Override
	public void register() {}

	@Override
	public void unregister() {}

	public interface Observer {
		void onStart();
		void onEnd();
	}

	public interface Handler {
		Invincibility getInvincibility();
	}

	private class InvincibilityTimer extends GameTimer implements Listener {

		private BroadBar bossBar = null;
		private int half, twentyPercent;

		private InvincibilityTimer(int duration) {
			game.super(TaskType.REVERSE, duration);
			if (InvincibilitySettings.isBossbarEnabled()) {
				final int[] time = TimeUtil.parseTime(duration);
				this.bossBar = new BroadBar(String.format(message, time[0], time[1]), BarColor.GREEN, BarStyle.SEGMENTED_12);
				this.half = duration / 2;
				this.twentyPercent = duration / 5;
			}
		}

		private InvincibilityTimer() {
			game.super(TaskType.INFINITE, -1);
			if (InvincibilitySettings.isBossbarEnabled()) {
				this.bossBar = new BroadBar(infiniteMessage, BarColor.GREEN, BarStyle.SEGMENTED_12);
			}
		}

		@EventHandler
		private void onEntityDamage(final EntityDamageEvent e) {
			if (game.isParticipating(e.getEntity().getUniqueId())) {
				e.setCancelled(true);
			}
		}

		@EventHandler
		private void onEntityDamageByEntity(final EntityDamageByEntityEvent e) {
			this.onEntityDamage(e);
		}

		@EventHandler
		private void onEntityDamageByBlock(final EntityDamageByBlockEvent e) {
			this.onEntityDamage(e);
		}

		@Override
		protected void onStart() {
			game.setRestricted(true);
			for (Invincibility.Observer observer : observers) {
				observer.onStart();
			}
			if (getTaskType() == TaskType.INFINITE) {
				Bukkit.broadcastMessage(ChatColor.GREEN + "무적이 적용되었습니다. 지금부터 무적이 해제될 때까지 대미지를 입지 않습니다.");
			} else {
				Bukkit.broadcastMessage(ChatColor.GREEN + "무적이 " + ChatColor.WHITE + TimeUtil.parseTimeAsString(getMaximumCount()) + ChatColor.GREEN + "동안 적용됩니다.");
			}
			Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
			Bukkit.getPluginManager().callEvent(new InvincibilityStatusChangeEvent(game, true));
		}

		@Override
		protected void run(int count) {
			if (getTaskType() != TaskType.INFINITE) {
				if (this.bossBar != null) {
					final ParsedTime parsedTime = ParsedTime.parse(count);
					this.bossBar.setTitle(String.format(message, parsedTime.minutes, parsedTime.seconds));
					this.bossBar.setProgress(Math.min(count / (double) getMaximumCount(), 1.0));
					if (count > twentyPercent && count <= half) {
						this.bossBar.setColor(BarColor.YELLOW);
					} else if (count <= twentyPercent) {
						this.bossBar.setColor(BarColor.RED);
					}
				}
				if (count == (getMaximumCount()) / 2 || (count <= 5 && count >= 1)) {
					Bukkit.broadcastMessage("§a무적 시간이 §f" + TimeUtil.parseTimeAsString(count) + " §a후에 종료됩니다.");
					SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				}
			}
		}

		@Override
		protected void onEnd() {
			onSilentEnd();
			for (Invincibility.Observer observer : observers) {
				observer.onEnd();
			}
			game.setRestricted(false);
			Bukkit.broadcastMessage(ChatColor.GREEN + "무적이 해제되었습니다. 지금부터 대미지를 입습니다.");
			SoundLib.ENTITY_ENDER_DRAGON_GROWL.broadcastSound();
			Bukkit.getPluginManager().callEvent(new InvincibilityStatusChangeEvent(game, false));
		}

		@Override
		protected void onSilentEnd() {
			HandlerList.unregisterAll(this);
			if (this.bossBar != null) {
				this.bossBar.unregister();
			}
		}

	}

}
