package Marlang.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.Timer.CooldownTimer;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.Utils.LocationUtil;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.TimerBase;
import Marlang.AbilityWar.Utils.Library.SoundLib;

public class Nex extends AbilityBase {

	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>("넥스", "Cooldown", 120, "# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public Nex() {
		super("넥스", Rank.B,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 공중으로 올라갔다가 바닥으로 내려 찍으며"),
				ChatColor.translateAlternateColorCodes('&', "주변의 플레이어들에게 데미지를 입힙니다. " + Messager.formatCooldown(CooldownConfig.getValue())));

		registerTimer(Cool);

		Skill.setPeriod(10);

		registerTimer(Skill);

	}

	CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());

	@Override
	public boolean ActiveSkill(ActiveMaterialType mt, ActiveClickType ct) {
		if (mt.equals(ActiveMaterialType.Iron_Ingot)) {
			if (ct.equals(ActiveClickType.RightClick)) {
				if(!Cool.isCooldown()) {
					for(Player player : LocationUtil.getNearbyPlayers(getPlayer(), 5, 5)) {
						SoundLib.ENTITY_WITHER_SPAWN.playSound(player);
					}
					SoundLib.ENTITY_WITHER_SPAWN.playSound(getPlayer());
					Skill.StartTimer();
					
					Cool.StartTimer();
					
					return true;
				}
			}
		}
		
		return false;
	}

	boolean NoFall = false;
	boolean RunSkill = false;

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
			RunSkill = true;
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
						}
					}
				}
			}
		} else if(event instanceof PlayerMoveEvent) {
			PlayerMoveEvent e = (PlayerMoveEvent) event;
			if(e.getPlayer().equals(getPlayer())) {
				if(RunSkill) {
					Block b = getPlayer().getLocation().getBlock();
					Block db = getPlayer().getLocation().subtract(0, 1, 0).getBlock();
					
					if(!b.getType().equals(Material.AIR) || !db.getType().equals(Material.AIR)) {
						RunSkill = false;
						for(Player player : LocationUtil.getNearbyPlayers(getPlayer(), 5, 5)) {
							SoundLib.ENTITY_GENERIC_EXPLODE.playSound(player);
							player.damage(10, getPlayer());
						}
						SoundLib.ENTITY_GENERIC_EXPLODE.playSound(getPlayer());
					}
				}
			}
		}
	}

	@Override
	public void AbilityEvent(EventType type) {}

}
