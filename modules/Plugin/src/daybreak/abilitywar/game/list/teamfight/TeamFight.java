package daybreak.abilitywar.game.list.teamfight;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.serializable.team.PresetContainer;
import daybreak.abilitywar.config.serializable.team.TeamPreset;
import daybreak.abilitywar.game.AbstractGame.Observer;
import daybreak.abilitywar.game.Game;
import daybreak.abilitywar.game.GameManifest;
import daybreak.abilitywar.game.event.GameCreditEvent;
import daybreak.abilitywar.game.interfaces.TeamGame;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.game.manager.object.DefaultKitHandler;
import daybreak.abilitywar.game.manager.object.InfiniteDurability;
import daybreak.abilitywar.game.script.manager.ScriptManager;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil.Josa;
import daybreak.abilitywar.utils.base.minecraft.PlayerCollector;
import daybreak.abilitywar.utils.library.SoundLib;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import javax.naming.OperationNotSupportedException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

@GameManifest(name = "팀 전투", description = {"§f능력자 전쟁을 팀 대항전으로 플레이할 수 있습니다."})
public class TeamFight extends Game implements DefaultKitHandler, TeamGame, Observer {

	private final TeamPreset preset;
	private final boolean invincible = Settings.InvincibilitySettings.isEnabled();

	public TeamFight(String[] args) throws IllegalArgumentException {
		super(PlayerCollector.EVERY_PLAYER_EXCLUDING_SPECTATORS());
		PresetContainer container = Settings.getPresetContainer();
		if (args.length >= 1) {
			if (container.hasPreset(args[0])) {
				this.preset = validatePreset(container.getPreset(args[0]));
			} else {
				StringJoiner joiner = new StringJoiner(", ");
				for (String name : container.getKeys()) {
					joiner.add(name);
				}
				super.onEnd();
				throw new IllegalArgumentException(args[0] + KoreanUtil.getJosa(args[0], Josa.은는) + " 존재하지 않는 팀 프리셋입니다. 사용 가능한 프리셋: " + joiner.toString());
			}
		} else {
			if (container.getPresets().size() == 1) {
				this.preset = validatePreset(new ArrayList<>(container.getPresets()).get(0));
			} else {
				if (container.getPresets().size() == 0) {
					super.onEnd();
					throw new IllegalArgumentException("팀 전투에서 사용 가능한 팀 프리셋이 존재하지 않습니다. '/aw config teampreset' 에서 프리셋을 만들어주세요.");
				} else {
					StringJoiner joiner = new StringJoiner(", ");
					for (String name : container.getKeys()) {
						joiner.add(name);
					}
					super.onEnd();
					throw new IllegalArgumentException("팀 전투에서 사용 가능한 팀 프리셋이 2개 이상 있습니다. '/.. start <팀 프리셋 이름>'과 같은 방법으로 사용할 프리셋을 선택해주세요. 사용 가능한 프리셋: " + joiner.toString());
				}
			}
		}
		setRestricted(invincible);
		attachObserver(this);
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
	}

	private TeamPreset validatePreset(TeamPreset preset) {
		if (!preset.isValid())
			throw new IllegalArgumentException("프리셋 '" + preset.getName() + "'" + KoreanUtil.getJosa(preset.getName(), Josa.은는) + " 유효하지 않은 프리셋입니다. 프리셋에 설정된 팀의 수가 1개 이상인지 확인해주세요.");
		return preset;
	}

