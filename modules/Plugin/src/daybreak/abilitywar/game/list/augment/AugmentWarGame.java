package daybreak.abilitywar.game.list.augment;

import daybreak.abilitywar.game.GameAliases;
import daybreak.abilitywar.game.GameManifest;
import daybreak.abilitywar.game.augment.Augment;
import daybreak.abilitywar.game.augment.AugmentRegistry;
import daybreak.abilitywar.game.augment.AugmentSelection;
import daybreak.abilitywar.game.augment.DefaultAugments;
import daybreak.abilitywar.game.augment.ParticipantAugments;
import daybreak.abilitywar.game.list.standard.WarGame;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@GameManifest(name = "증강 능력자 전쟁", description = {
		"§f일정 시간마다 참가자가 증강을 획득하는 능력자 전쟁입니다.",
		"",
		"§a● §f첫 증강은 게임 시작 직후 지급됩니다.",
		"§a● §f이후 2분마다 생존 참가자에게 새로운 증강이 지급됩니다."
})
@GameAliases({"증강", "augment", "augments"})
public class AugmentWarGame extends WarGame {

	private static final int FIRST_AUGMENT_SECONDS = 18;
	private static final int AUGMENT_INTERVAL_SECONDS = 120;
	private static final int[] AUGMENT_SLOTS = {11, 13, 15};

	private final ParticipantAugments participantAugments = new ParticipantAugments(this);
	private final AugmentRegistry augmentRegistry = DefaultAugments.createRegistry();
	private final AugmentSelection augmentSelection = new AugmentSelection(augmentRegistry, new Random());
	private final Map<UUID, List<Augment>> pendingChoices = new HashMap<>();

	@Override
	protected void progressGame(int seconds) {
		super.progressGame(seconds);
		if (seconds >= FIRST_AUGMENT_SECONDS && (seconds - FIRST_AUGMENT_SECONDS) % AUGMENT_INTERVAL_SECONDS == 0) {
			grantAugmentChoices(seconds);
		}
	}

	private void grantAugmentChoices(int seconds) {
		Bukkit.broadcastMessage("§d[증강] §f새로운 증강 라운드가 시작되었습니다. §7(" + seconds + "초)");
		for (Participant participant : getParticipants()) {
			if (!getDeathManager().isExcluded(participant.getPlayer())) {
				final List<Augment> candidates = augmentSelection.roll(participantAugments, participant, 3);
				if (!candidates.isEmpty()) {
					pendingChoices.put(participant.getPlayer().getUniqueId(), candidates);
					participant.getPlayer().sendMessage("§d[증강] §f선택 가능한 증강이 도착했습니다. §7/aw augment §f명령어로 다시 열 수 있습니다.");
					openAugmentGUI(participant.getPlayer());
				} else {
					participant.getPlayer().sendMessage("§d[증강] §7현재 선택 가능한 증강이 없습니다.");
				}
			}
		}
	}

	public void openAugmentGUI(Player player) {
		final Participant participant = getParticipant(player);
		if (participant == null || getDeathManager().isExcluded(player)) {
			player.sendMessage("§d[증강] §c증강을 선택할 수 없는 상태입니다.");
			return;
		}
		final List<Augment> choices = pendingChoices.get(player.getUniqueId());
		if (choices == null || choices.isEmpty()) {
			player.sendMessage("§d[증강] §7선택 대기 중인 증강이 없습니다.");
			return;
		}
		final AugmentInventoryHolder holder = new AugmentInventoryHolder(player.getUniqueId());
		final Inventory inventory = Bukkit.createInventory(holder, 27, "§0§l증강 선택");
		holder.setInventory(inventory);
		for (int i = 0; i < choices.size() && i < AUGMENT_SLOTS.length; i++) {
			inventory.setItem(AUGMENT_SLOTS[i], createIcon(choices.get(i)));
		}
		player.openInventory(inventory);
	}

	private ItemStack createIcon(Augment augment) {
		final ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
		final ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			meta.setDisplayName(augment.getRarity().format("[" + augment.getRarity().getDisplayName() + "] ") + "§f" + augment.getDisplayName());
			final List<String> lore = new ArrayList<>();
			lore.add("§7" + augment.getDescription());
			lore.add("");
			lore.add("§e클릭해서 이 증강을 선택합니다.");
			meta.setLore(lore);
			item.setItemMeta(meta);
		}
		return item;
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (!(event.getInventory().getHolder() instanceof AugmentInventoryHolder)) return;
		event.setCancelled(true);
		if (!(event.getWhoClicked() instanceof Player)) return;
		final Player player = (Player) event.getWhoClicked();
		if (!((AugmentInventoryHolder) event.getInventory().getHolder()).isOwner(player.getUniqueId())) return;
		final List<Augment> choices = pendingChoices.get(player.getUniqueId());
		if (choices == null) {
			player.closeInventory();
			return;
		}
		for (int i = 0; i < AUGMENT_SLOTS.length && i < choices.size(); i++) {
			if (event.getRawSlot() == AUGMENT_SLOTS[i]) {
				final Augment selected = choices.get(i);
				pendingChoices.remove(player.getUniqueId());
				selected.apply(participantAugments, getParticipant(player));
				player.sendMessage("§d[증강] §f획득: " + DefaultAugments.format(selected));
				player.closeInventory();
				return;
			}
		}
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if (event.getInventory().getHolder() instanceof AugmentInventoryHolder
				&& ((AugmentInventoryHolder) event.getInventory().getHolder()).isOwner(event.getPlayer().getUniqueId())
				&& pendingChoices.containsKey(event.getPlayer().getUniqueId())) {
			event.getPlayer().sendMessage("§d[증강] §7아직 증강을 선택하지 않았습니다. §f/aw augment §7명령어로 다시 열 수 있습니다.");
		}
	}

	@Override
	protected void onEnd() {
		pendingChoices.clear();
		augmentRegistry.unregisterListeners();
		super.onEnd();
	}

	private static class AugmentInventoryHolder implements InventoryHolder {
		private final UUID playerId;
		private Inventory inventory;

		private AugmentInventoryHolder(UUID playerId) {
			this.playerId = playerId;
		}

		private void setInventory(Inventory inventory) {
			this.inventory = inventory;
		}

		private boolean isOwner(UUID playerId) {
			return this.playerId.equals(playerId);
		}

		@Override
		public Inventory getInventory() {
			return inventory;
		}
	}
}
