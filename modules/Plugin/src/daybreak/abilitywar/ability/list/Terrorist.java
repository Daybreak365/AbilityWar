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
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.NotNull;

@AbilityManifest(name = "테러리스트", rank = Rank.A, species = Species.HUMAN, explain = {
		"철괴를 우클릭하면 자신의 주위에 §cTNT §f$[CountConfig] X 2개를 소환합니다. $[COOLDOWN_CONFIG]",
		"폭발 대미지를 입지 않습니다."
})
public class Terrorist extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Terrorist.class, "cooldown", 100,
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

	public static final SettingObject<Integer> CountConfig = abilitySettings.new SettingObject<Integer>(Terrorist.class, "count", 15,
			"# TNT 개수") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

	};

	public Terrorist(Participant participant) {
		super(participant);
	}

	private final int count = CountConfig.getValue();
	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());
	private final Circle circle = Circle.of(10, count);

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT) {
			if (clickType == ClickType.RIGHT_CLICK) {
				if (!cooldownTimer.isCooldown()) {
					Location center = getPlayer().getLocation();

					for (Location l : LocationUtil.getRandomLocations(center, 9, count)) {
						l.getWorld().spawn(l, TNTPrimed.class).setFuseTicks(50);
					}
					for (Location l : circle.toLocations(center).floor(center.getY())) {
						l.getWorld().spawn(l, TNTPrimed.class).setFuseTicks(50);
					}

					cooldownTimer.start();

					return true;
				}
			}
		}

		return false;
	}

	@SubscribeEvent(onlyRelevant = true)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.getCause().equals(DamageCause.BLOCK_EXPLOSION) || e.getCause().equals(DamageCause.ENTITY_EXPLOSION)) {
			e.setCancelled(true);
		}
	}

}
