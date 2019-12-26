package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.event.AbilityRestrictionClearEvent;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.math.LocationUtil;
import daybreak.abilitywar.utils.math.geometry.Circle;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;

@AbilityManifest(Name = "쇼맨쉽", Rank = Rank.A, Species = Species.HUMAN)
public class ShowmanShip extends AbilityBase {

	public ShowmanShip(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f주변 10칸 이내에 있는 사람 수에 따라 효과를 받습니다."),
				ChatColor.translateAlternateColorCodes('&', "&a1명 이하 &7: &f나약함  &a2명 이상 &7: &f힘 II  &a3명 이상 &7: &f힘 III"));
	}

	private final RGB WEAK = new RGB(214, 255, 212);
	private final RGB POWER = new RGB(255, 184, 150);
	private final RGB POWERFUL = new RGB(255, 59, 59);
	
	private final Timer Passive = new Timer() {

		private final Circle circle = new Circle(getPlayer().getLocation(), 10).setAmount(100).setHighestLocation(true);
		
		@Override
		public void onStart() {}
		
		@Override
		public void onProcess(int count) {
			circle.setCenter(getPlayer().getLocation());
			final int Count = LocationUtil.getNearbyPlayers(getPlayer(), 10, 10).size();
			
			if(Count <= 1) {
				PotionEffects.WEAKNESS.addPotionEffect(getPlayer(), 20, 0, true);
				for(Location l : circle.getLocations()) {
					ParticleLib.REDSTONE.spawnParticle(getPlayer(), l, WEAK, 0);
				}
			} else if(Count == 2) {
				PotionEffects.INCREASE_DAMAGE.addPotionEffect(getPlayer(), 20, 1, true);
				for(Location l : circle.getLocations()) {
					ParticleLib.REDSTONE.spawnParticle(getPlayer(), l, POWER, 0);
				}
			} else {
				PotionEffects.INCREASE_DAMAGE.addPotionEffect(getPlayer(), 20, 2, true);
				for(Location l : circle.getLocations()) {
					ParticleLib.REDSTONE.spawnParticle(getPlayer(), l, POWERFUL, 0);
				}
			}
		}
		
		@Override
		public void onEnd() {}
		
	}.setPeriod(1);
	
	@Override
	public boolean ActiveSkill(Material materialType, ClickType ct) {
		return false;
	}

	@SubscribeEvent(onlyRelevant = true)
	public void onRestrictionClear(AbilityRestrictionClearEvent e) {
		Passive.startTimer();
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {}
	
}
