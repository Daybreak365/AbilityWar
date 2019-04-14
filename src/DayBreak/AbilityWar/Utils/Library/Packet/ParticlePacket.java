package DayBreak.AbilityWar.Utils.Library.Packet;

import java.lang.reflect.Constructor;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * 파티클 패킷
 * @author DayBreak 새벽
 */
public class ParticlePacket extends AbstractPacket {
	
	private Class<?> enumParticleClass = getNMSClass("EnumParticle");
	private Object enumParticle = null;
	private Location location;
	private float offsetX, offsetY, offsetZ, speed;
	private int count;
	private int[] args;

	public ParticlePacket(String particleName, Location location, float offsetX, float offsetY, float offsetZ, float speed, int count) {
		for(Object obj : enumParticleClass.getEnumConstants()) {
			if(obj.toString().equalsIgnoreCase(particleName)) {
				enumParticle = obj;
				break;
			}
		}
		this.location = location;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.offsetZ = offsetZ;
		this.speed = speed;
		this.count = count;
	}
	
	public ParticlePacket(String particleName, Location location, float offsetX, float offsetY, float offsetZ, int count) {
		for(Object obj : enumParticleClass.getEnumConstants()) {
			if(obj.toString().equalsIgnoreCase(particleName)) {
				enumParticle = obj;
				break;
			}
		}
		this.location = location;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.offsetZ = offsetZ;
		this.speed = 0;
		this.count = count;
	}
	
	public ParticlePacket(String particleName, Location location, float offsetX, float offsetY, float offsetZ, float speed, int count, int... args) {
		for(Object obj : enumParticleClass.getEnumConstants()) {
			if(obj.toString().equalsIgnoreCase(particleName)) {
				enumParticle = obj;
				break;
			}
		}
		this.location = location;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.offsetZ = offsetZ;
		this.speed = speed;
		this.count = count;
		this.args = args;
	}
	
	@Override
	public void Send(Player p) {
		try {
			if(enumParticle != null) {
				Constructor<?> Constructor = getNMSClass("PacketPlayOutWorldParticles").getConstructor(
						enumParticleClass, boolean.class, float.class, float.class, float.class, float.class, float.class,
						float.class, float.class, int.class, int[].class);
				Object ParticlePacket = Constructor.newInstance(
						enumParticle,
						true,
						(float) location.getX(), 
						(float) location.getY(),
						(float) location.getZ(),
						offsetX, offsetY, offsetZ, speed, count, args);
				sendPacket(p, ParticlePacket);
			}
		} catch(Exception ex) {}
	}
	
}
