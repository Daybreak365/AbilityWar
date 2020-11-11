package daybreak.abilitywar.game;

import com.google.common.base.Preconditions;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityBase.ClickType;
import daybreak.abilitywar.ability.AbilityFactory;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.ability.decorator.TargetHandler;
import daybreak.abilitywar.ability.event.AbilityActiveSkillEvent;
import daybreak.abilitywar.ability.event.AbilityPreActiveSkillEvent;
import daybreak.abilitywar.config.game.GameSettings;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.event.GameEndEvent;
import daybreak.abilitywar.game.event.GameReadyEvent;
import daybreak.abilitywar.game.event.GameStartEvent;
import daybreak.abilitywar.game.interfaces.IGame;
import daybreak.abilitywar.game.manager.GameFactory;
import daybreak.abilitywar.game.manager.GameFactory.GameRegistration;
import daybreak.abilitywar.game.manager.object.CommandHandler;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.module.EventManager;
import daybreak.abilitywar.game.module.Module;
import daybreak.abilitywar.game.module.ModuleBase;
import daybreak.abilitywar.utils.base.Hashes;
import daybreak.abilitywar.utils.base.collect.QueueOnIterateHashSet;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.io.FileUtil;
import daybreak.abilitywar.utils.base.logging.Logger;
import daybreak.abilitywar.utils.base.minecraft.boundary.BoundingBox;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;

public abstract class AbstractGame extends SimpleTimer implements IGame, Listener, CommandHandler {

	private static final Logger logger = Logger.getLogger(AbstractGame.class);

	public static final GameSettings gameSettings = new GameSettings(FileUtil.newFile("gamesettings.yml"));

	public interface Observer {
		void update(GameUpdate update);
	}

	public enum GameUpdate {START, END}

	private final Set<AbstractGame.Observer> observers = new HashSet<>();

	public final void attachObserver(AbstractGame.Observer observer) {
		observers.add(observer);
	}

	public final void detachObserver(AbstractGame.Observer observer) {
		observers.remove(observer);
	}

	private final Map<Class<? extends Module>, Module> modules = new HashMap<>();

	protected final <M extends Module> M addModule(final @NotNull M module) throws IllegalStateException {
		final Class<? extends Module> type = module.getClass();
		final ModuleBase base = type.getAnnotation(ModuleBase.class);
		if (base == null || base.value() == Module.class) throw new IllegalStateException("There is no valid @ModuleBase for " + type.getName() + " module.");
		if (modules.containsKey(base.value())) throw new IllegalStateException("Module with " + base.value().getName() + " base is already registered.");
		modules.put(base.value(), module);
		module.register();
		return module;
	}

	public final boolean hasModule(final Class<? extends Module> type) {
		return modules.containsKey(type);
	}

	public final <M extends Module> M getModule(final Class<M> type) {
		final Module module = modules.get(type);
		return module != null ? type.cast(module) : null;
	}

	private boolean restricted = true;
	private boolean gameStarted = false;

	private final GameRegistration<?> registration;
	protected final ParticipantStrategy participantStrategy;
	private final EventManager eventManager = addModule(new EventManager());

	public AbstractGame(Collection<Player> players) throws IllegalArgumentException {
		super(TaskType.INFINITE, -1);
		this.participantStrategy = newParticipantStrategy(players);
		this.registration = GameFactory.getRegistration(getClass());
		if (this.registration == null) {
			onEnd();
			throw new IllegalArgumentException("GameFactory에 등록되지 않은 게임입니다.");
		}
	}

	@Override
	public boolean start() {
		if (GameManager.currentGame == null && super.start()) {
			GameManager.currentGame = this;
			return true;
		}
		return false;
	}

	@Override
	public boolean stop(boolean silent) {
		if (silent) logger.debug("You cannot stop game silently.");
		if (GameManager.currentGame == this && super.stop(false)) {
			GameManager.currentGame = null;
			return true;
		}
		return false;
	}

