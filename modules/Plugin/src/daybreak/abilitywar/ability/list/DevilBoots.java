package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.Tips;
import daybreak.abilitywar.ability.Tips.Description;
import daybreak.abilitywar.ability.Tips.Difficulty;
import daybreak.abilitywar.ability.Tips.Level;
import daybreak.abilitywar.ability.Tips.Stats;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.BlockX;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.PotionEffects;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

@AbilityManifest(name = "악마의 부츠", rank = Rank.B, species = Species.OTHERS, explain = {
		"신속하게 이동하며 지나가는 모든 곳에 불이 붙습니다. 화염 피해를 받지 않습니다.",
		"물에 들어가면 축축함 효과가 생기며, 축축함 효과가 있는 도중에는 불이 생기지",
		"않습니다."
}, summarize = {
		"§b신속 버프§f를 얻고 지나간 자리를 §c불태웁니다§f.",
		"§c화염 피해 면역§f이며 §3물§f에 닿으면 효과가 잠시 사라집니다."
})
@Tips(tip = {
		"빠르게 이동해서 상대방을 불태우세요. 항상 물과 얼음은",
		"멀리 하세요."
}, strong = {
		@Description(subject = "빠른 이동", explain = {
				"신속 효과 덕분에 빠르게 이동할 수 있습니다."
		}),
		@Description(subject = "화염 대미지 무시", explain = {
				"화염 대미지를 무시합니다. 용암 대미지는 그대로 받으니",
				"조심하세요!"
		})
}, weak = {
		@Description(subject = "물", explain = {
				"물이 악마의 부츠의 최대 약점입니다. 물을 최대한 피하세요."
		})
}, stats = @Stats(offense = Level.THREE, survival = Level.ZERO, crowdControl = Level.ZERO, mobility = Level.SIX, utility = Level.ZERO), difficulty = Difficulty.EASY)
public class DevilBoots extends AbilityBase {

	public DevilBoots(Participant participant) {
		super(participant);
	}

	private final Wet wet = new Wet(4);

	private final AbilityTimer speed = new AbilityTimer() {
		@Override
		protected void run(int count) {
			PotionEffects.SPEED.addPotionEffect(getPlayer(), 20, 1, true);
		}
	}.register();

	private final Deque<Block> blocks = new LinkedList<>();
	private final Set<Block> blockSet = new HashSet<>();

	@SubscribeEvent(onlyRelevant = true)
	private void onPlayerMove(PlayerMoveEvent e) {
		if (wet.isRunning()) {
			if (BlockX.isWater(e.getTo().getBlock().getType())) {
				wet.reset();
			}
			return;
		}
		final Block to = e.getTo().getBlock(), below = to.getRelative(BlockFace.DOWN);
		if ((to.isEmpty() || to.getType() == Material.SNOW) && below.getType().isSolid()) {
			to.setType(Material.FIRE);
			if (to.getType() == Material.FIRE) {
				blocks.add(to);
				blockSet.add(to);
				if (blocks.size() >= 20) {
					final Block removed = blocks.removeFirst();
					blockSet.remove(removed);
					removed.setType(Material.AIR);
				}
			}
		} else if (BlockX.isWater(to.getType())) {
			wet.start();
			return;
		}
		if (below.getType().equals(Material.SNOW_BLOCK)) {
			below.setType(Material.DIRT);
		} else if (below.getType().equals(Material.PACKED_ICE) || below.getType().equals(Material.ICE) || MaterialX.FROSTED_ICE.compare(below) || MaterialX.BLUE_ICE.compare(below)) {
			below.setType(Material.WATER);
		}
	}

	@SubscribeEvent
	private void onBlockSpread(final BlockSpreadEvent e) {
		if (blockSet.contains(e.getSource()) && e.getNewState().getType() == Material.FIRE) {
			e.setCancelled(true);
		}
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onEntityDamage(EntityDamageEvent e) {
		final DamageCause cause = e.getCause();
		if (cause.equals(DamageCause.FIRE) || cause.equals(DamageCause.FIRE_TICK) || cause.equals(DamageCause.LAVA)) {
			e.setCancelled(true);
		}
	}

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			speed.start();
		} else if (update == Update.ABILITY_DESTROY) {
			for (Block block : blocks) {
				if (block.getType() == Material.FIRE) block.setType(Material.AIR);
			}
			getPlayer().setFireTicks(0);
		}
	}

	private class Wet extends AbilityTimer {

		private final ActionbarChannel actionbarChannel = newActionbarChannel();

		private Wet(final int seconds) {
			super(seconds * 5);
			setPeriod(TimeUnit.TICKS, 4);
		}

		@Override
		protected void onStart() {
			speed.stop(false);
		}

		@Override
		protected void run(int count) {
			actionbarChannel.update("§b축축함§f: " + (getCount() / 5.0) + "초");
		}

		private void reset() {
			setCount(getMaximumCount());
		}

		@Override
		protected void onEnd() {
			onSilentEnd();
			speed.start();
		}

		@Override
		protected void onSilentEnd() {
			actionbarChannel.update(null);
		}
	}

}
