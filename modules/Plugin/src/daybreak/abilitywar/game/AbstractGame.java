package daybreak.abilitywar.game;

import com.google.common.base.Preconditions;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityBase.ClickType;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.ability.decorator.TargetHandler;
import daybreak.abilitywar.ability.event.AbilityActiveSkillEvent;
import daybreak.abilitywar.game.ParticipantStrategy.DefaultManagement;
import daybreak.abilitywar.game.event.participant.ParticipantAbilitySetEvent;
import daybreak.abilitywar.game.interfaces.iGame;
import daybreak.abilitywar.game.manager.object.CommandHandler;
import daybreak.abilitywar.game.manager.object.DeathManager;
import daybreak.abilitywar.game.manager.object.EventManager;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.base.Hashes;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.logging.Logger;
import daybreak.abilitywar.utils.base.math.geometry.Boundary.BoundingBox;
import daybreak.abilitywar.utils.base.minecraft.compat.nms.NMSHandler;
import daybreak.abilitywar.utils.base.reflect.ReflectionUtil;
import daybreak.abilitywar.utils.base.reflect.ReflectionUtil.FieldUtil;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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

public abstract class AbstractGame extends SimpleTimer implements iGame, Listener, CommandHandler {

	private static final Logger logger = Logger.getLogger(AbstractGame.class);

	public interface Observer {
		void update(GameUpdate update);
	}

	public enum GameUpdate {START, END}

	private final Set<Observer> observers = new HashSet<>();

	public final void attachObserver(Observer observer) {
		observers.add(observer);
	}

	private boolean restricted = true;
	private boolean gameStarted = false;

	protected final ParticipantStrategy participantStrategy;
	private final EventManager eventManager = new EventManager(this);

