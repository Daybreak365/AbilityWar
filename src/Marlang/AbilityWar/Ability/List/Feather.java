package Marlang.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.AbilityManifest;
import Marlang.AbilityWar.Ability.AbilityManifest.Rank;
import Marlang.AbilityWar.Ability.Timer.CooldownTimer;
import Marlang.AbilityWar.Ability.Timer.DurationTimer;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.GameManager.Object.Participant;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Library.SoundLib;

@AbilityManifest(Name = "", Rank = Rank.A)
public class Feather extends AbilityBase {

	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>("깃털", "Cooldown", 80, 
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};

	public static SettingObject<Integer> DurationConfig = new SettingObject<Integer>("깃털", "Duration", 15, 
			"# 지속시간") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};
	
	public Feather(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 15초간 비행할 수 있습니다. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&f낙하 데미지를 무시합니다."));
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
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		if(mt.equals(MaterialType.Iron_Ingot)) {
			if(ct.equals(ClickType.RightClick)) {
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
							Messager.sendMessage(p, ChatColor.translateAlternateColorCodes('&', "&a낙하 데미지를 받지 않습니다."));
							SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(p);
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
