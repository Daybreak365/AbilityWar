package DayBreak.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;

import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Ability.AbilityManifest;
import DayBreak.AbilityWar.Ability.AbilityManifest.Rank;
import DayBreak.AbilityWar.Ability.AbilityManifest.Species;
import DayBreak.AbilityWar.Ability.Timer.CooldownTimer;
import DayBreak.AbilityWar.Ability.Timer.DurationTimer;
import DayBreak.AbilityWar.Config.AbilitySettings.SettingObject;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame.Participant;
import DayBreak.AbilityWar.Utils.Messager;
import DayBreak.AbilityWar.Utils.Library.EffectLib;
import DayBreak.AbilityWar.Utils.Library.ParticleLib;
import DayBreak.AbilityWar.Utils.Library.SoundLib;
import DayBreak.AbilityWar.Utils.Math.LocationUtil;

@AbilityManifest(Name = "뮤즈", Rank = Rank.S, Species = Species.OTHERS)
public class Muse extends AbilityBase {

	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Muse.class, "Cooldown", 80, 
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};
	
	public Muse(Participant participant) {
		super(participant,
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
					if(LocationUtil.isInCircle(center, p.getLocation(), 10.0)) {
						EffectLib.GLOWING.addPotionEffect(p, 4, 0, true);
						
						if(SoundCount % 5 == 0) {
							SoundCount = 1;
							
							SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(p);
						}
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
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		if(mt.equals(MaterialType.Iron_Ingot)) {
			if(ct.equals(ClickType.RightClick)) {
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
					if(LocationUtil.isInCircle(center, e.getEntity().getLocation(), 10.0)) {
						ParticleLib.HEART.spawnParticle(e.getEntity().getLocation(), 5, 2, 2, 2);
						e.setCancelled(true);
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
