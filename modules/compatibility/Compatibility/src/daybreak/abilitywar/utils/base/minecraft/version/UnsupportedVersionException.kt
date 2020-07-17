package daybreak.abilitywar.utils.base.minecraft.version

class UnsupportedVersionException : RuntimeException {
	constructor() : super()
	constructor(version: IVersion) : super("지원되지 않는 버전: v1_${version.version}_R${version.release}")
}