package daybreak.abilitywar.ability.list;

import com.google.common.base.Preconditions;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;

@AbilityManifest(name = "스왑", rank = Rank.B, species = Species.HUMAN, explain = {
		"철괴를 우클릭하면 $[DURATION_CONFIG]초간 주변 $[DISTANCE_CONFIG]칸 이내에 있는 모든 플레이어의 핫바 슬롯을",
		"임의로 변경하고 본인을 제외한 플레이어의 슬롯을 $[LOCK_DURATION_CONFIG]초간 고정합니다. $[COOLDOWN_CONFIG]",
})
public class Swap extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Swap.class, "cooldown", 35,
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

	public static final SettingObject<Integer> LOCK_DURATION_CONFIG = abilitySettings.new SettingObject<Integer>(Swap.class, "lock-duration", 3,
			"# 슬롯 고정 지속 시간") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	public static final SettingObject<Integer> DURATION_CONFIG = abilitySettings.new SettingObject<Integer>(Swap.class, "duration", 2,
			"# 지속 시간") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	public static final SettingObject<Integer> DISTANCE_CONFIG = abilitySettings.new SettingObject<Integer>(Swap.class, "distance", 8,
			"# 스킬 범위") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	private static final Random random = new Random();

	public Swap(Participant participant) {
		super(participant);
	}

	private final Predicate<Entity> predicate = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			if (entity instanceof Player) {
				if (!getGame().isParticipating(entity.getUniqueId())
						|| (getGame() instanceof DeathManager.Handler && ((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
						|| !getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue()) {
					return false;
				}
				if (getGame() instanceof Teamable) {
					final Teamable teamGame = (Teamable) getGame();
					final Participant entityParticipant = teamGame.getParticipant(entity.getUniqueId()), participant = getParticipant();
					return !teamGame.hasTeam(entityParticipant) || !teamGame.hasTeam(participant) || (!teamGame.getTeam(entityParticipant).equals(teamGame.getTeam(participant)));
				}
			}
			return true;
		}
	};

	private final int distance = DISTANCE_CONFIG.getValue();
	private final Map<UUID, Lock> locks = new HashMap<>();
	private final Cooldown cooldown = new Cooldown(COOLDOWN_CONFIG.getValue());
	private final Duration duration = new Duration(DURATION_CONFIG.getValue() * 10, cooldown) {
		@Override
		protected void onDurationProcess(int count) {
			for (final Player player : LocationUtil.getNearbyEntities(Player.class, getPlayer().getLocation(), distance, distance, predicate)) {
				player.getInventory().setHeldItemSlot(random.nextInt(9));
				if (count % 3 == 0) {
					SoundLib.UI_BUTTON_CLICK.playSound(player);
				}
			}
		}

		@Override
		protected void onDurationEnd() {
			for (final Player player : LocationUtil.getNearbyEntities(Player.class, getPlayer().getLocation(), distance, distance, predicate.and(new Predicate<Entity>() {
				@Override
				public boolean test(Entity entity) {
					return !getPlayer().equals(entity);
				}
			}))) {
				final int slot = random.nextInt(9);
				player.getInventory().setHeldItemSlot(slot);
				if (!locks.containsKey(player.getUniqueId())) {
					new Lock(player, slot).start();
				}
				SoundLib.UI_BUTTON_CLICK.playSound(player);
			}
		}
	}.setPeriod(TimeUnit.TICKS, 2);

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !duration.isDuration() && !cooldown.isCooldown()) {
			duration.start();
		}
		return false;
	}

	private class Lock extends AbilityTimer implements Listener {

		private final ActionbarChannel channel;

		private final Player player;
		private final int slot;

		private Lock(final Player player, final int slot) {
			super(TaskType.REVERSE, LOCK_DURATION_CONFIG.getValue() * 10);
			Preconditions.checkArgument(slot < 9 && slot >= 0, "slot '" + slot + "' out of range(0-8)");
			this.player = player;
			this.channel = getGame().getParticipant(player).actionbar().newChannel();
			this.slot = slot;
			final Lock lock = locks.get(player.getUniqueId());
			if (lock != null) {
				lock.stop(false);
			}
			locks.put(player.getUniqueId(), this);
			setPeriod(TimeUnit.TICKS, 2);
		}

		@Override
		protected void onStart() {
			Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		}

		@Override
		protected void run(int count) {
			channel.update("§c고정§f: " + (getCount() / 10.0) + "초");
		}

		@EventHandler
		private void onPlayerItemHeld(final PlayerItemHeldEvent e) {
			if (player.getUniqueId().equals(e.getPlayer().getUniqueId())) {
				if (e.getNewSlot() != slot) {
					e.setCancelled(true);
				}
			}
		}

		@Override
		protected void onEnd() {
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			HandlerList.unregisterAll(this);
			locks.remove(player.getUniqueId());
			channel.unregister();
		}
	}
}
