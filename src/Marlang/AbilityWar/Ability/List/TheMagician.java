package Marlang.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.ProjectileHitEvent;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.AbilityManifest;
import Marlang.AbilityWar.Ability.AbilityManifest.Rank;
import Marlang.AbilityWar.Ability.Timer.CooldownTimer;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.Game.Games.AbstractGame.Participant;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Library.ParticleLib;
import Marlang.AbilityWar.Utils.Library.SoundLib;
import Marlang.AbilityWar.Utils.Math.LocationUtil;

@AbilityManifest(Name = "마술사", Rank = Rank.A)
public class TheMagician extends AbilityBase {

	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(TheMagician.class, "Cooldown", 5, 
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};

	public static SettingObject<Integer> DamageConfig = new SettingObject<Integer>(TheMagician.class, "Damage", 3, 
			"# 데미지") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}
		
	};

	public TheMagician(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f활을 쐈을 때, 화살이 맞은 위치에서 5칸 범위 내에 있는 플레이어들에게"),
				ChatColor.translateAlternateColorCodes('&', "&f" + DamageConfig.getValue() + "만큼의 데미지를 추가로 입힙니다. " + Messager.formatCooldown(CooldownConfig.getValue())));
	}

	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		return false;
	}

	CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());
	
	@Override
	public void PassiveSkill(Event event) {
		if(event instanceof ProjectileHitEvent) {
			ProjectileHitEvent e = (ProjectileHitEvent) event;
			if(e.getEntity() instanceof Arrow) {
				if(e.getEntity().getShooter().equals(getPlayer())) {
					if(!Cool.isCooldown()) {
						Location center = e.getEntity().getLocation();
						for(Player p : LocationUtil.getNearbyPlayers(center, 5, 5)) {
							if(!p.equals(getPlayer())) {
								if(LocationUtil.isInCircle(center, p.getLocation(), 5.0)) {
									p.damage(3, p);
									SoundLib.ENTITY_ILLUSIONER_CAST_SPELL.playSound(p);
								}
							}
						}
						
						for(Location l : LocationUtil.getCircle(center, 5, 10, true)) {
							ParticleLib.SPELL_WITCH.spawnParticle(l, 1, 0, 0, 0);
						}
						
						Cool.StartTimer();
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
