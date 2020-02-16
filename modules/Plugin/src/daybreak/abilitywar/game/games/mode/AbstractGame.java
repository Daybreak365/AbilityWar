package daybreak.abilitywar.game.games.mode;

import com.google.common.base.Preconditions;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityBase.ClickType;
import daybreak.abilitywar.ability.event.AbilityActiveSkillEvent;
import daybreak.abilitywar.game.events.participant.ParticipantAbilitySetEvent;
import daybreak.abilitywar.game.manager.object.CommandHandler;
import daybreak.abilitywar.game.manager.object.DeathManager;
import daybreak.abilitywar.game.manager.object.EffectManager;
import daybreak.abilitywar.game.manager.passivemanager.PassiveManager;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.base.Precondition;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.minecraft.compat.NMSHandler;
import daybreak.abilitywar.utils.base.minecraft.version.VersionUtil;
import daybreak.abilitywar.utils.math.geometry.Boundary.BoundingBox;
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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;

public abstract class AbstractGame extends SimpleTimer implements Listener, EffectManager.Handler, CommandHandler {

	public interface Observer {
		void update(GAME_UPDATE update);
	}

	public enum GAME_UPDATE {START, END}

	private final Set<Observer> observers = new HashSet<>();

	/**
	 * 게임이 종료될 때 등록 해제되어야 하는 {@link Listener}를 등록합니다.
	 */
	public final void attachObserver(Observer observer) {
		observers.add(observer);
	}

	private boolean restricted = true;
	private boolean gameStarted = false;

	private ParticipantStrategy participantStrategy;
	private final PassiveManager passiveManager = new PassiveManager(this);
	private final EffectManager effectManager = new EffectManager(this);

	public AbstractGame(Collection<Player> players) {
		super(TaskType.INFINITE, -1);
		this.participantStrategy = new ParticipantStrategy.DEFAULT_MANAGEMENT(this, players);
	}

	public void setParticipantStrategy(ParticipantStrategy participantStrategy) {
		this.participantStrategy = Precondition.checkNotNull(participantStrategy);
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
		for (Participant participant : getParticipants()) {
			if (participant.hasAbility()) {
				participant.ability.setRestricted(restricted);
			}
		}
	}

	public boolean isGameStarted() {
		return gameStarted;
	}

	protected void startGame() {
		this.gameStarted = true;
		observers.forEach(observer -> observer.update(GAME_UPDATE.START));
	}

	@Override
	protected void onEnd() {
		stopTimers();
		HandlerList.unregisterAll(this);
		observers.forEach(observer -> observer.update(GAME_UPDATE.END));
	}

	public class Participant implements Listener, Observer {

		private final Attributes attributes = new Attributes();
		private final ActionbarNotification actionbarNotification = new ActionbarNotification();
		private Player player;

		protected Participant(Player player) {
			this.player = player;
			attachObserver(this);
			Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		}

		@Override
		public void update(GAME_UPDATE update) {
			if (update.equals(GAME_UPDATE.END)) {
				HandlerList.unregisterAll(this);
			}
		}

		private long lastClick = System.currentTimeMillis();

		@EventHandler
		private void onPlayerLogin(PlayerLoginEvent e) {
			if (e.getPlayer().getUniqueId().equals(player.getUniqueId())) {
				this.player = e.getPlayer();
			}
		}

		@EventHandler
		private void onPlayerInteract(PlayerInteractEvent e) {
			Player player = e.getPlayer();
			if (player.equals(getPlayer()) && hasAbility()) {
				AbilityBase ability = getAbility();
				if (!ability.isRestricted()) {
					Material material = VersionUtil.getItemInHand(player).getType();
					ClickType clickType = e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK) ? ClickType.RIGHT_CLICK : ClickType.LEFT_CLICK;
					if (attributes.SKILL_MATERIALS.set.contains(material)) {
						long current = System.currentTimeMillis();
						if (current - lastClick >= 250) {
							this.lastClick = current;
							if (ability.ActiveSkill(material, clickType)) {
								Bukkit.getPluginManager().callEvent(new AbilityActiveSkillEvent(ability, material, clickType));
								ability.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&d능력을 사용하였습니다."));
							}
						}
					}
				}
			}
		}

