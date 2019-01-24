package Marlang.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.Timer.CooldownTimer;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.Utils.LocationUtil;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.TimerBase;

public class Nex extends AbilityBase {

	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>("³Ø½º", "Cooldown", 120, "# ÄðÅ¸ÀÓ") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public Nex() {
		super("³Ø½º", Rank.A,
				ChatColor.translateAlternateColorCodes('&', "&f»ó´ë¹æÀ» Ã¶±«·Î Å¸°ÝÇÏ¸é ÇÏ´Ã·Î ²ø°í ¿Ã¶ó°¬´Ù°¡ ³»·Á¿À¸ç ¹Ù´Ú¿¡ ³»·Á Âï½À´Ï´Ù."),
				ChatColor.translateAlternateColorCodes('&', Messager.formatCooldown(CooldownConfig.getValue())));

		registerTimer(Cool);

		Skill.setPeriod(10);

		registerTimer(Skill);

	}

	CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());

	@Override
	public void ActiveSkill(ActiveMaterialType mt, ActiveClickType ct) {
		if (mt.equals(ActiveMaterialType.Iron_Ingot)) {
			if (ct.equals(ActiveClickType.RightClick)) {
				if(!Cool.isCooldown()) {
					Skill.StartTimer();
					
					Cool.StartTimer();
				}
			}
		}
	}

	boolean NoFall = false;

	TimerBase Skill = new TimerBase(4) {

		@Override
		public void TimerStart() {
			NoFall = true;
			Vector v = new Vector(0, 4, 0);

			getPlayer().setVelocity(getPlayer().getVelocity().add(v));
		}

		@Override
		public void TimerProcess(Integer Seconds) {
		}

		@Override
		public void TimerEnd() {
			Vector v = new Vector(0, -4, 0);

			getPlayer().setVelocity(getPlayer().getVelocity().add(v));
		}

	};

	@Override
	public void PassiveSkill(Event event) {
		if (event instanceof EntityDamageEvent) {
			EntityDamageEvent e = (EntityDamageEvent) event;
			if (e.getEntity() instanceof Player) {
				if(e.getEntity().equals(getPlayer())) {
					if (NoFall) {
						if (e.getCause().equals(DamageCause.FALL)) {
							e.setCancelled(true);
							NoFall = false;
							
							for(Player player : LocationUtil.getNearbyPlayers(getPlayer(), 5, 5)) {
								player.damage(10, getPlayer());
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
