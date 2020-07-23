package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.TargetHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.object.DeathManager;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.minecraft.block.Blocks;
import daybreak.abilitywar.utils.base.minecraft.block.IBlockSnapshot;
import daybreak.abilitywar.utils.library.BlockX;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.PotionEffects;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

@AbilityManifest(name = "글래디에이터", rank = Rank.S, species = Species.HUMAN, explain = {
		"상대방을 철괴로 우클릭하면 부셔지지 않는 투기장이 생성되며 추가 체력을 얻고,",
		"상대방과 본인을 제외한 모든 생명체를 투기장 밖으로 날려보냅니다. $[COOLDOWN_CONFIG]"
})
public class Gladiator extends AbilityBase implements TargetHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Gladiator.class, "Cooldown", 120,
			"# 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};

	public Gladiator(Participant participant) {
		super(participant);
	}

	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());

	private final Map<Block, IBlockSnapshot> saves = new HashMap<>();

	private final AbilityTimer clearField = new AbilityTimer(20) {
		@Override
		public void run(int count) {
			target.sendMessage("§4[§c투기장§4] §f" + count + "초 후에 투기장이 삭제됩니다.");
			getPlayer().sendMessage("§4[§c투기장§4] §f" + count + "초 후에 투기장이 삭제됩니다.");
		}

		@Override
		public void onEnd() {
			for (IBlockSnapshot blockSnapshot : saves.values()) {
				blockSnapshot.apply();
			}
			saves.clear();
			target = null;
		}

		@Override
		public void onSilentEnd() {
			for (IBlockSnapshot blockSnapshot : saves.values()) {
				blockSnapshot.apply();
			}
			saves.clear();
			target = null;
		}
	}.register();

	private Player target = null;
	private final Random random = new Random();
	private final Predicate<Entity> predicate = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			if (entity.equals(getPlayer()) || entity.equals(target)) return false;
			return (!(entity instanceof Player)) || (getGame().isParticipating(entity.getUniqueId())
					&& (!(getGame() instanceof DeathManager.Handler) || !((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
					&& getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue());
		}
	};

	private final AbilityTimer createField = new AbilityTimer(26) {

		private int buildCount;
		private int totalCount;
		private Location center;

		@Override
		public void onStart() {
			buildCount = 1;
			totalCount = 1;
			center = getPlayer().getLocation().clone().subtract(0, 1, 0);
		}

		@Override
		public void run(int count) {
			if (totalCount <= 10) {
				for (Block block : LocationUtil.getBlocks2D(center, buildCount, true, false, true)) {
					saves.putIfAbsent(block, Blocks.createSnapshot(block));
					if (random.nextInt(5) <= 1) {
						BlockX.setType(block, MaterialX.STONE_BRICKS);
					} else {
						BlockX.setType(block, MaterialX.MOSSY_STONE_BRICKS);
					}
				}
				for (LivingEntity livingEntity : LocationUtil.getNearbyEntities(LivingEntity.class, center, buildCount, 6, predicate)) {
					livingEntity.setVelocity(livingEntity.getLocation().toVector().clone().subtract(center.toVector()).normalize());
				}

				buildCount++;
			} else if (totalCount <= 15) {
				for (Block block : LocationUtil.getBlocks2D(center.clone().add(0, totalCount - 10, 0), buildCount - 2, true, false, true)) {
					saves.putIfAbsent(block, Blocks.createSnapshot(block));
					BlockX.setType(block, MaterialX.IRON_BARS);
				}

				for (Block block : LocationUtil.getBlocks2D(center.clone().add(0, totalCount - 10, 0), buildCount - 1, true, false, true)) {
					saves.putIfAbsent(block, Blocks.createSnapshot(block));
					BlockX.setType(block, MaterialX.IRON_BARS);
				}
			} else if (totalCount <= 26) {
				for (Block block : LocationUtil.getBlocks2D(center.clone().add(0, 6, 0), buildCount, true, false, true)) {
					saves.putIfAbsent(block, Blocks.createSnapshot(block));
					if (random.nextInt(5) <= 1) {
						BlockX.setType(block, MaterialX.STONE_BRICKS);
					} else {
						BlockX.setType(block, MaterialX.MOSSY_STONE_BRICKS);
					}
				}

				buildCount--;
			}
			totalCount++;
		}

		@Override
		public void onEnd() {
			Block check = center.getBlock().getRelative(0, 6, 0);

			if (!BlockX.isType(check, MaterialX.STONE_BRICKS)) {
				saves.putIfAbsent(check, Blocks.createSnapshot(check));
				BlockX.setType(check, MaterialX.STONE_BRICKS);
			}

			Location teleport = center.clone().add(0, 1, 0);

			getPlayer().teleport(teleport);
			PotionEffects.ABSORPTION.addPotionEffect(getPlayer(), 400, 2, true);
			PotionEffects.DAMAGE_RESISTANCE.addPotionEffect(getPlayer(), 400, 0, true);
			target.teleport(teleport);

			clearField.start();
		}

		@Override
		protected void onSilentEnd() {
			for (IBlockSnapshot blockSnapshot : saves.values()) {
				blockSnapshot.apply();
			}
			saves.clear();
		}
	}.setPeriod(TimeUnit.TICKS, 1).register();

	@SubscribeEvent
	public void onBlockBreak(BlockBreakEvent e) {
		if (saves.containsKey(e.getBlock())) {
			e.setCancelled(true);
			e.getPlayer().sendMessage("§c투기장§f은 부술 수 없습니다!");
		}
	}

	@SubscribeEvent
	public void onExplode(BlockExplodeEvent e) {
		if (target != null) {
			e.blockList().removeIf(saves::containsKey);
		}
	}

	@SubscribeEvent
	public void onExplode(EntityExplodeEvent e) {
		if (target != null) {
			e.blockList().removeIf(saves::containsKey);
		}
	}

	@Override
	public void TargetSkill(Material material, LivingEntity entity) {
		if (material == Material.IRON_INGOT) {
			if (entity != null) {
				if (entity instanceof Player) {
					if (!cooldownTimer.isCooldown()) {
						this.target = (Player) entity;
						createField.start();

						cooldownTimer.start();
					}
				}
			} else {
				cooldownTimer.isCooldown();
			}
		}
	}

}