		@EventHandler
		private void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
			Player p = e.getPlayer();
			if (p.equals(getPlayer()) && !e.isCancelled() && hasAbility()) {
				AbilityBase ability = this.getAbility();
				if (!ability.isRestricted()) {
					Material material = VersionUtil.getItemInHand(p).getType();
					if (attributes.SKILL_MATERIALS.set.contains(material)) {
						long current = System.currentTimeMillis();
						if (current - lastClick >= 250) {
							if (ability.ActiveSkill(material, ClickType.RIGHT_CLICK)) {
								Bukkit.getPluginManager().callEvent(new AbilityActiveSkillEvent(ability, material, ClickType.RIGHT_CLICK));
								ability.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&d능력을 사용하였습니다."));
							}
							Entity targetEntity = e.getRightClicked();
							if (targetEntity instanceof LivingEntity) {
								if (targetEntity instanceof Player) {
									Player targetPlayer = (Player) targetEntity;
									if (isParticipating(targetPlayer) && (!(this instanceof DeathManager.Handler) || !((DeathManager.Handler) this).getDeathManager().isDead(targetPlayer))) {
										this.lastClick = current;
										ability.TargetSkill(material, targetPlayer);
									}
								} else {
									LivingEntity target = (LivingEntity) targetEntity;
									this.lastClick = current;
									ability.TargetSkill(material, target);
								}
							}
						}
					}
				}
			}
		}

		private AbilityBase ability;


		/**
		 * 플레이어에게 새 능력을 부여합니다.
		 *
		 * @param abilityClass 부여할 능력의 클래스
		 */
		public void setAbility(Class<? extends AbilityBase> abilityClass) throws SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
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
		public void setAbility(AbilityBase ability) {
			AbilityBase oldAbility = null;
			if (hasAbility())
				oldAbility = removeAbility();

			ability.setRestricted(isRestricted() || !isGameStarted());

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

		public Attributes attributes() {
			return attributes;
		}

		public class Attributes {
			public final Attribute<Boolean> TEAM_CHAT = new Attribute<>(false);
			public final Attribute<Boolean> TARGETABLE = new Attribute<>(true);
			public final SetAttribute<Material> SKILL_MATERIALS = new SetAttribute<>(Material.IRON_INGOT, Material.GOLD_INGOT);
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

			private HashSet<E> set;

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

			private Set<ActionbarChannel> channels = Collections.synchronizedSet(new HashSet<>());
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
				NMSHandler.getNMS().sendActionbar(getPlayer(), lastString, 0, 20, 20);
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
					NMSHandler.getNMS().sendActionbar(getPlayer(), lastString, 0, 20, 20);
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

		public GameTimer setDelay(TimeUnit timeUnit, int delay) {
			super.setDelay(timeUnit, delay);
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

	private final Set<CustomEntity> customEntities = new LinkedHashSet<>();

	public List<CustomEntity> getCustomEntities(Chunk chunk) {
		List<CustomEntity> collect = new LinkedList<>();
		int x = chunk.getX(), z = chunk.getZ();
		for (CustomEntity customEntity : customEntities) {
			Chunk that = customEntity.world.getChunkAt((int) customEntity.x >> 4, (int) customEntity.z >> 4);
			if (that.getX() == x && that.getZ() == z) {
				collect.add(customEntity);
			}
		}
		return collect;
	}

	public class CustomEntity {

		private final CustomEntityBoundingBox boundingBox = new CustomEntityBoundingBox(0, 0, 0, 0, 0, 0);
		private World world;
		private double x, y, z;

		public CustomEntity(World world, double x, double y, double z) {
			this.world = Preconditions.checkNotNull(world);
			this.x = x;
			this.y = y;
			this.z = z;
			customEntities.add(this);
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
			return x;
		}

		public double z() {
			return x;
		}

		public void setX(double x) {
			this.x = x;
		}

		public void setY(double y) {
			this.y = y;
		}

		public void setZ(double z) {
			this.z = z;
		}

		public void setLocation(Location location) {
			this.world = Preconditions.checkNotNull(location.getWorld());
			this.x = location.getX();
			this.y = location.getY();
			this.z = location.getZ();
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
			customEntities.remove(this);
		}

		public class CustomEntityBoundingBox implements BoundingBox {

			private double minX, minY, minZ, maxX, maxY, maxZ;

			public CustomEntityBoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
				this.minX = minX;
				this.minY = minY;
				this.minZ = minZ;
				this.maxX = maxX;
				this.maxY = maxY;
				this.maxZ = maxZ;
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
			public Location getLocation() {
				return CustomEntity.this.getLocation();
			}

		}

	}

}
