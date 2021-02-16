package daybreak.abilitywar.game;

import com.google.common.base.Preconditions;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration.Tip;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.game.ParticipantStrategy.DefaultManagement;
import daybreak.abilitywar.game.event.participant.ParticipantAbilitySetEvent;
import daybreak.abilitywar.game.manager.object.AbilitySelect;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.module.Firewall;
import daybreak.abilitywar.game.module.Invincibility;
import daybreak.abilitywar.game.module.ScoreboardManager;
import daybreak.abilitywar.game.module.Wreck;
import daybreak.abilitywar.game.module.ZeroTick;
import daybreak.abilitywar.music.MusicRadio;
import daybreak.abilitywar.music.christmas.ChristmasMusic;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.Seasons;
import daybreak.abilitywar.utils.base.logging.Logger;
import daybreak.abilitywar.utils.base.minecraft.WorldReset;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.naming.OperationNotSupportedException;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public abstract class Game extends AbstractGame implements AbilitySelect.Handler, DeathManager.Handler, Invincibility.Handler, Wreck.Handler, ScoreboardManager.Handler, Firewall.Handler {

	private static final Logger logger = Logger.getLogger(Game.class);

	public Game(Collection<Player> players) throws IllegalArgumentException {
		super(players);
	}

	private final DeathManager deathManager = addModule(Preconditions.checkNotNull(newDeathManager()));
	private final Invincibility invincibility = addModule(new Invincibility(this));
	private final Wreck wreck = addModule(newWreck());
	private final ScoreboardManager scoreboardManager = addModule(new ScoreboardManager(this));
	private final Firewall firewall = addModule(new Firewall(this, this));
	private final AbilitySelect abilitySelect = newAbilitySelect();
	private final Listener listener = new Listener() {
		@EventHandler
		private void onWeatherChange(final WeatherChangeEvent e) {
			if (Settings.getClearWeather() && e.toWeatherState()) e.setCancelled(true);
		}

		@EventHandler
		private void onFoodLevelChange(final FoodLevelChangeEvent e) {
			if (Settings.getNoHunger()) {
				e.setFoodLevel(19);
			}
		}

		@SuppressWarnings("deprecation")
		@EventHandler(ignoreCancelled = true)
		private void onEntityDamageByEntity(final EntityDamageByEntityEvent e) {
			if (e.getDamager() instanceof Arrow) {
				final Arrow arrow = (Arrow) e.getDamager();
				if (arrow.getShooter() instanceof Entity) {
					final Entity shooter = (Entity) arrow.getShooter();
					final double distanceSquared = e.getEntity().getLocation().distanceSquared(shooter.getLocation());
					if (Settings.isBowCooldownEnabled() && distanceSquared <= 100 && shooter instanceof Player) {
						final Player entity = (Player) shooter;
						NMS.setCooldown(entity, Material.BOW, (int) (15 * (1 - (Math.min(100, distanceSquared) / 100))));
						new BukkitRunnable() {
							@Override
							public void run() {
								NMS.clearActiveItem(entity);
							}
						}.runTaskLater(AbilityWar.getPlugin(), 1L);
					}
					if (Settings.isArrowDamageDistanceProportional() && distanceSquared <= 100) {
						e.setDamage(e.getDamage() * (Math.max(0.6, Math.min(100, distanceSquared) / 100)));
					}
				}
			}
			if (Settings.isShieldCooldownEnabled() && e.getEntity() instanceof Player) {
				final Player entity = (Player) e.getEntity();
				if (e.getDamage(DamageModifier.BLOCKING) < 0 && !NMS.hasCooldown(entity, Material.SHIELD)) {
					NMS.setCooldown(entity, Material.SHIELD, e.getDamager() instanceof Projectile ? 80 : 120);
					new BukkitRunnable() {
						@Override
						public void run() {
							NMS.clearActiveItem(entity);
						}
					}.runTaskLater(AbilityWar.getPlugin(), 1L);
				}
			}
		}

		@EventHandler(ignoreCancelled = true)
		private void onShootBow(final EntityShootBowEvent e) {
			if (e.getEntity() instanceof Player && NMS.hasCooldown((Player) e.getEntity(), Material.BOW)) {
				e.setCancelled(true);
			}
		}
	};

	@Override
	protected void onStart() {
		super.onStart();
		Bukkit.getPluginManager().registerEvents(listener, AbilityWar.getPlugin());
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
		HandlerList.unregisterAll(listener);
		if (Settings.isAutoWorldResetEnabled()) {
			WorldReset.resetWorlds();
		}
		super.onEnd();
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
		return new AbilitySelect(this, getParticipants(), Settings.getAbilityChangeCount()) {

			private List<Class<? extends AbilityBase>> abilities;

			@Override
			protected void drawAbility(Collection<? extends Participant> selectors) {
				abilities = AbilityCollector.EVERY_ABILITY_EXCLUDING_BLACKLISTED.collect(Game.this.getClass());
				if (getSelectors().size() <= abilities.size()) {
					Random random = new Random();

					for (Participant participant : selectors) {
						Class<? extends AbilityBase> abilityClass = abilities.get(random.nextInt(abilities.size()));
						try {
							participant.setAbility(abilityClass);
							final AbilityBase ability = participant.getAbility();
							abilities.remove(abilityClass);

							final Player player = participant.getPlayer();
							player.sendMessage("§a능력이 할당되었습니다. §e/aw check§f로 확인하세요.");
							if (!hasDecided(participant)) {
								player.sendMessage("§e/aw yes §f명령어로 능력을 확정하거나, §e/aw no §f명령어로 능력을 변경하세요.");
							}
							final Tip tip = ability.getRegistration().getTip();
							if (tip != null) {
								player.sendMessage("§e/aw abtip§f으로 능력 팁을 확인하세요.");
							}
						} catch (ReflectiveOperationException | SecurityException | IllegalArgumentException e) {
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
							final AbilityBase ability = participant.getAbility();
							final Player player = participant.getPlayer();
							player.sendMessage("§a능력이 할당되었습니다. §e/aw check§f로 확인하세요.");
							if (!hasDecided(participant)) {
								player.sendMessage("§e/aw yes §f명령어로 능력을 확정하거나, §e/aw no §f명령어로 능력을 변경하세요.");
							}
							final Tip tip = ability.getRegistration().getTip();
							if (tip != null) {
								player.sendMessage("§e/aw abtip§f으로 능력 팁을 확인하세요.");
							}
						} catch (SecurityException | ReflectiveOperationException | IllegalArgumentException e) {
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

	@Override
	protected ParticipantStrategy newParticipantStrategy(Collection<Player> players) {
		return new DefaultManagement(this, players);
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
		return firewall;
	}

	@Override
	public ScoreboardManager getScoreboardManager() {
		return scoreboardManager;
	}

	protected @NotNull Wreck newWreck() {
		return new Wreck();
	}

	protected @NotNull DeathManager newDeathManager() {
		return new DeathManager(this);
	}

	/**
	 * DeathManager를 반환합니다.
	 * null을 반환하지 않습니다.
	 */
	@Override
	public @NotNull DeathManager getDeathManager() {
		return deathManager;
	}

	/**
	 * WRECK을 반환합니다.
	 * null을 반환하지 않습니다.
	 */
	@Override
	public @NotNull Wreck getWreck() {
		return wreck;
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
		if (wreck.isEnabled()) {
			Bukkit.broadcastMessage("§cW§6R§eE§aC§bK §f모드가 활성화되었습니다!");
			Bukkit.broadcastMessage("§c모든 능력의 쿨타임이 §4" + Settings.getCooldownDecrease().getPercentage() + "% §c감소합니다.");
		}
		if (Settings.isZeroTickEnabled()) {
			Bukkit.broadcastMessage("§f제로틱 §a모드가 활성화되었습니다! §2(§f공격 딜레이 §a없이 타격할 수 있습니다.§2)");
			addModule(new ZeroTick(this));
		}
		if (Settings.isDefaultMaxHealthEnabled()) {
			for (Participant participant : getParticipants()) {
				participant.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(Settings.getDefaultMaxHealth());
				participant.getPlayer().setHealth(participant.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
			}
		}
		if (Seasons.isChristmas()) {
			new MusicRadio(ChristmasMusic.getRandom()) {
				{
					Game.this.attachObserver(new Observer() {
						@Override
						public void onEnd() {
							stop(false);
						}
					});
				}
			}.start();
		}
		super.startGame();
	}

	protected class ParticipantImpl extends Participant {

		private AbilityBase ability = null;
		private final Attributes attributes = new Attributes();

		protected ParticipantImpl(@NotNull Player player) {
			super(player);
		}

		@Override
		public void setAbility(AbilityRegistration registration) throws ReflectiveOperationException {
			final AbilityBase oldAbility = removeAbility();
			final AbilityBase ability = AbilityBase.create(registration, this);
			ability.setRestricted(false);
			this.ability = ability;
			Bukkit.getPluginManager().callEvent(new ParticipantAbilitySetEvent(this, oldAbility, ability));
		}

		@Override
		public boolean hasAbility() {
			return ability != null;
		}

		@Override
		@Nullable
		public AbilityBase getAbility() {
			return ability;
		}

		@Override
		@Nullable
		public AbilityBase removeAbility() {
			final AbilityBase ability = this.ability;
			if (ability != null) {
				ability.destroy();
				this.ability = null;
			}
			return ability;
		}

		@Override
		public Attributes attributes() {
			return attributes;
		}
	}

}
