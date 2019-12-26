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
import daybreak.abilitywar.utils.math.LocationUtil;
import daybreak.abilitywar.utils.math.geometry.Circle;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

@AbilityManifest(Name = "테러리스트", Rank = Rank.A, Species = Species.HUMAN)
public class Terrorist extends AbilityBase {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Terrorist.class, "Cooldown", 100,
			"# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
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
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 자신의 주위에 TNT " + (CountConfig.getValue() * 2) + "개를 떨어뜨립니다. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&f폭발 데미지를 입지 않습니다."));
	}

	private final int count = CountConfig.getValue();
	private final CooldownTimer Cool = new CooldownTimer(CooldownConfig.getValue());

	@Override
	public boolean ActiveSkill(Material materialType, ClickType ct) {
		if (materialType.equals(Material.IRON_INGOT)) {
			if (ct.equals(ClickType.RIGHT_CLICK)) {
				if (!Cool.isCooldown()) {
					Location center = getPlayer().getLocation();
					for (int i = 0; i < 10; i++) {
						for (Location l : new Circle(center, i).setAmount(20).setHighestLocation(true).getLocations()) {
							ParticleLib.LAVA.spawnParticle(l, 0, 0, 0, 1);
						}
					}

					for (Location l : LocationUtil.getRandomLocations(center, 9, count))
						l.getWorld().spawnEntity(l, EntityType.PRIMED_TNT);
					for (Location l : new Circle(center, 10).setAmount(count).setHighestLocation(true).getLocations())
						l.getWorld().spawnEntity(l, EntityType.PRIMED_TNT);

					Cool.startTimer();

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


	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
	}

}
