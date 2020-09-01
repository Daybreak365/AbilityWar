package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.manager.object.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer.Observer;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.minecraft.block.Blocks;
import daybreak.abilitywar.utils.base.minecraft.block.IBlockSnapshot;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.library.BlockX;
import daybreak.abilitywar.utils.library.MaterialX;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

@AbilityManifest(name = "구속", rank = Rank.B, species = Species.HUMAN, explain = {
		"플레이어를 바라보고 철괴를 우클릭해 대상을 유리구 속에 가두거나,",
		"철괴를 좌클릭해 본인을 중심으로 유리구를 생성합니다. $[COOLDOWN_CONFIG]",
		"능력으로 생성된 유리는 §7(§e강도 스택§7)§f번 깨야 파괴할 수 있습니다.",
		"15초마다 §e강도 스택§f이 1씩 오르며, 최대 4 스택을 모을 수 있습니다. 모은 §e강도 스택§f은",
		"능력을 사용하면 초기화됩니다. 능력으로 생성될 유리구의 크기를 철괴를 들고,",
		"웅크린 상태로 마우스 휠을 이용하여 조절할 수 있습니다."
})
public class Imprison extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Imprison.class, "Cooldown", 25, "# 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};

	public static final SettingObject<Integer> MAX_SIZE_CONFIG = abilitySettings.new SettingObject<Integer>(Imprison.class, "MAX_SIZE", 7, "# 유리 구의 최대 크기") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	public static final SettingObject<Integer> MAX_SOLIDITY_CONFIG = abilitySettings.new SettingObject<Integer>(Imprison.class, "MaxSolidity", 4, "# 최대 강도") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};
	private static final MaterialX[] materials = {
			MaterialX.RED_STAINED_GLASS,
			MaterialX.ORANGE_STAINED_GLASS,
			MaterialX.YELLOW_STAINED_GLASS,
			MaterialX.GREEN_STAINED_GLASS,
			MaterialX.BLUE_STAINED_GLASS
	};

	private final AbilityTimer stackAdder = new AbilityTimer() {
		@Override
		protected void onStart() {
			actionbarChannel.update("§f강도: §c" + solidity);
		}

		@Override
		protected void run(int count) {
			if (solidity < maxSolidity) {
				solidity++;
				actionbarChannel.update("§f강도: §c" + solidity);
			}
		}
		@Override
		protected void onEnd() {
			actionbarChannel.update(null);
		}
	}.setInitialDelay(TimeUnit.SECONDS, 15).setPeriod(TimeUnit.SECONDS, 15).register();
	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue()).attachObserver(new Observer() {
		@Override
		public void onStart() {
			stackAdder.stop(false);
		}

		@Override
		public void onEnd() {
			stackAdder.start();
		}
		@Override
		public void onSilentEnd() {}
		@Override
		public void onPause() {}
		@Override
		public void onResume() {}
	});
	private final int maxSolidity = MAX_SOLIDITY_CONFIG.getValue(), maxSize = MAX_SIZE_CONFIG.getValue();
	private final ActionbarChannel actionbarChannel = newActionbarChannel();
	private final Map<Block, BlockDatum> blocks = new HashMap<>();
	private final Random random = new Random();
	private final AbilityTimer titleClear = new AbilityTimer(10) {
		@Override
		protected void run(int count) {
		}

		@Override
		protected void onEnd() {
			NMS.clearTitle(getPlayer());
		}

		@Override
		protected void onSilentEnd() {
			NMS.clearTitle(getPlayer());
		}
	}.setPeriod(TimeUnit.TICKS, 4);
	private int size = maxSize, solidity = 1;

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

	public Imprison(Participant participant) {
		super(participant);
	}

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT) {
			if (clickType == ClickType.RIGHT_CLICK) {
				if (cooldownTimer.isCooldown()) return false;
				final Player target = LocationUtil.getEntityLookingAt(Player.class, getPlayer(), 7, predicate);
				if (target != null) {
					skill(target);
					return true;
				}
			} else if (clickType == ClickType.LEFT_CLICK) {
				if (cooldownTimer.isCooldown()) return false;
				skill(getPlayer());
				return true;
			}
		}
		return false;
	}

	private void skill(final Player center) {
		for (BlockDatum blockDatum : blocks.values()) {
			blockDatum.snapshot.apply();
		}
		blocks.clear();
		for (Block block : LocationUtil.getBlocks3D(center.getLocation(), size, true, true)) {
			if (!BlockX.isIndestructible(block.getType())) {
				blocks.put(block, new BlockDatum(block, solidity));
				BlockX.setType(block, MaterialX.WHITE_STAINED_GLASS);
			}
		}
		solidity = 1;
		actionbarChannel.update("§f강도: §c" + solidity);
		cooldownTimer.start();
	}

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			stackAdder.start();
		} else if (update == Update.ABILITY_DESTROY) {
			for (BlockDatum blockDatum : blocks.values()) {
				blockDatum.snapshot.apply();
			}
		}
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onSlotChange(final PlayerItemHeldEvent e) {
		if (!getPlayer().isSneaking() || e.getPreviousSlot() == e.getNewSlot()) return;
		final PlayerInventory inventory = getPlayer().getInventory();
		final ItemStack previous = inventory.getItem(e.getPreviousSlot());
		if (previous != null && previous.getType() == Material.IRON_INGOT) {
			e.setCancelled(true);
			final State state = getState(e.getPreviousSlot(), e.getNewSlot());
			if (state == State.UNKNOWN) return;
			switch (state) {
				case UP:
					size = limit(size + 1, maxSize, 3);
					break;
				case DOWN:
					size = limit(size - 1, maxSize, 3);
					break;
			}
			NMS.sendTitle(getPlayer(), state == State.UP ? "§c↑" : "§9↓", String.valueOf(size), 0, 20, 0);
			if (!titleClear.start()) {
				titleClear.setCount(10);
			}
		}
	}

	private int limit(final int value, final int max, final int min) {
		return Math.max(min, Math.min(max, value));
	}

	private State getState(final int previousSlot, final int newSlot) {
		if (previousSlot == 0) {
			return newSlot >= 6 ? State.UP : (newSlot <= 3 ? State.DOWN : State.UNKNOWN);
		} else if (previousSlot == 8) {
			return newSlot <= 2 ? State.DOWN : (newSlot >= 5 ? State.UP : State.UNKNOWN);
		} else {
			return calculate(previousSlot, -1) == newSlot
					|| calculate(previousSlot, -2) == newSlot
					|| calculate(previousSlot, -3) == newSlot ? State.UP :
					(
							calculate(previousSlot, 1) == newSlot
							|| calculate(previousSlot, 2) == newSlot
							|| calculate(previousSlot, 3) == newSlot ? State.DOWN : State.UNKNOWN
					);
		}
	}

	private int calculate(int slot, int offset) {
		final int value = slot + offset;
		if (value < 0) return 9 + value;
		else if (value > 8) return value - 9;
		else return value;
	}

	@SubscribeEvent
	private void onExplode(BlockExplodeEvent e) {
		e.blockList().removeIf(new Predicate<Block>() {
			@Override
			public boolean test(Block block) {
				if (blocks.containsKey(block)) {
					return !blocks.get(block).weaken(2);
				}
				return false;
			}
		});
	}

	@SubscribeEvent
	private void onExplode(EntityExplodeEvent e) {
		e.blockList().removeIf(new Predicate<Block>() {
			@Override
			public boolean test(Block block) {
				if (blocks.containsKey(block)) {
					return !blocks.get(block).weaken(2);
				}
				return false;
			}
		});
	}

	@SubscribeEvent
	private void onBlockBreak(BlockBreakEvent e) {
		if (blocks.containsKey(e.getBlock())) {
			if (!blocks.get(e.getBlock()).weaken(1)) {
				e.setCancelled(true);
			}
		}
	}

	private enum State {
		UP, DOWN, UNKNOWN
	}

	private class BlockDatum {

		private final Block block;
		private final IBlockSnapshot snapshot;
		private int solidity;

		private BlockDatum(final Block block, final int solidity) {
			this.block = block;
			this.snapshot = Blocks.createSnapshot(block);
			this.solidity = solidity;
		}

		private boolean weaken(final int amount) {
			if ((solidity -= amount) <= 0) {
				snapshot.apply();
				blocks.remove(block);
				return true;
			} else {
				BlockX.setType(block, materials[random.nextInt(materials.length)]);
				return false;
			}
		}

	}

}
