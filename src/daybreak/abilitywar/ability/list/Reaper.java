package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.ability.event.AbilityRestrictionClearEvent;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.minecraft.version.NMSUtil.Hologram;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.math.LocationUtil;
import daybreak.abilitywar.utils.math.LocationUtil.Locations;
import daybreak.abilitywar.utils.math.VectorUtil.Vectors;
import daybreak.abilitywar.utils.math.geometry.Circle;
import daybreak.abilitywar.utils.math.geometry.Line;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Random;

@AbilityManifest(Name = "영혼수확자", Rank = AbilityManifest.Rank.A, Species = AbilityManifest.Species.HUMAN)
public class Reaper extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Reaper.class, "Cooldown", 140,
			"# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public static final SettingObject<Integer> DistanceConfig = new SettingObject<Integer>(Reaper.class, "Distance", 7,
			"# 거리 설정") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}

	};

	public Reaper(AbstractGame.Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f생명체가 죽을 경우 그 자리에 60초간 영혼이 남으며,"),
				ChatColor.translateAlternateColorCodes('&', "&f가까이 가면 수확할 수 있습니다. 철괴 우클릭 시 수확한 영혼을 모두 방출해"),
				ChatColor.translateAlternateColorCodes('&', "&f7초간 주위를 떠돌게 합니다. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&f주위를 떠도는 영혼들은 생명체에 닿으면 해당 생명체에게 대미지를 주며,"),
				ChatColor.translateAlternateColorCodes('&', "&f7초 후에는 영혼들을 모두 온 사방으로 흩뿌리며 닿은 생명체들에게"),
				ChatColor.translateAlternateColorCodes('&', "&f큰 대미지를 줍니다. 스킬을 맞은 모든 적에게 구속 디버프를 부여하며"),
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 좌클릭하면 수확한 영혼의 개수를 확인합니다."));
	}

	private static final RGB BLACK = RGB.of(1, 1, 1);
	private static final RGB SOUL_COLOUR = RGB.of(1, 17, 48);
	private static final Vector MULTIPLY = new Vector(0.1, 0.55, 0.1);

	private final int distance = DistanceConfig.getValue();

	private int soulCount = 0;

	private final Circle wingVectors = Circle.of(0.6, 20);
	private Vectors souls;

	private int tempSoul;

	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());

	private final Timer abilityOne = new Timer(140) {
		@Override
		protected void onStart() {
			tempSoul = soulCount;
			souls = new Vectors();
			Random random = new Random();
			for (int i = 0; i < tempSoul; i++) {
				souls.add(Vector.getRandom().multiply(new Vector(
						random.nextBoolean() ? distance : -distance,
						0,
						random.nextBoolean() ? distance : -distance
				)).setY(Math.random() * 1.4));
			}
			soulCount = 0;
			soulNotice.update(ChatColor.translateAlternateColorCodes('&', "&f" + soulCount + " &0●"));
		}

		@Override
		protected void run(int count) {
			getPlayer().setVelocity(getPlayer().getVelocity().multiply(MULTIPLY));
			Location playerLocation = getPlayer().getLocation().clone().add(0, 1, 0);
			for (Location location : wingVectors.clone().rotateAroundAxisY(playerLocation.getYaw()).rotateAroundAxis(playerLocation.getDirection().clone().setY(0).normalize(), 52).toLocations(playerLocation)) {
				ParticleLib.REDSTONE.spawnParticle(location, BLACK);
			}
			for (Location location : wingVectors.clone().rotateAroundAxisY(playerLocation.getYaw()).rotateAroundAxis(playerLocation.getDirection().clone().setY(0).normalize(), -52).toLocations(playerLocation)) {
				ParticleLib.REDSTONE.spawnParticle(location, BLACK);
			}
			for (Location location : souls.rotateAroundAxisY(6).toLocations(playerLocation)) {
				ParticleLib.REDSTONE.spawnParticle(location, SOUL_COLOUR);
				for (Damageable damageable : LocationUtil.getNearbyDamageableEntities(location, 1.5, 1.5)) {
					if (!getPlayer().equals(damageable)) {
						damageable.damage(3.5, getPlayer());
						if (damageable instanceof LivingEntity)
							PotionEffects.SLOW.addPotionEffect((LivingEntity) damageable, 60, 2, true);
					}
				}
			}
		}

		@Override
		protected void onEnd() {
			abilityTwo.start();
		}
	}.setPeriod(TimeUnit.TICKS, 1);

	private final Timer abilityTwo = new Timer(25) {
		int count;

		@Override
		protected void onStart() {
			count = 0;
		}

		@Override
		protected void run(int seconds) {
			count++;
			getPlayer().setVelocity(getPlayer().getVelocity().multiply(MULTIPLY));
			Location playerLocation = getPlayer().getLocation().clone().add(0, 1, 0);
			for (Location location : souls.rotateAroundAxisY(6).toLocations(playerLocation)) {
				Location realLocation = location.add(Line.vectorAt(location, playerLocation, 25, count));
				ParticleLib.REDSTONE.spawnParticle(realLocation, SOUL_COLOUR);
				for (Damageable damageable : LocationUtil.getNearbyDamageableEntities(realLocation, 1.5, 1.5)) {
					if (!getPlayer().equals(damageable)) {
						damageable.damage(3.5, getPlayer());
						if (damageable instanceof LivingEntity)
							PotionEffects.SLOW.addPotionEffect((LivingEntity) damageable, 60, 2, true);
					}
				}
			}
		}

		@Override
		protected void onEnd() {
			abilityThree.start();
		}
	}.setPeriod(TimeUnit.TICKS, 1);

	private final Timer abilityThree = new Timer(20) {
		int count;
		ArrayList<Locations> locationsList;

		@Override
		protected void onStart() {
			count = 0;
			locationsList = new ArrayList<>();
			Random random = new Random();
			Location playerLocation = getPlayer().getLocation();
			for (int i = 0; i < tempSoul; i++) {
				locationsList.add(Line.of(Vector.getRandom().multiply(new Vector(
						random.nextBoolean() ? distance * 2 : -distance * 2,
						random.nextBoolean() ? distance * 2 : -distance * 2,
						random.nextBoolean() ? distance * 2 : -distance * 2
				)), 20).toLocations(playerLocation));
			}
		}

		@Override
		protected void run(int seconds) {
			count++;
			for (Locations locations : locationsList) {
				Location location = locations.get(count);
				ParticleLib.REDSTONE.spawnParticle(location, SOUL_COLOUR);
				for (Damageable damageable : LocationUtil.getNearbyDamageableEntities(location, 1.5, 1.5)) {
					if (!getPlayer().equals(damageable)) {
						damageable.damage(7, getPlayer());
						if (damageable instanceof LivingEntity)
							PotionEffects.SLOW.addPotionEffect((LivingEntity) damageable, 60, 2, true);
					}
				}
			}
		}
	}.setPeriod(TimeUnit.TICKS, 1);

	@SubscribeEvent(onlyRelevant = true)
	private void onRestrictionClear(AbilityRestrictionClearEvent e) {
		soulNotice.update(ChatColor.translateAlternateColorCodes('&', "&f" + soulCount + " &0●"));
	}

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT)) {
			if (clickType.equals(ClickType.LEFT_CLICK)) {
				getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&0수확한 영혼&f: " + soulCount + "개"));
			} else if (clickType.equals(ClickType.RIGHT_CLICK) && !cooldownTimer.isCooldown() && !abilityOne.isRunning() && !abilityTwo.isRunning()) {
				abilityOne.start();
				cooldownTimer.start();
				return true;
			}
		}
		return false;
	}

	private final Vectors sphere = LocationUtil.getSphere(0.07, 4);

	private final ActionbarChannel soulNotice = newActionbarChannel();

	@SubscribeEvent
	private void onPlayerDeath(PlayerDeathEvent e) {
		if (getGame().isParticipating(e.getEntity())) {
			Locations locations = sphere.toLocations(e.getEntity().getLocation().clone().add(0, 1, 0));
			new Timer(1200) {
				@Override
				protected void run(int count) {
					for (Location location : locations) {
						ParticleLib.REDSTONE.spawnParticle(getPlayer(), location, SOUL_COLOUR);
						if (location.distanceSquared(getPlayer().getLocation()) <= 1.2) {
							stop(false);
							soulCount += 40;
							soulNotice.update(ChatColor.translateAlternateColorCodes('&', "&f" + soulCount + " &0●"));
							Hologram hologram = new Hologram(e.getEntity().getLocation().clone(), ChatColor.translateAlternateColorCodes('&', "&f+ " + 40 + " &0●"));
							new Timer(4) {
								@Override
								protected void onStart() {
									hologram.display(getPlayer());
								}

								@Override
								protected void run(int count) {
								}

								@Override
								protected void onEnd() {
									hologram.hide(getPlayer());
								}

								@Override
								protected void onSilentEnd() {
									hologram.hide(getPlayer());
								}
							}.start();
							break;
						}
					}
				}
			}.setPeriod(TimeUnit.TICKS, 1).start();
		}
	}

	@SubscribeEvent
	private void onEntityDeath(EntityDeathEvent e) {
		Locations locations = sphere.toLocations(e.getEntity().getLocation().clone().add(0, 1, 0));
		new Timer(1200) {
			@Override
			protected void run(int count) {
				for (Location location : locations) {
					ParticleLib.REDSTONE.spawnParticle(getPlayer(), location, SOUL_COLOUR);
					if (location.distanceSquared(getPlayer().getLocation()) <= 1.2) {
						stop(false);
						int soulGain = (int) e.getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
						if (e.getEntity() instanceof Animals) soulGain /= 7;
						else if (e.getEntity() instanceof Monster) soulGain /= 5;
						soulCount += soulGain;
						soulNotice.update(ChatColor.translateAlternateColorCodes('&', "&f" + soulCount + " &0●"));
						Hologram hologram = new Hologram(e.getEntity().getLocation().clone(), ChatColor.translateAlternateColorCodes('&', "&f+ " + soulGain + " &0●"));
						new Timer(4) {
							@Override
							protected void onStart() {
								hologram.display(getPlayer());
							}

							@Override
							protected void run(int count) {
							}

							@Override
							protected void onEnd() {
								hologram.hide(getPlayer());
							}

							@Override
							protected void onSilentEnd() {
								hologram.hide(getPlayer());
							}
						}.start();
						break;
					}
				}
			}
		}.setPeriod(TimeUnit.TICKS, 1).start();
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {

	}

}
