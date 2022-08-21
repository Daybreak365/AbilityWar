package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.Tips;
import daybreak.abilitywar.ability.Tips.Description;
import daybreak.abilitywar.ability.Tips.Difficulty;
import daybreak.abilitywar.ability.Tips.Level;
import daybreak.abilitywar.ability.Tips.Stats;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.ParticleLib;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

@AbilityManifest(name = "에너지 블로커", rank = Rank.A, species = Species.HUMAN, explain = {
		"원거리 피해를 3분의 1로, 근거리 피해를 두 배로 받거나",
		"원거리 피해를 두 배로, 근거리 피해를 3분의 1로 받을 수 있습니다.",
		"철괴를 우클릭하면 각각의 피해 정도를 뒤바꿉니다. 피해 정도를 변경한 이후",
		"한 번 이상 공격을 받아야만 다시 피해 정도를 뒤바꿀 수 있습니다.",
		"$[PARTICLE_NOTICE]"
}, summarize = {
		"§a근거리§f나 §b원거리§f 피해를 §e1§7/§e3§f배로 받고, 그 반대는 §c2§f배로 받습니다.",
		"피해를 입으면 §7철괴 우클릭§f으로 §a근§7/§b원§f거리 피해 배수를 서로 바꿉니다."
})
@Tips(tip = {
		"근접 공격과 원거리 공격을 번갈아 가며 방어할 수 있지만,",
		"현재 방어 중이지 않은 공격은 큰 대미지를 받을 수 있으니",
		"타이밍을 잘 생각해서 변경하세요. 내가 현재 방어중인 공격의",
		"색이 머리 위에 뜨는지, 내가 현재 취약한 공격의 색이 머리 위에",
		"뜨는지 상대는 모르기 때문에, 심리전 또한 중요합니다. 공격을",
		"방어했을 때, 마치 내가 큰 대미지를 받은 것처럼 연기해보세요!"
}, strong = {
		@Description(subject = "대미지 감소", explain = {
				"대미지를 무려 3분의 1로 줄여받을 수 있습니다. 예를 들어 날카로움 V",
				"다이아 검의 대미지를 방어하면, 마법 부여가 되지 않은 나무 검으로",
				"공격받는 것과 같습니다."
		})
}, weak = {
		@Description(subject = "대미지 증가", explain = {
				"대미지를 무려 2배로 늘려받습니다. 예를 들어 마법 부여가 되지 않은",
				"돌 검으로 공격받으면, 날카로움 V 다이아 검으로 공격받는 것과 같습니다."
		})
}, stats = @Stats(offense = Level.ZERO, survival = Level.FIVE, crowdControl = Level.ZERO, mobility = Level.ZERO, utility = Level.ZERO), difficulty = Difficulty.VERY_HARD)
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
