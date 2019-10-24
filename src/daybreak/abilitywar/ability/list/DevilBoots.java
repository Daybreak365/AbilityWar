package daybreak.abilitywar.ability.list;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.library.EffectLib;
import daybreak.abilitywar.utils.thread.TimerBase;

@AbilityManifest(Name = "악마의 부츠", Rank = Rank.B, Species = Species.HUMAN)
public class DevilBoots extends AbilityBase {

	public DevilBoots(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f신속하게 이동하며 지나가는 자리에 불이 붙습니다. 화염 피해를 받지 않습니다."));
	}
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		return false;
	}
	
	private final TimerBase speed = new TimerBase() {
		
		@Override
		protected void onStart() {}
		
		@Override
		protected void onEnd() {}
		
		@Override
		protected void onProcess(int Seconds) {
			EffectLib.SPEED.addPotionEffect(getPlayer(), 20, 1, true);
		}
	};
	
	@SubscribeEvent
	public void onPlayerMove(PlayerMoveEvent e) {
		if(e.getPlayer().equals(getPlayer())) {
			Location To = e.getTo();
			if(To.getBlock().getType().equals(Material.AIR)) {
				To.getBlock().setType(Material.FIRE);
			}
		}
	}
	
	@SubscribeEvent
	public void onEntityDamage(EntityDamageEvent e) {
		if(e.getEntity().equals(getPlayer())) {
			DamageCause cause = e.getCause();
			if(cause.equals(DamageCause.FIRE) || cause.equals(DamageCause.FIRE_TICK) || cause.equals(DamageCause.LAVA)) {
				e.setCancelled(true);
			}
		}
	}
	
	@Override
	public void onRestrictClear() {
		speed.StartTimer();
	}

	@Override
	public void TargetSkill(MaterialType mt, LivingEntity entity) {}
	
}
