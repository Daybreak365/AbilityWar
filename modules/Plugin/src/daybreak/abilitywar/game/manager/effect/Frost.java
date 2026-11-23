package daybreak.abilitywar.game.manager.effect;

import com.google.common.collect.MapMaker;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.Effect;
import daybreak.abilitywar.game.AbstractGame.GameTimer;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.effect.registry.ApplicationMethod;
import daybreak.abilitywar.game.manager.effect.registry.EffectManifest;
import daybreak.abilitywar.game.manager.effect.registry.EffectRegistry;
import daybreak.abilitywar.game.manager.effect.registry.EffectRegistry.EffectRegistration;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.minecraft.block.Blocks;
import daybreak.abilitywar.utils.base.minecraft.block.IBlockSnapshot;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Map;
import java.util.WeakHashMap;

public class Frost {

	private static final Map<LivingEntity, Object> frozenEntities = new WeakHashMap<>();

	public static void apply(Participant participant, TimeUnit timeUnit, int duration) {
		registration.apply(participant, timeUnit, duration);
	}

	public static final EffectRegistration<ParticipantFrost> registration = EffectRegistry.registerEffect(ParticipantFrost.class);

	/** Capture both originals before changing a block or granting invulnerability. */
	private static final class FrozenArea {
		private final Block[] blocks;
		private final IBlockSnapshot[] snapshots;
		private final Location teleport;
		private int changedBlocks;
		private boolean grantedInvulnerability;

		private FrozenArea(LivingEntity target) {
			Block head = target.getEyeLocation().getBlock();
			Block feet = head.getRelative(BlockFace.DOWN);
			blocks = new Block[] {head, feet};
			snapshots = new IBlockSnapshot[] {Blocks.createSnapshot(head), Blocks.createSnapshot(feet)};
			teleport = feet.getLocation().add(0.5, 0, 0.5).setDirection(target.getLocation().getDirection());
		}

		private void freeze(LivingEntity target) {
			for (Block block : blocks) {
				changedBlocks++;
				block.setType(Material.ICE);
			}
			if (ServerVersion.getVersion() >= 10 && !target.isInvulnerable()) {
				grantedInvulnerability = true;
				target.setInvulnerable(true);
			}
		}

		private void thaw(LivingEntity target) {
			if (grantedInvulnerability) {
				target.setInvulnerable(false);
				grantedInvulnerability = false;
			}
			while (changedBlocks > 0) {
				snapshots[--changedBlocks].apply();
			}
		}
	}

	@EffectManifest(name = "빙결", displayName = "§b빙결", method = ApplicationMethod.UNIQUE_LONGEST, description = {
			"몸이 얼어붙어 이동이 불가능해지고 대미지를 입지 않습니다.",
			"얼음으로 인해 시야가 차단됩니다."
	})
	public static class ParticipantFrost extends Effect implements Listener {

		private static final Map<Location, ParticipantFrost> frosts = new MapMaker().weakValues().makeMap();

		public static ParticipantFrost getFrost(Location location) {
			return frosts.get(location);
		}

		private final Participant target;
		private final FrozenArea area;
		public boolean noDamage = true;

		public ParticipantFrost(Participant target, TimeUnit timeUnit, int duration) {
			this(target, timeUnit, duration, new FrozenArea(target.getPlayer()));
		}

		private ParticipantFrost(Participant target, TimeUnit timeUnit, int duration, FrozenArea area) {
			target.getGame().super(registration, target, timeUnit.toTicks(duration));
			setPeriod(TimeUnit.TICKS, 1);
			this.target = target;
			this.area = area;
		}

		@EventHandler
		public void onBlockBreak(BlockBreakEvent e) {
			if (e.getBlock().equals(area.blocks[0]) || e.getBlock().equals(area.blocks[1])) e.setCancelled(true);
		}

		@EventHandler
		public void onExplode(BlockExplodeEvent e) {
			e.blockList().removeIf(block -> block.equals(area.blocks[0]) || block.equals(area.blocks[1]));
		}

		@EventHandler
		public void onExplode(EntityExplodeEvent e) {
			e.blockList().removeIf(block -> block.equals(area.blocks[0]) || block.equals(area.blocks[1]));
		}

		@EventHandler
		private void onEntityDamage(EntityDamageEvent e) {
			if (e.getEntity().equals(target.getPlayer()) && noDamage) {
				e.setCancelled(true);
			}
		}

		@EventHandler
		private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
			onEntityDamage(e);
		}

		@EventHandler
		private void onEntityDamageByBlock(EntityDamageByBlockEvent e) {
			onEntityDamage(e);
		}

		@Override
		protected void onStart() {
			try {
				area.freeze(target.getPlayer());
				for (Block block : area.blocks) frosts.put(block.getLocation(), this);
				Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
			} catch (RuntimeException | Error failure) {
				stop(true);
				throw failure;
			}
		}

		@Override
		protected void run(int count) {
			super.run(count);
			target.getPlayer().teleport(area.teleport);
		}

		@Override
		protected void onSilentEnd() {
			super.onSilentEnd();
			HandlerList.unregisterAll(this);
			for (Block block : area.blocks) frosts.remove(block.getLocation(), this);
			area.thaw(target.getPlayer());
		}

	}

	public static void apply(AbstractGame game, LivingEntity livingEntity, TimeUnit timeUnit, int duration) {
		if (game.isParticipating(livingEntity.getUniqueId())) {
			apply(game.getParticipant(livingEntity.getUniqueId()), timeUnit, duration);
		} else if (!frozenEntities.containsKey(livingEntity)) {
			new LivingEntityFrost(game, livingEntity, timeUnit, duration).start();
		}
	}

	private static class LivingEntityFrost extends GameTimer implements Listener {

		private final LivingEntity target;
		private final FrozenArea area;

		private LivingEntityFrost(AbstractGame game, LivingEntity target, TimeUnit timeUnit, int duration) {
			game.super(TaskType.REVERSE, timeUnit.toTicks(duration));
			setPeriod(TimeUnit.TICKS, 1);
			this.target = target;
			this.area = new FrozenArea(target);
		}

		@EventHandler
		public void onBlockBreak(BlockBreakEvent e) {
			if (e.getBlock().equals(area.blocks[0]) || e.getBlock().equals(area.blocks[1])) e.setCancelled(true);
		}

		@EventHandler
		public void onExplode(BlockExplodeEvent e) {
			e.blockList().removeIf(block -> block.equals(area.blocks[0]) || block.equals(area.blocks[1]));
		}

		@EventHandler
		public void onExplode(EntityExplodeEvent e) {
			e.blockList().removeIf(block -> block.equals(area.blocks[0]) || block.equals(area.blocks[1]));
		}

		@EventHandler
		private void onEntityDamage(EntityDamageEvent e) {
			if (e.getEntity().equals(target)) {
				e.setCancelled(true);
			}
		}

		@EventHandler
		private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
			onEntityDamage(e);
		}

		@EventHandler
		private void onEntityDamageByBlock(EntityDamageByBlockEvent e) {
			onEntityDamage(e);
		}

		@Override
		protected void onStart() {
			try {
				frozenEntities.put(target, Object.class);
				area.freeze(target);
				Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
			} catch (RuntimeException | Error failure) {
				stop(true);
				throw failure;
			}
		}

		@Override
		protected void run(int count) {
			target.teleport(area.teleport);
		}

		@Override
		protected void onEnd() {
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			HandlerList.unregisterAll(this);
			frozenEntities.remove(target);
			area.thaw(target);
		}

	}

}
