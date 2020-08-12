package daybreak.abilitywar.game.team

import daybreak.abilitywar.AbilityWar
import daybreak.abilitywar.config.Configuration.Settings
import daybreak.abilitywar.config.serializable.SpawnLocation
import daybreak.abilitywar.config.serializable.team.PresetContainer
import daybreak.abilitywar.config.serializable.team.TeamPreset
import daybreak.abilitywar.game.AbstractGame
import daybreak.abilitywar.game.AbstractGame.GameUpdate.END
import daybreak.abilitywar.game.AbstractGame.GameUpdate.START
import daybreak.abilitywar.game.Game
import daybreak.abilitywar.game.ParticipantStrategy
import daybreak.abilitywar.game.team.event.ParticipantTeamChangedEvent
import daybreak.abilitywar.game.team.event.TeamCreatedEvent
import daybreak.abilitywar.game.team.event.TeamRemovedEvent
import daybreak.abilitywar.game.team.interfaces.Members
import daybreak.abilitywar.game.team.interfaces.Teamable
import daybreak.abilitywar.utils.base.Messager
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer.TaskType.REVERSE
import daybreak.abilitywar.utils.base.concurrent.TimeUnit.TICKS
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil.Josa.으로로
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil.Josa.은는
import daybreak.abilitywar.utils.base.minecraft.FireworkUtil
import daybreak.abilitywar.utils.library.SoundLib
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.ChatColor.GREEN
import org.bukkit.ChatColor.WHITE
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.util.ArrayList
import java.util.Collections
import java.util.StringJoiner
import java.util.UUID

abstract class TeamGame(players: Collection<Player>, args: Array<String>): Game(players), Teamable {

	private val teamPreset: TeamPreset?
	private val teams: MutableMap<String, Members>
	private val participantTeamMap: MutableMap<AbstractGame.Participant, Members>

	init {
		val presetContainer: PresetContainer = Settings.getPresetContainer()
		if (args.isNotEmpty()) {
			if (presetContainer.hasPreset(args[0])) {
				this.teamPreset = validatePreset(presetContainer.getPreset(args[0]))
			} else {
				val joiner = StringJoiner(", ")
				for (name in presetContainer.keys) {
					joiner.add(name)
				}
				super.onEnd()
				throw IllegalArgumentException(args[0] + KoreanUtil.getJosa(args[0], 은는) + " 존재하지 않는 팀 프리셋입니다. 사용 가능한 프리셋: " + joiner.toString())
			}
		} else {
			if (presetContainer.presets.size == 1) {
				this.teamPreset = validatePreset(ArrayList(presetContainer.presets)[0])
			} else {
				if (presetContainer.presets.isEmpty()) {
					super.onEnd()
					throw IllegalArgumentException("팀 전투에서 사용 가능한 팀 프리셋이 존재하지 않습니다. '/aw config teampreset' 에서 프리셋을 만들어주세요.")
				} else {
					val joiner = StringJoiner(", ")
					for (name in presetContainer.keys) {
						joiner.add(name)
					}
					super.onEnd()
					throw IllegalArgumentException("팀 전투에서 사용 가능한 팀 프리셋이 2개 이상 있습니다. '/.. start <팀 프리셋 이름>'과 같은 방법으로 사용할 프리셋을 선택해주세요. 사용 가능한 프리셋: $joiner")
				}
			}
		}
		this.teams = HashMap()
		this.participantTeamMap = HashMap()
		val listener = object: Listener, Observer {
			override fun update(update: GameUpdate) {
				when (update) {
					START -> {
						teamPreset.divisionType.divide(this@TeamGame, teamPreset)
						if (Settings.getSpawnEnable()) {
							val spawn = Settings.getSpawnLocation().toBukkitLocation()
							for (participant in participants) {
								participant.player.teleport(if (participant.hasTeam()) participant.getTeam()!!.getSpawn().toBukkitLocation() else spawn)
							}
						}
					}
					END -> {
						HandlerList.unregisterAll(this)
					}
				}
			}

			@EventHandler
			fun onChat(e: AsyncPlayerChatEvent) {
				val participant = getParticipant(e.player)
				if (participant != null) {
					val team = getTeam(participant)
					if (participant.attributes().TEAM_CHAT.value) {
						e.format = "§5[§d팀§5] §e${participant.player.name}§f: §r${e.message.replace("%".toRegex(), "%%")}"
						val recipients = e.recipients
						recipients.clear()
						if (team != null) {
							for (recipient in team.getMembers()) {
								if (recipient.player.isOnline) recipients.add(recipient.player)
							}
						} else {
							recipients.add(participant.player)
						}
					} else if (team != null) {
						e.format = "$WHITE[${team.displayName}$WHITE] ${e.format}"
					}
				}
			}

			@EventHandler
			fun onEntityDamageByEntity(e: EntityDamageByEntityEvent) {
				val entity = getParticipant(e.entity.uniqueId)
				val damager: Participant? = when (e.damager) {
					is Player -> {
						getParticipant(e.damager.uniqueId)
					}
					is Projectile -> {
						val shooter = (e.damager as Projectile).shooter
						if (shooter is Entity) getParticipant(shooter.uniqueId)
						else null
					}
					else -> null
				}
				if (entity != null && damager != null && hasTeam(entity) && hasTeam(damager) && getTeam(entity) == getTeam(damager)) {
					e.isCancelled = true
				}
			}

		}
		Bukkit.getPluginManager().registerEvents(listener, AbilityWar.getPlugin())
		attachObserver(listener)
	}

