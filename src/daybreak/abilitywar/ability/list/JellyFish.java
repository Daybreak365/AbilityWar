package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@AbilityManifest(Name = "해파리", Rank = Rank.A, Species = Species.ANIMAL)
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
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		return false;
	}

	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if(e.getDamager().equals(getPlayer())) {
			Entity entity = e.getEntity();
			if(entity instanceof Player) {
				Player p = (Player) entity;
				SoundLib.ENTITY_ITEM_PICKUP.playSound(getPlayer());
				SoundLib.ENTITY_ITEM_PICKUP.playSound(p);
				JellyFish.this.getGame().getEffectManager().Stun(p, duration);
			}
		}
	}
	
	@Override
	public void TargetSkill(MaterialType mt, LivingEntity entity) {}

	@Override
	protected void onRestrictClear() {}

}
