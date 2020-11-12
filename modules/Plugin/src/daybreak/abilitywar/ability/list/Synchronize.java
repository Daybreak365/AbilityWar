package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.FastMath;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.geometry.Line;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

@AbilityManifest(name = "동기화", rank = Rank.S, species = Species.OTHERS, explain = {
		"철괴를 우클릭하면 $[DURATION_CONFIG]초간 높이 상관 없이 10칸 이내에 있는 모든 플레이어의",
		"체력을 평균값으로 서서히 맞춥니다. $[COOLDOWN_CONFIG]"
})
public class Synchronize extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Synchronize.class, "cooldown", 45,
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

	public static final SettingObject<Integer> DURATION_CONFIG = abilitySettings.new SettingObject<Integer>(Synchronize.class, "duration", 9,
			"# 지속 시간") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};

	private static final RGB RED = RGB.of(254, 69, 69), BLUE = RGB.of(69, 122, 254);

	public Synchronize(Participant participant) {
		super(participant);
	}

	private final Predicate<Entity> ONLY_PARTICIPANTS = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			return getGame().isParticipating(entity.getUniqueId())
					&& (!(getGame() instanceof DeathManager.Handler) || !((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
					&& getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue();
		}
	};

	private static final double[] RADIANS = {
			0,
			2.0943951023931954923084289221863,
			4.1887902047863909846168578443727
	};
	private final Cooldown cooldown = new Cooldown(COOLDOWN_CONFIG.getValue());
	private final Duration skill = new Duration(DURATION_CONFIG.getValue() * 20, cooldown) {
		@Override
		protected void onDurationProcess(int count) {
			for (int i = 0; i < 10; i++) {
				final int index = count * 10 + i;
				for (final double d : RADIANS) {
					final double radians = 0.00411233516712056609118103791661 * index + d;
					final double cos = FastMath.cos(radians), sin = FastMath.sin(radians);
					ParticleLib.REDSTONE.spawnParticle(getPlayer().getLocation().clone().add(cos * 10, FastMath.sin(0.01644934066848226436472415166644 * index) + 1.25, sin * 10), RED);
					ParticleLib.REDSTONE.spawnParticle(getPlayer().getLocation().clone().add(cos * 10, FastMath.cos(0.01644934066848226436472415166644 * index + 1.5707963267948966192313216916398) + 1.25, sin * 10), BLUE);
				}
			}
			if (count % 6 == 0) {
				final List<Player> nearby = LocationUtil.getEntitiesInCircle(Player.class, getPlayer().getLocation(), 10, ONLY_PARTICIPANTS);
				double average = 0.0;
				for (Player player : nearby) {
					average += player.getHealth();
				}
				average /= nearby.size();
				for (Player player : nearby) {
					final double diff = player.getHealth() - average;
					new Synchronization(player, diff <= 0, Math.min(1, Math.abs(diff))).start();
				}
			}
		}
	}.setPeriod(TimeUnit.TICKS, 1);

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !cooldown.isCooldown() && !skill.isDuration()) {
			skill.start();
			return true;
		}
		return false;
	}

	private class Synchronization extends AbilityTimer {

		private final Player target;
		private final boolean flag;

		private final SimpleTimer.Observer observer = new Observer() {
			@Override
			public void onResume() {
				Synchronization.this.resume();
			}

			@Override
			public void onPause() {
				Synchronization.this.pause();
			}

			@Override
			public void onSilentEnd() {
				Synchronization.this.stop(true);
			}
		};
		private final RGB color;
		private final double amount;

		/**
		 * @param target 대상
		 * @param flag true -> +, false -> -
		 */
		private Synchronization(final Player target, final boolean flag, final double amount) {
			super(TaskType.NORMAL, 6);
			setPeriod(TimeUnit.TICKS, 1);
			skill.attachObserver(observer);
			this.target = target;
			this.flag = flag;
			this.color = flag ? RED : BLUE;
			this.amount = amount;
		}

		@Override
		protected void run(int count) {
			if (getPlayer().equals(target)) return;
			for (int i = 0; i < 3; i++) {
				if (flag) {
					ParticleLib.REDSTONE.spawnParticle(getPlayer().getLocation().clone().add(Line.vectorAt(getPlayer().getLocation(), target.getLocation(), 18, (3 * count) - 2 + i)).add(0, 1, 0), color);
				} else {
					ParticleLib.REDSTONE.spawnParticle(target.getLocation().clone().add(Line.vectorAt(target.getLocation(), getPlayer().getLocation(), 18, (3 * count) - 2 + i)).add(0, 1, 0), color);
				}
			}
		}

		@Override
		protected void onEnd() {
			onSilentEnd();
			if (target.isDead()) return;
			if (flag) {
				target.setHealth(Math.min(target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), Math.max(1, target.getHealth() + amount)));
			} else {
				target.setHealth(Math.min(target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), Math.max(1, target.getHealth() - amount)));
			}
		}

		@Override
		protected void onSilentEnd() {
			skill.detachObserver(observer);
		}
	}

}
