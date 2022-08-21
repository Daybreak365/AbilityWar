package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.Tips;
import daybreak.abilitywar.ability.Tips.Description;
import daybreak.abilitywar.ability.Tips.Difficulty;
import daybreak.abilitywar.ability.Tips.Level;
import daybreak.abilitywar.ability.Tips.Stats;
import daybreak.abilitywar.ability.decorator.TargetHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.minecraft.block.Blocks;
import daybreak.abilitywar.utils.base.minecraft.block.IBlockSnapshot;
import daybreak.abilitywar.utils.library.BlockX;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.function.Predicate;

@AbilityManifest(name = "글래디에이터", rank = Rank.S, species = Species.HUMAN, explain = {
		"상대를 철괴 우클릭하면 부술 수 없는 투기장이 생성되며 §e흡수§f/§3저항 §f효과를 얻고,",
		"상대와 본인을 제외한 모든 생명체를 투기장 밖으로 날려보냅니다. $[COOLDOWN_CONFIG]"
}, summarize = {
		"상대를 철괴 우클릭하면 부술 수 없는 투기장이 생성되며 §e흡수§f/§3저항 §f효과를 얻고,",
		"상대와 본인을 제외한 모든 생명체를 투기장 밖으로 날려보냅니다. $[COOLDOWN_CONFIG]"
})
@Tips(tip = {
		"다른 사람의 방해 없이, 유리한 1:1 전투를 펼치고 싶다면 글래디에이터를",
		"사용하세요. 하지만 아무리 많은 효과를 받더라도, 승패를 결정하는 것은",
		"상대방의 능력과 사용자의 실력입니다. 항상 방심하지 마세요."
}, strong = {
		@Description(subject = "좁은 공간에서 약한 상대", explain = {
				"글래디에이터는 좁은 공간을 만들어 그 곳에서 전투를 펼치기",
				"때문에, 좁은 공간에서 약한 상대에 강합니다. 예를 들어, §b깃털§f과 §b로렘§f",
				"등이 있습니다."
		})
}, weak = {
		@Description(subject = "좁은 공간에서 강한 상대", explain = {
				"글래디에이터는 좁은 공간을 만들어 그 곳에서 전투를 펼치기",
				"때문에, 좁은 공간에서 강한 상대에 약합니다."
		}),
		@Description(subject = "순간 이동", explain = {
				"글래디에이터는 물리적으로 밖으로 나가지 못하도록 막을 뿐, 순간 이동을",
				"막지는 않습니다. 글래디에이터의 투기장 밖으로 순간 이동할 수 있는 능력에게",
				"카운터당할 수 있습니다."
		})
}, stats = @Stats(offense = Level.ZERO, survival = Level.ZERO, crowdControl = Level.ZERO, mobility = Level.ZERO, utility = Level.ZERO), difficulty = Difficulty.EASY)
public class Gladiator extends AbilityBase implements TargetHandler {

	private static final MaterialX COMMON_BLOCK, RARE_BLOCK, BAR_BLOCK;
	private static final int RARE_BOUND;

	static {
		COMMON_BLOCK = MaterialX.STONE_BRICKS;
		RARE_BLOCK = MaterialX.MOSSY_STONE_BRICKS;
		BAR_BLOCK = MaterialX.IRON_BARS;
		RARE_BOUND = 5;
	}

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Gladiator.class, "cooldown", 120,
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

	private static final Random random = new Random();

	public Gladiator(Participant participant) {
		super(participant);
	}

	private Arena arena = null;
	private final Cooldown cooldown = new Cooldown(COOLDOWN_CONFIG.getValue());

	private class Arena extends AbilityTimer implements Listener {

