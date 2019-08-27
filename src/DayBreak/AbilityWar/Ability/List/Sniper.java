package DayBreak.AbilityWar.Ability.List;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Ability.AbilityManifest;
import DayBreak.AbilityWar.Ability.AbilityManifest.Rank;
import DayBreak.AbilityWar.Ability.AbilityManifest.Species;
import DayBreak.AbilityWar.Ability.SubscribeEvent;
import DayBreak.AbilityWar.Config.AbilitySettings.SettingObject;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame.Participant;
import DayBreak.AbilityWar.Utils.Library.EffectLib;
import DayBreak.AbilityWar.Utils.Library.SoundLib;
import DayBreak.AbilityWar.Utils.Thread.TimerBase;
import DayBreak.AbilityWar.Utils.VersionCompat.ServerVersion;

@AbilityManifest(Name = "스나이퍼", Rank = Rank.S, Species = Species.HUMAN)
public class Sniper extends AbilityBase {

	public static SettingObject<Integer> DurationConfig = new SettingObject<Integer>(Sniper.class, "Duration", 2, 
			"# 능력 지속시간") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}
		
	};
	
	public Sniper(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f스나이퍼가 쏘는 화살은 " + DurationConfig.getValue() + "초간 빠른 속도로 곧게 뻗어나가다 떨어집니다."));
	}

	private final int Duration = DurationConfig.getValue();
	
	private TimerBase Snipe = ServerVersion.getVersion() < 14 ?
	new TimerBase() {
		
		@Override
		protected void onStart() {}
		
		@Override
		protected void onEnd() {}
		
		@Override
		protected void TimerProcess(Integer Seconds) {
			Material main = getPlayer().getInventory().getItemInMainHand().getType();
			Material off = getPlayer().getInventory().getItemInMainHand().getType();
			if(main.equals(Material.BOW) || off.equals(Material.BOW)) {
				EffectLib.SLOW.addPotionEffect(getPlayer(), 5, 8, true);
				EffectLib.JUMP.addPotionEffect(getPlayer(), 5, 200, true);
			}
		}
	}.setPeriod(3)
	:
	new TimerBase() {
		
		@Override
		protected void onStart() {}
		
		@Override
		protected void onEnd() {}
		
		@Override
		protected void TimerProcess(Integer Seconds) {
			Material main = getPlayer().getInventory().getItemInMainHand().getType();
			Material off = getPlayer().getInventory().getItemInMainHand().getType();
			if(main.equals(Material.BOW) || off.equals(Material.BOW) || main.equals(Material.CROSSBOW) || off.equals(Material.CROSSBOW)) {
				EffectLib.SLOW.addPotionEffect(getPlayer(), 5, 8, true);
				EffectLib.JUMP.addPotionEffect(getPlayer(), 5, 200, true);
			}
		}
	}.setPeriod(3);
	
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		return false;
	}

	private List<Arrow> arrows = new ArrayList<Arrow>();
	
	@SubscribeEvent
	public void onProjectileLaunch(ProjectileLaunchEvent e) {
		if(e.getEntity().getShooter().equals(getPlayer())) {
			if(e.getEntity() instanceof Arrow) {
				Arrow a = (Arrow) e.getEntity();
				new TimerBase(Duration) {
					
					@Override
					protected void onStart() {
						a.setVelocity(a.getVelocity().multiply(2.5));
						a.setGlowing(true);
						a.setGravity(false);
						arrows.add(a);
					}
					
					@Override
					protected void onEnd() {
						a.setGlowing(false);
						a.setGravity(true);
						arrows.remove(a);
					}
					
					@Override
					protected void TimerProcess(Integer Seconds) {}
				}.StartTimer();
			}
		}
	}
	
	@SubscribeEvent
	public void onProjectileHit(ProjectileHitEvent e) {
		if(e.getHitEntity() != null && arrows.contains(e.getEntity())) {
			SoundLib.ENTITY_ARROW_HIT_PLAYER.playSound(getPlayer());
		}
	}
	
	@Override
	public void TargetSkill(MaterialType mt, LivingEntity entity) {}

	@Override
	protected void onRestrictClear() {
		Snipe.StartTimer();
	}

}
