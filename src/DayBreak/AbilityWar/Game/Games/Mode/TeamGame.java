package DayBreak.AbilityWar.Game.Games.Mode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

import DayBreak.AbilityWar.Utils.Language.KoreanUtil;

public abstract class TeamGame extends AbstractGame {

	public TeamGame() {
		registerListener(new Listener() {
			@EventHandler
			private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
				Entity damagerEntity = e.getDamager();
				if(damagerEntity instanceof Projectile) {
					ProjectileSource source = ((Projectile) damagerEntity).getShooter();
					if(source instanceof Entity) damagerEntity = (Entity) source;
				}
				if(e.getEntity() instanceof Player && damagerEntity instanceof Player) {
					Player victim = (Player) e.getEntity(), damager = (Player) damagerEntity;
					if(isParticipating(victim) && isParticipating(damager)) {
						Participant v = getParticipant(victim), d = getParticipant(damager);
						if(hasTeam(v) && hasTeam(d) && (getTeam(v).equals(getTeam(d)))) e.setCancelled(true);
					}
				}
			}
		});
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
		
		public void addMember(Participant p) {
			if(!isMember(p)) {
				members.add(p);
				p.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&f당신의 팀이 " + KoreanUtil.getCompleteWord(this.name, "&f으로", "&f로") + " 설정되었습니다."));
			}
		}

		public void removeMember(Participant p) {
			if(isMember(p)) {
				members.remove(p);
				p.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', this.name + "&f 팀에서 나왔습니다."));
			}
		}
		
		public boolean isMember(Participant p) {
			return members.contains(p);
		}
		
	}
	
}
