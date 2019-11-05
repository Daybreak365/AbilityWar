package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.timer.CooldownTimer;
import daybreak.abilitywar.config.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.library.EffectLib;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.math.LocationUtil;
import daybreak.abilitywar.utils.math.geometry.Circle;
import daybreak.abilitywar.utils.thread.TimerBase;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

@AbilityManifest(Name = "플로라", Rank = Rank.C, Species = Species.GOD)
public class Flora extends AbilityBase {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Flora.class, "Cooldown", 3,
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
	
	private final TimerBase Passive = new TimerBase() {
		
		private Location center;
		
		@Override
		public void onStart() {}
		
		private final Circle circle = new Circle(getPlayer().getLocation(), 6).setAmount(20).setHighestLocation(true);
		
		@Override
		public void onProcess(int count) {
			center = getPlayer().getLocation();
			for(Location l : circle.setCenter(center).getLocations()) {
				ParticleLib.SPELL.spawnParticle(l.subtract(0, 1, 0), 0, 0, 0, 1);
			}
			
			for(Player p : LocationUtil.getNearbyPlayers(center, 6, 200)) {
				if(LocationUtil.isInCircle(center, p.getLocation(), 6.0, true)) {
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
	
	private final CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		if(mt.equals(MaterialType.IRON_INGOT)) {
			if(ct.equals(ClickType.RIGHT_CLICK)) {
				if(!Cool.isCooldown()) {
					Player p = getPlayer();
					if(type.equals(EffectType.Speed)) {
						type = EffectType.Regeneration;
					} else {
						type = EffectType.Speed;
					}
					
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', type.getName() + "&f으로 변경되었습니다."));
					
					Cool.startTimer();
				}
			} else if(ct.equals(ClickType.LEFT_CLICK)) {
				getPlayer().sendMessage( ChatColor.translateAlternateColorCodes('&', "&6현재 상태&f: " + type.getName()));
			}
		}
		
		return false;
	}

	@Override
	public void onRestrictClear() {
		Passive.startTimer();
	}

	private enum EffectType {
		
		Regeneration(ChatColor.translateAlternateColorCodes('&', "&c재생")),
		Speed(ChatColor.translateAlternateColorCodes('&', "&b신속"));
		
		final String name;
		
		EffectType(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
		
	}

	@Override
	public void TargetSkill(MaterialType mt, LivingEntity entity) {}
	
}
