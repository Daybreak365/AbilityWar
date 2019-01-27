package Marlang.AbilityWar.Utils.Library;

import org.bukkit.Location;
import org.bukkit.Particle;

import Marlang.AbilityWar.Config.AbilityWarSettings;

/**
 * 파티클 라이브러리
 * @author _Marlang 말랑
 */
public class ParticleLib {
	
	public static Particles BARRIER = new Particles("BARRIER");
	public static Particles BLOCK_CRACK = new Particles("BLOCK_CRACK");
	public static Particles BLOCK_DUST = new Particles("BLOCK_DUST");
	public static Particles CLOUD = new Particles("CLOUD");
	public static Particles CRIT = new Particles("CRIT");
	public static Particles CRIT_MAGIC = new Particles("CRIT_MAGIC");
	public static Particles DAMAGE_INDICATOR = new Particles("DAMAGE_INDICATOR");
	public static Particles DRAGON_BREATH = new Particles("DRAGON_BREATH");
	public static Particles DRIP_LAVA = new Particles("DRIP_LAVA");
	public static Particles DRIP_WATER = new Particles("DRIP_WATER");
	public static Particles ENCHANTMENT_TABLE = new Particles("ENCHANTMENT_TABLE");
	public static Particles END_ROD = new Particles("END_ROD");
	public static Particles EXPLOSION_HUGE = new Particles("EXPLOSION_HUGE");
	public static Particles EXPLOSION_LARGE = new Particles("EXPLOSION_LARGE");
	public static Particles EXPLOSION_NORMAL = new Particles("EXPLOSION_NORMAL");
	public static Particles FALLING_DUST = new Particles("FALLING_DUST");
	public static Particles FIREWORKS_SPARK = new Particles("FIREWORKS_SPARK");
	public static Particles FLAME = new Particles("FLAME");
	public static Particles FOOTSTEP = new Particles("FOOTSTEP");
	public static Particles HEART = new Particles("HEART");
	public static Particles ITEM_CRACK = new Particles("ITEM_CRACK");
	public static Particles ITEM_TAKE = new Particles("ITEM_TAKE");
	public static Particles LAVA = new Particles("LAVA");
	public static Particles MOB_APPEARANCE = new Particles("MOB_APPEARANCE");
	public static Particles NOTE = new Particles("NOTE");
	public static Particles PORTAL = new Particles("PORTAL");
	public static Particles REDSTONE = new Particles("REDSTONE");
	public static Particles SLIME = new Particles("SLIME");
	public static Particles SMOKE_LARGE = new Particles("SMOKE_LARGE");
	public static Particles SMOKE_NORMAL = new Particles("SMOKE_NORMAL");
	public static Particles SNOW_SHOVEL = new Particles("SNOW_SHOVEL");
	public static Particles SNOWBALL = new Particles("SNOWBALL");
	public static Particles SPELL = new Particles("SPELL");
	public static Particles SPELL_INSTANT = new Particles("SPELL_INSTANT");
	public static Particles SPELL_MOB = new Particles("SPELL_MOB");
	public static Particles SPELL_MOB_AMBIENT = new Particles("SPELL_MOB_AMBIENT");
	public static Particles SPELL_WITCH = new Particles("SPELL_WITCH");
	public static Particles SPIT = new Particles("SPIT");
	public static Particles SUSPENDED = new Particles("SUSPENDED");
	public static Particles SUSPENDED_DEPTH = new Particles("SUSPENDED_DEPTH");
	public static Particles SWEEP_ATTACK = new Particles("SWEEP_ATTACK");
	public static Particles TOTEM = new Particles("TOTEM");
	public static Particles TOWN_AURA = new Particles("TOWN_AURA");
	public static Particles VILLAGER_ANGRY = new Particles("VILLAGER_ANGRY");
	public static Particles VILLAGER_HAPPY = new Particles("VILLAGER_HAPPY");
	public static Particles WATER_BUBBLE = new Particles("WATER_BUBBLE");
	public static Particles WATER_DROP = new Particles("WATER_DROP");
	public static Particles WATER_SPLASH = new Particles("WATER_SPLASH");
	public static Particles WATER_WAKE = new Particles("WATER_WAKE");

	public static class Particles {

		String particleName;
		Particle particle = null;

		public Particles(String particleName) {
			this.particleName = particleName;
			
			for(Particle p : Particle.values()) {
				if(p.toString().equalsIgnoreCase(getName())) {
					particle = p;
				}
			}
		}
		
		public String getName() {
			return particleName;
		}
		
		private Particle getParticle() {
			return particle;
		}

		public void spawnParticle(Location l, int Count, double offsetX, double offsetY, double offsetZ) {
			if(AbilityWarSettings.getVisualEffect()) {
				Particle p = getParticle();
				if(p != null) {
					l.getWorld().spawnParticle(p, l, Count, offsetX, offsetY, offsetZ);
				}
			}
		}
		
	}

}
