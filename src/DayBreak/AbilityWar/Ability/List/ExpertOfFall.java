package DayBreak.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Ability.AbilityManifest;
import DayBreak.AbilityWar.Ability.AbilityManifest.Rank;
import DayBreak.AbilityWar.Ability.AbilityManifest.Species;
import DayBreak.AbilityWar.Ability.SubscribeEvent;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame.Participant;
import DayBreak.AbilityWar.Utils.Library.SoundLib;

@AbilityManifest(Name = "³«¹ýÀÇ ´ÞÀÎ", Rank = Rank.C, Species = Species.HUMAN)
public class ExpertOfFall extends AbilityBase {

	public ExpertOfFall(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f³«ÇÏÇØ ¶¥¿¡ ´ê¾ÒÀ» ¶§ ÀÚµ¿À¸·Î ¹°³«¹ýÀ» ÇÕ´Ï´Ù."));
	}

	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		return false;
	}

	@SubscribeEvent
	public void onEntityDamage(EntityDamageEvent e) {
		if(e.getEntity().equals(getPlayer())) {
			if(e.getCause().equals(DamageCause.FALL)) {
				e.setCancelled(true);
				getPlayer().getLocation().getBlock().setType(Material.WATER);
				SoundLib.ENTITY_PLAYER_SPLASH.playSound(getPlayer());
			}
		}
	}
	
	@Override
	public void onRestrictClear() {}

	@Override
	public void TargetSkill(MaterialType mt, Entity entity) {}
	
}
