package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.Tips;
import daybreak.abilitywar.ability.Tips.Description;
import daybreak.abilitywar.ability.Tips.Difficulty;
import daybreak.abilitywar.ability.Tips.Level;
import daybreak.abilitywar.ability.Tips.Stats;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.effect.EvilSpirit;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.VectorUtil;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

@AbilityManifest(name = "유령", rank = Rank.A, species = Species.OTHERS, explain = {
		"§7철괴 우클릭 §8- §c유령화§f: 순간 벽을 통과할 수 있고 타게팅되지 않는 상태로 변하여",
		"바라보는 방향으로 이동합니다. §c쿨타임 §7: §f$(currentCooldown)초",
		"§7패시브 §8- §c혼§f: 유령화를 사용할 때마다 쿨타임이 $(cooldownIncrease)초씩 증가하며, 다른 플레이어를",
		"죽일 경우 쿨타임이 0초로 초기화됩니다.",
		"§7패시브 §8- §c악령§f: 나를 죽인 플레이어에게 약령 효과를 25초간 부여합니다.",
		"§7악령 효과§f: 간헐적으로 시야가 차단되고 환청이 들립니다. 이 효과를 가지고 있는",
		"플레이어를 타격한 대상에게도 이 효과가 부여됩니다."
}, summarize = {
		"§7철괴 우클릭§f 시 §3관전 상태§f로 돌진합니다.",
		"스킬의 §c쿨타임§f은 사용 시마다 증가하고, 적 처치 시 초기화됩니다.",
		"나를 죽인 대상은 §c악령§f 효과를 받습니다.",
		"§4[§c악령§4]§f 간헐적으로 환청 및 §8실명§f이 걸립니다. 소유자를 타격한 사람에게도 전염됩니다."
})
@Tips(tip = {
		"유령화로 벽을 넘고, 다른 플레이어의 스킬을 피하거나, 빠져 나오세요.",
		"유령을 막을 수 있는 장애물은 존재하지 않습니다."
}, strong = {
		@Description(subject = "장애물이 많은 환경", explain = {
				"유령의 능력은 장애물이 많은 환경에서 더욱 돋보입니다. 지형지물을 넘어",
				"상대가 예측하지 못하는 곳에서 나오세요!"
		})
}, stats = @Stats(offense = Level.ZERO, survival = Level.FIVE, crowdControl = Level.ZERO, mobility = Level.SEVEN, utility = Level.FOUR), difficulty = Difficulty.NORMAL)
public class Ghost extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_INCREASE_CONFIG = abilitySettings.new SettingObject<Integer>(Ghost.class, "cooldown-increase", 1,
			"# 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};

	private static final Vector ZERO_VECTOR = new Vector();

	public Ghost(Participant participant) throws IllegalStateException {
		super(participant);
	}

	private final int cooldownIncrease = COOLDOWN_INCREASE_CONFIG.getValue();
	private Location targetLocation;
	private final AbilityTimer skill = new AbilityTimer() {
		private GameMode originalMode;
		private float flySpeed;

		@Override
		protected void onStart() {
			this.originalMode = getPlayer().getGameMode();
			if (originalMode == GameMode.SPECTATOR) originalMode = GameMode.SURVIVAL;
			this.flySpeed = getPlayer().getFlySpeed();
			getParticipant().attributes().TARGETABLE.setValue(false);
			getPlayer().setGameMode(GameMode.SPECTATOR);
		}

		@Override
		protected void run(int count) {
			if (getPlayer().getSpectatorTarget() != null) getPlayer().setSpectatorTarget(null);
			getPlayer().setFlySpeed(0f);
			if (targetLocation != null && count <= 30) {
				final Location playerLocation = getPlayer().getLocation();
				getPlayer().setVelocity(VectorUtil.validateVector(targetLocation.toVector().subtract(playerLocation.toVector()).multiply(0.3)));
				if (playerLocation.distanceSquared(targetLocation) < 1) {
					stop(false);
					getPlayer().teleport(targetLocation.setDirection(getPlayer().getLocation().getDirection()));
				}
			} else {
				stop(true);
				if (targetLocation != null)
					getPlayer().teleport(targetLocation.setDirection(getPlayer().getLocation().getDirection()));
			}
		}

		@Override
		protected void onEnd() {
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			getPlayer().setGameMode(originalMode);
			getPlayer().setVelocity(ZERO_VECTOR);
			getPlayer().setFlySpeed(flySpeed);
			getPlayer().setFlying(false);
			getParticipant().attributes().TARGETABLE.setValue(true);
			NMS.setInvisible(getPlayer(), false);
		}
	}.setPeriod(TimeUnit.TICKS, 1).register();
	private final Cooldown cooldownTimer = new Cooldown(0, 15);
	private int currentCooldown = 0;

	@SubscribeEvent(onlyRelevant = true)
	private void onGameModeChange(PlayerGameModeChangeEvent e) {
		if (skill.isRunning() && getPlayer().getGameMode() == GameMode.SPECTATOR) e.setCancelled(true);
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onPlayerTeleport(PlayerTeleportEvent e) {
		if (skill.isRunning() && getPlayer().getGameMode() == GameMode.SPECTATOR) e.setCancelled(true);
	}

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !skill.isRunning()) {
			if (!cooldownTimer.isCooldown()) {
				Block lastEmpty = null;
				try {
					for (BlockIterator iterator = new BlockIterator(getPlayer().getWorld(), getPlayer().getLocation().toVector(), getPlayer().getLocation().getDirection(), 1, 7); iterator.hasNext(); ) {
						final Block block = iterator.next();
						if (!block.getType().isSolid()) {
							lastEmpty = block;
						}
					}
				} catch (IllegalStateException ignored) {
				}
				if (lastEmpty != null) {
					this.targetLocation = lastEmpty.getLocation();
					skill.start();
					cooldownTimer.setCooldown(currentCooldown += cooldownIncrease, 15);
					cooldownTimer.start();
					return true;
				} else {
					getPlayer().sendMessage(ChatColor.RED + "바라보는 방향에 이동할 수 있는 곳이 없습니다.");
				}
			}
		}
		return false;
	}

	private final Predicate<Player> predicate = new Predicate<Player>() {
		@Override
		public boolean test(Player entity) {
			return getGame().isParticipating(entity.getUniqueId())
					&& (!(getGame() instanceof DeathManager.Handler) || !((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()));
		}
	};

	@SubscribeEvent
	private void onPlayerDeath(final PlayerDeathEvent e) {
		final Player entity = e.getEntity();
		if (!getPlayer().equals(entity) && predicate.test(entity)) {
			if (!getPlayer().equals(entity.getKiller())) return;
			this.currentCooldown = 0;
			cooldownTimer.setCooldown(0, 0);
		} else if (getPlayer().equals(entity)) {
			final Player killer = getPlayer().getKiller();
			if (killer == null || getPlayer().equals(killer) || !predicate.test(getPlayer().getKiller())) return;
			EvilSpirit.apply(getGame().getParticipant(killer), TimeUnit.SECONDS, 30);
		}
	}

}
