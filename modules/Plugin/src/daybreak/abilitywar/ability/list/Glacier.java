package daybreak.abilitywar.ability.list;

import com.google.common.base.Strings;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Effect;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.effect.Frost;
import daybreak.abilitywar.game.manager.effect.Frost.ParticipantFrost;
import daybreak.abilitywar.game.manager.effect.event.ParticipantNewEffectApplyEvent;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer.Observer;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.minecraft.damage.Damages;
import daybreak.abilitywar.utils.base.minecraft.entity.health.Healths;
import daybreak.abilitywar.utils.base.minecraft.nms.IHologram;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

@AbilityManifest(name = "글래시어", rank = Rank.S, species = Species.HUMAN, explain = {
        "§7공격 §8- §b결빙§7/§b쇄빙§f: §5빙결 §f상태가 아닌 적을 네 번 공격할 때마다 3초간",
        " §5빙결§f시킵니다. §7/ §5빙결 §f상태인 적에게 근접 공격으로 피해를 입힐 수 있습니다.",
        " §5빙결§f의 지속시간을 0.3초 소모하여 0.4의 고정 피해를 입히며, 공격 쿨타임이",
        " 적용되지 않습니다. §b쇄빙§f을 이용해 피해를 입힐 때마다 §b빙하기§f의 쿨타임을",
        " 1초씩 감소시킵니다.",
        "§7패시브 §8- §b얼어붙은 심장§f: 본인의 §5빙결 §f상태이상이 빠르게 종료되며, §5빙결 §f상태에서",
        " 매 초 잃은 체력에 비례해 체력을 회복합니다.",
        "§7철괴 우클릭 §8- §b빙하기§f: 본인을 포함한 주변 8칸 이내의 모든 플레이어를 6초간",
        " §5빙결§f시킵니다. $[COOLDOWN_CONFIG]",
        "§a[§e능력 제공자§a] §dspace_kdd"
})
@Beta
public class Glacier extends AbilityBase implements ActiveHandler {

