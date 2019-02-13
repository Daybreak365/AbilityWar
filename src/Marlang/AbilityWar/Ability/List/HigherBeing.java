package Marlang.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.Utils.Library.ParticleLib;
import Marlang.AbilityWar.Utils.Library.SoundLib;

public class HigherBeing extends AbilityBase {

	public static SettingObject<Double> DamageConfig = new SettingObject<Double>("상위존재", "DamageMultiple", 2.0,
			"# 공격 배수") {
		
		@Override
		public boolean Condition(Double value) {
			return value > 1;
		}
		
	};
	
	public HigherBeing(Player player) {
		super(player, "상위존재", Rank.B,
				ChatColor.translateAlternateColorCodes('&', "&f자신보다 낮은 위치에 있는 생명체를 근접 공격 할 때"),
				ChatColor.translateAlternateColorCodes('&', "&f" + DamageConfig.getValue() + "배 강력하게 공격합니다."),
				ChatColor.translateAlternateColorCodes('&', "&f자신보다 높은 위치에 있는 생명체는 근접 공격으로 데미지를 입힐 수 없습니다."));
	}

	@Override
	public boolean ActiveSkill(ActiveMaterialType mt, ActiveClickType ct) {
		return false;
	}

	Double Multiple = DamageConfig.getValue();
	
	@Override
	public void PassiveSkill(Event event) {
		if(event instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
			if(e.getDamager().equals(getPlayer())) {
				if(e.getEntity().getLocation().getY() < getPlayer().getLocation().getY()) {
					e.setDamage(e.getDamage() * Multiple);
					SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(getPlayer());
					ParticleLib.LAVA.spawnParticle(e.getEntity().getLocation(), 5, 1, 1, 1);
				} else if(e.getEntity().getLocation().getY() != getPlayer().getLocation().getY()) {
					e.setCancelled(true);
					SoundLib.BLOCK_ANVIL_BREAK.playSound(getPlayer());
				}
			}
		}
	}

	@Override
	public void AbilityEvent(EventType type) {}

}
