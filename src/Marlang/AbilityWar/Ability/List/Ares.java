package Marlang.AbilityWar.Ability.List;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.Timer.CooldownTimer;
import Marlang.AbilityWar.Ability.Timer.DurationTimer;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Library.ParticleLib;
import Marlang.AbilityWar.Utils.Math.LocationUtil;

public class Ares extends AbilityBase {
	
	public static SettingObject<Integer> DamageConfig = new SettingObject<Integer>("아레스", "Damage", 8, 
			"# 스킬 데미지") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};
	
	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>("아레스", "Cooldown", 60, 
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};
	
	public static SettingObject<Boolean> DashConfig = new SettingObject<Boolean>("아레스", "DashIntoTheAir", false, 
			"# true로 설정하면 아레스 능력 사용 시 공중으로 돌진 할 수 있습니다.") {
		
		@Override
		public boolean Condition(Boolean value) {
			return true;
		}
		
	};
	
	public Ares(Player player) {
		super(player, "아레스", Rank.GOD, 
				ChatColor.translateAlternateColorCodes('&', "&f전쟁의 신 아레스."),
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 앞으로 돌진하며 주위의 엔티티에게 데미지를 주며,"),
				ChatColor.translateAlternateColorCodes('&', "&f데미지를 받은 엔티티들을 끌고 갑니다. ") + Messager.formatCooldown(CooldownConfig.getValue()));
	}
	
	CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());
	
	DurationTimer Duration = new DurationTimer(this, 14, Cool) {
		
		private boolean DashIntoTheAir = DashConfig.getValue();
		private int Damage = DamageConfig.getValue();
		private ArrayList<Damageable> Attacked;
		
		@Override
		protected void onDurationStart() {
			Attacked = new ArrayList<Damageable>();
		}
		
		@Override
		public void DurationProcess(Integer Seconds) {
			Player p = getPlayer();
			
			ParticleLib.LAVA.spawnParticle(p.getLocation(), 40, 4, 4, 4);
			
			if(DashIntoTheAir) {
				p.setVelocity(p.getVelocity().add(p.getLocation().getDirection()));
			} else {
				p.setVelocity(p.getVelocity().add(p.getLocation().getDirection().setY(0)));
			}
			
			for(Damageable d : LocationUtil.getNearbyDamageableEntities(p, 4, 4)) {
				if(!Attacked.contains(d)) {
					d.damage(Damage, p);
					Attacked.add(d);
				}
			}
			
			for(Damageable d : Attacked) {
				d.teleport(p);
			}
		}
		
		@Override
		protected void onDurationEnd() {}
		
	}.setPeriod(1);
	
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
	public void PassiveSkill(Event event) {}

	@Override
	public void onRestrictClear() {}

	@Override
	public void TargetSkill(MaterialType mt, Entity entity) {}
	
}
