package daybreak.abilitywar.game.manager.object;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.config.AbilityWarSettings.Settings;
import daybreak.abilitywar.game.events.InvincibleEndEvent;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.game.games.standard.Game;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.library.tItle.Title;
import daybreak.abilitywar.utils.math.NumberUtil;
import daybreak.abilitywar.utils.thread.TimerBase;
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
 * @author DayBreak 새벽
 */
public class Invincibility implements EventExecutor {

	private final Integer Duration = Settings.getInvincibilityDuration();
	private final Game game;

	public Invincibility(Game game) {
		this.game = game;
		Bukkit.getPluginManager().registerEvent(EntityDamageEvent.class, game, EventPriority.HIGH, this,
				AbilityWar.getPlugin());
	}

	private TimerBase invincibilityTimer;

	public boolean Start(boolean Infinite) {
		if (this.invincibilityTimer == null || !this.invincibilityTimer.isRunning()) {
			if (!Infinite) {
				this.invincibilityTimer = new TimerBase(Duration * 60) {

					@Override
					protected void onStart() {
						game.setRestricted(true);
						for (Participant participant : game.getParticipants()) {
							if (participant.hasAbility()) {
								AbilityBase ability = participant.getAbility();
								ability.setRestricted(true);
							}
						}

						Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
								"&a무적이 &f" + NumberUtil.parseTimeString(Duration * 60) + "&a동안 적용됩니다."));
					}

					@Override
					protected void onProcess(int count) {
						if (count == (Duration * 60) / 2) {
							Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
									"&a무적이 &f" + NumberUtil.parseTimeString(count) + " &a후에 해제됩니다."));
						}

						if (count <= 5 && count >= 1) {
							Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
									"&a무적이 &f" + NumberUtil.parseTimeString(count) + " &a후에 해제됩니다."));
							SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
						}
					}

					@Override
					protected void onEnd() {
						game.setRestricted(false);
						Title titlePacket = new Title(ChatColor.translateAlternateColorCodes('&', "&c&lWarning"),
								ChatColor.translateAlternateColorCodes('&', "&f무적이 해제되었습니다."), 20, 60, 20);
						titlePacket.Broadcast();
						SoundLib.ENTITY_ENDER_DRAGON_AMBIENT.broadcastSound();

						for (Participant participant : game.getParticipants()) {
							if (participant.hasAbility()) {
								participant.getAbility().setRestricted(false);
							}
						}

						Bukkit.getPluginManager().callEvent(new InvincibleEndEvent(game));
					}

				};
			} else {
				this.invincibilityTimer = new TimerBase() {

					@Override
					protected void onStart() {
						game.setRestricted(true);
						for (Participant participant : game.getParticipants()) {
							if (participant.hasAbility()) {
								AbilityBase ability = participant.getAbility();
								ability.setRestricted(true);
							}
						}
						Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
								"&a무적이 적용되었습니다. 지금부터 무적이 해제될 때까지 데미지를 입지 않습니다."));
					}

					@Override
					protected void onProcess(int count) {}

					@Override
					protected void onEnd() {
						game.setRestricted(false);
						Title titlePacket = new Title(ChatColor.translateAlternateColorCodes('&', "&c&lWarning"),
								ChatColor.translateAlternateColorCodes('&', "&f무적이 해제되었습니다."), 20, 60, 20);
						titlePacket.Broadcast();
						SoundLib.ENTITY_ENDER_DRAGON_AMBIENT.broadcastSound();

						for (Participant participant : game.getParticipants()) {
							if (participant.hasAbility()) {
								participant.getAbility().setRestricted(false);
							}
						}

						Bukkit.getPluginManager().callEvent(new InvincibleEndEvent(game));
					}

				};
			}

			this.invincibilityTimer.startTimer();
			return true;
		}

		return false;
	}

	public boolean Stop() {
		if (this.invincibilityTimer != null && this.invincibilityTimer.isRunning()) {
			this.invincibilityTimer.stopTimer(false);
			return true;
		}

		return false;
	}

	public boolean isInvincible() {
		return this.invincibilityTimer != null && this.invincibilityTimer.isRunning();
	}

	@Override
	public void execute(Listener listener, Event event) {
		if (event instanceof EntityDamageEvent) {
			if (this.isInvincible()) {
				EntityDamageEvent e = (EntityDamageEvent) event;
				if (e.getEntity() instanceof Player) {
					Player p = (Player) e.getEntity();
					if (game.isParticipating(p)) {
						e.setCancelled(true);
					}
				}
			}
		}
	}

	public interface Handler {
		Invincibility getInvincibility();
	}

}
