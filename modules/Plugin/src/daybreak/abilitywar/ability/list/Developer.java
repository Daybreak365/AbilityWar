package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityBase.Cooldown.CooldownTimer;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.ability.decorator.TargetHandler;
import daybreak.abilitywar.game.AbstractGame.GameTimer;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.effect.Rooted;
import daybreak.abilitywar.game.manager.object.AbilitySelect.AbilityCollector;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.collect.SetUnion;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.base.random.Random;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import kotlin.ranges.RangesKt;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Predicate;

@AbilityManifest(name = "개발자", rank = Rank.SPECIAL, species = Species.HUMAN, explain = {
        "§a---------------------------------",
        "$(explain)",
        "§a---------------------------------",
        "§7패시브 §f- §3재설정§f: 50초마다 사용 가능한 모든 능력 중 무작위로 하나를 부여받습니다.",
        " 부여받은 능력이 남아있다면 자동으로 버립니다. 재설정으로 부여받은 능력의 모든",
        " 쿨타임이 빠르게 종료됩니다.",
        "§7아이템 버리기 §f- §3종료§f/§3디버깅§f: 재설정으로 부여받은 능력을 버리고 재설정 대기",
        " 시간을 단축합니다. §3/§f 이후 주위 10칸 이내의 모든 플레이어를 2.5초간 §5속박§f하고,",
        " 체력을 §d4hp§f + 주위의 플레이어 당 §d1hp§f만큼 §a회복§f합니다."
})
public class Developer extends AbilityBase implements ActiveHandler, TargetHandler {

    private final Object explain = new Object() {
        @Override
        public String toString() {
            if (hasAbility()) {
                final StringJoiner joiner = new StringJoiner("\n");
                joiner.add("§b" + ability.getName() + " §f[" + (ability.isRestricted() ? "§7능력 비활성화됨" : "§a능력 활성화됨") + "§f] " + ability.getRank().getRankName() + " " + ability.getSpecies().getSpeciesName());
                for (final Iterator<String> iterator = ability.getExplanation(); iterator.hasNext(); ) {
                    joiner.add(ChatColor.RESET + iterator.next());
                }
                return joiner.toString();
            } else return "능력이 없습니다.";
        }
    };

    private final Predicate<Entity> predicate = new Predicate<Entity>() {
        @Override
        public boolean test(Entity entity) {
            if (entity.equals(getPlayer())) return false;
            if (entity instanceof Player) {
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
            }
            return true;
        }
    };

    private final Random random = new Random();
    private final Circle rootCircle = Circle.of(1, 15), circle = Circle.of(10, 100);
    private AbilityBase ability;
    private final AbilityChanger abilityChanger = new AbilityChanger();

    public boolean hasAbility() {
        return ability != null;
    }

    public Developer(Participant participant) {
        super(participant);
    }

    @Override
    public boolean usesMaterial(Material material) {
        return super.usesMaterial(material) || (ability != null && ability.usesMaterial(material));
    }

    @Override
    public Set<GameTimer> getTimers() {
        if (hasAbility()) {
            return SetUnion.union(super.getTimers(), ability.getTimers());
        } else return super.getTimers();
    }

    @Override
    public Set<GameTimer> getRunningTimers() {
        if (hasAbility()) {
            return SetUnion.union(super.getRunningTimers(), ability.getRunningTimers());
        } else return super.getRunningTimers();
    }

    @Override
    public boolean ActiveSkill(Material material, ClickType clickType) {
        if (hasAbility()) {
            return ability instanceof ActiveHandler && ((ActiveHandler) ability).ActiveSkill(material, clickType);
        }
        return false;
    }

    @Override
    public void TargetSkill(Material material, LivingEntity entity) {
        if (hasAbility()) {
            if (ability instanceof TargetHandler) ((TargetHandler) ability).TargetSkill(material, entity);
        }
    }

    @Override
    protected void onUpdate(Update update) {
        if (update == Update.RESTRICTION_CLEAR) {
            if (ability != null) {
                ability.setRestricted(false);
            }
            abilityChanger.start();
        } else if (update == Update.RESTRICTION_SET) {
            if (ability != null) {
                ability.setRestricted(true);
            }
        } else if (update == Update.ABILITY_DESTROY) {
            if (ability != null) {
                ability.destroy();
                this.ability = null;
                abilityChanger.bossBar.removeAll();
            }
        }
    }

    @SubscribeEvent(onlyRelevant = true)
    private void onPlayerDropItem(PlayerDropItemEvent e) {
        if (abilityChanger.removeAbility()) {
            e.setCancelled(true);
        }
    }

    @SubscribeEvent(onlyRelevant = true)
    private void onPlayerJoin(PlayerJoinEvent e) {
        abilityChanger.bossBar.addPlayer(getPlayer());
    }

