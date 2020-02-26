package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.math.LocationUtil;
import daybreak.abilitywar.utils.math.geometry.Circle;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

@AbilityManifest(Name = "마술사", Rank = Rank.A, Species = Species.HUMAN)
public class Magician extends AbilityBase {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Magician.class, "Cooldown", 8,
			"# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public Magician(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f활을 쐈을 때, 화살이 맞은 위치에서 5칸 범위 내에 있는 생명체들에게"),
				ChatColor.translateAlternateColorCodes('&', "&f최대체력의 1/5 만큼의 대미지를 추가로 입히고 위치를 뒤바꿉니다. " + Messager.formatCooldown(CooldownConfig.getValue())));
	}

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		return false;
	}

	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());
	private final Circle circle = Circle.of(5, 70);

	@SubscribeEvent
	public void onProjectileHit(ProjectileHitEvent e) {
		if (e.getEntity() instanceof Arrow) {
			if (getPlayer().equals(e.getEntity().getShooter())) {
				if (!cooldownTimer.isCooldown()) {
					SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(getPlayer());
					Location center = e.getEntity().getLocation();
					HashMap<Damageable, Location> locationMap = new HashMap<>();
					ArrayList<Damageable> damageables = LocationUtil.getNearbyDamageableEntities(center, 5, 5);
					for (Damageable damageable : damageables) {
						locationMap.put(damageable, damageable.getLocation());
						if (!damageable.equals(getPlayer())) {
							if (LocationUtil.isInCircle(center, damageable.getLocation(), 5)) {
								damageable.damage((damageable instanceof Attributable ? ((Attributable) damageable).getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() : 0) / 5, getPlayer());
								if (damageable instanceof Player) {
									SoundLib.ENTITY_ILLUSIONER_CAST_SPELL.playSound((Player) damageable);
								}
							}
						}
					}

					Collections.shuffle(damageables);
					ArrayList<Damageable> keySet = new ArrayList<>(locationMap.keySet());
					for (int i = 0; i < damageables.size(); i++) {
						damageables.get(i).teleport(locationMap.get(keySet.get(i)));
					}

					for (Location l : circle.toLocations(center).floor(center.getY())) {
						ParticleLib.SPELL_WITCH.spawnParticle(l);
					}
					ParticleLib.CLOUD.spawnParticle(center, 5, 5, 5, 50);

					cooldownTimer.start();
				}
			}
		}
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
	}

}
