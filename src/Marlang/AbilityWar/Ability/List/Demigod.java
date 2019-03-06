package Marlang.AbilityWar.Ability.List;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.AbilityManifest;
import Marlang.AbilityWar.Ability.AbilityManifest.Rank;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.GameManager.Object.Participant;
import Marlang.AbilityWar.Utils.Library.EffectLib;

@AbilityManifest(Name = "데미갓", Rank = Rank.GOD)
public class Demigod extends AbilityBase {
	
	public static SettingObject<Integer> ChanceConfig = new SettingObject<Integer>("데미갓", "Chance", 40,
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
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		return false;
	}
	
	private Integer Chance = ChanceConfig.getValue();
	
	@Override
	public void PassiveSkill(Event event) {
		if(event instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
			if(e.getEntity() instanceof Player) {
				Player p = (Player) e.getEntity();
				if(p.equals(getPlayer())) {
					if(!e.isCancelled()) {
						Random r = new Random();
						
						if((r.nextInt(100) + 1) <= Chance) {
							Integer Buff = r.nextInt(3);
							if(Buff.equals(0)) {
								EffectLib.ABSORPTION.addPotionEffect(p, 100, 0, true);
							} else if(Buff.equals(1)) {
								EffectLib.REGENERATION.addPotionEffect(p, 100, 0, true);
							} else if(Buff.equals(2)) {
								EffectLib.DAMAGE_RESISTANCE.addPotionEffect(p, 100, 1, true);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void onRestrictClear() {}

	@Override
	public void TargetSkill(MaterialType mt, Entity entity) {}
	
}
