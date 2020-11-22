package daybreak.abilitywar.game.team;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.serializable.SpawnLocation;
import daybreak.abilitywar.config.serializable.team.PresetContainer;
import daybreak.abilitywar.config.serializable.team.TeamPreset;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.Game;
import daybreak.abilitywar.game.ParticipantStrategy;
import daybreak.abilitywar.game.event.GameWinEvent;
import daybreak.abilitywar.game.event.GameWinEvent.TeamWinner;
import daybreak.abilitywar.game.team.event.ParticipantTeamChangedEvent;
import daybreak.abilitywar.game.team.event.TeamCreatedEvent;
import daybreak.abilitywar.game.team.event.TeamRemovedEvent;
import daybreak.abilitywar.game.team.interfaces.Members;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil.Josa;
import daybreak.abilitywar.utils.base.minecraft.FireworkUtil;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;

public abstract class TeamGame extends Game implements Teamable {

	private interface ListenerObserver extends Listener, Observer {}

	private final TeamPreset teamPreset;
	private final Map<String, Members> teams;
	private final Map<AbstractGame.Participant, Members> participantTeamMap;

	protected TeamGame(final Collection<Player> players, final String[] args) {
		super(players);
		final PresetContainer presetContainer = Settings.getPresetContainer();
		if (args.length != 0) {
			if (presetContainer.hasPreset(args[0])) {
				this.teamPreset = validatePreset(presetContainer.getPreset(args[0]));
			} else {
				final StringJoiner joiner = new StringJoiner(", ");
				for (final String name : presetContainer.getKeys()) {
					joiner.add(name);
				}
				super.onEnd();
				throw new IllegalArgumentException(args[0] + KoreanUtil.getJosa(args[0], Josa.은는) + " 존재하지 않는 팀 프리셋입니다. 사용 가능한 프리셋: " + joiner.toString());
			}
		} else {
			if (presetContainer.getPresets().size() == 1) {
				this.teamPreset = validatePreset(new ArrayList<>(presetContainer.getPresets()).get(0));
			} else {
				if (presetContainer.getPresets().isEmpty()) {
					super.onEnd();
					throw new IllegalArgumentException("팀 전투에서 사용 가능한 팀 프리셋이 존재하지 않습니다. '/aw config teampreset' 에서 프리셋을 만들어주세요.");
				} else {
					final StringJoiner joiner = new StringJoiner(", ");
					for (final String name : presetContainer.getKeys()) {
						joiner.add(name);
					}
					super.onEnd();
					throw new IllegalArgumentException("팀 전투에서 사용 가능한 팀 프리셋이 2개 이상 있습니다. '/.. start <팀 프리셋 이름>'과 같은 방법으로 사용할 프리셋을 선택해주세요. 사용 가능한 프리셋: " + joiner.toString());
				}
			}
		}
		this.teams = new HashMap<>();
		this.participantTeamMap = new HashMap<>();
		final ListenerObserver listenerObserver = new ListenerObserver() {
			@Override
			public void update(GameUpdate update) {
				switch (update) {
					case START:
						teamPreset.getDivisionType().divide(TeamGame.this, teamPreset);
						if (Settings.getSpawnEnable()) {
							final Location spawn = Settings.getSpawnLocation().toBukkitLocation();
							for (Participant participant : getParticipants()) {
								participant.getPlayer().teleport(hasTeam(participant) ? getTeam(participant).getSpawn().toBukkitLocation() : spawn);
							}
						}
						break;
					case END:
						HandlerList.unregisterAll(this);
						break;
				}
			}


			@EventHandler
			private void onChat(final AsyncPlayerChatEvent e) {
				final Participant participant = getParticipant(e.getPlayer());
				if (participant != null) {
					final Members team = getTeam(participant);
					if (participant.attributes().TEAM_CHAT.getValue()) {
						e.setFormat("§5[§d팀§5] §e" + participant.getPlayer().getName() + "§f: §r" + e.getMessage().replace("%", "%%"));
						final Set<Player> recipients = e.getRecipients();
						recipients.clear();
						if (team != null) {
							for (AbstractGame.Participant recipient : team.getMembers()) {
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

			@Nullable
			private Entity getDamager(final Entity damager) {
				if (damager instanceof Projectile) {
					final ProjectileSource shooter = ((Projectile) damager).getShooter();
					return shooter instanceof Entity ? (Entity) shooter : null;
				} else return damager;
			}

			@EventHandler
			private void onEntityDamageByEntity(final EntityDamageByEntityEvent e) {
				final Participant entity = getParticipant(e.getEntity().getUniqueId());
				if (entity == null) return;
				final Entity damagerEntity = getDamager(e.getDamager());
				if (damagerEntity != null) {
					final Participant damager = getParticipant(damagerEntity.getUniqueId());
					if (damager != null && hasTeam(entity) && hasTeam(damager) && getTeam(entity) == getTeam(damager)) {
						e.setCancelled(true);
					}
				}
			}

		};
		Bukkit.getPluginManager().registerEvents(listenerObserver, AbilityWar.getPlugin());
		attachObserver(listenerObserver);
	}

	private TeamPreset validatePreset(final TeamPreset preset) {
		if (!preset.isValid()) {
			super.onEnd();
			throw new IllegalArgumentException("프리셋 '" + preset.getName() + "'" + KoreanUtil.getJosa(preset.getName(), Josa.은는) + " 유효하지 않은 프리셋입니다. 프리셋에 설정된 팀의 수가 1개 이상인지 확인해주세요.");
		}
		return preset;
	}

	@Override
	public Collection<? extends Participant> getParticipants() {
		return ((TeamGameStrategy) participantStrategy).getParticipants();
	}

	@Override
	public Participant getParticipant(final Player player) {
		return ((TeamGameStrategy) participantStrategy).getParticipant(player.getUniqueId());
	}

	@Override
	public Participant getParticipant(final UUID uuid) {
		return ((TeamGameStrategy) participantStrategy).getParticipant(uuid);
	}

	@Override
	protected ParticipantStrategy newParticipantStrategy(Collection<Player> players) {
		return new TeamGameStrategy(players);
	}

	private class TeamGameStrategy implements ParticipantStrategy {
		private final Map<UUID, Participant> participants = new HashMap<>();

		private TeamGameStrategy(final Collection<Player> players) {
			for (Player player : players) {
				participants.put(player.getUniqueId(), new Participant(player));
			}
		}

		@Override
		public boolean isParticipating(UUID uuid) {
			return participants.containsKey(uuid);
		}

		@Override
		public Participant getParticipant(UUID uuid) {
			return participants.get(uuid);
		}

		@Override
		public Collection<? extends Participant> getParticipants() {
			return Collections.unmodifiableCollection(participants.values());
		}

		@Override
		public void removeParticipant(UUID uuid) throws UnsupportedOperationException {
			throw new UnsupportedOperationException("참가자를 제거할 수 없습니다.");
		}

		@Override
		public void addParticipant(Player player) throws UnsupportedOperationException {
			throw new UnsupportedOperationException("참가자를 추가할 수 없습니다.");
		}

	}

	@Override
	public boolean hasTeam(@NotNull AbstractGame.Participant participant) {
		return participantTeamMap.containsKey(participant);
	}

	@Nullable
	@Override
	public Members getTeam(@NotNull AbstractGame.Participant participant) {
		return participantTeamMap.get(participant);
	}

	private int unique = 0;

	@Override
	public void setTeam(@NotNull AbstractGame.Participant participant, @Nullable Members nullableTeam) {
		final Members oldTeam = getTeam(participant);
		if (oldTeam != null) {
			oldTeam.removeMember(participant);
			participant.getPlayer().sendMessage(oldTeam.getDisplayName() + "§f 팀에서 나왔습니다.");
		}
		final Members team = nullableTeam != null ? nullableTeam : newSoloTeam();
		participant.getPlayer().sendMessage("§f당신의 팀이 " + team.getDisplayName() + "§f" + KoreanUtil.getJosa(team.getDisplayName().replace("_", ""), Josa.으로로) + " 설정되었습니다.");
		team.addMember(participant);
		participantTeamMap.put(participant, team);
		if (oldTeam != null) {
			Bukkit.getPluginManager().callEvent(new ParticipantTeamChangedEvent(this, this, participant, oldTeam, team));
		}
	}

	private Members newSoloTeam() {
		do {
			unique++;
		} while (teamExists(String.valueOf(unique)) || getScoreboardManager().getScoreboard().getTeam(String.valueOf(unique)) != null);
		return newTeam(String.valueOf(unique), ChatColor.GREEN + "개인팀");
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
		if (teamExists(name)) throw new IllegalStateException("$name 팀은 이미 등록된 팀입니다.");
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
		for (AbstractGame.Participant member : team.getMembers()) {
			setTeam(member, null);
		}
		team.unregister();
		Bukkit.getPluginManager().callEvent(new TeamRemovedEvent(this, this, team));
	}

	public class Team implements Members {

		private final String name, displayName;
		private final Set<AbstractGame.Participant> members = new HashSet<>();
		private org.bukkit.scoreboard.Team team;
		@NotNull
		private SpawnLocation spawn = Settings.getSpawnLocation();

		private Team(final String name, final String displayName) {
			this.name = name;
			this.displayName = displayName;
			this.team = getScoreboardManager().registerNewTeam(name);
			team.setCanSeeFriendlyInvisibles(true);
			team.setDisplayName(displayName);
			team.setAllowFriendlyFire(false);
			team.setPrefix(displayName + ChatColor.WHITE);
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
		public boolean addMember(@NotNull AbstractGame.Participant participant) {
			if (members.add(participant)) {
				team.addEntry(participant.getPlayer().getName());
				return true;
			}
			return false;
		}

		@Override
		public boolean removeMember(@NotNull AbstractGame.Participant participant) {
			if (members.remove(participant)) {
				team.removeEntry(participant.getPlayer().getName());
				return true;
			}
			return false;
		}

		@Override
		public boolean isMember(@NotNull AbstractGame.Participant participant) {
			return members.contains(participant);
		}

		@NotNull
		@Override
		public Set<AbstractGame.Participant> getMembers() {
			return Collections.unmodifiableSet(members);
		}

		@Override
		public boolean isExcluded() {
			for (AbstractGame.Participant member : members) {
				if (!getDeathManager().isExcluded(member.getPlayer())) return false;
			}
			return true;
		}

		@NotNull
		@Override
		public SpawnLocation getSpawn() {
			return spawn;
		}

		@Override
		public void setSpawn(@NotNull final SpawnLocation spawn) {
			this.spawn = spawn;
		}

		@Override
		public void unregister() {
			getScoreboardManager().unregisterTeam(team);
		}

		@Override
		public boolean equals(Object other) {
			return other instanceof Team && ((Team) other).name.equals(this.name);
		}

		@Override
		public int hashCode() {
			return name != null ? name.hashCode() : 0;
		}

		@Override
		public String toString() {
			final StringJoiner joiner = new StringJoiner(ChatColor.WHITE + ", " + ChatColor.GRAY, ChatColor.GRAY.toString(), "");
			for (AbstractGame.Participant member : members) {
				joiner.add(member.getPlayer().getName());
			}
			return displayName + "§8(" + joiner.toString() + "§8)§f";
		}

	}

	public class Participant extends ParticipantImpl {

		private Participant(final Player player) {
			super(player);
		}

		@Override
		public String toString() {
			return getPlayer().getName();
		}

		public boolean hasTeam() {
			return TeamGame.this.hasTeam(this);
		}

		public Members getTeam() {
		return TeamGame.this.getTeam(this);
		}

	}

	public interface Winnable extends daybreak.abilitywar.game.interfaces.Winnable {
		default void Win(final Members winTeam) {
			if (!isRunning()) return;
			Messager.clearChat();
			for (AbstractGame.Participant member : winTeam.getMembers()) {
				SoundLib.UI_TOAST_CHALLENGE_COMPLETE.playSound(member.getPlayer());
				new SimpleTimer(TaskType.REVERSE, 8) {
					@Override
					protected void run(int count) {
						FireworkUtil.spawnWinnerFirework(member.getPlayer().getEyeLocation());
					}
				}.setPeriod(TimeUnit.TICKS, 4).start();
			}
			Bukkit.broadcastMessage("§5§l우승자§f: §d" + winTeam + ".");
			stop();
			Bukkit.getPluginManager().callEvent(new GameWinEvent((AbstractGame) this, new TeamWinner(winTeam)));
		}

	}

}
