package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.event.AbilityRestrictionClearEvent;
import daybreak.abilitywar.config.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.math.LocationUtil;
import daybreak.abilitywar.utils.math.geometry.Circle;
import daybreak.abilitywar.utils.versioncompat.VersionUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

@AbilityManifest(Name = "플로라", Rank = Rank.B, Species = Species.GOD)
public class Flora extends AbilityBase {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Flora.class, "Cooldown", 3,
			"# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public Flora(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f꽃과 풍요의 여신."),
				ChatColor.translateAlternateColorCodes('&', "&f주변에 있는 모든 플레이어를 재생시키거나 신속 효과를 줍니다."),
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 효과를 뒤바꿉니다. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 좌클릭하면 범위를 변경합니다."));
	}

	private final Circle circle = new Circle(getPlayer().getLocation(), 6).setAmount(50).setHighestLocation(true);
	private EffectType type = EffectType.SPEED;
	private Radius radius = Radius.BIG;
	private final ParticleLib.RGB REGENERATION_COLOR = new ParticleLib.RGB(255, 93, 82);
	private final ParticleLib.RGB SPEED_COLOR = new ParticleLib.RGB(46, 219, 202);

	private final Timer Passive = new Timer() {

		private double y;
		private boolean add;

		@Override
		public void onStart() {
			y = 1.0;
		}

		@Override
		public void onProcess(int count) {
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
			for (Location l : circle.setCenter(center).getLocations()) {
				if (type.equals(EffectType.SPEED)) {
					ParticleLib.REDSTONE.spawnParticle(l.subtract(0, y, 0), SPEED_COLOR);
				} else {
					ParticleLib.REDSTONE.spawnParticle(l.subtract(0, y, 0), REGENERATION_COLOR);
				}
			}

			for (Player p : LocationUtil.getNearbyPlayers(center, radius.radius, 200)) {
				if (LocationUtil.isInCircle(center, p.getLocation(), radius.radius)) {
					if (type.equals(EffectType.SPEED)) {
						PotionEffects.SPEED.addPotionEffect(p, 20, 2, true);
					} else {
						if(!p.isDead()) {
							double maxHealth = VersionUtil.getMaxHealth(p);

							if(p.getHealth() < maxHealth) {
								p.setHealth(Math.min(p.getHealth() + 0.05, 20.0));
							}
						}
					}
				}
			}
		}

	}.setPeriod(1);

	private final CooldownTimer Cool = new CooldownTimer(CooldownConfig.getValue());

	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		if (mt.equals(MaterialType.IRON_INGOT)) {
			if (ct.equals(ClickType.RIGHT_CLICK)) {
				if (!Cool.isCooldown()) {
					Player p = getPlayer();
					if (type.equals(EffectType.SPEED)) {
						type = EffectType.REGENERATION;
					} else {
						type = EffectType.SPEED;
					}

					p.sendMessage(ChatColor.translateAlternateColorCodes('&', type.name + "&f으로 변경되었습니다."));

					Cool.startTimer();
				}
			} else if (ct.equals(ClickType.LEFT_CLICK)) {
				radius = radius.next();
				circle.setRadius(radius.radius);
				getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6범위 설정&f: " + radius.radius));
			}
		}

		return false;
	}

	@SubscribeEvent(onlyRelevant = true)
	public void onRestrictionClear(AbilityRestrictionClearEvent e) {
		Passive.startTimer();
	}

	private enum EffectType {

		REGENERATION(ChatColor.translateAlternateColorCodes('&', "&c재생")),
		SPEED(ChatColor.translateAlternateColorCodes('&', "&b신속"));

		private final String name;

		EffectType(String name) {
			this.name = name;
		}

	}

	private enum Radius {

		BIG(6) {
			protected Radius next() {
				return Radius.MIDIUM;
			}
		},
		MIDIUM(4) {
			protected Radius next() {
				return Radius.SMALL;
			}
		},
		SMALL(2) {
			protected Radius next() {
				return Radius.BIG;
			}
		};

		private final int radius;

		Radius(int radius) {
			this.radius = radius;
		}

		protected abstract Radius next();

	}

	@Override
	public void TargetSkill(MaterialType mt, LivingEntity entity) {
	}

}
