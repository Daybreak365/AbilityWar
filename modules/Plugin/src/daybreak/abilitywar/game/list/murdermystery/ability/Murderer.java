package daybreak.abilitywar.game.list.murdermystery.ability;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.list.murdermystery.Items;
import daybreak.abilitywar.game.list.murdermystery.MurderMystery;
import daybreak.abilitywar.game.manager.object.CommandHandler.CommandType;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import java.lang.reflect.InvocationTargetException;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.PlayerInventory;

@AbilityManifest(name = "머더", rank = Rank.SPECIAL, species = Species.HUMAN, explain = {
		"모든 시민을 죽이세요!",
		"살인자의 검으로 상대를 죽일 경우 5초간 투명 효과를 받습니다.",
		"금 우클릭으로 금 8개를 소모해 활과 화살을 얻을 수 있습니다.",
		"살인자의 검을 얻은 이후 금 좌클릭으로 금 5개를 소모해 무작위 직업을",
		"배정받을 수 있습니다."
})
public class Murderer extends AbstractMurderer {

	private final AbilityTimer PASSIVE = new AbilityTimer() {
		@Override
		protected void run(int count) {
			if (Items.isMurdererSword(getPlayer().getInventory().getItemInMainHand())) {
				getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.15);
			}
		}
	}.setPeriod(TimeUnit.TICKS, 1).register();

	public Murderer(Participant participant) {
		super(participant);
	}

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			PlayerInventory inventory = getPlayer().getInventory();
			inventory.clear();
			getPlayer().getInventory().setHeldItemSlot(0);
			((MurderMystery) getGame()).updateGold(getParticipant());
			NMS.sendTitle(getPlayer(), "§e역할§f: §5머더", "§f모든 §a시민§f과 §5탐정§f을 죽이세요!", 10, 80, 10);
			new AbilityTimer(1) {
				@Override
				protected void run(int count) {
				}

				@Override
				protected void onEnd() {
					NMS.clearTitle(getPlayer());
				}
			}.setInitialDelay(TimeUnit.SECONDS, 5).start();
			getPlayer().sendMessage("§e50초 §f뒤에 §4살인자§c의 검§f을 얻습니다.");
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
			}.setInitialDelay(TimeUnit.SECONDS, 50).start();
			PASSIVE.start();
		}
	}

	private static final String[] EMPTY_STRING_ARRAY = new String[0];

	@SubscribeEvent(onlyRelevant = true)
	private void onInteract(PlayerInteractEvent e) {
		if (Items.isGold(e.getItem())) {
			if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				MurderMystery murderMystery = (MurderMystery) getGame();
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
				if (!hasSword()) return;
				final MurderMystery murderMystery = (MurderMystery) getGame();
				if (murderMystery.consumeGold(getParticipant(), 5)) {
					try {
						getParticipant().setAbility(MurderMystery.getRandomMurderJob());
						getGame().executeCommand(CommandType.ABILITY_CHECK, getPlayer(), "aw", EMPTY_STRING_ARRAY, AbilityWar.getPlugin());
					} catch (ReflectiveOperationException ignored) {
					}
				}
			}
		}
	}

}