	private fun validatePreset(preset: TeamPreset): TeamPreset {
		if (!preset.isValid) {
			super.onEnd()
			throw IllegalArgumentException("프리셋 '" + preset.name + "'" + KoreanUtil.getJosa(preset.name, 은는) + " 유효하지 않은 프리셋입니다. 프리셋에 설정된 팀의 수가 1개 이상인지 확인해주세요.")
		}
		return preset
	}

	override fun getParticipants(): MutableCollection<out Participant> {
		return (participantStrategy as TeamGameStrategy).participants
	}

	override fun getParticipant(player: Player): Participant? {
		return (participantStrategy as TeamGameStrategy).getParticipant(player.uniqueId)
	}

	override fun getParticipant(uniqueId: UUID): Participant? {
		return (participantStrategy as TeamGameStrategy).getParticipant(uniqueId)
	}

	override fun newParticipantStrategy(players: Collection<Player>): ParticipantStrategy {
		return TeamGameStrategy(players)
	}

	private inner class TeamGameStrategy(players: Collection<Player>): ParticipantStrategy {
		private val participants: MutableMap<UUID, Participant> = HashMap()

		init {
			for (player in players) {
				participants[player.uniqueId] = Participant(player)
			}
		}

		override fun isParticipating(uniqueId: UUID): Boolean {
			return participants.containsKey(uniqueId)
		}

		override fun getParticipant(uniqueId: UUID): Participant? {
			return participants[uniqueId]
		}

		override fun getParticipants(): MutableCollection<out Participant> {
			return Collections.unmodifiableCollection(participants.values)
		}

		override fun removeParticipant(uuid: UUID) {
			throw UnsupportedOperationException("참가자를 제거할 수 없습니다.")
		}

		override fun addParticipant(player: Player) {
			throw UnsupportedOperationException("참가자를 추가할 수 없습니다.")
		}
	}

	override fun hasTeam(participant: AbstractGame.Participant): Boolean {
		return participantTeamMap.containsKey(participant)
	}

	override fun getTeam(participant: AbstractGame.Participant): Members? {
		return participantTeamMap[participant]
	}

	private var unique = 0

	override fun setTeam(participant: AbstractGame.Participant, nullableTeam: Members?) {
		val oldTeam: Members? = getTeam(participant)
		if (oldTeam != null) {
			oldTeam.removeMember(participant)
			participant.player.sendMessage(oldTeam.displayName + "§f 팀에서 나왔습니다.")
		}
		val team: Members = if (nullableTeam != null) nullableTeam else {
			do {
				unique++
			} while (teamExists(unique.toString()) || scoreboardManager.scoreboard.getTeam(unique.toString()) != null)
			newTeam(unique.toString(), GREEN.toString() + "개인팀")
		}
		participant.player.sendMessage("§f당신의 팀이 ${team.displayName}§f${KoreanUtil.getJosa(team.displayName.replace("_".toRegex(), ""), 으로로)} 설정되었습니다.")
		team.addMember(participant)
		participantTeamMap[participant] = team
		if (oldTeam != null) {
			Bukkit.getPluginManager().callEvent(ParticipantTeamChangedEvent(this, this, participant, oldTeam, team))
		}
	}