	public boolean stop() {
		if (GameManager.currentGame == this && super.stop(false)) {
			GameManager.currentGame = null;
			return true;
		}
		return false;
	}

	@Override
	public boolean pause() {
		return false;
	}

	@Override
	public boolean resume() {
		return false;
	}

	protected abstract ParticipantStrategy newParticipantStrategy(Collection<Player> players);

	/**
	 * EventManager를 반환합니다.
	 * <p>
	 * null을 반환하지 않습니다.
	 */
	public EventManager getEventManager() {
		return eventManager;
	}

	public GameRegistration<?> getRegistration() {
		return registration;
	}

	/**
	 * 참여자 목록을 반환합니다.
	 *
	 * @return 참여자 목록
	 */
	public Collection<? extends Participant> getParticipants() {
		return participantStrategy.getParticipants();
	}

	/**
	 * {@link Player}를 기반으로 하는 {@link Participant}를 탐색합니다.
	 *
	 * @param player 탐색할 플레이어
	 * @return 존재할 경우 {@link Participant}를 반환합니다. 존재하지 않을 경우 null을 반환합니다.
	 * null을 반환할 수 있습니다.
	 */
	public Participant getParticipant(Player player) {
		return participantStrategy.getParticipant(player.getUniqueId());
	}

	/**
	 * 해당 {@link UUID}를 가지고 있는 {@link Player}를 기반으로 하는 {@link Participant}를 탐색합니다.
	 *
	 * @param uuid 탐색할 플레이어의 UUID
	 * @return 존재할 경우 {@link Participant}를 반환합니다. 존재하지 않을 경우 null을 반환합니다.
	 * null을 반환할 수 있습니다.
	 */
	public Participant getParticipant(UUID uuid) {
		return participantStrategy.getParticipant(uuid);
	}

