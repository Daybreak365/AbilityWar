package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.Scheduled;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.ability.event.AbilityDestroyEvent;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.minecraft.compat.block.BlockHandler;
import daybreak.abilitywar.utils.base.minecraft.compat.block.BlockSnapshot;
import daybreak.abilitywar.utils.library.BlockX;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.PotionEffects;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

@AbilityManifest(name = "설인", rank = Rank.S, species = Species.HUMAN, explain = {
		"눈과 얼음 위에서 §6힘§f, §b신속 §f버프를 받습니다.",
		"철괴를 우클릭하면 주변을 눈 지형으로 바꿉니다. $[CooldownConfig]"
})
public class Yeti extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Yeti.class, "Cooldown", 80, "# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};

	public static final SettingObject<Integer> RangeConfig = new SettingObject<Integer>(Yeti.class, "Range", 15,
			"# 스킬 사용 시 눈 지형으로 바꿀 범위") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 1 && value <= 50;
		}

	};

	public Yeti(Participant participant) {
		super(participant);
	}

	@Scheduled
	private final Timer buff = new Timer() {

		@Override
		public void onStart() {
		}

		@Override
		public void run(int count) {
			Material m = getPlayer().getLocation().getBlock().getType();
			Material bm = getPlayer().getLocation().subtract(0, 1, 0).getBlock().getType();
			if (m.equals(Material.SNOW) || bm.equals(Material.SNOW) || bm.equals(Material.SNOW_BLOCK) || bm.equals(Material.ICE) || bm.equals(Material.PACKED_ICE)) {
				PotionEffects.SPEED.addPotionEffect(getPlayer(), 5, 2, true);
				PotionEffects.INCREASE_DAMAGE.addPotionEffect(getPlayer(), 5, 1, true);
			}
		}

		@Override
		public void onEnd() {
		}

	}.setPeriod(TimeUnit.TICKS, 1);

	private final Map<Block, BlockSnapshot> blockData = new HashMap<>();

	private final Timer iceMaker = new Timer(RangeConfig.getValue()) {

		private int count;
		private Location center;

		@Override
		public void onStart() {
			count = 1;
			center = getPlayer().getLocation();
		}

		@Override
		public void run(int sec) {
			Location playerLocation = getPlayer().getLocation();
			World world = getPlayer().getWorld();
			for (Block block : LocationUtil.getBlocks2D(center, count, true, false)) {
				block = world.getBlockAt(block.getX(), LocationUtil.getFloorYAt(world, playerLocation.getY(), block.getX(), block.getZ()), block.getZ());
				Block belowBlock = block.getRelative(BlockFace.DOWN);
				Material type = belowBlock.getType();
				if (type == Material.WATER) {
					blockData.putIfAbsent(belowBlock, BlockHandler.createSnapshot(belowBlock));
					belowBlock.setType(Material.PACKED_ICE);
				} else if (type == Material.LAVA) {
					blockData.putIfAbsent(belowBlock, BlockHandler.createSnapshot(belowBlock));
					belowBlock.setType(Material.OBSIDIAN);
				} else if (MaterialX.ACACIA_LEAVES.compareType(belowBlock) || MaterialX.BIRCH_LEAVES.compareType(belowBlock) || MaterialX.DARK_OAK_LEAVES.compareType(belowBlock)
						|| MaterialX.JUNGLE_LEAVES.compareType(belowBlock) || MaterialX.OAK_LEAVES.compareType(belowBlock) || MaterialX.SPRUCE_LEAVES.compareType(belowBlock)) {
					BlockX.setType(belowBlock, MaterialX.GREEN_WOOL);
				} else {
					blockData.putIfAbsent(belowBlock, BlockHandler.createSnapshot(belowBlock));
					belowBlock.setType(Material.SNOW_BLOCK);
				}

				blockData.putIfAbsent(block, BlockHandler.createSnapshot(block));
				block.setType(Material.SNOW);
			}
			count++;
		}

		@Override
		public void onEnd() {
		}

	}.setPeriod(TimeUnit.TICKS, 1);

	@SubscribeEvent(onlyRelevant = true)
	private void onAbilityDestroy(AbilityDestroyEvent e) {
		for (Entry<Block, BlockSnapshot> entry : blockData.entrySet()) {
			Block key = entry.getKey();
			if (key.getType() == Material.PACKED_ICE || key.getType() == Material.OBSIDIAN || key.getType() == Material.SNOW_BLOCK || key.getType() == Material.SNOW) {
				entry.getValue().apply();
			}
		}
	}

	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT) && clickType.equals(ClickType.RIGHT_CLICK) && !cooldownTimer.isCooldown()) {
			iceMaker.start();
			cooldownTimer.start();
			return true;
		}

		return false;
	}

}
