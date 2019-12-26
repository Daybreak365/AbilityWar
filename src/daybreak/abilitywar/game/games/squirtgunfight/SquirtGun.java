package daybreak.abilitywar.game.games.squirtgunfight;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.event.AbilityRestrictionClearEvent;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.math.LocationUtil;
import daybreak.abilitywar.utils.versioncompat.ServerVersion;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;

@AbilityManifest(Name = "물총", Rank = Rank.SPECIAL, Species = Species.SPECIAL)
public class SquirtGun extends AbilityBase {

	public SquirtGun(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f물 안에서 웅크리면 빠른 속도로 앞으로 나아갑니다."),
				ChatColor.translateAlternateColorCodes('&', "&f활을 쏘면 물총이 나가며, 플레이어를 맞추면 한방에 죽일 수 있습니다. " + Messager.formatCooldown(3)),
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 물폭탄을 터뜨리며, 주변 플레이어들에게 피해를 줍니다."),
				ChatColor.translateAlternateColorCodes('&', Messager.formatCooldown(30)),
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 좌클릭하면 스펀지로 주변의 물을 빨아들입니다. " + Messager.formatCooldown(15)),
				ChatColor.translateAlternateColorCodes('&', "&f시원한 &e여름 &f보내세요!"));
	}

	private final CooldownTimer bombCool = new CooldownTimer(30, "물폭탄");
	private final CooldownTimer spongeCool = new CooldownTimer(15, "스펀지");
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		if(mt.equals(MaterialType.IRON_INGOT)) {
			if(ct.equals(ClickType.RIGHT_CLICK)) {
				if(!bombCool.isCooldown()) {
					Location center = getPlayer().getLocation();
					for(int i = 2; i > 0; i--)
					for(Location l : LocationUtil.getSphere(center, i, 40)) {
						l.getBlock().setType(Material.WATER);
					}
					for(Player p : LocationUtil.getNearbyPlayers(center, 5, 5)) {
						if(!p.equals(getPlayer())) {
							p.damage(20, getPlayer());
						}
					}
					
					SoundLib.ENTITY_PLAYER_SPLASH.playSound(getPlayer());
					
					bombCool.startTimer();
				}
			} else {
				if(!spongeCool.isCooldown()) {
					Location center = getPlayer().getLocation();
					for(int i = 10; i > 0; i--)
					for(Location l : LocationUtil.getSphere(center, i, 40)) {
						if(l.getBlock().getType().equals(Material.WATER) || (ServerVersion.getVersion() < 13 && l.getBlock().getType().equals(Material.valueOf("STATIONARY_WATER")))) {
							l.getBlock().setType(Material.AIR);
						}
					}
					
					SoundLib.ENTITY_PLAYER_SPLASH.playSound(getPlayer());
					
					spongeCool.startTimer();
				}
			}
		}
		return false;
	}

	private final CooldownTimer gunCool = new CooldownTimer(3, "물총");
	
	private final ArrayList<Arrow> arrows = new ArrayList<>();
	
	private final Timer passive = new Timer() {
		@Override
		protected void onProcess(int count) {
			for(Arrow a : arrows) {
				ParticleLib.DRIP_WATER.spawnParticle(a.getLocation(), 10, 1, 1, 1);
			}
			PotionEffects.NIGHT_VISION.addPotionEffect(getPlayer(), 400, 0, true);
		}
	}.setPeriod(3);
	
	@SubscribeEvent
	public void onProjectileLaunch(ProjectileLaunchEvent e) {
		if(e.getEntity().getShooter().equals(getPlayer()) && e.getEntity() instanceof Arrow) {
			Arrow a = (Arrow) e.getEntity();
			arrows.add(a);
		}
	}
	
	@SubscribeEvent
	public void onProjectileHit(ProjectileHitEvent e) {
		if(e.getEntity() instanceof Arrow) {
			arrows.remove(e.getEntity());
			if(e.getEntity().getShooter().equals(getPlayer())) {
				if(!gunCool.isCooldown()) {
					if(e.getHitEntity() != null && e.getHitEntity() instanceof Damageable) {
						((Damageable) e.getHitEntity()).damage(200, getPlayer());
					}
					SoundLib.ENTITY_PLAYER_SPLASH.playSound(getPlayer());
					Location center = e.getHitEntity() != null ? e.getHitEntity().getLocation() : e.getHitBlock().getLocation();
					for(Location l : LocationUtil.getRandomLocations(center, 10, 20)) {
						l.getBlock().setType(Material.WATER);
					}
					
					gunCool.startTimer();
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerMove(PlayerMoveEvent e) {
		if(e.getPlayer().equals(getPlayer())
				&& (e.getFrom().getBlock().getType().equals(Material.WATER) || (ServerVersion.getVersion() < 13 && e.getTo().getBlock().getType().equals(Material.valueOf("STATIONARY_WATER")))) 
						& getPlayer().isSneaking()) {
			getPlayer().setVelocity(getPlayer().getLocation().getDirection().multiply(1.3));
		}
	}
	
	@SubscribeEvent
	public void onEntityDamage(EntityDamageEvent e) {
		if(e.getEntity().equals(getPlayer()) && e.getCause().equals(DamageCause.FALL)) {
			e.setDamage(e.getDamage() / 5);
		}
	}
	
	@Override
	public void TargetSkill(MaterialType mt, LivingEntity entity) {}

	@SubscribeEvent(onlyRelevant = true)
	public void onRestrictionClear(AbilityRestrictionClearEvent e) {
		passive.startTimer();
	}

}
