package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.SubscribeEvent.Priority;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.FastMath;
import daybreak.abilitywar.utils.base.math.VectorUtil;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.ParticleLib;
import kotlin.ranges.RangesKt;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.util.Vector;

@AbilityManifest(name = "구울", rank = Rank.S, species = Species.UNDEAD, explain = {
		"BETA"
})
@Beta
public class Ghoul extends AbilityBase {

	private static class Component {

		private final RGB color;
		private final double radius, length;

		private Component(RGB color, double radius, double length) {
			this.color = color;
			this.radius = radius;
			this.length = length;
		}

	}

	private static final Component[] components = new Component[]{
			new Component(RGB.BLACK, 1.5, 1.05),
			new Component(RGB.RED, 1.5, 1)
	};

	private final Cooldown cooldown = new Cooldown(7, "할퀴기", 0);
	private boolean state = false;

	public Ghoul(Participant participant) {
		super(participant);
	}

	@SubscribeEvent(onlyRelevant = true, ignoreCancelled = true)
	private void onEntityRegainHealth(final EntityRegainHealthEvent e) {
		if (e.getRegainReason() == RegainReason.REGEN) {
			e.setCancelled(true);
		}
	}

	@SubscribeEvent(priority = Priority.HIGHEST, ignoreCancelled = true)
	private void onEntityDamageByEntity(final EntityDamageByEntityEvent e) {
		final Entity entity = e.getEntity();
		if (getPlayer().equals(e.getDamager()) && !cooldown.isRunning() && entity instanceof LivingEntity) {
			final LivingEntity livingEntity = (LivingEntity) entity;
			final double amount = e.getFinalDamage() / 2;
			final EntityRegainHealthEvent event = new EntityRegainHealthEvent(getPlayer(), amount, RegainReason.CUSTOM);
			Bukkit.getPluginManager().callEvent(event);
			if (!event.isCancelled()) {
				getPlayer().setHealth(RangesKt.coerceIn(getPlayer().getHealth() + event.getAmount(), 0, getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
			}
			new Cut(entity, state = !state).start();
			ParticleLib.BLOCK_CRACK.spawnParticle(livingEntity.getEyeLocation(), .3f, .3f, .3f, 30, MaterialX.REDSTONE_BLOCK);
			cooldown.start();
			if ((livingEntity.getHealth() / livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) <= .4) {
				e.setDamage(e.getDamage() / 2);
			}
		}
	}

	public class Cut extends AbilityTimer {

		private final Location base;
		private final double angle;

		private Cut(Entity target, boolean state) {
			super(state ? TaskType.NORMAL : TaskType.REVERSE, 3);
			setPeriod(TimeUnit.TICKS, 1);
			final Vector delta = target.getLocation().toVector().subtract(getPlayer().getLocation().toVector()).normalize();
			this.base = target.getLocation().clone().subtract(delta.multiply(1.6)).setDirection(delta.normalize()).add(0, 1, 0);
			this.angle = state ? 25 : -25;
		}

		@Override
		protected void run(int count) {
			final Vector direction = base.getDirection().setY(0).normalize(), axis = VectorUtil.rotateAroundAxisY(direction.clone(), 90);
			for (int i = 0; i < 8; i++) {
				final double radians = 0.78539816339744830961566084581988 + 0.06544984694978735913463840381832 * ((count - 1) * 8 + i);
				final double cos = FastMath.cos(radians), sin = FastMath.sin(radians);
				for (Component component : components) {
					final Location loc = base.clone().add(
							VectorUtil.rotateAroundAxis(
									VectorUtil.rotateAroundAxis(
											VectorUtil.rotateAroundAxisY(new Vector(cos, 0, component.length * sin), -base.getYaw()), direction, angle
									), axis, base.getPitch()
							).multiply(component.radius)
					).subtract(0, .75, 0);
					for (int y = 0; y < 3; y++) {
						ParticleLib.REDSTONE.spawnParticle(loc.add(0, .5, 0), component.color);
					}
				}
			}

		}

	}

}
