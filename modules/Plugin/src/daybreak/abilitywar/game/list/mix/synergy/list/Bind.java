package daybreak.abilitywar.game.list.mix.synergy.list;

import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.library.BlockX;
import daybreak.abilitywar.utils.library.MaterialX;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;

@AbilityManifest(name = "결박", rank = Rank.B, species = Species.HUMAN, explain = {
		"주위 15칸 안에 있는 생명체를 원거리에서 철괴 우클릭으로 타게팅해",
		"세 겹의 유리막 속에 가둡니다. $[COOLDOWN_CONFIG]",
		"10초마다 §e강도 스택§f이 1씩 오르며, 최대 $[MaxSolidityConfig] 스택을 모을 수 있습니다.",
		"§e강도 스택§f은 능력을 사용하면 초기화됩니다."
})
public class Bind extends Synergy implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = synergySettings.new SettingObject<Integer>(Bind.class, "cooldown", 25, "# 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};

	public static final SettingObject<Integer> SizeConfig = synergySettings.new SettingObject<Integer>(Bind.class, "size", 6, "# 유리 구의 크기") {

		@Override
		public boolean condition(Integer value) {
			return value >= 5;
		}

	};

	public static final SettingObject<Integer> MaxSolidityConfig = synergySettings.new SettingObject<Integer>(Bind.class, "max-solidity", 4, "# 최대 강도") {

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
	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());

	private final int maxSolidity = MaxSolidityConfig.getValue();
	private final ActionbarChannel actionbarChannel = newActionbarChannel();
	private final Map<Block, Integer> blocks = new HashMap<>();
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
	private final Random random = new Random();
	private int solidity = 1;

	private final AbilityTimer stackAdder = new AbilityTimer() {
		@Override
		protected void run(int count) {
			if (solidity < maxSolidity) {
				solidity++;
				actionbarChannel.update("§f강도: §c" + solidity);
			}
		}
	}.setPeriod(TimeUnit.SECONDS, 10).register();

	public Bind(Participant participant) {
		super(participant);
	}

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !cooldownTimer.isCooldown()) {
			LivingEntity entity = LocationUtil.getEntityLookingAt(LivingEntity.class, getPlayer(), 15, predicate);
			if (entity != null) {
				if (!cooldownTimer.isCooldown()) {
					final int size = SizeConfig.getValue();
					for (int i = 0; i < 3; i++) {
						for (Block block : LocationUtil.getBlocks3D(entity.getLocation(), size - i, true, true)) {
							if (!BlockX.isIndestructible(block.getType())) {
								BlockX.setType(block, MaterialX.WHITE_STAINED_GLASS);
								blocks.put(block, solidity);
							}
						}
					}
					solidity = 1;
					actionbarChannel.update("§f강도: §c" + solidity);

					cooldownTimer.start();
				}
			}
		}
		return false;
	}

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			stackAdder.start();
		} else if (update == Update.ABILITY_DESTROY) {
			for (Block block : blocks.keySet()) {
				block.setType(Material.AIR);
			}
		}
	}

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
