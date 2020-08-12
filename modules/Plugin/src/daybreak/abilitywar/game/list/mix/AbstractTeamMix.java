package daybreak.abilitywar.game.list.mix;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.serializable.SpawnLocation;
import daybreak.abilitywar.config.serializable.team.PresetContainer;
import daybreak.abilitywar.config.serializable.team.TeamPreset;
import daybreak.abilitywar.game.team.event.ParticipantTeamChangedEvent;
import daybreak.abilitywar.game.team.event.TeamCreatedEvent;
import daybreak.abilitywar.game.team.event.TeamRemovedEvent;
import daybreak.abilitywar.game.team.interfaces.Members;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil.Josa;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractTeamMix extends AbstractMix implements Teamable {

	private final TeamPreset teamPreset;
	private final Map<String, Members> teams = new HashMap<>();
	private final Map<Participant, Members> participantTeamMap = new HashMap<>();

	public AbstractTeamMix(Collection<Player> players, String[] args) {
		super(players);
		final PresetContainer container = Settings.getPresetContainer();
		if (args.length >= 1) {
			if (container.hasPreset(args[0])) {
				this.teamPreset = validatePreset(container.getPreset(args[0]));
			} else {
				final StringJoiner joiner = new StringJoiner(", ");
				for (String name : container.getKeys()) {
					joiner.add(name);
				}
				super.onEnd();
				throw new IllegalArgumentException(args[0] + KoreanUtil.getJosa(args[0], Josa.은는) + " 존재하지 않는 팀 프리셋입니다. 사용 가능한 프리셋: " + joiner.toString());
			}
		} else {
			if (container.getPresets().size() == 1) {
				this.teamPreset = validatePreset(new ArrayList<>(container.getPresets()).get(0));
			} else {
				if (container.getPresets().size() == 0) {
					super.onEnd();
					throw new IllegalArgumentException("팀 전투에서 사용 가능한 팀 프리셋이 존재하지 않습니다. '/aw config teampreset' 에서 프리셋을 만들어주세요.");
				} else {
					final StringJoiner joiner = new StringJoiner(", ");
					for (String name : container.getKeys()) {
						joiner.add(name);
					}
					super.onEnd();
					throw new IllegalArgumentException("팀 전투에서 사용 가능한 팀 프리셋이 2개 이상 있습니다. '/.. start <팀 프리셋 이름>'과 같은 방법으로 사용할 프리셋을 선택해주세요. 사용 가능한 프리셋: " + joiner.toString());
				}
			}
		}

		class Handler implements Listener, Observer {
			private Handler() {
				Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
			}
			@EventHandler
			public void onChat(AsyncPlayerChatEvent e) {
				final Participant participant = getParticipant(e.getPlayer());
				if (participant != null) {
					final Members team = getTeam(participant);
					if (participant.attributes().TEAM_CHAT.getValue()) {
						e.setFormat("§5[§d팀§5] §e" + participant.getPlayer().getName() + "§f: §r" + e.getMessage().replaceAll("%", "%%"));
						final Set<Player> recipients = e.getRecipients();
						recipients.clear();
						if (team != null) {
							for (Participant recipient : team.getMembers()) {
								if (recipient.getPlayer().isOnline()) recipients.add(recipient.getPlayer());
							}
						} else {
							recipients.add(participant.getPlayer());
						}
					} else if (team != null) {
						e.setFormat(ChatColor.WHITE + "[" + team.getDisplayName() + ChatColor.WHITE + "] " + e.getFormat());
					}
				}
			}

			@EventHandler
			public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
				final Participant entity = getParticipant(e.getEntity().getUniqueId());
				if (entity != null) {
					final Participant damager;
					if (e.getDamager() instanceof Player) {
						damager = getParticipant((Player) e.getDamager());
					} else if (e.getDamager() instanceof Projectile) {
						final Projectile projectile = (Projectile) e.getDamager();
						if (projectile.getShooter() instanceof Player) {
							damager = getParticipant((Player) projectile.getShooter());
						} else {
							damager = null;
						}
					} else damager = null;
					if (damager != null && hasTeam(entity) && hasTeam(damager) && getTeam(entity).equals(getTeam(damager))) {
						e.setCancelled(true);
					}
				}
			}
			@Override
			public void update(GameUpdate update) {
				if (update == GameUpdate.START) {
					teamPreset.getDivisionType().divide(AbstractTeamMix.this, teamPreset);
					if (Settings.getSpawnEnable()) {
						final Location spawn = Settings.getSpawnLocation().toBukkitLocation();
						for (Participant participant : getParticipants()) {
							participant.getPlayer().teleport(hasTeam(participant) ? getTeam(participant).getSpawn().toBukkitLocation() : spawn);
						}
					}
				} else if (update == GameUpdate.END) {
					HandlerList.unregisterAll(this);
				}
			}
		}
		attachObserver(new Handler());
	}

	private TeamPreset validatePreset(TeamPreset preset) {
		if (!preset.isValid()) {
			super.onEnd();
			throw new IllegalArgumentException("프리셋 '" + preset.getName() + "'" + KoreanUtil.getJosa(preset.getName(), Josa.은는) + " 유효하지 않은 프리셋입니다. 프리셋에 설정된 팀의 수가 1개 이상인지 확인해주세요.");
		}
		return preset;
	}

	@Override
	public boolean hasTeam(@NotNull Participant participant) {
		return participantTeamMap.containsKey(participant);
	}

	@Nullable
	@Override
	public Members getTeam(@NotNull Participant participant) {
		return participantTeamMap.get(participant);
	}

	private int unique = 0;

	@Override
	public void setTeam(@NotNull Participant participant, @Nullable Members nullableTeam) {
		final Members oldTeam = getTeam(participant);
		if (oldTeam != null) {
			oldTeam.removeMember(participant);
			participant.getPlayer().sendMessage(oldTeam.getDisplayName() + "§f 팀에서 나왔습니다.");
		}
		final Members team;
		if (nullableTeam != null) {
			team = nullableTeam;
		} else {
			do {
				unique++;
			} while (teamExists(String.valueOf(unique)) || getScoreboardManager().getScoreboard().getTeam(String.valueOf(unique)) != null);
			team = newTeam(String.valueOf(unique), ChatColor.GREEN + "개인팀");
		}
		participant.getPlayer().sendMessage("§f당신의 팀이 " + team.getDisplayName() + "§f" + KoreanUtil.getJosa(team.getDisplayName().replaceAll("_", ""), KoreanUtil.Josa.으로로) + " 설정되었습니다.");
		team.addMember(participant);
		participantTeamMap.put(participant, team);
		if (oldTeam != null) {
			Bukkit.getPluginManager().callEvent(new ParticipantTeamChangedEvent(this, this, participant, oldTeam, team));
		}
	}

	@Override
	public boolean teamExists(@NotNull String name) {
		return teams.containsKey(name);
	}

	@Nullable
	@Override
	public Members getTeam(@NotNull String name) {
		return teams.get(name);
	}

	@NotNull
	@Override
	public Collection<Members> getTeams() {
		return Collections.unmodifiableCollection(teams.values());
	}

	@NotNull
	@Override
	public Members newTeam(@NotNull String name, @NotNull String displayName) throws IllegalStateException, IllegalArgumentException {
		if (teamExists(name)) throw new IllegalStateException(name + " 팀은 이미 등록된 팀입니다.");
		if (getScoreboardManager().getScoreboard().getTeam(name) != null) throw new IllegalStateException("스코어보드에서 이미 사용중인 팀 이름은 사용할 수 없습니다.");
		if (name.length() > 12) throw new IllegalArgumentException("팀 이름은 최대 12글자까지 입력할 수 있습니다.");
		if (displayName.length() > 12) throw new IllegalArgumentException("팀 별명은 최대 12글자까지 입력할 수 있습니다.");
		final Team newTeam = new Team(name, displayName);
		teams.put(name, newTeam);
		Bukkit.getPluginManager().callEvent(new TeamCreatedEvent(this, this, newTeam));
		return newTeam;
	}

	@Override
	public void removeTeam(@NotNull Members team) {
		teams.remove(team.getName());
		for (Participant member : team.getMembers()) {
			setTeam(member, null);
		}
		team.unregister();
		Bukkit.getPluginManager().callEvent(new TeamRemovedEvent(this, this, team));
	}

	protected class Team implements Members {

		private final String name, displayName;
		private final Set<Participant> members = new HashSet<>();
		private final org.bukkit.scoreboard.Team team;
		private SpawnLocation spawn = Settings.getSpawnLocation();

		private Team(final String name, final String displayName) {
			this.name = name;
			this.displayName = displayName;
			this.team = getScoreboardManager().registerNewTeam(name);
			team.setCanSeeFriendlyInvisibles(true);
			team.setDisplayName(displayName);
			team.setAllowFriendlyFire(false);
			team.setPrefix(displayName + ChatColor.WHITE + " ");
		}

		@NotNull
		@Override
		public String getName() {
			return name;
		}

		@NotNull
		@Override
		public String getDisplayName() {
			return displayName;
		}

		@Override
		public boolean addMember(@NotNull Participant participant) {
			if (members.add(participant)) {
				team.addEntry(participant.getPlayer().getName());
				return true;
			}
			return false;
		}

		@Override
		public boolean removeMember(@NotNull Participant participant) {
			if (members.remove(participant)) {
				team.removeEntry(participant.getPlayer().getName());
				return true;
			}
			return false;
		}

		@Override
		public boolean isMember(@NotNull Participant participant) {
			return members.contains(participant);
		}

		@NotNull
		@Override
		public Set<Participant> getMembers() {
			return Collections.unmodifiableSet(members);
		}

		@Override
		public boolean isExcluded() {
			for (Participant member : members) {
				if (!getDeathManager().isExcluded(member.getPlayer())) return false;
			}
			return true;
		}

		@Override
		public void unregister() {
			getScoreboardManager().unregisterTeam(team);
		}

		@Override
		public String toString() {
			final StringJoiner joiner = new StringJoiner(ChatColor.WHITE + ", " + ChatColor.GRAY, ChatColor.GRAY.toString(), "");
			for (Participant member : members) {
				joiner.add(member.getPlayer().getName());
			}
			return displayName + " §8(" + joiner.toString() + "§8)§f";
		}

		@NotNull
		@Override
		public SpawnLocation getSpawn() {
			return spawn;
		}

		@Override
		public void setSpawn(@NotNull SpawnLocation spawn) {
			this.spawn = spawn;
		}
	}

}