	/**
	 * 대상 플레이어의 참여 여부를 반환합니다.
	 *
	 * @param player 대상 플레이어
	 * @return 대상 플레이어의 참여 여부
	 */
	public final boolean isParticipating(Player player) {
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

	public void addParticipant(Player player) throws UnsupportedOperationException {
		participantStrategy.addParticipant(player);
	}

	public void removeParticipant(UUID uuid) throws UnsupportedOperationException {
		participantStrategy.removeParticipant(uuid);
	}

	public boolean isRestricted() {
		return restricted;
	}

	public void setRestricted(final boolean restricted) {
		this.restricted = restricted;
		for (Participant participant : getParticipants()) {
			if (participant.hasAbility()) {
				participant.getAbility().setRestricted(restricted);
			}
		}
	}

	public boolean isGameStarted() {
		return gameStarted;
	}

	protected void startGame() {
		this.gameStarted = true;
		observers.forEach(observer -> observer.update(GameUpdate.START));
		if (!restricted) {
			for (Participant participant : getParticipants()) {
				if (participant.hasAbility()) {
					participant.getAbility().setRestricted(false);
				}
			}
		}
		Bukkit.getPluginManager().callEvent(new GameStartEvent(this));
	}

	@Override
	protected void onStart() {
		Bukkit.getPluginManager().callEvent(new GameReadyEvent(this));
	}

	@Override
	protected void onEnd() {
		stopTimers();
		for (Participant participant : getParticipants()) {
			participant.removeAbility();
		}
		HandlerList.unregisterAll(this);
		for (final Iterator<Module> iterator = modules.values().iterator(); iterator.hasNext();) {
			iterator.next().unregister();
			iterator.remove();
		}
		observers.forEach(observer -> observer.update(GameUpdate.END));
		Bukkit.broadcastMessage("§7게임이 중지되었습니다.");
		Bukkit.getPluginManager().callEvent(new GameEndEvent(this));
	}

	@Override
	protected final void onSilentEnd() {
		this.onEnd();
	}

	public abstract class Participant implements AbstractGame.Observer {

		private final ActionbarNotification actionbarNotification = new ActionbarNotification();
		private @NotNull Player player;
		private final Listener listener;

		protected Participant(@NotNull Player player) {
			this.player = player;
			this.listener = new Listener() {
				private long lastClick = System.currentTimeMillis();

				@EventHandler
				public void onPlayerLogin(PlayerLoginEvent e) {
					if (e.getPlayer().getUniqueId().equals(Participant.this.player.getUniqueId())) {
						Participant.this.player = e.getPlayer();
					}
				}

				@EventHandler
				public void onPlayerInteract(PlayerInteractEvent e) {
					if (e.useItemInHand() == Result.DENY || e.getAction() == Action.PHYSICAL) return;
					final Player player = e.getPlayer();
					if (player.equals(getPlayer()) && hasAbility()) {
						final AbilityBase ability = getAbility();
						if (ability instanceof ActiveHandler && !ability.isRestricted()) {
							final Material material = player.getInventory().getItemInMainHand().getType();
							if (NMS.hasCooldown(player, material)) return;
							if (ability.usesMaterial(material)) {
								final long current = System.currentTimeMillis();
								if (current - lastClick >= 250) {
									final ClickType clickType = e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK) ? ClickType.RIGHT_CLICK : ClickType.LEFT_CLICK;
									final AbilityPreActiveSkillEvent preEvent = new AbilityPreActiveSkillEvent(ability, material, clickType);
									Bukkit.getPluginManager().callEvent(preEvent);
									if (!preEvent.isCancelled()) {
										this.lastClick = current;
										if (((ActiveHandler) ability).ActiveSkill(material, clickType)) {
											Bukkit.getPluginManager().callEvent(new AbilityActiveSkillEvent(ability, material, clickType));
											ability.getPlayer().sendMessage("§d능력을 사용하였습니다.");
										}
									}
								}
							}
						}
					}
				}

				@EventHandler
				public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
					if (e.isCancelled()) return;
					final Player player = e.getPlayer();
					if (player.equals(getPlayer()) && !e.isCancelled() && hasAbility()) {
						final AbilityBase ability = getAbility();
						if ((ability instanceof ActiveHandler || ability instanceof TargetHandler) && !ability.isRestricted()) {
							final Material material = player.getInventory().getItemInMainHand().getType();
							if (NMS.hasCooldown(player, material)) return;
							if (ability.usesMaterial(material)) {
								long current = System.currentTimeMillis();
								if (current - lastClick >= 250) {
									if (ability instanceof ActiveHandler && ((ActiveHandler) ability).ActiveSkill(material, ClickType.RIGHT_CLICK)) {
										Bukkit.getPluginManager().callEvent(new AbilityActiveSkillEvent(ability, material, ClickType.RIGHT_CLICK));
										ability.getPlayer().sendMessage("§d능력을 사용하였습니다.");
									}
									if (ability instanceof TargetHandler) {
										final Entity targetEntity = e.getRightClicked();
										if (targetEntity instanceof LivingEntity) {
											if (targetEntity instanceof Player) {
												final Player targetPlayer = (Player) targetEntity;
												if (isParticipating(targetPlayer)) {
													if (AbstractGame.this instanceof DeathManager.Handler && ((DeathManager.Handler) AbstractGame.this).getDeathManager().isExcluded(targetPlayer))
														return;
													if (!getParticipant(targetPlayer).attributes().TARGETABLE.getValue())
														return;

													this.lastClick = current;
													((TargetHandler) ability).TargetSkill(material, targetPlayer);
												}
											} else {
												this.lastClick = current;
												((TargetHandler) ability).TargetSkill(material, (LivingEntity) targetEntity);
											}
										}
									}
								}
							}
						}
					}
				}
			};
			attachObserver(this);
			Bukkit.getPluginManager().registerEvents(listener, AbilityWar.getPlugin());
		}

		@Override
		public void update(GameUpdate update) {
			if (update.equals(GameUpdate.END)) {
				HandlerList.unregisterAll(listener);
			}
		}

