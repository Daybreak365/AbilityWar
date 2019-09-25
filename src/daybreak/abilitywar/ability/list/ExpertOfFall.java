package daybreak.abilitywar.ability.list;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.library.SoundLib;

@AbilityManifest(Name = "낙법의 달인", Rank = Rank.C, Species = Species.HUMAN)
public class ExpertOfFall extends AbilityBase {

	public ExpertOfFall(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f30년간의 고된 수련으로 낙법과 일심동체가 된 낙법의 달인."),
				ChatColor.translateAlternateColorCodes('&', "&f낙하해 땅에 닿았을 때 자동으로 물낙법을 합니다."));
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
	public void TargetSkill(MaterialType mt, LivingEntity entity) {}
	
}
