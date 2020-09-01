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
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.PlayerInventory;

@AbilityManifest(name = "머더: 암살자", rank = Rank.SPECIAL, species = Species.HUMAN, explain = {
		"모든 시민을 죽이세요!",
		"살인자의 검으로 상대를 죽일 경우 5초간 투명 효과를 받습니다.",
		"금 우클릭으로 금 8개를 소모해 활과 화살을 얻을 수 있습니다.",
		"웅크리면 투명해지고, 웅크리지 않으면 불투명해집니다."
})
public class AssassinMurderer extends AbstractMurderer {

	private final AbilityTimer PASSIVE = new AbilityTimer() {
		@Override
		protected void run(int count) {
			if (Items.isMurdererSword(getPlayer().getInventory().getItemInMainHand())) {
				getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.15);
			}
		}
	}.setPeriod(TimeUnit.TICKS, 1).register();

	public AssassinMurderer(Participant participant) {
		super(participant);
	}

	private final Cooldown cooldown = new Cooldown(20);
	private boolean skill = false;

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			PlayerInventory inventory = getPlayer().getInventory();
			final boolean hadSword = Items.isMurdererSword(inventory.getItem(1));
			inventory.clear();
			getPlayer().getInventory().setHeldItemSlot(0);
			((MurderMystery) getGame()).updateGold(getParticipant());
			NMS.sendTitle(getPlayer(), "§e직업§f: §5암살자", "§f모든 §a시민§f과 §5탐정§f을 조용히 죽이세요.", 10, 80, 10);
			new AbilityTimer(1) {
				@Override
				protected void run(int count) {
				}

				@Override
				protected void onEnd() {
					NMS.clearTitle(getPlayer());
				}
			}.setInitialDelay(TimeUnit.SECONDS, 5).start();
			if (!hadSword) {
				getPlayer().sendMessage("§e15초 §f뒤에 §4살인자§c의 검§f을 얻습니다.");
				new AbilityTimer(1) {
					@Override
					protected void run(int count) {
						getPlayer().getInventory().setHeldItemSlot(0);
						inventory.setItem(1, Items.MURDERER_SWORD.getStack());
						getPlayer().sendMessage("§4살인자§c의 검§f을 들고 있을 때 더 빠르게 움직일 수 있습니다.");
						for (Player player : Bukkit.getOnlinePlayers()) {
							NMS.sendTitle(player, "§4머더§c가 검을 얻었습니다.", "", 10, 80, 10);
							new AbilityTimer(1) {
								@Override
								protected void run(int count) {
								}

								@Override
								protected void onEnd() {
									NMS.clearTitle(player);
								}
							}.setInitialDelay(TimeUnit.SECONDS, 5).start();
						}
					}
				}.setInitialDelay(TimeUnit.SECONDS, 15).start();
			} else {
				getPlayer().getInventory().setHeldItemSlot(0);
				inventory.setItem(1, Items.MURDERER_SWORD.getStack());
			}
			PASSIVE.start();
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

	@SubscribeEvent(onlyRelevant = true)
	private void onToggleSneak(final PlayerToggleSneakEvent e) {
		if (e.isSneaking() && !cooldown.isRunning()) {
			this.skill = true;
			getPlayer().getInventory().setArmorContents(null);
			NMS.setArrowsInBody(getPlayer(), 0);
			NMS.setInvisible(getPlayer(), true);
			ParticleLib.DRIP_LAVA.spawnParticle(getPlayer().getLocation().clone().add(0, 1, 0), 0.15, 0.15, 0.15, 100, 0);
			SoundLib.ENTITY_SILVERFISH_AMBIENT.playSound(getPlayer().getLocation(), 0.5f, 1f);
		} else if (skill) {
			cooldown.start();
			NMS.setInvisible(getPlayer(), false);
			this.skill = false;
		}
	}

}
