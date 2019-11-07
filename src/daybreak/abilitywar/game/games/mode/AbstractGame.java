package daybreak.abilitywar.game.games.mode;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityBase.ClickType;
import daybreak.abilitywar.ability.AbilityBase.MaterialType;
import daybreak.abilitywar.game.manager.object.EffectManager;
import daybreak.abilitywar.game.manager.passivemanager.PassiveManager;
import daybreak.abilitywar.utils.thread.OverallTimer;
import daybreak.abilitywar.utils.thread.TimerBase;
import daybreak.abilitywar.utils.versioncompat.VersionUtil;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;


import static daybreak.abilitywar.utils.Validate.notNull;

public abstract class AbstractGame extends OverallTimer implements Listener, EffectManager.Handler {

    private final ArrayList<Listener> registeredListeners = new ArrayList<>();

    /**
     * 게임이 종료될 때 등록 해제되어야 하는 {@link Listener}를 등록합니다.
     */
    public final void registerListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(notNull(listener), AbilityWar.getPlugin());
        registeredListeners.add(listener);
    }

    private boolean restricted = true;
    private boolean gameStarted = false;

    private ParticipantStrategy participantStrategy;
    private final PassiveManager passiveManager = new PassiveManager(this);
    private final EffectManager effectManager = new EffectManager(this);

    public AbstractGame(Collection<Player> players) {
        this.participantStrategy = new ParticipantStrategy.DEFAULT_MANAGEMENT(this, players);
    }

    public void setParticipantStrategy(ParticipantStrategy participantStrategy) {
        this.participantStrategy = notNull(participantStrategy);
    }

    /**
     * PassiveManager을 반환합니다.
     * <p>
     * null을 반환하지 않습니다.
     */
    public PassiveManager getPassiveManager() {
        return passiveManager;
    }

    /**
     * EffectManager를 반환합니다.
     * <p>
     * null을 반환하지 않습니다.
     */
    public EffectManager getEffectManager() {
        return effectManager;
    }

    /**
     * 참여자 목록을 반환합니다.
     *
     * @return 참여자 목록
     */
    public Collection<Participant> getParticipants() {
        return participantStrategy.getParticipants();
    }

    /**
     * {@link Player}를 기반으로 하는 {@link Participant}를 탐색합니다.
     *
     * @param player 탐색할 플레이어
     * @return 존재할 경우 {@link Participant}를 반환합니다. 존재하지 않을 경우 null을 반환합니다.
     * null을 반환할 수 있습니다.
     */
    public final Participant getParticipant(Player player) {
        return participantStrategy.getParticipant(player.getUniqueId());
    }

    /**
     * 해당 {@link UUID}를 가지고 있는 {@link Player}를 기반으로 하는 {@link Participant}를 탐색합니다.
     *
     * @param uuid 탐색할 플레이어의 UUID
     * @return 존재할 경우 {@link Participant}를 반환합니다. 존재하지 않을 경우 null을 반환합니다.
     * null을 반환할 수 있습니다.
     */
    public final Participant getParticipant(UUID uuid) {
        return participantStrategy.getParticipant(uuid);
    }

    /**
     * 대상 플레이어의 참여 여부를 반환합니다.
     *
     * @param player 대상 플레이어
     * @return 대상 플레이어의 참여 여부
     */
    public boolean isParticipating(Player player) {
        return participantStrategy.isParticipating(player.getUniqueId());
    }

    /**
     * 대상 플레이어의 참여 여부를 반환합니다.
     *
     * @param uuid 대상 플레이어의 UniqueId
     * @return 대상 플레이어의 참여 여부
     */
    public boolean isParticipating(UUID uuid) {
        return participantStrategy.isParticipating(uuid);
    }

    public void addParticipant(Player player) {
        participantStrategy.addParticipant(player);
    }

    public boolean isRestricted() {
        return restricted;
    }

    public void setRestricted(boolean restricted) {
        this.restricted = restricted;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    protected void startGame() {
        this.gameStarted = true;
    }

    @Override
    protected void onEnd() {
        TimerBase.resetTasks();
        HandlerList.unregisterAll(this);
        for (Listener listener : registeredListeners) {
            HandlerList.unregisterAll(listener);
        }
    }

    public class Participant implements Listener {

        private Player player;

        Participant(Player player) {
            this.player = player;
            registerListener(this);
        }

        private Instant lastClick = Instant.now();

        @EventHandler
        private void onPlayerLogin(PlayerLoginEvent e) {
            if (e.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                this.player = e.getPlayer();
            }
        }

        @EventHandler
        private void onPlayerInteract(PlayerInteractEvent e) {
            Player p = e.getPlayer();
            if (p.equals(getPlayer())) {
                MaterialType materialType = MaterialType.valueOf(VersionUtil.getItemInHand(p).getType());
                ClickType clickType = e.getAction().equals(Action.RIGHT_CLICK_AIR)
                        || e.getAction().equals(Action.RIGHT_CLICK_BLOCK) ? ClickType.RIGHT_CLICK
                        : ClickType.LEFT_CLICK;
                if (materialType != null) {
                    if (hasAbility()) {
                        AbilityBase ability = getAbility();
                        if (!ability.isRestricted()) {
                            Instant currentInstant = Instant.now();
                            long duration = java.time.Duration.between(lastClick, currentInstant).toMillis();
                            if (duration >= 250) {
                                this.lastClick = currentInstant;
                                if (ability.ActiveSkill(materialType, clickType)) {
                                    ability.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&d능력을 사용하였습니다."));
                                }
                            }
                        }
                    }
                }
            }
        }

        @EventHandler
        private void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
            Player p = e.getPlayer();
            if (p.equals(getPlayer())) {
                MaterialType materialType = MaterialType.valueOf(VersionUtil.getItemInHand(p).getType());
                if (materialType != null && !e.isCancelled() && this.hasAbility()) {
                    AbilityBase ability = this.getAbility();
                    if (!ability.isRestricted()) {
                        Instant currentInstant = Instant.now();
                        long duration = java.time.Duration.between(lastClick, currentInstant).toMillis();
                        if (duration >= 250) {
                            Entity targetEntity = e.getRightClicked();
                            if (targetEntity instanceof LivingEntity) {
                                if (targetEntity instanceof Player) {
                                    Player targetPlayer = (Player) targetEntity;
                                    if (isParticipating(targetPlayer)) {
                                        this.lastClick = currentInstant;
                                        ability.TargetSkill(materialType, targetPlayer);
                                    }
                                } else {
                                    LivingEntity target = (LivingEntity) targetEntity;
                                    this.lastClick = currentInstant;
                                    ability.TargetSkill(materialType, target);
                                }
                            }
                        }
                    }
                }
            }
        }

        private AbilityBase ability;

        public void setAbility(Class<? extends AbilityBase> abilityClass)
                throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
                IllegalArgumentException, InvocationTargetException {
            if (hasAbility())
                removeAbility();

            Constructor<? extends AbilityBase> constructor = abilityClass.getConstructor(Participant.class);
            AbilityBase ability = constructor.newInstance(this);

            ability.setRestricted(isRestricted() || !isGameStarted());

            this.ability = ability;
        }

        /**
         * 플레이어에게 해당 능력을 부여합니다.
         *
         * @param ability 부여할 능력
         */
        public void setAbility(AbilityBase ability) {
            if (hasAbility()) {
                removeAbility();
            }

            ability.setRestricted(isRestricted() || !isGameStarted());

            this.ability = ability;
        }

        public boolean hasAbility() {
            return ability != null;
        }

        public AbilityBase getAbility() {
            return ability;
        }

        public void removeAbility() {
            if (getAbility() != null) {
                getAbility().destroy();
                ability = null;
            }
        }

        public Player getPlayer() {
            return player;
        }

    }

}
