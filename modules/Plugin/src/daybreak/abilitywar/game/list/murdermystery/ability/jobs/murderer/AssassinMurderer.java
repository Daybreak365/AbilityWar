package daybreak.abilitywar.game.list.murdermystery.ability.jobs.murderer;

import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.list.murdermystery.Items;
import daybreak.abilitywar.game.list.murdermystery.MurderMystery;
import daybreak.abilitywar.game.list.murdermystery.ability.AbstractMurderer;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil.Josa;
import daybreak.abilitywar.utils.base.math.FastMath;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

@AbilityManifest(name = "머더: 암살자", rank = Rank.SPECIAL, species = Species.HUMAN, explain = {
		"모든 시민을 죽이세요!",
		"살인자의 검으로 상대를 죽일 경우 5초간 투명 효과를 받습니다.",
		"금 우클릭으로 금 8개를 소모해 활과 화살을 얻을 수 있습니다.",
		"암살자가 시민을 죽일 때 킬 메시지가 뜨지 않습니다.",
		"웅크리면 투명해지고, 웅크리지 않으면 불투명해집니다."
})
public class AssassinMurderer extends AbstractMurderer {

	private static final int TELEPORT_RADIUS = 6;

	public AssassinMurderer(Participant participant) {
		super(participant);
	}

	private final Cooldown cooldown = new Cooldown(20);
	private final Duration skill = new Duration(7, cooldown) {
		@Override
		protected void onDurationStart() {
			NMS.setInvisible(getPlayer(), true);
			final Location center = getPlayer().getLocation();
			final double radians = Math.toRadians(random.nextDouble() * 360);
			getPlayer().teleport(LocationUtil.floorY(new Location(
					center.getWorld(),
					center.getX() + (random.nextDouble() * TELEPORT_RADIUS * FastMath.cos(radians)),
					center.getY(),
					center.getZ() + (random.nextDouble() * TELEPORT_RADIUS * FastMath.sin(radians))
			)));
		}
		@Override
		protected void onDurationProcess(int count) {
			NMS.setInvisible(getPlayer(), true);
		}
		@Override
		protected void onDurationEnd() {
			NMS.setInvisible(getPlayer(), false);
		}
		@Override
		protected void onDurationSilentEnd() {
			NMS.setInvisible(getPlayer(), false);
		}
	};

	@Override
	protected void onUpdate(Update update) {
		super.onUpdate(update);
		if (update == Update.RESTRICTION_CLEAR) {
			NMS.sendTitle(getPlayer(), "§e직업§f: §5암살자", "§f모든 §a시민§f과 §5탐정§f을 조용히 죽이세요.", 10, 80, 10);
			new AbilityTimer(1) {
				@Override
				protected void onEnd() {
					NMS.clearTitle(getPlayer());
				}
			}.setInitialDelay(TimeUnit.SECONDS, 5).start();
		} else if (update == Update.ABILITY_DESTROY) {
			NMS.setInvisible(getPlayer(), false);
		}
	}

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
			}
		}
	}

	@SubscribeEvent(eventPriority = EventPriority.HIGHEST)
	private void onPlayerDeath(final PlayerDeathEvent e) {
		final Player dead = e.getEntity();
		if (getPlayer().equals(dead.getKiller())) {
			e.setDeathMessage(null);
			for (Participant participant : getGame().getParticipants()) {
				if (participant.getAbility() instanceof AbstractMurderer) {
					participant.getPlayer().sendMessage("§8" + dead.getName() + "§7" + KoreanUtil.getJosa(dead.getName(), Josa.이가) + " 죽었습니다.");
				}
			}
		}
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onToggleSneak(final PlayerToggleSneakEvent e) {
		if (e.isSneaking() && !skill.isRunning() && !cooldown.isRunning()) {
			ParticleLib.DRIP_LAVA.spawnParticle(getPlayer().getLocation().clone().add(0, 1, 0), 0.15, 0.15, 0.15, 100, 0);
			skill.start();
			getPlayer().getInventory().setArmorContents(null);
			NMS.setArrowsInBody(getPlayer(), 0);
			SoundLib.ENTITY_SILVERFISH_AMBIENT.playSound(getPlayer().getLocation(), 0.5f, 1f);
		} else if (skill.isRunning()) {
			skill.stop(false);
		}
	}

}
