package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.library.PotionEffects;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Random;

@AbilityManifest(Name = "데미갓", Rank = Rank.S, Species = Species.DEMIGOD)
public class Demigod extends AbilityBase {

	public static final SettingObject<Integer> ChanceConfig = new SettingObject<Integer>(Demigod.class, "Chance", 40,
			"# 공격을 받았을 시 몇 퍼센트 확률로 랜덤 버프를 받을지 설정합니다.",
			"# 40은 40%를 의미합니다.") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 1 && value <= 100;
		}

	};

	public Demigod(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f반신반인의 능력자입니다. 공격을 받으면"),
				ChatColor.translateAlternateColorCodes('&', "&f" + ChanceConfig.getValue() + "% 확률로 5초간 랜덤 버프가 발동됩니다."));
	}

	@Override
	public boolean ActiveSkill(Material materialType, ClickType ct) {
		return false;
	}

	private final int Chance = ChanceConfig.getValue();

	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if (p.equals(getPlayer())) {
				if (!e.isCancelled()) {
					Random r = new Random();

					if ((r.nextInt(100) + 1) <= Chance) {
						Integer Buff = r.nextInt(3);
						if (Buff.equals(0)) {
							PotionEffects.ABSORPTION.addPotionEffect(p, 100, 1, true);
						} else if (Buff.equals(1)) {
							PotionEffects.REGENERATION.addPotionEffect(p, 100, 0, true);
						} else if (Buff.equals(2)) {
							PotionEffects.DAMAGE_RESISTANCE.addPotionEffect(p, 100, 1, true);
						}
					}
				}
			}
		}
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
	}

}
