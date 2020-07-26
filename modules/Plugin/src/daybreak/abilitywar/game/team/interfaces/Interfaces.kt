package daybreak.abilitywar.game.team.interfaces

import daybreak.abilitywar.game.AbstractGame.Participant
import daybreak.abilitywar.game.interfaces.IGame

interface Teamable: IGame {

	fun hasTeam(participant: Participant): Boolean
	fun getTeam(participant: Participant): Members?
	fun setTeam(participant: Participant, nullableTeam: Members?)
	fun teamExists(name: String): Boolean
	fun getTeam(name: String): Members?
	fun getTeams(): Collection<Members>
	@Throws(IllegalStateException::class, IllegalArgumentException::class)
	fun newTeam(name: String, displayName: String): Members
	fun removeTeam(team: Members)

}

interface Members {
	val name: String
	val displayName: String
	fun addMember(participant: Participant): Boolean
	fun removeMember(participant: Participant): Boolean
	fun isMember(participant: Participant): Boolean
	fun getMembers(): Set<Participant>
	fun isExcluded(): Boolean
	fun unregister()
}
