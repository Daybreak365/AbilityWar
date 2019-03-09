package Marlang.AbilityWar.Ability.List;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.AbilityManifest;
import Marlang.AbilityWar.Ability.AbilityManifest.Rank;
import Marlang.AbilityWar.Ability.Timer.CooldownTimer;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.GameManager.Game.AbstractGame.Participant;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Library.SoundLib;
import Marlang.AbilityWar.Utils.Math.LocationUtil;
import Marlang.AbilityWar.Utils.Thread.TimerBase;

@AbilityManifest(Name = "암살자", Rank = Rank.A)
public class Assassin extends AbilityBase {
	
	public static SettingObject<Integer> DamageConfig = new SettingObject<Integer>("암살자", "Damage", 10, 
			"# 스킬 데미지") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};
	
	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>("암살자", "Cooldown", 60, 
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};
	
	public static SettingObject<Integer> TeleportCountConfig = new SettingObject<Integer>("암살자", "TeleportCount", 4,
			"# 능력 사용 시 텔레포트 횟수") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}
		
	};
	
	public Assassin(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 주변에 있는 적 " + TeleportCountConfig.getValue() + "명에게 텔레포트하며"),
				ChatColor.translateAlternateColorCodes('&', "&f데미지를 줍니다. " + Messager.formatCooldown(CooldownConfig.getValue())));
	}
	
	CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());
	
	TimerBase Duration = new TimerBase(TeleportCountConfig.getValue()) {
		
		ArrayList<Damageable> Entities = new ArrayList<Damageable>();
		
		Integer Damage = DamageConfig.getValue();
		
		@Override
		public void onStart() {
			Entities.addAll(LocationUtil.getNearbyDamageableEntities(getPlayer(), 6, 3));
		}
		
		@Override
		public void TimerProcess(Integer Seconds) {
			if(Entities.size() >= 1) {
				Damageable e = Entities.get(0);
				Entities.remove(e);
				getPlayer().teleport(e);
				e.damage(Damage, getPlayer());
				SoundLib.ENTITY_PLAYER_ATTACK_SWEEP.playSound(getPlayer());
				SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(getPlayer());
			} else {
				this.StopTimer(false);
			}
		}
		
		@Override
		public void onEnd() {}
		
	}.setPeriod(5);
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		if(mt.equals(MaterialType.Iron_Ingot)) {
			if(ct.equals(ClickType.RightClick)) {
				if(!Cool.isCooldown()) {
					Duration.StartTimer();
					
					Cool.StartTimer();
					
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
