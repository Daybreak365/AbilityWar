package DayBreak.AbilityWar.Ability.List;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Ability.AbilityManifest;
import DayBreak.AbilityWar.Ability.AbilityManifest.Rank;
import DayBreak.AbilityWar.Ability.AbilityManifest.Species;
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
	
	TimerBase Snipe = new TimerBase() {
		
		@Override
		protected void onStart() {}
		
		@Override
		protected void onEnd() {}
		
		@Override
		protected void TimerProcess(Integer Seconds) {
			if(getPlayer().getInventory().getItemInMainHand().getType().equals(Material.BOW)
			|| getPlayer().getInventory().getItemInOffHand().getType().equals(Material.BOW)) {
				EffectLib.SLOW.addPotionEffect(getPlayer(), 7, 8, true);
				EffectLib.JUMP.addPotionEffect(getPlayer(), 7, 200, true);
			}
		}
	}.setPeriod(5);
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		return false;
	}

	List<Arrow> arrows = new ArrayList<Arrow>();
	
	@Override
	public void PassiveSkill(Event event) {
		if(event instanceof ProjectileLaunchEvent) {
			ProjectileLaunchEvent e = (ProjectileLaunchEvent) event;
			if(e.getEntity().getShooter().equals(getPlayer())) {
				if(e.getEntity() instanceof Arrow) {
					Arrow a = (Arrow) e.getEntity();
					new TimerBase(Duration) {
						
						@Override
						protected void onStart() {
							a.setVelocity(a.getVelocity().multiply(1.5));
							if(ServerVersion.getVersion() >= 9) a.setGlowing(true);
							a.setGravity(false);
							arrows.add(a);
						}
						
						@Override
						protected void onEnd() {
							if(ServerVersion.getVersion() >= 9) a.setGlowing(false);
							a.setGravity(true);
							arrows.remove(a);
						}
						
						@Override
						protected void TimerProcess(Integer Seconds) {}
					}.StartTimer();
				}
			}
		} else if(event instanceof ProjectileHitEvent) {
			ProjectileHitEvent e = (ProjectileHitEvent) event;
			if(e.getHitEntity() != null && arrows.contains(e.getEntity())) {
				SoundLib.ENTITY_ARROW_HIT_PLAYER.playSound(getPlayer());
			}
		}
	}

	@Override
	public void TargetSkill(MaterialType mt, Entity entity) {}

	@Override
	protected void onRestrictClear() {
		Snipe.StartTimer();
	}

}
