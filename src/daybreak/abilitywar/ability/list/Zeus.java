package daybreak.abilitywar.ability.list;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.timer.CooldownTimer;
import daybreak.abilitywar.config.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.math.LocationUtil;
import daybreak.abilitywar.utils.math.geometry.Circle;
import daybreak.abilitywar.utils.thread.TimerBase;

@AbilityManifest(Name = "제우스", Rank = Rank.S, Species = Species.GOD)
public class Zeus extends AbilityBase {
	
	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Zeus.class, "Cooldown", 180,
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};
	
	public Zeus(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f번개의 신 제우스."),
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 주변에 번개를 떨어뜨리며 폭발을 일으킵니다. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&f번개를 맞은 플레이어는 3초간 구속됩니다."),
				ChatColor.translateAlternateColorCodes('&', "&f번개 데미지와 폭발 데미지를 받지 않습니다."));
	}
	
	private CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());
	
	private TimerBase Skill = new TimerBase(3) {

		Location center;
		
		@Override
		public void onStart() {
			center = getPlayer().getLocation();
		}
		
		@Override
		public void onProcess(int Seconds) {
			Circle circle = new Circle(center, 2 * (5 - getCount())).setAmount(7).setHighestLocation(true);
			for(Location l : circle.getLocations()) {
				l.getWorld().strikeLightningEffect(l);
				for(Damageable d : LocationUtil.getNearbyDamageableEntities(l, 4, 4)) {
					if(!d.equals(getPlayer())) {
						d.damage(d.getHealth() / 5, getPlayer());
						if(d instanceof Player) {
							Zeus.this.getGame().getEffectManager().Stun((Player) d, 60);
						}
					}
				}
				l.getWorld().createExplosion(l, 3);
			}
		}
		
		@Override
		public void onEnd() {}
		
	}.setPeriod(2);
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		if(mt.equals(MaterialType.Iron_Ingot)) {
			if(ct.equals(ClickType.RightClick)) {
				if(!Cool.isCooldown()) {
					Skill.StartTimer();
					
					Cool.StartTimer();
					
					return true;
				}
			}
		}
		
		return false;
	}

	@SubscribeEvent
	public void onEntityDamage(EntityDamageEvent e) {
		if(e.getEntity().equals(getPlayer())) {
			if(e.getCause().equals(DamageCause.LIGHTNING) || e.getCause().equals(DamageCause.BLOCK_EXPLOSION) || e.getCause().equals(DamageCause.ENTITY_EXPLOSION)) {
				e.setCancelled(true);
			}
		}
	}

	@SubscribeEvent
	public void onEntityDamage(EntityDamageByEntityEvent e) {
		if(e.getEntity().equals(getPlayer())) {
			if(e.getCause().equals(DamageCause.LIGHTNING) || e.getCause().equals(DamageCause.BLOCK_EXPLOSION) || e.getCause().equals(DamageCause.ENTITY_EXPLOSION)) {
				e.setCancelled(true);
			}
		}
	}

	@SubscribeEvent
	public void onEntityDamage(EntityDamageByBlockEvent e) {
		if(e.getEntity().equals(getPlayer())) {
			if(e.getCause().equals(DamageCause.LIGHTNING) || e.getCause().equals(DamageCause.BLOCK_EXPLOSION) || e.getCause().equals(DamageCause.ENTITY_EXPLOSION)) {
				e.setCancelled(true);
			}
		}
	}
	
	@Override
	public void onRestrictClear() {}
	
	@Override
	public void TargetSkill(MaterialType mt, LivingEntity entity) {}
	
}
