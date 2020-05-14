package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.event.participant.ParticipantDeathEvent;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer.TaskType;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.geometry.Sphere;
import daybreak.abilitywar.utils.library.ParticleLib;
import org.bukkit.Location;

@AbilityManifest(name = "초신성", rank = Rank.B, species = Species.OTHERS, explain = {
		"치명적인 피해를 입은 마지막 순간에 큰 폭발을 일으키고 사망합니다."
})
public class SuperNova extends AbilityBase {

	public static final SettingObject<Integer> SizeConfig = abilitySettings.new SettingObject<Integer>(SuperNova.class, "Size", 10,
			"# 초신성이 사망할 때 일어날 폭발의 크기") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	public SuperNova(Participant participant) {
		super(participant);
	}

	private final int size = SizeConfig.getValue();

	private Location center;
	private final SimpleTimer explosion = new SimpleTimer(TaskType.REVERSE, size) {

		@Override
		public void run(int seconds) {
			double count = ((size + 1) - seconds) / 1.2;
			for (Location location : Sphere.of(count, 5).toLocations(center)) {
				location.getWorld().createExplosion(location, 2);
				ParticleLib.SPELL.spawnParticle(location, 0, 0, 0, 1);
			}
		}

		@Override
		public void onEnd() {
		}

	}.setPeriod(TimeUnit.TICKS, 1);

	@SubscribeEvent
	public void onPlayerDeath(ParticipantDeathEvent e) {
		if (e.getParticipant().equals(getParticipant())) {
			this.center = e.getPlayer().getLocation();
			explosion.start();
		}
	}

}
