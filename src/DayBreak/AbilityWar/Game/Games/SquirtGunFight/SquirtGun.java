package DayBreak.AbilityWar.Game.Games.SquirtGunFight;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Ability.AbilityManifest;
import DayBreak.AbilityWar.Ability.AbilityManifest.Rank;
import DayBreak.AbilityWar.Ability.AbilityManifest.Species;
import DayBreak.AbilityWar.Ability.SubscribeEvent;
import DayBreak.AbilityWar.Ability.Timer.CooldownTimer;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame.Participant;
import DayBreak.AbilityWar.Utils.Messager;
import DayBreak.AbilityWar.Utils.Library.EffectLib;
import DayBreak.AbilityWar.Utils.Library.ParticleLib;
import DayBreak.AbilityWar.Utils.Library.SoundLib;
import DayBreak.AbilityWar.Utils.Math.LocationUtil;
import DayBreak.AbilityWar.Utils.Thread.TimerBase;

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

	CooldownTimer bombCool = new CooldownTimer(this, 30, "물폭탄").setActionbarNotice(false);

	CooldownTimer spongeCool = new CooldownTimer(this, 15, "스펀지").setActionbarNotice(false);
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		if(mt.equals(MaterialType.Iron_Ingot)) {
			if(ct.equals(ClickType.RightClick)) {
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
					
					bombCool.StartTimer();
				}
			} else {
				if(!spongeCool.isCooldown()) {
					Location center = getPlayer().getLocation();
					for(int i = 10; i > 0; i--)
					for(Location l : LocationUtil.getSphere(center, i, 40)) {
						if(l.getBlock().getType().equals(Material.WATER) || l.getBlock().getType().equals(Material.STATIONARY_WATER)) {
							l.getBlock().setType(Material.AIR);
						}
					}
					
					SoundLib.ENTITY_PLAYER_SPLASH.playSound(getPlayer());
					
					spongeCool.StartTimer();
				}
			}
		}
		return false;
	}

	CooldownTimer gunCool = new CooldownTimer(this, 3, "물총");
	
	List<Arrow> arrows = new ArrayList<Arrow>();
	
	TimerBase passive = new TimerBase() {
		
		@Override
		protected void onStart() {}
		
		@Override
		protected void onEnd() {}
		
		@Override
		protected void TimerProcess(Integer Seconds) {
			for(Arrow a : arrows) {
				ParticleLib.DRIP_WATER.spawnParticle(a.getLocation(), 10, 1, 1, 1);
			}
			EffectLib.NIGHT_VISION.addPotionEffect(getPlayer(), 400, 0, true);
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
					
					gunCool.StartTimer();
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerMove(PlayerMoveEvent e) {
		if(e.getPlayer().equals(getPlayer()) && (e.getTo().getBlock().getType().equals(Material.WATER) || e.getTo().getBlock().getType().equals(Material.STATIONARY_WATER)) && getPlayer().isSneaking()) {
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
	public void TargetSkill(MaterialType mt, Entity entity) {}

	@Override
	protected void onRestrictClear() {
		passive.StartTimer();
	}

}
