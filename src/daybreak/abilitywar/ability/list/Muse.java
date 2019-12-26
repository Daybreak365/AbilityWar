package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.math.LocationUtil;
import daybreak.abilitywar.utils.math.geometry.Circle;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

@AbilityManifest(Name = "뮤즈", Rank = Rank.S, Species = Species.GOD)
public class Muse extends AbilityBase {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Muse.class, "Cooldown", 80,
			"# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public Muse(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 뮤즈가 주변 지역을 축복하여"),
				ChatColor.translateAlternateColorCodes('&', "&f모두가 데미지를 받지 않는 지역을 만들어냅니다. ") + Messager.formatCooldown(CooldownConfig.getValue()));
	}

	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());

	private Location center = null;

	private final DurationTimer skill = new DurationTimer(90, cooldownTimer) {

		private int count;
		private int soundCount;
		private Circle circle;

		@Override
		public void onDurationStart() {
			count = 1;
			soundCount = 1;
			center = getPlayer().getLocation();
			circle = new Circle(center, count).setAmount(count * 6).setHighestLocation(true);
		}

		@Override
		public void onDurationProcess(int seconds) {
			circle.setRadius(count).setAmount(count * 6);

			if (count <= 10) {
				for (Location l : circle.getLocations()) {
					ParticleLib.NOTE.spawnParticle(l.subtract(0, 1, 0), 0, 0, 0, 1);
				}

				if (count == 1) {
					for (Player p : LocationUtil.getNearbyPlayers(center, 20, 20)) {
						SoundLib.BELL.playInstrument(p, Note.natural(0, Tone.C));
					}
				} else if (count == 2) {
					for (Player p : LocationUtil.getNearbyPlayers(center, 20, 20)) {
						SoundLib.BELL.playInstrument(p, Note.natural(0, Tone.E));
					}
				} else if (count == 3) {
					for (Player p : LocationUtil.getNearbyPlayers(center, 20, 20)) {
						SoundLib.BELL.playInstrument(p, Note.natural(0, Tone.G));
					}
				} else if (count == 4) {
					for (Player p : LocationUtil.getNearbyPlayers(center, 20, 20)) {
						SoundLib.BELL.playInstrument(p, Note.natural(1, Tone.C));
					}
				} else if (count == 5) {
					for (Player p : LocationUtil.getNearbyPlayers(center, 20, 20)) {
						SoundLib.BELL.playInstrument(p, Note.natural(0, Tone.G));
					}
				} else if (count == 6) {
					for (Player p : LocationUtil.getNearbyPlayers(center, 20, 20)) {
						SoundLib.BELL.playInstrument(p, Note.natural(0, Tone.E));
					}
				} else if (count == 7) {
					for (Player p : LocationUtil.getNearbyPlayers(center, 20, 20)) {
						SoundLib.BELL.playInstrument(p, Note.natural(0, Tone.C));
					}
				} else if (count == 8) {
					for (Player p : LocationUtil.getNearbyPlayers(center, 20, 20)) {
						SoundLib.BELL.playInstrument(p, Note.natural(0, Tone.E));
					}
				} else if (count == 9) {
					for (Player p : LocationUtil.getNearbyPlayers(center, 20, 20)) {
						SoundLib.BELL.playInstrument(p, Note.natural(0, Tone.G));
					}
				} else if (count == 10) {
					for (Player p : LocationUtil.getNearbyPlayers(center, 20, 20)) {
						SoundLib.BELL.playInstrument(p, Note.natural(1, Tone.C));
					}
				}

				count++;
			} else {
				for (Location l : circle.getLocations()) {
					ParticleLib.NOTE.spawnParticle(l.subtract(0, 1, 0), 0, 0, 0, 1);
				}

				for (Player p : LocationUtil.getNearbyPlayers(center, 11, 200)) {
					PotionEffects.GLOWING.addPotionEffect(p, 4, 0, true);

					if (soundCount % 5 == 0) {
						soundCount = 1;

						SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(p);
					}
				}

				soundCount++;
			}
		}

		@Override
		public void onDurationEnd() {
			center = null;
		}

	}.setPeriod(2);

	@Override
	public boolean ActiveSkill(Material materialType, ClickType ct) {
		if (materialType.equals(Material.IRON_INGOT)) {
			if (ct.equals(ClickType.RIGHT_CLICK)) {
				if (!skill.isDuration() && !cooldownTimer.isCooldown()) {
					skill.startTimer();

					return true;
				}
			}
		}

		return false;
	}

	@SubscribeEvent
	public void onEntityDamage(EntityDamageEvent e) {
		if (center != null) {
			if (LocationUtil.isInCircle(center, e.getEntity().getLocation(), 11)) {
				ParticleLib.HEART.spawnParticle(e.getEntity().getLocation(), 2, 2, 2, 5);
				e.setCancelled(true);
			}
		}
	}

	@SubscribeEvent
	public void onEntityDamage(EntityDamageByEntityEvent e) {
		if (center != null) {
			if (LocationUtil.isInCircle(center, e.getEntity().getLocation(), 11)) {
				ParticleLib.HEART.spawnParticle(e.getEntity().getLocation(), 2, 2, 2, 5);
				e.setCancelled(true);
			}
		}
	}

	@SubscribeEvent
	public void onEntityDamage(EntityDamageByBlockEvent e) {
		if (center != null) {
			if (LocationUtil.isInCircle(center, e.getEntity().getLocation(), 11)) {
				ParticleLib.HEART.spawnParticle(e.getEntity().getLocation(), 2, 2, 2, 5);
				e.setCancelled(true);
			}
		}
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
	}

}
