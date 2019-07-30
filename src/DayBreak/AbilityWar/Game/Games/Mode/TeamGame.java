package DayBreak.AbilityWar.Game.Games.Mode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.EventExecutor;

import DayBreak.AbilityWar.AbilityWar;
import DayBreak.AbilityWar.Utils.Messager;

public abstract class TeamGame extends AbstractGame {

	public TeamGame() {
		Bukkit.getPluginManager().registerEvent(EntityDamageByEntityEvent.class, this, EventPriority.HIGHEST, new EventExecutor() {
			@Override
			public void execute(Listener listener, Event event) throws EventException {
				if(event instanceof EntityDamageByEntityEvent) {
					EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
					if(e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
						Player victim = (Player) e.getEntity();
						Player damager = (Player) e.getDamager();
						if(isParticipating(victim) && isParticipating(damager)) {
							Participant v = getParticipant(victim);
							Participant d = getParticipant(damager);
							if(hasTeam(v) && hasTeam(d) && (getTeam(v).equals(getTeam(d)))) {
								e.setCancelled(true);
							}
						}
					}
				}
			}
		}, AbilityWar.getPlugin());
	}
	
	private List<Team> teams = new ArrayList<>();
	private Map<Participant, Team> teamMap = new HashMap<>();
	
	protected abstract List<Team> setupTeams();
	
	private HashMap<Participant, Team> setupTeamMap() {
		final HashMap<Participant, Team> map = new HashMap<>();
		
		for(Team t : teams) {
			for(Participant p : t.getMembers()) {
				map.put(p, t);
			}
		}
		
		return map;
	}
	
	protected void initializeTeam() {
		this.teams = setupTeams();
		this.teamMap = setupTeamMap();
	}
	
	protected Team newTeam(String name, List<Participant> members) throws IllegalArgumentException {
		if(teamNameExists(name)) throw new IllegalArgumentException("이미 존재하는 팀 이름입니다: " + name);
		return new Team(name, members);
	}
	
	private boolean teamNameExists(String name) {
		boolean bool = false;
		for(Team t : teams) {
			if(t.getName().equalsIgnoreCase(name)) bool = true;
		}
		
		return bool;
	}
	
	public boolean hasTeam(Participant p) {
		return teamMap.get(p) != null;
	}
	
	public Team getTeam(Participant p) {
		return teamMap.get(p);
	}
	
	public void setTeam(Participant p, Team t) {
		if(hasTeam(p)) getTeam(p).removeMember(p);
		t.addMember(p);
	}
	
	public class Team {
		
		private final String name;
		private final List<Participant> members = new ArrayList<>();
		
		private Team(String name, List<Participant> members) {
			this.name = name;
			for(Participant p : members) addMember(p);
		}

		public String getName() {
			return name;
		}

		public List<Participant> getMembers() {
			return Collections.unmodifiableList(members);
		}
		
		private void addMember(Participant p) {
			if(!members.contains(p)) {
				members.add(p);
				
				Messager.sendMessage(p.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&f당신의 팀이 " + this.name + "&f(으)로 설정되었습니다."));
			}
		}

		private void removeMember(Participant p) {
			members.remove(p);
		}
		
	}
	
}
