package DayBreak.AbilityWar.Ability.List;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Ability.AbilityManifest;
import DayBreak.AbilityWar.Ability.AbilityManifest.Rank;
import DayBreak.AbilityWar.Ability.AbilityManifest.Species;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame.Participant;
import DayBreak.AbilityWar.Utils.Library.SoundLib;
import DayBreak.AbilityWar.Utils.Thread.TimerBase;

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

	@Override
	public void PassiveSkill(Event event) {
		if(event instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
			if(e.getDamager().equals(getPlayer())) {
				Entity entity = e.getEntity();
				if(entity instanceof Player) {
					Player p = (Player) entity;
					SoundLib.ENTITY_ITEM_PICKUP.playSound(getPlayer());
					SoundLib.ENTITY_ITEM_PICKUP.playSound(p);
					new TimerBase(2) {
						
						@Override
						protected void onStart() {
							MoveRestrict.add(p);
						}
						
						@Override
						protected void onEnd() {
							MoveRestrict.remove(p);
						}
						
						@Override
						protected void TimerProcess(Integer Seconds) {}
					}.setPeriod(2).StartTimer();;
				}
			}
		} else if(event instanceof PlayerMoveEvent) {
			PlayerMoveEvent e = (PlayerMoveEvent) event;
			if(MoveRestrict.contains(e.getPlayer())) {
				e.setCancelled(true);
			}
		}
	}

	List<Player> MoveRestrict = new ArrayList<Player>();
	
	@Override
	public void TargetSkill(MaterialType mt, Entity entity) {}

	@Override
	protected void onRestrictClear() {}

}
