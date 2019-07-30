package DayBreak.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Ability.AbilityManifest;
import DayBreak.AbilityWar.Ability.AbilityManifest.Rank;
import DayBreak.AbilityWar.Ability.AbilityManifest.Species;
import DayBreak.AbilityWar.Ability.SubscribeEvent;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame.Participant;
import DayBreak.AbilityWar.Utils.Library.SoundLib;

@AbilityManifest(Name = "해파리", Rank = Rank.A, Species = Species.ANIMAL)
public class JellyFish extends AbilityBase {

	public JellyFish(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "플레이어를 타격하면 대상을 0.2초간 움직이지 못하게 합니다."));
	}

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
				JellyFish.this.getGame().getEffectManager().Stun(p, 4);
			}
		}
	}
	
	@Override
	public void TargetSkill(MaterialType mt, Entity entity) {}

	@Override
	protected void onRestrictClear() {}

}
