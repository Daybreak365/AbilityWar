package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

@AbilityManifest(name = "테러리스트", rank = Rank.A, species = Species.HUMAN, explain = {
		"철괴를 우클릭하면 자신의 주위에 §cTNT §f$[CountConfig] X 2개를 소환합니다. $[CooldownConfig]",
		"폭발 대미지를 입지 않습니다."
})
public class Terrorist extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Terrorist.class, "Cooldown", 100,
			"# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};

	public static final SettingObject<Integer> CountConfig = new SettingObject<Integer>(Terrorist.class, "Count", 15,
			"# TNT 개수") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public Terrorist(Participant participant) {
		super(participant);
	}

	private final int count = CountConfig.getValue();
	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());
	private final Circle circle = Circle.of(10, count);

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT)) {
			if (clickType.equals(ClickType.RIGHT_CLICK)) {
				if (!cooldownTimer.isCooldown()) {
					Location center = getPlayer().getLocation();

					for (Location l : LocationUtil.getRandomLocations(center, 9, count))
						l.getWorld().spawnEntity(l, EntityType.PRIMED_TNT);
					for (Location l : circle.toLocations(center).floor(center.getY()))
						l.getWorld().spawnEntity(l, EntityType.PRIMED_TNT);

					cooldownTimer.start();

					return true;
				}
			}
		}

		return false;
	}

	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.getEntity().equals(getPlayer())) {
			if (e.getCause().equals(DamageCause.BLOCK_EXPLOSION) || e.getCause().equals(DamageCause.ENTITY_EXPLOSION)) {
				e.setCancelled(true);
			}
		}
	}

}
