package daybreak.abilitywar.game.games.teamgame;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.game.events.GameCreditEvent;
import daybreak.abilitywar.game.games.mode.GameManifest;
import daybreak.abilitywar.game.games.mode.decorator.TeamGame;
import daybreak.abilitywar.game.games.standard.Game;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.game.manager.object.DefaultKitHandler;
import daybreak.abilitywar.game.manager.object.InfiniteDurability;
import daybreak.abilitywar.game.script.Script;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.PlayerCollector;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.message.KoreanUtil;
import daybreak.abilitywar.utils.thread.AbilityWarThread;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import javax.naming.OperationNotSupportedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

@GameManifest(Name = "팀 전투", Description = {"§f능력자 전쟁을 팀 대항전으로 플레이할 수 있습니다."})
public class TeamFight extends Game implements DefaultKitHandler, TeamGame {

	public TeamFight() {
		super(PlayerCollector.EVERY_PLAYER_EXCLUDING_SPECTATORS());
		setRestricted(invincible);
	}

	private boolean invincible = Settings.InvincibilitySettings.isEnabled();

	@Override
	protected void progressGame(int seconds) {
		switch (seconds) {
			case 1:
				ArrayList<String> lines = Messager.asList(ChatColor.translateAlternateColorCodes('&', "&6==== &e게임 참여자 목록 &6===="));
				int count = 0;
				for (Participant p : getParticipants()) {
					count++;
					lines.add(ChatColor.translateAlternateColorCodes('&', "&a" + count + ". &f" + p.getPlayer().getName()));
				}
				lines.add(ChatColor.translateAlternateColorCodes('&', "&e총 인원수 : " + count + "명"));
				lines.add(ChatColor.translateAlternateColorCodes('&', "&6=========================="));

				for (String line : lines) {
					Bukkit.broadcastMessage(line);
				}

				if (getParticipants().size() < 1) {
					AbilityWarThread.StopGame();
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c최소 참가자 수를 충족하지 못하여 게임을 중지합니다. &8(&71명&8)"));
				}
				break;
			case 3:
				lines = Messager.asList(
						ChatColor.translateAlternateColorCodes('&', "&cAbilityWar &f- &6능력자 전쟁"),
						ChatColor.translateAlternateColorCodes('&', "&e버전 &7: &f" + AbilityWar.getPlugin().getDescription().getVersion()),
						ChatColor.translateAlternateColorCodes('&', "&b개발자 &7: &fDaybreak 새벽"),
						ChatColor.translateAlternateColorCodes('&', "&9디스코드 &7: &f새벽&7#5908")
				);

				GameCreditEvent event = new GameCreditEvent();
				Bukkit.getPluginManager().callEvent(event);
				lines.addAll(event.getCreditList());

				for (String line : lines) {
					Bukkit.broadcastMessage(line);
				}
				break;
			case 5:
				if (Settings.getDrawAbility()) {
					for (String line : Messager.asList(
							ChatColor.translateAlternateColorCodes('&', "&f플러그인에 총 &b" + AbilityList.nameValues().size() + "개&f의 능력이 등록되어 있습니다."),
							ChatColor.translateAlternateColorCodes('&', "&7능력을 무작위로 할당합니다..."))) {
						Bukkit.broadcastMessage(line);
					}
					try {
						startAbilitySelect();
					} catch (OperationNotSupportedException ignored) {
					}
				}
				break;
			case 7:
				if (Settings.getDrawAbility()) {
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f모든 참가자가 능력을 &b확정&f했습니다."));
				} else {
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f능력자 게임 설정에 따라 &b능력&f을 추첨하지 않습니다."));
				}
				break;
			case 9:
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e잠시 후 게임이 시작됩니다."));
				break;
			case 11:
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e게임이 &c5&e초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 12:
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e게임이 &c4&e초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 13:
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e게임이 &c3&e초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 14:
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e게임이 &c2&e초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 15:
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e게임이 &c1&e초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 16:
				for (String line : Messager.asList(
						ChatColor.translateAlternateColorCodes('&', "&e■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■"),
						ChatColor.translateAlternateColorCodes('&', "&f             &cAbilityWar &f- &6능력자 전쟁  "),
						ChatColor.translateAlternateColorCodes('&', "&f                    게임 시작                "),
						ChatColor.translateAlternateColorCodes('&', "&e■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■"))) {
					Bukkit.broadcastMessage(line);
				}

				giveDefaultKit(getParticipants());

				if (Settings.getSpawnEnable()) {
					Location spawn = Settings.getSpawnLocation();
					for (Participant participant : getParticipants()) {
						participant.getPlayer().teleport(spawn);
					}
				}

				if (Settings.getNoHunger()) {
					new TimerBase() {
						@Override
						public void onStart() {
							Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&a배고픔 무제한이 적용됩니다."));
						}

						@Override
						public void onProcess(int count) {
							for (Participant participant : getParticipants()) {
								participant.getPlayer().setFoodLevel(19);
							}
						}
					}.setPeriod(1).startTimer();
				} else {
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4배고픔 무제한&c이 적용되지 않습니다."));
				}

				if (Settings.getInfiniteDurability()) {
					attachObserver(new InfiniteDurability());
				} else {
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4내구도 무제한&c이 적용되지 않습니다."));
				}

				if (Settings.getClearWeather()) {
					for (World w : Bukkit.getWorlds()) {
						w.setStorm(false);
					}
				}

				if (invincible) {
					getInvincibility().Start(false);
				} else {
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4초반 무적&c이 적용되지 않습니다."));
					setRestricted(false);
				}

				for (Participant participant : getParticipants()) {
					setTeam(participant, null);
				}

				Script.RunAll(this);

				startGame();
				break;
		}
	}

