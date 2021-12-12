package daybreak.abilitywar.game.list.mix.synergy.list;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.AbstractGame.GameTimer;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.event.participant.ParticipantDeathEvent;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;
import daybreak.abilitywar.game.manager.effect.EvilSpirit;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.base.minecraft.item.Skulls;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.inventory.EntityEquipment;

import java.util.function.Predicate;

@AbilityManifest(name = "원한", rank = Rank.B, species = Species.OTHERS, explain = {
		"죽을 때 나를 죽인 플레이어의 저주 인형을 내 위치에 만들어냅니다. 저주 인형은",
		"대상이 죽기 전까지 지속되며, 저주 인형이 대미지를 입을 경우 대미지의 일부가",
		"상대에게 전이되고 약령 효과를 6초간 부여합니다. 대상의 체력이 적을수록 더욱",
		"큰 대미지를 입힐 수 있습니다.",
		"§7악령 효과§f: 간헐적으로 시야가 차단되고 환청이 들립니다. 이 효과를 가지고 있는",
		"플레이어를 타격한 대상에게도 이 효과가 부여됩니다."
})
public class Grudge extends Synergy {

	public Grudge(Participant participant) {
		super(participant);
	}

	private final Predicate<Entity> predicate = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			if (entity.equals(getPlayer())) return false;
			if (entity instanceof Player) {
				if (!getGame().isParticipating(entity.getUniqueId())
						|| (getGame() instanceof DeathManager.Handler && ((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
						|| !getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue()) {
					return false;
				}
				if (getGame() instanceof Teamable) {
					final Teamable teamGame = (Teamable) getGame();
					final Participant entityParticipant = teamGame.getParticipant(entity.getUniqueId()), participant = getParticipant();
					return !teamGame.hasTeam(entityParticipant) || !teamGame.hasTeam(participant) || (!teamGame.getTeam(entityParticipant).equals(teamGame.getTeam(participant)));
				}
			}
			return true;
		}
	};

	@SubscribeEvent
	private void onPlayerDeath(final ParticipantDeathEvent e) {
		if (!getParticipant().equals(e.getParticipant())) return;
		final Player killer = getPlayer().getKiller();
		if (killer != null && predicate.test(killer)) {
			new VoodooDoll(this, getGame().getParticipant(killer)).start();
		}
	}

	private static class VoodooDoll extends GameTimer implements Listener {

		private final Participant target;
		private final ArmorStand doll;

		private VoodooDoll(final Grudge grudge, final Participant target) {
			grudge.getGame().super(TaskType.INFINITE, -1);
			setPeriod(TimeUnit.TICKS, 2);
			this.target = target;
			final Player targetPlayer = target.getPlayer();
			this.doll = targetPlayer.getWorld().spawn(grudge.getPlayer().getLocation(), ArmorStand.class);
			if (ServerVersion.getVersion() >= 10 && ServerVersion.getVersion() < 15)
				doll.setInvulnerable(true);
			doll.setCustomName(targetPlayer.getName());
			doll.setCustomNameVisible(true);
			doll.setBasePlate(false);
			doll.setArms(true);
			doll.setGravity(false);
			final EntityEquipment equipment = doll.getEquipment();
			equipment.setArmorContents(targetPlayer.getInventory().getArmorContents());
			equipment.setHelmet(Skulls.createSkull(targetPlayer));
		}

		@Override
		protected void onStart() {
			Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		}

		@Override
		protected void run(int count) {
			if (count % 10 == 0) {
				showHelix(doll.getLocation());
			}
			final Location location = doll.getLocation();
			location.setYaw(location.getYaw() + 5);
			doll.teleport(location);
		}

		@Override
		protected void onEnd() {
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			doll.remove();
			HandlerList.unregisterAll(this);
		}

		@EventHandler
		private void onPlayerDeath(final PlayerDeathEvent e) {
			if (e.getEntity().getUniqueId().equals(target.getPlayer().getUniqueId())) {
				stop(false);
			}
		}

		@EventHandler
		private void onEntityDamageByEntity(final EntityDamageByEntityEvent e) {
			if (e.getEntity().equals(doll)) {
				e.setCancelled(true);
				final Player targetPlayer = target.getPlayer();
				targetPlayer.damage(e.getDamage() * (2.3 * (1 / Math.max(targetPlayer.getHealth(), 0.01))), doll);
				if (e.getDamager() instanceof Player) {
					SoundLib.ENTITY_PLAYER_ATTACK_SWEEP.playSound((Player) e.getDamager());
				}
				EvilSpirit.apply(target, TimeUnit.SECONDS, 6);
			}
		}

		@EventHandler
		private void onEntityDamageByBlock(final EntityDamageByBlockEvent e) {
			onEntityDamage(e);
		}

		@EventHandler
		private void onEntityDamage(final EntityDamageEvent e) {
			if (e.getEntity().equals(doll)) {
				e.setCancelled(true);
				final Player targetPlayer = target.getPlayer();
				targetPlayer.damage(e.getDamage() * (2.3 * (1 / Math.max(targetPlayer.getHealth(), 0.01))), doll);
				EvilSpirit.apply(target, TimeUnit.SECONDS, 6);
			}
		}

		@EventHandler
		private void onPlayerArmorStandManipulate(final PlayerArmorStandManipulateEvent e) {
			if (e.getRightClicked().equals(doll)) e.setCancelled(true);
		}

		private static final RGB BLACK = RGB.of(1, 1, 1);
		private static final int particleCount = 20;
		private static final double yDiff = 0.6 / particleCount;
		private static final Circle helixCircle = Circle.of(0.5, particleCount);

		private void showHelix(Location target) {
			getGame().new GameTimer(TaskType.REVERSE, (particleCount * 3) / 2) {
				int count = 0;
				@Override
				protected void run(int a) {
					for (int i = 0; i < 2; i++) {
						ParticleLib.REDSTONE.spawnParticle(target.clone().add(helixCircle.get(count % 20)).add(0, count * yDiff, 0), BLACK);
						count++;
					}
				}
			}.setPeriod(TimeUnit.TICKS, 1).start();
		}
	}

}
