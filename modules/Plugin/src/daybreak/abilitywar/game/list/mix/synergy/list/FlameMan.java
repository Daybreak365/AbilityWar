package daybreak.abilitywar.game.list.mix.synergy.list;

import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.minecraft.block.Blocks;
import daybreak.abilitywar.utils.base.minecraft.block.IBlockSnapshot;
import daybreak.abilitywar.utils.library.BlockX;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.PotionEffects;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

@AbilityManifest(name = "염인", rank = Rank.S, species = Species.HUMAN, explain = {
		"마그마와 용암 위에서 §6힘§f, §3저항 §f버프를 받습니다.",
		"철괴를 우클릭하면 주변을 마그마 지형으로 바꿉니다. $[COOLDOWN_CONFIG]",
		"신속하게 이동하며 지나가는 모든 곳에 불이 붙습니다. 화염 피해를 받지 않습니다."
})
@Beta
public class FlameMan extends Synergy implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = synergySettings.new SettingObject<Integer>(FlameMan.class, "cooldown", 80, "# 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};

	public static final SettingObject<Integer> RangeConfig = synergySettings.new SettingObject<Integer>(FlameMan.class, "range", 15,
			"# 스킬 사용 시 불 지형으로 바꿀 범위") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1 && value <= 50;
		}

	};
	private final AbilityTimer speed = new AbilityTimer() {
		@Override
		protected void run(int count) {
			PotionEffects.SPEED.addPotionEffect(getPlayer(), 20, 1, true);
		}
	}.register();
	private final LinkedList<Block> blocks = new LinkedList<>();
	private final AbilityTimer buff = new AbilityTimer() {
		@Override
		public void run(int count) {
			Block block = getPlayer().getLocation().getBlock(), belowBlock = getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN);
			if (MaterialX.LAVA.compare(block) || MaterialX.LAVA.compare(belowBlock) || block.getType() == Material.LAVA || belowBlock.getType() == Material.LAVA || MaterialX.MAGMA_BLOCK.compare(belowBlock)) {
				PotionEffects.INCREASE_DAMAGE.addPotionEffect(getPlayer(), 5, 0, true);
				PotionEffects.DAMAGE_RESISTANCE.addPotionEffect(getPlayer(), 5, 0, true);
			}
		}
	}.setPeriod(TimeUnit.TICKS, 1).register();
	private final Map<Block, IBlockSnapshot> blockData = new HashMap<>();
	private final AbilityTimer terrain = new AbilityTimer(RangeConfig.getValue()) {

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
					blockData.putIfAbsent(belowBlock, Blocks.createSnapshot(belowBlock));
					belowBlock.setType(Material.LAVA);
				} else {
					blockData.putIfAbsent(belowBlock, Blocks.createSnapshot(belowBlock));
					BlockX.setType(belowBlock, MaterialX.MAGMA_BLOCK);
				}
			}
			count++;
		}
	}.setPeriod(TimeUnit.TICKS, 1).register();
	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());

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
			} else if (toBelow.getType().equals(Material.PACKED_ICE) || toBelow.getType().equals(Material.ICE) || MaterialX.FROSTED_ICE.compare(toBelow) || MaterialX.BLUE_ICE.compare(toBelow)) {
				toBelow.setType(Material.WATER);
			}
		}
	}

	@SubscribeEvent
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity().equals(getPlayer())) {
			DamageCause cause = e.getCause();
			if (cause.equals(DamageCause.FIRE) || cause.equals(DamageCause.FIRE_TICK) || cause.equals(DamageCause.LAVA) || cause.equals(DamageCause.HOT_FLOOR)) {
				e.setCancelled(true);
			}
		}
	}

	@SubscribeEvent
	private void onEntityDamageByBlock(EntityDamageByBlockEvent e) {
		onEntityDamage(e);
	}

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			speed.start();
			buff.start();
		} else if (update == Update.ABILITY_DESTROY) {
			for (Entry<Block, IBlockSnapshot> entry : blockData.entrySet()) {
				Block key = entry.getKey();
				if (MaterialX.MAGMA_BLOCK.compare(key) || key.getType() == Material.LAVA || MaterialX.LAVA.compare(key) || key.getType() == Material.FIRE) {
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
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !cooldownTimer.isCooldown()) {
			terrain.start();
			cooldownTimer.start();
			return true;
		}
		return false;
	}

}