	override fun teamExists(name: String): Boolean {
		return teams.containsKey(name)
	}

	override fun getTeam(name: String): Members? {
		return teams[name]
	}

	override fun getTeams(): Collection<Members> {
		return Collections.unmodifiableCollection(teams.values)
	}

	@Throws(IllegalStateException::class, IllegalArgumentException::class)
	override fun newTeam(name: String, displayName: String): Team {
		if (teamExists(name)) throw IllegalStateException("$name 팀은 이미 등록된 팀입니다.")
		if (scoreboardManager.scoreboard.getTeam(name) != null) throw IllegalStateException("스코어보드에서 이미 사용중인 팀 이름은 사용할 수 없습니다.")
		if (name.length > 12) throw IllegalArgumentException("팀 이름은 최대 12글자까지 입력할 수 있습니다.")
		if (displayName.length > 12) throw IllegalArgumentException("팀 별명은 최대 12글자까지 입력할 수 있습니다.")
		val newTeam = Team(name, displayName)
		teams[name] = newTeam
		Bukkit.getPluginManager().callEvent(TeamCreatedEvent(this, this, newTeam))
		return newTeam
	}

	override fun removeTeam(team: Members) {
		teams.remove(team.name)
		for (participant in team.getMembers()) {
			setTeam(participant, null)
		}
		team.unregister()
		Bukkit.getPluginManager().callEvent(TeamRemovedEvent(this, this, team))
	}

	inner class Team(override val name: String, override val displayName: String): Members {
		private val members: MutableSet<AbstractGame.Participant> = HashSet()
		private val team: org.bukkit.scoreboard.Team = scoreboardManager.registerNewTeam(name)
		private var spawn: SpawnLocation = Settings.getSpawnLocation()

		init {
			team.setCanSeeFriendlyInvisibles(true)
			team.displayName = displayName
			team.setAllowFriendlyFire(false)
			team.prefix = "$displayName$WHITE "
		}

		override fun addMember(participant: AbstractGame.Participant): Boolean {
			if (members.add(participant)) {
				team.addEntry(participant.player.name)
				return true
			}
			return false
		}

		override fun removeMember(participant: AbstractGame.Participant): Boolean {
			if (members.remove(participant)) {
				team.removeEntry(participant.player.name)
				return true
			}
			return false
		}

		override fun isMember(participant: AbstractGame.Participant): Boolean {
			return members.contains(participant)
		}

		override fun getMembers(): Set<AbstractGame.Participant> {
			return Collections.unmodifiableSet(members)
		}

		override fun isExcluded(): Boolean {
			for (member in members) {
				if (!deathManager.isExcluded(member.player)) return false
			}
			return true
		}

		override fun getSpawn(): SpawnLocation {
			return spawn
		}

		override fun setSpawn(spawn: SpawnLocation) {
			this.spawn = spawn
		}

		override fun unregister() {
			scoreboardManager.unregisterTeam(team)
		}

		override fun equals(other: Any?): Boolean {
			return if (other is Team) {
				other.name == name
			} else false
		}

		override fun hashCode(): Int {
			return name.hashCode()
		}


		override fun toString(): String {
			val joiner = StringJoiner("$WHITE, ${ChatColor.GRAY}", ChatColor.GRAY.toString(), "")
			for (member in members) {
				joiner.add(member.player.name)
			}
			return "$displayName §8(${joiner}§8)§f"
		}

	}

	inner class Participant(player: Player) : AbstractGame.Participant(player) {
		override fun toString(): String {
			return player.name
		}
		fun hasTeam(): Boolean {
			return this@TeamGame.hasTeam(this)
		}
		fun getTeam(): Members? {
			return this@TeamGame.getTeam(this)
		}
	}

	interface Winnable: daybreak.abilitywar.game.interfaces.Winnable {
		fun Win(winTeam: Members) {
			if (!isRunning()) return
			Messager.clearChat()
			for (participant in winTeam.getMembers()) {
				SoundLib.UI_TOAST_CHALLENGE_COMPLETE.playSound(participant.player)
				object : SimpleTimer(REVERSE, 8) {
					override fun run(count: Int) {
						FireworkUtil.spawnWinnerFirework(participant.player.eyeLocation)
					}
				}.setPeriod(TICKS, 4).start()
			}
			Bukkit.broadcastMessage("§5§l우승자§f: §d$winTeam.")
			stop()
		}

	}

}