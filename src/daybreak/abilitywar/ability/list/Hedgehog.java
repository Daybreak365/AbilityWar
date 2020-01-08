package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.event.AbilityRestrictionClearEvent;
import daybreak.abilitywar.config.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.math.LocationUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.LivingEntity;

@AbilityManifest(Name = "고슴도치", Rank = Rank.C, Species = Species.ANIMAL)
public class Hedgehog extends AbilityBase {

	public static final SettingObject<Double> DamageConfig = new SettingObject<Double>(Hedgehog.class, "Damage", 2.0,
			"# 데미지") {

		@Override
		public boolean Condition(Double value) {
			return value >= 0;
		}

	};

	public Hedgehog(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f가시로 가까이에 있는 모든 생명체를 찌릅니다."),
				ChatColor.translateAlternateColorCodes('&', "&f아야! 그것 참 아프겠네요."));
	}

	@Override
	public boolean ActiveSkill(Material materialType, ClickType ct) {
		return false;
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
	}

	private final double damage = DamageConfig.getValue();

	private final Timer passive = new Timer() {
		@Override
		protected void onProcess(int count) {
			for (Damageable damageable : LocationUtil.getNearbyDamageableEntities(getPlayer(), 1.5, 1.5)) {
				damageable.damage(damage, getPlayer());
			}
		}
	}.setPeriod(10);

	@SubscribeEvent(onlyRelevant = true)
	private void onRestrictionClear(AbilityRestrictionClearEvent e) {
		passive.startTimer();
	}

}
