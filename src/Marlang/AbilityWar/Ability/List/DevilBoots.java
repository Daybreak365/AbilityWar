package Marlang.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.GameManager.Object.Participant;

public class DevilBoots extends AbilityBase {

	public DevilBoots(Participant participant) {
		super(participant, "악마의 부츠", Rank.B,
				ChatColor.translateAlternateColorCodes('&', "&f지나가는 자리에 불이 붙습니다. 화염 피해를 받지 않습니다."));
	}
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		return false;
	}
	
	@Override
	public void PassiveSkill(Event event) {
		if(event instanceof PlayerMoveEvent) {
			PlayerMoveEvent e = (PlayerMoveEvent) event;
			if(e.getPlayer().equals(getPlayer())) {
				Location To = e.getTo();
				if(To.getBlock().getType().equals(Material.AIR)) {
					To.getBlock().setType(Material.FIRE);
				}
			}
		} else if(event instanceof EntityDamageEvent) {
			EntityDamageEvent e = (EntityDamageEvent) event;
			if(e.getEntity().equals(getPlayer())) {
				DamageCause cause = e.getCause();
				if(cause.equals(DamageCause.FIRE) || cause.equals(DamageCause.FIRE_TICK) || cause.equals(DamageCause.LAVA)) {
					e.setCancelled(true);
				}
			}
		}
	}
	
	@Override
	public void onRestrictClear() {}

	@Override
	public void TargetSkill(MaterialType mt, Entity entity) {}
	
}
