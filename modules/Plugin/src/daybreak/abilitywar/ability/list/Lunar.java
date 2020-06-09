package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.FastMath;
import daybreak.abilitywar.utils.base.math.VectorUtil;
import daybreak.abilitywar.utils.base.math.VectorUtil.Vectors;
import daybreak.abilitywar.utils.base.math.geometry.Crescent;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

@Beta
@AbilityManifest(name = "루나", rank = Rank.A, species = Species.OTHERS, explain = {
		"BETA"
})
public class Lunar extends AbilityBase implements ActiveHandler {

	private static final RGB MOONLIGHT_COLOUR = RGB.of(235, 200, 21);
	private final Crescent crescent = Crescent.of(1, 20);
	private boolean particleSide = true;

	public Lunar(Participant participant) {
		super(participant);
	}

	private void updateTime(World world) {
		final long diff = 15000 - world.getTime();
		if (diff < 0) {
			if (-diff > 1000) world.setTime(world.getTime() - 1000);
			else world.setTime(world.getTime() + diff);
		} else if (diff > 0) {
			if (diff > 1000) world.setTime(world.getTime() + 1000);
			else world.setTime(world.getTime() + diff);
		}
	}

	@SubscribeEvent
	private void onAttack(EntityDamageByEntityEvent e) {
		if (getPlayer().equals(e.getDamager()) && e.getEntity() instanceof LivingEntity) {
			LivingEntity entity = (LivingEntity) e.getEntity();
			new CutParticle(particleSide ? 45 : -45).start();
			updateTime(getPlayer().getWorld());
			SoundLib.ENTITY_PLAYER_ATTACK_SWEEP.playSound(getPlayer());
			particleSide = !particleSide;
		}
	}

	@Override
	public boolean ActiveSkill(Material material, ClickType clickType) {
		return material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK;
	}

	private class CutParticle extends Timer {

		private final Vector axis;
		private final Vector vector;
		private final Vectors crescentVectors;

		private CutParticle(double angle) {
			super(4);
			setPeriod(TimeUnit.TICKS, 1);
			this.axis = VectorUtil.rotateAroundAxis(VectorUtil.rotateAroundAxisY(getPlayer().getLocation().getDirection().setY(0).normalize(), 90), getPlayer().getLocation().getDirection().setY(0).normalize(), angle);
			this.vector = getPlayer().getLocation().getDirection().setY(0).normalize().multiply(0.5);
			this.crescentVectors = crescent.clone()
					.rotateAroundAxisY(-getPlayer().getLocation().getYaw())
					.rotateAroundAxis(getPlayer().getLocation().getDirection().setY(0).normalize(), (180 - angle) % 180)
					.rotateAroundAxis(axis, -75);
		}

		@Override
		protected void run(int count) {
			Location baseLoc = getPlayer().getLocation().clone().add(vector).add(0, 1.3, 0);
			for (Location loc : crescentVectors.toLocations(baseLoc)) {
				ParticleLib.REDSTONE.spawnParticle(loc, MOONLIGHT_COLOUR);
			}
			crescentVectors.rotateAroundAxis(axis, 40);
		}

	}

	private class LunarSkill extends Timer {

		private final Location baseLocation;
		private double radius, yOffset, rotateAngle = 0;

		private LunarSkill(double radius) {
			super((int) (radius / 0.1));
			setPeriod(TimeUnit.TICKS, 1);
			this.baseLocation = getPlayer().getLocation().clone();
			this.radius = radius;
		}

		@Override
		protected void run(int count) {
			final double divided = 6.283185307179586 / 8;
			for (int i = 1; i <= 8; i++) {
				final double radians = divided * i;
				ParticleLib.REDSTONE.spawnParticle(baseLocation.clone().add(VectorUtil.rotateAroundAxisY(new Vector(FastMath.cos(radians) * radius, yOffset, FastMath.sin(radians) * radius), rotateAngle)), MOONLIGHT_COLOUR);
			}
			radius = Math.max(.1, radius - .1);
			yOffset += .1;
			rotateAngle += 5;
		}
	}

}
