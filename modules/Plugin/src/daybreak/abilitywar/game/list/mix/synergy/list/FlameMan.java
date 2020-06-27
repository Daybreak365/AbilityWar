package daybreak.abilitywar.game.list.mix.synergy.list;

import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.Scheduled;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.annotations.Support;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.minecraft.compat.block.BlockHandler;
import daybreak.abilitywar.utils.base.minecraft.compat.block.BlockSnapshot;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion.Version;
import daybreak.abilitywar.utils.library.BlockX;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.PotionEffects;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;

@AbilityManifest(name = "염인", rank = Rank.S, species = Species.HUMAN, explain = {
		""
})
@Support(min = Version.v1_12_R1)
@Beta
public class FlameMan extends Synergy implements ActiveHandler {

	public static final SettingObject<Integer> CooldownConfig = synergySettings.new SettingObject<Integer>(FlameMan.class, "Cooldown", 80, "# 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};

	public static final SettingObject<Integer> RangeConfig = synergySettings.new SettingObject<Integer>(FlameMan.class, "Range", 15,
			"# 스킬 사용 시 불 지형으로 바꿀 범위") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1 && value <= 50;
		}

	};
	@Scheduled
	private final Timer speed = new Timer() {
		@Override
		protected void run(int count) {
			PotionEffects.SPEED.addPotionEffect(getPlayer(), 20, 1, true);
		}
	};
	private final LinkedList<Block> blocks = new LinkedList<>();
	@Scheduled
	private final Timer buff = new Timer() {
		@Override
		public void run(int count) {
			Block block = getPlayer().getLocation().getBlock(), belowBlock = getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN);
			if (MaterialX.LAVA.compareType(block) || MaterialX.LAVA.compareType(belowBlock) || block.getType() == Material.LAVA || belowBlock.getType() == Material.LAVA || MaterialX.MAGMA_BLOCK.compareType(belowBlock)) {
				PotionEffects.SPEED.addPotionEffect(getPlayer(), 5, 2, true);
				PotionEffects.INCREASE_DAMAGE.addPotionEffect(getPlayer(), 5, 1, true);
			}
		}
	}.setPeriod(TimeUnit.TICKS, 1);
	private final Map<Block, BlockSnapshot> blockData = new HashMap<>();
	private final Timer terrain = new Timer(RangeConfig.getValue()) {

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
			for (Block block : LocationUtil.getBlocks2D(center, count, true, false, true)) {
				block = world.getBlockAt(block.getX(), LocationUtil.getFloorYAt(world, playerLocation.getY(), block.getX(), block.getZ()), block.getZ());
				Block belowBlock = block.getRelative(BlockFace.DOWN);
				Material type = belowBlock.getType();
				if (type == Material.WATER) {
					blockData.putIfAbsent(belowBlock, BlockHandler.createSnapshot(belowBlock));
					belowBlock.setType(Material.LAVA);
				} else {
					blockData.putIfAbsent(belowBlock, BlockHandler.createSnapshot(belowBlock));
					BlockX.setType(belowBlock, MaterialX.MAGMA_BLOCK);
				}
			}
			count++;
		}
	}.setPeriod(TimeUnit.TICKS, 1);
	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());

	public FlameMan(Participant participant) {
		super(participant);
	}

	@SubscribeEvent
	public void onPlayerMove(PlayerMoveEvent e) {
		if (e.getPlayer().equals(getPlayer())) {
			Block to = e.getTo().getBlock();
			Block toBelow = to.getLocation().subtract(0, 1, 0).getBlock();
			if (to.getType().equals(Material.AIR) || to.getType().equals(Material.SNOW)) {
				to.setType(Material.FIRE);
				if (to.getType().equals(Material.FIRE)) {
					blocks.add(to);
					if (blocks.size() >= 30) {
						blocks.removeFirst().setType(Material.AIR);
					}
				}
			}
			if (toBelow.getType().equals(Material.SNOW_BLOCK)) {
				toBelow.setType(Material.DIRT);
			} else if (toBelow.getType().equals(Material.PACKED_ICE) || toBelow.getType().equals(Material.ICE) || MaterialX.FROSTED_ICE.compareType(toBelow) || MaterialX.BLUE_ICE.compareType(toBelow)) {
				toBelow.setType(Material.WATER);
			}
		}
	}

	@SubscribeEvent
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity().equals(getPlayer())) {
			DamageCause cause = e.getCause();
			if (cause.equals(DamageCause.FIRE) || cause.equals(DamageCause.FIRE_TICK) || cause.equals(DamageCause.LAVA)) {
				e.setCancelled(true);
			}
		}
	}

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.ABILITY_DESTROY) {
			for (Entry<Block, BlockSnapshot> entry : blockData.entrySet()) {
				Block key = entry.getKey();
				if (MaterialX.MAGMA_BLOCK.compareType(key) || key.getType() == Material.LAVA || MaterialX.LAVA.compareType(key) || key.getType() == Material.FIRE) {
					entry.getValue().apply();
				}
			}
			for (Block block : blocks) {
				if (block.getType() == Material.FIRE) block.setType(Material.AIR);
			}
			getPlayer().setFireTicks(0);
		}
	}

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT) && clickType.equals(ClickType.RIGHT_CLICK) && !cooldownTimer.isCooldown()) {
			terrain.start();
			cooldownTimer.start();
			return true;
		}
		return false;
	}

}
