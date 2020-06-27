package daybreak.abilitywar.game;

import com.google.common.base.Preconditions;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.game.event.GameEndEvent;
import daybreak.abilitywar.game.event.GameReadyEvent;
import daybreak.abilitywar.game.event.GameStartEvent;
import daybreak.abilitywar.game.manager.object.AbilitySelect;
import daybreak.abilitywar.game.manager.object.DeathManager;
import daybreak.abilitywar.game.manager.object.Firewall;
import daybreak.abilitywar.game.manager.object.Invincibility;
import daybreak.abilitywar.game.manager.object.ScoreboardManager;
import daybreak.abilitywar.game.manager.object.WRECK;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.logging.Logger;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import javax.naming.OperationNotSupportedException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public abstract class Game extends AbstractGame implements AbilitySelect.Handler, DeathManager.Handler, Invincibility.Handler, WRECK.Handler, ScoreboardManager.Handler, Firewall.Handler {

	private static final Logger logger = Logger.getLogger(Game.class);

	public Game(Collection<Player> players) throws IllegalArgumentException {
		super(players);
	}

	private final DeathManager deathManager = Preconditions.checkNotNull(newDeathManager());
	private final Invincibility invincibility = new Invincibility(this);
	private final WRECK wreck = newWRECK();
	private final ScoreboardManager scoreboardManager = new ScoreboardManager(this);
	private final Firewall fireWall = new Firewall(this, this);
	private final AbilitySelect abilitySelect = newAbilitySelect();
	private final Listener listener = new Listener() {
		@EventHandler
		public void onWeatherChange(WeatherChangeEvent e) {
			if (Settings.getClearWeather() && e.toWeatherState()) e.setCancelled(true);
		}

		@EventHandler
		public void onFoodLevelChange(FoodLevelChangeEvent e) {
			if (Settings.getNoHunger()) {
				e.setFoodLevel(19);
			}
		}
	};

	@Override
	protected void onStart() {
		Bukkit.getPluginManager().registerEvents(listener, AbilityWar.getPlugin());
		Bukkit.getPluginManager().callEvent(new GameReadyEvent(this));
	}

	private int seconds = 0;

	@Override
	protected void run(int seconds) {
		if (abilitySelect == null || !abilitySelect.isStarted() || abilitySelect.isEnded()) {
			this.seconds++;
			progressGame(this.seconds);
		}
	}

	@Override
	protected void onEnd() {
		super.onEnd();
		HandlerList.unregisterAll(listener);
		Bukkit.getPluginManager().callEvent(new GameEndEvent(this));
	}

	/**
	 * 게임 진행
	 */
	protected abstract void progressGame(int seconds);

	/**
	 * AbilitySelect 초깃값 설정
	 * null을 반환할 수 있습니다. 능력 할당이 필요하지 않을 경우 null을 반환하세요.
	 */
	@Override
	public AbilitySelect newAbilitySelect() {
		return new AbilitySelect(this, getParticipants(), 1) {

			private List<Class<? extends AbilityBase>> abilities;

			@Override
			protected void drawAbility(Collection<? extends Participant> selectors) {
				abilities = AbilitySelectStrategy.EVERY_ABILITY_EXCLUDING_BLACKLISTED.getAbilities();
				if (getSelectors().size() <= abilities.size()) {
					Random random = new Random();

					for (Participant participant : selectors) {
						Class<? extends AbilityBase> abilityClass = abilities.get(random.nextInt(abilities.size()));
						try {
							participant.setAbility(abilityClass);
							abilities.remove(abilityClass);

							participant.getPlayer().sendMessage(new String[]{
									"§a능력이 할당되었습니다. §e/aw check§f로 확인 할 수 있습니다.",
									"§e/aw yes §f명령어를 사용하여 능력을 확정합니다.",
									"§e/aw no §f명령어를 사용하여 능력을 변경합니다."
							});
						} catch (IllegalAccessException | SecurityException | InstantiationException | IllegalArgumentException | InvocationTargetException e) {
							logger.error(ChatColor.YELLOW + participant.getPlayer().getName() + ChatColor.WHITE + "님에게 능력을 할당하는 도중 오류가 발생하였습니다.");
							logger.error("문제가 발생한 능력: " + ChatColor.AQUA + abilityClass.getName());
						}
					}
				} else if (abilities.size() > 0) {
					Random random = new Random();

					for (Participant participant : selectors) {
						Class<? extends AbilityBase> abilityClass = abilities.get(random.nextInt(abilities.size()));
						try {
							participant.setAbility(abilityClass);
							participant.getPlayer().sendMessage(new String[]{
									"§a능력이 할당되었습니다. §e/aw check§f로 확인 할 수 있습니다.",
									"§e/aw yes §f명령어를 사용하여 능력을 확정합니다.",
									"§e/aw no §f명령어를 사용하여 능력을 변경합니다."
							});
						} catch (IllegalAccessException | SecurityException | InstantiationException | IllegalArgumentException | InvocationTargetException e) {
							logger.error(ChatColor.YELLOW + participant.getPlayer().getName() + ChatColor.WHITE + "님에게 능력을 할당하는 도중 오류가 발생하였습니다.");
							logger.error("문제가 발생한 능력: " + ChatColor.AQUA + abilityClass.getName());
						}
					}
				} else {
					Messager.broadcastErrorMessage("사용 가능한 능력이 없습니다.");
					GameManager.stopGame();
				}
			}

			@Override
			protected boolean changeAbility(Participant participant) {
				Player p = participant.getPlayer();

				if (abilities.size() > 0) {
					Random random = new Random();

					if (participant.hasAbility()) {
						Class<? extends AbilityBase> oldAbilityClass = participant.getAbility().getClass();
						Class<? extends AbilityBase> abilityClass = abilities.get(random.nextInt(abilities.size()));
						try {
							abilities.remove(abilityClass);
							abilities.add(oldAbilityClass);

							participant.setAbility(abilityClass);

							return true;
						} catch (Exception e) {
							logger.error(ChatColor.YELLOW + p.getName() + ChatColor.WHITE + "님의 능력을 변경하는 도중 오류가 발생하였습니다.");
							logger.error(ChatColor.WHITE + "문제가 발생한 능력: " + ChatColor.AQUA + abilityClass.getName());
						}
					}
				} else {
					Messager.sendErrorMessage(p, "능력을 변경할 수 없습니다.");
				}

				return false;
			}
		};
	}

	/**
	 * AbilitySelect를 반환합니다.
	 * null을 반환할 수 있습니다. 능력 할당 전이거나 능력 할당 기능을 사용하지 않을 경우 null을 반환합니다.
	 */
	@Override
	public AbilitySelect getAbilitySelect() {
		return abilitySelect;
	}

	@Override
	public void startAbilitySelect() throws OperationNotSupportedException {
		if (abilitySelect == null) {
			throw new OperationNotSupportedException("AbilitySelect is null");
		}
		abilitySelect.start();
	}

	@Override
	public Firewall getFirewall() {
		return fireWall;
	}

	@Override
	public ScoreboardManager getScoreboardManager() {
		return scoreboardManager;
	}

	/**
	 * DeathManager 초깃값 설정
	 * null을 반환하지 않습니다.
	 */
	@Override
	public DeathManager newDeathManager() {
		return new DeathManager(this);
	}

	/**
	 * DeathManager를 반환합니다.
	 * null을 반환하지 않습니다.
	 */
	@Override
	public DeathManager getDeathManager() {
		return deathManager;
	}

	/**
	 * WRECK을 반환합니다.
	 * null을 반환하지 않습니다.
	 */
	@Override
	public WRECK getWRECK() {
		return wreck;
	}

	@Override
	public boolean isWRECKEnabled() {
		return wreck.isEnabled();
	}

	/**
	 * Invincibility를 반환합니다.
	 * null을 반환하지 않습니다.
	 */
	@Override
	public Invincibility getInvincibility() {
		return invincibility;
	}

	@Override
	protected void startGame() {
		super.startGame();
		if (wreck.isEnabled()) {
			Bukkit.broadcastMessage("§cW§6R§eE§aC§bK §f모드가 활성화되었습니다!");
			Bukkit.broadcastMessage("§c모든 능력의 쿨타임이 §4" + Settings.getCooldownDecrease().getPercentage() + "% §c감소합니다.");
		}
		if (Settings.isDefaultMaxHealthEnabled()) {
			for (Participant participant : getParticipants()) {
				participant.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(Settings.getDefaultMaxHealth());
				participant.getPlayer().setHealth(participant.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
			}
		}
		Bukkit.getPluginManager().callEvent(new GameStartEvent(this));
	}

}
