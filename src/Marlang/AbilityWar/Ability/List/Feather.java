package Marlang.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.Timer.CooldownTimer;
import Marlang.AbilityWar.Ability.Timer.DurationTimer;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Library.SoundLib;

public class Feather extends AbilityBase {

	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>("±êÅÐ", "Cooldown", 80, 
			"# ÄðÅ¸ÀÓ") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};

	public static SettingObject<Integer> DurationConfig = new SettingObject<Integer>("±êÅÐ", "Duration", 15, 
			"# Áö¼Ó½Ã°£") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};
	
	public Feather(Player player) {
		super(player, "±êÅÐ", Rank.A,
				ChatColor.translateAlternateColorCodes('&', "&fÃ¶±«¸¦ ¿ìÅ¬¸¯ÇÏ¸é 15ÃÊ°£ ºñÇàÇÒ ¼ö ÀÖ½À´Ï´Ù. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&f³«ÇÏ µ¥¹ÌÁö¸¦ ¹«½ÃÇÕ´Ï´Ù."));
	}
	
	CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());
	
	DurationTimer Duration = new DurationTimer(this, DurationConfig.getValue(), Cool) {
		
		@Override
		public void onDurationStart() {}
		
		@Override
		public void DurationProcess(Integer Seconds) {
			getPlayer().setAllowFlight(true);
			getPlayer().setFlying(true);
		}
		
		@Override
		public void onDurationEnd() {
			getPlayer().setAllowFlight(false);
		}
		
	};
	
	@Override
	public boolean ActiveSkill(ActiveMaterialType mt, ActiveClickType ct) {
		if(mt.equals(ActiveMaterialType.Iron_Ingot)) {
			if(ct.equals(ActiveClickType.RightClick)) {
				if(!Duration.isDuration() && !Cool.isCooldown()) {
					Duration.StartTimer();
					
					return true;
				}
			}
		}
		
		return false;
	}
	
	@Override
	public void PassiveSkill(Event event) {
		if(event instanceof EntityDamageEvent) {
			EntityDamageEvent e = (EntityDamageEvent) event;
			if(!e.isCancelled()) {
				if(e.getEntity() instanceof Player) {
					Player p = (Player) e.getEntity();
					if(p.equals(this.getPlayer())) {
						if(e.getCause().equals(DamageCause.FALL)) {
							e.setCancelled(true);
							Messager.sendMessage(p, ChatColor.translateAlternateColorCodes('&', "&a³«ÇÏ µ¥¹ÌÁö¸¦ ¹ÞÁö ¾Ê½À´Ï´Ù."));
							SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(p);
						}
					}
				}
			}
		}
	}

	@Override
	public void onRestrictClear() {}
	
}