		private final Predicate<Entity> predicate = new Predicate<Entity>() {
			@Override
			public boolean test(Entity entity) {
				if (entity.equals(getPlayer()) || entity.equals(target)) return false;
				return (!(entity instanceof Player)) || (getGame().isParticipating(entity.getUniqueId())
						&& (!(getGame() instanceof DeathManager.Handler) || !((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
						&& getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue());
			}
		};
		private final Map<Block, IBlockSnapshot> saves = new HashMap<>();
		private final Location center;
		private final Player target;
		private int build = 1;

		private Arena(Location center, Player target) {
			super(TaskType.NORMAL, 27 + 400);
			setPeriod(TimeUnit.TICKS, 1);
			Gladiator.this.arena = this;
			this.center = center;
			this.target = target;
		}

		@Override
		protected void onStart() {
			Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
			SoundLib.ENTITY_WITHER_SPAWN.playSound(center, .45f, .75f);
		}

		@Override
		protected void run(int count) {
			if (count <= 10) {
				for (Block block : LocationUtil.getBlocks2D(center, build, true, false, true)) {
					saves.putIfAbsent(block, Blocks.createSnapshot(block));
					if (random.nextInt(RARE_BOUND) > 1) {
						BlockX.setType(block, COMMON_BLOCK);
					} else {
						BlockX.setType(block, RARE_BLOCK);
					}
				}
				for (LivingEntity livingEntity : LocationUtil.getNearbyEntities(LivingEntity.class, center, build, 6, predicate)) {
					livingEntity.setVelocity(livingEntity.getLocation().toVector().clone().subtract(center.toVector()).normalize());
				}

				build++;
			} else if (count <= 15) {
				for (Block block : LocationUtil.getBlocks2D(center.clone().add(0, count - 10, 0), build - 2, true, false, true)) {
					saves.putIfAbsent(block, Blocks.createSnapshot(block));
					BlockX.setType(block, BAR_BLOCK);
				}

				for (Block block : LocationUtil.getBlocks2D(center.clone().add(0, count - 10, 0), build - 1, true, false, true)) {
					saves.putIfAbsent(block, Blocks.createSnapshot(block));
					BlockX.setType(block, BAR_BLOCK);
				}
			} else if (count <= 26) {
				for (Block block : LocationUtil.getBlocks2D(center.clone().add(0, 6, 0), build, true, false, true)) {
					saves.putIfAbsent(block, Blocks.createSnapshot(block));
					if (random.nextInt(RARE_BOUND) > 1) {
						BlockX.setType(block, COMMON_BLOCK);
					} else {
						BlockX.setType(block, RARE_BLOCK);
					}
				}

				build--;
			} else {
				if (count == 27) {
					final Location teleport = center.clone().add(0, 1, 0);
					getPlayer().teleport(teleport);
					PotionEffects.ABSORPTION.addPotionEffect(getPlayer(), 400, 2, true);
					PotionEffects.DAMAGE_RESISTANCE.addPotionEffect(getPlayer(), 400, 0, true);
					target.teleport(teleport);
				}
				final int sub = count - 27;
				if (sub % 20 == 0 && sub != 400) {
					final int left = 20 - sub / 20;
					if (left == 20 || left == 10 || left <= 5) {
						target.sendMessage("§4[§c투기장§4] §f" + left + "초 후에 투기장이 사라집니다.");
						getPlayer().sendMessage("§4[§c투기장§4] §f" + left + "초 후에 투기장이 사라집니다.");
					}
				}
			}
		}

		@EventHandler
		private void onBlockBreak(BlockBreakEvent e) {
			if (saves.containsKey(e.getBlock())) {
				e.setCancelled(true);
				e.getPlayer().sendMessage("§c투기장§f은 부술 수 없습니다!");
			}
		}

		@EventHandler
		private void onExplode(BlockExplodeEvent e) {
			e.blockList().removeIf(saves::containsKey);
		}

		@EventHandler
		private void onExplode(EntityExplodeEvent e) {
			e.blockList().removeIf(saves::containsKey);
		}

		@Override
		protected void onEnd() {
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			cooldown.start();
			Gladiator.this.arena = null;
			SoundLib.ENTITY_GENERIC_EXPLODE.playSound(center);
			int i = 0;
			for (Entry<Block, IBlockSnapshot> entry : saves.entrySet()) {
				if (++i % 4 == 0) {
					ParticleLib.BLOCK_CRACK.spawnParticle(entry.getKey().getLocation(), .5, .5, .5, 5, entry.getKey());
				}
				entry.getValue().apply();
			}
			saves.clear();
			HandlerList.unregisterAll(this);
		}
	}

	@Override
	public void TargetSkill(Material material, LivingEntity entity) {
		if (material == Material.IRON_INGOT) {
			if (entity instanceof Player) {
				if (!cooldown.isCooldown() && arena == null) {
					new Arena(getPlayer().getLocation().clone().subtract(0, 1, 0), (Player) entity).start();
				}
			} else {
				cooldown.isCooldown();
			}
		}
	}

}
