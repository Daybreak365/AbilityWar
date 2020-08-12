package daybreak.abilitywar.game.list.murdermystery.ability.jobs.innocent;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.list.murdermystery.Items;
import daybreak.abilitywar.game.list.murdermystery.MurderMystery;
import daybreak.abilitywar.game.list.murdermystery.MurderMystery.ArrowKillEvent;
import daybreak.abilitywar.game.list.murdermystery.ability.AbstractInnocent;
import daybreak.abilitywar.game.list.murdermystery.ability.AbstractMurderer.MurderEvent;
import daybreak.abilitywar.game.manager.object.DeathManager;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
import java.util.function.Predicate;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

@AbilityManifest(name = "시민: 의사", rank = Rank.SPECIAL, species = Species.HUMAN, explain = {
		"금 우클릭으로 금 8개를 소모해 활과 화살을 얻을 수 있습니다.",
		"금 좌클릭으로 금 6개를 소모해 2.5초간 y에 상관 없이 주변 5칸 이내에서",
		"플레이어가 죽지 못하게 합니다."
})
public class Doctor extends AbstractInnocent {

	public Doctor(Participant participant) {
		super(participant);
	}

	private final Predicate<Entity> predicate = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			if (entity.equals(getPlayer())) return false;
			return (!(entity instanceof Player)) || (getGame().isParticipating(entity.getUniqueId())
					&& (!(getGame() instanceof DeathManager.Handler) || !((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
					&& getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue());
		}
	};

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			PlayerInventory inventory = getPlayer().getInventory();
			ItemStack two = inventory.getItem(2), three = inventory.getItem(3);
			inventory.clear();
			inventory.setItem(2, two);
			inventory.setItem(3, three);
			getPlayer().getInventory().setHeldItemSlot(0);
			((MurderMystery) getGame()).updateGold(getParticipant());
			NMS.sendTitle(getPlayer(), "§e직업§f: §a의사", "§f모든 §e시민§f을 구하세요!", 10, 80, 10);
			new AbilityTimer(1) {
				@Override
				protected void run(int count) {
				}

				@Override
				protected void onEnd() {
					NMS.clearTitle(getPlayer());
				}
			}.setInitialDelay(TimeUnit.SECONDS, 5).start();
		}
	}

	@SubscribeEvent
	private void onArrowKill(final ArrowKillEvent e) {
		if (duration.isRunning() && e.getTarget().getPlayer().getLocation().distanceSquared(getPlayer().getLocation()) <= 25) {
			e.setCancelled(true);
			new BukkitRunnable() {
				@Override
				public void run() {
					NMS.broadcastEntityEffect(e.getTarget().getPlayer(), (byte) 35);
				}
			}.runTaskLater(AbilityWar.getPlugin(), 3L);
		}
	}

	@SubscribeEvent
	private void onArrowKill(final MurderEvent e) {
		if (duration.isRunning() && e.getTarget().getPlayer().getLocation().distanceSquared(getPlayer().getLocation()) <= 25) {
			e.setCancelled(true);
			new BukkitRunnable() {
				@Override
				public void run() {
					NMS.broadcastEntityEffect(e.getTarget().getPlayer(), (byte) 35);
				}
			}.runTaskLater(AbilityWar.getPlugin(), 3L);
		}
	}

	private static final Circle CIRCLE = Circle.of(5, 60);
	private static final RGB color = RGB.of(0, 0, 0);

	private final Duration duration = new Duration(15) {
		@Override
		protected void onDurationProcess(int count) {
			for (final Location loc : CIRCLE.toLocations(getPlayer().getLocation()).floor(getPlayer().getLocation().getY())) {
				ParticleLib.REDSTONE.spawnParticle(loc, color);
			}
		}
	}.setPeriod(TimeUnit.TICKS, 4);

	@SubscribeEvent(onlyRelevant = true)
	private void onInteract(PlayerInteractEvent e) {
		if (Items.isGold(e.getItem())) {
			if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				final MurderMystery murderMystery = (MurderMystery) getGame();
				if (murderMystery.consumeGold(getParticipant(), 8)) {
					if (!addArrow()) {
						murderMystery.addGold(getParticipant());
					} else {
						if (!hasBow()) {
							getPlayer().getInventory().setItem(2, Items.NORMAL_BOW.getStack());
						}
					}
				}
			} else if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
				final MurderMystery murderMystery = (MurderMystery) getGame();
				if (!duration.isDuration() && murderMystery.consumeGold(getParticipant(), 6)) {
					getPlayer().sendMessage("§a의술 §f능력을 사용했습니다.");
					duration.start();
				}
			}
		}
	}

}
