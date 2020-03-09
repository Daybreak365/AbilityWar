package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@AbilityManifest(name = "해파리", rank = Rank.A, Species = Species.ANIMAL)
public class JellyFish extends AbilityBase {

	public static final SettingObject<Integer> DurationConfig = new SettingObject<Integer>(JellyFish.class, "Duration", 2,
			"# 지속 시간 (틱 단위)") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public JellyFish(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "플레이어를 타격하면 대상을 " + DurationConfig.getValue() + "틱동안 움직이지 못하게 합니다."));
	}

	private final int duration = DurationConfig.getValue();

	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.getDamager().equals(getPlayer())) {
			Entity entity = e.getEntity();
			if (entity instanceof Player) {
				Player p = (Player) entity;
				SoundLib.ENTITY_ITEM_PICKUP.playSound(getPlayer());
				SoundLib.ENTITY_ITEM_PICKUP.playSound(p);
				JellyFish.this.getGame().getEffectManager().Stun(p, duration);
			}
		}
	}

}
