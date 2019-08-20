package DayBreak.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;

import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Ability.AbilityManifest;
import DayBreak.AbilityWar.Ability.AbilityManifest.Rank;
import DayBreak.AbilityWar.Ability.AbilityManifest.Species;
import DayBreak.AbilityWar.Ability.SubscribeEvent;
import DayBreak.AbilityWar.Ability.Timer.CooldownTimer;
import DayBreak.AbilityWar.Config.AbilitySettings.SettingObject;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame.Participant;
import DayBreak.AbilityWar.Utils.Messager;
import DayBreak.AbilityWar.Utils.Library.ParticleLib;
import DayBreak.AbilityWar.Utils.Library.SoundLib;
import DayBreak.AbilityWar.Utils.Math.LocationUtil;
import DayBreak.AbilityWar.Utils.Math.Geometry.Circle;
import DayBreak.AbilityWar.Utils.VersionCompat.VersionUtil;

@AbilityManifest(Name = "마술사", Rank = Rank.A, Species = Species.HUMAN)
public class TheMagician extends AbilityBase {

	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(TheMagician.class, "Cooldown", 3, 
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};

	public TheMagician(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f활을 쐈을 때, 화살이 맞은 위치에서 5칸 범위 내에 있는 엔티티들에게"),
				ChatColor.translateAlternateColorCodes('&', "&f최대체력의 1/5 만큼의 데미지를 추가로 입힙니다. " + Messager.formatCooldown(CooldownConfig.getValue())));
	}

	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		return false;
	}

	private CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());
	
	@SubscribeEvent
	public void onProjectileHit(ProjectileHitEvent e) {
		if(e.getEntity() instanceof Arrow) {
			if(e.getEntity().getShooter().equals(getPlayer())) {
				if(!Cool.isCooldown()) {
					SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(getPlayer());
					Location center = e.getEntity().getLocation();
					for(Damageable d : LocationUtil.getNearbyDamageableEntities(center, 5, 5)) {
						if(!d.equals(getPlayer())) {
							if(LocationUtil.isInCircle(center, d.getLocation(), 5.0, false)) {
								d.damage(VersionUtil.getMaxHealth(d) / 5, getPlayer());
								if(d instanceof Player) {
									SoundLib.ENTITY_ILLUSIONER_CAST_SPELL.playSound((Player) d);
								}
							}
						}
					}
					
					for(Location l : new Circle(center, 5).setAmount(30).setHighestLocation(true).getLocations()) {
						ParticleLib.SPELL_WITCH.spawnParticle(l, 0, 0, 0, 1);
					}
					ParticleLib.CLOUD.spawnParticle(center, 5, 5, 5, 50);
					
					Cool.StartTimer();
				}
			}
		}
	}
	
	@Override
	public void onRestrictClear() {}

	@Override
	public void TargetSkill(MaterialType mt, LivingEntity entity) {}
	
}
