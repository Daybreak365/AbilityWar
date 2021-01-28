package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

@AbilityManifest(name = "보이드", rank = Rank.A, species = Species.OTHERS, explain = {
		"§7패시브 §8- §5공허 차원문§f: 보이드는 순간 이동할 때 그 자리에 이동할 위치로",
		"통하는 공허 차원문을 열며, 모든 플레이어는 공허 차원문을 이용해 순간 이동할",
		"수 있습니다. 공허 차원문은 10초간 남으며, 12초에 한 번씩만 열 수 있습니다.",
		"§7철괴 우클릭 §8- §5순간 이동§f: 철괴를 우클릭하면 보이드가 공허를 통하여",
		"가장 가까이 있는 플레이어에게 순간 이동하고 $[INVINCIBILITY_DURATION_CONFIG]초간",
		"타게팅 불가능 무적 상태에 돌입합니다. $[COOLDOWN_CONFIG]"
})
public class Void extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Void.class, "cooldown", 100,
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

	public static final SettingObject<Integer> INVINCIBILITY_DURATION_CONFIG = abilitySettings.new SettingObject<Integer>(Void.class, "invincibility-duration", 50,
			"# 순간이동 후 무적 지속시간 (틱 단위)") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

		@Override
		public String toString() {
			return String.valueOf(getValue() / 20.0);
		}
	};

	private static final RGB PURPLE = RGB.of(113, 43, 204);

	public Void(Participant participant) {
		super(participant);
	}

	private final Cooldown cooldown = new Cooldown(COOLDOWN_CONFIG.getValue());
	private final int duration = INVINCIBILITY_DURATION_CONFIG.getValue();

	private final Predicate<Entity> predicate = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			if (entity.equals(getPlayer())) return false;
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

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT) {
			if (clickType == ClickType.RIGHT_CLICK) {
				if (!cooldown.isCooldown()) {
					final Player target = LocationUtil.getNearestEntity(Player.class, getPlayer().getLocation(), predicate);
					if (target != null) {
						getPlayer().sendMessage("§d" + target.getName() + "§f에게 §5순간 이동§f합니다.");
						getPlayer().teleport(target);
						ParticleLib.DRAGON_BREATH.spawnParticle(getPlayer().getLocation(), 1, 1, 1, 20);
						new Invincibility(duration).start();
						cooldown.start();
					} else {
						getPlayer().sendMessage("§5가장 가까운 플레이어§f가 존재하지 않습니다.");
					}
				}
			}
		}

		return false;
	}

	private long lastCreation = 0;

	@SubscribeEvent(onlyRelevant = true, ignoreCancelled = true)
	private void onPlayerTeleport(PlayerTeleportEvent e) {
		final long current = System.currentTimeMillis();
		if (current - lastCreation >= 12000) {
			this.lastCreation = current;
			new Portal(e.getFrom(), e.getTo()).start();
		}
	}

	private class Invincibility extends AbilityTimer implements Listener {

		private final ActionbarChannel actionbarChannel = newActionbarChannel();

		private Invincibility(final int ticks) {
			super(ticks);
			setPeriod(TimeUnit.TICKS, 1);
			Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		}

		@Override
		protected void onStart() {
			getParticipant().attributes().TARGETABLE.setValue(false);
		}

		@EventHandler
		private void onEntityDamage(EntityDamageEvent e) {
			if (getPlayer().equals(e.getEntity()) && isRunning()) {
				e.setCancelled(true);
				ParticleLib.DRAGON_BREATH.spawnParticle(getPlayer().getLocation(), 1, 1, 1, 20, 0);
			}
		}

		@EventHandler
		private void onEntityDamageByBlock(EntityDamageByBlockEvent e) {
			this.onEntityDamage(e);
		}

		@EventHandler
		private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
			this.onEntityDamage(e);
		}

		@Override
		protected void run(int count) {
			actionbarChannel.update("§d무적§f: " + (getCount() / 20.0) + "초");
		}

		@Override
		protected void onEnd() {
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			actionbarChannel.unregister();
			HandlerList.unregisterAll(this);
			getParticipant().attributes().TARGETABLE.setValue(true);
		}
	}

	private class Portal extends AbilityTimer implements Listener {

		private final ActionbarChannel actionbarChannel = newActionbarChannel();
		private final Location from, to;
		private final Map<Participant, ActionbarChannel> actionbarChannels = new HashMap<>();

		private Portal(final Location from, final Location to) {
			super(50);
			setPeriod(TimeUnit.TICKS, 4);
			this.from = from.clone().add(0, 1, 0);
			this.to = to.clone().add(0, 1, 0);
			SoundLib.ITEM_CHORUS_FRUIT_TELEPORT.playSound(getPlayer());
			Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		}

		@EventHandler(ignoreCancelled = true)
		private void onPlayerMove(PlayerMoveEvent e) {
			if (getGame().isParticipating(e.getPlayer())) {
				final Participant participant = getGame().getParticipant(e.getPlayer());
				if (!actionbarChannels.containsKey(participant)) {
					if (!isInRadius(e.getTo())) return;
					final ActionbarChannel channel = participant.actionbar().newChannel();
					channel.update("§f웅크려서 §5차원문§f을 이용하세요.");
					actionbarChannels.put(participant, channel);
				} else {
					if (isInRadius(e.getTo())) return;
					actionbarChannels.remove(participant).unregister();
				}
			}
		}

		@EventHandler(ignoreCancelled = true)
		private void onPlayerTeleport(PlayerTeleportEvent e) {
			onPlayerMove(e);
		}

		@EventHandler(ignoreCancelled = true)
		private void onPlayerToggleSneak(PlayerToggleSneakEvent e) {
			if (getGame().isParticipating(e.getPlayer()) && isInRadius(e.getPlayer().getLocation())) {
				e.getPlayer().teleport(to, TeleportCause.PLUGIN);
				SoundLib.ITEM_CHORUS_FRUIT_TELEPORT.playSound(e.getPlayer());
			}
		}

		private boolean isInRadius(final Location loc) {
			return from.getWorld().equals(loc.getWorld()) && from.distanceSquared(loc) <= 4;
		}

		@Override
		protected void run(int count) {
			actionbarChannel.update("§5공허 차원문§f: " + (getCount() / 5.0) + "초");
			ParticleLib.DRAGON_BREATH.spawnParticle(from, 0.2, 0.2, 0.2, 7, 0);
			for (int i = 0; i < 14; i++) {
				ParticleLib.REDSTONE.spawnParticle(to.clone().add(Math.random() * 0.6, Math.random() * 0.6, Math.random() * 0.6), PURPLE);
			}
		}

		@Override
		protected void onEnd() {
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			actionbarChannel.unregister();
			for (ActionbarChannel value : actionbarChannels.values()) {
				value.unregister();
			}
			HandlerList.unregisterAll(this);
		}
	}

}