	@Override
	protected void progressGame(int seconds) {
		switch (seconds) {
			case 1:
				List<String> lines = Messager.asList("§6==== §e게임 참여자 목록 §6====");
				int count = 0;
				for (Participant p : getParticipants()) {
					count++;
					lines.add("§a" + count + ". §f" + p.getPlayer().getName());
				}
				lines.add("§e총 인원수 : " + count + "명");
				lines.add("§6==========================");

				for (String line : lines) {
					Bukkit.broadcastMessage(line);
				}

				if (getParticipants().size() < 1) {
					stop();
					Bukkit.broadcastMessage("§c최소 참가자 수를 충족하지 못하여 게임을 중지합니다. §8(§71명§8)");
				}
				break;
			case 3:
				lines = Messager.asList(
						"§cAbilityWar §f- §6능력자 전쟁",
						"§e버전 §7: §f" + AbilityWar.getPlugin().getDescription().getVersion(),
						"§b개발자 §7: §fDaybreak 새벽",
						"§9디스코드 §7: §f새벽§7#5908"
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
							"§f플러그인에 총 §b" + AbilityList.nameValues().size() + "개§f의 능력이 등록되어 있습니다.",
							"§7능력을 무작위로 할당합니다...")) {
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
					Bukkit.broadcastMessage("§f모든 참가자가 능력을 §b확정§f했습니다.");
				} else {
					Bukkit.broadcastMessage("§f능력자 게임 설정에 따라 §b능력§f을 추첨하지 않습니다.");
				}
				break;
			case 9:
				Bukkit.broadcastMessage("§e잠시 후 게임이 시작됩니다.");
				break;
			case 11:
				Bukkit.broadcastMessage("§e게임이 §c5§e초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 12:
				Bukkit.broadcastMessage("§e게임이 §c4§e초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 13:
				Bukkit.broadcastMessage("§e게임이 §c3§e초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 14:
				Bukkit.broadcastMessage("§e게임이 §c2§e초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 15:
				Bukkit.broadcastMessage("§e게임이 §c1§e초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 16:
				for (String line : Messager.asList(
						"§e■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■",
						"§f             §cAbilityWar §f- §6능력자 전쟁  ",
						"§f                    게임 시작                ",
						"§e■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■")) {
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
					Bukkit.broadcastMessage("§2배고픔 무제한§a이 적용됩니다.");
				} else {
					Bukkit.broadcastMessage("§4배고픔 무제한§c이 적용되지 않습니다.");
				}

				if (Settings.getInfiniteDurability()) {
					attachObserver(new InfiniteDurability());
				} else {
					Bukkit.broadcastMessage("§4내구도 무제한§c이 적용되지 않습니다.");
				}

				if (Settings.getClearWeather()) {
					for (World w : Bukkit.getWorlds()) {
						w.setStorm(false);
					}
				}

				if (invincible) {
					getInvincibility().start(false);
				} else {
					Bukkit.broadcastMessage("§4초반 무적§c이 적용되지 않습니다.");
					setRestricted(false);
				}

				preset.getDivisionType().divide(this, preset);

				ScriptManager.runAll(this);

				startGame();
				break;
		}
	}

	private final Map<String, Team> teams = new HashMap<>();
	private final Map<Participant, Team> participantTeamMap = new HashMap<>();

	@Override
	public boolean hasTeam(Participant participant) {
		return participantTeamMap.containsKey(participant);
	}

	@Override
	public Team getTeam(Participant participant) {
		return participantTeamMap.get(participant);
	}

	private static int unique = 0;

	@Override
	public void setTeam(Participant participant, Team team) {
		Player player = participant.getPlayer();
		if (hasTeam(participant)) {
			Team oldTeam = getTeam(participant);
			oldTeam.remove(participant);
			player.sendMessage(oldTeam.getDisplayName() + "§f 팀에서 나왔습니다.");
		}
		if (team == null) {
			do {
				unique++;
			} while (teamExists(String.valueOf(unique)) || getScoreboardManager().getScoreboard().getTeam(String.valueOf(unique)) != null);
			String teamName = String.valueOf(unique);
			if (teamExists(teamName)) {
				team = getTeam(teamName);
			} else {
				team = newTeam(teamName, ChatColor.GREEN + "개인팀");
			}
		}
		player.sendMessage("§f당신의 팀이 " + team.getDisplayName() + "§f" +
				KoreanUtil.getJosa(team.getDisplayName().replaceAll("_", ""), KoreanUtil.Josa.으로로) + " 설정되었습니다.");
		team.add(participant);
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
	public Team newTeam(String name, String displayName) throws IllegalStateException, IllegalArgumentException {
		if (teamExists(name)) throw new IllegalStateException(name + " 팀은 이미 등록된 팀입니다.");
		if (getScoreboardManager().getScoreboard().getTeam(name) != null)
			throw new IllegalStateException("스코어보드에서 이미 사용중인 팀 이름은 사용할 수 없습니다.");
		if (displayName.length() > 12) throw new IllegalArgumentException("팀 별명은 최대 12글자까지 입력할 수 있습니다.");
		Team newTeam = new Team(getScoreboardManager(), name, displayName);
		teams.put(name, newTeam);
		return newTeam;
	}

	@Override
	public void removeTeam(Team team) {
		teams.remove(team.getName());
		for (Participant participant : team.getParticipants()) {
			setTeam(participant, null);
		}
	}

	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		Player player = e.getPlayer();
		if (isParticipating(player)) {
			Participant participant = getParticipant(e.getPlayer());
			if (participant.attributes().TEAM_CHAT.getValue()) {
				e.setFormat("§5[§d팀§5] §e" + player.getName() + "§f: §r" + e.getMessage().replaceAll("%", "%%"));
				Set<Player> recipients = e.getRecipients();
				recipients.clear();
				if (hasTeam(participant)) {
					for (Participant p : getTeam(participant).getParticipants()) {
						if (p.getPlayer().isOnline()) recipients.add(p.getPlayer());
					}
				} else {
					recipients.add(participant.getPlayer());
				}
			} else if (hasTeam(participant)) {
				e.setFormat(ChatColor.WHITE + "[" + getTeam(participant).getDisplayName() + ChatColor.WHITE + "] " + e.getFormat());
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
			} else if (e.getDamager() instanceof Projectile) {
				Projectile projectile = (Projectile) e.getDamager();
				if (projectile.getShooter() instanceof Player) {
					damager = getParticipant((Player) projectile.getShooter());
				}
			}
			if (entity != null && damager != null) {
				if (hasTeam(entity) && hasTeam(damager) && getTeam(entity).equals(getTeam(damager))) {
					e.setCancelled(true);
				}
			}
		}
	}

	@Override
	public void update(GameUpdate update) {
		if (update == GameUpdate.END) {
			HandlerList.unregisterAll(this);
		}
	}

}
