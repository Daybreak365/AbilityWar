package daybreak.abilitywar.utils.base.minecraft.server

class ServerNotSupportedException(val supported: Array<ServerType>): RuntimeException()