    private static final Note[] NOTES = {
            Note.natural(1, Tone.D), Note.flat(1, Tone.F), Note.natural(1, Tone.A)
    };
    private class AbilityChanger extends AbilityTimer {

        private final BossBar bossBar = Bukkit.createBossBar("§f능력 없음", BarColor.WHITE, BarStyle.SOLID);
        private final List<Class<? extends AbilityBase>> abilities = AbilityCollector.EVERY_ABILITY_EXCLUDING_BLACKLISTED.collect(getGame().getClass());
        private boolean accelerate = false;

        private AbilityChanger() {
            super(TaskType.NORMAL, 500);
            setPeriod(TimeUnit.TICKS, 2);
            setBehavior(RestrictionBehavior.PAUSE_RESUME);
        }

        @Override
        protected void onStart() {
            bossBar.addPlayer(getPlayer());
            bossBar.setProgress(1d);
            try {
                setAbility(random.pick(abilities));
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
            this.accelerate = false;
        }

        @Override
        protected void run(int count) {
            if (count <= 14) {
                if (count == 3 || count == 7) {
                    for (Note note : NOTES) {
                        SoundLib.PIANO.playInstrument(getPlayer(), note);
                    }
                }
                SoundLib.BLOCK_WOODEN_TRAPDOOR_CLOSE.playSound(getPlayer(), 1, 2);
                SoundLib.BLOCK_WOODEN_TRAPDOOR_OPEN.playSound(getPlayer(), 1, 2);
                int index = (count % 7) - 1;
                if (index == -1) index = 6;
                bossBar.setColor(BarColor.values()[index]);
            } else {
                if (accelerate) setCount(getCount() + 4);
            }
            if (count % 5 == 0 && hasAbility()) {
                for (GameTimer timer : ability.getRunningTimers()) {
                    if (timer instanceof CooldownTimer) {
                        timer.setCount(timer.getCount() - 1);
                    }
                }
            }
            bossBar.setProgress((double) (getMaximumCount() - count) / getMaximumCount());
        }

        @Override
        protected void onEnd() {
            start();
        }

        @Override
        protected void onSilentEnd() {
            bossBar.removeAll();
        }

        public void setAbility(final Class<? extends AbilityBase> clazz) throws ReflectiveOperationException {
            removeAbility();
            final AbilityBase ability = create(clazz, getParticipant());
            Developer.this.ability = ability;
            Developer.this.ability.setRestricted(false);
            for (String line : Formatter.formatAbilityInfo(ability)) {
                getPlayer().sendMessage(line);
            }
            bossBar.setTitle("§b" + ability.getName() + "§r " + ability.getRank().getRankName() + "§r " + ability.getSpecies().getSpeciesName());
        }

        public boolean removeAbility() {
            if (hasAbility()) {
                this.accelerate = true;
                ability.destroy();
                Developer.this.ability = null;
                bossBar.setTitle("§f능력 없음");
                final List<Player> players = LocationUtil.getEntitiesInCircle(Player.class, getPlayer().getLocation(), 10, predicate);
                for (Player player : players) {
                    final Participant participant = getGame().getParticipant(player);
                    Rooted.apply(participant, TimeUnit.TICKS, 50);
                    new AbilityTimer(TaskType.NORMAL, 20) {
                        @Override
                        protected void run(int count) {
                            for (Vector vector : rootCircle) {
                                ParticleLib.REDSTONE.spawnParticle(player.getLocation().clone().add(vector).add(0, count / 20.0 * 2, 0), RGB.AQUA);
                            }
                        }
                    }.setPeriod(TimeUnit.TICKS, 1).start();
                }

                final EntityRegainHealthEvent event = new EntityRegainHealthEvent(getPlayer(), 4 + players.size(), RegainReason.CUSTOM);
                Bukkit.getPluginManager().callEvent(event);
                if (!event.isCancelled()) {
                    getPlayer().setHealth(RangesKt.coerceIn(getPlayer().getHealth() + event.getAmount(), 0, getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
                }
                SoundLib.BLOCK_END_PORTAL_SPAWN.playSound(getPlayer(), 1, 2);
                SoundLib.BLOCK_END_PORTAL_SPAWN.playSound(getPlayer(), 1, 1);
                PotionEffects.BLINDNESS.addPotionEffect(getPlayer(), 20, 1, true);
                new AbilityTimer(TaskType.NORMAL, 11) {
                    final Location location = getPlayer().getLocation();
                    @Override
                    protected void run(int count) {
                        circle.rotateAroundAxisY(17);
                        for (Vector vector : circle) {
                            ParticleLib.REDSTONE.spawnParticle(location.clone().add(vector).add(0, (count - 6) / 5.0 * 2, 0), RGB.WHITE);
                        }
                    }
                }.setPeriod(TimeUnit.TICKS, 1).start();
                return true;
            } else return false;
        }

    }

}
