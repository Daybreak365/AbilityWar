package daybreak.abilitywar.game.list.mix.synergy.list;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;
import daybreak.abilitywar.game.manager.effect.Stun;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.VectorUtil;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Note;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

@AbilityManifest(name = "히히 못 가", rank = Rank.S, species = Species.OTHERS, explain = {
        "적을 공격하면 대상을 자신의 방향으로 끌어오며, 이후 $[DURATION_CONFIG]초간 기절시킵니다.",
        "§3[§b아이디어 제공§3] §f매스님"
})
public class Motga extends Synergy {

    public static final SettingObject<Integer> DURATION_CONFIG = synergySettings.new SettingObject<Integer>(Motga.class, "duration", 7,
            "# 기절 지속 시간 (틱 단위)") {

        @Override
        public boolean condition(Integer value) {
            return value >= 0;
        }

        @Override
        public String toString() {
            return String.valueOf(getValue() / 20.0);
        }

    };

    private final Predicate<Entity> predicate = new Predicate<Entity>() {
        @Override
        public boolean test(Entity entity) {
            if (entity.equals(getPlayer())) return false;
            if (!getGame().isParticipating(entity.getUniqueId())
                    || (getGame() instanceof DeathManager.Handler && ((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
                    || !getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue()) {
                return false;
            }
            if (getGame() instanceof Teamable) {
                final Teamable teamGame = (Teamable) getGame();
                final Participant entityParticipant = teamGame.getParticipant(entity.getUniqueId()), participant = getParticipant();
                return !teamGame.hasTeam(entityParticipant) || !teamGame.hasTeam(participant) || (!teamGame.getTeam(entityParticipant).equals(teamGame.getTeam(participant)));
            }
            return true;
        }
    };

    public Motga(Participant participant) {
        super(participant);
    }

    private final int stunDuration = DURATION_CONFIG.getValue();

    @Nullable
    private static Entity getDamager(final Entity damager) {
        if (damager instanceof Projectile) {
            final ProjectileSource shooter = ((Projectile) damager).getShooter();
            return shooter instanceof Entity ? (Entity) shooter : null;
        } else return damager;
    }

    @SubscribeEvent(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (getPlayer().equals(getDamager(e.getDamager())) && predicate.test(e.getEntity())) {
            final Entity entity = e.getEntity();
            final Participant target = getGame().getParticipant(entity.getUniqueId());
            new BukkitRunnable() {
                @Override
                public void run() {
                    entity.setVelocity(VectorUtil.validateVector(getPlayer().getLocation().toVector().subtract(entity.getLocation().toVector()).normalize()).multiply(0.65).setY(0.15));
                }
            }.runTaskLater(AbilityWar.getPlugin(), 1);
            SoundLib.PIANO.playInstrument(getPlayer(), Note.flat(0, Note.Tone.D));
            new AbilityTimer(1) {
                @Override
                protected void onEnd() {
                    SoundLib.ENTITY_ITEM_PICKUP.playSound(getPlayer());
                    SoundLib.ENTITY_ITEM_PICKUP.playSound(target.getPlayer());
                    Stun.apply(target, TimeUnit.TICKS, stunDuration);
                }
            }.setPeriod(TimeUnit.TICKS, 8).start();
        }
    }

}
