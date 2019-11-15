package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@AbilityManifest(Name = "좀비", Rank = Rank.B, Species = Species.OTHERS)
public class Zombie extends AbilityBase {

	public Zombie(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f받는 데미지가 50% 감소합니다. 근육 경련으로 인해"),
				ChatColor.translateAlternateColorCodes('&', "&f에임이 종종 튑니다."));
	}

	private final Timer Aim = new Timer() {
		
		@Override
		public void onStart() {}
		
		@Override
		public void onProcess(int count) {
			Random r = new Random();
			int random = r.nextInt(100) + 1;
			
			if(random <= 10) {
				Location l = getPlayer().getLocation();
				l.setPitch(r.nextInt(360) - 179);
				l.setYaw(r.nextInt(180) - 89);
				getPlayer().teleport(l);
			}
		}
		
		@Override
		public void onEnd() {}
		
	}.setPeriod(5);
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		return false;
	}

	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if(e.getEntity().equals(getPlayer())) {
			e.setDamage(e.getDamage() / 2);
		}
	}
	
	@Override
	public void onRestrictClear() {
		Aim.startTimer();
	}

	@Override
	public void TargetSkill(MaterialType mt, LivingEntity entity) {}
	
}
