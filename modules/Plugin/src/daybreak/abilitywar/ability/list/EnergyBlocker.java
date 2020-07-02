package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.Scheduled;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

@AbilityManifest(name = "에너지 블로커", rank = Rank.A, species = Species.HUMAN, explain = {
		"원거리 공격 피해를 1/3로, 근거리 공격 피해를 두 배로 받거나",
		"원거리 공격 피해를 두 배로, 근거리 공격 피해를 1/3로 받을 수 있습니다.",
		"철괴를 우클릭하면 각각의 피해 정도를 뒤바꿉니다.",
		"철괴를 좌클릭하면 현재 상태를 확인할 수 있습니다.",
		"$[PARTICLE_NOTICE]"
})
public class EnergyBlocker extends AbilityBase implements ActiveHandler {

	private boolean projectileBlocking = true;

	public EnergyBlocker(Participant participant) {
		super(participant);
	}

	private final ActionbarChannel actionbarChannel = newActionbarChannel();

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT)) {
			if (clickType.equals(ClickType.RIGHT_CLICK)) {
				projectileBlocking = !projectileBlocking;
				getPlayer().sendMessage(getState() + "로 변경되었습니다.");
				actionbarChannel.update(getState());
			} else if (clickType.equals(ClickType.LEFT_CLICK)) {
				getPlayer().sendMessage("§6현재 상태§f: " + getState());
			}
		}

		return false;
	}

	private final boolean particleShowState = new Random().nextBoolean();

	private static final RGB LONG_DISTANCE = RGB.of(116, 237, 167);
	private static final RGB SHORT_DISTANCE = RGB.of(85, 237, 242);
	private final Object PARTICLE_NOTICE = new Object() {
		@Override
		public String toString() {
			return "현재 " + (particleShowState ? "취약한 공격" : "방어 중인 공격") + "이 머리 위에 파티클로 뜹니다.";
		}
	};
	@Scheduled
	private final Timer particle = particleShowState ? new Timer() {

		@Override
		public void run(int count) {
			if (projectileBlocking) {
				ParticleLib.REDSTONE.spawnParticle(getPlayer().getLocation().add(0, 2.2, 0), LONG_DISTANCE);
			} else {
				ParticleLib.REDSTONE.spawnParticle(getPlayer().getLocation().add(0, 2.2, 0), SHORT_DISTANCE);
			}
		}

	}.setPeriod(TimeUnit.TICKS, 1) : new Timer() {

		@Override
		public void run(int count) {
			if (projectileBlocking) {
				ParticleLib.REDSTONE.spawnParticle(getPlayer().getEyeLocation().add(0, 0.5, 0), SHORT_DISTANCE);
			} else {
				ParticleLib.REDSTONE.spawnParticle(getPlayer().getLocation().add(0, 2.2, 0), LONG_DISTANCE);
			}
		}

	}.setPeriod(TimeUnit.TICKS, 1);

	public String getState() {
		return projectileBlocking ? "§b원거리 §f1/3 배§7, §a근거리 §f두 배" : "§b원거리 §f두 배§7, §a근거리 §f1/3 배";
	}

	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.getEntity().equals(getPlayer())) {
			DamageCause cause = e.getCause();
			if (cause.equals(DamageCause.PROJECTILE)) {
				if (projectileBlocking) {
					e.setDamage(e.getDamage() / 3);
				} else {
					e.setDamage(e.getDamage() * 2);
				}
			} else if (cause.equals(DamageCause.ENTITY_ATTACK)) {
				if (projectileBlocking) {
					e.setDamage(e.getDamage() * 2);
				} else {
					e.setDamage(e.getDamage() / 3);
				}
			}
		}
	}

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			actionbarChannel.update(getState());
		}
	}

}