    public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Glacier.class, "cooldown", 60,
            "# 쿨타임") {

        @Override
        public boolean condition(Integer value) {
            return value >= 0;
        }

        @Override
        public String toString() {
            return Formatter.formatCooldown(getValue());
        }

    };

    private final Cooldown cooldown = new Cooldown(COOLDOWN_CONFIG.getValue(), "빙하기", 0);

    private final Map<UUID, Stack> stacks = new HashMap<>();
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

    public Glacier(Participant participant) {
        super(participant);
    }

    @Override
    public boolean ActiveSkill(Material material, ClickType clickType) {
        if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !cooldown.isCooldown()) {
            cooldown.start();
            for (Player target : LocationUtil.getNearbyEntities(Player.class, getPlayer().getLocation(), 8, 8, predicate)) {
                Frost.apply(getGame().getParticipant(target), TimeUnit.SECONDS, 6);
            }
            Frost.apply(getParticipant(), TimeUnit.SECONDS, 6);
            return true;
        }
        return false;
    }

    @SubscribeEvent(onlyRelevant = true)
    private void onPlayerJoin(PlayerJoinEvent e) {
        for (Stack stack : stacks.values()) {
            stack.hologram.display(getPlayer());
        }
    }

    @Nullable
    private static Entity getDamager(final Entity damager) {
        if (damager instanceof Projectile) {
            final ProjectileSource shooter = ((Projectile) damager).getShooter();
            return shooter instanceof Entity ? (Entity) shooter : null;
        } else return damager;
    }

    @SubscribeEvent(ignoreCancelled = true)
    private void onAttack(EntityDamageByEntityEvent e) {
        if (getPlayer().equals(getDamager(e.getDamager())) && predicate.test(e.getEntity()) && !getGame().getParticipant(e.getEntity().getUniqueId()).hasEffect(Frost.registration)) {
            final Stack stack = stacks.get(e.getEntity().getUniqueId());
            if (stack != null) {
                if (stack.addStack()) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Frost.apply(getGame().getParticipant(stack.getPlayer()), TimeUnit.SECONDS, 3);
                        }
                    }.runTaskLater(AbilityWar.getPlugin(), 2L);
                }
            } else new Stack((Player) e.getEntity()).start();
        }
    }

    @SubscribeEvent
    private void onDeath(EntityDeathEvent e) {
        if (stacks.containsKey(e.getEntity().getUniqueId())) stacks.get(e.getEntity().getUniqueId()).stop(true);
    }

    @SubscribeEvent
    private void onDeath(PlayerDeathEvent e) {
        if (stacks.containsKey(e.getEntity().getUniqueId())) stacks.get(e.getEntity().getUniqueId()).stop(true);
    }

    @SubscribeEvent(onlyRelevant = true)
    private void onInteractBlock(PlayerInteractEvent e) {
        if (e.getAction() == Action.LEFT_CLICK_BLOCK && e.getClickedBlock() != null) {
            final ParticipantFrost frost = ParticipantFrost.getFrost(e.getClickedBlock().getLocation());
            if (frost != null && frost.getParticipant() != getParticipant()) {
                final Player target = frost.getParticipant().getPlayer();
                frost.noDamage = false;
                if (Damages.canDamage(target, getPlayer(), DamageCause.CUSTOM, 0.3)) {
                    frost.setCount(frost.getCount() - 6);
                    final Location loc = target.getLocation();
                    ParticleLib.BLOCK_CRACK.spawnParticle(loc, 1.5, 1.5, 1.5, 20, MaterialX.ICE);
                    SoundLib.BLOCK_GLASS_BREAK.playSound(loc, .45f, 0);
                    SoundLib.BLOCK_GLASS_BREAK.playSound(loc, .45f, 1);
                    SoundLib.BLOCK_GLASS_BREAK.playSound(loc, .45f, 2);
                    target.setHealth(Math.max(0, target.getHealth() - .4));
                    NMS.broadcastEntityEffect(target, (byte) 2);
                    if (cooldown.isRunning()) cooldown.setCount(Math.max(cooldown.getCount() - 1, 0));
                }
                frost.noDamage = true;
            }
        }
    }

    @SubscribeEvent
    private void onChat(AsyncPlayerChatEvent e) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Frost.apply(getParticipant(), TimeUnit.SECONDS, 10);
            }
        }.runTask(AbilityWar.getPlugin());
    }

    @SubscribeEvent(onlyRelevant = true)
    private void onEffectApply(ParticipantNewEffectApplyEvent e) {
        if (e.getEffect().getRegistration() == Frost.registration) {
            final Effect effect = e.getEffect();
            effect.attachObserver(new Observer() {
                @Override
                public void run(int count) {
                    effect.setCount(Math.max(0, effect.getCount() - 2));
                    final double healthLoss = Healths.getMaxHealth(getPlayer()) - getPlayer().getHealth();
                    Healths.setHealth(getPlayer(), getPlayer().getHealth() + (healthLoss / 100));
                }
            });
        }
    }

    private class Stack extends AbilityTimer {

        private final Player player;
        private final IHologram hologram;
        private int stack = 0;

        private Stack(Player player) {
            super();
            setPeriod(TimeUnit.TICKS, 4);
            this.player = player;
            this.hologram = NMS.newHologram(player.getWorld(), player.getLocation().getX(), player.getLocation().getY() + player.getEyeHeight() + 0.6, player.getLocation().getZ(), Strings.repeat("§b✦", stack).concat(Strings.repeat("§b✧", 4 - stack)));
            hologram.display(Glacier.this.getPlayer());
            stacks.put(player.getUniqueId(), this);
            addStack();
        }

        @Override
        protected void run(int count) {
            hologram.teleport(player.getWorld(), player.getLocation().getX(), player.getLocation().getY() + player.getEyeHeight() + 0.6, player.getLocation().getZ(), player.getLocation().getYaw(), 0);
        }

        public Player getPlayer() {
            return player;
        }

        private boolean addStack() {
            stack++;
            hologram.setText(Strings.repeat("§b✦", stack).concat(Strings.repeat("§b✧", 4 - stack)));
            if (stack >= 4) {
                stop(false);
                return true;
            }
            return false;
        }

        @Override
        protected void onEnd() {
            onSilentEnd();
        }

        @Override
        protected void onSilentEnd() {
            hologram.unregister();
            stacks.remove(player.getUniqueId());
        }
    }

}
