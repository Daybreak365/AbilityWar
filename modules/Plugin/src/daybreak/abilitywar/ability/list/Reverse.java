package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.jetbrains.annotations.NotNull;

@AbilityManifest(name = "리버스", rank = Rank.B, species = Species.HUMAN, explain = {
		"철괴를 우클릭하면 $[DURATION_CONFIG]초간 주변에 반지름 $[RADIUS_CONFIG]의 리버스 지대를 생성합니다.",
		"리버스 지대 안의 플레이어들은 모든 넉백 또는 끌어당겨지는 효과를 반대로",
		"받습니다. $[COOLDOWN_CONFIG]"
})
public class Reverse extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> RADIUS_CONFIG = abilitySettings.new SettingObject<Integer>(Reverse.class, "radius", 5,
			"# 스킬 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	public static final SettingObject<Integer> DURATION_CONFIG = abilitySettings.new SettingObject<Integer>(Reverse.class, "duration", 8,
			"# 스킬 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Reverse.class, "cooldown", 30,
			"# 스킬 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}
	};

	private static final RGB PURPLE = RGB.of(148, 61, 166);

	public Reverse(Participant participant) {
		super(participant);
	}

	private final int radius = RADIUS_CONFIG.getValue(), duration = DURATION_CONFIG.getValue();
	private final Circle circleVectors = Circle.of(radius, radius * 17);
	private final Cooldown cooldown = new Cooldown(COOLDOWN_CONFIG.getValue());

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull AbilityBase.ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !cooldown.isCooldown()) {
			new ReverseZone(duration, getPlayer().getLocation()).start();
			cooldown.start();
		}
		return false;
	}

	private class ReverseZone extends AbilityTimer implements Listener {

		private ReverseZone(final int duration, final Location center) {
			super(TaskType.NORMAL, duration * 10);
			setPeriod(TimeUnit.TICKS, 2);
		}

		@Override
		protected void onStart() {
			Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		}

		@Override
		protected void run(int count) {
			for (final Location location : circleVectors.toLocations(getPlayer().getLocation()).floor(getPlayer().getLocation().getY())) {
				ParticleLib.REDSTONE.spawnParticle(location, PURPLE);
			}
		}

		@EventHandler
		private void onPlayerVelocity(final PlayerVelocityEvent e) {
			if (LocationUtil.isInCircle(getPlayer().getLocation(), e.getPlayer().getLocation(), radius)) {
				e.setVelocity(e.getPlayer().getVelocity().multiply(-1));
			}
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

}
