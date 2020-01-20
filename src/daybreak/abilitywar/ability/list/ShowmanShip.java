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

@AbilityManifest(Name = "쇼맨쉽", Rank = Rank.B, Species = Species.HUMAN)
public class ShowmanShip extends AbilityBase {

	public ShowmanShip(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f주변 10칸 이내에 있는 사람 수에 따라 효과를 받습니다."),
				ChatColor.translateAlternateColorCodes('&', "&a1명 이하 &7: &f나약함  &a2명 이상 &7: &f힘 II  &a3명 이상 &7: &f힘 III"));
	}

	private final RGB WEAK = new RGB(214, 255, 212);
	private final RGB POWER = new RGB(255, 184, 150);
	private final RGB POWERFUL = new RGB(255, 59, 59);
	private final Circle circle = Circle.of(10, 100);

	private final Timer Passive = new Timer() {

		@Override
		public void onStart() {
		}

		@Override
		public void onProcess(int count) {
			final int players = LocationUtil.getNearbyPlayers(getPlayer(), 10, 10).size();

			final RGB color;
			if (players <= 1) {
				PotionEffects.WEAKNESS.addPotionEffect(getPlayer(), 20, 0, true);
				color = WEAK;
			} else if (players == 2) {
				PotionEffects.INCREASE_DAMAGE.addPotionEffect(getPlayer(), 20, 1, true);
				color = POWER;
			} else {
				PotionEffects.INCREASE_DAMAGE.addPotionEffect(getPlayer(), 20, 2, true);
				color = POWERFUL;
			}

			for (Location l : circle.toLocations(getPlayer().getLocation()).floor(getPlayer().getLocation().getY())) {
				ParticleLib.REDSTONE.spawnParticle(getPlayer(), l, color);
			}
		}

		@Override
		public void onEnd() {
		}

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
	public void TargetSkill(Material materialType, LivingEntity entity) {
	}

}
