package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.AbilitySettings;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.library.SoundLib;
import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

@AbilityManifest(Name = "좀비", Rank = Rank.B, Species = Species.OTHERS)
public class Zombie extends AbilityBase {

	public static final AbilitySettings.SettingObject<Integer> DecreaseConfig = new AbilitySettings.SettingObject<Integer>(TheHighPriestess.class, "Decrease", 50,
			"# 데미지 감소량 (단위: 퍼센트)") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 1 && value <= 50;
		}

	};

	public Zombie(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f받는 데미지가 " + DecreaseConfig.getValue() + "% 감소합니다. 근육 경련으로 인해"),
				ChatColor.translateAlternateColorCodes('&', "&f공격할 떄 에임이 튑니다."));
	}

	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		return false;
	}

	private final Random random = new Random();

	private final int decrease = DecreaseConfig.getValue();

	@SubscribeEvent
	private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		Player player = getPlayer();
		Entity entity = e.getEntity();
		Entity damager = e.getDamager();
		if (entity.equals(player)) {
			if (damager instanceof Player) {
				SoundLib.ENTITY_ZOMBIE_HURT.playSound((Player) damager);
			}
			e.setDamage((e.getDamage() / 100) * (100 - decrease));
		}
		if (damager.equals(player)) {
			Location playerLocation = player.getLocation();
			playerLocation.setPitch(random.nextInt(360) - 179);
			playerLocation.setYaw(random.nextInt(180) - 89);
			player.teleport(playerLocation);
		}
	}

	@SubscribeEvent
	private void onMobTarget(EntityTargetLivingEntityEvent e) {
		if (e.getTarget() instanceof Player && getPlayer().equals(e.getTarget()) && e.getEntityType().equals(EntityType.ZOMBIE)) {
			e.setCancelled(true);
		}
	}

	@Override
	public void TargetSkill(MaterialType mt, LivingEntity entity) {}

}
