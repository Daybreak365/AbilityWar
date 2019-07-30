package DayBreak.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Ability.AbilityManifest;
import DayBreak.AbilityWar.Ability.AbilityManifest.Rank;
import DayBreak.AbilityWar.Ability.AbilityManifest.Species;
import DayBreak.AbilityWar.Ability.Timer.CooldownTimer;
import DayBreak.AbilityWar.Config.AbilitySettings.SettingObject;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame.Participant;
import DayBreak.AbilityWar.Utils.Messager;
import DayBreak.AbilityWar.Utils.Library.EffectLib;
import DayBreak.AbilityWar.Utils.Library.ParticleLib;
import DayBreak.AbilityWar.Utils.Math.LocationUtil;
import DayBreak.AbilityWar.Utils.Thread.TimerBase;

@AbilityManifest(Name = "플로라", Rank = Rank.C, Species = Species.GOD)
public class Flora extends AbilityBase {

	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Flora.class, "Cooldown", 3, 
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};
	
	public Flora(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f꽃과 풍요의 여신."),
				ChatColor.translateAlternateColorCodes('&', "&f주변에 있는 모든 플레이어에게 재생 효과를 주거나 신속 효과를 줍니다."),
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 효과를 뒤바꿉니다. " + Messager.formatCooldown(CooldownConfig.getValue())));
	}
	
	private EffectType type = EffectType.Speed;
	
	private TimerBase Passive = new TimerBase() {
		
		private Location center;
		
		@Override
		public void onStart() {}
		
		@Override
		public void TimerProcess(Integer Seconds) {
			center = getPlayer().getLocation();
			for(Location l : LocationUtil.getCircle(center, 6, 20, true)) {
				ParticleLib.SPELL.spawnParticle(l.subtract(0, 1, 0), 1, 0, 0, 0);
			}
			
			for(Player p : LocationUtil.getNearbyPlayers(center, 6, 200)) {
				if(LocationUtil.isInCircle(center, p.getLocation(), 6.0)) {
					if(type.equals(EffectType.Speed)) {
						EffectLib.SPEED.addPotionEffect(p, 40, 1, true);
					} else if(type.equals(EffectType.Regeneration)) {
						EffectLib.REGENERATION.addPotionEffect(p, 100, 0, false);
					}
				}
			}
		}
		
		@Override
		public void onEnd() {}
		
	}.setPeriod(1);
	
	private CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		if(mt.equals(MaterialType.Iron_Ingot)) {
			if(ct.equals(ClickType.RightClick)) {
				if(!Cool.isCooldown()) {
					Player p = getPlayer();
					if(type.equals(EffectType.Speed)) {
						type = EffectType.Regeneration;
					} else {
						type = EffectType.Speed;
					}
					
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', type.getName() + "&f으로 변경되었습니다."));
					
					Cool.StartTimer();
				}
			} else if(ct.equals(ClickType.LeftClick)) {
				Messager.sendMessage(getPlayer(), ChatColor.translateAlternateColorCodes('&', "&6현재 상태&f: " + type.getName()));
			}
		}
		
		return false;
	}

	@Override
	public void onRestrictClear() {
		Passive.StartTimer();
	}

	private enum EffectType {
		
		Regeneration(ChatColor.translateAlternateColorCodes('&', "&c재생")),
		Speed(ChatColor.translateAlternateColorCodes('&', "&b신속"));
		
		String name;
		
		private EffectType(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
		
	}

	@Override
	public void TargetSkill(MaterialType mt, Entity entity) {}
	
}
