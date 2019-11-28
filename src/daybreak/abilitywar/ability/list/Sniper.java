package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.versioncompat.ServerVersion;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

@AbilityManifest(Name = "스나이퍼", Rank = Rank.S, Species = Species.HUMAN)
public class Sniper extends AbilityBase {

	public static final SettingObject<Integer> DurationConfig = new SettingObject<Integer>(Sniper.class, "Duration", 2,
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
	
	private final Timer Snipe = ServerVersion.getVersion() < 14 ?
	new Timer() {
		
		@Override
		protected void onStart() {}
		
		@Override
		protected void onEnd() {}
		
		@Override
		protected void onProcess(int count) {
			Material main = getPlayer().getInventory().getItemInMainHand().getType();
			Material off = getPlayer().getInventory().getItemInOffHand().getType();
			if(main.equals(Material.BOW) || off.equals(Material.BOW)) {
				PotionEffects.SLOW.addPotionEffect(getPlayer(), 5, 8, true);
				PotionEffects.JUMP.addPotionEffect(getPlayer(), 5, 200, true);
			}
		}
	}.setPeriod(3)
	:
	new Timer() {
		
		@Override
		protected void onStart() {}
		
		@Override
		protected void onEnd() {}
		
		@Override
		protected void onProcess(int count) {
			Material main = getPlayer().getInventory().getItemInMainHand().getType();
			Material off = getPlayer().getInventory().getItemInOffHand().getType();
			if(main.equals(Material.BOW) || off.equals(Material.BOW) || main.equals(Material.CROSSBOW) || off.equals(Material.CROSSBOW)) {
				PotionEffects.SLOW.addPotionEffect(getPlayer(), 5, 8, true);
				PotionEffects.JUMP.addPotionEffect(getPlayer(), 5, 200, true);
			}
		}
	}.setPeriod(3);
	
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		return false;
	}

	private final ArrayList<Arrow> arrows = new ArrayList<>();
	
	@SubscribeEvent
	public void onProjectileLaunch(ProjectileLaunchEvent e) {
		if(e.getEntity().getShooter().equals(getPlayer())) {
			if(e.getEntity() instanceof Arrow) {
				Arrow a = (Arrow) e.getEntity();
				new Timer(Duration) {
					
					@Override
					protected void onStart() {
						a.setVelocity(a.getVelocity().multiply(1.5));
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
					protected void onProcess(int count) {}
				}.startTimer();
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
		Snipe.startTimer();
	}

}