	public AbstractGame(Collection<Player> players) throws IllegalArgumentException {
		super(TaskType.INFINITE, -1);
		this.participantStrategy = newParticipantStrategy(players);
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

	protected ParticipantStrategy newParticipantStrategy(Collection<Player> players) {
		return new DefaultManagement(this, players);
	}

	/**
	 * EventManager를 반환합니다.
	 * <p>
	 * null을 반환하지 않습니다.
	 */
	public EventManager getEventManager() {
		return eventManager;
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

	public void setRestricted(boolean restricted) {
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
	}

	@Override
	protected void onEnd() {
		stopTimers();
		for (Participant participant : getParticipants()) {
			participant.removeAbility();
		}
		HandlerList.unregisterAll(this);
		observers.forEach(observer -> observer.update(GameUpdate.END));
		Bukkit.broadcastMessage(ChatColor.GRAY.toString().concat("게임이 중지되었습니다."));
	}

	public class Participant implements AbstractGame.Observer {

		private final Attributes attributes = new Attributes();
		private final ActionbarNotification actionbarNotification = new ActionbarNotification();
		private Player player;
		private final Listener listener;

		protected Participant(Player player) {
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
					Player player = e.getPlayer();
					if (player.equals(getPlayer()) && hasAbility()) {
						AbilityBase ability = getAbility();
						if (ability instanceof ActiveHandler && !ability.isRestricted()) {
							Material material = player.getInventory().getItemInMainHand().getType();
							ClickType clickType = e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK) ? ClickType.RIGHT_CLICK : ClickType.LEFT_CLICK;
							if (ability.getRegistration().getMaterials().contains(material)) {
								long current = System.currentTimeMillis();
								if (current - lastClick >= 250) {
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

				@EventHandler
				public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
					Player player = e.getPlayer();
					if (player.equals(getPlayer()) && !e.isCancelled() && hasAbility()) {
						AbilityBase ability = getAbility();
						if ((ability instanceof ActiveHandler || ability instanceof TargetHandler) && !ability.isRestricted()) {
							Material material = player.getInventory().getItemInMainHand().getType();
							if (ability.getRegistration().getMaterials().contains(material)) {
								long current = System.currentTimeMillis();
								if (current - lastClick >= 250) {
									if (ability instanceof ActiveHandler && ((ActiveHandler) ability).ActiveSkill(material, ClickType.RIGHT_CLICK)) {
										Bukkit.getPluginManager().callEvent(new AbilityActiveSkillEvent(ability, material, ClickType.RIGHT_CLICK));
										ability.getPlayer().sendMessage("§d능력을 사용하였습니다.");
									}
									if (ability instanceof TargetHandler) {
										Entity targetEntity = e.getRightClicked();
										if (targetEntity instanceof LivingEntity) {
											if (targetEntity instanceof Player) {
												Player targetPlayer = (Player) targetEntity;
												if (isParticipating(targetPlayer)) {
													if (AbstractGame.this instanceof DeathManager.Handler && ((DeathManager.Handler) AbstractGame.this).getDeathManager().isExcluded(targetPlayer))
														return;
													if (!getParticipant(targetPlayer).attributes.TARGETABLE.getValue())
														return;

													this.lastClick = current;
													((TargetHandler) ability).TargetSkill(material, targetPlayer);
												}
											} else {
												LivingEntity target = (LivingEntity) targetEntity;
												this.lastClick = current;
												((TargetHandler) ability).TargetSkill(material, target);
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

		protected AbilityBase ability;

		/**
		 * 플레이어에게 새 능력을 부여합니다.
		 *
		 * @param abilityClass 부여할 능력의 클래스
		 */
		public void setAbility(Class<? extends AbilityBase> abilityClass) throws IllegalAccessException, InstantiationException, InvocationTargetException {
			AbilityBase oldAbility = null;
			if (hasAbility()) {
				oldAbility = removeAbility();
			}

			AbilityBase ability = AbilityBase.create(abilityClass, this);
			ability.setRestricted(isRestricted() || !isGameStarted());
			this.ability = ability;
			Bukkit.getPluginManager().callEvent(new ParticipantAbilitySetEvent(this, oldAbility, ability));
		}

		/**
		 * 플레이어에게 해당 능력을 그대로 적용합니다.
		 *
		 * @param ability 부여할 능력
		 */
		@Beta
		public void setAbility(AbilityBase ability) throws NoSuchFieldException, IllegalAccessException {
			if (hasAbility() && this.ability.equals(ability)) return;
			AbilityBase oldAbility = removeAbility();
			if (ability != null) {
				ability.getParticipant().ability = null;
				ability.setRestricted(isRestricted() || !isGameStarted());

				Field participant = FieldUtil.removeFlag(AbilityBase.class.getDeclaredField("participant"), Modifier.FINAL);
				ReflectionUtil.setAccessible(participant).set(ability, Participant.this);
				FieldUtil.addFlag(participant, Modifier.FINAL);
			}

			this.ability = ability;
			Bukkit.getPluginManager().callEvent(new ParticipantAbilitySetEvent(this, oldAbility, ability));
		}

		public boolean hasAbility() {
			return ability != null;
		}

		public AbilityBase getAbility() {
			return ability;
		}

		/**
		 * 참가자의 능력을 제거합니다.
		 *
		 * @return 제거된 능력
		 */
		public AbilityBase removeAbility() {
			AbilityBase ability = getAbility();
			if (ability != null) {
				ability.destroy();
				this.ability = null;
			}
			return ability;
		}

		public Player getPlayer() {
			return player;
		}

		public AbstractGame getGame() {
			return AbstractGame.this;
		}

		public Attributes attributes() {
			return attributes;
		}

		public class Attributes {
			public final Attribute<Boolean> TEAM_CHAT = new Attribute<>(false);
			public final Attribute<Boolean> TARGETABLE = new Attribute<>(true);
		}

		public class Attribute<T> {

			private final T defaultValue;
			private T value;

			private Attribute(T defaultValue) {
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
			private SetAttribute(E... defaultElements) {
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
				StringJoiner joiner = new StringJoiner(ChatColor.WHITE + " | ");
				for (ActionbarChannel channel : channels) {
					if (channel.string != null) {
						joiner.add(channel.string);
					}
				}
				this.lastString = joiner.toString();
				if (!lastString.isEmpty()) {
					NMSHandler.getNMS().sendActionbar(getPlayer(), lastString, 0, 20, 20);
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
						NMSHandler.getNMS().sendActionbar(getPlayer(), lastString, 0, 20, 20);
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

	private final List<GameTimer> timerTasks = new LinkedList<>();

	/**
	 * 현재 실행중인 모든 {@link GameTimer}를 반환합니다.
	 */
	public final Collection<GameTimer> getTimers() {
		return new ArrayList<>(timerTasks);
	}

	/**
	 * 해당 타입의 {@link GameTimer}를 모두 종료합니다.
	 *
	 * @param type 종료할 타이머 타입
	 */
	public final void stopTimers(Class<? extends GameTimer> type) {
		for (GameTimer timer : getTimers()) {
			if (type.isAssignableFrom(timer.getClass())) {
				timer.stop(false);
			}
		}
	}

	/**
	 * 현재 실행중인 {@link GameTimer}를 모두 종료합니다.
	 */
	public void stopTimers() {
		for (GameTimer timer : getTimers()) {
			timer.stop(true);
		}
		timerTasks.clear();
	}

	public abstract class GameTimer extends SimpleTimer {

		private RestrictionBehavior behavior = RestrictionBehavior.STOP_START;

		public GameTimer(TaskType taskType, int maximumCount) {
			super(taskType, maximumCount);
			timerTasks.add(this);
		}

		public GameTimer setInitialDelay(TimeUnit timeUnit, int initialDelay) {
			super.setInitialDelay(timeUnit, initialDelay);
			return this;
		}

		public GameTimer setPeriod(TimeUnit timeUnit, int period) {
			super.setPeriod(timeUnit, period);
			return this;
		}

		public GameTimer setBehavior(RestrictionBehavior behavior) {
			this.behavior = Preconditions.checkNotNull(behavior);
			return this;
		}

		public RestrictionBehavior getBehavior() {
			return behavior;
		}

	}

	public enum RestrictionBehavior {STOP_START, PAUSE_RESUME}

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

		public CustomEntity setBoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
			this.boundingBox.minX = minX;
			this.boundingBox.minY = minY;
			this.boundingBox.minZ = minZ;
			this.boundingBox.maxX = maxX;
			this.boundingBox.maxY = maxY;
			this.boundingBox.maxZ = maxZ;
			return this;
		}

		public BoundingBox getBoundingBox() {
			return boundingBox;
		}

		public void remove() {
			customEntities.remove(lastChunkHash, this);
		}

		public class CustomEntityBoundingBox implements BoundingBox {

			private double minX, minY, minZ, maxX, maxY, maxZ;

			public CustomEntityBoundingBox(double x1, double y1, double z1, double x2, double y2, double z2) {
				this.minX = Math.min(x1, x2);
				this.minY = Math.min(y1, y2);
				this.minZ = Math.min(z1, z2);
				this.maxX = Math.max(x1, x2);
				this.maxY = Math.max(y1, y2);
				this.maxZ = Math.max(z1, z2);
			}

			@Override
			public double getMinX() {
				return x + minX;
			}

			@Override
			public double getMinY() {
				return y + minY;
			}

			@Override
			public double getMinZ() {
				return z + minZ;
			}

			@Override
			public double getMaxX() {
				return x + maxX;
			}

			@Override
			public double getMaxY() {
				return y + maxY;
			}

			@Override
			public double getMaxZ() {
				return z + maxZ;
			}

			@Override
			public BoundingBox expand(double negativeX, double negativeY, double negativeZ, double positiveX, double positiveY, double positiveZ) {
				if (negativeX == 0.0D && negativeY == 0.0D && negativeZ == 0.0D && positiveX == 0.0D && positiveY == 0.0D && positiveZ == 0.0D) {
					return this;
				}
				double newMinX = minX - negativeX, newMinY = minY - negativeY, newMinZ = minZ - negativeZ, newMaxX = maxX + positiveX, newMaxY = maxY + positiveY, newMaxZ = maxZ + positiveZ;

				if (newMinX > newMaxX) {
					double centerX = getLocation().getX();
					if (newMaxX >= centerX) {
						newMinX = newMaxX;
					} else if (newMinX <= centerX) {
						newMaxX = newMinX;
					} else {
						newMinX = centerX;
						newMaxX = centerX;
					}
				}
				if (newMinY > newMaxY) {
					double centerY = getLocation().getY();
					if (newMaxY >= centerY) {
						newMinY = newMaxY;
					} else if (newMinY <= centerY) {
						newMaxY = newMinY;
					} else {
						newMinY = centerY;
						newMaxY = centerY;
					}
				}
				if (newMinZ > newMaxZ) {
					double centerZ = getLocation().getZ();
					if (newMaxZ >= centerZ) {
						newMinZ = newMaxZ;
					} else if (newMinZ <= centerZ) {
						newMaxZ = newMinZ;
					} else {
						newMinZ = centerZ;
						newMaxZ = centerZ;
					}
				}

				this.minX = newMinX;
				this.minY = newMinY;
				this.minZ = newMinZ;
				this.maxX = newMaxX;
				this.maxY = newMaxY;
				this.maxZ = newMaxZ;
				return this;
			}

			@Override
			public Location getCenter() {
				return CustomEntity.this.getLocation();
			}

		}

	}

}
