package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.Scheduled;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.TargetHandler;
import daybreak.abilitywar.ability.event.AbilityDestroyEvent;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.minecraft.compat.block.BlocksHandler;
import daybreak.abilitywar.utils.library.BlockX;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.math.LocationUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;

@AbilityManifest(Name = "구속", Rank = Rank.B, Species = Species.HUMAN)
public class Imprison extends AbilityBase implements TargetHandler {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Imprison.class, "Cooldown", 25, "# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public static final SettingObject<Integer> SizeConfig = new SettingObject<Integer>(Imprison.class, "Size", 5, "# 유리 구의 크기") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}

	};

	public static final SettingObject<Integer> MaxSolidityConfig = new SettingObject<Integer>(Imprison.class, "MaxSolidity", 4, "# 최대 강도") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}

	};

	public Imprison(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f상대방을 철괴로 우클릭하면 대상을 유리막 속에 가둡니다. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&f10초마다 &e강도 스택&f이 1씩 오르며, 최대 " + MaxSolidityConfig.getValue() + " 스택을 모을 수 있습니다."),
				ChatColor.translateAlternateColorCodes('&', "&e강도 스택&f은 능력을 사용하면 초기화되며, "));
	}

	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());

	private final int size = SizeConfig.getValue();
	private final int maxSolidity = MaxSolidityConfig.getValue();
	private int solidity = 1;

	@Scheduled
	private final Timer stackAdder = new Timer() {
		@Override
		protected void run(int count) {
			if (solidity < maxSolidity) {
				solidity++;
				actionbarChannel.update(ChatColor.translateAlternateColorCodes('&', "&f강도: &c" + solidity));
			}
		}
	}.setPeriod(TimeUnit.SECONDS, 10);

	private final ActionbarChannel actionbarChannel = newActionbarChannel();

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		return false;
	}

	private final Map<Block, Integer> blocks = new HashMap<>();

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
		if (materialType.equals(Material.IRON_INGOT)) {
			if (entity != null) {
				if (!cooldownTimer.isCooldown()) {
					for (Block block : LocationUtil.getBlocks3D(entity.getLocation(), size, true, true)) {
						if (!BlocksHandler.getBlocks().isIndestructible(block.getType())) {
							BlockX.setType(block, MaterialX.WHITE_STAINED_GLASS);
							blocks.put(block, solidity);
						}
					}
					solidity = 1;
					actionbarChannel.update(ChatColor.translateAlternateColorCodes('&', "&f강도: &c" + solidity));

					cooldownTimer.start();
				}
			} else {
				cooldownTimer.isCooldown();
			}
		}
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onAbilityDestroy(AbilityDestroyEvent e) {
		for (Block block : blocks.keySet()) {
			block.setType(Material.AIR);
		}
	}

	private final Random random = new Random();
	private static final MaterialX[] materials = {
			MaterialX.RED_STAINED_GLASS,
			MaterialX.ORANGE_STAINED_GLASS,
			MaterialX.YELLOW_STAINED_GLASS,
			MaterialX.GREEN_STAINED_GLASS,
			MaterialX.BLUE_STAINED_GLASS
	};

	@SubscribeEvent
	public void onExplode(BlockExplodeEvent e) {
		e.blockList().removeIf(new Predicate<Block>() {
			@Override
			public boolean test(Block block) {
				if (blocks.containsKey(block)) {
					int subtract = blocks.get(block) - 2;
					if (subtract > 0) {
						blocks.put(block, subtract);
						BlockX.setType(block, materials[random.nextInt(materials.length)]);
						return true;
					} else {
						blocks.remove(block);
					}
				}
				return false;
			}
		});
	}

	@SubscribeEvent
	public void onExplode(EntityExplodeEvent e) {
		e.blockList().removeIf(new Predicate<Block>() {
			@Override
			public boolean test(Block block) {
				if (blocks.containsKey(block)) {
					int subtract = blocks.get(block) - 2;
					if (subtract > 0) {
						blocks.put(block, subtract);
						BlockX.setType(block, materials[random.nextInt(materials.length)]);
						return true;
					} else {
						blocks.remove(block);
					}
				}
				return false;
			}
		});
	}

	@SubscribeEvent
	private void onBlockBreak(BlockBreakEvent e) {
		if (blocks.containsKey(e.getBlock())) {
			Block block = e.getBlock();
			int subtract = blocks.get(block) - 1;
			if (subtract > 0) {
				e.setCancelled(true);
				blocks.put(block, subtract);
				BlockX.setType(block, materials[random.nextInt(materials.length)]);
			} else {
				blocks.remove(block);
			}
		}
	}

}