		public abstract void setAbility(final AbilityRegistration registration) throws ReflectiveOperationException, UnsupportedOperationException;

		/**
		 * 플레이어에게 새 능력을 부여합니다.
		 * @param abilityClass 부여할 능력의 클래스
		 */
		public void setAbility(final Class<? extends AbilityBase> abilityClass) throws ReflectiveOperationException, UnsupportedOperationException {
			if (!AbilityFactory.isRegistered(abilityClass)) throw new IllegalArgumentException(abilityClass.getSimpleName() + " 능력은 AbilityFactory에 등록되지 않은 능력입니다.");
			this.setAbility(AbilityFactory.getRegistration(abilityClass));
		}

		public abstract boolean hasAbility();

		@Nullable
		public abstract AbilityBase getAbility();

		/**
		 * 참가자의 능력을 제거합니다.
		 * @return 제거된 능력
		 */
		@Nullable
		public abstract AbilityBase removeAbility();

		@NotNull
		public Player getPlayer() {
			return player;
		}

		public AbstractGame getGame() {
			return AbstractGame.this;
		}

		public abstract Attributes attributes();

		public class Attributes {
			public final Attribute<Boolean> TEAM_CHAT = new Attribute<>(false);
			public final Attribute<Boolean> TARGETABLE = new Attribute<>(true);
		}

		public class Attribute<T> {

			private final T defaultValue;
			private T value;

			public Attribute(T defaultValue) {
				this.defaultValue = defaultValue;
				this.value = defaultValue;
			}

			public T getDefaultValue() {
				return defaultValue;
			}

			public T getValue() {
				return value;
			}

			public T setValue(T value) {
				T origin = this.value;
				this.value = value;
				return origin;
			}

		}

		public class SetAttribute<E> {

			private final Set<E> set;

			@SafeVarargs
			public SetAttribute(E... defaultElements) {
				this.set = new HashSet<>();
				Collections.addAll(set, defaultElements);
			}

			public Set<E> getView() {
				return Collections.unmodifiableSet(set);
			}

			public void addValue(E element) {
				set.add(element);
			}

			public void removeValue(E element) {
				set.remove(element);
			}

		}

		public ActionbarNotification actionbar() {
			return actionbarNotification;
		}

		public class ActionbarNotification extends GameTimer {

			private final Set<ActionbarChannel> channels = Collections.synchronizedSet(new HashSet<>());
			private String lastString = "";

			private ActionbarNotification() {
				super(TaskType.INFINITE, -1);
				start();
			}

			private void update() {
				final StringJoiner joiner = new StringJoiner(ChatColor.WHITE + " | ");
				for (ActionbarChannel channel : channels) {
					if (channel.string != null) {
						joiner.add(channel.string);
					}
				}
				final String string = joiner.toString();
				if (!string.isEmpty() || !lastString.isEmpty()) {
					NMS.sendActionbar(getPlayer(), string, 0, 20, 20);
					this.lastString = string;
				}
			}

			@Override
			protected void run(int count) {
				boolean updated = false;
				for (ActionbarChannel channel : channels) {
					if (channel.seconds > 0) {
						if (--channel.seconds == 0) {
							channel.string = null;
							updated = true;
						}
					}
				}
				if (updated) {
					update();
				} else {
					if (!lastString.isEmpty()) {
						NMS.sendActionbar(getPlayer(), lastString, 0, 20, 20);
					}
				}
			}

			public ActionbarChannel newChannel() {
				return new ActionbarChannel();
			}

			public class ActionbarChannel {

				private String string;
				private int seconds = 0;

				public ActionbarChannel() {
					channels.add(this);
				}

				public ActionbarChannel update(String string) {
					this.string = string;
					this.seconds = 0;
					ActionbarNotification.this.update();
					return this;
				}

				public ActionbarChannel update(String string, int seconds) {
					this.string = string;
					this.seconds = seconds;
					ActionbarNotification.this.update();
					return this;
				}

				public void unregister() {
					channels.remove(this);
					ActionbarNotification.this.update();
				}

