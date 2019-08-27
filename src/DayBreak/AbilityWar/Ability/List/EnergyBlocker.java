package DayBreak.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
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
import DayBreak.AbilityWar.Utils.Library.ParticleLib.RGB;
import DayBreak.AbilityWar.Utils.Thread.TimerBase;

@AbilityManifest(Name = "에너지 블로커", Rank = Rank.A, Species = Species.HUMAN)
public class EnergyBlocker extends AbilityBase {

	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(EnergyBlocker.class, "Cooldown", 3, 
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};
	
	private boolean Default = true;
	
	public EnergyBlocker(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f원거리 공격 피해를 1/3로, 근거리 공격 피해를 두 배로 받거나"),
				ChatColor.translateAlternateColorCodes('&', "&f원거리 공격 피해를 두 배로, 근거리 공격 피해를 1/3로 받을 수 있습니다."),
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 각각의 피해 정도를 뒤바꿉니다. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 좌클릭하면 현재 상태를 확인할 수 있습니다."));
	}
	
	private CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		if(mt.equals(MaterialType.Iron_Ingot)) {
			if(ct.equals(ClickType.RightClick)) {
				if(!Cool.isCooldown()) {
					Default = !Default;
					Player p = getPlayer();
					if(Default) {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b원거리 &f1/3&7, &a근거리 &f두 배로 변경되었습니다."));
					} else {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b원거리 &f두 배&7, &a근거리 &f1/3로 변경되었습니다."));
					}
					
					Cool.StartTimer();
				}
			} else if(ct.equals(ClickType.LeftClick)) {
				if(Default) {
					Messager.sendMessage(getPlayer(), ChatColor.translateAlternateColorCodes('&', "&6현재 상태&f: &b원거리 &f1/3&7, &a근거리 &f두 배"));
				} else {
					Messager.sendMessage(getPlayer(), ChatColor.translateAlternateColorCodes('&', "&6현재 상태&f: &b원거리 &f두 배&7, &a근거리 &f1/3"));
				}
			}
		}
		
		return false;
	}

	private TimerBase Particle = new TimerBase() {

		@Override
		public void onStart() {}
		
		@Override
		public void TimerProcess(Integer Seconds) {
			if(Default) {
				ParticleLib.REDSTONE.spawnParticle(getPlayer().getLocation().add(0, 2.2, 0), new RGB(116, 237, 167), 0);
			} else {
				ParticleLib.REDSTONE.spawnParticle(getPlayer().getLocation().add(0, 2.2, 0), new RGB(85, 237, 242), 0);
			}
		}
		
		@Override
		public void onEnd() {}
		
	}.setPeriod(1);
	
	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if(e.getEntity().equals(getPlayer())) {
			DamageCause dc = e.getCause();
			if(dc != null) {
				if(dc.equals(DamageCause.PROJECTILE)) {
					if(Default) {
						e.setDamage(e.getDamage() / 3);
					} else {
						e.setDamage(e.getDamage() * 2);
					}
				} else if(dc.equals(DamageCause.ENTITY_ATTACK)) {
					if(Default) {
						e.setDamage(e.getDamage() * 2);
					} else {
						e.setDamage(e.getDamage() / 3);
					}
				}
			}
		}
	}
	
	@Override
	public void onRestrictClear() {
		Particle.StartTimer();
	}

	@Override
	public void TargetSkill(MaterialType mt, LivingEntity entity) {}
	
}
