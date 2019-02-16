package Marlang.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.Timer.CooldownTimer;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.Utils.FallBlock;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Library.ParticleLib;
import Marlang.AbilityWar.Utils.Library.SoundLib;
import Marlang.AbilityWar.Utils.Math.LocationUtil;
import Marlang.AbilityWar.Utils.Thread.TimerBase;

public class Nex extends AbilityBase {

	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>("넥스", "Cooldown", 120, "# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public static SettingObject<Integer> DamageConfig = new SettingObject<Integer>("넥스", "Damage", 8, "# 데미지") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}

	};
	
	public Nex(Player player) {
		super(player, "넥스", Rank.B,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 공중으로 올라갔다가 바닥으로 내려 찍으며"),
				ChatColor.translateAlternateColorCodes('&', "주변의 플레이어들에게 데미지를 입힙니다. " + Messager.formatCooldown(CooldownConfig.getValue())));
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
		public void onStart() {
			NoFall = true;
			Vector v = new Vector(0, 4, 0);

			getPlayer().setVelocity(getPlayer().getVelocity().add(v));
		}

		@Override
		public void TimerProcess(Integer Seconds) {
		}

		@Override
		public void onEnd() {
			RunSkill = true;
			Vector v = new Vector(0, -4, 0);

			getPlayer().setVelocity(getPlayer().getVelocity().add(v));
		}

	}.setPeriod(10);
	
	Integer Damage = DamageConfig.getValue();
	
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
							player.damage(Damage, getPlayer());
						}
						SoundLib.ENTITY_GENERIC_EXPLODE.playSound(getPlayer());
						
						if(!db.getType().equals(Material.AIR)) {
							ParticleLib.BLOCK_CRACK.spawnParticle(getPlayer().getLocation(), 30, 2, 2, 2, new MaterialData(db.getType()));
						} else {
							ParticleLib.BLOCK_CRACK.spawnParticle(getPlayer().getLocation(), 30, 2, 2, 2, new MaterialData(b.getType()));
						}
						
						FallBlock.StartTimer();
					}
				}
			}
		}
	}
	
	TimerBase FallBlock = new TimerBase(5) {
		
		Location center;
		
		@Override
		public void onStart() {
			this.center = getPlayer().getLocation();
		}
		
		@Override
		public void TimerProcess(Integer Seconds) {
			Integer Distance = 6 - Seconds;
			
			for(Block block : LocationUtil.getBlocks(center, Distance, true, true, false)) {
				FallBlock fb = new FallBlock(block.getState().getData(), block.getLocation().add(0, 1, 0), new Vector(0, 0.5, 0));
				fb.Spawn(false);
			}
			
			for(Damageable e : LocationUtil.getNearbyDamageableEntities(center, 5, 5)) {
				if(!e.equals(getPlayer())) {
					e.setVelocity(center.toVector().subtract(e.getLocation().toVector()).multiply(-1).setY(1.2));
				}
			}
		}
		
		@Override
		public void onEnd() {}
		
	}.setPeriod(4);
	
	@Override
	public void onRestrictClear() {}

}
