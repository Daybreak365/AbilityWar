package daybreak.abilitywar.game.games.standard;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.config.AbilityWarSettings.Settings;
import daybreak.abilitywar.game.events.GameEndEvent;
import daybreak.abilitywar.game.events.GameReadyEvent;
import daybreak.abilitywar.game.events.GameStartEvent;
import daybreak.abilitywar.game.games.mode.AbstractGame;
import daybreak.abilitywar.game.manager.object.AbilitySelect;
import daybreak.abilitywar.game.manager.object.DeathManager;
import daybreak.abilitywar.game.manager.object.Firewall;
import daybreak.abilitywar.game.manager.object.Invincibility;
import daybreak.abilitywar.game.manager.object.ScoreboardManager;
import daybreak.abilitywar.game.manager.object.WRECK;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.thread.AbilityWarThread;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import static daybreak.abilitywar.utils.Validate.notNull;

public abstract class Game extends AbstractGame implements AbilitySelect.Handler, DeathManager.Handler, Invincibility.Handler, WRECK.Handler {

	public Game(Collection<Player> players) {
		super(players);
	}

	private final DeathManager deathManager = notNull(setupDeathManager());
	private final Invincibility invincibility = new Invincibility(this);
	private final WRECK wreck = new WRECK();
	private final ScoreboardManager scoreboardManager = new ScoreboardManager(this);
	@SuppressWarnings("unused")
	private final Firewall fireWall = new Firewall(this, this);
	private AbilitySelect abilitySelect = null;

	@Override
	protected void onStart() {
		Bukkit.getPluginManager().callEvent(new GameReadyEvent(this));
	}

	private int seconds = 0;

	@Override
	protected void onProcess(int seconds) {
		if (getAbilitySelect() == null || (getAbilitySelect() != null && getAbilitySelect().isEnded())) {
			this.seconds++;
			progressGame(this.seconds);
		}
	}

	@Override
	protected void onEnd() {
		super.onEnd();
		scoreboardManager.Clear();
		Bukkit.getPluginManager().callEvent(new GameEndEvent(this));
	}

	/**
	 * 게임 진행
	 */
	protected abstract void progressGame(int Seconds);

	/**
	 * AbilitySelect 초깃값 설정
	 * null을 반환할 수 있습니다. 능력 할당이 필요하지 않을 경우 null을 반환하세요.
	 */
	protected AbilitySelect setupAbilitySelect() {
		return new AbilitySelect(this, 1) {
			@Override
			protected Collection<Participant> initSelectors() {
				return getParticipants();
			}

			private ArrayList<Class<? extends AbilityBase>> abilities;

			@Override
			protected void drawAbility(Collection<Participant> selectors) {
				abilities = AbilitySelectStrategy.EVERY_ABILITY_EXCLUDING_BLACKLISTED.getAbilities();
				if (getSelectors().size() <= abilities.size()) {
					Random random = new Random();

					for (Participant participant : selectors) {
						Player p = participant.getPlayer();

						Class<? extends AbilityBase> abilityClass = abilities.get(random.nextInt(abilities.size()));
						try {
							participant.setAbility(abilityClass);
							abilities.remove(abilityClass);

							p.sendMessage(new String[]{
									ChatColor.translateAlternateColorCodes('&', "&a당신에게 능력이 할당되었습니다. &e/ability check&f로 확인 할 수 있습니다."),
									ChatColor.translateAlternateColorCodes('&', "&e/ability yes &f명령어를 사용하면 능력을 확정합니다."),
									ChatColor.translateAlternateColorCodes('&', "&e/ability no &f명령어를 사용하면 능력을 변경할 수 있습니다.")});
						} catch (IllegalAccessException | NoSuchMethodException | SecurityException |
								InstantiationException | IllegalArgumentException | InvocationTargetException e) {
							Messager.sendConsoleErrorMessage(
									ChatColor.translateAlternateColorCodes('&', "&e" + p.getName() + "&f님에게 능력을 할당하는 도중 오류가 발생하였습니다."),
									ChatColor.translateAlternateColorCodes('&', "&f문제가 발생한 능력: &b" + abilityClass.getName()));
						}
					}
				} else {
					Messager.broadcastErrorMessage("사용 가능한 능력의 수가 참가자의 수보다 적어 게임을 종료합니다.");
					AbilityWarThread.StopGame();
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&7게임이 초기화되었습니다."));
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
							Messager.sendConsoleErrorMessage(ChatColor.translateAlternateColorCodes('&', "&e" + p.getName() + "&f님의 능력을 변경하는 도중 오류가 발생하였습니다."));
							Messager.sendConsoleErrorMessage(ChatColor.translateAlternateColorCodes('&', "&f문제가 발생한 능력: &b" + abilityClass.getName()));
						}
					}
				} else {
					Messager.sendErrorMessage(p, "능력을 변경할 수 없습니다.");
				}

				return false;
			}

			@Override
			protected void onSelectEnd() {
			}
		};
	}

	/**
	 * DeathManager 초깃값 설정
	 * null을 반환하지 않습니다.
	 */
	protected DeathManager setupDeathManager() {
		return new DeathManager(this);
	}

	protected ScoreboardManager getScoreboardManager() {
		return scoreboardManager;
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
	 * AbilitySelect를 반환합니다.
	 * null을 반환할 수 있습니다. 능력 할당 전이거나 능력 할당 기능을 사용하지 않을 경우 null을 반환합니다.
	 */
	@Override
	public AbilitySelect getAbilitySelect() {
		return abilitySelect;
	}

	/**
	 * Invincibility를 반환합니다.
	 * null을 반환하지 않습니다.
	 */
	@Override
	public Invincibility getInvincibility() {
		return invincibility;
	}

	protected void startAbilitySelect() {
		this.abilitySelect = setupAbilitySelect();
	}

	@Override
	protected void startGame() {
		super.startGame();
		wreck.noticeIfEnabled();
		this.getScoreboardManager().Initialize();
		Bukkit.getPluginManager().callEvent(new GameStartEvent(this));
	}

	@EventHandler
	public final void onWeatherChange(WeatherChangeEvent e) {
		if (isGameStarted() && Settings.getClearWeather()) e.setCancelled(true);
	}

	@EventHandler
	public final void onFoodLevelChange(FoodLevelChangeEvent e) {
		if (Settings.getNoHunger()) {
			e.setCancelled(true);

			Player p = (Player) e.getEntity();
			p.setFoodLevel(19);
		}
	}

}
