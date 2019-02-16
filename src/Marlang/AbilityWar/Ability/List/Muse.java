package Marlang.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.Timer.CooldownTimer;
import Marlang.AbilityWar.Ability.Timer.DurationTimer;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Library.ParticleLib;
import Marlang.AbilityWar.Utils.Library.SoundLib;
import Marlang.AbilityWar.Utils.Math.LocationUtil;

public class Muse extends AbilityBase {

	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>("뮤즈", "Cooldown", 80, 
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};
	
	public Muse(Player player) {
		super(player, "뮤즈", Rank.S,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 뮤즈가 주변 지역을 축복하여"),
				ChatColor.translateAlternateColorCodes('&', "&f모두가 데미지를 받지 않는 지역을 만들어냅니다. ") + Messager.formatCooldown(CooldownConfig.getValue()));
	}

	CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());

	Location center = null;
	
	DurationTimer Skill = new DurationTimer(this, 90, Cool) {
		
		Integer Count;
		Integer SoundCount;
		
		@Override
		public void onDurationStart() {
			Count = 1;
			SoundCount = 1;
			center = getPlayer().getLocation();
		}
		
		@Override
		public void DurationProcess(Integer Seconds) {
			if(Count <= 10) {
				for(Location l : LocationUtil.getCircle(center, Count, Count * 6, true)) {
					ParticleLib.NOTE.spawnParticle(l.subtract(0, 1, 0), 1, 0, 0, 0);
				}
				
				if(Count.equals(1)) {
					for(Player p : LocationUtil.getNearbyPlayers(center, 20, 20)) {
						SoundLib.BELL.playInstrument(p, Note.natural(0, Tone.C));
					}
				} else if(Count.equals(2)) {
					for(Player p : LocationUtil.getNearbyPlayers(center, 20, 20)) {
						SoundLib.BELL.playInstrument(p, Note.natural(0, Tone.E));
					}
				} else if(Count.equals(3)) {
					for(Player p : LocationUtil.getNearbyPlayers(center, 20, 20)) {
						SoundLib.BELL.playInstrument(p, Note.natural(0, Tone.G));
					}
				} else if(Count.equals(4)) {
					for(Player p : LocationUtil.getNearbyPlayers(center, 20, 20)) {
						SoundLib.BELL.playInstrument(p, Note.natural(1, Tone.C));
					}
				} else if(Count.equals(5)) {
					for(Player p : LocationUtil.getNearbyPlayers(center, 20, 20)) {
						SoundLib.BELL.playInstrument(p, Note.natural(0, Tone.G));
					}
				} else if(Count.equals(6)) {
					for(Player p : LocationUtil.getNearbyPlayers(center, 20, 20)) {
						SoundLib.BELL.playInstrument(p, Note.natural(0, Tone.E));
					}
				} else if(Count.equals(7)) {
					for(Player p : LocationUtil.getNearbyPlayers(center, 20, 20)) {
						SoundLib.BELL.playInstrument(p, Note.natural(0, Tone.C));
					}
				} else if(Count.equals(8)) {
					for(Player p : LocationUtil.getNearbyPlayers(center, 20, 20)) {
						SoundLib.BELL.playInstrument(p, Note.natural(0, Tone.E));
					}
				} else if(Count.equals(9)) {
					for(Player p : LocationUtil.getNearbyPlayers(center, 20, 20)) {
						SoundLib.BELL.playInstrument(p, Note.natural(0, Tone.G));
					}
				} else if(Count.equals(10)) {
					for(Player p : LocationUtil.getNearbyPlayers(center, 20, 20)) {
						SoundLib.BELL.playInstrument(p, Note.natural(1, Tone.C));
					}
				}
				
				Count++;
			} else {
				for(Location l : LocationUtil.getCircle(center, Count, Count * 6, true)) {
					ParticleLib.NOTE.spawnParticle(l.subtract(0, 1, 0), 1, 0, 0, 0);
				}
				
				for(Player p : LocationUtil.getNearbyPlayers(center, 10, 200)) {
					if(LocationUtil.isInCircle(p.getLocation(), center, 20.0)) {
						p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 4, 0), true);
					}
					
					if(SoundCount % 5 == 0) {
						SoundCount = 1;
						
						SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(p);
					}
				}
				
				SoundCount++;
			}
		}
		
		@Override
		public void onDurationEnd() {
			center = null;
		}
		
	}.setPeriod(2);
	
	@Override
	public boolean ActiveSkill(ActiveMaterialType mt, ActiveClickType ct) {
		if(mt.equals(ActiveMaterialType.Iron_Ingot)) {
			if(ct.equals(ActiveClickType.RightClick)) {
				if(!Skill.isDuration() && !Cool.isCooldown()) {
					Skill.StartTimer();
					
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public void PassiveSkill(Event event) {
		if(event instanceof EntityDamageEvent) {
			EntityDamageEvent e = (EntityDamageEvent) event;
			if(center != null) {
				if(LocationUtil.getNearbyDamageableEntities(center, 10, 200).contains(e.getEntity())) {
					if(LocationUtil.isInCircle(e.getEntity().getLocation(), center, 20.0)) {
						ParticleLib.HEART.spawnParticle(e.getEntity().getLocation(), 5, 2, 2, 2);
						e.setCancelled(true);
					}
				}
			}
		}
	}

	@Override
	public void onRestrictClear() {}

}
