package daybreak.abilitywar.game.games.defaultgame;

import static daybreak.abilitywar.utils.Validate.notNull;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import daybreak.abilitywar.config.AbilityWarSettings.Settings;
import daybreak.abilitywar.game.events.GameEndEvent;
import daybreak.abilitywar.game.events.GameReadyEvent;
import daybreak.abilitywar.game.events.GameStartEvent;
import daybreak.abilitywar.game.games.mode.AbstractGame;
import daybreak.abilitywar.game.games.mode.PlayerStrategy;
import daybreak.abilitywar.game.manager.AbilitySelect;
import daybreak.abilitywar.game.manager.DeathManager;
import daybreak.abilitywar.game.manager.Firewall;
import daybreak.abilitywar.game.manager.Invincibility;
import daybreak.abilitywar.game.manager.ScoreboardManager;
import daybreak.abilitywar.game.manager.WRECK;

public abstract class Game extends AbstractGame implements AbilitySelect.Handler, DeathManager.Handler, Invincibility.Handler, WRECK.Handler {

	public Game(PlayerStrategy strategy) {
		super(strategy);
	}

	private final DeathManager deathManager = notNull(setupDeathManager());
	private final Invincibility invincibility = new Invincibility(this);
	private final WRECK wreck = new WRECK(this);
	private final ScoreboardManager scoreboardManager = new ScoreboardManager(this);
	@SuppressWarnings("unused")
	private final Firewall fireWall = new Firewall(this, this);
	private AbilitySelect abilitySelect = null;

	@Override
	protected void onStart() {
		Bukkit.getPluginManager().callEvent(new GameReadyEvent(this));
		registerListener(this);
	}

	private int seconds = 0;

	@Override
	protected void TimerProcess(Integer count) {
		if(getAbilitySelect() == null || (getAbilitySelect() != null && getAbilitySelect().isEnded())) {
			seconds++;
			progressGame(seconds);
		}
	}

	@Override
	protected void onEnd() {
		this.scoreboardManager.Clear();
		this.onGameEnd();
		Bukkit.getPluginManager().callEvent(new GameEndEvent(this));
	}

	protected abstract void onGameEnd();
	
	/**
	 * 게임 진행
	 */
	protected abstract void progressGame(Integer Seconds);
	
	/**
	 * AbilitySelect 초깃값 설정
	 * @Nullable 능력 할당이 필요하지 않을 경우 null을 반환하세요.
	 */
	protected abstract AbilitySelect setupAbilitySelect();

	/**
	 * DeathManager 초깃값 설정
	 * @NotNull
	 */
	protected DeathManager setupDeathManager() {
		return new DeathManager(this);
	}

	protected ScoreboardManager getScoreboardManager() {
		return scoreboardManager;
	}

	/**
	 * DeathManager를 반환합니다.
	 * @NotNull
	 */
	@Override
	public DeathManager getDeathManager() {
		return deathManager;
	}

	/**
	 * WRECK을 반환합니다.
	 * @NotNull
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
	 * @Nullable 능력 할당 전이거나 능력 할당 기능을 사용하지 않을 경우 null을 반환합니다.
	 */
	@Override
	public AbilitySelect getAbilitySelect() {
		return abilitySelect;
	}
	
	/**
	 * Invincibility를 반환합니다.
	 * @NotNull
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
	private void onWeatherChange(WeatherChangeEvent e) {
		if(isGameStarted() && Settings.getClearWeather()) e.setCancelled(true);
	}
	
	@EventHandler
	private void onFoodLevelChange(FoodLevelChangeEvent e) {
		if(Settings.getNoHunger()) {
			e.setCancelled(true);
			
			Player p = (Player) e.getEntity();
			p.setFoodLevel(19);
		}
	}

}
