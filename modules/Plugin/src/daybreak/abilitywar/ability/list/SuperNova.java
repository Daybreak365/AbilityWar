package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.event.participant.ParticipantDeathEvent;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer.TaskType;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.geometry.Sphere;
import daybreak.abilitywar.utils.base.math.geometry.location.LocationIterator;
import daybreak.abilitywar.utils.base.minecraft.damage.Damages;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.function.Predicate;

@AbilityManifest(name = "초신성", rank = Rank.B, species = Species.OTHERS, explain = {
		"치명적인 피해를 입은 마지막 순간에 큰 폭발을 일으킵니다.",
		"폭발은 적에게 최대 체력에 비례하는 피해를 입힙니다."
}, summarize = {
		"치명적인 피해를 받으면 §c거대한 폭발§f을 일으킵니다."
})
public class SuperNova extends AbilityBase {

	public static final SettingObject<Integer> SIZE_CONFIG = abilitySettings.new SettingObject<Integer>(SuperNova.class, "size", 7,
			"# 초신성이 사망할 때 일어날 폭발의 크기") {

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

	public SuperNova(Participant participant) {
		super(participant);
	}

	private final int size = SIZE_CONFIG.getValue();

	@SubscribeEvent
	private void onParticipantDeath(ParticipantDeathEvent e) {
		if (e.getParticipant().equals(getParticipant())) {
			final Location center = e.getPlayer().getLocation();
			new SimpleTimer(TaskType.NORMAL, 8) {
				@Override
				protected void run(int count) {
					final float r = size * ((float) count / getMaximumCount());
					for (final LocationIterator iterator = Sphere.iteratorOf(center, r, (int) (r * 1.4)); iterator.hasNext(); ) {
						ParticleLib.EXPLOSION_LARGE.spawnParticle(iterator.next(), 0, 0, 0, 1, .01);
					}
				}

				@Override
				protected void onEnd() {
					final double offset = size / 3.0;
					ParticleLib.END_ROD.spawnParticle(center, offset, offset, offset, size * 35, .01);
				}
			}.setPeriod(TimeUnit.TICKS, 1).start();
			for (Player player : LocationUtil.getNearbyEntities(Player.class, center, size, size, predicate)) {
				player.setNoDamageTicks(0);
				Damages.damageExplosion(player, getPlayer(), (float) (player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() / 1.35));
			}
			new SimpleTimer(TaskType.REVERSE, 10) {
				@Override
				protected void run(int count) {
					SoundLib.ENTITY_GENERIC_EXPLODE.playSound(center, 4, (float) count / getMaximumCount());
				}
			}.setPeriod(TimeUnit.TICKS, 1).start();
		}
	}

}
