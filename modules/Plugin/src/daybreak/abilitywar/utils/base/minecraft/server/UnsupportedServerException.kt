package daybreak.abilitywar.utils.base.minecraft.server

import java.lang.RuntimeException

class UnsupportedServerException(val supported: Array<ServerType>): RuntimeException()