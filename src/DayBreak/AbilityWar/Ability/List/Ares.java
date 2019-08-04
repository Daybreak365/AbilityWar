package DayBreak.AbilityWar.Ability.List;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Ability.AbilityManifest;
import DayBreak.AbilityWar.Ability.AbilityManifest.Rank;
import DayBreak.AbilityWar.Ability.AbilityManifest.Species;
import DayBreak.AbilityWar.Ability.SubscribeEvent;
import DayBreak.AbilityWar.Ability.Timer.CooldownTimer;
import DayBreak.AbilityWar.Ability.Timer.DurationTimer;
import DayBreak.AbilityWar.Config.AbilitySettings.SettingObject;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame.Participant;
import DayBreak.AbilityWar.Utils.Messager;
import DayBreak.AbilityWar.Utils.Library.ParticleLib;
import DayBreak.AbilityWar.Utils.Library.SoundLib;
import DayBreak.AbilityWar.Utils.Math.LocationUtil;

@AbilityManifest(Name = "아레스", Rank = Rank.A, Species = Species.GOD)
public class Ares extends AbilityBase {
	
	public static SettingObject<Integer> DamageConfig = new SettingObject<Integer>(Ares.class, "DamagePercent", 55, 
			"# 스킬 데미지") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};
	
	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Ares.class, "Cooldown", 60, 
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};
	
	public static SettingObject<Boolean> DashConfig = new SettingObject<Boolean>(Ares.class, "DashIntoTheAir", false, 
			"# true로 설정하면 아레스 능력 사용 시 공중으로 돌진 할 수 있습니다.") {
		
		@Override
		public boolean Condition(Boolean value) {
			return true;
		}
		
	};
	
	public Ares(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f전쟁의 신 아레스."),
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 앞으로 돌진하며 주위의 엔티티에게 데미지를 주며,"),
				ChatColor.translateAlternateColorCodes('&', "&f데미지를 받은 엔티티들을 밀쳐냅니다. ") + Messager.formatCooldown(CooldownConfig.getValue()));
	}
	
	private CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());
	
	private DurationTimer Duration = new DurationTimer(this, 20, Cool) {
		
		private boolean DashIntoTheAir = DashConfig.getValue();
		private int DamagePercent = DamageConfig.getValue();
		private ArrayList<Damageable> Attacked;
		
		@Override
		protected void onDurationStart() {
			Attacked = new ArrayList<Damageable>();
			List<Player> nearby = LocationUtil.getNearbyPlayers(getPlayer().getLocation(), 10, 10);
			SoundLib.BLOCK_BELL_USE.playSound(nearby);
		}
		
		@Override
		public void DurationProcess(Integer Seconds) {
			Player p = getPlayer();
			
			ParticleLib.LAVA.spawnParticle(p.getLocation(), 4, 4, 4, 40);
			
			if(DashIntoTheAir) {
				p.setVelocity(p.getVelocity().add(p.getLocation().getDirection().multiply(0.7)));
			} else {
				p.setVelocity(p.getVelocity().add(p.getLocation().getDirection().multiply(0.7).setY(0)));
			}
			
			for(Damageable d : LocationUtil.getNearbyDamageableEntities(p, 4, 4)) {
				double Damage = (d.getHealth() / 100) * DamagePercent;
				if(!Attacked.contains(d)) {
					d.damage(Damage, p);
					Attacked.add(d);
					SoundLib.BLOCK_ANVIL_LAND.playSound(p, 0.5f, 1);
				} else {
					d.damage(Damage / 5, p);
				}

				d.setVelocity(p.getLocation().toVector().subtract(d.getLocation().toVector()).multiply(-1).setY(1));
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
	
	@SubscribeEvent
	public void onEntityDamage(EntityDamageEvent e) {
		if(e.getEntity().equals(getPlayer()) && e.getCause().equals(DamageCause.FALL) && Duration.isDuration()) {
			e.setCancelled(true);
		}
	}
	
	@Override
	public void onRestrictClear() {}

	@Override
	public void TargetSkill(MaterialType mt, Entity entity) {}
	
}
