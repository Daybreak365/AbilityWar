package Marlang.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.Timer.CooldownTimer;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.Utils.Messager;

public class EnergyBlocker extends AbilityBase {

	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>("에너지블로커", "Cooldown", 10, 
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};
	
	boolean Default = true;
	
	public EnergyBlocker() {
		super("에너지 블로커", Rank.A,
				ChatColor.translateAlternateColorCodes('&', "&f원거리 공격 피해를 절반으로, 근거리 공격 피해를 두 배로 받거나"),
				ChatColor.translateAlternateColorCodes('&', "&f원거리 공격 피해를 두 배로, 근거리 공격 피해를 절반으로 받을 수 있습니다."),
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 각각의 피해 정도를 뒤바꿉니다. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 좌클릭하면 현재 상태를 확인할 수 있습니다."));
		
		registerTimer(Cool);
	}
	
	CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());
	
	@Override
	public void ActiveSkill(ActiveMaterialType mt, ActiveClickType ct) {
		if(mt.equals(ActiveMaterialType.Iron_Ingot)) {
			if(ct.equals(ActiveClickType.RightClick)) {
				if(!Cool.isCooldown()) {
					Default = !Default;
					Player p = getPlayer();
					if(Default) {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b원거리 &f절반&7, &a근거리 &f두 배로 변경되었습니다."));
					} else {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b원거리 &f두 배&7, &a근거리 &f절반으로 변경되었습니다."));
					}
					
					Cool.StartTimer();
				}
			} else if(ct.equals(ActiveClickType.LeftClick)) {
				if(Default) {
					Messager.sendMessage(getPlayer(), ChatColor.translateAlternateColorCodes('&', "&6현재 상태&f: &b원거리 &f절반&7, &a근거리 &f두 배"));
				} else {
					Messager.sendMessage(getPlayer(), ChatColor.translateAlternateColorCodes('&', "&6현재 상태&f: &b원거리 &f두 배&7, &a근거리 &f절반"));
				}
			}
		}
	}
	
	@Override
	public void PassiveSkill(Event event) {
		if(event instanceof EntityDamageEvent) {
			EntityDamageEvent e = (EntityDamageEvent) event;
			if(e.getEntity() instanceof Player) {
				Player p = (Player) e.getEntity();
				if(p.equals(getPlayer())) {
					if(!e.isCancelled()) {
						DamageCause dc = e.getCause();
						if(dc != null) {
							if(dc.equals(DamageCause.PROJECTILE)) {
								if(Default) {
									e.setDamage(e.getDamage() / 2);
								} else {
									e.setDamage(e.getDamage() * 2);
								}
							} else if(dc.equals(DamageCause.ENTITY_ATTACK)) {
								if(Default) {
									e.setDamage(e.getDamage() * 2);
								} else {
									e.setDamage(e.getDamage() / 2);
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void AbilityEvent(EventType type) {}
	
}
