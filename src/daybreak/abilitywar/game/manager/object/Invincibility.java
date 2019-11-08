package daybreak.abilitywar.game.manager.object;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.config.AbilityWarSettings.Settings;
import daybreak.abilitywar.game.events.InvincibleEndEvent;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.game.games.standard.Game;
import daybreak.abilitywar.utils.Bar;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.library.tItle.Title;
import daybreak.abilitywar.utils.math.NumberUtil;
import daybreak.abilitywar.utils.thread.TimerBase;
import java.util.Optional;
import java.util.function.Consumer;
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

    private final int duration = Settings.InvincibilitySettings.getDuration();
    private final int durationSeconds = duration * 60;

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

    public boolean Start(boolean infinite) {
        if (this.timer == null || !this.timer.isRunning()) {
            if (!infinite) {
                this.timer = new TimerBase(duration * 60) {

                    private Optional<Bar> bar = Optional.empty();

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
                                "&a무적이 &f" + NumberUtil.parseTimeString(duration * 60) + "&a동안 적용됩니다."));
                        if (isBossbarEnabled) {
                            int[] time = NumberUtil.parseTime(durationSeconds);
                            bar = Optional.of(new Bar(String.format(bossbarMessage, time[0], time[1]), BarColor.GREEN, BarStyle.SEGMENTED_10));
                        }
                    }

                    @Override
                    protected void onProcess(int count) {
                        bar.ifPresent(new Consumer<Bar>() {
                            @Override
                            public void accept(Bar bar) {
                                int[] time = NumberUtil.parseTime(count);
                                bar.setTitle(String.format(bossbarMessage, time[0], time[1])).setProgress(Math.min(count / (double) durationSeconds, 1.0));
                            }
                        });
                        if (count == (duration * 60) / 2) {
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
                        bar.ifPresent(Bar::remove);
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

                }.setSilentNotice(true);
            } else {
                this.timer = new TimerBase() {

                    private Optional<Bar> bar = Optional.empty();

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
                        if (isBossbarEnabled) {
                            bar = Optional.of(new Bar(bossbarInfiniteMessage, BarColor.GREEN, BarStyle.SEGMENTED_10));
                        }
                    }

                    @Override
                    protected void onProcess(int count) {
                    }

                    @Override
                    protected void onEnd() {
                        bar.ifPresent(Bar::remove);
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

            this.timer.startTimer();
            return true;
        }

        return false;
    }

    public boolean Stop() {
        if (this.timer != null && this.timer.isRunning()) {
            this.timer.stopTimer(false);
            this.timer = null;
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

}
