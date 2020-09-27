package daybreak.abilitywar.game.list.summervacation;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.minecraft.damage.Damages;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

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

	private final Cooldown bombCool = new Cooldown(30, "물폭탄");
	private final Cooldown spongeCool = new Cooldown(15, "스펀지");

	private final Predicate<Entity> ONLY_PARTICIPANTS = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			return getGame().isParticipating(entity.getUniqueId())
					&& (!(getGame() instanceof DeathManager.Handler) || !((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
					&& getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue();
		}
	};

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT) {
			if (clickType == ClickType.RIGHT_CLICK) {
				if (!bombCool.isCooldown()) {
					Location center = getPlayer().getLocation();
					for (Block b : LocationUtil.getBlocks3D(center, 2, false, true)) {
						b.setType(Material.WATER);
					}
					for (Player p : LocationUtil.getNearbyEntities(Player.class, center, 5, 5, ONLY_PARTICIPANTS)) {
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
						if (b.getType().equals(Material.WATER) || (ServerVersion.getVersion() < 13 && b.getType().equals(Material.valueOf("STATIONARY_WATER")))) {
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

	private final Cooldown gunCool = new Cooldown(3, "물총");

	private final List<Arrow> arrows = new LinkedList<>();

	private final AbilityTimer passive = new AbilityTimer() {
		@Override
		protected void run(int count) {
			for (final Arrow arrow : arrows) {
				ParticleLib.DRIP_WATER.spawnParticle(arrow.getLocation(), 10, 1, 1, 1);
			}
			PotionEffects.WATER_BREATHING.addPotionEffect(getPlayer(), 400, 0, true);
			PotionEffects.NIGHT_VISION.addPotionEffect(getPlayer(), 400, 0, true);
		}
	}.setPeriod(TimeUnit.TICKS, 3).register();

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			passive.start();
		}
	}

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
					if (e.getHitEntity() instanceof Damageable && !getPlayer().equals(e.getHitEntity())) {
						Damages.damageFixed(e.getHitEntity(), getPlayer(), 100);
					}
					SoundLib.ENTITY_PLAYER_SPLASH.playSound(getPlayer());
					for (Location loc : LocationUtil.getRandomLocations(e.getHitEntity() != null ? e.getHitEntity().getLocation() : e.getHitBlock().getLocation(), 10, 20)) {
						loc.getBlock().setType(Material.WATER);
					}

					gunCool.start();
				}
			}
		}
	}

	@SubscribeEvent
	public void onPlayerMove(PlayerMoveEvent e) {
		if (e.getPlayer().equals(getPlayer())
				&& (e.getFrom().getBlock().getType().equals(Material.WATER) || (ServerVersion.getVersion() < 13 && e.getTo().getBlock().getType().equals(Material.valueOf("STATIONARY_WATER"))))
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