	private final HashMap<String, Team> teams = new HashMap<>();
	private final HashMap<Participant, Team> participantTeamMap = new HashMap<>();
	private final TeamParticipantMap teamParticipantMap = new TeamParticipantMap();

	@Override
	public boolean hasTeam(Participant participant) {
		return participantTeamMap.containsKey(participant);
	}

	@Override
	public Team getTeam(Participant participant) {
		return participantTeamMap.get(participant);
	}

	@Override
	public void setTeam(Participant participant, Team team) {
		Player player = participant.getPlayer();
		if (hasTeam(participant)) {
			Team oldTeam = getTeam(participant);
			teamParticipantMap.remove(oldTeam, participant);
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', oldTeam.getDisplayName() + "&f 팀에서 나왔습니다."));
		}
		if (team == null) {
			team = newTeam(UUID.randomUUID().toString(), ChatColor.GREEN + player.getName());
		}
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f당신의 팀이 " + team.getDisplayName() + "&f" +
				KoreanUtil.getNeededJosa(team.getDisplayName().replaceAll("_", ""), KoreanUtil.Josa.으로로) + " 설정되었습니다."));
		teamParticipantMap.add(team, participant);
		participantTeamMap.put(participant, team);
	}

	@Override
	public boolean teamExists(String name) {
		return teams.containsKey(name);
	}

	@Override
	public Team getTeam(String name) {
		return teams.get(name);
	}

	@Override
	public Collection<Team> getTeams() {
		return Collections.unmodifiableCollection(teams.values());
	}

	@Override
	public Collection<Participant> getParticipants(Team team) {
		return Collections.unmodifiableCollection(teamParticipantMap.get(team));
	}

	@Override
	public Team newTeam(String name, String displayName) throws IllegalStateException {
		if (teamExists(name)) {
			throw new IllegalStateException(name + " 팀은 이미 등록된 팀입니다.");
		}
		Team newTeam = new Team(name, displayName);
		teams.put(name, newTeam);
		return newTeam;
	}

	@Override
	public void removeTeam(Team team) {
		teams.remove(team.getName());
		for (Participant participant : new ArrayList<>(getParticipants(team))) {
			setTeam(participant, null);
		}
	}

	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		Player player = e.getPlayer();
		if (isParticipating(player)) {
			Participant participant = getParticipant(e.getPlayer());
			if (participant.attributes().TEAM_CHAT.getValue()) {
				e.setFormat(ChatColor.translateAlternateColorCodes('&', "&5[&d팀&5] &e" + player.getName() + "&f: &r" + e.getMessage()));
				Set<Player> recipients = e.getRecipients();
				recipients.clear();
				if (hasTeam(participant)) {
					for (Participant p : getParticipants(getTeam(participant))) {
						recipients.add(p.getPlayer());
					}
				} else {
					recipients.add(participant.getPlayer());
				}
			}
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player) {
			Participant entity = getParticipant((Player) e.getEntity());
			Participant damager = null;
			if (e.getDamager() instanceof Player) {
				damager = getParticipant((Player) e.getDamager());
			} else if (e.getDamager() instanceof Projectile && ((Projectile) e.getDamager()).getShooter() instanceof Player) {
				damager = getParticipant((Player) ((Projectile) e.getDamager()).getShooter());
			}
			if (entity != null && damager != null) {
				if (hasTeam(entity) && hasTeam(damager) && getTeam(entity).equals(getTeam(damager))) {
					e.setCancelled(true);
				}
			}
		}
	}

	class TeamParticipantMap extends HashMap<Team, ArrayList<Participant>> {

		void add(Team team, Participant participant) {
			if (get(team) == null) {
				put(team, new ArrayList<>());
			}
			get(team).add(participant);
		}

		void remove(Team team, Participant participant) {
			if (get(team) != null) {
				get(team).remove(participant);
			}
		}

		boolean contains(Team team, Participant participant) {
			if (get(team) != null) {
				return get(team).contains(participant);
			}
			return false;
		}

		ArrayList<Participant> get(Team team) {
			if (super.get(team) == null) {
				put(team, new ArrayList<>());
			}
			return super.get(team);
		}

	}

}
