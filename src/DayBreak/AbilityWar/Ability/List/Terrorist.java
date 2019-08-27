package DayBreak.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

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
import DayBreak.AbilityWar.Utils.Math.LocationUtil;
import DayBreak.AbilityWar.Utils.Math.Geometry.Circle;

@AbilityManifest(Name = "테러리스트", Rank = Rank.A, Species = Species.HUMAN)
public class Terrorist extends AbilityBase {

	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Terrorist.class, "Cooldown", 100,
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};

	public static SettingObject<Integer> CountConfig = new SettingObject<Integer>(Terrorist.class, "Count", 15,
			"# TNT 개수") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};
	
	public Terrorist(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 자신의 주위에 TNT " + (CountConfig.getValue() * 2) + "개를 떨어뜨립니다. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&f폭발 데미지를 입지 않습니다."));
	}

	private final int count = CountConfig.getValue();
	private CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		if(mt.equals(MaterialType.Iron_Ingot)) {
			if(ct.equals(ClickType.RightClick)) {
				if(!Cool.isCooldown()) {
					Location center = getPlayer().getLocation();
					for(int i = 0; i < 10; i++) {
						for(Location l : new Circle(center, i).setAmount(20).setHighestLocation(true).getLocations()) {
							ParticleLib.LAVA.spawnParticle(l, 0, 0, 0, 1);
						}
					}
					
					for(Location l : LocationUtil.getRandomLocations(center, 9, count)) l.getWorld().spawnEntity(l, EntityType.PRIMED_TNT);
					for(Location l : new Circle(center, 10).setAmount(count).setHighestLocation(true).getLocations()) l.getWorld().spawnEntity(l, EntityType.PRIMED_TNT);
					
					Cool.StartTimer();
					
					return true;
				}
			}
		}
		
		return false;
	}

	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if(e.getEntity().equals(getPlayer())) {
			if(e.getCause().equals(DamageCause.BLOCK_EXPLOSION) || e.getCause().equals(DamageCause.ENTITY_EXPLOSION)) {
				e.setCancelled(true);
			}
		}
	}
	
	@Override
	public void onRestrictClear() {}

	@Override
	public void TargetSkill(MaterialType mt, LivingEntity entity) {}
	
}
