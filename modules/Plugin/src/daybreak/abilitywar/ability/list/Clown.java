package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@AbilityManifest(name = "광대", rank = Rank.B, species = Species.HUMAN, explain = {
		"철괴를 우클릭하면 스폰으로 이동합니다. $[CooldownConfig]",
		"스폰으로 이동한 후 10초 안에 철괴를 다시 우클릭하면 원래 위치로 돌아가",
		"주변 $[RangeConfig]칸 이내의 플레이어들을 실명시킵니다."
})
public class Clown extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Clown.class, "Cooldown", 60,
			"# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};

	public static final SettingObject<Integer> RangeConfig = new SettingObject<Integer>(Clown.class, "Range", 10,
			"# 스킬 범위") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public Clown(Participant participant) {
		super(participant);
	}

	private Location originalPoint = null;

	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());
	private final DurationTimer skill = new DurationTimer(10, cooldownTimer) {

		@Override
		protected void onDurationStart() {
			originalPoint = getPlayer().getLocation();
			getPlayer().teleport(getPlayer().getWorld().getSpawnLocation());
		}

		@Override
		protected void onDurationProcess(int seconds) {
		}

	};

	private final int range = RangeConfig.getValue();

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT)) {
			if (clickType.equals(ClickType.RIGHT_CLICK)) {
				if (!skill.isDuration()) {
					if (!cooldownTimer.isCooldown()) {
						skill.start();

						return true;
					}
				} else {
					if (originalPoint != null) getPlayer().teleport(originalPoint);
					SoundLib.ENTITY_BAT_TAKEOFF.playSound(getPlayer());
					skill.stop(false);

					for (Player p : LocationUtil.getNearbyPlayers(getPlayer(), range, 250)) {
						SoundLib.ENTITY_WITHER_SPAWN.playSound(p);
						PotionEffects.BLINDNESS.addPotionEffect(p, 100, 2, true);
					}
				}
			}
		}

		return false;
	}

}
