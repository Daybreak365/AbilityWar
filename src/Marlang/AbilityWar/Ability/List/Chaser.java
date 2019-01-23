package Marlang.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.Timer.CooldownTimer;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.Utils.Messager;

public class Chaser extends AbilityBase {

	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>("추적자", "Cooldown", 120,
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};
	
	public Chaser() {
		super("추적자", Rank.B,
				ChatColor.translateAlternateColorCodes('&', "&f상대방을 철괴로 타격하면 대상에게 추적 장치를 부착합니다. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&f이후 철괴를 우클릭하면 추적 장치를 부착한 플레이어의 좌표를 알 수 있습니다."),
				ChatColor.translateAlternateColorCodes('&', "&f추적 장치는 한명에게만 부착할 수 있습니다."));
		
		registerTimer(Cool);
	}

	CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());
	
	Player target = null;
	
	@Override
	public void ActiveSkill(ActiveMaterialType mt, ActiveClickType ct) {
		if(mt.equals(ActiveMaterialType.Iron_Ingot)) {
			if(ct.equals(ActiveClickType.LeftClick)) {
				if(!Cool.isCooldown()) {
					Messager.sendMessage(getPlayer(), ChatColor.translateAlternateColorCodes('&', "&a대상&f이 없습니다!"));
				}
			} else if(ct.equals(ActiveClickType.RightClick)) {
				if(target != null) {
					int X = (int) target.getLocation().getX();
					int Y = (int) target.getLocation().getY();
					int Z = (int) target.getLocation().getZ();
					
					Messager.sendMessage(getPlayer(), ChatColor.translateAlternateColorCodes('&', "&e" + target.getName() + "&f님은 &aX " + X + "&f, &aY " + Y + "&f, &aZ " + Z + "&f에 있습니다."));
				} else {
					Messager.sendMessage(getPlayer(), ChatColor.translateAlternateColorCodes('&', "&f아무에게도 추적 장치를 부척하지 않았습니다. &8( &7추적 불가능 &8)"));
				}
			}
		}
	}

	@Override
	public void PassiveSkill(Event event) {
		if(event instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
			if(e.getDamager().equals(getPlayer())) {
				if(e.getEntity() instanceof Player) {
					if(!e.isCancelled()) {
						if(getPlayer().getInventory().getItemInMainHand().getType().equals(Material.IRON_INGOT)) {
							if(!Cool.isCooldown()) {
								Player p = (Player) e.getEntity();
								this.target = p;
								Messager.sendMessage(getPlayer(), ChatColor.translateAlternateColorCodes('&', "&e" + p.getName() + "&f님에게 추적 장치를 부착하였습니다."));
								
								Cool.StartTimer();
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
