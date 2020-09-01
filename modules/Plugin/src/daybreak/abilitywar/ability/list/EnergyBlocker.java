package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AbilityManifest(name = "에너지 블로커", rank = Rank.A, species = Species.HUMAN, explain = {
		"원거리 피해를 3분의 1로, 근거리 피해를 두 배로 받거나",
		"원거리 피해를 두 배로, 근거리 피해를 3분의 1로 받을 수 있습니다.",
		"철괴를 우클릭하면 각각의 피해 정도를 뒤바꿉니다. 피해 정도를 변경한 이후",
		"한 번 이상 공격을 받아야만 다시 피해 정도를 뒤바꿀 수 있습니다.",
		"$[PARTICLE_NOTICE]"
})
public class EnergyBlocker extends AbilityBase implements ActiveHandler {

	private boolean projectileBlocking = true;

	public EnergyBlocker(Participant participant) {
		super(participant);
	}

	private final ActionbarChannel actionbarChannel = newActionbarChannel();

	private boolean canChange = true;

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK) {
			if (canChange) {
				projectileBlocking = !projectileBlocking;
				getPlayer().sendMessage(getState() + "§f으로 변경되었습니다.");
				actionbarChannel.update(getState());
				canChange = false;
			} else getPlayer().sendMessage("§f[§c!§f] 한 번 이상 공격을 받아야만 §a피해 §b정도§f를 뒤바꿀 수 있습니다.");
		}
		return false;
	}

	private final boolean particleShowState = new Random().nextBoolean();

	private static final RGB LONG_DISTANCE = RGB.of(82, 108, 179), SHORT_DISTANCE = RGB.of(130, 255, 147);
	private final Object PARTICLE_NOTICE = new Object() {
		@Override
		public String toString() {
			return "현재 " + (particleShowState ? "취약한 공격" : "방어 중인 공격") + "이 머리 위에 파티클로 뜹니다.";
		}
	};

	private final AbilityTimer particle = (particleShowState ? new AbilityTimer() {

		@Override
		public void run(int count) {
			if (projectileBlocking) {
				ParticleLib.REDSTONE.spawnParticle(getPlayer().getLocation().add(0, 2.2, 0), LONG_DISTANCE);
			} else {
				ParticleLib.REDSTONE.spawnParticle(getPlayer().getLocation().add(0, 2.2, 0), SHORT_DISTANCE);
			}
		}

	} : new AbilityTimer() {

		@Override
		public void run(int count) {
			if (projectileBlocking) {
				ParticleLib.REDSTONE.spawnParticle(getPlayer().getEyeLocation().add(0, 0.5, 0), SHORT_DISTANCE);
			} else {
				ParticleLib.REDSTONE.spawnParticle(getPlayer().getLocation().add(0, 2.2, 0), LONG_DISTANCE);
			}
		}

	}).setPeriod(TimeUnit.TICKS, 1).register();

	public String getState() {
		return projectileBlocking ? "§b원거리 §f보호 §8(§a근거리 §f취약§8)" : "§a근거리 §f보호 §8(§b원거리 §f취약§8)";
	}

	@SubscribeEvent
	private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.getEntity().equals(getPlayer())) {
			final Entity damager = getDamager(e.getDamager());
			if (getPlayer().equals(damager)) return;
			if (e.getCause() == DamageCause.PROJECTILE) {
				e.setDamage(projectileBlocking ? e.getDamage() / 3 : (e.getDamage() * 2));
				this.canChange = true;
			} else if (e.getCause() == DamageCause.ENTITY_ATTACK) {
				e.setDamage(projectileBlocking ? e.getDamage() * 2 : (e.getDamage() / 3));
				this.canChange = true;
			}
		}
	}

	@Nullable
	private static Entity getDamager(final Entity damager) {
		if (damager instanceof Projectile) {
			final ProjectileSource shooter = ((Projectile) damager).getShooter();
			return shooter instanceof Entity ? (Entity) shooter : null;
		} else return damager;
	}

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			actionbarChannel.update(getState());
			particle.start();
		}
	}

}
