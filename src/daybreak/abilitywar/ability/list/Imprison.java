package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.math.LocationUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

@AbilityManifest(Name = "구속", Rank = Rank.B, Species = Species.HUMAN)
public class Imprison extends AbilityBase {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Imprison.class, "Cooldown", 25, "# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public static final SettingObject<Integer> SizeConfig = new SettingObject<Integer>(Imprison.class, "Size", 3, "# 스킬 크기") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public Imprison(Participant participant) {
		super(participant, ChatColor.translateAlternateColorCodes('&',
				"&f상대방을 철괴로 우클릭하면 대상을 유리막 속에 가둡니다. " + Messager.formatCooldown(CooldownConfig.getValue())));
	}

	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());

	private final int size = SizeConfig.getValue();

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		return false;
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
		if (materialType.equals(Material.IRON_INGOT)) {
			if (entity != null) {
				if (!cooldownTimer.isCooldown()) {
					for (Block b : LocationUtil.getBlocks3D(entity.getLocation(), size, true, true)) {
						b.setType(Material.GLASS);
					}

					cooldownTimer.startTimer();
				}
			} else {
				cooldownTimer.isCooldown();
			}
		}
	}

}
