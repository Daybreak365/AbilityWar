package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.config.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.math.LocationUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

@AbilityManifest(Name = "카오스", Rank = Rank.S, Species = Species.GOD)
public class Chaos extends AbilityBase {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Chaos.class, "Cooldown", 80,
			"# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public static final SettingObject<Integer> DurationConfig = new SettingObject<Integer>(Chaos.class, "Duration", 5,
			"# 능력 지속 시간") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}

	};

	public static final SettingObject<Integer> DistanceConfig = new SettingObject<Integer>(Chaos.class, "Distance", 5,
			"# 거리 설정") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}

	};

	public Chaos(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f시작의 신 카오스."),
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 5초간 짙은 암흑 속으로 주변의 생명체들을"),
				ChatColor.translateAlternateColorCodes('&', "&f모두 끌어당깁니다. " + Messager.formatCooldown(CooldownConfig.getValue())));
	}

	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());

	private final int distance = DistanceConfig.getValue();

	private final DurationTimer Duration = new DurationTimer(DurationConfig.getValue() * 20, cooldownTimer) {

		private Location center;

		@Override
		public void onDurationStart() {
			center = getPlayer().getLocation();
		}

		@Override
		public void onDurationProcess(int seconds) {
			ParticleLib.SMOKE_LARGE.spawnParticle(center, 0, 0, 0, 100);
			for (Damageable d : LocationUtil.getNearbyEntities(Damageable.class, getPlayer(), distance, distance)) {
				d.damage(1);
				Vector vector = center.toVector().subtract(d.getLocation().toVector()).multiply(0.7);
				d.setVelocity(vector);
			}
		}

	}.setPeriod(1);

	@Override
	public boolean ActiveSkill(Material materialType, ClickType ct) {
		if (materialType.equals(Material.IRON_INGOT)) {
			if (ct.equals(ClickType.RIGHT_CLICK)) {
				if (!Duration.isDuration() && !cooldownTimer.isCooldown()) {
					Duration.startTimer();

					return true;
				}
			}
		}

		return false;
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
	}

}
