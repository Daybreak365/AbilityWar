package daybreak.abilitywar.ability.event

import daybreak.abilitywar.ability.AbilityBase
import daybreak.abilitywar.game.event.participant.ParticipantEvent

abstract class AbilityEvent protected constructor(val ability: AbilityBase) : ParticipantEvent(ability.participant)