package Marlang.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.Timer.CooldownTimer;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Library.ParticleLib;
import Marlang.AbilityWar.Utils.Math.LocationUtil;
import Marlang.AbilityWar.Utils.Thread.TimerBase;
import Marlang.AbilityWar.Utils.VersionCompat.PlayerCompat;
import Marlang.AbilityWar.Utils.VersionCompat.PotionEffectType;

public class Flora extends AbilityBase {

	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>("플로라", "Cooldown", 10, 
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};
	
	public Flora(Player player) {
		super(player, "플로라", Rank.GOD,
				ChatColor.translateAlternateColorCodes('&', "&f꽃과 풍요의 여신."),
				ChatColor.translateAlternateColorCodes('&', "&f주변에 있는 모든 플레이어에게 재생 효과를 주거나 신속 효과를 줍니다."),
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 효과를 뒤바꿉니다. " + Messager.formatCooldown(CooldownConfig.getValue())));
	}
	
	EffectType type = EffectType.Speed;
	
	TimerBase Passive = new TimerBase() {
		
		Location center;
		
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
					PlayerCompat.addPotionEffect(p, type.getPotionEffect(), 40, 1, true);
				}
			}
		}
		
		@Override
		public void onEnd() {}
		
	}.setPeriod(1);
	
	CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());
	
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
	public void PassiveSkill(Event event) {}

	@Override
	public void onRestrictClear() {
		Passive.StartTimer();
	}

	private enum EffectType {
		
		Regeneration(PotionEffectType.REGENERATION, ChatColor.translateAlternateColorCodes('&', "&c재생")),
		Speed(PotionEffectType.SPEED, ChatColor.translateAlternateColorCodes('&', "&b신속"));
		
		PotionEffectType potionEffect;
		String name;
		
		private EffectType(PotionEffectType potionEffect, String name) {
			this.potionEffect = potionEffect;
			this.name = name;
		}

		public PotionEffectType getPotionEffect() {
			return potionEffect;
		}

		public String getName() {
			return name;
		}
		
	}

	@Override
	public void TargetSkill(MaterialType mt, Entity entity) {}
	
}
