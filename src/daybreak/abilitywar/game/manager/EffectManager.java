package daybreak.abilitywar.game.manager;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.EventExecutor;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.list.BlackCandle;
import daybreak.abilitywar.game.games.mode.AbstractGame;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.thread.TimerBase;

public class EffectManager implements EventExecutor {

	private final AbstractGame game;
	
	public EffectManager(AbstractGame game) {
		this.game = game;
		Bukkit.getPluginManager().registerEvent(PlayerMoveEvent.class, game, EventPriority.HIGHEST, this, AbilityWar.getPlugin());
		
		registerCondition(new EffectCondition() {
			@Override
			protected boolean checkCondition(Participant p, EffectType type) {
				if(type.equals(EffectType.STUN) && p.hasAbility() && p.getAbility().getClass().equals(BlackCandle.class)) return false;
				return true;
			}
		});
	}
	
	private final List<Participant> STUN = new ArrayList<Participant>();
	
	public void Stun(Player p, int Tick) {
		if(game.isParticipating(p)) {
			Participant part = game.getParticipant(p);

			for(EffectCondition condition : conditions) {
				if(!condition.checkCondition(part, EffectType.STUN)) return;
			}
			
			new TimerBase(Tick) {
				@Override
				protected void onStart() {
					STUN.add(part);
				}
				@Override
				protected void onEnd() {
					STUN.remove(part);
				}
				@Override
				protected void TimerProcess(Integer Seconds) {}
			}.setPeriod(1).StartTimer();
		}
	}

	private final List<EffectCondition> conditions = new ArrayList<EffectCondition>();
	
	public void registerCondition(EffectCondition condition) {
		if(!conditions.contains(condition)) conditions.add(condition);
	}

	public abstract class EffectCondition {
		
		protected abstract boolean checkCondition(Participant p, EffectType type);
		
	}

	private enum EffectType {
		
		STUN;
		
	}
	
	@Override
	public void execute(Listener listener, Event event) throws EventException {
		if(event instanceof PlayerMoveEvent) {
			PlayerMoveEvent e = (PlayerMoveEvent) event;
			Player p = e.getPlayer();
			if(game.isParticipating(p)) {
				Participant part = game.getParticipant(p);
				if(STUN.contains(part)) {
					e.setCancelled(true);
				}
			}
		}
	}
	
}
