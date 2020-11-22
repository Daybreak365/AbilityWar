package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Location;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@AbilityManifest(name = "마술사", rank = Rank.A, species = Species.HUMAN, explain = {
		"활을 쐈을 때, 화살이 맞은 위치에서 5칸 범위 내에 있는 생명체들에게",
		"최대체력의 30% 만큼의 대미지를 추가로 입히고 위치를 뒤바꿉니다. $[COOLDOWN_CONFIG]"
})
public class Magician extends AbilityBase {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Magician.class, "cooldown", 8,
			"# 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};

	public Magician(Participant participant) {
		super(participant);
	}

	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());
	private final Circle circle = Circle.of(5, 70);

	private final Predicate<Entity> ONLY_PARTICIPANTS = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			return !(entity instanceof Player) || (getGame().isParticipating(entity.getUniqueId())
					&& (!(getGame() instanceof DeathManager.Handler) || !((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
					&& getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue());
		}
	};

	@SubscribeEvent
	public void onProjectileHit(ProjectileHitEvent e) {
		if (e.getEntity() instanceof Arrow) {
			if (getPlayer().equals(e.getEntity().getShooter())) {
				if (!cooldownTimer.isCooldown()) {
					SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(getPlayer());
					final Location center = e.getEntity().getLocation();
					final Map<Damageable, Location> locationMap = new HashMap<>();
					final List<Damageable> damageables = LocationUtil.getNearbyEntities(Damageable.class, center, 5, 5, ONLY_PARTICIPANTS);
					for (Damageable damageable : damageables) {
						locationMap.put(damageable, damageable.getLocation());
						if (!damageable.equals(getPlayer())) {
							if (LocationUtil.isInCircle(center, damageable.getLocation(), 5)) {
								damageable.damage((damageable instanceof Attributable ? ((Attributable) damageable).getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() : 0) * 0.3, getPlayer());
								if (damageable instanceof Player) {
									SoundLib.ENTITY_ILLUSIONER_CAST_SPELL.playSound((Player) damageable);
								}
							}
						}
					}

					Collections.shuffle(damageables);
					final List<Damageable> keySet = new ArrayList<>(locationMap.keySet());
					for (int i = 0; i < damageables.size(); i++) {
						damageables.get(i).teleport(locationMap.get(keySet.get(i)));
					}

					for (Location loc : circle.toLocations(center).floor(center.getY())) {
						ParticleLib.SPELL_WITCH.spawnParticle(loc);
					}
					ParticleLib.CLOUD.spawnParticle(center, 5, 5, 5, 50);
					cooldownTimer.start();
				}
			}
		}
	}

}
