package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.config.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.library.EffectLib;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.math.LocationUtil;
import daybreak.abilitywar.utils.math.geometry.Circle;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

@AbilityManifest(Name = "교황", Rank = Rank.A, Species = Species.HUMAN)
public class TheHighPriestess extends AbilityBase {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(TheHighPriestess.class, "Cooldown", 80,
			"# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public static final SettingObject<Integer> DurationConfig = new SettingObject<Integer>(TheHighPriestess.class, "Duration", 6,
			"# 스킬 지속시간") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 1 && value <= 50;
		}

	};

	public static final SettingObject<Double> RangeConfig = new SettingObject<Double>(TheHighPriestess.class, "Range", 8.0,
			"# 스킬 사용 시 자신의 영지로 선포할 범위") {

		@Override
		public boolean Condition(Double value) {
			return value >= 1.0 && value <= 50.0;
		}

	};

	public TheHighPriestess(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 " + DurationConfig.getValue() + "초간 주변 " + RangeConfig.getValue() + "칸을 자신의 영지로 선포합니다. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&f영지 안에서 자신은 재생 효과를, 상대방은 시듦 효과를 받습니다."));
	}

	private final int duration = DurationConfig.getValue();
	private final double range = RangeConfig.getValue();

	private final CooldownTimer Cool = new CooldownTimer(CooldownConfig.getValue());

	private final DurationTimer Skill = new DurationTimer(duration * 20, Cool) {

		private Location center;

		@Override
		protected void onDurationStart() {
			center = getPlayer().getLocation();

			for (Player p : LocationUtil.getNearbyPlayers(center, range, range)) {
				if (LocationUtil.isInCircle(center, p.getLocation(), range, true)) {
					SoundLib.ENTITY_EVOKER_CAST_SPELL.playSound(p);
				}
			}
		}

		@Override
		public void onDurationProcess(int seconds) {
			for (Location l : new Circle(center, range).setAmount((int) range * 8).setHighestLocation(true).getLocations()) {
				ParticleLib.SPELL_INSTANT.spawnParticle(l, 0, 0, 0, 1);
			}
			for (Player p : LocationUtil.getNearbyPlayers(getPlayer(), range, range)) {
				EffectLib.WITHER.addPotionEffect(p, 60, 1, true);
			}
			if (LocationUtil.isInCircle(center, getPlayer().getLocation(), range, true)) {
				EffectLib.REGENERATION.addPotionEffect(getPlayer(), 100, 2, false);
			}
		}

	}.setPeriod(1);

	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		if (mt.equals(MaterialType.IRON_INGOT)) {
			if (ct.equals(ClickType.RIGHT_CLICK)) {
				if (!Skill.isDuration() && !Cool.isCooldown()) {

					Skill.startTimer();

					return true;
				}
			}
		}

		return false;
	}

	@Override
	public void TargetSkill(MaterialType mt, LivingEntity entity) {
	}

}
