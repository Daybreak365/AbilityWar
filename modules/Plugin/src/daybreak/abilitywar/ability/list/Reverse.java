package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.library.ParticleLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.jetbrains.annotations.NotNull;

@AbilityManifest(name = "리버스", rank = Rank.B, species = Species.HUMAN, explain = {
		"§7철괴 우클릭 §8(§7토글§8) §8- §5리버스§f: 자신을 중심으로 반지름 $[RADIUS_CONFIG]의 리버스 지대를",
		" 생성하거나, 재사용하여 제거할 수 있습니다. 리버스 지대 안의 플레이어들은",
		" 모든 §5넉백 §f또는 §5끌어당겨지는 §f효과를 반대로 받습니다."
})
public class Reverse extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> RADIUS_CONFIG = abilitySettings.new SettingObject<Integer>(Reverse.class, "radius", 5,
			"# 스킬 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	private static final RGB PURPLE = RGB.of(148, 61, 166);

	public Reverse(Participant participant) {
		super(participant);
	}

	private final int radius = RADIUS_CONFIG.getValue();
	private final Circle circleVectors = Circle.of(radius, radius * 17);
	private final ReverseZone zone = new ReverseZone();
	private final ActionbarChannel channel = newActionbarChannel();

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull AbilityBase.ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK) {
			channel.update("§f상태§7: " + (zone.toggleReverse() ? "§5리버스" : "§a일반"));
		}
		return false;
	}

	private class ReverseZone extends AbilityTimer implements Listener {

		private boolean isReverse;

		private ReverseZone() {
			super();
			setPeriod(TimeUnit.TICKS, 2);
		}

		@Override
		protected void onStart() {
			Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
			if (!isReverse) {
				pause();
			}
		}

		@Override
		protected void run(int count) {
			for (final Location location : circleVectors.toLocations(getPlayer().getLocation()).floor(getPlayer().getLocation().getY())) {
				ParticleLib.REDSTONE.spawnParticle(location, PURPLE);
			}
		}

		@EventHandler
		private void onPlayerVelocity(final PlayerVelocityEvent e) {
			if (isReverse() && LocationUtil.isInCircle(getPlayer().getLocation(), e.getPlayer().getLocation(), radius)) {
				e.setVelocity(e.getPlayer().getVelocity().multiply(-1));
			}
		}

		public boolean isReverse() {
			return isReverse;
		}

		public boolean toggleReverse() {
			this.isReverse = !isReverse;
			if (isReverse) resume();
			else pause();
			return isReverse;
		}

		@Override
		protected void onEnd() {
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			HandlerList.unregisterAll(this);
		}
	}

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			zone.start();
			channel.update("§f상태§7: " + (zone.isReverse() ? "§5리버스" : "§a일반"));
		}
	}
}