				public boolean isValid() {
					return channels.contains(this);
				}

			}

		}

	}

	private final Set<GameTimer> runningTimers = new QueueOnIterateHashSet<>();

	/**
	 * 현재 실행중인 모든 {@link GameTimer}를 반환합니다.
	 */
	public final Set<GameTimer> getRunningTimers() {
		return Collections.unmodifiableSet(runningTimers);
	}

	/**
	 * 해당 타입의 {@link GameTimer}를 모두 종료합니다.
	 *
	 * @param type 종료할 타이머 타입
	 */
	public final void stopTimers(final @NotNull Class<? extends GameTimer> type) {
		for (GameTimer gameTimer : runningTimers) {
			if (type.isAssignableFrom(gameTimer.getClass())) {
				gameTimer.stop(false);
			}
		}
	}

	/**
	 * 현재 실행중인 {@link GameTimer}를 모두 종료합니다.
	 */
	public final void stopTimers() {
		for (GameTimer gameTimer : runningTimers) {
			gameTimer.stop(true);
		}
	}

	public abstract class GameTimer extends daybreak.abilitywar.utils.base.concurrent.SimpleTimer {

		public GameTimer(final @NotNull TaskType taskType, final int maximumCount) {
			super(taskType, maximumCount);
			attachObserver(new SimpleTimer.Observer() {
				@Override
				public void onStart() {
					runningTimers.add(GameTimer.this);
				}
				@Override
				public void onEnd() {
					runningTimers.remove(GameTimer.this);
				}
				@Override
				public void onSilentEnd() {
					runningTimers.remove(GameTimer.this);
				}
				@Override
				public void onResume() {
					runningTimers.add(GameTimer.this);
				}
				@Override
				public void onPause() {
					runningTimers.remove(GameTimer.this);
				}
			});
		}

		@NotNull
		public GameTimer setInitialDelay(@NotNull TimeUnit timeUnit, int initialDelay) {
			super.setInitialDelay(timeUnit, initialDelay);
			return this;
		}

		@NotNull
		public GameTimer setPeriod(@NotNull TimeUnit timeUnit, int period) {
			super.setPeriod(timeUnit, period);
			return this;
		}

		@NotNull
		public AbstractGame getGame() {
			return AbstractGame.this;
		}

	}

	private final SetMultimap<Integer, CustomEntity> customEntities = MultimapBuilder.hashKeys().hashSetValues().build();

	public List<CustomEntity> getCustomEntities(Chunk chunk) {
		return new ArrayList<>(customEntities.get(Hashes.hashCode(chunk.getX(), chunk.getZ())));
	}

	public class CustomEntity {

		private final CustomEntityBoundingBox boundingBox = new CustomEntityBoundingBox(0, 0, 0, 0, 0, 0);
		private World world;
		private double x, y, z;
		private int lastChunkHash;

		public CustomEntity(World world, double x, double y, double z) {
			this.world = Preconditions.checkNotNull(world);
			this.x = x;
			this.y = y;
			this.z = z;
			this.lastChunkHash = Hashes.hashCode((int) x >> 4, (int) z >> 4);
			customEntities.put(lastChunkHash, this);
		}

		public World getWorld() {
			return world;
		}

		public void setWorld(World world) {
			this.world = Preconditions.checkNotNull(world);
		}

		public double x() {
			return x;
		}

		public double y() {
			return y;
		}

		public double z() {
			return z;
		}

		public void setX(double x) {
			this.x = x;
			updateLocation();
		}

		public void setY(double y) {
			this.y = y;
		}

		public void setZ(double z) {
			this.z = z;
			updateLocation();
		}

		public void setLocation(Location location) {
			this.world = Preconditions.checkNotNull(location.getWorld());
			this.x = location.getX();
			this.y = location.getY();
			this.z = location.getZ();
			updateLocation();
		}

		private void updateLocation() {
			final int chunkHash = Hashes.hashCode((int) x >> 4, (int) z >> 4);
			if (this.lastChunkHash != chunkHash) {
				customEntities.remove(lastChunkHash, this);
				this.lastChunkHash = chunkHash;
				customEntities.put(chunkHash, this);
			}
		}

		public Location getLocation() {
			return new Location(world, x, y, z);
		}

		public CustomEntity resizeBoundingBox(double x1, double y1, double z1, double X2, double y2, double z2) {
			this.boundingBox.resize(x1, y1, z1, X2, y2, z2);
			return this;
		}

		public CustomEntityBoundingBox getBoundingBox() {
			return boundingBox;
		}

		public void remove() {
			customEntities.remove(lastChunkHash, this);
		}

		public class CustomEntityBoundingBox extends BoundingBox {

			public CustomEntityBoundingBox(double x1, double y1, double z1, double x2, double y2, double z2) {
				super(x1, y1, z1, x2, y2, z2);
			}

			@Override
			public @NotNull CustomEntityBoundingBox copy() {
				return new CustomEntityBoundingBox(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
			}

			@Override
			public @NotNull Vector getCenter() {
				return CustomEntity.this.getLocation().toVector();
			}

			@Override
			public @NotNull CustomEntityBoundingBox resize(double x1, double y1, double z1, double x2, double y2, double z2) {
				super.resize(x1, y1, z1, x2, y2, z2);
				return this;
			}

			@Override
			public @NotNull CustomEntityBoundingBox expand(double negativeX, double negativeY, double negativeZ, double positiveX, double positiveY, double positiveZ) {
				super.expand(negativeX, negativeY, negativeZ, positiveX, positiveY, positiveZ);
				return this;
			}

			@Override
			public @NotNull CustomEntityBoundingBox expand(double x, double y, double z) {
				super.expand(x, y, z);
				return this;
			}

			@Override
			public @NotNull CustomEntityBoundingBox expand(double expansion) {
				super.expand(expansion);
				return this;
			}

			@Override
			public @NotNull CustomEntityBoundingBox expand(double dirX, double dirY, double dirZ, double expansion) {
				super.expand(dirX, dirY, dirZ, expansion);
				return this;
			}

			@Override
			public @NotNull CustomEntityBoundingBox expand(@NotNull Vector direction, double expansion) {
				super.expand(direction, expansion);
				return this;
			}

			@Override
			public @NotNull CustomEntityBoundingBox expand(@NotNull BlockFace blockFace, double expansion) {
				super.expand(blockFace, expansion);
				return this;
			}

			@Override
			public @NotNull CustomEntityBoundingBox expandDirectional(double dirX, double dirY, double dirZ) {
				super.expandDirectional(dirX, dirY, dirZ);
				return this;
			}

			@Override
			public @NotNull CustomEntityBoundingBox expandDirectional(@NotNull Vector direction) {
				super.expandDirectional(direction);
				return this;
			}

			@Override
			public @NotNull CustomEntityBoundingBox shift(double shiftX, double shiftY, double shiftZ) {
				super.shift(shiftX, shiftY, shiftZ);
				return this;
			}

			@Override
			public @NotNull CustomEntityBoundingBox shift(@NotNull Vector shift) {
				super.shift(shift);
				return this;
			}
		}

	}

	public abstract class Effect extends GameTimer {

		private final ActionbarChannel channel;
		private final String displayName;

		protected Effect(final Participant participant, final String displayName, final TaskType taskType, final int maximumCount) {
			super(taskType, maximumCount);
			this.channel = participant.actionbar().newChannel();
			this.displayName = displayName;
		}

		@Override
		public boolean start() {
			if (!channel.isValid()) return false;
			return super.start();
		}

		@Override
		protected void run(int count) {
			channel.update(displayName + "§7: §f" + (count / (20.0 / getPeriod())) + "초");
		}

		@Override
		protected void onEnd() {
			channel.unregister();
		}

		@Override
		protected void onSilentEnd() {
			channel.unregister();
		}

	}

}
