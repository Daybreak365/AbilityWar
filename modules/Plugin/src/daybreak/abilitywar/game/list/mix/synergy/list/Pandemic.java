package daybreak.abilitywar.game.list.mix.synergy.list;

import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.list.virus.Virus;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.event.participant.ParticipantDeathEvent;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer.TaskType;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.geometry.Sphere;
import daybreak.abilitywar.utils.library.ParticleLib;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.function.Predicate;

@AbilityManifest(name = "팬데믹", rank = Rank.A, species = Species.OTHERS, explain = {
		"치명적인 피해를 입은 마지막 순간에 큰 폭발을 일으키고 사망합니다.",
		"폭발시 주변에 있었던 모든 플레이어가 바이러스에 감염됩니다."
})
public class Pandemic extends Synergy {

	public static final SettingObject<Integer> SizeConfig = synergySettings.new SettingObject<Integer>(Pandemic.class, "size", 10,
			"# 팬데믹이 사망할 때 일어날 폭발의 크기",
			"# 바이러스 감염 범위") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	private final Predicate<Entity> predicate = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			if (entity.equals(getPlayer())) return false;
			if (entity instanceof Player) {
				if (!getGame().isParticipating(entity.getUniqueId())
						|| (getGame() instanceof DeathManager.Handler && ((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
						|| !getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue()) {
					return false;
				}
				if (getGame() instanceof Teamable) {
					final Teamable teamGame = (Teamable) getGame();
					final Participant entityParticipant = teamGame.getParticipant(entity.getUniqueId()), participant = getParticipant();
					return !teamGame.hasTeam(entityParticipant) || !teamGame.hasTeam(participant) || (!teamGame.getTeam(entityParticipant).equals(teamGame.getTeam(participant)));
				}
			}
			return true;
		}
	};

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
			for (Player player : LocationUtil.getNearbyEntities(Player.class, getPlayer().getLocation(), size, size, predicate)) {
				if (getGame().isParticipating(player)) {
					try {
						getGame().getParticipant(player).setAbility(Virus.class);
					} catch (ReflectiveOperationException ignored) {
					}
				}
			}
		}

		@Override
		public void onEnd() {
		}

	}.setPeriod(TimeUnit.TICKS, 1);

	public Pandemic(Participant participant) {
		super(participant);
	}

	@SubscribeEvent
	private void onPlayerDeath(ParticipantDeathEvent e) {
		if (e.getParticipant().equals(getParticipant())) {
			this.center = e.getPlayer().getLocation();
			explosion.start();
		}
	}

}
