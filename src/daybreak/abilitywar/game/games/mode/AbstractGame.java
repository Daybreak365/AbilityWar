package daybreak.abilitywar.game.games.mode;

import static daybreak.abilitywar.utils.Validate.notNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import daybreak.abilitywar.utils.thread.OverallTimer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.EventExecutor;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityBase.ClickType;
import daybreak.abilitywar.ability.AbilityBase.MaterialType;
import daybreak.abilitywar.game.manager.EffectManager;
import daybreak.abilitywar.game.manager.passivemanager.PassiveManager;
import daybreak.abilitywar.utils.thread.TimerBase;
import daybreak.abilitywar.utils.versioncompat.VersionUtil;

public abstract class AbstractGame extends OverallTimer implements Listener, EffectManager.Handler {

	private final List<Listener> registeredListeners = new ArrayList<>();

	/**
	 * 게임이 종료될 때 등록 해제되어야 하는 {@link Listener}를 등록합니다.
	 */
	public final void registerListener(Listener listener) {
		Bukkit.getPluginManager().registerEvents(notNull(listener), AbilityWar.getPlugin());
		registeredListeners.add(listener);
	}

	protected final Map<String, Participant> participants;

	private boolean restricted = true;
	private boolean gameStarted = false;

	private final PassiveManager passiveManager = new PassiveManager(this);
	private final EffectManager effectManager = new EffectManager(this);

	public AbstractGame(PlayerStrategy strategy) {
		Map<String, Participant> participants = new HashMap<>();
		for (Player player : strategy.getPlayers()) {
			participants.put(player.getUniqueId().toString(), new Participant(this, player));
		}
		this.participants = Collections.unmodifiableMap(participants);
	}

	/**
	 * PassiveManager을 반환합니다.
	 * 
	 * null을 반환하지 않습니다.
	 */
	public PassiveManager getPassiveManager() {
		return passiveManager;
	}

	/**
	 * EffectManager를 반환합니다.
	 * 
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
		return participants.values();
	}

	/**
	 * {@link Player}를 기반으로 하는 {@link Participant}를 탐색합니다.
	 * 
	 * @param player 탐색할 플레이어
	 * @return 존재할 경우 {@link Participant}를 반환합니다. 존재하지 않을 경우 null을 반환합니다.
	 * null을 반환할 수 있습니다.
	 */
	public final Participant getParticipant(Player player) {
		String key = player.getUniqueId().toString();
		if (participants.containsKey(key))
			return participants.get(key);
		return null;
	}

	/**
	 * 해당 {@link UUID}를 가지고 있는 {@link Player}를 기반으로 하는 {@link Participant}를 탐색합니다.
	 * 
	 * @param uuid 탐색할 플레이어의 UUID
	 * @return 존재할 경우 {@link Participant}를 반환합니다. 존재하지 않을 경우 null을 반환합니다.
	 * null을 반환할 수 있습니다.
	 */
	public final Participant getParticipant(UUID uuid) {
		String key = uuid.toString();
		if (participants.containsKey(key))
			return participants.get(key);
		return null;
	}

	/**
	 * 대상 플레이어의 참여 여부를 반환합니다.
	 * 
	 * @param player 대상 플레이어
	 * @return 대상 플레이어의 참여 여부
	 */
	public boolean isParticipating(Player player) {
		return getParticipant(player) != null;
	}

	/**
	 * 대상 플레이어의 참여 여부를 반환합니다.
	 * 
	 * @param uuid 대상 플레이어의 UniqueId
	 * @return     대상 플레이어의 참여 여부
	 */
	public boolean isParticipating(UUID uuid) {
		return getParticipant(uuid) != null;
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
		TimerBase.ResetTasks();
		HandlerList.unregisterAll(this);
		for (Listener listener : registeredListeners){
			HandlerList.unregisterAll(listener);
		}
	}

	public class Participant implements EventExecutor {

		private Player player;

		private Participant(AbstractGame game, Player player) {
			this.player = player;

			Bukkit.getPluginManager().registerEvent(PlayerLoginEvent.class, game, EventPriority.HIGH, this,
					AbilityWar.getPlugin());
			Bukkit.getPluginManager().registerEvent(PlayerInteractEvent.class, game, EventPriority.HIGH, this,
					AbilityWar.getPlugin());
			Bukkit.getPluginManager().registerEvent(PlayerInteractAtEntityEvent.class, game, EventPriority.HIGH, this,
					AbilityWar.getPlugin());
		}

		private Instant lastClick = Instant.now();

		@Override
		public void execute(Listener listener, Event event) {
			if (event instanceof PlayerLoginEvent) {
				PlayerLoginEvent e = (PlayerLoginEvent) event;
				if (e.getPlayer().getUniqueId().equals(player.getUniqueId())) {
					this.player = e.getPlayer();
				}
			} else if (event instanceof PlayerInteractEvent) {
				PlayerInteractEvent e = (PlayerInteractEvent) event;

				Player p = e.getPlayer();
				if (p.equals(getPlayer())) {
					MaterialType mt = parseMaterialType(VersionUtil.getItemInHand(p).getType());
					ClickType ct = e.getAction().equals(Action.RIGHT_CLICK_AIR)
							|| e.getAction().equals(Action.RIGHT_CLICK_BLOCK) ? ClickType.RIGHT_CLICK
									: ClickType.LEFT_CLICK;
					if (mt != null) {
						if (hasAbility()) {
							AbilityBase Ability = this.getAbility();
							if (!Ability.isRestricted()) {
								Instant Now = Instant.now();
								long Duration = java.time.Duration.between(lastClick, Now).toMillis();
								if (Duration >= 250) {
									this.lastClick = Now;
									ActiveSkill(Ability, mt, ct);
								}
							}
						}
					}
				}
			} else if (event instanceof PlayerInteractAtEntityEvent) {
				PlayerInteractAtEntityEvent e = (PlayerInteractAtEntityEvent) event;

				Player p = e.getPlayer();
				if (p.equals(getPlayer())) {
					MaterialType mt = parseMaterialType(VersionUtil.getItemInHand(p).getType());
					if (mt != null && !e.isCancelled() && this.hasAbility()) {
						AbilityBase Ability = this.getAbility();
						if (!Ability.isRestricted()) {
							Instant Now = Instant.now();
							long Duration = java.time.Duration.between(lastClick, Now).toMillis();
							if (Duration >= 250) {
								Entity targetEntity = e.getRightClicked();
								if (targetEntity instanceof LivingEntity) {
									if (targetEntity instanceof Player) {
										Player targetPlayer = (Player) targetEntity;
										if (isParticipating(targetPlayer)) {
											this.lastClick = Now;
											Ability.TargetSkill(mt, targetPlayer);
										}
									} else {
										LivingEntity target = (LivingEntity) targetEntity;
										this.lastClick = Now;
										Ability.TargetSkill(mt, target);
									}
								}
							}
						}
					}
				}
			}
		}

		private void ActiveSkill(AbilityBase Ability, MaterialType mt, ClickType ct) {
			boolean Executed = Ability.ActiveSkill(mt, ct);

			if (Executed) {
				Ability.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&d능력을 사용하였습니다."));
			}
		}

		private MaterialType parseMaterialType(Material m) {
			for (MaterialType Type : MaterialType.values()) {
				if (Type.getMaterial().equals(m)) {
					return Type;
				}
			}

			return null;
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
