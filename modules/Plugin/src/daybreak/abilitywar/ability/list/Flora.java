package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.Scheduled;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.PotionEffects;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

@AbilityManifest(name = "플로라", rank = Rank.A, species = Species.GOD, explain = {
		"꽃과 풍요의 여신.",
		"주변에 있는 모든 플레이어를 §c재생§f시키거나 §b신속 §f효과를 줍니다.",
		"철괴를 우클릭하면 효과를 뒤바꿉니다. $[CooldownConfig]",
		"철괴를 좌클릭하면 범위를 변경합니다."
})
public class Flora extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> CooldownConfig = abilitySettings.new SettingObject<Integer>(Flora.class, "Cooldown", 3,
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

	public Flora(Participant participant) {
		super(participant);
	}

	private EffectType type = EffectType.SPEED;
	private Radius radius = Radius.BIG;

	@Scheduled
	private final Timer passive = new Timer() {

		private double y;
		private boolean add;

		@Override
		public void onStart() {
			y = 1.0;
		}

		@Override
		public void run(int count) {
			if (add && y >= 1.0) {
				add = false;
			} else if (!add && y <= 0) {
				add = true;
			}

			if (add) {
				y += 0.2;
			} else {
				y -= 0.2;
			}

			Location center = getPlayer().getLocation();
			for (Location location : radius.circle.toLocations(center).floor(center.getY())) {
				ParticleLib.REDSTONE.spawnParticle(location.subtract(0, y - 1, 0), type.color);
			}

			for (Player player : LocationUtil.getNearbyPlayers(center, radius.radius, 200)) {
				if (LocationUtil.isInCircle(center, player.getLocation(), radius.radius)) {
					if (type.equals(EffectType.SPEED)) {
						PotionEffects.SPEED.addPotionEffect(player, 20, 1, true);
					} else {
						if (!player.isDead()) {
							final double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
							if (player.getHealth() < maxHealth) {
								player.setHealth(Math.min(player.getHealth() + 0.04, player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
							}
						}
					}
				}
			}
		}

	}.setPeriod(TimeUnit.TICKS, 1);

	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT)) {
			if (clickType.equals(ClickType.RIGHT_CLICK)) {
				if (!cooldownTimer.isCooldown()) {
					Player p = getPlayer();
					if (type.equals(EffectType.SPEED)) {
						type = EffectType.REGENERATION;
					} else {
						type = EffectType.SPEED;
					}

					p.sendMessage(type.name + "§f으로 변경되었습니다.");

					cooldownTimer.start();
				}
			} else if (clickType.equals(ClickType.LEFT_CLICK)) {
				radius = radius.next();
				getPlayer().sendMessage("§6범위 설정§f: " + radius.radius);
			}
		}

		return false;
	}

	private enum EffectType {

		REGENERATION("§c재생", ParticleLib.RGB.of(255, 93, 82)),
		SPEED("§b신속", ParticleLib.RGB.of(46, 219, 202));

		private final String name;
		private final ParticleLib.RGB color;

		EffectType(String name, ParticleLib.RGB color) {
			this.name = name;
			this.color = color;
		}

	}

	private enum Radius {

		BIG(6, Circle.of(6, 80)) {
			protected Radius next() {
				return Radius.MIDIUM;
			}
		},
		MIDIUM(4, Circle.of(4, 60)) {
			protected Radius next() {
				return Radius.SMALL;
			}
		},
		SMALL(2, Circle.of(2, 40)) {
			protected Radius next() {
				return Radius.BIG;
			}
		};

		private final int radius;
		private final Circle circle;

		Radius(int radius, Circle circle) {
			this.radius = radius;
			this.circle = circle;
		}

		protected abstract Radius next();

	}

}
