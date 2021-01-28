package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.Tips;
import daybreak.abilitywar.ability.Tips.Description;
import daybreak.abilitywar.ability.Tips.Difficulty;
import daybreak.abilitywar.ability.Tips.Level;
import daybreak.abilitywar.ability.Tips.Stats;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.PotionEffects;
import kotlin.ranges.RangesKt;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

@AbilityManifest(name = "플로라", rank = Rank.A, species = Species.GOD, explain = {
		"꽃과 풍요의 여신.",
		"주변에 있는 모든 플레이어를 §c재생§f시키거나 §b신속 §f효과를 줍니다.",
		"철괴를 우클릭하면 효과를 뒤바꿉니다. $[COOLDOWN_CONFIG]",
		"철괴를 좌클릭하면 범위를 변경합니다."
})
@Tips(tip = {
		"신속, 그리고 재생. 버프를 같이 받고싶은 플레이어가 있다면 범위를 넓게",
		"설정하고 이용하세요. 개인전에서는 그럭저럭 사용할 만한 능력이지만,",
		"팀전에서는 서포팅 능력으로 유용하게 사용할 수 있습니다."
}, strong = {
		@Description(subject = "모두 함께", explain = {
				"범위 안의 모든 플레이어가 버프를 함께 받을 수 있습니다."
		})
}, weak = {
		@Description(subject = "모두 함께", explain = {
				"범위 안의 적 플레이어가 버프를 함께 받습니다."
		})
}, stats = @Stats(offense = Level.ZERO, survival = Level.FOUR, crowdControl = Level.ZERO, mobility = Level.FIVE, utility = Level.ZERO), difficulty = Difficulty.EASY)
public class Flora extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Flora.class, "cooldown", 3,
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

	private final Predicate<Entity> ONLY_PARTICIPANTS = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			return getGame().isParticipating(entity.getUniqueId())
					&& (!(getGame() instanceof DeathManager.Handler) || !((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
					&& getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue();
		}
	};

	private final AbilityTimer passive = new AbilityTimer() {

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

			if (type.equals(EffectType.SPEED)) {
				for (Player player : LocationUtil.getEntitiesInCircle(Player.class, center, radius.radius, ONLY_PARTICIPANTS)) {
					PotionEffects.SPEED.addPotionEffect(player, 3, 1, true);
				}
			} else if (count % 10 == 0) {
				for (Player player : LocationUtil.getEntitiesInCircle(Player.class, center, radius.radius, ONLY_PARTICIPANTS)) {
					if (!player.isDead()) {
						final double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
						if (player.getHealth() < maxHealth) {
							final EntityRegainHealthEvent event = new EntityRegainHealthEvent(player, .3, RegainReason.CUSTOM);
							Bukkit.getPluginManager().callEvent(event);
							if (!event.isCancelled()) {
								player.setHealth(RangesKt.coerceIn(player.getHealth() + event.getAmount(), 0, maxHealth));
							}
						}
					}
				}
			}
		}

	}.setPeriod(TimeUnit.TICKS, 1).register();

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			passive.start();
		}
	}

	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT) {
			if (clickType == ClickType.RIGHT_CLICK) {
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
			} else if (clickType == ClickType.LEFT_CLICK) {
				radius = radius.next();
				getPlayer().sendMessage("§6범위 설정§f: " + radius.radius);
			}
		}

		return false;
	}

	private enum EffectType {

		REGENERATION("§c재생", RGB.of(255, 93, 82)),
		SPEED("§b신속", RGB.of(46, 219, 202));

		private final String name;
		private final RGB color;

		EffectType(String name, RGB color) {
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
		SMALL(2.5, Circle.of(2.5, 40)) {
			protected Radius next() {
				return Radius.BIG;
			}
		};

		private final double radius;
		private final Circle circle;

		Radius(double radius, Circle circle) {
			this.radius = radius;
			this.circle = circle;
		}

		protected abstract Radius next();

	}

}
