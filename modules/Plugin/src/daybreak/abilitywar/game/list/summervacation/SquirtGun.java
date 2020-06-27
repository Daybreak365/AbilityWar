package daybreak.abilitywar.game.list.summervacation;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.Scheduled;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerMoveEvent;

@AbilityManifest(name = "물총", rank = Rank.SPECIAL, species = Species.SPECIAL, explain = {
		"물 안에서 웅크리면 빠른 속도로 앞으로 나아갑니다.",
		"활을 쏘면 물총이 나가며, 플레이어를 맞추면 한방에 죽일 수 있습니다. $[THREE_SECONDS_COOL]",
		"철괴를 우클릭하면 물폭탄을 터뜨리며, 주변 플레이어들에게 피해를 줍니다.",
		"$[THIRTY_SECONDS_COOL]",
		"철괴를 좌클릭하면 스펀지로 주변의 물을 빨아들입니다. $[FIFTEEN_SECONDS_COOL]",
		"시원한 §e여름 §f보내세요!"
})
public class SquirtGun extends AbilityBase implements ActiveHandler {

	private static final String THREE_SECONDS_COOL = Formatter.formatCooldown(3), THIRTY_SECONDS_COOL = Formatter.formatCooldown(30), FIFTEEN_SECONDS_COOL = Formatter.formatCooldown(15);

	public SquirtGun(Participant participant) {
		super(participant);
	}

	private final CooldownTimer bombCool = new CooldownTimer(30, "물폭탄");
	private final CooldownTimer spongeCool = new CooldownTimer(15, "스펀지");

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT)) {
			if (clickType.equals(ClickType.RIGHT_CLICK)) {
				if (!bombCool.isCooldown()) {
					Location center = getPlayer().getLocation();
					for (Block b : LocationUtil.getBlocks3D(center, 2, false, true)) {
						b.setType(Material.WATER);
					}
					for (Player p : LocationUtil.getNearbyPlayers(center, 5, 5)) {
						if (!p.equals(getPlayer())) {
							p.damage(20, getPlayer());
						}
					}

					SoundLib.ENTITY_PLAYER_SPLASH.playSound(getPlayer());

					bombCool.start();
				}
			} else {
				if (!spongeCool.isCooldown()) {
					Location center = getPlayer().getLocation();
					for (Block b : LocationUtil.getBlocks3D(center, 9, false, true)) {
						if (b.getType().equals(Material.WATER) || (ServerVersion.getVersionNumber() < 13 && b.getType().equals(Material.valueOf("STATIONARY_WATER")))) {
							b.setType(Material.AIR);
						}
					}

					SoundLib.ENTITY_PLAYER_SPLASH.playSound(getPlayer());

					spongeCool.start();
				}
			}
		}
		return false;
	}

	private final CooldownTimer gunCool = new CooldownTimer(3, "물총");

	private final List<Arrow> arrows = new LinkedList<>();

	@Scheduled
	private final Timer passive = new Timer() {
		@Override
		protected void run(int count) {
			for (Arrow arrow : arrows) {
				ParticleLib.DRIP_WATER.spawnParticle(arrow.getLocation(), 10, 1, 1, 1);
			}
			PotionEffects.NIGHT_VISION.addPotionEffect(getPlayer(), 400, 0, true);
		}
	}.setPeriod(TimeUnit.TICKS, 3);

	@SubscribeEvent
	public void onProjectileLaunch(ProjectileLaunchEvent e) {
		if (getPlayer().equals(e.getEntity().getShooter()) && e.getEntity() instanceof Arrow) {
			Arrow arrow = (Arrow) e.getEntity();
			arrows.add(arrow);
		}
	}

	@SubscribeEvent
	public void onProjectileHit(ProjectileHitEvent e) {
		if (e.getEntity() instanceof Arrow) {
			arrows.remove(e.getEntity());
			if (getPlayer().equals(e.getEntity().getShooter())) {
				if (!gunCool.isCooldown()) {
					if (e.getHitEntity() != null && e.getHitEntity() instanceof Damageable) {
						((Damageable) e.getHitEntity()).damage(200, getPlayer());
					}
					SoundLib.ENTITY_PLAYER_SPLASH.playSound(getPlayer());
					Location center = e.getHitEntity() != null ? e.getHitEntity().getLocation() : e.getHitBlock().getLocation();
					for (Location l : LocationUtil.getRandomLocations(center, 10, 20)) {
						l.getBlock().setType(Material.WATER);
					}

					gunCool.start();
				}
			}
		}
	}

	@SubscribeEvent
	public void onPlayerMove(PlayerMoveEvent e) {
		if (e.getPlayer().equals(getPlayer())
				&& (e.getFrom().getBlock().getType().equals(Material.WATER) || (ServerVersion.getVersionNumber() < 13 && e.getTo().getBlock().getType().equals(Material.valueOf("STATIONARY_WATER"))))
				& getPlayer().isSneaking()) {
			getPlayer().setVelocity(getPlayer().getLocation().getDirection().multiply(1.3));
		}
	}

	@SubscribeEvent
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity().equals(getPlayer()) && e.getCause().equals(DamageCause.FALL)) {
			e.setDamage(e.getDamage() / 5);
		}
	}

}
