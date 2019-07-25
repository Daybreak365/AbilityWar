package DayBreak.AbilityWar.Ability.List;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Ability.AbilityManifest;
import DayBreak.AbilityWar.Ability.AbilityManifest.Rank;
import DayBreak.AbilityWar.Ability.AbilityManifest.Species;
import DayBreak.AbilityWar.Config.AbilitySettings.SettingObject;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame.Participant;
import DayBreak.AbilityWar.Utils.VersionCompat.VersionUtil;

@AbilityManifest(Name = "이열치열", Rank = Rank.B, Species = Species.HUMAN)
public class FireFightWithFire extends AbilityBase {

	public static SettingObject<Integer> ChanceConfig = new SettingObject<Integer>(FireFightWithFire.class, "Chance", 50,
			"# 공격을 받았을 시 몇 퍼센트 확률로 회복을 할지 설정합니다.",
			"# 50은 50%를 의미합니다.") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1 && value <= 100;
		}
		
	};
	
	public FireFightWithFire(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f불 데미지를 받을 때, " + ChanceConfig.getValue() + "% 확률로 체력을 회복합니다."));
	}

	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		return false;
	}

	@Override
	public void PassiveSkill(Event event) {
		if(event instanceof EntityDamageEvent) {
			EntityDamageEvent e = (EntityDamageEvent) event;
			if(e.getEntity().equals(getPlayer())) {
				if(e.getCause().equals(DamageCause.FIRE) || e.getCause().equals(DamageCause.FIRE_TICK) || e.getCause().equals(DamageCause.LAVA)) {
					Random r = new Random();
					if(r.nextInt(100) <= ChanceConfig.getValue() - 1) {
						Double damage = e.getDamage();
						e.setDamage(0);
						
						Double health = getPlayer().getHealth() + damage;
						
						if(health > VersionUtil.getMaxHealth(getPlayer())) health = VersionUtil.getMaxHealth(getPlayer());
						
						if(!getPlayer().isDead()) {
							getPlayer().setHealth(health);
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
