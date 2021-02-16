package daybreak.abilitywar.utils.base.minecraft.nms

import daybreak.abilitywar.utils.base.minecraft.SkinInfo
import daybreak.abilitywar.utils.base.minecraft.boundary.EntityBoundingBox
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion
import daybreak.abilitywar.utils.base.minecraft.version.VersionNotSupportedException
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.WorldBorder
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Arrow
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

class NMS private constructor() {
	companion object INSTANCE : INMS {
		private val INSTANCE: INMS = try {
			Class.forName("daybreak.abilitywar.utils.base.minecraft.nms." + ServerVersion.name + ".NMSImpl").asSubclass(INMS::class.java).getConstructor().newInstance()
		} catch (e: Exception) {
			throw VersionNotSupportedException()
		}

		@JvmStatic
		override fun respawn(player: Player) {
			INSTANCE.respawn(player)
		}

		@JvmStatic
		override fun clearTitle(player: Player) {
			INSTANCE.clearTitle(player)
		}

		@JvmStatic
		override fun sendTitle(player: Player, title: String, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int) {
			INSTANCE.sendTitle(player, title, subtitle, fadeIn, stay, fadeOut)
		}

		@JvmStatic
		override fun sendActionbar(player: Player, string: String, fadeIn: Int, stay: Int, fadeOut: Int) {
			INSTANCE.sendActionbar(player, string, fadeIn, stay, fadeOut)
		}

		@JvmStatic
		override fun getAttackCooldown(player: Player): Float {
			return INSTANCE.getAttackCooldown(player)
		}

		@JvmStatic
		override fun rotateHead(receiver: Player, entity: Entity, yaw: Float, pitch: Float) {
			INSTANCE.rotateHead(receiver, entity, yaw, pitch)
		}

		@JvmStatic
		override fun newHologram(world: World, x: Double, y: Double, z: Double, text: String): IHologram {
			return INSTANCE.newHologram(world, x, y, z, text)
		}

		@JvmStatic
		override fun newHologram(world: World, x: Double, y: Double, z: Double): IHologram {
			return INSTANCE.newHologram(world, x, y, z)
		}

		@JvmStatic
		fun newHologram(world: World, location: Location, text: String): IHologram {
			return INSTANCE.newHologram(world, location.x, location.y, location.z, text)
		}

		@JvmStatic
		fun newHologram(world: World, location: Location): IHologram {
			return INSTANCE.newHologram(world, location.x, location.y, location.z)
		}

		@JvmStatic
		override fun createDummy(location: Location): IDummy {
			return INSTANCE.createDummy(location)
		}

		@JvmStatic
		override fun createDummy(location: Location, skinInfo: SkinInfo): IDummy {
			return INSTANCE.createDummy(location, skinInfo)
		}

		@JvmStatic
		override fun getAbsorptionHearts(livingEntity: LivingEntity): Float {
			return INSTANCE.getAbsorptionHearts(livingEntity)
		}

		@JvmStatic
		override fun setAbsorptionHearts(livingEntity: LivingEntity, absorptionHearts: Float) {
			INSTANCE.setAbsorptionHearts(livingEntity, absorptionHearts)
		}

		@JvmStatic
		override fun broadcastEntityEffect(entity: Entity, status: Byte) {
			INSTANCE.broadcastEntityEffect(entity, status)
		}

		@JvmStatic
		override fun setLocation(entity: Entity, x: Double, y: Double, z: Double, yaw: Float, pitch: Float) {
			INSTANCE.setLocation(entity, x, y, z, yaw, pitch)
		}

		@JvmStatic
		override fun removeBoundingBox(armorStand: ArmorStand) {
			INSTANCE.removeBoundingBox(armorStand)
		}

		@JvmStatic
		override fun setArrowsInBody(player: Player, count: Int) {
			INSTANCE.setArrowsInBody(player, count)
		}

		@JvmStatic
		override fun isInvisible(entity: Entity): Boolean {
			return INSTANCE.isInvisible(entity)
		}

		@JvmStatic
		override fun setInvisible(entity: Entity, invisible: Boolean) {
			INSTANCE.setInvisible(entity, invisible)
		}

		@JvmStatic
		override fun setCooldown(player: Player, material: Material, ticks: Int) {
			INSTANCE.setCooldown(player, material, ticks)
		}

		@JvmStatic
		override fun hasCooldown(player: Player, material: Material): Boolean {
			return INSTANCE.hasCooldown(player, material)
		}

		@JvmStatic
		override fun getCooldown(player: Player, material: Material): Int {
			return INSTANCE.getCooldown(player, material)
		}

		@JvmStatic
		override fun fakeCollect(entity: Entity, item: Item) {
			INSTANCE.fakeCollect(entity, item)
		}

		@JvmStatic
		override fun clearActiveItem(livingEntity: LivingEntity) {
			INSTANCE.clearActiveItem(livingEntity)
		}

		@JvmStatic
		override fun swingHand(livingEntity: LivingEntity, hand: Hand) {
			INSTANCE.swingHand(livingEntity, hand)
		}

		@JvmStatic
		override fun getBoundingBox(entity: Entity): EntityBoundingBox {
			return INSTANCE.getBoundingBox(entity)
		}

		@JvmStatic
		override fun setCamera(receiver: Player, entity: Entity) {
			INSTANCE.setCamera(receiver, entity)
		}

		@JvmStatic
		override fun createWorldBorder(world: World): IWorldBorder {
			return INSTANCE.createWorldBorder(world)
		}

		@JvmStatic
		override fun createWorldBorder(bukkit: WorldBorder): IWorldBorder {
			return INSTANCE.createWorldBorder(bukkit)
		}

		@JvmStatic
		override fun setWorldBorder(receiver: Player, worldBorder: IWorldBorder) {
			INSTANCE.setWorldBorder(receiver, worldBorder)
		}

		@JvmStatic
		override fun resetWorldBorder(receiver: Player) {
			INSTANCE.resetWorldBorder(receiver)
		}

		@JvmStatic
		override fun setInGround(arrow: Arrow, inGround: Boolean) {
			INSTANCE.setInGround(arrow, inGround)
		}
	}
}